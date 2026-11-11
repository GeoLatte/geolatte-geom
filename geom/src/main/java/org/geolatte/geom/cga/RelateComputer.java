package org.geolatte.geom.cga;

import org.geolatte.geom.SegmentIntersection;
import org.geolatte.geom.cga.exact.Constructions;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.*;

/**
 * Computes the DE-9IM matrix of two geometries, exactly and with union semantics; see {@link Relate}.
 *
 * <p>The computation builds the planar arrangement of all segments of both geometries:</p>
 * <ol>
 *     <li><b>Nodes</b> are all vertices and points, and all points where segments meet. A crossing point of two
 *     segments is in general not representable; it is identified by the pair of segments, and ordered along a
 *     segment with exact predicates ({@link Predicates#orientationOfCrossing}). Its correctly rounded value is
 *     only used as a hash key to find coinciding nodes, which are then compared exactly.</li>
 *     <li><b>Edges</b> are the parts of segments between consecutive nodes. Coinciding parts of different segments
 *     (overlaps) become one edge. Each edge records, for each geometry, whether a line runs along it and the net
 *     number of polygons whose interior lies to its left minus those to its right.</li>
 *     <li>The edges around each node are sorted by angle (with {@link Predicates#crossProductSign}), and for each
 *     geometry the <b>depth</b> (the number of its polygons covering a face) is propagated around nodes and along
 *     edges. The depth of one face of each connected component is computed from the lexicographically smallest
 *     vertex v of the component: the face around v in direction (-1, 0) is outside all polygons of the component,
 *     so its depth is the number of polygons (of other components) that contain v.</li>
 * </ol>
 * <p>Nodes, edges, and the faces next to the edges partition the plane into cells, in each of which the location
 * relative to each geometry is constant. The matrix entry of the locations of a cell is at least the dimension of
 * the cell.</p>
 */
final class RelateComputer {

    static final int A = 0, B = 1;
    static final int POINT = 0, LINE = 1, RING = 2;

    private final GeometryElements[] elements;
    private final List<Seg> segments = new ArrayList<>();
    private final List<Node> vertices = new ArrayList<>();
    private final Map<Long, Node> nodes = new HashMap<>();
    private final List<Node> allNodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Long, Edge> sharedEdges = new HashMap<>();
    int inconsistencies = 0;   // edges that received different depths from their two ends (invalid input)

    RelateComputer(GeometryElements a, GeometryElements b) {
        this.elements = new GeometryElements[]{a, b};
    }

    IntersectionMatrix compute() {
        addElements(elements[A], A);
        addElements(elements[B], B);
        findContacts();
        buildEdges();
        for (Node n : allNodes) n.sortStar();
        label();
        return matrix();
    }

    // ---------------------------------------------------------------------------------------------------------
    // the arrangement
    // ---------------------------------------------------------------------------------------------------------

    static final class Seg {
        final int geom, kind, polygon, delta;
        final double x0, y0, x1, y1;
        final double minX, maxX, minY, maxY;
        final Node n0, n1;
        List<Incidence> incidences;   // nodes in the interior of the segment
        boolean overlapping;          // overlaps another segment

        Seg(int geom, int kind, int polygon, int delta, Node n0, Node n1) {
            this.geom = geom;
            this.kind = kind;
            this.polygon = polygon;
            this.delta = delta;
            this.n0 = n0;
            this.n1 = n1;
            this.x0 = n0.x;
            this.y0 = n0.y;
            this.x1 = n1.x;
            this.y1 = n1.y;
            this.minX = Math.min(x0, x1);
            this.maxX = Math.max(x0, x1);
            this.minY = Math.min(y0, y1);
            this.maxY = Math.max(y0, y1);
        }

        void addIncidence(Node node, Seg other) {
            if (node == n0 || node == n1) return;
            if (incidences == null) incidences = new ArrayList<>(2);
            incidences.add(new Incidence(node, other));
        }

        // does the (non-degenerate) segment contain the point?
        boolean contains(double x, double y) {
            return minX <= x && x <= maxX && minY <= y && y <= maxY
                    && Predicates.orientation(x0, y0, x1, y1, x, y) == 0;
        }

        // for a point on the line of the segment: does it lie strictly between the endpoints?
        boolean strictlyInside(double x, double y) {
            return x0 != x1 ? minX < x && x < maxX : minY < y && y < maxY;
        }
    }

    /**
     * A node in the interior of a segment: either an exact point (other == null), or the crossing point with the
     * segment other.
     */
    static final class Incidence {
        final Node node;
        final Seg other;

