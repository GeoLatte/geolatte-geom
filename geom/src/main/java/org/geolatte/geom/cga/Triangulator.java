package org.geolatte.geom.cga;

import org.geolatte.geom.cga.exact.Predicates;

import java.util.Arrays;

/**
 * Incremental construction of the Delaunay triangulation of a set of distinct points, with exact predicates.
 *
 * <p>This is the incremental algorithm of Ratschek &amp; Rokne (Sec. 8.3.7, Algorithm 23): each point is located
 * in the current triangulation with orientation tests, the triangle (or the two triangles sharing the edge)
 * containing it is split, and the edges opposite to the new point are legalized by flipping, as decided by the
 * in-circle test (procedure LEGALIZEEDGE). Both tests are exact ({@link Predicates#orientation(double, double,
 * double, double, double, double)} and {@link Predicates#inCircle(double, double, double, double, double, double,
 * double, double)}). Compared to the book:</p>
 * <ul>
 *     <li>Instead of an initial triangle with finite vertices that encloses all points, a symbolic vertex at
 *     infinity is used: every edge of the convex hull is the base of a "ghost" triangle with the infinite vertex
 *     as apex. A point outside the convex hull is in conflict with a ghost triangle if it lies strictly on the
 *     outer side of its base. With finite enclosing vertices, the triangles near the convex hull may be wrong
 *     (they are only correct in the limit of infinitely distant vertices).</li>
 *     <li>Cocircular points (a zero in-circle determinant), which the book excludes, are handled by the symbolic
 *     perturbation of O. Devillers and M. Teillaud ("Perturbations for Delaunay and weighted Delaunay 3D
 *     triangulations", Comput. Geom. 44(3), 2011), as in CGAL: the result is the unique Delaunay triangulation
 *     of the perturbed points, which does not depend on the insertion order.</li>
 *     <li>The points are inserted in the order of a Hilbert curve, and located by a (stochastic) walk from the
 *     previously inserted point, which gives an expected running time of about O(n log n) for practical inputs.
 *     </li>
 * </ul>
 *
 * <p>Triangles are stored in flat arrays: triangle {@code t} has vertices {@code tv[3t..3t+2]} in
 * counterclockwise order, and {@code tn[3t+k]} is the triangle across the edge opposite vertex {@code k}.</p>
 */
final class Triangulator {

    static final int INFINITE = -1;

    private final double[] xs, ys;
    private int[] tv = new int[48];
    private int[] tn = new int[48];
    private int count;
    private int[] stack = new int[64];
    private long random = 0x9E3779B97F4A7C15L;

    // the result: real triangles in canonical order, and their neighbors (-1 across the convex hull)
    int[] triangles;
    int[] neighbors;

    Triangulator(double[] xs, double[] ys) {
        this.xs = xs;
        this.ys = ys;
    }

    /**
     * Triangulates the points (which must be distinct). Returns false if all points are collinear (then there are
     * no triangles).
     */
    boolean triangulate() {
        int n = xs.length;
        triangles = new int[0];
        neighbors = new int[0];
        if (n < 3) return false;
        int[] order = hilbertOrder();
        int a = order[0], b = order[1], first = -1;
        for (int i = 2; i < n; i++) {
            if (orientation(a, b, order[i]) != 0) {
                first = i;
                break;
            }
        }
        if (first < 0) return false;
        int c = order[first];
        if (orientation(a, b, c) < 0) {
            int s = a;
            a = b;
            b = s;
        }
        initialize(a, b, c);
        int last = 0;
        for (int i = 2; i < n; i++) {
            if (i == first) continue;
            last = insert(order[i], last);
        }
        extract();
        return true;
    }

    // ---------------------------------------------------------------------------------------------------------
    // the triangulation
    // ---------------------------------------------------------------------------------------------------------

    private void initialize(int a, int b, int c) {
        int t = newTriangle(a, b, c);
        int g1 = newTriangle(b, a, INFINITE), g2 = newTriangle(c, b, INFINITE), g3 = newTriangle(a, c, INFINITE);
        setNeighbors(t, g2, g3, g1);
        setNeighbors(g1, g3, g2, t);
        setNeighbors(g2, g1, g3, t);
        setNeighbors(g3, g2, g1, t);
    }

    /**
     * Inserts point p, starting the search at the real triangle start. Returns a real triangle that has p as
     * vertex.
     */
    private int insert(int p, int start) {
        int location = locate(p, start);
        int t = location >> 2, edge = location & 3;
        if (edge == 3) {
            splitTriangle(t, p);
        } else {
            splitEdge(t, edge, p);
        }
        legalize();
        // slot t contains p (flips keep p in the slot); if it is a ghost, its finite edge leads to a real triangle
        if (!isGhost(t)) return t;
        return tn[3 * t + indexOf(t, INFINITE)];
    }

