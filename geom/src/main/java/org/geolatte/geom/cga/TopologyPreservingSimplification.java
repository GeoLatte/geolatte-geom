package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Interval;
import org.geolatte.geom.cga.exact.Predicates;
import org.locationtech.jts.geom.Envelope;

import java.util.*;

/**
 * Topology preserving Douglas-Peucker simplification; see
 * {@link DouglasPeucker#simplify(Geometry, double, boolean)} for the guarantees.
 *
 * <p>The geometry is flattened into components: its lines and rings, and its points (as degenerate segments). All
 * current segments of all components are kept in a spatial index. The components are simplified in order, with
 * the same algorithm as {@link DouglasPeucker#keptPositions(PositionSequence, double)}, but a section
 * {@code C = p_i ... p_k} that is within the tolerance is only replaced by the segment {@code s = (p_i, p_k)}
 * if, for every other segment {@code t} in the index (including the remaining segments of the same component):</p>
 * <ol>
 *     <li>{@code t} has no point in common with {@code s} or with {@code C}, other than {@code p_i} or
 *     {@code p_k}. Then the intersection of {@code t} with the line is the same before and after the
 *     replacement;</li>
 *     <li>{@code t} does not lie in the region bounded by {@code C} and {@code s}: the winding number of
 *     {@code C} followed by {@code s} (reversed) around the endpoints of {@code t} is zero. By 1, {@code t} does
 *     not meet the boundary of the region (except at {@code p_i} or {@code p_k}), so it suffices to test one
 *     endpoint. As the winding number of any closed line around {@code t} changes by exactly that winding number,
 *     no component moves from the inside to the outside of a ring, or vice versa.</li>
 * </ol>
 * <p>The region lies within distance {@code tolerance} of {@code s} (the convex capsule around {@code s} contains
 * all of {@code C}), which is used as a cheap exact prefilter for the winding number test. A component that has
 * no point at all in common with {@code C} or {@code s} lies entirely inside or outside the region, so it is
 * tested only once.</p>
 */
final class TopologyPreservingSimplification {

    private final double tolerance;
    private final Interval tolerance2;
    private final List<Component> components = new ArrayList<>();
    private SegmentIndex index;

    private TopologyPreservingSimplification(double tolerance) {
        this.tolerance = tolerance;
        this.tolerance2 = Interval.product(tolerance, tolerance);
    }

    static <P extends Position> Geometry<P> simplify(Geometry<P> geometry, double tolerance) {
        TopologyPreservingSimplification simplification = new TopologyPreservingSimplification(tolerance);
        simplification.collect(geometry, false);
        simplification.buildIndex();
        for (Component c : simplification.components) {
            simplification.simplify(c);
        }
        return simplification.rebuild(geometry, simplification.components.iterator());
    }

    // ---------------------------------------------------------------------------------------------------------
    // flattening and rebuilding
    // ---------------------------------------------------------------------------------------------------------

    static final class Component {
        final PositionSequence<?> positions;
        final double[] xs, ys;
        final boolean[] keep;
        final boolean ring;
        final boolean point;
        int count;   // the number of kept positions
        Segment[] segments;  // the original segments, segments[m] = (m, m + 1)

        Component(PositionSequence<?> positions, boolean ring, boolean point) {
            this.positions = positions;
            int n = positions.size();
            this.xs = new double[n];
            this.ys = new double[n];
            DouglasPeucker.coordinates(positions, xs, ys);
            this.keep = new boolean[n];
            java.util.Arrays.fill(keep, true);
            this.count = n;
            this.ring = ring;
            this.point = point;
        }
    }

    static final class Segment {
        final Component component;
        final int start, end;
        final Envelope envelope;

        Segment(Component component, int start, int end) {
            this.component = component;
            this.start = start;
            this.end = end;
            this.envelope = new Envelope(component.xs[start], component.xs[end], component.ys[start], component.ys[end]);
        }

        // is this one of the segments of the section (i, k) of the component?
        boolean isIn(Component c, int i, int k) {
            return component == c && start >= i && end <= k && start < end;
        }
    }

