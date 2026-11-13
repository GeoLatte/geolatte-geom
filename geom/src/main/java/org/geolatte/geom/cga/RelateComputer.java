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
 * quadrant) in an {@link EnvelopeTree} per geometry: segments of the same monotone chain only meet in a common
 * vertex. The trees of A and B are joined first. A geometry is only intersected with itself if it has lines, more
 * than one polygon, or points and a polygon: the rings of a valid polygon only touch in vertices, and these are
 * found by looking up every node in the ring chains afterwards (the closing step).</p>
 * <p>With a {@link Predicate}, the computation stops as soon as the predicate is decided, see {@link #compute()}.</p>
 * <p>The structures that depend on one geometry only are in an {@link Operand}, which can be prepared once and used
 * in many computations (see {@link PreparedRelate}). A computation then only uses the connected components of a
 * prepared operand that are near the other geometry, and none if the geometries do not meet.</p>
 * <p>Nodes, edges, and the faces next to the edges partition the plane into cells, in each of which the location
 * relative to each geometry is constant. The matrix entry of the locations of a cell is at least the dimension of
 * the cell.</p>
 */
final class RelateComputer {

    static final int A = 0, B = 1;
    static final int POINT = 0, LINE = 1, RING = 2;

    private final Operand[] operands;
    private final GeometryElements[] elements;
    private final List<Chain> chains = new ArrayList<>();
    private final Chain[][] chainsOf = new Chain[2][];
    private final Seg[][] pointsOf = new Seg[2][];
    private final List<Node> vertices = new ArrayList<>();
    private final Map<Long, Node> nodes = new HashMap<>();
    private final List<Node> allNodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Long, Edge> sharedEdges = new HashMap<>();
    private int[][] sectors = new int[2][8];
    int inconsistencies = 0;   // edges that received different depths from their two ends (invalid input)

    /**
     * The predicates that can be decided before the matrix is complete
     */
    enum Predicate {
        NONE,
        INTERSECTS,     // true if the geometries meet
        TOUCHES,        // false if the interiors meet
        CONTAINMENT,    // contains, within, covers, covered by, equals: false if both have points outside the other
        CROSSES,        // true for a line crossing the boundary of an area
        OVERLAPS        // true for crossing boundaries of areas
    }

    private final Predicate predicate;
    private final boolean[] simple = new boolean[2];
    private final boolean[][] kept = new boolean[2][];   // per component of a prepared operand: is it used?
    private final int[][] keptList = new int[2][];
    private final boolean[][] inside = new boolean[2][]; // per component: not used, and in the other's interior
    private boolean contactAB;   // an element of A meets one of B
    Boolean decided;   // the value of the predicate, if decided before the matrix is complete

    RelateComputer(GeometryElements a, GeometryElements b) {
        this(new Operand(a), new Operand(b), Predicate.NONE);
    }

    RelateComputer(GeometryElements a, GeometryElements b, Predicate predicate) {
        this(new Operand(a), new Operand(b), predicate);
    }

    RelateComputer(Operand a, Operand b, Predicate predicate) {
        this.operands = new Operand[]{a, b};
        this.elements = new GeometryElements[]{a.elements, b.elements};
        this.predicate = predicate;
        // a geometry is locally simple if no element covers the neighbourhood of a point on the boundary of another
        // element: then a proper crossing that no other segment passes through tells the locations around it
        for (int g = 0; g < 2; g++) {
            simple[g] = elements[g].polygonCount == 0 || (elements[g].polygonCount == 1 && elements[g].lines.isEmpty());
        }
    }

    /**
     * @return the matrix, or null if the predicate was decided before the matrix was complete (see decided)
     */
    IntersectionMatrix compute() {
        for (int g = 0; g < 2; g++) operands[g].index();
        for (int g = 0; g < 2; g++) {
            selectComponents(g);
            pointsOf[g] = new Seg[operands[g].elements.points.size()];
            chainsOf[g] = new Chain[operands[g].chains.length];
            // the elements of a prepared operand are added after the join, when it is known which are needed
            if (kept[g] == null) addElements(g);
        }
        findContacts();
        if (decided != null) return null;
        buildEdges();
        for (Node n : allNodes) n.sortStar();
        label();
        return matrix();
    }

    // ---------------------------------------------------------------------------------------------------------
    // the operands
    // ---------------------------------------------------------------------------------------------------------

    /**
     * A geometry with the structures that do not depend on the other geometry: its lines and rings, their monotone
     * chains, and an R-tree on these. A prepared operand (see {@link #prepare()}) also has the pairs of its
     * segments that meet and a point locator; it can be used in any number of computations, also concurrently.
     */
    static final class Operand {
        final GeometryElements elements;
        final boolean selfContacts;   // elements can meet other than in rings of the same polygon
        ChainData[] chains;           // the lines, then the rings
        MonotoneChain[] monotone;     // the points along a Hilbert curve, then the chains along the lines and rings
        EnvelopeTree tree;            // on the monotone chains
        int[] contacts;               // if prepared: the pairs of segments (chain, segment) that meet
        PointLocator locator;         // if prepared
        // if prepared: the connected components (chains and points that meet each other), so that a computation
        // only needs the components near the other geometry; the others lie in its exterior
        int componentCount;
        int[] memberStart, members;   // per component, its chains (k) and points (-1 - i)
        private int[] component;      // per chain, then per point: its component
        int[] contactStart;           // per component, its contacts (in contacts, 4 values per contact)
        int[][] dims;                 // per location (I, BD) and component: the highest dimension of its cells in
                                      // that location of this geometry, -1 if none
        int[][] byDim;                // per location: the components with such cells, by decreasing dimension
        EnvelopeTree componentTree;   // on the bounding boxes of the components

        Operand(GeometryElements elements) {
            this.elements = elements;
            this.selfContacts = !elements.lines.isEmpty() || elements.polygonCount > 1
                    || (!elements.points.isEmpty() && elements.polygonCount > 0);
        }

        // builds the chains, monotone chains and their R-tree
        void index() {
            if (tree != null) return;
            List<ChainData> data = new ArrayList<>();
            for (double[][] line : elements.lines) data.add(new ChainData(LINE, -1, 0, line[0], line[1]));
            for (GeometryElements.Ring r : elements.rings) data.add(new ChainData(RING, r.polygon, r.delta, r.xs, r.ys));
            chains = data.toArray(new ChainData[0]);
            List<MonotoneChain> mcs = new ArrayList<>();
            List<double[]> points = elements.points;
            if (!points.isEmpty()) {
                double[] px = new double[points.size()], py = new double[points.size()];
                for (int i = 0; i < px.length; i++) {
                    px[i] = points.get(i)[0];
                    py[i] = points.get(i)[1];
                }
                for (int i : Hilbert.order(px, py)) mcs.add(new MonotoneChain(i, px[i], py[i]));
            }
            for (int k = 0; k < chains.length; k++) {
                ChainData c = chains[k];
                int n = c.xs.length - 1, start = 0;
                int q = quadrant(c.xs[1] - c.xs[0], c.ys[1] - c.ys[0]);
                for (int i = 1; i < n; i++) {
                    int qi = quadrant(c.xs[i + 1] - c.xs[i], c.ys[i + 1] - c.ys[i]);
                    if (qi != q) {
                        mcs.add(new MonotoneChain(k, c, start, i));
                        start = i;
                        q = qi;
                    }
                }
                mcs.add(new MonotoneChain(k, c, start, n));
            }
            monotone = mcs.toArray(new MonotoneChain[0]);
            int m = monotone.length;
            double[] minX = new double[m], minY = new double[m], maxX = new double[m], maxY = new double[m];
            for (int i = 0; i < m; i++) {
                MonotoneChain mc = monotone[i];
                minX[i] = Math.min(mc.x(mc.start), mc.x(mc.end));
                maxX[i] = Math.max(mc.x(mc.start), mc.x(mc.end));
                minY[i] = Math.min(mc.y(mc.start), mc.y(mc.end));
                maxY[i] = Math.max(mc.y(mc.start), mc.y(mc.end));
            }
            tree = new EnvelopeTree(minX, minY, maxX, maxY);
        }

        /**
         * Computes the pairs of segments of the geometry that meet, and a point locator
         */
        Operand prepare() {
            index();
            locator = new PointLocator(elements);
            int[][] found = {new int[16]};
            int[] count = {0};
            if (selfContacts) {
                tree.selfJoin((i, j) -> {
                    MonotoneChain a = monotone[i], b = monotone[j];
                    if (independent(a, b)) return false;
                    pairs(a, a.start, a.end, b, b.start, b.end, (s, t) -> {
                        if (consecutive(a, s, b, t)) return false;
                        SegmentIntersection kind = Predicates.segmentIntersection(a.x(s), a.y(s), a.x(s + 1),
                                a.y(s + 1), b.x(t), b.y(t), b.x(t + 1), b.y(t + 1));
                        if (kind == SegmentIntersection.DISJOINT) return false;
                        if (count[0] + 4 > found[0].length) found[0] = Arrays.copyOf(found[0], 2 * found[0].length);
                        int[] f = found[0];
                        f[count[0]++] = a.code();
                        f[count[0]++] = s;
                        f[count[0]++] = b.code();
                        f[count[0]++] = t;
                        return false;
                    });
                    return false;
                });
            }
            contacts = Arrays.copyOf(found[0], count[0]);
            EnvelopeTree tree = components();
            dims = new RelateComputer(this, new Operand(new GeometryElements()), Predicate.NONE).componentDims();
            byDim = new int[2][];
            for (int loc = 0; loc < 2; loc++) {
                int[] d = dims[loc];
                byDim[loc] = java.util.stream.IntStream.range(0, componentCount).filter(c -> d[c] >= 0).boxed()
                        .sorted((c1, c2) -> Integer.compare(d[c2], d[c1])).mapToInt(Integer::intValue).toArray();
            }
            componentTree = tree;   // from now on, computations use only the components they need
            return this;
        }

        // the index of a chain (k) or point (-1 - i) in the union-find arrays
        private int id(int code) {
            return code >= 0 ? code : chains.length - 1 - code;
        }

        // finds the connected components, groups the members and contacts per component, and returns an R-tree on
        // the components
        private EnvelopeTree components() {
            int n = chains.length + elements.points.size();
            int[] parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
            for (int k = 0; k < contacts.length; k += 4) union(parent, id(contacts[k]), id(contacts[k + 2]));
            // the rings of a polygon touch at most in vertices, which are not in the contacts
            int[] firstRing = new int[elements.polygonCount];
            Arrays.fill(firstRing, -1);
            for (int k = 0; k < chains.length; k++) {
                if (chains[k].kind != RING) continue;
                int p = chains[k].polygon;
                if (firstRing[p] < 0) firstRing[p] = k;
                else union(parent, firstRing[p], k);
            }
            component = new int[n];
            int[] index = new int[n];
            Arrays.fill(index, -1);
            componentCount = 0;
            for (int i = 0; i < n; i++) {
                int r = find(parent, i);
                if (index[r] < 0) index[r] = componentCount++;
                component[i] = index[r];
            }
            memberStart = new int[componentCount + 1];
            for (int i = 0; i < n; i++) memberStart[component[i] + 1]++;
            for (int c = 0; c < componentCount; c++) memberStart[c + 1] += memberStart[c];
            members = new int[n];
            int[] next = Arrays.copyOf(memberStart, componentCount);
            for (int i = 0; i < n; i++) members[next[component[i]]++] = i < chains.length ? i : chains.length - 1 - i;
            // the contacts, ordered by component
            int m = contacts.length / 4;
            contactStart = new int[componentCount + 1];
            for (int k = 0; k < m; k++) contactStart[component[id(contacts[4 * k])] + 1]++;
            for (int c = 0; c < componentCount; c++) contactStart[c + 1] += contactStart[c];
            int[] sorted = new int[contacts.length];
            next = Arrays.copyOf(contactStart, componentCount);
            for (int k = 0; k < m; k++) {
                System.arraycopy(contacts, 4 * k, sorted, 4 * next[component[id(contacts[4 * k])]]++, 4);
            }
            contacts = sorted;
            // the bounding boxes
            double[] minX = new double[componentCount], minY = new double[componentCount];
            double[] maxX = new double[componentCount], maxY = new double[componentCount];
            Arrays.fill(minX, Double.POSITIVE_INFINITY);
            Arrays.fill(minY, Double.POSITIVE_INFINITY);
            Arrays.fill(maxX, Double.NEGATIVE_INFINITY);
            Arrays.fill(maxY, Double.NEGATIVE_INFINITY);
            for (int i = 0; i < n; i++) {
                int c = component[i];
                double x0, y0, x1, y1;
                if (i < chains.length) {
                    x0 = chains[i].minX;
                    y0 = chains[i].minY;
                    x1 = chains[i].maxX;
                    y1 = chains[i].maxY;
                } else {
                    double[] p = elements.points.get(i - chains.length);
                    x0 = x1 = p[0];
                    y0 = y1 = p[1];
                }
                minX[c] = Math.min(minX[c], x0);
                minY[c] = Math.min(minY[c], y0);
                maxX[c] = Math.max(maxX[c], x1);
                maxY[c] = Math.max(maxY[c], y1);
            }
            return new EnvelopeTree(minX, minY, maxX, maxY);
        }

        private static int find(int[] parent, int i) {
            while (parent[i] != i) {
                parent[i] = parent[parent[i]];
                i = parent[i];
            }
            return i;
        }

        private static void union(int[] parent, int i, int j) {
            parent[find(parent, i)] = find(parent, j);
        }

        // the component of a chain (k) or point (-1 - i)
        int componentOf(int code) {
            return component[id(code)];
        }

        // pairs of monotone chains that need not be tested: two points meet only if they are the same node, and the
        // rings of a valid polygon do not cross each other
        boolean independent(MonotoneChain a, MonotoneChain b) {
            if (a.chain < 0 && b.chain < 0) return true;
            if (a.chain < 0 || b.chain < 0) return false;
            ChainData ca = chains[a.chain], cb = chains[b.chain];
            return ca.kind == RING && cb.kind == RING && ca.polygon == cb.polygon;
        }

        // pairs of segments that need not be tested: consecutive segments of a chain meet in their common vertex,
        // unless they overlap (the chain turns back)
        boolean consecutive(MonotoneChain a, int i, MonotoneChain b, int j) {
            if (a.chain < 0 || a.chain != b.chain) return false;
            ChainData c = chains[a.chain];
            int n = c.xs.length - 1;
            int vertex;
            if (i + 1 == j || (c.closed && i == n - 1 && j == 0)) vertex = j;
            else if (j + 1 == i || (c.closed && j == n - 1 && i == 0)) vertex = i;
            else return false;
            return c.turnsBack == null || !c.turnsBack[vertex];
        }

        /**
         * The location of the point (see {@link GeometryElements#roughLocation})
         */
        int roughLocation(double x, double y) {
            return locator == null ? elements.roughLocation(x, y) : locator.roughLocation(x, y);
        }
    }

    /**
     * A line, or a ring of a polygon, of an operand
     */
    static final class ChainData {
        final int kind, polygon, delta;
        final double[] xs, ys;     // a ring is closed: its last position is the first one
        final boolean closed;
        final int vertexCount;     // distinct vertices: those of a ring exclude the closing position
        final int lexMin;          // the index of the lexicographically smallest vertex
        final boolean[] turnsBack; // for a line: the segments before and after the vertex overlap; null for a ring
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        ChainData(int kind, int polygon, int delta, double[] xs, double[] ys) {
            this.kind = kind;
            this.polygon = polygon;
            this.delta = delta;
            this.xs = xs;
            this.ys = ys;
            this.closed = kind == RING;
            this.vertexCount = closed ? xs.length - 1 : xs.length;
            this.turnsBack = closed ? null : new boolean[vertexCount];
            int min = 0;
            for (int i = 0; i < vertexCount; i++) {
                minX = Math.min(minX, xs[i]);
                maxX = Math.max(maxX, xs[i]);
                minY = Math.min(minY, ys[i]);
                maxY = Math.max(maxY, ys[i]);
                if (xs[i] < xs[min] || (xs[i] == xs[min] && ys[i] < ys[min])) min = i;
                // only consecutive segments of a line can be tested against each other: rings of a polygon are
                // not tested against the rings of the same polygon
                if (closed || i == 0 || i == vertexCount - 1) continue;
                turnsBack[i] = turnsBack(xs[i - 1], ys[i - 1], xs[i], ys[i], xs[i + 1], ys[i + 1]);
            }
            this.lexMin = min;
        }

        // for consecutive segments (p, q) and (q, r): do they overlap?
        private static boolean turnsBack(double px, double py, double qx, double qy, double rx, double ry) {
            // they can only overlap if (p - q) . (r - q) > 0; most turns are decided by a floating point filter
            double d1 = (px - qx) * (rx - qx), d2 = (py - qy) * (ry - qy);
            if (d1 + d2 < -(1e-15 * (Math.abs(d1) + Math.abs(d2)) + Double.MIN_NORMAL)) return false;
            return Predicates.orientation(px, py, qx, qy, rx, ry) == 0
                    && Predicates.dotSign(qx, qy, px, py, qx, qy, rx, ry) > 0;
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // the arrangement
    // ---------------------------------------------------------------------------------------------------------

    /**
     * A line or ring in the computation. Only the vertices and segments where something happens have state, so
     * that the cost does not depend on the length of the chain.
     */
    static final class Chain {
        final ChainData data;
        final int geom, index, kind, polygon, delta;
        final double[] xs, ys;
        final boolean closed;
        final int vertexCount;
        private final IntMap<Node> nodes = new IntMap<>();   // the node at a vertex, if the vertex is a node
        private final IntMap<Seg> segs = new IntMap<>();     // the segments that meet another one
        private int[] marks = new int[4];   // 2 v for a node at vertex v, 2 i + 1 for nodes inside segment i
        private int markCount = 0;
        boolean registered;   // part of the arrangement (a chain of a prepared operand may be created before)

        Chain(ChainData data, int geom, int index) {
            this.data = data;
            this.geom = geom;
            this.index = index;
            this.kind = data.kind;
            this.polygon = data.polygon;
            this.delta = data.delta;
            this.xs = data.xs;
            this.ys = data.ys;
            this.closed = data.closed;
            this.vertexCount = data.vertexCount;
        }

        Node node(int vertex) {
            return nodes.get(vertex);
        }

        Seg seg(int i) {
            return segs.get(i);
        }

        private void mark(int m) {
            if (markCount == marks.length) marks = Arrays.copyOf(marks, 2 * markCount);
            marks[markCount++] = m;
        }
    }

    static final class Seg {
        final Chain chain;          // null for a point
        final int index;            // the index in the chain: from vertex index to vertex index + 1; for a point,
                                    // the index of the point
        final int geom, kind;
        Node point;                 // the node of a point, once it is needed
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

        Seg(int geom, int index, double x, double y) {
            this.chain = null;
            this.index = index;
            this.geom = geom;
            this.kind = POINT;
            this.x0 = this.x1 = this.minX = this.maxX = x;
            this.y0 = this.y1 = this.minY = this.maxY = y;
        }

        // the index in the chain of vertex 0 or 1 of the segment
        int vertex(int end) {
            return end == 0 ? index : (chain.closed && index + 1 == chain.vertexCount ? 0 : index + 1);
        }

        void addIncidence(Node node, Seg other) {
            if (incidences == null) {
                incidences = new ArrayList<>(2);
                chain.mark(2 * index + 1);
            }
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
     * A map from non-negative ints to objects, with open addressing
     */
    static final class IntMap<V> {
        private int[] keys;
        private Object[] values;
        private int size;

        @SuppressWarnings("unchecked")
        V get(int key) {
            if (keys == null) return null;
            int mask = keys.length - 1;
            for (int i = hash(key) & mask; values[i] != null; i = (i + 1) & mask) {
                if (keys[i] == key) return (V) values[i];
            }
            return null;
        }

        // the key must not be present
        void put(int key, V value) {
            if (keys == null) {
                keys = new int[4];
                values = new Object[4];
            } else if (2 * (size + 1) > keys.length) {
                int[] oldKeys = keys;
                Object[] oldValues = values;
                keys = new int[2 * oldKeys.length];
                values = new Object[2 * oldKeys.length];
                size = 0;
                for (int i = 0; i < oldKeys.length; i++) {
                    if (oldValues[i] != null) insert(oldKeys[i], oldValues[i]);
                }
            }
            insert(key, value);
        }

        private void insert(int key, Object value) {
            int mask = keys.length - 1;
            int i = hash(key) & mask;
            while (values[i] != null) i = (i + 1) & mask;
            keys[i] = key;
            values[i] = value;
            size++;
        }

        private static int hash(int key) {
            int h = key * 0x9E3779B9;
            return h ^ (h >>> 16);
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
        Chain chain;             // a chain of which the edge is a part
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

    /**
     * For a prepared operand: the components whose bounding box meets the bounding box of the other geometry. The
     * other components lie in the exterior of the other geometry, at a distance; they do not meet the used
     * components, and their contribution to the matrix is known (see Operand.dims).
     */
    private void selectComponents(int g) {
        Operand operand = operands[g];
        if (operand.componentTree == null) return;
        GeometryElements other = elements[1 - g];
        kept[g] = new boolean[operand.componentCount];
        int[][] list = {new int[8]};
        int[] count = {0};
        operand.componentTree.query(other.minX, other.minY, other.maxX, other.maxY, c -> {
            kept[g][c] = true;
            if (count[0] == list[0].length) list[0] = Arrays.copyOf(list[0], 2 * count[0]);
            list[0][count[0]++] = c;
        });
        keptList[g] = Arrays.copyOf(list[0], count[0]);
    }

    /**
     * If the geometries do not meet, each component of a prepared operand lies in the interior or in the exterior
     * of the other geometry, so that its contribution to the matrix is known (see Operand.dims), and it is not
     * used. The location of one of its positions decides.
     */
    private void locateComponents(int g) {
        Operand operand = operands[g], other = operands[1 - g];
        PointLocator locator = keptList[g].length > MAX_SCANS && other.locator == null
                ? new PointLocator(other.elements) : null;
        inside[g] = new boolean[operand.componentCount];
        int count = 0;
        for (int c : keptList[g]) {
            int member = operand.members[operand.memberStart[c]];
            double x, y;
            if (member >= 0) {
                x = operand.chains[member].xs[0];
                y = operand.chains[member].ys[0];
            } else {
                double[] p = operand.elements.points.get(-1 - member);
                x = p[0];
                y = p[1];
            }
            int location = locator != null ? locator.roughLocation(x, y) : other.roughLocation(x, y);
            if (location == GeometryElements.ON) {   // not expected without contacts: use the component
                keptList[g][count++] = c;
                continue;
            }
            kept[g][c] = false;
            inside[g][c] = location == GeometryElements.INSIDE;
        }
        keptList[g] = Arrays.copyOf(keptList[g], count);
    }

    private void addElements(int g) {
        Operand operand = operands[g];
        if (kept[g] == null) {
            for (int i = 0; i < pointsOf[g].length; i++) addPoint(g, i);
            for (int k = 0; k < chainsOf[g].length; k++) addChain(g, k);
            return;
        }
        for (int c : keptList[g]) {
            for (int k = operand.memberStart[c]; k < operand.memberStart[c + 1]; k++) {
                int member = operand.members[k];
                if (member >= 0) addChain(g, member);
                else addPoint(g, -1 - member);
            }
        }
    }

    private void addPoint(int g, int i) {
        vertexNode(point(g, i), 0);
    }

    private void addChain(int g, int k) {
        Chain c = chain(g, k);
        c.registered = true;
        chains.add(c);
        vertexNode(c, c.data.lexMin);
        if (c.kind == LINE) {
            vertexNode(c, 0).endpoints[g]++;
            vertexNode(c, c.vertexCount - 1).endpoints[g]++;
        }
    }

    private static Seg seg(Chain c, int i) {
        Seg s = c.segs.get(i);
        if (s == null) {
            s = new Seg(c, i);
            c.segs.put(i, s);
        }
        return s;
    }

    // segment i of a monotone chain of geometry g
    private Seg seg(int g, MonotoneChain mc, int i) {
        return seg(g, mc.code(), i);
    }

    // segment i of a chain, or a point, of geometry g (see MonotoneChain.code)
    private Seg seg(int g, int code, int i) {
        return code < 0 ? point(g, -1 - code) : seg(chain(g, code), i);
    }

    private Chain chain(int g, int k) {
        Chain c = chainsOf[g][k];
        if (c == null) c = chainsOf[g][k] = new Chain(operands[g].chains[k], g, k);
        return c;
    }

    private Seg point(int g, int i) {
        Seg s = pointsOf[g][i];
        if (s == null) {
            double[] p = operands[g].elements.points.get(i);
            s = pointsOf[g][i] = new Seg(g, i, p[0], p[1]);
        }
        return s;
    }

    private static long key(double x, double y) {
        long h = Double.doubleToLongBits(x) * 0x9E3779B97F4A7C15L + Double.doubleToLongBits(y);
        return h ^ (h >>> 29);
    }

    // the node of a vertex of a chain
    private Node vertexNode(Chain c, int vertex) {
        Node n = c.nodes.get(vertex);
        if (n == null) {
            n = nodeAt(c.xs[vertex], c.ys[vertex]);
            c.nodes.put(vertex, n);
            c.mark(2 * vertex);
        }
        return n;
    }

    // the node of vertex 0 or 1 of a segment
    private Node vertexNode(Seg s, int end) {
        if (s.kind != POINT) return vertexNode(s.chain, s.vertex(end));
        if (s.point == null) {
            s.point = nodeAt(s.x0, s.y0);
            s.point.point[s.geom] = true;
        }
        return s.point;
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
        final int chain;        // the index of the chain in its operand; -1 for a point
        final int point;        // the index of the point; -1 for a chain
        final double[] xs, ys;  // the coordinates of the chain
        final double px, py;    // the point
        final int start, end;   // the segments start to end - 1, from vertex start to vertex end

        MonotoneChain(int chain, ChainData data, int start, int end) {
            this.chain = chain;
            this.point = -1;
            this.xs = data.xs;
            this.ys = data.ys;
            this.px = this.py = 0;
            this.start = start;
            this.end = end;
        }

        MonotoneChain(int point, double px, double py) {
            this.chain = -1;
            this.point = point;
            this.xs = this.ys = null;
            this.px = px;
            this.py = py;
            this.start = 0;
            this.end = 1;
        }

        double x(int vertex) {
            return xs == null ? px : xs[vertex];
        }

        double y(int vertex) {
            return ys == null ? py : ys[vertex];
        }

        // the chain index, or -1 - the point index
        int code() {
            return chain >= 0 ? chain : -1 - point;
        }
    }

    private static int quadrant(double dx, double dy) {
        return dx >= 0 ? (dy >= 0 ? 0 : 3) : (dy >= 0 ? 1 : 2);
    }

    /**
     * All pairs of segments with intersecting bounding boxes, except pairs of segments of rings of the same polygon:
     * the polygon is valid, so its rings meet only in isolated points, which do not change the topology of the
     * polygon. Where such a point is a node for another reason, the rings are split there afterwards.
     *
     * <p>The monotone chains of each geometry are in an R-tree; pairs within a geometry come from a self-join (if
     * it can have such pairs; a prepared operand has them already), pairs between the geometries from a join of
     * the two trees.</p>
     */
    private void findContacts() {
        // first the pairs between the geometries, which may decide the predicate
        MonotoneChain[] ma = operands[A].monotone, mb = operands[B].monotone;
        EnvelopeTree.join(operands[A].tree, operands[B].tree, (i, j) -> {
            MonotoneChain a = ma[i], b = mb[j];
            if (a.chain < 0 && b.chain < 0) {
                if (a.px == b.px && a.py == b.py) {
                    contactAB = true;
                    if (predicate == Predicate.INTERSECTS) decided = true;
                }
            } else {
                overlap(A, a, B, b);
            }
            return decided != null;
        });
        if (decided != null) return;
        for (int g = 0; g < 2; g++) {
            if (kept[g] == null) continue;
            if (!contactAB) locateComponents(g);
            addElements(g);
        }
        for (int g = 0; g < 2; g++) {
            Operand operand = operands[g];
            if (!operand.selfContacts) continue;
            if (operand.contacts != null) {
                int[] f = operand.contacts;
                if (kept[g] == null) {
                    for (int k = 0; k < f.length; k += 4) contact(seg(g, f[k], f[k + 1]), seg(g, f[k + 2], f[k + 3]));
                } else {
                    for (int c : keptList[g]) {
                        for (int k = 4 * operand.contactStart[c]; k < 4 * operand.contactStart[c + 1]; k += 4) {
                            contact(seg(g, f[k], f[k + 1]), seg(g, f[k + 2], f[k + 3]));
                        }
                    }
                }
                continue;
            }
            int geom = g;
            MonotoneChain[] mcs = operand.monotone;
            operand.tree.selfJoin((i, j) -> {
                MonotoneChain a = mcs[i], b = mcs[j];
                if (!operand.independent(a, b)) overlap(geom, a, geom, b);
                return false;
            });
        }
        // split the rings that pass through exact nodes
        List<Node> exact = new ArrayList<>();
        for (Node n : allNodes) if (n.exact) exact.add(n);
        for (int g = 0; g < 2; g++) {
            if (elements[g].polygonCount == 0) continue;
            MonotoneChain[] mcs = operands[g].monotone;
            Chain[] own = chainsOf[g];
            for (Node n : exact) {
                operands[g].tree.query(n.x, n.y, n.x, n.y, i -> {
                    MonotoneChain mc = mcs[i];
                    // a chain that is not used does not pass through the node: it would be in the same component
                    if (mc.chain >= 0 && own[mc.chain] != null && own[mc.chain].registered && own[mc.chain].kind == RING) {
                        splitAt(own[mc.chain], mc, mc.start, mc.end, n);
                    }
                });
            }
        }
    }

    // if the exact node lies on segments v0 to v1 - 1 of the monotone chain of c, it is a node of c there
    private void splitAt(Chain c, MonotoneChain mc, int v0, int v1, Node n) {
        double x0 = mc.x(v0), x1 = mc.x(v1), y0 = mc.y(v0), y1 = mc.y(v1);
        if (n.x < Math.min(x0, x1) || n.x > Math.max(x0, x1) || n.y < Math.min(y0, y1) || n.y > Math.max(y0, y1)) {
            return;
        }
        if (v1 - v0 > 1) {
            int mid = (v0 + v1) >>> 1;
            splitAt(c, mc, v0, mid, n);
            splitAt(c, mc, mid, v1, n);
            return;
        }
        if (n.x == x0 && n.y == y0) {
            vertexNode(c, v0);
        } else if (n.x == x1 && n.y == y1) {
            vertexNode(c, c.closed && v1 == c.vertexCount ? 0 : v1);
        } else if (Predicates.orientation(x0, y0, x1, y1, n.x, n.y) == 0) {
            seg(c, v0).addIncidence(n, null);
        }
    }

    // classifies the pairs of segments of monotone chains a (of geometry ga) and b (of gb) that may meet
    private void overlap(int ga, MonotoneChain a, int gb, MonotoneChain b) {
        Operand operand = operands[ga];
        pairs(a, a.start, a.end, b, b.start, b.end, (i, j) -> {
            if (ga != gb || !operand.consecutive(a, i, b, j)) contact(seg(ga, a, i), seg(gb, b, j));
            return decided != null;
        });
    }

    /**
     * Calls the action for the pairs of segments a0 to a1 - 1 of a and b0 to b1 - 1 of b with intersecting bounding
     * boxes, until it returns true
     *
     * @return true if the action returned true
     */
    static boolean pairs(MonotoneChain a, int a0, int a1, MonotoneChain b, int b0, int b1,
                         EnvelopeTree.PairConsumer action) {
        // the bounding box of a monotone run is that of its end vertices
        double ax0 = a.x(a0), ax1 = a.x(a1), bx0 = b.x(b0), bx1 = b.x(b1);
        if (Math.max(ax0, ax1) < Math.min(bx0, bx1) || Math.min(ax0, ax1) > Math.max(bx0, bx1)) return false;
        double ay0 = a.y(a0), ay1 = a.y(a1), by0 = b.y(b0), by1 = b.y(b1);
        if (Math.max(ay0, ay1) < Math.min(by0, by1) || Math.min(ay0, ay1) > Math.max(by0, by1)) return false;
        if (a1 - a0 == 1 && b1 - b0 == 1) return action.accept(a0, b0);
        if (a1 - a0 >= b1 - b0) {
            int mid = (a0 + a1) >>> 1;
            return pairs(a, a0, mid, b, b0, b1, action) || pairs(a, mid, a1, b, b0, b1, action);
        }
        int mid = (b0 + b1) >>> 1;
        return pairs(a, a0, a1, b, b0, mid, action) || pairs(a, a0, a1, b, mid, b1, action);
    }

    private void contact(Seg s, Seg t) {
        SegmentIntersection kind = Predicates.segmentIntersection(s.x0, s.y0, s.x1, s.y1, t.x0, t.y0, t.x1, t.y1);
        if (kind != SegmentIntersection.DISJOINT && s.geom != t.geom) contactAB = true;
        switch (kind) {
            case DISJOINT:
                return;
            case CROSSING: {
                double[] xy = Constructions.crossingPoint(s.x0, s.y0, s.x1, s.y1, t.x0, t.y0, t.x1, t.y1);
                if (predicate != Predicate.NONE && s.geom != t.geom) {
                    decideAtCrossing(s.geom == A ? s : t, s.geom == A ? t : s, xy[0], xy[1]);
                    if (decided != null) return;
                }
                Node n = crossingNode(s, t, xy[0], xy[1]);
                s.addIncidence(n, n.exact ? null : t);
                t.addIncidence(n, n.exact ? null : s);
                return;
            }
            case TOUCHING:
                if (predicate == Predicate.INTERSECTS && s.geom != t.geom) {
                    decided = true;
                    return;
                }
                endpointOn(s, 0, t);
                if (s.kind != POINT) endpointOn(s, 1, t);
                endpointOn(t, 0, s);
                if (t.kind != POINT) endpointOn(t, 1, s);
                return;
            default: {
                if (predicate == Predicate.INTERSECTS && s.geom != t.geom) {
                    decided = true;
                    return;
                }
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

    /**
     * Decides the predicate, if possible, from a proper crossing of segment s of A and segment t of B, near the
     * rounded crossing point (x, y). If both geometries are locally simple and no other segment passes (near) the
     * crossing point, the neighbourhood of the crossing point consists of s and t and the parts of the plane they
     * separate: both geometries have points outside the other, and their interiors meet.
     */
    private void decideAtCrossing(Seg s, Seg t, double x, double y) {
        if (predicate == Predicate.INTERSECTS) {
            decided = true;
            return;
        }
        if (!simple[A] || !simple[B] || !alone(A, s, x, y) || !alone(B, t, x, y)) return;
        switch (predicate) {
            case TOUCHES:
            case CONTAINMENT:
                decided = false;
                return;
            case CROSSES:
                if (s.kind != t.kind) decided = true;    // a line crossing the boundary of an area
                return;
            case OVERLAPS:
                if (s.kind == RING && t.kind == RING) decided = true;
                return;
            default:
        }
    }

    // is s the only segment of geometry g whose bounding box meets the box of an ulp around (x, y)?
    private boolean alone(int g, Seg s, double x, double y) {
        double ex = Math.ulp(x), ey = Math.ulp(y);
        double x0 = x - ex, x1 = x + ex, y0 = y - ey, y1 = y + ey;
        MonotoneChain[] mcs = operands[g].monotone;
        boolean[] other = {false};
        operands[g].tree.query(x0, y0, x1, y1, i -> {
            if (!other[0]) other[0] = touchesBox(mcs[i], mcs[i].start, mcs[i].end, s, x0, y0, x1, y1);
        });
        return !other[0];
    }

    // does a segment v0 to v1 - 1 of the monotone chain, other than s, meet the box?
    private static boolean touchesBox(MonotoneChain mc, int v0, int v1, Seg s, double x0, double y0, double x1,
                                      double y1) {
        double ax = mc.x(v0), bx = mc.x(v1), ay = mc.y(v0), by = mc.y(v1);
        if (Math.max(ax, bx) < x0 || Math.min(ax, bx) > x1 || Math.max(ay, by) < y0 || Math.min(ay, by) > y1) {
            return false;
        }
        if (v1 - v0 > 1) {
            int mid = (v0 + v1) >>> 1;
            return touchesBox(mc, v0, mid, s, x0, y0, x1, y1) || touchesBox(mc, mid, v1, s, x0, y0, x1, y1);
        }
        if (mc.chain < 0) return !(s.chain == null && s.index == mc.point);
        return !(s.chain != null && s.chain.index == mc.chain && v0 == s.index);
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
            int n = c.xs.length - 1;   // the number of segments
            int count = c.markCount;
            int[] marks = c.marks;
            Arrays.sort(marks, 0, count);
            int start = c.closed ? c.data.lexMin : 0;   // a node
            int p = Arrays.binarySearch(marks, 0, count, 2 * start);
            Node from = c.node(start);
            int first = start;   // the segment in which the current edge starts
            // the marks after the start, in order along the chain; for a ring, back to the start
            int steps = c.closed ? count : count - 1 - p;
            for (int k = 1; k <= steps; k++) {
                int mark = marks[(p + k) % count];
                if ((mark & 1) == 1) {
                    int i = mark >> 1;
                    Seg s = c.seg(i);
                    s.incidences.sort((i1, i2) -> compareAlong(s, i1, i2));
                    Node previous = null;
                    for (Incidence incidence : s.incidences) {
                        if (incidence.node == previous) continue;
                        addEdge(c, from, first, incidence.node, i);
                        from = previous = incidence.node;
                        first = i;
                    }
                } else {
                    int v = mark >> 1;
                    Node end = c.node(v);
                    addEdge(c, from, first, end, (v + n - 1) % n);
                    from = end;
                    first = v;
                }
            }
        }
    }

    // adds the edge from u to v, which leaves u along segment first and arrives at v along segment last
    private void addEdge(Chain c, Node u, int first, Node v, int last) {
        Seg firstSeg = c.seg(first);
        boolean shared = first == last && firstSeg != null && firstSeg.overlapping;
        long key = u.id < v.id ? ((long) u.id << 32) | v.id : ((long) v.id << 32) | u.id;
        if (shared) {
            Edge e = sharedEdges.get(key);
            if (e != null) {
                e.add(c, e.n0 == u ? 1 : -1);
                return;
            }
        }
        Edge e = new Edge(u, v);
        e.chain = c;
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
     * lexicographically smallest vertex of a component (see {@link PointLocator#countContainingLeft}). A prepared
     * operand has a point locator; otherwise, for a few components, the rings are scanned, and for more, a point
     * locator with an index is built.
     */
    private int countContainingLeft(int g, double x, double y) {
        if (elements[g].polygonCount == 0) return 0;
        if (operands[g].locator != null) return operands[g].locator.countContainingLeft(x, y);
        if (locators[g] == null && scans[g]++ < MAX_SCANS) return scanContainingLeft(g, x, y);
        if (locators[g] == null) locators[g] = new PointLocator(elements[g]);
        return locators[g].countContainingLeft(x, y);
    }

    // for each polygon, the parity of the crossings of the ray to the right of (x, y) with its rings that do not
    // pass through (x, y)
    private int scanContainingLeft(int g, double x, double y) {
        Set<Integer> odd = new HashSet<>();
        for (Chain chain : chainsOf[g]) {
            if (chain == null) continue;
            ChainData c = chain.data;
            if (c.kind != RING || y < c.minY || y > c.maxY || x > c.maxX) continue;
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
        // the components that were not used lie in the exterior of the other geometry
        for (int g = 0; g < 2; g++) {
            if (kept[g] == null) continue;
            Operand operand = operands[g];
            for (int loc = I; loc <= BD; loc++) {
                // the highest dimension of the components in the exterior, and of those in the interior
                for (int c : operand.byDim[loc]) {
                    if (kept[g][c] || (inside[g] != null && inside[g][c])) continue;
                    int d = operand.dims[loc][c];
                    if (g == A) set(m, loc, E, d);
                    else set(m, E, loc, d);
                    break;
                }
                if (inside[g] == null) continue;
                for (int c : operand.byDim[loc]) {
                    if (!inside[g][c]) continue;
                    int d = operand.dims[loc][c];
                    if (g == A) set(m, loc, I, d);
                    else set(m, I, loc, d);
                    break;
                }
            }
        }
        return new IntersectionMatrix(m, elements[A].dimension, elements[B].dimension);
    }

    /**
     * For a computation of a prepared operand A against the empty geometry: per location (I, BD) in A and per
     * component of A, the highest dimension of the cells of the component in that location; -1 if none
     */
    int[][] componentDims() {
        compute();
        Operand operand = operands[A];
        int[][] dims = new int[2][operand.componentCount];
        for (int[] d : dims) Arrays.fill(d, -1);
        for (Edge e : edges) {
            int c = operand.componentOf(e.chain.index);
            note(dims, c, edgeLocation(e, A), 1);
            note(dims, c, faceLocation(e.left[A]), 2);
            note(dims, c, faceLocation(e.right[A]), 2);
        }
        for (Node n : allNodes) {
            if (!n.star.isEmpty()) note(dims, operand.componentOf(n.star.get(0).edge.chain.index), nodeLocation(n, A), 0);
        }
        for (Seg p : pointsOf[A]) {
            if (p != null && p.point.star.isEmpty()) note(dims, operand.componentOf(-1 - p.index), nodeLocation(p.point, A), 0);
        }
        return dims;
    }

    private static void note(int[][] dims, int component, int location, int dimension) {
        if (location != E) dims[location][component] = Math.max(dims[location][component], dimension);
    }

    private static void set(int[] m, int a, int b, int dimension) {
        m[3 * a + b] = Math.max(m[3 * a + b], dimension);
    }
}