    /**
     * Walks from the real triangle start towards p. Returns 4 * t + k if p lies on the edge opposite vertex k of
     * real triangle t, 4 * t + 3 if p lies in the interior of real triangle t, or in the conflict region of ghost
     * triangle t (strictly outside the convex hull edge of t).
     *
     * <p>The walk is a visibility walk: it crosses an edge if p lies strictly on the other side. The first edge
     * tested is chosen at random (with a fixed seed), which guarantees termination.</p>
     */
    private int locate(int p, int start) {
        int t = start, previous = -1;
        walk:
        while (true) {
            if (isGhost(t)) return 4 * t + 3;
            int r = nextRandom3();
            int onEdge = 3;
            for (int i = 0; i < 3; i++) {
                int k = (r + i) % 3;
                int neighbor = tn[3 * t + k];
                if (neighbor == previous) continue;   // p is strictly on this side of the edge we came through
                int o = orientation(tv[3 * t + (k + 1) % 3], tv[3 * t + (k + 2) % 3], p);
                if (o < 0) {
                    previous = t;
                    t = neighbor;
                    continue walk;
                }
                if (o == 0) onEdge = k;
            }
            return 4 * t + onEdge;
        }
    }

    // splits triangle t (real or ghost) at p into three; pushes the edges opposite p on the stack
    private void splitTriangle(int t, int p) {
        int v0 = tv[3 * t], v1 = tv[3 * t + 1], v2 = tv[3 * t + 2];
        int n0 = tn[3 * t], n1 = tn[3 * t + 1], n2 = tn[3 * t + 2];
        set(t, v0, v1, p);
        int t1 = newTriangle(v1, v2, p), t2 = newTriangle(v2, v0, p);
        setNeighbors(t, t1, t2, n2);
        setNeighbors(t1, t2, t, n0);
        setNeighbors(t2, t, t1, n1);
        replaceNeighbor(n0, t, t1);
        replaceNeighbor(n1, t, t2);
        push(t, 2);
        push(t1, 2);
        push(t2, 2);
    }

    // splits real triangle t and its neighbor across the edge opposite vertex k at p, which lies on that edge
    private void splitEdge(int t, int k, int p) {
        int c = tv[3 * t + k], a = tv[3 * t + (k + 1) % 3], b = tv[3 * t + (k + 2) % 3];
        int u = tn[3 * t + k];
        int j = indexOfNeighbor(u, t);
        int d = tv[3 * u + j];   // may be the infinite vertex
        int nbc = tn[3 * t + (k + 1) % 3], nca = tn[3 * t + (k + 2) % 3];
        int nad = tn[3 * u + (j + 1) % 3], ndb = tn[3 * u + (j + 2) % 3];
        // u = (d, b, a): the neighbor opposite b is across (a, d), opposite a is across (d, b)
        set(t, c, a, p);
        int t1 = newTriangle(c, p, b);
        set(u, d, b, p);
        int u1 = newTriangle(d, p, a);
        setNeighbors(t, u1, t1, nca);
        setNeighbors(t1, u, nbc, t);
        setNeighbors(u, t1, u1, ndb);
        setNeighbors(u1, t, nad, u);
        replaceNeighbor(nbc, t, t1);
        replaceNeighbor(nad, u, u1);
        push(t, 2);
        push(t1, 1);
        push(u, 2);
        push(u1, 1);
    }

    /**
     * Procedure LEGALIZEEDGE, with an explicit stack of (triangle, index of the new point): the edge opposite the
     * new point is flipped if the new point is in conflict with the triangle on the other side.
     */
    private void legalize() {
        while (top > 0) {
            int k = stack[--top];
            int t = stack[--top];
            int p = tv[3 * t + k];
            int u = tn[3 * t + k];
            int j = indexOfNeighbor(u, t);
            if (!inConflict(u, j, p)) continue;
            // t = (p, a, b), u = (d, b, a)  ->  t = (p, a, d), u = (p, d, b)
            int a = tv[3 * t + (k + 1) % 3], b = tv[3 * t + (k + 2) % 3], d = tv[3 * u + j];
            int nta = tn[3 * t + (k + 1) % 3], ntb = tn[3 * t + (k + 2) % 3];
            int nub = tn[3 * u + (j + 1) % 3], nua = tn[3 * u + (j + 2) % 3];
            set(t, p, a, d);
            set(u, p, d, b);
            setNeighbors(t, nub, u, ntb);
            setNeighbors(u, nua, nta, t);
            replaceNeighbor(nub, u, t);
            replaceNeighbor(nta, t, u);
            push(t, 0);
            push(u, 0);
        }
    }

