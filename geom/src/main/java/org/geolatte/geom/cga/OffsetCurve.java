package org.geolatte.geom.cga;

import org.geolatte.geom.SegmentIntersection;
import org.geolatte.geom.cga.exact.Constructions;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.ArrayList;
import java.util.List;

/**
 * The raw offset curve of a closed path, to its right, at a positive distance d. Used by {@link Buffer}.
 *
 * <p>The curve consists of the offset segments of the segments of the path, joined at each vertex:</p>
 * <ul>
 *     <li>at an outside turn (a turn to the left, so the right side is convex), and at a reversal, by the join:
 *     an arc around the vertex, a mitre or a bevel. A reversal at a cap vertex (the end of a line) is joined by
 *     the end cap instead;</li>
 *     <li>at an inside turn, through the crossing point of the two offset segments if they cross and the region it
 *     cuts off lies in both quadrilaterals (see {@code canShortcut}), and otherwise through the vertex itself (a
 *     spike): from the end of the first offset segment to the vertex, and on to the start of the second;</li>
 *     <li>on a straight continuation, by connecting the two offset endpoints.</li>
 * </ul>
 * <p>The kind of turn, and whether the offset segments cross, are decided exactly.</p>
 *
 * <p>The winding number of the curve is that of the path, plus the number of pieces that contain the point: the
 * quadrilaterals between the segments and their offsets, and the wedges of the joins and caps. See
 * {@link Buffer} for the argument. For a path with spikes, {@link #local} splits that sum, so that the spikes can be
 * dealt with locally.</p>
 */
final class OffsetCurve {

    private static final byte STRAIGHT = 0, INSIDE = 1, SHORTCUT = 2, OUTSIDE = 3, REVERSAL = 4, CAP = 5, SPIKE = 6;
    // a spike that comes near more segments than this is long
    private static final int NEAR = 8;

    private final double d;
    private final BufferParameters parameters;
    private final double quantum;

    // the path
    private double[] px, py;
    private int k;
    private int capA, capB;
    // per segment j (from vertex j to vertex j + 1): the unit direction, and the offset segment
    private double[] ux, uy, sx, sy, ex, ey;
    // per segment the offset distance, or null for d
    private double[] depth;
    // the bounding boxes of the segments, built when needed
    private EnvelopeTree segmentTree;
    // the curve
    private double[] cx, cy;
    private int n;
    // per vertex: the kind of join
    private byte[] kind;
    // the number of long spikes, and of inside turns that can be joined through the crossing point of the offset
    // segments
    private int spikes, shortcuts;

    OffsetCurve(double distance, BufferParameters parameters) {
        this.d = distance;
        this.parameters = parameters;
        this.quantum = Math.PI / 2 / parameters.getQuadrantSegments();
    }

    /**
     * Sets the closed path through the vertices, in order (the path returns from the last vertex to the first one).
     *
     * @param xs   the X coordinates of the vertices; consecutive vertices differ, also the last and the first
     * @param ys   the Y coordinates
     * @param k    the number of vertices, at least 2
     * @param capA the index of a vertex at which the path reverses as the end of a line, or -1
     * @param capB the index of the other such vertex, or -1
     */
    void path(double[] xs, double[] ys, int k, int capA, int capB) {
        path(xs, ys, k, capA, capB, null);
    }

    /**
     * Sets the closed path through the vertices, with an offset distance per segment: the quadrilateral of segment j
     * (from vertex j to vertex j + 1) has that depth. The joins and caps still use the distance d around the
     * vertices; where a quadrilateral is less deep, the curve steps out along the normal to the arc.
     *
     * @param depth per segment the offset distance, at most d; or null for d everywhere
     */
    void path(double[] xs, double[] ys, int k, int capA, int capB, double[] depth) {
        this.depth = depth;
        this.px = xs;
        this.py = ys;
        this.k = k;
        this.capA = capA;
        this.capB = capB;
        segments();
        kinds();
    }

