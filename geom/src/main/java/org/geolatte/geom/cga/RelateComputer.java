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
 *     <li><b>Nodes</b> are the points where elements meet: points, line endpoints, and all points where segments
 *     that are not consecutive in a line or ring meet; and, to start the labelling, the lexicographically
 *     smallest vertex of each line and ring. A crossing point of two segments is in general not representable;
 *     it is identified by the pair of segments, and ordered along a segment with exact predicates
 *     ({@link Predicates#orientationOfCrossing}). Its correctly rounded value is only used as a hash key to find
 *     coinciding nodes, which are then compared exactly.</li>
 *     <li><b>Edges</b> are the parts of lines and rings between consecutive nodes: polylines, whose other vertices
 *     touch nothing. Coinciding parts of different segments (overlaps) become one edge; the endpoints of
 *     overlapping segments are nodes, so such an edge is part of a single segment. Each edge records, for each
 *     geometry, whether a line runs along it and the net number of polygons whose interior lies to its left minus
 *     those to its right.</li>
 *     <li>The edges around each node are sorted by the direction of their first segment (with
 *     {@link Predicates#crossProductSign}), and for each geometry the <b>depth</b> (the number of its polygons
 *     covering a face) is propagated around nodes and along edges. The depth of one face of each connected
 *     component is computed from the lexicographically smallest vertex v of the component, which is a node: the
 *     face around v in direction (-1, 0) is outside all polygons of the component, so its depth is the number of
 *     polygons (of other components) that contain v.</li>
 * </ol>
 * <p>The segments that meet are found with monotone chains (runs of segments with directions in the same
 * quadrant) in an {@link EnvelopeTree}: segments of the same monotone chain only meet in a common vertex.</p>
 * <p>Nodes, edges, and the faces next to the edges partition the plane into cells, in each of which the location
 * relative to each geometry is constant. The matrix entry of the locations of a cell is at least the dimension of
 * the cell.</p>
 */
final class RelateComputer {

    static final int A = 0, B = 1;
    static final int POINT = 0, LINE = 1, RING = 2;

    private final GeometryElements[] elements;
    private final List<Chain> chains = new ArrayList<>();
    private final List<Seg> points = new ArrayList<>();
    private final List<Node> vertices = new ArrayList<>();
    private final Map<Long, Node> nodes = new HashMap<>();
    private final List<Node> allNodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Long, Edge> sharedEdges = new HashMap<>();
    private int[][] sectors = new int[2][8];
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

    /**
     * A line, or a ring of a polygon
     */
    static final class Chain {
        final int geom, kind, polygon, delta;
        final double[] xs, ys;     // a ring is closed: its last position is the first one
        final boolean closed;
        final int vertexCount;     // distinct vertices: those of a ring exclude the closing position
        final Node[] nodes;        // the node at each vertex, null if the vertex is not a node
        final Seg[] segs;          // created when a segment meets another one
        final boolean[] turnsBack; // the segments before and after the vertex overlap
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        Chain(int geom, int kind, int polygon, int delta, double[] xs, double[] ys) {
            this.geom = geom;
            this.kind = kind;
            this.polygon = polygon;
            this.delta = delta;
            this.xs = xs;
            this.ys = ys;
            this.closed = kind == RING;
            this.vertexCount = closed ? xs.length - 1 : xs.length;
            this.nodes = new Node[vertexCount];
            this.segs = new Seg[xs.length - 1];
            this.turnsBack = new boolean[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                minX = Math.min(minX, xs[i]);
                maxX = Math.max(maxX, xs[i]);
                minY = Math.min(minY, ys[i]);
                maxY = Math.max(maxY, ys[i]);
                if (!closed && (i == 0 || i == vertexCount - 1)) continue;
                int previous = i > 0 ? i - 1 : vertexCount - 1;
                turnsBack[i] = turnsBack(xs[previous], ys[previous], xs[i], ys[i], xs[i + 1], ys[i + 1]);
            }
        }

        // for consecutive segments (p, q) and (q, r): do they overlap?
        private static boolean turnsBack(double px, double py, double qx, double qy, double rx, double ry) {
            // they can only overlap if (p - q) . (r - q) > 0; most turns are decided by a floating point filter
            double d1 = (px - qx) * (rx - qx), d2 = (py - qy) * (ry - qy);
            if (d1 + d2 < -(1e-15 * (Math.abs(d1) + Math.abs(d2)) + Double.MIN_NORMAL)) return false;
            return Predicates.orientation(px, py, qx, qy, rx, ry) == 0
                    && Predicates.dotSign(qx, qy, px, py, qx, qy, rx, ry) > 0;
        }

        // the index of the lexicographically smallest vertex
        int lexMin() {
            int min = 0;
            for (int i = 1; i < vertexCount; i++) {
                if (xs[i] < xs[min] || (xs[i] == xs[min] && ys[i] < ys[min])) min = i;
            }
            return min;
        }
    }

    static final class Seg {
        final Chain chain;          // null for a point
        final int index;            // the index in the chain: from vertex index to vertex index + 1
        final int geom, kind;
        final Node point;           // the node of a point
        final double x0, y0, x1, y1;
        final double minX, maxX, minY, maxY;
        List<Incidence> incidences;   // nodes in the interior of the segment
        boolean overlapping;          // overlaps another segment

        Seg(Chain chain, int index) {
            this.chain = chain;
            this.index = index;
            this.geom = chain.geom;
            this.kind = chain.kind;
            this.point = null;
            this.x0 = chain.xs[index];
            this.y0 = chain.ys[index];
            this.x1 = chain.xs[index + 1];
            this.y1 = chain.ys[index + 1];
            this.minX = Math.min(x0, x1);
            this.maxX = Math.max(x0, x1);
            this.minY = Math.min(y0, y1);
            this.maxY = Math.max(y0, y1);
        }

        Seg(int geom, Node point) {
            this.chain = null;
            this.index = 0;
            this.geom = geom;
            this.kind = POINT;
            this.point = point;
            this.x0 = this.x1 = this.minX = this.maxX = point.x;
            this.y0 = this.y1 = this.minY = this.maxY = point.y;
        }

        // the index in the chain of vertex 0 or 1 of the segment
        int vertex(int end) {
            return end == 0 ? index : (chain.closed && index + 1 == chain.vertexCount ? 0 : index + 1);
        }

        void addIncidence(Node node, Seg other) {
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
        boolean vertex;          // a vertex or point of one of the geometries
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
            if (star.size() > 2) star.sort(End::compareTo);   // the cyclic order of 2 ends is unique
            for (int i = 0; i < star.size(); i++) star.get(i).index = i;
        }
    }

    static final class Edge {
        final Node n0, n1;
        final int[] delta = new int[2];
        final boolean[] line = new boolean[2];
        final int[] left = new int[2], right = new int[2];
        boolean labeled;

        Edge(Node n0, Node n1) {
            this.n0 = n0;
            this.n1 = n1;
        }

        void add(Chain c, int sign) {
            if (c.kind == LINE) line[c.geom] = true;
            else delta[c.geom] += sign * c.delta;
        }
    }

    /**
     * An edge leaving a node, in the direction b - a of its first segment
     */
    static final class End implements Comparable<End> {
        final Edge edge;
        final boolean forward;   // leaves from edge.n0
        final double ax, ay, bx, by;
        int index;               // the position in the sorted star

        End(Edge edge, boolean forward, double ax, double ay, double bx, double by) {
            this.edge = edge;
            this.forward = forward;
            this.ax = ax;
            this.ay = ay;
            this.bx = bx;
            this.by = by;
        }

        int deltaOut(int g) {
            return forward ? edge.delta[g] : -edge.delta[g];
        }

        Node far() {
            return forward ? edge.n1 : edge.n0;
        }

        End reverse() {
            for (End e : far().star) {
                if (e.edge == edge && e.forward != forward) return e;
            }
            throw new IllegalStateException();
        }

        @Override
        public int compareTo(End o) {
            return PointLocator.compareDirections(ax, ay, bx, by, o.ax, o.ay, o.bx, o.by);
        }

        // 0 if the direction has an angle in [0, pi), 1 otherwise
        int half() {
            return by > ay || (by == ay && bx > ax) ? 0 : 1;
        }
    }

    private void addElements(GeometryElements e, int g) {
        for (double[] p : e.points) {
            Node n = nodeAt(p[0], p[1]);
            n.point[g] = true;
            points.add(new Seg(g, n));
        }
        for (double[][] line : e.lines) {
            Chain c = addChain(new Chain(g, LINE, -1, 0, line[0], line[1]));
            vertexNode(c, 0).endpoints[g]++;
            vertexNode(c, c.vertexCount - 1).endpoints[g]++;
        }
        for (GeometryElements.Ring ring : e.rings) {
            addChain(new Chain(g, RING, ring.polygon, ring.delta, ring.xs, ring.ys));
        }
    }

    private Chain addChain(Chain c) {
        chains.add(c);
        vertexNode(c, c.lexMin());
        return c;
    }

    private static Seg seg(Chain c, int i) {
        Seg s = c.segs[i];
        if (s == null) s = c.segs[i] = new Seg(c, i);
        return s;
    }

    private static long key(double x, double y) {
        long h = Double.doubleToLongBits(x) * 0x9E3779B97F4A7C15L + Double.doubleToLongBits(y);
        return h ^ (h >>> 29);
    }

    // the node of a vertex of a chain
    private Node vertexNode(Chain c, int vertex) {
        Node n = c.nodes[vertex];
        if (n == null) {
            n = nodeAt(c.xs[vertex], c.ys[vertex]);
            c.nodes[vertex] = n;
        }
        return n;
    }

    // the node of vertex 0 or 1 of a segment
    private Node vertexNode(Seg s, int end) {
        return s.kind == POINT ? s.point : vertexNode(s.chain, s.vertex(end));
    }

    // the node of a vertex or point at (x, y)
    private Node nodeAt(double x, double y) {
        long key = key(x, y);
        Node head = nodes.get(key);
        Node n = head;
        while (n != null && !(n.exact && n.x == x && n.y == y)) n = n.next;
        if (n == null) n = newNode(key, head, x, y, true, null, null);
        if (!n.vertex) {
            n.vertex = true;
            vertices.add(n);
        }
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

    /**
     * A monotone run of segments of a chain (all directions in the same quadrant), or a point. Two segments of the
     * same monotone chain only meet if they are consecutive, in their common vertex.
     */
    static final class MonotoneChain {
        final Chain chain;   // null for a point
        final Seg point;
        final int start, end;  // the segments start to end - 1, from vertex start to vertex end

        MonotoneChain(Chain chain, int start, int end) {
            this.chain = chain;
            this.point = null;
            this.start = start;
            this.end = end;
        }

        MonotoneChain(Seg point) {
            this.chain = null;
            this.point = point;
            this.start = 0;
            this.end = 1;
        }

        double x(int vertex) {
            return chain == null ? point.x0 : chain.xs[vertex];
        }

        double y(int vertex) {
            return chain == null ? point.y0 : chain.ys[vertex];
        }

        Seg seg(int i) {
            return chain == null ? point : RelateComputer.seg(chain, i);
        }
    }

    private static int quadrant(double dx, double dy) {
        return dx >= 0 ? (dy >= 0 ? 0 : 3) : (dy >= 0 ? 1 : 2);
    }

    // all pairs of segments with intersecting bounding boxes: monotone chains in an R-tree
    private void findContacts() {
        List<MonotoneChain> monotone = new ArrayList<>();
        // the points along a Hilbert curve, the monotone chains in their order along the lines and rings
        if (!points.isEmpty()) {
            double[] px = new double[points.size()], py = new double[points.size()];
            for (int i = 0; i < px.length; i++) {
                px[i] = points.get(i).x0;
                py[i] = points.get(i).y0;
            }
            for (int i : Hilbert.order(px, py)) monotone.add(new MonotoneChain(points.get(i)));
        }
        for (Chain c : chains) {
            int n = c.xs.length - 1, start = 0;
            int q = quadrant(c.xs[1] - c.xs[0], c.ys[1] - c.ys[0]);
            for (int i = 1; i < n; i++) {
                int qi = quadrant(c.xs[i + 1] - c.xs[i], c.ys[i + 1] - c.ys[i]);
                if (qi != q) {
                    monotone.add(new MonotoneChain(c, start, i));
                    start = i;
                    q = qi;
                }
            }
            monotone.add(new MonotoneChain(c, start, n));
        }
        int m = monotone.size();
        double[] minX = new double[m], minY = new double[m], maxX = new double[m], maxY = new double[m];
        for (int i = 0; i < m; i++) {
            MonotoneChain mc = monotone.get(i);
            minX[i] = Math.min(mc.x(mc.start), mc.x(mc.end));
            maxX[i] = Math.max(mc.x(mc.start), mc.x(mc.end));
            minY[i] = Math.min(mc.y(mc.start), mc.y(mc.end));
            maxY[i] = Math.max(mc.y(mc.start), mc.y(mc.end));
        }
        new EnvelopeTree(minX, minY, maxX, maxY).selfJoin((i, j) -> {
            MonotoneChain a = monotone.get(i), b = monotone.get(j);
            if (a.point == null || b.point == null) overlap(a, a.start, a.end, b, b.start, b.end);
        });
    }

    // the pairs of segments a0 to a1 - 1 of a and b0 to b1 - 1 of b with intersecting bounding boxes
    private void overlap(MonotoneChain a, int a0, int a1, MonotoneChain b, int b0, int b1) {
        // the bounding box of a monotone run is that of its end vertices
        double ax0 = a.x(a0), ax1 = a.x(a1), bx0 = b.x(b0), bx1 = b.x(b1);
        if (Math.max(ax0, ax1) < Math.min(bx0, bx1) || Math.min(ax0, ax1) > Math.max(bx0, bx1)) return;
        double ay0 = a.y(a0), ay1 = a.y(a1), by0 = b.y(b0), by1 = b.y(b1);
        if (Math.max(ay0, ay1) < Math.min(by0, by1) || Math.min(ay0, ay1) > Math.max(by0, by1)) return;
        if (a1 - a0 == 1 && b1 - b0 == 1) {
            Seg s = a.seg(a0), t = b.seg(b0);
            if (!skip(s, t)) contact(s, t);
        } else if (a1 - a0 >= b1 - b0) {
            int mid = (a0 + a1) >>> 1;
            overlap(a, a0, mid, b, b0, b1);
            overlap(a, mid, a1, b, b0, b1);
        } else {
            int mid = (b0 + b1) >>> 1;
            overlap(a, a0, a1, b, b0, mid);
            overlap(a, a0, a1, b, mid, b1);
        }
    }

    /**
     * Pairs that need no test: two points meet only if they are the same node; consecutive segments of a chain
     * meet in their common vertex, unless they overlap (the chain turns back).
     */
    private static boolean skip(Seg s, Seg t) {
        if (s.kind == POINT) return t.kind == POINT;
        if (s.chain != t.chain) return false;
        int n = s.chain.segs.length;
        if (s.index + 1 == t.index || (s.chain.closed && s.index == n - 1 && t.index == 0)) {
            return !s.chain.turnsBack[t.index];
        }
        if (t.index + 1 == s.index || (s.chain.closed && t.index == n - 1 && s.index == 0)) {
            return !s.chain.turnsBack[s.index];
        }
        return false;
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
                endpointOn(s, 0, t);
                if (s.kind != POINT) endpointOn(s, 1, t);
                endpointOn(t, 0, s);
                if (t.kind != POINT) endpointOn(t, 1, s);
                return;
            default: {
                // OVERLAPPING: the endpoints of both segments are nodes, so that the overlapping parts are
                // edges between nodes; the endpoints of each segment that lie inside the other are nodes on it
                s.overlapping = true;
                t.overlapping = true;
                Node s0 = vertexNode(s, 0), s1 = vertexNode(s, 1), t0 = vertexNode(t, 0), t1 = vertexNode(t, 1);
                if (s.strictlyInside(t.x0, t.y0)) s.addIncidence(t0, null);
                if (s.strictlyInside(t.x1, t.y1)) s.addIncidence(t1, null);
                if (t.strictlyInside(s.x0, s.y0)) t.addIncidence(s0, null);
                if (t.strictlyInside(s.x1, s.y1)) t.addIncidence(s1, null);
            }
        }
    }

    // if endpoint `end` of s lies on t, it is a node, of s and of t
    private void endpointOn(Seg s, int end, Seg t) {
        double x = end == 0 ? s.x0 : s.x1, y = end == 0 ? s.y0 : s.y1;
        if (t.kind == POINT) {
            if (x == t.x0 && y == t.y0) vertexNode(s, end);
            return;
        }
        if (!t.contains(x, y)) return;
        Node n = vertexNode(s, end);
        if (x == t.x0 && y == t.y0) vertexNode(t, 0);
        else if (x == t.x1 && y == t.y1) vertexNode(t, 1);
        else t.addIncidence(n, null);
    }

    // splits the chains into edges at their nodes
    private void buildEdges() {
        for (Chain c : chains) {
            int n = c.segs.length;
            int start = c.closed ? c.lexMin() : 0;   // a node
            Node from = c.nodes[start];
            int first = -1;   // the segment in which the current edge starts
            for (int k = 0; k < n; k++) {
                int i = (start + k) % n;
                if (first < 0) first = i;
                Seg s = c.segs[i];
                if (s != null && s.incidences != null) {
                    s.incidences.sort((i1, i2) -> compareAlong(s, i1, i2));
                    Node previous = null;
                    for (Incidence incidence : s.incidences) {
                        if (incidence.node == previous) continue;
                        addEdge(c, from, first, incidence.node, i);
                        from = previous = incidence.node;
                        first = i;
                    }
                }
                Node end = c.nodes[c.closed && i + 1 == c.vertexCount ? 0 : i + 1];
                if (end != null) {
                    addEdge(c, from, first, end, i);
                    from = end;
                    first = -1;
                }
            }
        }
    }

    // adds the edge from u to v, which leaves u along segment first and arrives at v along segment last
    private void addEdge(Chain c, Node u, int first, Node v, int last) {
        boolean shared = first == last && c.segs[first] != null && c.segs[first].overlapping;
        long key = u.id < v.id ? ((long) u.id << 32) | v.id : ((long) v.id << 32) | u.id;
        if (shared) {
            Edge e = sharedEdges.get(key);
            if (e != null) {
                e.add(c, e.n0 == u ? 1 : -1);
                return;
            }
        }
        Edge e = new Edge(u, v);
        e.add(c, 1);
        edges.add(e);
        double[] xs = c.xs, ys = c.ys;
        u.star.add(new End(e, true, xs[first], ys[first], xs[first + 1], ys[first + 1]));
        v.star.add(new End(e, false, xs[last + 1], ys[last + 1], xs[last], ys[last]));
        if (shared) sharedEdges.put(key, e);
    }

    // the order of two nodes in the interior of s, in the direction from (x0, y0) to (x1, y1)
    static int compareAlong(Seg s, Incidence i1, Incidence i2) {
        if (i1.node == i2.node) return 0;
        // the coordinate along the major axis of s is strictly monotone along s; it is exact for an exact node,
        // and correctly rounded (within half an ulp) for a crossing point
        boolean useX = Math.abs(s.x1 - s.x0) >= Math.abs(s.y1 - s.y0);
        double c1 = useX ? i1.node.x : i1.node.y, c2 = useX ? i2.node.x : i2.node.y;
        // a full ulp: twice the rounding error, which also covers the rounding of the difference below
        double e1 = i1.node.exact ? 0 : Math.ulp(c1), e2 = i2.node.exact ? 0 : Math.ulp(c2);
        if (Math.abs(c1 - c2) > e1 + e2 || (e1 == 0 && e2 == 0)) {
            int c = Double.compare(c1, c2);
            boolean increasing = useX ? s.x0 < s.x1 : s.y0 < s.y1;
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
        Deque<Node> queue = new ArrayDeque<>();
        int[] depth = new int[2];
        for (Node v : vertices) {
            if (v.labeled) continue;
            // v is the lexicographically smallest vertex of a new component: no edge leaves it in a direction
            // with negative X, and the sector containing direction (-1, 0) is outside all polygons of the
            // component
            for (int g = 0; g < 2; g++) {
                depth[g] = countContainingLeft(g, v.x, v.y);
            }
            labelNode(v, referenceSector(v), depth, queue);
            while (!queue.isEmpty()) {
                Node n = queue.poll();
                End end = n.pending;
                for (int g = 0; g < 2; g++) depth[g] = end.forward ? end.edge.left[g] : end.edge.right[g];
                labelNode(n, end.index, depth, queue);
            }
        }
    }

    private final PointLocator[] locators = new PointLocator[2];
    private final int[] scans = new int[2];
    private static final int MAX_SCANS = 16;

    /**
     * The number of polygons of geometry g that contain the points immediately to the left of (x, y), the
     * lexicographically smallest vertex of a component (see {@link PointLocator#countContainingLeft}). For a few
     * components, the rings are scanned; for more, a point locator with an index is built.
     */
    private int countContainingLeft(int g, double x, double y) {
        if (elements[g].polygonCount == 0) return 0;
        if (locators[g] == null && scans[g]++ < MAX_SCANS) return scanContainingLeft(g, x, y);
        if (locators[g] == null) locators[g] = new PointLocator(elements[g]);
        return locators[g].countContainingLeft(x, y);
    }

    // for each polygon, the parity of the crossings of the ray to the right of (x, y) with its rings that do not
    // pass through (x, y)
    private int scanContainingLeft(int g, double x, double y) {
        Set<Integer> odd = new HashSet<>();
        for (Chain c : chains) {
            if (c.geom != g || c.kind != RING || y < c.minY || y > c.maxY || x > c.maxX) continue;
            boolean parity = false;
            for (int i = 0; i + 1 < c.xs.length; i++) {
                double x0 = c.xs[i], y0 = c.ys[i], x1 = c.xs[i + 1], y1 = c.ys[i + 1];
                if ((y0 > y && y1 > y) || (y0 < y && y1 < y) || (x0 < x && x1 < x)) continue;
                int o = Predicates.orientation(x0, y0, x1, y1, x, y);
                if (o == 0 && Math.min(x0, x1) <= x && Math.min(y0, y1) <= y && y <= Math.max(y0, y1)) {
                    parity = false;   // the ring passes through (x, y)
                    break;
                }
                parity ^= y0 <= y ? y1 > y && o > 0 : y1 <= y && o < 0;
            }
            if (parity && !odd.remove(c.polygon)) odd.add(c.polygon);
        }
        return odd.size();
    }

    /**
     * For the lexicographically smallest vertex of a component: the index of the end that the sector containing
     * direction (-1, 0) follows (counterclockwise). This is the end with the largest angle in [0, pi), or if
     * there is none, the end with the largest angle. -1 if there are no ends.
     */
    private static int referenceSector(Node v) {
        int k = v.star.size();
        if (k <= 1) return k - 1;
        if (k == 2) {   // not sorted
            End e0 = v.star.get(0), e1 = v.star.get(1);
            int h0 = e0.half(), h1 = e1.half();
            if (h0 != h1) return h0 == 0 ? 0 : 1;
            return e0.compareTo(e1) > 0 ? 0 : 1;
        }
        int upper = 0;
        for (End e : v.star) upper += 1 - e.half();
        return (upper - 1 + k) % k;
    }

    // labels the edges around the node, given the depths of the sector that follows end number sector
    private void labelNode(Node n, int sector, int[] depth, Deque<Node> queue) {
        n.labeled = true;
        int k = n.star.size();
        if (k == 0) {
            for (int g = 0; g < 2; g++) n.areaAll[g] = n.areaAny[g] = depth[g] > 0;
            return;
        }
        if (sectors[0].length < k) sectors = new int[2][2 * k];
        for (int g = 0; g < 2; g++) {
            int[] s = sectors[g];
            s[sector] = depth[g];
            for (int j = 1; j < k; j++) {
                int i = (sector + j) % k;
                s[i] = s[(i + k - 1) % k] + n.star.get(i).deltaOut(g);
            }
            boolean all = true, any = false;
            for (int i = 0; i < k; i++) {
                all &= s[i] > 0;
                any |= s[i] > 0;
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
