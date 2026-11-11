package org.geolatte.geom.cga;

import java.util.ArrayList;
import java.util.List;

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
        void accept(int i, int j);
    }

    /**
     * Calls the action once for each (unordered) pair of different rectangles that have a point in common
     */
    void selfJoin(PairConsumer action) {
        int top = levels.length - 1;
        join(top + 1, 0, 0, action);
    }

    // pairs of rectangles below nodes i and j (i <= j) at the level, whose boxes intersect (or i == j); the level
    // above the top is a single root node
    private void join(int level, int i, int j, PairConsumer action) {
        if (level == 0) {
            if (i != j) action.accept(i, j);
            return;
        }
        int child = level - 1;
        double[] boxes = levels[child];
        int count = boxes.length / 4;
        boolean root = level == levels.length;
        int iFrom = root ? 0 : i * NODE_CAPACITY, iTo = root ? count : Math.min(iFrom + NODE_CAPACITY, count);
        if (i == j) {
            for (int a = iFrom; a < iTo; a++) {
                join(child, a, a, action);
                for (int b = a + 1; b < iTo; b++) {
                    if (intersects(boxes, a, b)) join(child, a, b, action);
                }
            }
        } else {
            int jFrom = j * NODE_CAPACITY, jTo = Math.min(jFrom + NODE_CAPACITY, count);
            for (int a = iFrom; a < iTo; a++) {
                for (int b = jFrom; b < jTo; b++) {
                    if (intersects(boxes, a, b)) join(child, a, b, action);
                }
            }
        }
    }

    private static boolean intersects(double[] boxes, int a, int b) {
        return boxes[4 * a] <= boxes[4 * b + 2] && boxes[4 * b] <= boxes[4 * a + 2]
                && boxes[4 * a + 1] <= boxes[4 * b + 3] && boxes[4 * b + 1] <= boxes[4 * a + 3];
    }
}
