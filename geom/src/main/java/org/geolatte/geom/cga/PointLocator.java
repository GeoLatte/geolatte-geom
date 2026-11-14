package org.geolatte.geom.cga;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.*;

/**
 * Determines the exact location (interior, boundary or exterior) of points relative to a geometry.
 *
 * <p>The geometry has union semantics: it is the union of its elements (points, lines and polygons, also
 * when they are members of a {@code GeometryCollection}). The location of a point is decided by the elements of
 * the highest dimension that contain it:</p>
 * <ol>
 *     <li>polygons: a point in the interior of the union of the polygons is in the interior, also when it lies on
 *     the boundaries of polygons that together cover a neighbourhood of it (e.g. on an edge shared by adjacent
 *     polygons); a point on the boundary of that union is on the boundary;</li>
 *     <li>lines: a point on a line is on the boundary if it is an endpoint of an odd number of lines (the
 *     "Mod-2" rule of the Simple Feature Access specification), and in the interior otherwise;</li>
 *     <li>points: a point equal to a point of the geometry is in the interior.</li>
 * </ol>
 * <p>The individual polygons must be valid. The location is exact for the given double coordinates.</p>
 */
public final class PointLocator {

    private final Segments rings, lines;   // the tag of a ring segment is the index of its ring
    private final int[] ringPolygon;       // the polygon of each ring
    private final Map<XY, Integer> endpoints = new HashMap<>();
    private final Set<XY> points = new HashSet<>();

    /**
     * Creates a locator for the geometry
     *
     * @param geometry the geometry
     * @return the locator
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static PointLocator of(Geometry<?> geometry) {
        return new PointLocator(GeometryElements.of(geometry));
    }

    PointLocator(GeometryElements elements) {
        this.rings = new Segments();
        this.ringPolygon = new int[elements.rings.size()];
        for (int r = 0; r < ringPolygon.length; r++) {
            GeometryElements.Ring ring = elements.rings.get(r);
            ringPolygon[r] = ring.polygon;
            for (int i = 0; i + 1 < ring.xs.length; i++) {
                rings.add(ring.xs[i], ring.ys[i], ring.xs[i + 1], ring.ys[i + 1], r, ring.delta);
            }
        }
        rings.build();
        this.lines = new Segments();
        for (double[][] line : elements.lines) {
            double[] xs = line[0], ys = line[1];
            for (int i = 0; i + 1 < xs.length; i++) {
                lines.add(xs[i], ys[i], xs[i + 1], ys[i + 1], 0, 0);
            }
            endpoints.merge(new XY(xs[0], ys[0]), 1, Integer::sum);
            endpoints.merge(new XY(xs[xs.length - 1], ys[ys.length - 1]), 1, Integer::sum);
        }
        lines.build();
        for (double[] p : elements.points) points.add(new XY(p[0], p[1]));
    }

    /**
     * @return the location of the position (only its X and Y coordinate are used)
     * @throws IllegalArgumentException if the X or Y coordinate is NaN or infinite
     */
    public Location locate(Position position) {
        return locate(position.getCoordinate(0), position.getCoordinate(1));
    }

    /**
     * @return the location of the point (x, y)
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public Location locate(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
        x += 0.0;
        y += 0.0;
        Location area = locateInArea(x, y);
        if (area != Location.EXTERIOR) return area;
        if (isOnLine(x, y)) {
            Integer count = endpoints.get(new XY(x, y));
            return count != null && count % 2 == 1 ? Location.BOUNDARY : Location.INTERIOR;
        }
        return points.contains(new XY(x, y)) ? Location.INTERIOR : Location.EXTERIOR;
    }

    /**
     * Returns the number of polygons that contain the points immediately to the left of (x, y), if (x, y) is the
     * lexicographically smallest vertex of the rings that pass through it, and of all rings connected to those.
     * Such rings do not contain those points, so for each polygon, the parity of the number of crossings of a ray
     * from (x, y) with its other rings decides.
     */
    int countContainingLeft(double x, double y) {
        return scan(x, y).containingLeft;
    }