    private void collect(Geometry<?> geometry, boolean ring) {
        if (geometry.isEmpty()) return;
        if (geometry instanceof Point) {
            add(new Component(geometry.getPositions(), false, true));
        } else if (geometry instanceof LineString) {
            add(new Component(geometry.getPositions(), ring || geometry instanceof LinearRing, false));
        } else if (geometry instanceof Polygon) {
            Polygon<?> polygon = (Polygon<?>) geometry;
            collect(polygon.getExteriorRing(), true);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                collect(polygon.getInteriorRingN(i), true);
            }
        } else if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                collect(collection.getGeometryN(i), false);
            }
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }
    }

    private void add(Component c) {
        components.add(c);
        int n = c.xs.length;
        c.segments = new Segment[c.point ? 1 : n - 1];
        for (int m = 0; m < c.segments.length; m++) {
            c.segments[m] = new Segment(c, m, c.point ? m : m + 1);
        }
    }

    private void buildIndex() {
        Envelope extent = new Envelope();
        int count = 0;
        for (Component c : components) {
            for (Segment s : c.segments) extent.expandToInclude(s.envelope);
            count += c.segments.length;
        }
        index = new SegmentIndex(extent, count);
        for (Component c : components) {
            for (Segment s : c.segments) index.insert(s);
        }
    }

    // mirrors collect()
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <P extends Position> Geometry<P> rebuild(Geometry<P> geometry, Iterator<Component> it) {
        if (geometry.isEmpty()) return geometry;
        if (geometry instanceof Point) {
            it.next();
            return geometry;
        }
        if (geometry instanceof LineString) {
            Component c = it.next();
            PositionSequence<P> positions = DouglasPeucker.kept((PositionSequence<P>) c.positions, c.keep);
            if (positions == geometry.getPositions()) return geometry;
            return geometry instanceof LinearRing
                    ? new LinearRing<>(positions, geometry.getCoordinateReferenceSystem())
                    : new LineString<>(positions, geometry.getCoordinateReferenceSystem());
        }
        if (geometry instanceof Polygon) {
            Polygon<P> polygon = (Polygon<P>) geometry;
            List<LinearRing<P>> rings = new ArrayList<>();
            boolean changed = false;
            LinearRing<P> shell = polygon.getExteriorRing();
            rings.add((LinearRing<P>) rebuild(shell, it));
            changed |= rings.get(0) != shell;
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                LinearRing<P> hole = polygon.getInteriorRingN(i);
                rings.add((LinearRing<P>) rebuild(hole, it));
                changed |= rings.get(rings.size() - 1) != hole;
            }
            return changed ? Geometries.mkPolygon(rings) : polygon;
        }
        AbstractGeometryCollection<P, ?> collection = (AbstractGeometryCollection<P, ?>) geometry;
        List members = new ArrayList<>();
        boolean changed = false;
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            Geometry<P> member = collection.getGeometryN(i);
            Geometry<P> simplified = rebuild(member, it);
            members.add(simplified);
            changed |= simplified != member;
        }
        if (!changed) return geometry;
        if (geometry instanceof MultiPoint) return geometry;
        if (geometry instanceof MultiLineString) return Geometries.mkMultiLineString(members);
        if (geometry instanceof MultiPolygon) return Geometries.mkMultiPolygon(members);
        return Geometries.mkGeometryCollection(members);
    }

    // ---------------------------------------------------------------------------------------------------------
    // simplification
    // ---------------------------------------------------------------------------------------------------------

    private void simplify(Component c) {
        int n = c.xs.length;
        if (c.point || n < 3) return;
        double[] lo = new double[n], hi = new double[n];
        int[] stack = new int[2 * n];
        int top = 0;
        stack[top++] = 0;
        stack[top++] = n - 1;
        while (top > 0) {
            int last = stack[--top];
            int first = stack[--top];
            if (last - first < 2) continue;
            int farthest = DouglasPeucker.farthestPosition(c.xs, c.ys, first, last, lo, hi);
            if (DouglasPeucker.isWithinTolerance(c.xs, c.ys, first, last, farthest, lo, hi, tolerance, tolerance2)
                    && replaceIfTopologyPreserved(c, first, last)) {
                continue;
            }
            // the parts are processed from left to right (depth first), so that the result is deterministic
            stack[top++] = farthest;
            stack[top++] = last;
            stack[top++] = first;
            stack[top++] = farthest;
        }
    }

    // replaces the section (i, k) of the component by the segment (p_i, p_k) if that preserves the topology. All
    // positions between i and k are still present.
    private boolean replaceIfTopologyPreserved(Component c, int i, int k) {
        double[] xs = c.xs, ys = c.ys;
        if (c.ring && c.count - (k - i - 1) < 4) return false;
        if (xs[i] == xs[k] && ys[i] == ys[k]) return false;
        Segment replacement = new Segment(c, i, k);

        // 1. contacts with the new segment, and with the section
        Envelope sectionEnvelope = new Envelope(replacement.envelope);
        for (int m = i + 1; m < k; m++) {
            sectionEnvelope.expandToInclude(xs[m], ys[m]);
        }
        // components that touch the section or the new segment (at p_i or p_k)
        Set<Component> touching = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Segment> candidates = query(sectionEnvelope, c, i, k);
        for (Segment t : candidates) {
            if (t.envelope.intersects(replacement.envelope) && !checkContact(c, i, k, i, k, t, touching)) return false;
        }
        if (!checkSectionContacts(c, i, k, candidates, touching)) return false;

        // 2. no other segment inside the region between the section and the new segment. A component that does not
        // touch the section or the new segment is connected and does not meet the boundary of the region, so it lies
        // entirely inside or outside the region, and a single test suffices.
        Set<Component> tested = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Segment t : candidates) {
            Component tc = t.component;
            if (!touching.contains(tc) && !tested.add(tc)) continue;
            int e = isEndpoint(c, i, k, tc.xs[t.start], tc.ys[t.start]) ? t.end : t.start;
            double ex = tc.xs[e], ey = tc.ys[e];
            if (isEndpoint(c, i, k, ex, ey)) continue;
            if (Predicates.compareDistanceToSegment(xs[i], ys[i], xs[k], ys[k], ex, ey, tolerance) > 0) continue;
            if (windingNumber(c, i, k, ex, ey) != 0) return false;
        }

        // replace
        for (int m = i; m < k; m++) {
            index.remove(c.segments[m]);
        }
        index.insert(replacement);
        for (int m = i + 1; m < k; m++) {
            c.keep[m] = false;
        }
        c.count -= k - i - 1;
        return true;
    }

    // checks the contacts of the segments of the section (i, k) with the candidates, for the pairs whose envelopes
    // intersect. These pairs are found by a sweep in the X direction (querying the index for each segment of the
    // section is much slower).
    private static boolean checkSectionContacts(Component c, int i, int k, List<Segment> candidates,
                                                Set<Component> touching) {
        Segment[] section = Arrays.copyOfRange(c.segments, i, k);
        Segment[] others = candidates.toArray(new Segment[0]);
        Arrays.sort(section, BY_MIN_X);
        Arrays.sort(others, BY_MIN_X);
        List<Segment> activeSection = new ArrayList<>(), activeOthers = new ArrayList<>();
        int a = 0, b = 0;
        while (a < section.length || b < others.length) {
            boolean fromSection = b == others.length
                    || (a < section.length && section[a].envelope.getMinX() <= others[b].envelope.getMinX());
            Segment next = fromSection ? section[a++] : others[b++];
            List<Segment> active = fromSection ? activeOthers : activeSection;
            for (int j = active.size() - 1; j >= 0; j--) {
                Segment o = active.get(j);
                if (o.envelope.getMaxX() < next.envelope.getMinX()) {
                    // no later segment overlaps o in X
                    active.set(j, active.get(active.size() - 1));
                    active.remove(active.size() - 1);
                } else if (o.envelope.intersects(next.envelope)) {
                    Segment sectionSegment = fromSection ? next : o, other = fromSection ? o : next;
                    if (!checkContact(c, i, k, sectionSegment.start, sectionSegment.end, other, touching)) {
                        return false;
                    }
                }
            }
            (fromSection ? activeSection : activeOthers).add(next);
        }
        return true;
    }

    private static final Comparator<Segment> BY_MIN_X = Comparator.comparingDouble(s -> s.envelope.getMinX());

    // the indexed segments whose envelope intersects the envelope, except those of the section (i, k)
    private List<Segment> query(Envelope envelope, Component c, int i, int k) {
        List<Segment> result = new ArrayList<>();
        index.query(envelope, t -> {
            if (!t.isIn(c, i, k)) result.add(t);
        });
        return result;
    }

    // false if segment t has a point other than p_i or p_k in common with the segment (p_a, p_b) of the
    // component. If it has p_i or p_k in common, its component is added to touching.
    private static boolean checkContact(Component c, int i, int k, int a, int b, Segment t, Set<Component> touching) {
        double[] xs = c.xs, ys = c.ys;
        double[] txs = t.component.xs, tys = t.component.ys;
        double t0x = txs[t.start], t0y = tys[t.start], t1x = txs[t.end], t1y = tys[t.end];
        switch (Predicates.segmentIntersection(xs[a], ys[a], xs[b], ys[b], t0x, t0y, t1x, t1y)) {
            case DISJOINT:
                return true;
            case TOUCHING:
                // the single common point is p_i or p_k if that point lies on both segments
                if (isOnBoth(xs[i], ys[i], xs[a], ys[a], xs[b], ys[b], t0x, t0y, t1x, t1y)
                        || isOnBoth(xs[k], ys[k], xs[a], ys[a], xs[b], ys[b], t0x, t0y, t1x, t1y)) {
                    touching.add(t.component);
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private static boolean isOnBoth(double px, double py, double ax, double ay, double bx, double by,
                                    double cx, double cy, double dx, double dy) {
        return isOn(px, py, ax, ay, bx, by) && isOn(px, py, cx, cy, dx, dy);
    }

    private static boolean isOn(double px, double py, double ax, double ay, double bx, double by) {
        return Predicates.segmentIntersection(px, py, px, py, ax, ay, bx, by) != SegmentIntersection.DISJOINT;
    }

    private static boolean isEndpoint(Component c, int i, int k, double x, double y) {
        return (x == c.xs[i] && y == c.ys[i]) || (x == c.xs[k] && y == c.ys[k]);
    }

    /**
     * The winding number around (x, y) of the closed line p_i, ..., p_k, p_i; the point must not lie on that line.
     * Edges are counted when they cross the horizontal line through the point, to the right of the point (Sunday's
     * algorithm, with the exact orientation test).
     */
    private static int windingNumber(Component c, int i, int k, double x, double y) {
        double[] xs = c.xs, ys = c.ys;
        int winding = 0;
        for (int m = i; m <= k; m++) {
            int next = m < k ? m + 1 : i;
            double ux = xs[m], uy = ys[m], vx = xs[next], vy = ys[next];
            if (uy <= y) {
                if (vy > y && Predicates.orientation(ux, uy, vx, vy, x, y) > 0) winding++;
            } else {
                if (vy <= y && Predicates.orientation(ux, uy, vx, vy, x, y) < 0) winding--;
            }
        }
        return winding;
    }

    /**
     * A dynamic spatial index of segments: a hierarchy of uniform grids (a "loose" quadtree without a tree). A
     * segment is stored in the cells it overlaps at the finest level whose cell size is at least the extent of the
     * segment, so it is stored in at most 2 x 2 cells, and a query only returns segments from cells near the query
     * envelope. (The JTS {@code Quadtree} stores a segment that crosses the midlines of a node in that node, so that
     * every query returns all segments crossing the midlines of the large nodes.)
     */
    static final class SegmentIndex {

        private static final double MAX_CELL = 0x1p30;

        // All computations use halved coordinates (v / 2 - origin / 2), which can not overflow. Halving is monotone,
        // and so is the cell index function: that is all that is needed for correctness; rounding errors and
        // clamping only affect efficiency.
        private final double x0, y0;  // halved origin
        private final double h0;      // halved cell size of level 0
        private final List<Map<Long, List<Segment>>> levels = new ArrayList<>();

        SegmentIndex(Envelope extent, int count) {
            this.x0 = extent.isNull() ? 0 : 0.5 * extent.getMinX();
            this.y0 = extent.isNull() ? 0 : 0.5 * extent.getMinY();
            // a cell size at which the segments would be evenly spread over the cells
            double w = extent.isNull() ? 0 : halfDifference(extent.getMaxX(), extent.getMinX());
            double h = extent.isNull() ? 0 : halfDifference(extent.getMaxY(), extent.getMinY());
            int n = Math.max(count, 1);
            double size = w > 0 && h > 0 ? Math.sqrt(w / n) * Math.sqrt(h) : Math.max(w, h) / n;
            this.h0 = size > 0 ? size : 1;
        }

        void insert(Segment s) {
            int level = level(s.envelope);
            while (levels.size() <= level) levels.add(new HashMap<>());
            Map<Long, List<Segment>> grid = levels.get(level);
            double h = cellSize(level);
            for (long cx = cell(s.envelope.getMinX(), x0, h); cx <= cell(s.envelope.getMaxX(), x0, h); cx++) {
                for (long cy = cell(s.envelope.getMinY(), y0, h); cy <= cell(s.envelope.getMaxY(), y0, h); cy++) {
                    grid.computeIfAbsent(key(cx, cy), key -> new ArrayList<>(4)).add(s);
                }
            }
        }

        void remove(Segment s) {
            int level = level(s.envelope);
            Map<Long, List<Segment>> grid = levels.get(level);
            double h = cellSize(level);
            for (long cx = cell(s.envelope.getMinX(), x0, h); cx <= cell(s.envelope.getMaxX(), x0, h); cx++) {
                for (long cy = cell(s.envelope.getMinY(), y0, h); cy <= cell(s.envelope.getMaxY(), y0, h); cy++) {
                    Long key = key(cx, cy);
                    List<Segment> cell = grid.get(key);
                    cell.remove(s);
                    if (cell.isEmpty()) grid.remove(key);
                }
            }
        }

        /**
         * Passes each segment whose envelope intersects the envelope to the consumer, once.
         */
        void query(Envelope envelope, java.util.function.Consumer<Segment> consumer) {
            for (int level = 0; level < levels.size(); level++) {
                Map<Long, List<Segment>> grid = levels.get(level);
                if (grid.isEmpty()) continue;
                double h = cellSize(level);
                long qx0 = cell(envelope.getMinX(), x0, h), qx1 = cell(envelope.getMaxX(), x0, h);
                long qy0 = cell(envelope.getMinY(), y0, h), qy1 = cell(envelope.getMaxY(), y0, h);
                if ((qx1 - qx0 + 1) * (qy1 - qy0 + 1) > grid.size()) {
                    for (Map.Entry<Long, List<Segment>> e : grid.entrySet()) {
                        long key = e.getKey();
                        report(e.getValue(), envelope, key >> 32, (int) key, qx0, qy0, h, consumer);
                    }
                } else {
                    for (long cx = qx0; cx <= qx1; cx++) {
                        for (long cy = qy0; cy <= qy1; cy++) {
                            List<Segment> cell = grid.get(key(cx, cy));
                            if (cell != null) report(cell, envelope, cx, cy, qx0, qy0, h, consumer);
                        }
                    }
                }
            }
        }

        // reports the segments of cell (cx, cy) that intersect the envelope. A segment that is stored in several
        // cells is only reported in the first cell (smallest cx and cy) that it shares with the query.
        private void report(List<Segment> cell, Envelope envelope, long cx, long cy, long qx0, long qy0, double h,
                            java.util.function.Consumer<Segment> consumer) {
            for (Segment s : cell) {
                if (!s.envelope.intersects(envelope)) continue;
                if (cx == Math.max(cell(s.envelope.getMinX(), x0, h), qx0)
                        && cy == Math.max(cell(s.envelope.getMinY(), y0, h), qy0)) {
                    consumer.accept(s);
                }
            }
        }

        // the smallest level whose cell size is at least the (halved) extent of the envelope
        private int level(Envelope e) {
            double extent = Math.max(halfDifference(e.getMaxX(), e.getMinX()), halfDifference(e.getMaxY(), e.getMinY()));
            if (extent <= h0) return 0;
            int level = Math.max(0, Math.getExponent(extent) - Math.getExponent(h0));
            while (cellSize(level) < extent) level++;
            while (level > 0 && cellSize(level - 1) >= extent) level--;
            return level;
        }

        // the halved cell size of a level
        private double cellSize(int level) {
            double h = Math.scalb(h0, level);
            return h < Double.MAX_VALUE ? h : Double.MAX_VALUE;
        }

        // the cell index of a coordinate, given the halved origin and cell size; monotone in v, so that the cells
        // of intersecting envelopes overlap
        private static long cell(double v, double origin, double h) {
            double c = Math.floor((0.5 * v - origin) / h);
            return (long) Math.max(-MAX_CELL, Math.min(MAX_CELL, c));
        }

        private static double halfDifference(double a, double b) {
            return 0.5 * a - 0.5 * b;
        }

        private static long key(long cx, long cy) {
            return (cx << 32) | (cy & 0xffffffffL);
        }
    }
}