    /**
     * @return the number of long spikes: inside turns of the path that the curve passes through the vertex, where
     * that may cross many other parts of the curve
     */
    int spikes() {
        return spikes;
    }

    /**
     * @return the offset curve of the path, closed, with its first point repeated at the end; or null if it has
     * fewer than 3 distinct points
     */
    double[][] curve() {
        start(2 * k + 8);
        // if every vertex is an inside turn with a shortcut, the shortcut regions could all contain a point that only
        // their quadrilaterals cover: then one vertex is joined through itself
        boolean all = shortcuts == k;
        for (int i = 0; i < k; i++) join(i, !all || i != 0);
        return finish();
    }

    /**
     * The offset curve with its long spikes handled per group. They are grouped: consecutive long spikes at most
     * {@code gap} vertices apart are in the same group, which runs from the vertex before its first spike to the
     * vertex after its last one. Each group is cut into chunks of consecutive vertices with at most
     * {@code maxSpikes} long spikes each. The curve of a chunk runs from its first vertex along the offset curve to its
     * last vertex, and back along the path; its winding number is the number of pieces of the chunk that contain the
     * point (the quadrilaterals of its segments and the wedges at its vertices, the wedge at its first vertex
     * included, the one at its last vertex not).
     *
     * <p>The returned curve is the offset curve with the chunks taken out: it follows the path through the groups.
     * Its winding number plus the sum of those of the chunks is the winding number of the path plus the number of
     * pieces that contain the point.</p>
     *
     * @param groups receives the curves of the chunks, per group
     * @return the curve, closed, with its first point repeated at the end; or null if it has fewer than 3 distinct
     * points
     */
    double[][] local(int maxSpikes, int gap, List<List<double[][]>> groups) {
        int m = spikes;
        int[] at = new int[m];
        for (int i = 0, j = 0; i < k; i++) if (kind[i] == SPIKE) at[j++] = i;
        // a spike after a gap larger than the given one
        int first = -1;
        for (int j = 0; j < m && first < 0; j++) {
            int length = m == 1 ? k : Math.floorMod(at[j] - at[j == 0 ? m - 1 : j - 1], k);
            if (length > gap) first = j;
        }
        if (first < 0) {
            // a single group around the whole path: the curve is the path itself
            List<double[][]> chunks = new ArrayList<>();
            int s = at[0], count = 0;
            for (int j = 1; j < m; j++) {
                if (++count > maxSpikes) {
                    add(chunks, chunk(s, at[j]));
                    s = at[j];
                    count = 0;
                }
            }
            add(chunks, chunk(s, at[0] + k));
            groups.add(chunks);
            start(k + 2);
            for (int i = 0; i < k; i++) add(px[i], py[i]);
            return finish();
        }
        // the spikes from the first one on, with their indices unwrapped
        int[] u = new int[m];
        for (int j = 0; j < m; j++) {
            int i = at[(first + j) % m];
            u[j] = i < at[first] ? i + k : i;
        }
        // the groups, as ranges of unwrapped vertex indices
        List<int[]> ranges = new ArrayList<>();
        for (int j = 0; j < m; ) {
            int last = j;
            while (last + 1 < m && u[last + 1] - u[last] <= gap) last++;
            int a = u[j] - 1, b = u[last] + 1;
            List<double[][]> chunks = new ArrayList<>();
            int s = a, count = 0;
            for (int t = j; t <= last; t++) {
                if (++count > maxSpikes) {
                    add(chunks, chunk(s, u[t]));
                    s = u[t];
                    count = 0;
                }
            }
            add(chunks, chunk(s, b));
            groups.add(chunks);
            ranges.add(new int[]{a, b});
            j = last + 1;
        }
        start(2 * k + 8);
        for (int g = 0; g < ranges.size(); g++) {
            int a = ranges.get(g)[0], b = ranges.get(g)[1];
            int next = g + 1 < ranges.size() ? ranges.get(g + 1)[0] : ranges.get(0)[0] + k;
            for (int i = a; i <= b; i++) add(px[mod(i)], py[mod(i)]);
            if (next > b) {
                startPart(b);
                for (int i = b + 1; i < next; i++) join(mod(i), true);
                add(ex[mod(next - 1)], ey[mod(next - 1)]);
            }
        }
        return finish();
    }

