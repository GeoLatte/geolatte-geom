package org.geolatte.geom.cga;

import org.geolatte.geom.cga.exact.Predicates;

import java.util.*;

/**
 * The planar graph of the snap rounded lines and rings of two geometries, labelled with the location of each node,
 * edge and face relative to each geometry. Used by {@link Overlay}.
 *
 * <p>The lines and rings of both geometries are snap rounded together to a {@link Grid} ({@link SnapRounder}). The snapped segments
 * become the edges of the graph; equal segments are merged into one edge, which records per geometry whether a line
 * runs along it and the sum of the deltas of the rings along it (the change of the number of polygons that contain
 * the points on its sides, from its right to its left). An edge with neither is dropped: it is the remains of a part
 * of a polygon that collapsed under snapping, or it separates polygons of a geometry that are adjacent. A line or
 * ring that collapses to a single grid point is dropped.</p>
 *
 * <p>The depths of the faces (the number of polygons of a geometry that contain them) are propagated around the
 * nodes and along the edges, starting for each connected component of the graph from the face to the left of its
 * smallest node. That depth is counted exactly by the crossings of a ray to the left with the edges of the other
 * components. So the labels are those of the snapped geometries, with union semantics, as in
 * {@link RelateComputer}.</p>
 */
final class OverlayGraph {

    static final int A = 0, B = 1;

    /**
     * A line or ring of one of the geometries, with the Z and M coordinates of its positions
     */
    static final class Input {
        final int geom;
        final boolean ring;
        final int delta;   // for a ring
        final double[] xs, ys, zs, ms;

        Input(int geom, boolean ring, int delta, double[][] c) {
            this.geom = geom;
            this.ring = ring;
            this.delta = delta;
            this.xs = c[0];
            this.ys = c[1];
            this.zs = c[2];
            this.ms = c[3];
        }
    }

    static final class Node {
        final int id;
        final double x, y;
        final double z, m;
        final List<End> star = new ArrayList<>(2);
        final int[] endpoints = new int[2];      // number of line endpoints at the node, per geometry
        final boolean[] areaAll = new boolean[2], areaAny = new boolean[2];
        boolean labeled, queued;
        End pending;             // the end through which a queued node was reached
        int mark;                // for the assembly of the result

        Node(int id, double x, double y, double z, double m) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.m = m;
        }

        void sortStar() {
            if (star.size() > 1) star.sort(End::compareTo);
            for (int i = 0; i < star.size(); i++) star.get(i).index = i;
        }

        boolean onLine(int g) {
            for (End e : star) {
                if (e.edge.line[g]) return true;
            }
            return false;
        }