    /**
     * The location as in {@link GeometryElements#roughLocation}: {@code INSIDE} if the point lies in the interior of
     * a polygon, {@code EXTERIOR} if it is not a point of the geometry, {@code ON} otherwise.
     */
    int roughLocation(double x, double y) {
        x += 0.0;
        y += 0.0;
        if (rings.size() > 0 && scan(x, y).containing > 0) return GeometryElements.INSIDE;
        return locate(x, y) == Location.EXTERIOR ? GeometryElements.EXTERIOR : GeometryElements.ON;
    }

    // the location relative to the union of the polygons
    private Location locateInArea(double x, double y) {
        if (rings.size() == 0) return Location.EXTERIOR;
        Scan scan = scan(x, y);
        if (scan.containing > 0) return Location.INTERIOR;
        if (scan.ends.isEmpty()) return Location.EXTERIOR;
        // the point lies on the boundary of some polygons, and in the exterior of all others: it is in the
        // interior of the union if the polygons cover all sectors around it
        List<End> ends = scan.ends;
        ends.sort(End::compareTo);
        int k = ends.size();
        // the depth of sector 0 (between ends 0 and 1): for each polygon, the sector is covered if the end of the
        // polygon that precedes it (counterclockwise) has the polygon to its left
        Map<Integer, Integer> preceding = new HashMap<>();
        for (int i = 1; i < k; i++) preceding.put(ends.get(i).polygon, i);
        preceding.put(ends.get(0).polygon, 0);
        int depth = 0;
        for (int i : preceding.values()) {
            if (ends.get(i).delta > 0) depth++;
        }
        // the sector between ends with the same direction (edges of different polygons that overlap) is empty, and
        // is not checked
        boolean all = depth > 0 || ends.get(0).compareTo(ends.get(1 % k)) == 0;
        for (int i = 1; i < k; i++) {
            depth += ends.get(i).delta;
            all &= depth > 0 || ends.get(i).compareTo(ends.get((i + 1) % k)) == 0;
        }
        return all ? Location.INTERIOR : Location.BOUNDARY;
    }

    private boolean isOnLine(double x, double y) {
        if (lines.size() == 0) return false;
        boolean[] on = {false};
        lines.index.query(y, i -> {
            if (!on[0] && lines.contains(i, x, y)) on[0] = true;
        });
        return on[0];
    }

    // an edge of a polygon leaving the point, in direction (bx - ax, by - ay); the polygon lies to its left if
    // delta == 1, to its right if delta == -1
    static final class End implements Comparable<End> {
        final double ax, ay, bx, by;
        final int polygon, delta;

        End(double ax, double ay, double bx, double by, int polygon, int delta) {
            this.ax = ax;
            this.ay = ay;
            this.bx = bx;
            this.by = by;
            this.polygon = polygon;
            this.delta = delta;
        }

        @Override
        public int compareTo(End o) {
            return compareDirections(ax, ay, bx, by, o.ax, o.ay, o.bx, o.by);
        }
    }

    /**
     * Compares directions by their angle with the positive X axis, in [0, 2 pi): the direction b - a with the
     * direction d - c. Exact.
     */
    static int compareDirections(double ax, double ay, double bx, double by,
                                 double cx, double cy, double dx, double dy) {
        int h1 = half(ax, ay, bx, by), h2 = half(cx, cy, dx, dy);
        if (h1 != h2) return h1 - h2;
        return -Predicates.crossProductSign(ax, ay, bx, by, cx, cy, dx, dy);
    }

    // 0 if the direction b - a has an angle in [0, pi), 1 if in [pi, 2 pi)
    private static int half(double ax, double ay, double bx, double by) {
        return by > ay || (by == ay && bx > ax) ? 0 : 1;
    }

    private static final class Scan {
        int containing;      // polygons that strictly contain the point (none of their rings passes through it)
        int containingLeft;  // polygons that contain the point, ignoring the rings that pass through it
        final List<End> ends = new ArrayList<>();   // the polygon edges leaving the point
    }