    private static void add(List<double[][]> curves, double[][] curve) {
        if (curve != null) curves.add(curve);
    }

    private int mod(int i) {
        return i >= k ? i - k : i < 0 ? i + k : i;
    }

    // the curve of the chunk from vertex s to vertex e > s, with unwrapped indices
    private double[][] chunk(int s, int e) {
        start(2 * (e - s) + 16);
        add(px[mod(s)], py[mod(s)]);
        startPart(s);
        for (int i = s + 1; i < e; i++) join(mod(i), true);
        add(ex[mod(e - 1)], ey[mod(e - 1)]);
        for (int i = e; i > s; i--) add(px[mod(i)], py[mod(i)]);
        return finish();
    }

    // after vertex i, at which the curve leaves the path: on to the start of offset segment i, with the wedge at
    // the vertex, if any
    private void startPart(int unwrapped) {
        int i = mod(unwrapped);
        if (kind[i] == INSIDE || kind[i] == SHORTCUT || kind[i] == SPIKE) {
            add(sx[i], sy[i]);
        } else {
            int a = i == 0 ? k - 1 : i - 1;
            add(ex[a], ey[a]);
            join(i, false);
        }
    }

    private void start(int capacity) {
        if (cx == null || cx.length < capacity) {
            cx = new double[capacity];
            cy = new double[capacity];
        }
        n = 0;
    }

    private double[][] finish() {
        if (n > 1 && cx[n - 1] == cx[0] && cy[n - 1] == cy[0]) n--;
        if (n < 3) return null;
        add(cx[0], cy[0]);
        double[][] curve = {new double[n], new double[n]};
        System.arraycopy(cx, 0, curve[0], 0, n);
        System.arraycopy(cy, 0, curve[1], 0, n);
        return curve;
    }

    /**
     * Sets the closed path of a line: forward through its vertices and back, with caps at both ends
     */
    void line(double[] xs, double[] ys) {
        line(xs, ys, null);
    }

    /**
     * Sets the closed path of a line, with an offset distance per segment of that path: first the m segments
     * forward, then the m segments back (segment m + t runs from vertex m - t to vertex m - t - 1)
     */
    void line(double[] xs, double[] ys, double[] depth) {
        int m = xs.length - 1;
        int k = 2 * m;
        double[] lx = new double[k], ly = new double[k];
        for (int i = 0; i <= m; i++) {
            lx[i] = xs[i];
            ly[i] = ys[i];
        }
        for (int i = 1; i < m; i++) {
            lx[k - i] = xs[i];
            ly[k - i] = ys[i];
        }
        path(lx, ly, k, 0, m, depth);
    }

    /**
     * The curve around a point: a polygon of 4q vertices on the circle, a square, or null for flat end caps
     */
    double[][] point(double x, double y) {
        switch (parameters.getEndCap()) {
            case FLAT:
                return null;
            case SQUARE:
                return new double[][]{{x - d, x + d, x + d, x - d, x - d}, {y - d, y - d, y + d, y + d, y - d}};
            default:
                int segments = 4 * parameters.getQuadrantSegments();
                double[][] c = {new double[segments + 1], new double[segments + 1]};
                for (int i = 0; i < segments; i++) {
                    double angle = i * quantum;
                    c[0][i] = x + d * StrictMath.cos(angle);
                    c[1][i] = y + d * StrictMath.sin(angle);
                }
                c[0][segments] = c[0][0];
                c[1][segments] = c[1][0];
                return c;
        }
    }

