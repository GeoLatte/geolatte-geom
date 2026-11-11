package org.geolatte.geom.cga;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A static, packed R-tree over rectangles: each node groups consecutive children. The rectangles are taken in the
 * given order, which should have spatial locality (e.g. the order along a curve, or along a Hilbert curve).
 */
final class EnvelopeTree {

    private static final int NODE_CAPACITY = 16;

    private final double[][] levels;   // per level, 4 values per node: minX, minY, maxX, maxY; level 0: the items

    EnvelopeTree(double[] minX, double[] minY, double[] maxX, double[] maxY) {
        int n = minX.length;
        List<double[]> result = new ArrayList<>();
        double[] level = new double[4 * n];
        for (int i = 0; i < n; i++) {
            level[4 * i] = minX[i];
            level[4 * i + 1] = minY[i];
            level[4 * i + 2] = maxX[i];
            level[4 * i + 3] = maxY[i];
        }
        result.add(level);
        int count = n;
        while (count > NODE_CAPACITY) {
            int parents = (count + NODE_CAPACITY - 1) / NODE_CAPACITY;
            double[] up = new double[4 * parents];
            for (int p = 0; p < parents; p++) {
                double x0 = Double.POSITIVE_INFINITY, y0 = Double.POSITIVE_INFINITY;
                double x1 = Double.NEGATIVE_INFINITY, y1 = Double.NEGATIVE_INFINITY;
                for (int c = p * NODE_CAPACITY; c < Math.min(count, (p + 1) * NODE_CAPACITY); c++) {
                    x0 = Math.min(x0, level[4 * c]);
                    y0 = Math.min(y0, level[4 * c + 1]);
                    x1 = Math.max(x1, level[4 * c + 2]);
                    y1 = Math.max(y1, level[4 * c + 3]);
                }
                up[4 * p] = x0;
                up[4 * p + 1] = y0;
                up[4 * p + 2] = x1;
                up[4 * p + 3] = y1;
            }
            result.add(up);
            level = up;
            count = parents;
        }
        levels = result.toArray(new double[0][]);
    }

    interface PairConsumer {
        /**
         * @return true to stop
         */
        boolean accept(int i, int j);
    }

    /**
     * Calls the action once for each (unordered) pair of different rectangles that have a point in common, until
     * it returns true
     */
    void selfJoin(PairConsumer action) {
        join(levels.length, 0, 0, action);
    }

    // pairs of rectangles below nodes i and j (i <= j) at the level, whose boxes intersect (or i == j); the level
    // above the top is a single root node
    private boolean join(int level, int i, int j, PairConsumer action) {
        if (level == 0) return i != j && action.accept(i, j);
        int child = level - 1;
        double[] boxes = levels[child];
        int count = boxes.length / 4;
        boolean root = level == levels.length;
        int iFrom = root ? 0 : i * NODE_CAPACITY, iTo = root ? count : Math.min(iFrom + NODE_CAPACITY, count);
        if (i == j) {
            for (int a = iFrom; a < iTo; a++) {
                if (join(child, a, a, action)) return true;
                for (int b = a + 1; b < iTo; b++) {
                    if (intersects(boxes, a, b) && join(child, a, b, action)) return true;
                }
            }
        } else {
            int jFrom = j * NODE_CAPACITY, jTo = Math.min(jFrom + NODE_CAPACITY, count);
            for (int a = iFrom; a < iTo; a++) {
                for (int b = jFrom; b < jTo; b++) {
                    if (intersects(boxes, a, b) && join(child, a, b, action)) return true;
                }
            }
        }
        return false;
    }

    private static boolean intersects(double[] boxes, int a, int b) {
        return boxes[4 * a] <= boxes[4 * b + 2] && boxes[4 * b] <= boxes[4 * a + 2]
                && boxes[4 * a + 1] <= boxes[4 * b + 3] && boxes[4 * b + 1] <= boxes[4 * a + 3];
    }

    /**
     * Calls the action once for each pair (i, j) of a rectangle i of tree a and a rectangle j of tree b that have a
     * point in common, until it returns true
     */
    static void join(EnvelopeTree a, EnvelopeTree b, PairConsumer action) {
        join(a, a.levels.length, 0, b, b.levels.length, 0, action);
    }

    // the level above the top is a single root node, whose box is unbounded
    private static boolean join(EnvelopeTree a, int la, int i, EnvelopeTree b, int lb, int j, PairConsumer action) {
        if (la == 0 && lb == 0) return action.accept(i, j);
        if (la >= lb) {
            double[] boxes = a.levels[la - 1];
            int from = la == a.levels.length ? 0 : i * NODE_CAPACITY;
            int to = la == a.levels.length ? boxes.length / 4 : Math.min(from + NODE_CAPACITY, boxes.length / 4);
            for (int c = from; c < to; c++) {
                if (b.intersects(lb, j, boxes, c) && join(a, la - 1, c, b, lb, j, action)) return true;
            }
        } else {
            double[] boxes = b.levels[lb - 1];
            int from = lb == b.levels.length ? 0 : j * NODE_CAPACITY;
            int to = lb == b.levels.length ? boxes.length / 4 : Math.min(from + NODE_CAPACITY, boxes.length / 4);
            for (int c = from; c < to; c++) {
                if (a.intersects(la, i, boxes, c) && join(a, la, i, b, lb - 1, c, action)) return true;
            }
        }
        return false;
    }

    // does node i at the level intersect rectangle c of the boxes?
    private boolean intersects(int level, int i, double[] boxes, int c) {
        if (level == levels.length) return true;
        double[] own = levels[level];
        return own[4 * i] <= boxes[4 * c + 2] && boxes[4 * c] <= own[4 * i + 2]
                && own[4 * i + 1] <= boxes[4 * c + 3] && boxes[4 * c + 1] <= own[4 * i + 3];
    }

    /**
     * Calls the action with the index of each rectangle that has a point in common with the query rectangle
     */
    void query(double minX, double minY, double maxX, double maxY, IntConsumer action) {
        query(levels.length, 0, minX, minY, maxX, maxY, action);
    }

    private void query(int level, int i, double minX, double minY, double maxX, double maxY, IntConsumer action) {
        if (level == 0) {
            action.accept(i);
            return;
        }
        double[] boxes = levels[level - 1];
        int from = level == levels.length ? 0 : i * NODE_CAPACITY;
        int to = level == levels.length ? boxes.length / 4 : Math.min(from + NODE_CAPACITY, boxes.length / 4);
        for (int c = from; c < to; c++) {
            if (boxes[4 * c] <= maxX && minX <= boxes[4 * c + 2] && boxes[4 * c + 1] <= maxY && minY <= boxes[4 * c + 3]) {
                query(level - 1, c, minX, minY, maxX, maxY, action);
            }
        }
    }
}