        /**
         * @return whether the node is a point of geometry g
         */
        boolean in(int g) {
            return areaAny[g] || onLine(g);
        }
    }

    static final class Edge {
        final Node n0, n1;
        final int[] delta = new int[2];          // of the rings along it, from n0 to n1
        final boolean[] line = new boolean[2];
        int direction;           // of the first line along it: 1 from n0 to n1, -1 from n1 to n0, 0 if none
        final int[] left = new int[2], right = new int[2];
        boolean labeled;
        End forward, backward;
        boolean resultLeft, resultRight, resultLine;

        Edge(Node n0, Node n1) {
            this.n0 = n0;
            this.n1 = n1;
        }

        boolean in(int g) {
            return left[g] > 0 || right[g] > 0 || line[g];
        }

        boolean isResultBoundary() {
            return resultLeft != resultRight;
        }

        boolean isResult() {
            return resultLeft || resultRight || resultLine;
        }
    }

    /**
     * An edge leaving a node
     */
    static final class End implements Comparable<End> {
        final Edge edge;
        final boolean forward;   // leaves from edge.n0
        final Node from, to;
        int index;               // the position in the sorted star
        boolean visited;

        End(Edge edge, boolean forward) {
            this.edge = edge;
            this.forward = forward;
            this.from = forward ? edge.n0 : edge.n1;
            this.to = forward ? edge.n1 : edge.n0;
        }

        int deltaOut(int g) {
            return forward ? edge.delta[g] : -edge.delta[g];
        }

        End reverse() {
            return forward ? edge.backward : edge.forward;
        }

        boolean leftInResult() {
            return forward ? edge.resultLeft : edge.resultRight;
        }

        // an edge of the boundary of the result, with the result to its left
        boolean isResultShellSide() {
            return edge.isResultBoundary() && leftInResult();
        }

        @Override
        public int compareTo(End o) {
            return PointLocator.compareDirections(from.x, from.y, to.x, to.y, o.from.x, o.from.y, o.to.x, o.to.y);
        }

        // 0 if the direction has an angle in [0, pi), 1 otherwise
        int half() {
            return to.y > from.y || (to.y == from.y && to.x > from.x) ? 0 : 1;
        }
    }

    final List<Node> nodes = new ArrayList<>();
    final List<Edge> edges = new ArrayList<>();
    private final Map<Key, Node> nodeIndex;
    private final Map<Long, Edge> edgeIndex;
    private final Map<Key, double[]> measures;   // {z, m} of the input vertices; null if there are none

    // a key for exact coordinates, with a well mixed hash
    private static final class Key {
        final double x, y;

        Key(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key && ((Key) o).x == x && ((Key) o).y == y;
        }

        @Override
        public int hashCode() {
            long h = Double.doubleToLongBits(x) * 0x9E3779B97F4A7C15L + Double.doubleToLongBits(y);
            h *= 0xBF58476D1CE4E5B9L;
            return (int) (h ^ (h >>> 31));
        }
    }

    OverlayGraph(List<Input> inputs, Grid grid) {
        int vertices = 0;
        boolean hasMeasures = false;
        for (Input in : inputs) {
            vertices += in.xs.length;
            for (int i = 0; i < in.xs.length && !hasMeasures; i++) {
                hasMeasures = !Double.isNaN(in.zs[i]) || !Double.isNaN(in.ms[i]);
            }
        }
        nodeIndex = new HashMap<>(2 * vertices);
        edgeIndex = new HashMap<>(2 * vertices);
        measures = hasMeasures ? new HashMap<>(2 * vertices) : null;
        if (hasMeasures) {
            for (Input in : inputs) {
                for (int i = 0; i < in.xs.length; i++) {
                    // a vertex moves to its grid point: the first input vertex there gives the measures
                    Key key = new Key(grid.round(in.xs[i]), grid.round(in.ys[i]));
                    measures.putIfAbsent(key, new double[]{in.zs[i], in.ms[i]});
                }
            }
        }
        List<double[][]> chains = new ArrayList<>(inputs.size());
        for (Input in : inputs) chains.add(new double[][]{in.xs, in.ys});
        List<SnapRounder.Chain> snapped = SnapRounder.snap(chains, grid);
        for (int k = 0; k < inputs.size(); k++) addChain(inputs.get(k), snapped.get(k));
        for (Edge e : edges) {
            if (e.delta[A] == 0 && e.delta[B] == 0 && !e.line[A] && !e.line[B]) continue;
            e.forward = new End(e, true);
            e.backward = new End(e, false);
            e.n0.star.add(e.forward);
            e.n1.star.add(e.backward);
        }
        edges.removeIf(e -> e.forward == null);
        nodes.removeIf(n -> n.star.isEmpty());
        for (Node n : nodes) n.sortStar();
    }

    private void addChain(Input in, SnapRounder.Chain c) {
        if (c.size() < 2) return;     // collapsed to a grid point
        int g = in.geom;
        Node previous = node(in, c, 0);
        if (!in.ring) previous.endpoints[g]++;
        for (int v = 1; v < c.size(); v++) {
            Node node = node(in, c, v);
            addSegment(in, previous, node);
            previous = node;
        }
        if (!in.ring) previous.endpoints[g]++;
    }

    private Node node(Input in, SnapRounder.Chain c, int v) {
        double x = c.xs[v], y = c.ys[v];
        Key key = new Key(x, y);
        Node n = nodeIndex.get(key);
        if (n != null) return n;
        double[] zm = measures == null ? NO_MEASURES : measures.get(key);
        if (zm == null) zm = interpolate(in, c.origin[v], x, y);
        n = new Node(nodes.size(), x, y, zm[0], zm[1]);
        nodes.add(n);
        nodeIndex.put(key, n);
        return n;
    }

    private static final double[] NO_MEASURES = {Double.NaN, Double.NaN};

    // the Z and M at the projection of (x, y) on segment i of the input
    private static double[] interpolate(Input in, int i, double x, double y) {
        double ax = in.xs[i], ay = in.ys[i], bx = in.xs[i + 1], by = in.ys[i + 1];
        double dx = bx - ax, dy = by - ay;
        double t = (dx * (x - ax) + dy * (y - ay)) / (dx * dx + dy * dy);
        if (!(t > 0)) t = 0;   // also for NaN
        if (t > 1) t = 1;
        return new double[]{in.zs[i] + t * (in.zs[i + 1] - in.zs[i]), in.ms[i] + t * (in.ms[i + 1] - in.ms[i])};
    }

    private void addSegment(Input in, Node p, Node q) {
        int ip = p.id, iq = q.id;
        Node n0 = ip < iq ? p : q, n1 = ip < iq ? q : p;
        // multiplying by an odd constant is a bijection, and mixes the bits for the hash of the Long
        long key = (((long) Math.min(ip, iq) << 32) | Math.max(ip, iq)) * 0x9E3779B97F4A7C15L;
        Edge e = edgeIndex.get(key);
        if (e == null) {
            e = new Edge(n0, n1);
            edgeIndex.put(key, e);
            edges.add(e);
        }
        int sign = p == n0 ? 1 : -1;
        int g = in.geom;
        if (in.ring) {
            e.delta[g] += sign * in.delta;
        } else {
            e.line[g] = true;
            if (e.direction == 0) e.direction = sign;
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // labelling
    // ---------------------------------------------------------------------------------------------------------

    private int[][] sectors = new int[2][8];
    private IntervalIndex ringEdges;
    private List<Edge> ringEdgeList;

    void label() {
        List<Node> vertices = new ArrayList<>(nodes);
        vertices.sort((u, v) -> u.x != v.x ? Double.compare(u.x, v.x) : Double.compare(u.y, v.y));
        Deque<Node> queue = new ArrayDeque<>();
        int[] depth = new int[2];
        for (Node v : vertices) {
            if (v.labeled) continue;
            // v is the smallest node of a new component: no edge leaves it in a direction with negative X, and
            // the face in direction (-1, 0) is outside its edges
            depthLeft(v, depth);
            labelNode(v, referenceSector(v), depth, queue);
            while (!queue.isEmpty()) {
                Node n = queue.poll();
                End end = n.pending;
                for (int g = 0; g < 2; g++) depth[g] = end.forward ? end.edge.left[g] : end.edge.right[g];
                labelNode(n, end.index, depth, queue);
            }
        }
    }

    /**
     * The depths of the points immediately to the left of v, the smallest node of a component: the sum of the
     * deltas of the edges crossed by the ray to the left of v, as it enters or leaves the polygons. No edge of
     * another component passes through v, and no edge of its own component crosses the ray.
     */
    private void depthLeft(Node v, int[] depth) {
        depth[A] = depth[B] = 0;
        if (ringEdges == null) {
            ringEdgeList = new ArrayList<>();
            for (Edge e : edges) {
                if (e.delta[A] != 0 || e.delta[B] != 0) ringEdgeList.add(e);
            }
            int n = ringEdgeList.size();
            double[] lo = new double[n], hi = new double[n];
            for (int i = 0; i < n; i++) {
                Edge e = ringEdgeList.get(i);
                lo[i] = Math.min(e.n0.y, e.n1.y);
                hi[i] = Math.max(e.n0.y, e.n1.y);
            }
            ringEdges = new IntervalIndex(lo, hi);
        }
        double x = v.x, y = v.y;
        ringEdges.query(y, i -> {
            Edge e = ringEdgeList.get(i);
            double x0 = e.n0.x, y0 = e.n0.y, x1 = e.n1.x, y1 = e.n1.y;
            if ((y0 <= y) == (y1 <= y)) return;           // does not cross the line Y = y (half open)
            if (x0 >= x && x1 >= x) return;
            boolean down = y1 < y0;
            // the crossing lies to the left of v if v lies to the right of the edge directed upwards
            int o = down ? Predicates.orientation(x1, y1, x0, y0, x, y) : Predicates.orientation(x0, y0, x1, y1, x, y);
            if (o >= 0) return;
            // going right across a downward edge, one enters the polygons to its left
            for (int g = 0; g < 2; g++) depth[g] += down ? e.delta[g] : -e.delta[g];
        });
    }

    /**
     * For the smallest node of a component: the index of the end that the sector containing direction (-1, 0)
     * follows (counterclockwise). This is the end with the largest angle in [0, pi), or if there is none, the end
     * with the largest angle.
     */
    private static int referenceSector(Node v) {
        int k = v.star.size();
        if (k == 1) return 0;
        int upper = 0;
        for (End e : v.star) upper += 1 - e.half();
        return (upper - 1 + k) % k;
    }

    // labels the edges around the node, given the depths of the sector that follows end number sector
    private void labelNode(Node n, int sector, int[] depth, Deque<Node> queue) {
        n.labeled = true;
        int k = n.star.size();
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
                    if (leftOut != sectors[g][i] || rightOut != sectors[g][previous]) {
                        throw new IllegalStateException("Inconsistent depths in the overlay graph");
                    }
                }
                continue;
            }
            for (int g = 0; g < 2; g++) {
                int leftOut = sectors[g][i], rightOut = sectors[g][previous];
                e.left[g] = end.forward ? leftOut : rightOut;
                e.right[g] = end.forward ? rightOut : leftOut;
            }
            e.labeled = true;
            Node far = end.to;
            if (!far.labeled && !far.queued) {
                far.queued = true;
                far.pending = end.reverse();
                queue.add(far);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // selection
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Marks the faces and edges of the result: the faces for which the operation holds, and the edges for which it
     * holds that are not next to such a face
     */
    void select(Overlay.Operation op) {
        for (Edge e : edges) {
            e.resultLeft = op.test(e.left[A] > 0, e.left[B] > 0);
            e.resultRight = op.test(e.right[A] > 0, e.right[B] > 0);
            e.resultLine = !e.resultLeft && !e.resultRight && op.test(e.in(A), e.in(B));
        }
    }
}
