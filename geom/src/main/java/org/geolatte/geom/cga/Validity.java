package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.PointLocator.XY;
import org.geolatte.geom.cga.exact.Constructions;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.*;

/**
 * Exact test of the validity of a geometry (whether it is well formed), as defined by the Simple Feature Access
 * specification and SQL/MM ({@code ST_IsValid}). Only the X and Y coordinates are used.
 *
 * <ul>
 *     <li>All coordinates must be finite.</li>
 *     <li>A line string must have at least two distinct positions.</li>
 *     <li>A linear ring must be closed, have at least four positions (three distinct ones) and be simple.</li>
 *     <li>A polygon: its rings are valid linear rings; two rings meet at most in single points, where they do not
 *     cross; each hole lies inside the exterior ring, and not inside another hole; and the interior is connected
 *     (the rings that touch each other do not enclose a part of it).</li>
 *     <li>A multipolygon: its polygons are valid, their interiors do not intersect, and their boundaries meet at
 *     most in single points, where they do not cross.</li>
 *     <li>A multipoint, multi line string or geometry collection is valid if its elements are valid.</li>
 *     <li>An empty geometry is valid.</li>
 * </ul>
 *
 * <p>The result is exact for the given double coordinates. Segment intersections are classified with exact
 * predicates; the rings that meet always meet in a position of one of them, so no intersection point is computed.
 * The order of the edges of the rings around such a position is decided exactly (see
 * {@link Predicates#crossProductSign}), as is the nesting of the rings.</p>
 *
 * <p>The reported location of a problem is a position of the geometry, except for a proper crossing of two
 * segments, whose location is the correctly rounded crossing point.</p>
 */
public final class Validity {

    /**
     * The reasons why a geometry is not valid
     */
    public enum Problem {
        /** The geometry is valid */
        NONE,
        /** A coordinate is NaN or infinite */
        INVALID_COORDINATE,
        /** A line string has fewer than two distinct positions, or a ring fewer than three */
        TOO_FEW_POINTS,
        /** A ring is not closed */
        RING_NOT_CLOSED,
        /** A ring passes through the same point twice */
        RING_SELF_INTERSECTION,
        /** Two rings cross, overlap, or meet in more than one point of a segment */
        SELF_INTERSECTION,
        /** A hole lies outside the exterior ring of its polygon */
        HOLE_OUTSIDE_SHELL,
        /** A hole lies inside another hole of the same polygon */
        NESTED_HOLES,
        /** The interiors of polygons of a multipolygon intersect */
        NESTED_SHELLS,
        /** The interior of a polygon is not connected */
        DISCONNECTED_INTERIOR
    }

    private static final Validity VALID = new Validity(Problem.NONE, Double.NaN, Double.NaN);

    private final Problem problem;
    private final double x, y;

    private Validity(Problem problem, double x, double y) {
        this.problem = problem;
        this.x = x;
        this.y = y;
    }

    /**
     * @return true if the geometry is valid
     */
    public static boolean isValid(Geometry<?> geometry) {
        return of(geometry).isValid();
    }