    private Scan scan(double x, double y) {
        Scan scan = new Scan();
        if (rings.size() == 0) return scan;
        int[][] crossings = {new int[8]};   // the rings of the edges crossed by the ray to the right
        int[] count = {0};
        Set<Integer> touching = new HashSet<>();   // the rings that pass through the point
        rings.index.query(y, i -> {
            double ux = rings.x0[i], uy = rings.y0[i], vx = rings.x1[i], vy = rings.y1[i];
            int ring = rings.tag[i], polygon = ringPolygon[ring], delta = rings.delta[i];
            if (rings.contains(i, x, y)) {
                touching.add(ring);
                if (!(x == vx && y == vy)) scan.ends.add(new End(ux, uy, vx, vy, polygon, delta));
                if (!(x == ux && y == uy)) scan.ends.add(new End(vx, vy, ux, uy, polygon, -delta));
                return;
            }
            boolean crosses = uy <= y
                    ? vy > y && Predicates.orientation(ux, uy, vx, vy, x, y) > 0
                    : vy <= y && Predicates.orientation(ux, uy, vx, vy, x, y) < 0;
            if (crosses) {
                if (count[0] == crossings[0].length) crossings[0] = Arrays.copyOf(crossings[0], 2 * count[0]);
                crossings[0][count[0]++] = ring;
            }
        });
        return finish(scan, crossings[0], count[0], touching);
    }

    private Scan finish(Scan scan, int[] crossings, int n, Set<Integer> touching) {
        // the parity of each polygon: the sum of the parities of its rings that do not pass through the point
        Arrays.sort(crossings, 0, n);
        Map<Integer, Boolean> odd = new HashMap<>();
        for (int i = 0; i < n; ) {
            int j = i;
            while (j < n && crossings[j] == crossings[i]) j++;
            if ((j - i) % 2 == 1 && !touching.contains(crossings[i])) {
                odd.merge(ringPolygon[crossings[i]], true, (u, v) -> !u);
            }
            i = j;
        }
        Set<Integer> touchingPolygons = new HashSet<>();
        for (int ring : touching) touchingPolygons.add(ringPolygon[ring]);
        for (Map.Entry<Integer, Boolean> e : odd.entrySet()) {
            if (!e.getValue()) continue;
            scan.containingLeft++;
            if (!touchingPolygons.contains(e.getKey())) scan.containing++;
        }
        return scan;
    }

    /**
     * Segments with an index on their Y range
     */
    static final class Segments {
        double[] x0 = new double[16], y0 = new double[16], x1 = new double[16], y1 = new double[16];
        int[] tag = new int[16], delta = new int[16];
        private int n = 0;
        IntervalIndex index;

        void add(double ax, double ay, double bx, double by, int tag, int delta) {
            if (n == x0.length) {
                x0 = Arrays.copyOf(x0, 2 * n);
                y0 = Arrays.copyOf(y0, 2 * n);
                x1 = Arrays.copyOf(x1, 2 * n);
                y1 = Arrays.copyOf(y1, 2 * n);
                this.tag = Arrays.copyOf(this.tag, 2 * n);
                this.delta = Arrays.copyOf(this.delta, 2 * n);
            }
            x0[n] = ax;
            y0[n] = ay;
            x1[n] = bx;
            y1[n] = by;
            this.tag[n] = tag;
            this.delta[n] = delta;
            n++;
        }

        void build() {
            double[] lo = new double[n], hi = new double[n];
            for (int i = 0; i < n; i++) {
                lo[i] = Math.min(y0[i], y1[i]);
                hi[i] = Math.max(y0[i], y1[i]);
            }
            index = new IntervalIndex(lo, hi);
        }

        int size() {
            return n;
        }

        // does segment i contain the point?
        boolean contains(int i, double x, double y) {
            if (x < Math.min(x0[i], x1[i]) || x > Math.max(x0[i], x1[i])) return false;
            if (y < Math.min(y0[i], y1[i]) || y > Math.max(y0[i], y1[i])) return false;
            return Predicates.orientation(x0[i], y0[i], x1[i], y1[i], x, y) == 0;
        }
    }

    /**
     * Exact coordinates as a hash key (-0.0 must have been replaced by 0.0)
     */
    static final class XY {
        final double x, y;

        XY(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof XY)) return false;
            XY other = (XY) o;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            long h = Double.doubleToLongBits(x) * 31 + Double.doubleToLongBits(y);
            return (int) (h ^ (h >>> 32));
        }
    }
}
