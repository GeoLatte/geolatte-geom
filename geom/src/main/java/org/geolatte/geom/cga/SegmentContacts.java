package org.geolatte.geom.cga;

import org.geolatte.geom.SegmentIntersection;
import org.geolatte.geom.cga.RelateComputer.MonotoneChain;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the pairs of segments of a set of chains (lines or rings) that meet, and classifies each pair exactly with
 * {@link Predicates#segmentIntersection}. Used by {@link Simplicity} and {@link Validity}.
 *
 * <p>The chains are split into monotone chains (runs of segments with directions in the same quadrant) in an
 * {@link EnvelopeTree}. Two segments of the same monotone chain only meet if they are consecutive, in their common
 * vertex; those pairs are not reported. All other pairs that meet are, including consecutive segments in different
 * monotone chains (which meet in their common vertex, and overlap if the chain turns back there).</p>
 */
final class SegmentContacts {

    interface Visitor {
        /**
         * A pair of segments that meet: segment i (from vertex i to vertex i + 1) of chain a, and segment j of chain b
         *
         * @return true to stop
         */
        boolean contact(int a, int i, int b, int j, SegmentIntersection kind);
    }

    interface SegmentConsumer {
        void accept(int chain, int segment);
    }

    private final MonotoneChain[] mcs;
    private final EnvelopeTree tree;

    /**
     * @param chains per chain, {xs, ys}: at least 2 positions, without repeated consecutive positions, finite
     */
    SegmentContacts(List<double[][]> chains) {
        List<MonotoneChain> list = new ArrayList<>();
        for (int k = 0; k < chains.size(); k++) {
            RelateComputer.addMonotoneChains(k, chains.get(k)[0], chains.get(k)[1], list);
        }
        mcs = list.toArray(new MonotoneChain[0]);
        tree = RelateComputer.tree(mcs);
    }

    /**
     * Calls the visitor for the pairs of segments of the chains that meet, until it returns true
     *
     * @param chains per chain, {xs, ys}: at least 2 positions, without repeated consecutive positions, finite
     * @return true if the visitor returned true
     */
    static boolean find(List<double[][]> chains, Visitor visitor) {
        return new SegmentContacts(chains).find(visitor);
    }

    /**
     * Calls the visitor for the pairs of segments that meet, until it returns true
     *
     * @return true if the visitor returned true
     */
    boolean find(Visitor visitor) {
        boolean[] stopped = {false};
        tree.selfJoin((p, q) -> {
            MonotoneChain a = mcs[p], b = mcs[q];
            stopped[0] = RelateComputer.pairs(a, a.start, a.end, b, b.start, b.end, (i, j) -> {
                SegmentIntersection kind = Predicates.segmentIntersection(a.x(i), a.y(i), a.x(i + 1), a.y(i + 1),
                        b.x(j), b.y(j), b.x(j + 1), b.y(j + 1));
                return kind != SegmentIntersection.DISJOINT && visitor.contact(a.chain, i, b.chain, j, kind);
            });
            return stopped[0];
        });
        return stopped[0];
    }

    /**
     * Calls the action for each segment whose bounding box meets the query box
     */
    void query(double minX, double minY, double maxX, double maxY, SegmentConsumer action) {
        tree.query(minX, minY, maxX, maxY, i -> query(mcs[i], mcs[i].start, mcs[i].end, minX, minY, maxX, maxY, action));
    }

    // the segments v0 to v1 - 1 of the monotone chain whose bounding boxes meet the query box
    private static void query(MonotoneChain mc, int v0, int v1, double minX, double minY, double maxX, double maxY,
                              SegmentConsumer action) {
        double ax = mc.x(v0), bx = mc.x(v1), ay = mc.y(v0), by = mc.y(v1);
        if (Math.max(ax, bx) < minX || Math.min(ax, bx) > maxX || Math.max(ay, by) < minY || Math.min(ay, by) > maxY) {
            return;
        }
        if (v1 - v0 == 1) {
            action.accept(mc.chain, v0);
            return;
        }
        int mid = (v0 + v1) >>> 1;
        query(mc, v0, mid, minX, minY, maxX, maxY, action);
        query(mc, mid, v1, minX, minY, maxX, maxY, action);
    }

    /**
     * @return true if segments i and j of a chain with n segments are consecutive
     */
    static boolean consecutive(int i, int j, int n, boolean closed) {
        return Math.abs(i - j) == 1 || (closed && n > 1 && Math.min(i, j) == 0 && Math.max(i, j) == n - 1);
    }

    /**
     * @return true if (x, y) lies on the segment (x0, y0) - (x1, y1)
     */
    static boolean onSegment(double x0, double y0, double x1, double y1, double x, double y) {
        return Math.min(x0, x1) <= x && x <= Math.max(x0, x1) && Math.min(y0, y1) <= y && y <= Math.max(y0, y1)
                && Predicates.orientation(x0, y0, x1, y1, x, y) == 0;
    }
}