    /**
     * @return the validity of the geometry, with the first problem found if it is not valid
     */
    public static Validity of(Geometry<?> geometry) {
        if (geometry.isEmpty()) return VALID;
        if (geometry instanceof Point) {
            double[][] c = coordinates(geometry.getPositions());
            return finite(c) ? VALID : new Validity(Problem.INVALID_COORDINATE, Double.NaN, Double.NaN);
        }
        if (geometry instanceof LinearRing) {
            List<double[][]> rings = new ArrayList<>(1);
            Validity v = addRing(geometry.getPositions(), rings);
            if (v != VALID) return v;
            double[][] c = rings.get(0);
            Validity[] error = {VALID};
            SegmentContacts.find(rings, (a, i, b, j, kind) -> {
                if (!Simplicity.selfContact(c, true, i, j, kind)) return false;
                error[0] = at(Problem.RING_SELF_INTERSECTION, contactPoint(c, i, c, j, kind));
                return true;
            });
            return error[0];
        }
        if (geometry instanceof LineString) {
            double[][] c = coordinates(geometry.getPositions());
            if (!finite(c)) return new Validity(Problem.INVALID_COORDINATE, Double.NaN, Double.NaN);
            c = GeometryElements.distinct(c[0], c[1]);
            return c[0].length >= 2 ? VALID : new Validity(Problem.TOO_FEW_POINTS, c[0][0], c[1][0]);
        }
        if (geometry instanceof Polygon) {
            return polygonal(Collections.singletonList((Polygon<?>) geometry));
        }
        if (geometry instanceof MultiPolygon) {
            MultiPolygon<?> multi = (MultiPolygon<?>) geometry;
            List<Polygon<?>> polygons = new ArrayList<>();
            for (int i = 0; i < multi.getNumGeometries(); i++) polygons.add(multi.getGeometryN(i));
            return polygonal(polygons);
        }
        if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                Validity v = of(collection.getGeometryN(i));
                if (!v.isValid()) return v;
            }
            return VALID;
        }
        throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
    }

    /**
     * @return true if the geometry is valid
     */
    public boolean isValid() {
        return problem == Problem.NONE;
    }

    /**
     * @return the problem, {@link Problem#NONE} if the geometry is valid
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * @return the X coordinate of the location of the problem; NaN if the geometry is valid, or for an invalid
     * coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * @return the Y coordinate of the location of the problem; NaN if the geometry is valid, or for an invalid
     * coordinate
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return isValid() ? "Valid" : problem + " at (" + x + ", " + y + ")";
    }

    // ---------------------------------------------------------------------------------------------------------
    // coordinates
    // ---------------------------------------------------------------------------------------------------------

    private static Validity at(Problem problem, double[] p) {
        return new Validity(problem, p[0], p[1]);
    }

    private static double[][] coordinates(PositionSequence<?> positions) {
        int n = positions.size();
        double[] xs = new double[n], ys = new double[n];
        double[] buffer = new double[Math.max(2, positions.getCoordinateDimension())];
        for (int i = 0; i < n; i++) {
            positions.getCoordinates(i, buffer);
            xs[i] = buffer[0];
            ys[i] = buffer[1];
        }
        return new double[][]{xs, ys};
    }

    private static boolean finite(double[][] c) {
        for (int i = 0; i < c[0].length; i++) {
            if (!Double.isFinite(c[0][i]) || !Double.isFinite(c[1][i])) return false;
        }
        return true;
    }

    // checks the coordinates of a ring, and adds them without repeated consecutive positions
    private static Validity addRing(PositionSequence<?> positions, List<double[][]> rings) {
        double[][] c = coordinates(positions);
        if (!finite(c)) return new Validity(Problem.INVALID_COORDINATE, Double.NaN, Double.NaN);
        int n = c[0].length;
        if (c[0][0] != c[0][n - 1] || c[1][0] != c[1][n - 1]) {
            return new Validity(Problem.RING_NOT_CLOSED, c[0][0] + 0.0, c[1][0] + 0.0);
        }
        c = GeometryElements.distinct(c[0], c[1]);
        if (c[0].length < 4) return new Validity(Problem.TOO_FEW_POINTS, c[0][0], c[1][0]);
        rings.add(c);
        return VALID;
    }

    /**
     * A point where segments i of chain a and j of chain b meet: the correctly rounded crossing point of a proper
     * crossing, otherwise an endpoint of one of them that lies on the other
     */
    private static double[] contactPoint(double[][] a, int i, double[][] b, int j, SegmentIntersection kind) {
        double ax = a[0][i], ay = a[1][i], bx = a[0][i + 1], by = a[1][i + 1];
        double cx = b[0][j], cy = b[1][j], dx = b[0][j + 1], dy = b[1][j + 1];
        if (kind == SegmentIntersection.CROSSING) return Constructions.crossingPoint(ax, ay, bx, by, cx, cy, dx, dy);
        if (SegmentContacts.onSegment(cx, cy, dx, dy, ax, ay)) return new double[]{ax, ay};
        if (SegmentContacts.onSegment(cx, cy, dx, dy, bx, by)) return new double[]{bx, by};
        if (SegmentContacts.onSegment(ax, ay, bx, by, cx, cy)) return new double[]{cx, cy};
        return new double[]{dx, dy};
    }

    // ---------------------------------------------------------------------------------------------------------
    // polygons
    // ---------------------------------------------------------------------------------------------------------

    /**
     * A point where rings touch, with the segments (ring, segment) that pass through it
     */
    private static final class Touch {
        final double x, y;
        final Set<Long> segments = new LinkedHashSet<>();

        Touch(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void add(int ring, int segment) {
            segments.add(((long) ring << 32) | segment);
        }
    }

    /**
     * A direction from a touch point (x, y) towards (tx, ty), along an edge of a ring
     */
    private static final class Ray {
        final int ring;
        final double tx, ty;

        Ray(int ring, double tx, double ty) {
            this.ring = ring;
            this.tx = tx;
            this.ty = ty;
        }
    }

    private static Validity polygonal(List<? extends Polygon<?>> polygons) {
        List<double[][]> rings = new ArrayList<>();
        List<Integer> polygonOf = new ArrayList<>();
        List<Boolean> isShell = new ArrayList<>();
        for (int p = 0; p < polygons.size(); p++) {
            Polygon<?> polygon = polygons.get(p);
            if (polygon.isEmpty()) continue;
            for (int r = 0; r <= polygon.getNumInteriorRing(); r++) {
                LinearRing<?> ring = r == 0 ? polygon.getExteriorRing() : polygon.getInteriorRingN(r - 1);
                Validity v = addRing(ring.getPositions(), rings);
                if (v != VALID) return v;
                polygonOf.add(p);
                isShell.add(r == 0);
            }
        }
        // the segments of the rings that meet: only rings of different polygons, or a hole and a shell or two
        // holes, may meet, and only in isolated points
        Map<XY, Touch> touches = new LinkedHashMap<>();
        Validity[] error = {VALID};
        SegmentContacts contacts = new SegmentContacts(rings);
        contacts.find((a, i, b, j, kind) -> {
            double[][] ca = rings.get(a), cb = rings.get(b);
            if (a == b) {
                if (!Simplicity.selfContact(ca, true, i, j, kind)) return false;
                error[0] = at(Problem.RING_SELF_INTERSECTION, contactPoint(ca, i, cb, j, kind));
                return true;
            }
            double[] p = contactPoint(ca, i, cb, j, kind);
            if (kind != SegmentIntersection.TOUCHING) {
                error[0] = at(Problem.SELF_INTERSECTION, p);
                return true;
            }
            Touch t = touches.computeIfAbsent(new XY(p[0], p[1]), k -> new Touch(p[0], p[1]));
            t.add(a, i);
            t.add(b, j);
            return false;
        });
        if (!error[0].isValid()) return error[0];
        for (Touch t : touches.values()) {
            Validity v = checkCrossing(t, rings);
            if (!v.isValid()) return v;
        }
        Validity v = checkNesting(rings, polygonOf, isShell, touches, contacts);
        if (!v.isValid()) return v;
        return checkConnected(rings.size(), polygonOf, touches);
    }

    /**
     * Rings that touch in a point must not cross there: around the point, the two edges of each ring must not be
     * separated by a single edge of another ring
     */
    private static Validity checkCrossing(Touch t, List<double[][]> rings) {
        double x = t.x, y = t.y;
        List<Ray> rays = new ArrayList<>();
        for (long key : t.segments) {
            int ring = (int) (key >>> 32), segment = (int) key;
            double[][] c = rings.get(ring);
            double x0 = c[0][segment], y0 = c[1][segment], x1 = c[0][segment + 1], y1 = c[1][segment + 1];
            if (!(x0 == x && y0 == y)) rays.add(new Ray(ring, x0, y0));
            if (!(x1 == x && y1 == y)) rays.add(new Ray(ring, x1, y1));
        }
        Map<Integer, Integer> count = new HashMap<>();
        for (Ray r : rays) count.merge(r.ring, 1, Integer::sum);
        for (int c : count.values()) {
            if (c != 2) return new Validity(Problem.RING_SELF_INTERSECTION, x, y);
        }
        // no two rays have the same direction: that would be an overlap
        rays.sort((r1, r2) -> PointLocator.compareDirections(x, y, r1.tx, r1.ty, x, y, r2.tx, r2.ty));
        Deque<Integer> open = new ArrayDeque<>();
        Set<Integer> seen = new HashSet<>();
        for (Ray r : rays) {
            if (seen.add(r.ring)) {
                open.push(r.ring);
            } else if (!open.isEmpty() && open.peek() == r.ring) {
                open.pop();
            } else {
                return new Validity(Problem.SELF_INTERSECTION, x, y);
            }
        }
        return VALID;
    }

    /**
     * The rings no longer cross or overlap, so each ring lies inside or outside each other ring. Then the number
     * of rings of each polygon that contain a point (as interior minus holes: the winding number) must be 0 or 1,
     * and its sum over the polygons too. These numbers change only across the rings, and are the same on each side
     * of a ring along all of it. So it is enough to check them on both sides of each ring, at one of its vertices
     * that does not lie on another ring.
     */
    private static Validity checkNesting(List<double[][]> rings, List<Integer> polygonOf, List<Boolean> isShell,
                                         Map<XY, Touch> touches, SegmentContacts segments) {
        int count = rings.size();
        double[] minX = new double[count], minY = new double[count], maxX = new double[count], maxY = new double[count];
        for (int r = 0; r < count; r++) {
            double[][] c = rings.get(r);
            minX[r] = minY[r] = Double.POSITIVE_INFINITY;
            maxX[r] = maxY[r] = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < c[0].length; i++) {
                minX[r] = Math.min(minX[r], c[0][i]);
                maxX[r] = Math.max(maxX[r], c[0][i]);
                minY[r] = Math.min(minY[r], c[1][i]);
                maxY[r] = Math.max(maxY[r], c[1][i]);
            }
        }
        RingBoxes boxes = new RingBoxes(maxX, new EnvelopeTree(minX, minY, maxX, maxY));
        // holes first, so that a hole outside its shell is reported as such
        for (int pass = 0; pass < 2; pass++) {
            for (int r = 0; r < count; r++) {
                if (isShell.get(r) != (pass == 1)) continue;
                Validity v = checkNesting(r, rings, polygonOf, isShell, touches, segments, boxes);
                if (!v.isValid()) return v;
            }
        }
        return VALID;
    }

    private static final class RingBoxes {
        final double[] maxX;
        final EnvelopeTree tree;

        RingBoxes(double[] maxX, EnvelopeTree tree) {
            this.maxX = maxX;
            this.tree = tree;
        }
    }

    private static Validity checkNesting(int ring, List<double[][]> rings, List<Integer> polygonOf,
                                         List<Boolean> isShell, Map<XY, Touch> touches, SegmentContacts segments,
                                         RingBoxes boxes) {
        double[][] c = rings.get(ring);
        int n = c[0].length - 1;
        int v = 0;
        while (v < n && touches.containsKey(new XY(c[0][v], c[1][v]))) v++;
        if (v == n) v = 0;   // all vertices touch other rings: the rings through v are decided by direction
        double vx = c[0][v], vy = c[1][v], dx = c[0][v + 1], dy = c[1][v + 1];
        // the other rings whose bounding box contains (vx, vy)
        Set<Integer> candidates = new HashSet<>();
        double[] rayEnd = {vx};
        boxes.tree.query(vx, vy, vx, vy, r -> {
            if (r == ring) return;
            candidates.add(r);
            rayEnd[0] = Math.max(rayEnd[0], boxes.maxX[r]);
        });
        // the candidates that contain (vx, vy), by the parity of the crossings of a ray to the right
        Map<Integer, Boolean> odd = new HashMap<>();
        Map<Integer, List<Integer>> through = new HashMap<>();
        if (!candidates.isEmpty()) {
            segments.query(vx, vy, rayEnd[0], vy, (other, s) -> {
                if (!candidates.contains(other)) return;
                double[][] t = rings.get(other);
                double ux = t[0][s], uy = t[1][s], wx = t[0][s + 1], wy = t[1][s + 1];
                if (SegmentContacts.onSegment(ux, uy, wx, wy, vx, vy)) {
                    through.computeIfAbsent(other, k -> new ArrayList<>(2)).add(s);
                    return;
                }
                boolean crosses = uy <= vy
                        ? wy > vy && Predicates.orientation(ux, uy, wx, wy, vx, vy) > 0
                        : wy <= vy && Predicates.orientation(ux, uy, wx, wy, vx, vy) < 0;
                if (crosses) odd.merge(other, true, (p, q) -> !p);
            });
        }
        Set<Integer> containing = new HashSet<>();
        for (Map.Entry<Integer, Boolean> e : odd.entrySet()) {
            if (e.getValue() && !through.containsKey(e.getKey())) containing.add(e.getKey());
        }
        // a ring through (vx, vy): ring lies on one side of it, as its first edge from (vx, vy) does
        for (Map.Entry<Integer, List<Integer>> e : through.entrySet()) {
            if (insideNear(rings.get(e.getKey()), e.getValue(), vx, vy, dx, dy)) containing.add(e.getKey());
        }
        // per polygon: the number of shells and holes that contain the ring
        Map<Integer, int[]> count = new HashMap<>();
        for (int other : containing) {
            count.computeIfAbsent(polygonOf.get(other), k -> new int[2])[isShell.get(other) ? 0 : 1]++;
        }
        int polygon = polygonOf.get(ring);
        int[] own = count.getOrDefault(polygon, new int[2]);
        if (!isShell.get(ring)) {
            if (own[0] == 0) return new Validity(Problem.HOLE_OUTSIDE_SHELL, vx, vy);
            if (own[1] > 0) return new Validity(Problem.NESTED_HOLES, vx, vy);
        }
        // the winding numbers outside and inside the ring
        int outside = 0;
        for (int[] k : count.values()) {
            int w = k[0] - k[1];
            if (w < 0 || w > 1) return new Validity(Problem.NESTED_SHELLS, vx, vy);
            outside += w;
        }
        int ownInside = own[0] - own[1] + (isShell.get(ring) ? 1 : -1);
        int inside = outside - (own[0] - own[1]) + ownInside;
        if (ownInside < 0 || ownInside > 1 || outside > 1 || inside > 1) {
            return new Validity(Problem.NESTED_SHELLS, vx, vy);
        }
        return VALID;
    }

    /**
     * For a ring that passes through v, with the given segments containing v: is the start of the segment from v
     * towards d inside the ring? The segment does not overlap the ring, so it starts inside or outside it.
     */
    private static boolean insideNear(double[][] t, List<Integer> segs, double vx, double vy, double dx, double dy) {
        // the ring enters v from (px, py) and leaves it towards (nx, ny)
        double px = 0, py = 0, nx = 0, ny = 0;
        if (segs.size() == 1) {
            int s = segs.get(0);
            px = t[0][s];
            py = t[1][s];
            nx = t[0][s + 1];
            ny = t[1][s + 1];
        } else {
            for (int s : segs) {
                if (t[0][s + 1] == vx && t[1][s + 1] == vy) {
                    px = t[0][s];
                    py = t[1][s];
                } else {
                    nx = t[0][s + 1];
                    ny = t[1][s + 1];
                }
            }
        }
        // the interior of a counterclockwise ring lies to the left: counterclockwise from the outgoing edge to the
        // incoming one
        boolean ccw = Predicates.ringOrientation(t[0], t[1], t[0].length) > 0;
        return ccw ? inWedge(vx, vy, nx, ny, px, py, dx, dy) : inWedge(vx, vy, px, py, nx, ny, dx, dy);
    }

    // does direction d - v lie strictly inside the counterclockwise sweep from direction f - v to direction g - v?
    private static boolean inWedge(double vx, double vy, double fx, double fy, double gx, double gy,
                                   double dx, double dy) {
        int fg = Predicates.crossProductSign(vx, vy, fx, fy, vx, vy, gx, gy);
        int fd = Predicates.crossProductSign(vx, vy, fx, fy, vx, vy, dx, dy);
        int dg = Predicates.crossProductSign(vx, vy, dx, dy, vx, vy, gx, gy);
        if (fg > 0) return fd > 0 && dg > 0;
        if (fg < 0) return fd >= 0 || dg >= 0;   // not inside the (convex) sweep from g to f
        return fd > 0;   // f and g have opposite directions (equal directions would be an overlap)
    }

    /**
     * The interior of a polygon is connected if its rings and the points where they touch form no cycle: a ring,
     * or a chain of rings, that touches another in two points encloses a part of the interior
     */
    private static Validity checkConnected(int ringCount, List<Integer> polygonOf, Map<XY, Touch> touches) {
        int size = ringCount;
        for (Touch t : touches.values()) size += t.segments.size();
        int[] parent = new int[size];
        for (int i = 0; i < size; i++) parent[i] = i;
        int next = ringCount;
        for (Touch t : touches.values()) {
            Map<Integer, Set<Integer>> byPolygon = new HashMap<>();
            for (long key : t.segments) {
                int ring = (int) (key >>> 32);
                byPolygon.computeIfAbsent(polygonOf.get(ring), k -> new LinkedHashSet<>()).add(ring);
            }
            for (Set<Integer> group : byPolygon.values()) {
                if (group.size() < 2) continue;
                int node = next++;
                for (int ring : group) {
                    int a = find(parent, node), b = find(parent, ring);
                    if (a == b) return new Validity(Problem.DISCONNECTED_INTERIOR, t.x, t.y);
                    parent[a] = b;
                }
            }
        }
        return VALID;
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }
}
