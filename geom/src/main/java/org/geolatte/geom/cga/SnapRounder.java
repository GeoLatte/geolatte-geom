package org.geolatte.geom.cga;

import org.geolatte.geom.SegmentIntersection;
import org.geolatte.geom.cga.exact.RoundingCell;

import java.util.*;

/**
 * Snap rounding of a set of chains (lines and rings) to a {@link Grid}.
 *
 * <p>A <em>hot pixel</em> is the cell of the grid that contains a vertex of a chain, or the point where two segments
 * cross. Each segment is replaced by the polyline through the grid points of the hot pixels it meets, in the order
 * in which it meets them, from the grid point of its start to that of its end. On the double grid the cells are the
 * rounding cells of the doubles ({@link RoundingCell}), and the vertices of the chains do not move; on a fixed grid
 * they move to their grid points. This is Hobby's snap rounding: the cells tile the plane, are convex and each
 * contains its grid point, so the snapped chains do not cross (Guibas &amp; Marimont); they meet in common
 * vertices, where a vertex lies on another segment, or along common parts. On a fixed grid a chain can collapse to
 * a single vertex.</p>
 *
 * <p>The result is checked exactly: the contacts of the snapped chains are computed again. A vertex in the interior
 * of a segment of another chain is inserted into it, and so are the ends of overlapping segments, so that chains
 * that meet share vertices and identical segments. Should two snapped segments cross nevertheless, their rounded
 * crossing point is added to the hot pixels and the snapping is repeated.</p>
 */
final class SnapRounder {

    private static final int MAX_ROUNDS = 8;

    /**
     * A snapped chain: its vertices, and for each vertex the index of the original segment it lies on (the segment
     * that starts at it, for the original vertices; the last segment, for the last vertex)
     */
    static final class Chain {
        final double[] xs, ys;
        final int[] origin;

        Chain(double[] xs, double[] ys, int[] origin) {
            this.xs = xs;
            this.ys = ys;
            this.origin = origin;
        }

        int size() {
            return xs.length;
        }
    }

    private final List<double[][]> chains;
    private final Grid grid;
    // per chain, the grid points of its vertices
    private final List<double[][]> rounded;
    // the hot pixels, by their points
    private double[] hx = new double[64], hy = new double[64];
    private int hotCount = 0;
    private final Map<Long, Integer> hotIndex = new HashMap<>();
    // per original segment (chain, segment): the crossing pixels it is known to meet
    private final Map<Long, List<Integer>> crossings = new HashMap<>();