        Incidence(Node node, Seg other) {
            this.node = node;
            this.other = other;
        }
    }

    static final class Node {
        final int id;
        final double x, y;       // exact coordinates, or the rounded crossing point if not exact
        final boolean exact;
        final Seg s, t;          // the crossing segments, if not exact
        Node next;               // the next node with the same hash key
        final List<End> star = new ArrayList<>(2);
        final int[] endpoints = new int[2];      // number of line endpoints at the node, per geometry
        final boolean[] point = new boolean[2];  // a point of the geometry lies at the node
        final boolean[] areaAll = new boolean[2], areaAny = new boolean[2];
        boolean labeled, queued;
        End pending;             // the end through which a queued node was reached

        Node(int id, double x, double y, boolean exact, Seg s, Seg t) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.exact = exact;
            this.s = s;
            this.t = t;
        }

        void sortStar() {
            if (star.size() > 1) star.sort(End::compareTo);
            for (int i = 0; i < star.size(); i++) star.get(i).index = i;
        }
    }

    static final class Edge {
        final Node n0, n1;
        final double ax, ay, bx, by;   // the direction b - a points from n0 to n1
        final int[] delta = new int[2];
        final boolean[] line = new boolean[2];
        final int[] left = new int[2], right = new int[2];
        boolean labeled;

        Edge(Node n0, Node n1, double ax, double ay, double bx, double by) {
            this.n0 = n0;
            this.n1 = n1;
            this.ax = ax;
            this.ay = ay;
            this.bx = bx;
            this.by = by;
        }

        void add(Seg s, int sign) {
            if (s.kind == LINE) line[s.geom] = true;
            else delta[s.geom] += sign * s.delta;
        }
    }

    /**
     * An edge leaving a node
     */
    static final class End implements Comparable<End> {
        final Edge edge;
        final boolean forward;   // leaves from edge.n0
        int index;               // the position in the sorted star

        End(Edge edge, boolean forward) {
            this.edge = edge;
            this.forward = forward;
        }

        int deltaOut(int g) {
            return forward ? edge.delta[g] : -edge.delta[g];
        }

        Node far() {
            return forward ? edge.n1 : edge.n0;
        }

        End reverse() {
            List<End> star = far().star;
            for (End e : star) {
                if (e.edge == edge && e.forward != forward) return e;
            }
            throw new IllegalStateException();
        }

        @Override
        public int compareTo(End o) {
            Edge e = edge, f = o.edge;
            return forward
                    ? (o.forward ? PointLocator.compareDirections(e.ax, e.ay, e.bx, e.by, f.ax, f.ay, f.bx, f.by)
                    : PointLocator.compareDirections(e.ax, e.ay, e.bx, e.by, f.bx, f.by, f.ax, f.ay))
                    : (o.forward ? PointLocator.compareDirections(e.bx, e.by, e.ax, e.ay, f.ax, f.ay, f.bx, f.by)
                    : PointLocator.compareDirections(e.bx, e.by, e.ax, e.ay, f.bx, f.by, f.ax, f.ay));
        }

        // 0 if the direction has an angle in [0, pi), 1 otherwise
        int half() {
            double fx = forward ? edge.bx : edge.ax, fy = forward ? edge.by : edge.ay;
            double sx = forward ? edge.ax : edge.bx, sy = forward ? edge.ay : edge.by;
            return fy > sy || (fy == sy && fx > sx) ? 0 : 1;
        }
    }

    private void addElements(GeometryElements e, int g) {
        for (double[] p : e.points) {
            Node n = vertexNode(p[0], p[1]);
            n.point[g] = true;
            segments.add(new Seg(g, POINT, -1, 0, n, n));
        }
        for (double[][] line : e.lines) {
            double[] xs = line[0], ys = line[1];
            Node first = addChain(xs, ys, g, LINE, -1, 0);
            Node last = vertexNode(xs[xs.length - 1], ys[ys.length - 1]);
            first.endpoints[g]++;
            last.endpoints[g]++;
        }
        for (GeometryElements.Ring ring : e.rings) {
            addChain(ring.xs, ring.ys, g, RING, ring.polygon, ring.delta);
        }
    }

    private Node addChain(double[] xs, double[] ys, int g, int kind, int polygon, int delta) {
        Node first = vertexNode(xs[0], ys[0]), previous = first;
        for (int i = 1; i < xs.length; i++) {
            Node next = vertexNode(xs[i], ys[i]);
            segments.add(new Seg(g, kind, polygon, delta, previous, next));
            previous = next;
        }
        return first;
    }

    private static long key(double x, double y) {
        long h = Double.doubleToLongBits(x) * 0x9E3779B97F4A7C15L + Double.doubleToLongBits(y);
        return h ^ (h >>> 29);
    }

    private Node vertexNode(double x, double y) {
        long key = key(x, y);
        Node head = nodes.get(key);
        for (Node n = head; n != null; n = n.next) {
            if (n.exact && n.x == x && n.y == y) return n;
        }
        Node n = newNode(key, head, x, y, true, null, null);
        vertices.add(n);
        return n;
    }

    private Node newNode(long key, Node head, double x, double y, boolean exact, Seg s, Seg t) {
        Node n = new Node(allNodes.size(), x, y, exact, s, t);
        n.next = head;
        nodes.put(key, n);
        allNodes.add(n);
        return n;
    }

    // the node of the crossing point of s and t, whose correctly rounded value is (x, y)
    private Node crossingNode(Seg s, Seg t, double x, double y) {
        x += 0.0;
        y += 0.0;
        long key = key(x, y);
        Node head = nodes.get(key);
        for (Node n = head; n != null; n = n.next) {
            if (n.x == x && n.y == y && isCrossing(n, s, t)) return n;
        }
        // the crossing point is exact if the rounded value lies on both segments
        boolean exact = Predicates.orientation(s.x0, s.y0, s.x1, s.y1, x, y) == 0
                && Predicates.orientation(t.x0, t.y0, t.x1, t.y1, x, y) == 0;
        return newNode(key, head, x, y, exact, exact ? null : s, exact ? null : t);
    }

    // is the node the crossing point of s and t?
    private static boolean isCrossing(Node n, Seg s, Seg t) {
        if (n.exact) {
            return Predicates.orientation(s.x0, s.y0, s.x1, s.y1, n.x, n.y) == 0
                    && Predicates.orientation(t.x0, t.y0, t.x1, t.y1, n.x, n.y) == 0;
        }
        if ((n.s == s && n.t == t) || (n.s == t && n.t == s)) return true;
        return onLine(n.s, s, t) && onLine(n.t, s, t);
    }

    // does the crossing point of s and t lie on the line through l?
    private static boolean onLine(Seg l, Seg s, Seg t) {
        if (l == s || l == t) return true;
        return Predicates.orientationOfCrossing(l.x0, l.y0, l.x1, l.y1,
                s.x0, s.y0, s.x1, s.y1, t.x0, t.y0, t.x1, t.y1) == 0;
    }

    // all pairs of segments with intersecting bounding boxes, by a sweep in the X direction
    private void findContacts() {
        Seg[] sorted = segments.toArray(new Seg[0]);
        Arrays.sort(sorted, Comparator.comparingDouble(s -> s.minX));
        List<Seg> active = new ArrayList<>();
        for (Seg s : sorted) {
            for (int j = active.size() - 1; j >= 0; j--) {
                Seg t = active.get(j);
                if (t.maxX < s.minX) {
                    active.set(j, active.get(active.size() - 1));
                    active.remove(active.size() - 1);
                } else if (t.minY <= s.maxY && s.minY <= t.maxY && !skip(s, t)) {
                    contact(s, t);
                }
            }
            active.add(s);
        }
    }

    // pairs of points: they meet only if they are the same node
    private static boolean skip(Seg s, Seg t) {
        return s.kind == POINT && t.kind == POINT;
    }

    private void contact(Seg s, Seg t) {
        SegmentIntersection kind = Predicates.segmentIntersection(s.x0, s.y0, s.x1, s.y1, t.x0, t.y0, t.x1, t.y1);
        switch (kind) {
            case DISJOINT:
                return;
            case CROSSING: {
                double[] xy = Constructions.crossingPoint(s.x0, s.y0, s.x1, s.y1, t.x0, t.y0, t.x1, t.y1);
                Node n = crossingNode(s, t, xy[0], xy[1]);
                s.addIncidence(n, n.exact ? null : t);
                t.addIncidence(n, n.exact ? null : s);
                return;
            }
            case TOUCHING:
                touching(s, t);
                return;
            default:
                // OVERLAPPING: the endpoints of each segment that lie inside the other are nodes on the other
                s.overlapping = true;
                t.overlapping = true;
                if (s.strictlyInside(t.x0, t.y0)) s.addIncidence(t.n0, null);
                if (s.strictlyInside(t.x1, t.y1)) s.addIncidence(t.n1, null);
                if (t.strictlyInside(s.x0, s.y0)) t.addIncidence(s.n0, null);
                if (t.strictlyInside(s.x1, s.y1)) t.addIncidence(s.n1, null);
        }
    }

    // the segments have a single point in common, which is an endpoint of one of them
    private static void touching(Seg s, Seg t) {
        if (s.n0 == t.n0 || s.n0 == t.n1 || s.n1 == t.n0 || s.n1 == t.n1) return;
        if (t.kind != POINT) {
            if (t.contains(s.x0, s.y0)) {
                t.addIncidence(s.n0, null);
                return;
            }
            if (t.contains(s.x1, s.y1)) {
                t.addIncidence(s.n1, null);
                return;
            }
        }
        if (s.kind != POINT) {
            if (s.contains(t.x0, t.y0)) {
                s.addIncidence(t.n0, null);
                return;
            }
            if (s.contains(t.x1, t.y1)) s.addIncidence(t.n1, null);
        }
    }

    private void buildEdges() {
        for (Seg s : segments) {
            if (s.kind == POINT) continue;
            Node previous = s.n0;
            if (s.incidences != null) {
                List<Incidence> incidences = s.incidences;
                incidences.sort((i1, i2) -> compareAlong(s, i1, i2));
                for (Incidence i : incidences) {
                    if (i.node == previous) continue;
                    addPiece(s, previous, i.node);
                    previous = i.node;
                }
            }
            addPiece(s, previous, s.n1);
        }
    }

    private void addPiece(Seg s, Node u, Node v) {
        long key = u.id < v.id ? ((long) u.id << 32) | v.id : ((long) v.id << 32) | u.id;
        if (s.overlapping) {
            Edge e = sharedEdges.get(key);
            if (e != null) {
                e.add(s, e.n0 == u ? 1 : -1);
                return;
            }
        }
        Edge e = new Edge(u, v, s.x0, s.y0, s.x1, s.y1);
        e.add(s, 1);
        edges.add(e);
        u.star.add(new End(e, true));
        v.star.add(new End(e, false));
        if (s.overlapping) sharedEdges.put(key, e);
    }

    // the order of two nodes in the interior of s, in the direction from (x0, y0) to (x1, y1)
    static int compareAlong(Seg s, Incidence i1, Incidence i2) {
        if (i1.node == i2.node) return 0;
        if (i1.other == null && i2.other == null) {
            int c = s.x0 != s.x1 ? Double.compare(i1.node.x, i2.node.x) : Double.compare(i1.node.y, i2.node.y);
            boolean increasing = s.x0 != s.x1 ? s.x0 < s.x1 : s.y0 < s.y1;
            return increasing ? c : -c;
        }
        if (i1.other == null) return vertexBeforeCrossing(s, i1.node, i2.other);
        if (i2.other == null) return -vertexBeforeCrossing(s, i2.node, i1.other);
        // the crossing point with t1 comes first iff it lies on the same side of the line of t2 as (x0, y0)
        Seg t1 = i1.other, t2 = i2.other;
        int o = Predicates.orientationOfCrossing(t2.x0, t2.y0, t2.x1, t2.y1,
                s.x0, s.y0, s.x1, s.y1, t1.x0, t1.y0, t1.x1, t1.y1);
        int start = Predicates.orientation(t2.x0, t2.y0, t2.x1, t2.y1, s.x0, s.y0);
        return o == start ? -1 : (o == -start ? 1 : 0);
    }

    // -1 if the exact node v on s comes before the crossing point of s and t; 1 if after
    private static int vertexBeforeCrossing(Seg s, Node v, Seg t) {
        int o = Predicates.orientation(t.x0, t.y0, t.x1, t.y1, v.x, v.y);
        int start = Predicates.orientation(t.x0, t.y0, t.x1, t.y1, s.x0, s.y0);
        return o == start ? -1 : (o == -start ? 1 : 0);
    }

    // ---------------------------------------------------------------------------------------------------------
    // depth labelling
    // ---------------------------------------------------------------------------------------------------------

    private void label() {
        vertices.sort((u, v) -> u.x != v.x ? Double.compare(u.x, v.x) : Double.compare(u.y, v.y));
        PointLocator[] locators = new PointLocator[2];
        for (int g = 0; g < 2; g++) {
            if (elements[g].polygonCount > 0) locators[g] = new PointLocator(elements[g]);
        }
        Deque<Node> queue = new ArrayDeque<>();
        int[] depth = new int[2];
        for (Node v : vertices) {
            if (v.labeled) continue;
            // v is the lexicographically smallest vertex of a new component: no edge leaves it in a direction
            // with negative X, and the sector containing direction (-1, 0) is outside all polygons of the
            // component
            for (int g = 0; g < 2; g++) {
                depth[g] = locators[g] == null ? 0 : locators[g].countContainingLeft(v.x, v.y);
            }
            int k = v.star.size();
            int upper = 0;
            for (End e : v.star) upper += 1 - e.half();
            labelNode(v, k == 0 ? -1 : (upper - 1 + k) % k, depth, queue);
            while (!queue.isEmpty()) {
                Node n = queue.poll();
                End end = n.pending;
                for (int g = 0; g < 2; g++) depth[g] = end.forward ? end.edge.left[g] : end.edge.right[g];
                labelNode(n, end.index, depth, queue);
            }
        }
    }

    // labels the edges around the node, given the depths of the sector that follows end number sector
    private void labelNode(Node n, int sector, int[] depth, Deque<Node> queue) {
        n.labeled = true;
        int k = n.star.size();
        if (k == 0) {
            for (int g = 0; g < 2; g++) n.areaAll[g] = n.areaAny[g] = depth[g] > 0;
            return;
        }
        int[][] sectors = new int[2][k];
        for (int g = 0; g < 2; g++) {
            int[] s = sectors[g];
            s[sector] = depth[g];
            for (int j = 1; j < k; j++) {
                int i = (sector + j) % k;
                s[i] = s[(i + k - 1) % k] + n.star.get(i).deltaOut(g);
            }
            boolean all = true, any = false;
            for (int d : s) {
                all &= d > 0;
                any |= d > 0;
            }
            n.areaAll[g] = all;
            n.areaAny[g] = any;
        }
        for (int i = 0; i < k; i++) {
            End end = n.star.get(i);
            Edge e = end.edge;
            int previous = (i + k - 1) % k;
            if (e.labeled) {
                for (int g = 0; g < 2; g++) {
                    int leftOut = end.forward ? e.left[g] : e.right[g];
                    int rightOut = end.forward ? e.right[g] : e.left[g];
                    if (leftOut != sectors[g][i] || rightOut != sectors[g][previous]) inconsistencies++;
                }
                continue;
            }
            for (int g = 0; g < 2; g++) {
                int leftOut = sectors[g][i], rightOut = sectors[g][previous];
                e.left[g] = end.forward ? leftOut : rightOut;
                e.right[g] = end.forward ? rightOut : leftOut;
            }
            e.labeled = true;
            Node far = end.far();
            if (!far.labeled && !far.queued) {
                far.queued = true;
                far.pending = end.reverse();
                queue.add(far);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // locations and matrix
    // ---------------------------------------------------------------------------------------------------------

    private static final int I = 0, BD = 1, E = 2;

    private static int nodeLocation(Node n, int g) {
        if (n.areaAll[g]) return I;
        if (n.areaAny[g]) return BD;
        boolean onLine = false;
        for (End end : n.star) onLine |= end.edge.line[g];
        if (onLine) return n.endpoints[g] % 2 == 1 ? BD : I;
        return n.point[g] ? I : E;
    }

    private static int edgeLocation(Edge e, int g) {
        boolean l = e.left[g] > 0, r = e.right[g] > 0;
        if (l && r) return I;
        if (l || r) return BD;
        return e.line[g] ? I : E;
    }

    private static int faceLocation(int depth) {
        return depth > 0 ? I : E;
    }

    private IntersectionMatrix matrix() {
        int[] m = new int[9];
        Arrays.fill(m, IntersectionMatrix.FALSE);
        m[3 * E + E] = 2;
        for (Node n : allNodes) {
            set(m, nodeLocation(n, A), nodeLocation(n, B), 0);
        }
        for (Edge e : edges) {
            set(m, edgeLocation(e, A), edgeLocation(e, B), 1);
            set(m, faceLocation(e.left[A]), faceLocation(e.left[B]), 2);
            set(m, faceLocation(e.right[A]), faceLocation(e.right[B]), 2);
        }
        return new IntersectionMatrix(m, elements[A].dimension, elements[B].dimension);
    }

    private static void set(int[] m, int a, int b, int dimension) {
        m[3 * a + b] = Math.max(m[3 * a + b], dimension);
    }
}