    private void segments() {
        if (ux == null || ux.length < k) {
            ux = new double[k];
            uy = new double[k];
            sx = new double[k];
            sy = new double[k];
            ex = new double[k];
            ey = new double[k];
        }
        for (int j = 0; j < k; j++) {
            int next = j + 1 == k ? 0 : j + 1;
            double dx = px[next] - px[j], dy = py[next] - py[j];
            double length = Math.sqrt(dx * dx + dy * dy);
            if (!(length < Double.POSITIVE_INFINITY)) length = Math.hypot(dx, dy);
            ux[j] = dx / length;
            uy[j] = dy / length;
            // the right normal is (uy, -ux)
            double dj = depth == null ? d : depth[j];
            double ox = dj * uy[j], oy = -dj * ux[j];
            sx[j] = px[j] + ox;
            sy[j] = py[j] + oy;
            ex[j] = px[next] + ox;
            ey[j] = py[next] + oy;
        }
    }

    private void add(double x, double y) {
        if (!(Math.abs(x) <= Double.MAX_VALUE) || !(Math.abs(y) <= Double.MAX_VALUE)) {
            throw new IllegalArgumentException("The buffer has coordinates that are too large");
        }
        if (n > 0 && cx[n - 1] == x && cy[n - 1] == y) return;
        if (n == cx.length) {
            cx = java.util.Arrays.copyOf(cx, 2 * n);
            cy = java.util.Arrays.copyOf(cy, 2 * n);
        }
        cx[n] = x;
        cy[n] = y;
        n++;
    }

    // whether the spike at vertex i, after segment a, is long compared to its segments (so it may cross the spikes of
    // the vertices around it), or comes near many segments of the path (estimated with their bounding boxes); a
    // decision for speed only
    private boolean isLong(int a, int i) {
        int c = i + 1 == k ? 0 : i + 1;
        double before = Math.abs(px[i] - px[a]) + Math.abs(py[i] - py[a]);
        double after = Math.abs(px[c] - px[i]) + Math.abs(py[c] - py[i]);
        if (d > 4 * Math.max(before, after)) return true;
        if (segmentTree == null) {
            double[] minX = new double[k], minY = new double[k], maxX = new double[k], maxY = new double[k];
            for (int j = 0; j < k; j++) {
                int next = j + 1 == k ? 0 : j + 1;
                minX[j] = Math.min(px[j], px[next]);
                maxX[j] = Math.max(px[j], px[next]);
                minY[j] = Math.min(py[j], py[next]);
                maxY[j] = Math.max(py[j], py[next]);
            }
            segmentTree = new EnvelopeTree(minX, minY, maxX, maxY);
        }
        double minX = Math.min(px[i], Math.min(ex[a], sx[i])), maxX = Math.max(px[i], Math.max(ex[a], sx[i]));
        double minY = Math.min(py[i], Math.min(ey[a], sy[i])), maxY = Math.max(py[i], Math.max(ey[a], sy[i]));
        return segmentTree.count(minX, minY, maxX, maxY, NEAR + 1) > NEAR;
    }

    private void kinds() {
        segmentTree = null;
        if (kind == null || kind.length < k) kind = new byte[k];
        spikes = 0;
        shortcuts = 0;
        for (int i = 0; i < k; i++) {
            int a = i == 0 ? k - 1 : i - 1;          // the previous segment, and its start vertex
            int c = i + 1 == k ? 0 : i + 1;
            int orientation = Predicates.orientation(px[a], py[a], px[i], py[i], px[c], py[c]);
            if (orientation < 0) {
                if (canShortcut(a, i)) {
                    kind[i] = SHORTCUT;
                    shortcuts++;
                } else if (isLong(a, i)) {
                    kind[i] = SPIKE;
                    spikes++;
                } else {
                    kind[i] = INSIDE;
                }
            } else if (orientation > 0) {
                kind[i] = OUTSIDE;
            } else if (dotSign(a, i) > 0) {
                kind[i] = STRAIGHT;
            } else if (i == capA || i == capB) {
                kind[i] = CAP;
            } else {
                kind[i] = REVERSAL;
            }
        }
    }