    private SnapRounder(List<double[][]> chains, Grid grid) {
        this.chains = chains;
        this.grid = grid;
        this.rounded = new ArrayList<>(chains.size());
        for (double[][] c : chains) {
            if (!grid.isFixed()) {
                rounded.add(c);
                continue;
            }
            int n = c[0].length;
            double[] xs = new double[n], ys = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = grid.round(c[0][i]);
                ys[i] = grid.round(c[1][i]);
            }
            rounded.add(new double[][]{xs, ys});
        }
    }

    /**
     * Snap rounds the chains to the double grid
     *
     * @param chains per chain, {xs, ys}: at least 2 positions, without repeated consecutive positions, finite
     * @return the snapped chains, in the same order
     */
    static List<Chain> snap(List<double[][]> chains) {
        return snap(chains, Grid.DOUBLE);
    }

    /**
     * Snap rounds the chains to the grid
     *
     * @param chains per chain, {xs, ys}: at least 2 positions, without repeated consecutive positions, finite, and
     *               within the range of the grid
     * @return the snapped chains, in the same order; on a fixed grid a chain can consist of a single vertex
     */
    static List<Chain> snap(List<double[][]> chains, Grid grid) {
        return new SnapRounder(chains, grid).run();
    }

    private List<Chain> run() {
        for (double[][] c : rounded) {
            for (int i = 0; i < c[0].length; i++) hot(c[0][i], c[1][i]);
        }
        SegmentContacts.find(chains, (a, i, b, j, kind) -> {
            if (kind == SegmentIntersection.CROSSING) {
                double[] ca = chains.get(a)[0], ya = chains.get(a)[1];
                double[] cb = chains.get(b)[0], yb = chains.get(b)[1];
                double[] p = grid.roundCrossing(ca[i], ya[i], ca[i + 1], ya[i + 1],
                        cb[j], yb[j], cb[j + 1], yb[j + 1]);
                int h = hot(p[0], p[1]);
                known(a, i, h);
                known(b, j, h);
            }
            return false;
        });
        for (int round = 0; round < MAX_ROUNDS; round++) {
            List<Chain> snapped = snapAll();
            if (verify(snapped)) return snapped;
        }
        throw new IllegalStateException("Snap rounding did not converge");
    }

    private int hot(double x, double y) {
        x += 0.0;
        y += 0.0;
        long key = key(x, y);
        Integer h = hotIndex.get(key);
        while (h != null && (hx[h] != x || hy[h] != y)) {
            key = key * 31 + 17;    // a collision of keys: probe on
            h = hotIndex.get(key);
        }
        if (h != null) return h;
        if (hotCount == hx.length) {
            hx = Arrays.copyOf(hx, 2 * hotCount);
            hy = Arrays.copyOf(hy, 2 * hotCount);
        }
        hx[hotCount] = x;
        hy[hotCount] = y;
        hotIndex.put(key, hotCount);
        return hotCount++;
    }

    private static long key(double x, double y) {
        return Double.doubleToLongBits(x) * 0x9E3779B97F4A7C15L ^ Double.doubleToLongBits(y);
    }

    // a key for segment i of chain k, well mixed for hashing (multiplying by an odd constant is a bijection)
    private static long segmentKey(int k, int i) {
        return (((long) k << 32) | i) * 0x9E3779B97F4A7C15L;
    }

    private void known(int chain, int segment, int h) {
        crossings.computeIfAbsent(segmentKey(chain, segment), k -> new ArrayList<>(2)).add(h);
    }

    // ---------------------------------------------------------------------------------------------------------
    // snapping
    // ---------------------------------------------------------------------------------------------------------

    private List<Chain> snapAll() {
        double[] minX = new double[hotCount], minY = new double[hotCount];
        double[] maxX = new double[hotCount], maxY = new double[hotCount];
        for (int h = 0; h < hotCount; h++) {
            // a box that contains the cell
            double gx = grid.boxHalfWidth(hx[h]), gy = grid.boxHalfWidth(hy[h]);
            minX[h] = hx[h] - gx;
            maxX[h] = hx[h] + gx;
            minY[h] = hy[h] - gy;
            maxY[h] = hy[h] + gy;
        }
        EnvelopeTree tree = new EnvelopeTree(minX, minY, maxX, maxY);
        List<Chain> result = new ArrayList<>(chains.size());
        List<Integer> met = new ArrayList<>();
        // the pixels known to be met by the current segment are marked with its number
        int[] mark = new int[hotCount];
        int[] segment = {0};
        for (int k = 0; k < chains.size(); k++) {
            double[] xs = chains.get(k)[0], ys = chains.get(k)[1];
            double[] rxs = rounded.get(k)[0], rys = rounded.get(k)[1];
            int n = xs.length;
            Polyline out = new Polyline(n);
            for (int i = 0; i + 1 < n; i++) {
                double ax = xs[i], ay = ys[i], bx = xs[i + 1], by = ys[i + 1];
                // the grid points of the cells of the ends
                double rax = rxs[i], ray = rys[i], rbx = rxs[i + 1], rby = rys[i + 1];
                out.add(rax, ray, i);
                met.clear();
                int current = ++segment[0];
                List<Integer> known = crossings.get(segmentKey(k, i));
                if (known != null) {
                    for (int h : known) mark[h] = current;
                }
                tree.query(Math.min(ax, bx), Math.min(ay, by), Math.max(ax, bx), Math.max(ay, by), h -> {
                    double px = hx[h], py = hy[h];
                    if ((px == rax && py == ray) || (px == rbx && py == rby)) return;
                    if (mark[h] == current
                            || SegmentContacts.onSegment(ax, ay, bx, by, px, py)
                            || grid.intersects(ax, ay, bx, by, px, py)) {
                        met.add(h);
                    }
                });
                if (met.size() > 1) {
                    met.sort((p, q) -> grid.compareAlong(ax, ay, bx, by, hx[p], hy[p], hx[q], hy[q]));
                }
                for (int h : met) out.add(hx[h], hy[h], i);
            }
            out.add(rxs[n - 1], rys[n - 1], n - 2);
            result.add(out.toChain());
        }
        return result;
    }

    // a growing polyline, without repeated consecutive positions
    private static final class Polyline {
        double[] xs, ys;
        int[] origin;
        int size;

        Polyline(int capacity) {
            xs = new double[capacity];
            ys = new double[capacity];
            origin = new int[capacity];
        }

        void add(double x, double y, int segment) {
            if (size > 0 && xs[size - 1] == x && ys[size - 1] == y) return;
            if (size == xs.length) {
                xs = Arrays.copyOf(xs, 2 * size + 2);
                ys = Arrays.copyOf(ys, 2 * size + 2);
                origin = Arrays.copyOf(origin, 2 * size + 2);
            }
            xs[size] = x;
            ys[size] = y;
            origin[size] = segment;
            size++;
        }

        Chain toChain() {
            return new Chain(Arrays.copyOf(xs, size), Arrays.copyOf(ys, size), Arrays.copyOf(origin, size));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // verification
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Checks the snapped chains exactly. If no segments cross, inserts the vertices that lie in the interior of
     * segments into them, and returns true. Otherwise adds the crossing points to the hot pixels, and returns false.
     */
    private boolean verify(List<Chain> snapped) {
        // the chains that did not collapse to a single vertex
        List<double[][]> list = new ArrayList<>(snapped.size());
        int[] chainOf = new int[snapped.size()];
        for (int k = 0; k < snapped.size(); k++) {
            Chain c = snapped.get(k);
            if (c.size() < 2) continue;
            chainOf[list.size()] = k;
            list.add(new double[][]{c.xs, c.ys});
        }
        // per (chain, segment): the points to insert
        Map<Long, List<double[]>> inserts = new HashMap<>();
        boolean[] crossed = {false};
        SegmentContacts.find(list, (la, i, lb, j, kind) -> {
            int a = chainOf[la], b = chainOf[lb];
            Chain ca = snapped.get(a), cb = snapped.get(b);
            double ax = ca.xs[i], ay = ca.ys[i], bx = ca.xs[i + 1], by = ca.ys[i + 1];
            double cx = cb.xs[j], cy = cb.ys[j], dx = cb.xs[j + 1], dy = cb.ys[j + 1];
            if (kind == SegmentIntersection.CROSSING) {
                double[] p = grid.roundCrossing(ax, ay, bx, by, cx, cy, dx, dy);
                hot(p[0], p[1]);
                crossed[0] = true;
                return false;
            }
            // touching or overlapping: the ends of each segment that lie in the interior of the other
            insertIfInside(inserts, a, i, ax, ay, bx, by, cx, cy);
            insertIfInside(inserts, a, i, ax, ay, bx, by, dx, dy);
            insertIfInside(inserts, b, j, cx, cy, dx, dy, ax, ay);
            insertIfInside(inserts, b, j, cx, cy, dx, dy, bx, by);
            return false;
        });
        if (crossed[0]) return false;
        if (inserts.isEmpty()) return true;
        for (int k = 0; k < snapped.size(); k++) {
            Chain c = snapped.get(k);
            if (c.size() < 2) continue;
            boolean any = false;
            for (int i = 0; i + 1 < c.size() && !any; i++) any = inserts.containsKey(segmentKey(k, i));
            if (!any) continue;
            Polyline out = new Polyline(c.size() + 4);
            for (int i = 0; i + 1 < c.size(); i++) {
                out.add(c.xs[i], c.ys[i], c.origin[i]);
                List<double[]> points = inserts.get(segmentKey(k, i));
                if (points == null) continue;
                double ax = c.xs[i], ay = c.ys[i], bx = c.xs[i + 1], by = c.ys[i + 1];
                // points on the segment: ordered by the coordinate that varies most monotonically along it
                boolean byX = ax != bx;
                double sign = byX ? Math.signum(bx - ax) : Math.signum(by - ay);
                points.sort((p, q) -> Double.compare(sign * (byX ? p[0] : p[1]), sign * (byX ? q[0] : q[1])));
                for (double[] p : points) out.add(p[0], p[1], c.origin[i]);
            }
            out.add(c.xs[c.size() - 1], c.ys[c.size() - 1], c.origin[c.size() - 1]);
            snapped.set(k, out.toChain());
        }
        return true;
    }

    private static void insertIfInside(Map<Long, List<double[]>> inserts, int chain, int segment,
                                       double ax, double ay, double bx, double by, double px, double py) {
        if ((px == ax && py == ay) || (px == bx && py == by)) return;
        if (!SegmentContacts.onSegment(ax, ay, bx, by, px, py)) return;
        List<double[]> list = inserts.computeIfAbsent(segmentKey(chain, segment), k -> new ArrayList<>(2));
        for (double[] q : list) {
            if (q[0] == px && q[1] == py) return;
        }
        list.add(new double[]{px, py});
    }
}