    /**
     * Is p (a real point) in conflict with triangle u, i.e. inside its circumcircle? p lies across the edge
     * opposite vertex j of u. For a ghost triangle, the "circumcircle" is the open half plane on the outer side of
     * its finite edge; points on the line through that edge are not in conflict.
     */
    private boolean inConflict(int u, int j, int p) {
        int v0 = tv[3 * u], v1 = tv[3 * u + 1], v2 = tv[3 * u + 2];
        if (v0 == INFINITE) return orientation(v1, v2, p) > 0;
        if (v1 == INFINITE) return orientation(v2, v0, p) > 0;
        if (v2 == INFINITE) return orientation(v0, v1, p) > 0;
        return inCirclePerturbed(v0, v1, v2, p) > 0;
    }

    /**
     * The in-circle test for counterclockwise a, b, c, with the symbolic perturbation of Devillers and Teillaud:
     * if d lies exactly on the circle, the points are perturbed infinitesimally, with a perturbation that grows
     * with their lexicographic (x, y) order. Then the sign is decided by the leading non-zero coefficient of the
     * perturbed determinant, which is an orientation (at most two are needed). Never returns 0.
     */
    int inCirclePerturbed(int a, int b, int c, int d) {
        int s = Predicates.inCircle(xs[a], ys[a], xs[b], ys[b], xs[c], ys[c], xs[d], ys[d]);
        if (s != 0) return s;
        int[] points = {a, b, c, d};
        // sort by lexicographic order (insertion sort of 4 distinct points)
        for (int i = 1; i < 4; i++) {
            for (int m = i; m > 0 && compareXY(points[m - 1], points[m]) > 0; m--) {
                int tmp = points[m];
                points[m] = points[m - 1];
                points[m - 1] = tmp;
            }
        }
        for (int i = 3; i > 1; i--) {
            int q = points[i];
            if (q == d) return -1;
            int o;
            if (q == c && (o = orientation(a, b, d)) != 0) return o;
            if (q == b && (o = orientation(a, d, c)) != 0) return o;
            if (q == a && (o = orientation(d, b, c)) != 0) return o;
        }
        throw new IllegalStateException("Degenerate perturbed in-circle test");
    }

    // ---------------------------------------------------------------------------------------------------------
    // result
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Collects the real triangles, each rotated to start at its smallest vertex index, sorted lexicographically.
     */
    private void extract() {
        int n = xs.length;
        int real = 0;
        for (int t = 0; t < count; t++) {
            if (!isGhost(t)) real++;
        }
        // bucket sort on the first (smallest) vertex; within a bucket, sort on the second vertex (a directed edge
        // belongs to at most one triangle, so the first two vertices identify a triangle)
        int[] rotation = new int[count];
        int[] bucketStart = new int[n + 1];
        for (int t = 0; t < count; t++) {
            if (isGhost(t)) continue;
            int r = 0;
            if (tv[3 * t + 1] < tv[3 * t + r]) r = 1;
            if (tv[3 * t + 2] < tv[3 * t + r]) r = 2;
            rotation[t] = r;
            bucketStart[tv[3 * t + r] + 1]++;
        }
        for (int v = 0; v < n; v++) bucketStart[v + 1] += bucketStart[v];
        int[] fill = Arrays.copyOf(bucketStart, n);
        int[] sorted = new int[real];
        for (int t = 0; t < count; t++) {
            if (!isGhost(t)) sorted[fill[tv[3 * t + rotation[t]]]++] = t;
        }
        for (int v = 0; v < n; v++) {
            for (int i = bucketStart[v] + 1; i < bucketStart[v + 1]; i++) {
                for (int m = i; m > bucketStart[v] && second(sorted[m - 1], rotation) > second(sorted[m], rotation); m--) {
                    int tmp = sorted[m];
                    sorted[m] = sorted[m - 1];
                    sorted[m - 1] = tmp;
                }
            }
        }
        int[] newIndex = new int[count];
        Arrays.fill(newIndex, -1);
        for (int i = 0; i < real; i++) newIndex[sorted[i]] = i;
        triangles = new int[3 * real];
        neighbors = new int[3 * real];
        for (int i = 0; i < real; i++) {
            int t = sorted[i], r = rotation[t];
            for (int k = 0; k < 3; k++) {
                triangles[3 * i + k] = tv[3 * t + (r + k) % 3];
                neighbors[3 * i + k] = newIndex[tn[3 * t + (r + k) % 3]];
            }
        }
    }