    // the points of the curve between offset segment i - 1 and offset segment i, at vertex i
    private void join(int i, boolean shortcut) {
        int a = i == 0 ? k - 1 : i - 1;
        switch (kind[i]) {
            case SHORTCUT:
                if (shortcut) {
                    double[] x = Constructions.crossingPoint(sx[a], sy[a], ex[a], ey[a], sx[i], sy[i], ex[i], ey[i]);
                    add(x[0], x[1]);
                    break;
                }
                // fall through
            case INSIDE:
            case SPIKE:
                add(ex[a], ey[a]);
                add(px[i], py[i]);
                add(sx[i], sy[i]);
                break;
            case OUTSIDE:
                outsideTurn(a, i, false);
                break;
            case STRAIGHT:
                add(ex[a], ey[a]);
                add(sx[i], sy[i]);
                break;
            case CAP:
                cap(a, i);
                break;
            default:
                outsideTurn(a, i, true);
        }
    }

    // the sign of the dot product of the directions of segments a and i, which are collinear
    private int dotSign(int a, int i) {
        int c = i + 1 == k ? 0 : i + 1;
        double dot = (px[i] - px[a]) * (px[c] - px[i]) + (py[i] - py[a]) * (py[c] - py[i]);
        if (dot != 0 && Math.abs(dot) < Double.POSITIVE_INFINITY) return dot > 0 ? 1 : -1;
        // the segments are collinear and non-degenerate, so the signs of the coordinate differences decide
        double before = px[i] - px[a], after = px[c] - px[i];
        if (before != 0) return (before > 0) == (after > 0) ? 1 : -1;
        return (py[i] - py[a] > 0) == (py[c] - py[i] > 0) ? 1 : -1;
    }

    /**
     * Whether the curve can take a shortcut at the inside turn at vertex i, after segment a: through the crossing
     * point x of the offset segments instead of from the end of offset segment a to the vertex and on to the start of
     * offset segment i. The shortcut removes the cycle (x, end of a, vertex, start of i), so it is allowed only if
     * that region lies in both quadrilaterals of the segments: if both offset endpoints lie in the quadrilateral of
     * the other segment (the quadrilaterals are convex). Then the shortcuts lower the number of pieces that contain a
     * point by the number of shortcut regions that contain it, which is less than the number of quadrilaterals that
     * contain it as long as not every vertex of a closed curve takes a shortcut. An inside turn without shortcut is a
     * spike.
     */
    private boolean canShortcut(int a, int i) {
        return Predicates.segmentIntersection(sx[a], sy[a], ex[a], ey[a], sx[i], sy[i], ex[i], ey[i])
                == SegmentIntersection.CROSSING
                && inQuadrilateral(i, ex[a], ey[a]) && inQuadrilateral(a, sx[i], sy[i]);
    }

    // whether the point lies in the quadrilateral of segment j: (start, offset start, offset end, end), which is
    // counterclockwise
    private boolean inQuadrilateral(int j, double x, double y) {
        int next = j + 1 == k ? 0 : j + 1;
        return Predicates.orientation(px[j], py[j], sx[j], sy[j], x, y) >= 0
                && Predicates.orientation(sx[j], sy[j], ex[j], ey[j], x, y) >= 0
                && Predicates.orientation(ex[j], ey[j], px[next], py[next], x, y) >= 0
                && Predicates.orientation(px[next], py[next], px[j], py[j], x, y) >= 0;
    }

    private void outsideTurn(int a, int i, boolean reversal) {
        switch (parameters.getJoin()) {
            case BEVEL:
                add(ex[a], ey[a]);
                add(sx[i], sy[i]);
                break;
            case MITRE:
                mitre(a, i, reversal);
                break;
            default:
                arc(a, i);
        }
    }

