package org.geolatte.geom.cga;

import java.util.Arrays;
import java.util.function.IntConsumer;

/**
 * A static index of intervals, answering which intervals contain a value: a complete binary tree over the
 * intervals sorted by their lower bound, in which each node stores the range of the intervals below it.
 */
final class IntervalIndex {

    private final int size;        // number of leaves (a power of 2)
    private final double[] min, max;
    private final int[] items;     // the item of each leaf, -1 for padding

    /**
     * @param lo the lower bounds of the intervals; interval i is [lo[i], hi[i]]
     * @param hi the upper bounds
     */
    IntervalIndex(double[] lo, double[] hi) {
        int n = lo.length;
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Double.compare(lo[a], lo[b]));
        int s = 1;
        while (s < n) s <<= 1;
        size = s;
        min = new double[2 * s];
        max = new double[2 * s];
        items = new int[s];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        Arrays.fill(items, -1);
        for (int i = 0; i < n; i++) {
            int item = order[i];
            items[i] = item;
            min[s + i] = lo[item];
            max[s + i] = hi[item];
        }
        for (int k = s - 1; k >= 1; k--) {
            min[k] = Math.min(min[2 * k], min[2 * k + 1]);
            max[k] = Math.max(max[2 * k], max[2 * k + 1]);
        }
    }

    /**
     * Passes each item whose interval contains v to the consumer
     */
    void query(double v, IntConsumer consumer) {
        if (items.length == 0) return;
        int[] stack = new int[64];
        int top = 0;
        stack[top++] = 1;
        while (top > 0) {
            int k = stack[--top];
            if (min[k] > v || max[k] < v) continue;
            if (k >= size) {
                consumer.accept(items[k - size]);
            } else {
                stack[top++] = 2 * k;
                stack[top++] = 2 * k + 1;
            }
        }
    }
}