    private int second(int t, int[] rotation) {
        return tv[3 * t + (rotation[t] + 1) % 3];
    }

    // ---------------------------------------------------------------------------------------------------------
    // insertion order
    // ---------------------------------------------------------------------------------------------------------

    // the point indices sorted along a Hilbert curve over the bounding box (on a 2^16 x 2^16 grid). Rounding
    // errors in the grid coordinates only affect the order, not the result.
    private int[] hilbertOrder() {
        int n = xs.length;
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }
        double sx = gridScale(minX, maxX), sy = gridScale(minY, maxY);
        long[] keys = new long[n];
        for (int i = 0; i < n; i++) {
            int gx = (int) Math.min(65535, Math.max(0, (0.5 * xs[i] - 0.5 * minX) * sx));
            int gy = (int) Math.min(65535, Math.max(0, (0.5 * ys[i] - 0.5 * minY) * sy));
            keys[i] = ((long) hilbert(gx, gy) << 32) | i;
        }
        Arrays.sort(keys);
        int[] order = new int[n];
        for (int i = 0; i < n; i++) order[i] = (int) keys[i];
        return order;
    }

    private static double gridScale(double min, double max) {
        double halfRange = 0.5 * max - 0.5 * min;
        double s = 65535 / halfRange;
        return halfRange > 0 && Double.isFinite(s) ? s : 0;
    }

    // the index of grid cell (x, y) along the Hilbert curve of order 16
    private static int hilbert(int x, int y) {
        int d = 0;
        for (int s = 1 << 15; s > 0; s >>= 1) {
            int rx = (x & s) > 0 ? 1 : 0;
            int ry = (y & s) > 0 ? 1 : 0;
            d += s * s * ((3 * rx) ^ ry);
            if (ry == 0) {
                if (rx == 1) {
                    x = s - 1 - x;
                    y = s - 1 - y;
                }
                int tmp = x;
                x = y;
                y = tmp;
            }
        }
        return d;
    }

    // ---------------------------------------------------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------------------------------------------------

    private int top;

    private void push(int t, int k) {
        if (top + 2 > stack.length) stack = Arrays.copyOf(stack, 2 * stack.length);
        stack[top++] = t;
        stack[top++] = k;
    }

    private int newTriangle(int a, int b, int c) {
        if (3 * count + 3 > tv.length) {
            tv = Arrays.copyOf(tv, 2 * tv.length);
            tn = Arrays.copyOf(tn, 2 * tn.length);
        }
        set(count, a, b, c);
        return count++;
    }

    private void set(int t, int a, int b, int c) {
        tv[3 * t] = a;
        tv[3 * t + 1] = b;
        tv[3 * t + 2] = c;
    }

    private void setNeighbors(int t, int n0, int n1, int n2) {
        tn[3 * t] = n0;
        tn[3 * t + 1] = n1;
        tn[3 * t + 2] = n2;
    }

    private void replaceNeighbor(int t, int old, int replacement) {
        tn[3 * t + indexOfNeighbor(t, old)] = replacement;
    }

    private int indexOfNeighbor(int t, int neighbor) {
        if (tn[3 * t] == neighbor) return 0;
        if (tn[3 * t + 1] == neighbor) return 1;
        if (tn[3 * t + 2] == neighbor) return 2;
        throw new IllegalStateException("Not a neighbor");
    }

    private int indexOf(int t, int vertex) {
        if (tv[3 * t] == vertex) return 0;
        if (tv[3 * t + 1] == vertex) return 1;
        return 2;
    }

    private boolean isGhost(int t) {
        return tv[3 * t] == INFINITE || tv[3 * t + 1] == INFINITE || tv[3 * t + 2] == INFINITE;
    }

    private int orientation(int a, int b, int c) {
        return Predicates.orientation(xs[a], ys[a], xs[b], ys[b], xs[c], ys[c]);
    }

    private int compareXY(int a, int b) {
        int c = Double.compare(xs[a] + 0.0, xs[b] + 0.0);   // + 0.0 turns -0.0 into 0.0
        return c != 0 ? c : Double.compare(ys[a] + 0.0, ys[b] + 0.0);
    }

    // xorshift; deterministic
    private int nextRandom3() {
        random ^= random << 13;
        random ^= random >>> 7;
        random ^= random << 17;
        return (int) ((random >>> 33) % 3);
    }
}