    private void cap(int a, int i) {
        switch (parameters.getEndCap()) {
            case FLAT:
                add(ex[a], ey[a]);
                add(sx[i], sy[i]);
                break;
            case SQUARE:
                add(ex[a] + d * ux[a], ey[a] + d * uy[a]);
                add(sx[i] + d * ux[a], sy[i] + d * uy[a]);
                break;
            default:
                arc(a, i);
        }
    }

    /**
     * A counterclockwise arc around vertex i, from the end of offset segment a to the start of offset segment i,
     * with its vertices placed as in JTS: the angle is divided into round(angle / quantum) equal parts
     */
    private void arc(int a, int i) {
        add(ex[a], ey[a]);
        // the arc has radius d; the offset segments may be less deep
        if (depth != null) add(px[i] + d * uy[a], py[i] - d * ux[a]);
        double start = StrictMath.atan2(-ux[a], uy[a]), end = StrictMath.atan2(-ux[i], uy[i]);
        if (end <= start) end += 2 * Math.PI;
        double total = end - start;
        int segments = (int) (total / quantum + 0.5);
        double increment = total / segments;
        for (int s = 1; s < segments; s++) {
            double angle = start + s * increment;
            add(px[i] + d * StrictMath.cos(angle), py[i] + d * StrictMath.sin(angle));
        }
        if (depth != null) add(px[i] + d * uy[i], py[i] - d * ux[i]);
        add(sx[i], sy[i]);
    }

    /**
     * A mitre join, as in JTS: the meeting point of the offset lines if it lies within the mitre limit from the
     * vertex, otherwise a mitre cut off perpendicular to the bisector at the mitre limit, or a bevel if that lies
     * beyond the limit
     */
    private void mitre(int a, int i, boolean reversal) {
        double vx = px[i], vy = py[i];
        double limit = parameters.getMitreLimit() * d;
        double cross = ux[a] * uy[i] - uy[a] * ux[i];
        if (!reversal && cross != 0) {
            double t = ((sx[i] - ex[a]) * uy[i] - (sy[i] - ey[a]) * ux[i]) / cross;
            double mx = ex[a] + t * ux[a], my = ey[a] + t * uy[a];
            if (Math.hypot(mx - vx, my - vy) <= limit) {
                add(mx, my);
                return;
            }
        }
        // the outward bisector: of the normals, or for a (nearly) complete reversal the direction of segment a
        double bx = uy[a] + uy[i], by = -ux[a] - ux[i];
        double length = Math.hypot(bx, by);
        if (reversal || !(length > 1e-6)) {
            bx = ux[a];
            by = uy[a];
        } else {
            bx /= length;
            by /= length;
        }
        // the distance of the vertex to the bevel, along the bisector
        double bevel = (ex[a] - vx) * bx + (ey[a] - vy) * by;
        if (bevel >= limit) {
            add(ex[a], ey[a]);
            add(sx[i], sy[i]);
            return;
        }
        double mx = vx + limit * bx, my = vy + limit * by;
        double t0 = ((mx - ex[a]) * bx + (my - ey[a]) * by) / (ux[a] * bx + uy[a] * by);
        double t1 = ((mx - sx[i]) * bx + (my - sy[i]) * by) / (ux[i] * bx + uy[i] * by);
        add(ex[a], ey[a]);
        if (!Double.isFinite(t0) || !Double.isFinite(t1)) {
            // the turn is too small to compute the cut: the mitre is a bevel to within rounding
            add(sx[i], sy[i]);
            return;
        }
        add(ex[a] + t0 * ux[a], ey[a] + t0 * uy[a]);
        add(sx[i] + t1 * ux[i], sy[i] + t1 * uy[i]);
        add(sx[i], sy[i]);
    }
}
