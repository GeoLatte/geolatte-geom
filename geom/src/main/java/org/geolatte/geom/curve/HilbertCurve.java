package org.geolatte.geom.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * The Hilbert curve: a space-filling curve that visits every cell of a 2<sup>order</sup> &times; 2<sup>order</sup>
 * grid once, always moving to an adjacent cell. Cells that are close along the curve are close in the plane, so
 * sorting points along it gives them spatial locality (better than the Morton or Z-order, which jumps between
 * quadrants). That is useful to bulk load spatial indexes, to group nearby items, or to insert points into a
 * triangulation.
 *
 * <p>The curve of order 1 visits the cells (0, 0), (0, 1), (1, 1), (1, 0); each higher order replaces every cell by a
 * rotated or reflected copy of the curve of order 1, so it starts in cell (0, 0) and ends in cell
 * (2<sup>order</sup> - 1, 0).</p>
 *
 * <p>{@link #order(double[], double[])} and {@link #sort(List, ToDoubleFunction, ToDoubleFunction)} map the points to a
 * grid of 2<sup>16</sup> &times; 2<sup>16</sup> cells over their bounding box. Rounding in that mapping only affects
 * the order of points that lie very close together, so the order suits any computation whose result does not depend
 * on it.</p>
 */
public final class HilbertCurve {

    /**
     * The highest order for {@link #index(int, int, int)} and {@link #cell(long, int)}: the index then still fits in a
     * {@code long}
     */
    public static final int MAX_ORDER = 31;

    // the order of the grid used for sorting: the index fits in the upper half of a long sort key
    private static final int SORT_ORDER = 16;
    private static final int SORT_MAX = (1 << SORT_ORDER) - 1;

    private HilbertCurve() {
    }

    /**
     * The position of a cell along the curve
     *
     * @param x     the column of the cell, from 0 to 2<sup>order</sup> - 1
     * @param y     the row of the cell, from 0 to 2<sup>order</sup> - 1
     * @param order the order of the curve, from 1 to {@link #MAX_ORDER}
     * @return the index of the cell along the curve, from 0 to 4<sup>order</sup> - 1
     * @throws IllegalArgumentException if the order or a cell coordinate is out of range
     */
    public static long index(int x, int y, int order) {
        checkOrder(order);
        int size = 1 << order;
        if (x < 0 || y < 0 || (order < MAX_ORDER && (x >= size || y >= size))) {
            throw new IllegalArgumentException("Cell (" + x + ", " + y + ") is outside a grid of order " + order);
        }
        return indexOf(x, y, order);
    }

    /**
     * The cell at a position along the curve; the inverse of {@link #index(int, int, int)}
     *
     * @param index the index along the curve, from 0 to 4<sup>order</sup> - 1
     * @param order the order of the curve, from 1 to {@link #MAX_ORDER}
     * @return the cell, as {x, y}
     * @throws IllegalArgumentException if the order or the index is out of range
     */
    public static int[] cell(long index, int order) {
        checkOrder(order);
        if (index < 0 || index >= 1L << (2 * order)) {
            throw new IllegalArgumentException("Index " + index + " is outside a curve of order " + order);
        }
        long t = index;
        int x = 0, y = 0, s = 1;
        for (int level = 0; level < order; level++, s <<= 1) {
            int rx = (int) (1 & (t >>> 1));
            int ry = (int) (1 & (t ^ rx));
            if (ry == 0) {
                if (rx == 1) {
                    x = s - 1 - x;
                    y = s - 1 - y;
                }
                int tmp = x;
                x = y;
                y = tmp;
            }
            x += s * rx;
            y += s * ry;
            t >>>= 2;
        }
        return new int[]{x, y};
    }

    /**
     * The indices of the points, sorted along a Hilbert curve over their bounding box
     *
     * @param xs the X coordinates of the points
     * @param ys the Y coordinates of the points, as many as X coordinates
     * @return the indices 0 to n - 1 of the points, in the order of the curve; points in the same cell of the 2<sup>16</sup>
     * &times; 2<sup>16</sup> grid keep their original order. NaN coordinates are taken as the minimum.
     */
    public static int[] order(double[] xs, double[] ys) {
        if (xs.length != ys.length) {
            throw new IllegalArgumentException("The number of X and Y coordinates differs");
        }
        return order(xs, ys, xs.length);
    }

    /**
     * The indices of the first n points, sorted along a Hilbert curve over their bounding box
     *
     * @param n the number of points, at most the length of both arrays
     * @return the indices 0 to n - 1 of the points, in the order of the curve
     * @see #order(double[], double[])
     */
    public static int[] order(double[] xs, double[] ys, int n) {
        if (n < 0 || n > xs.length || n > ys.length) {
            throw new IllegalArgumentException("Invalid number of points: " + n);
        }
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
            // halves, so that the range cannot overflow
            int gx = (int) Math.min(SORT_MAX, Math.max(0, (0.5 * xs[i] - 0.5 * minX) * sx));
            int gy = (int) Math.min(SORT_MAX, Math.max(0, (0.5 * ys[i] - 0.5 * minY) * sy));
            // the index (below 2^32) in the upper half, made signed so that the keys sort as the indices do
            keys[i] = ((indexOf(gx, gy, SORT_ORDER) << 32) ^ Long.MIN_VALUE) | i;
        }
        Arrays.sort(keys);
        int[] order = new int[n];
        for (int i = 0; i < n; i++) order[i] = (int) keys[i];
        return order;
    }

    /**
     * Sorts the items along a Hilbert curve over the bounding box of their locations
     *
     * @param items the items, sorted in place
     * @param x     the X coordinate of the location of an item (for example the centre of its bounding box)
     * @param y     the Y coordinate of the location of an item
     * @see #order(double[], double[])
     */
    public static <T> void sort(List<T> items, ToDoubleFunction<? super T> x, ToDoubleFunction<? super T> y) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);
        int n = items.size();
        if (n < 2) return;
        List<T> copy = new ArrayList<>(items);
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = x.applyAsDouble(copy.get(i));
            ys[i] = y.applyAsDouble(copy.get(i));
        }
        int[] order = order(xs, ys, n);
        for (int i = 0; i < n; i++) items.set(i, copy.get(order[i]));
    }

    private static void checkOrder(int order) {
        if (order < 1 || order > MAX_ORDER) {
            throw new IllegalArgumentException("The order must be from 1 to " + MAX_ORDER + ": " + order);
        }
    }

    // the scale from half a coordinate to the sort grid
    private static double gridScale(double min, double max) {
        double halfRange = 0.5 * max - 0.5 * min;
        double s = SORT_MAX / halfRange;
        return halfRange > 0 && Double.isFinite(s) ? s : 0;
    }

    // the index of cell (x, y) along the curve of the order; the reflections use the size of the current quadrant,
    // which leaves the bits that are still to be read the same as a reflection in the whole grid
    private static long indexOf(int x, int y, int order) {
        long d = 0;
        for (int s = 1 << (order - 1); s > 0; s >>>= 1) {
            int rx = (x & s) != 0 ? 1 : 0;
            int ry = (y & s) != 0 ? 1 : 0;
            d += (long) s * s * ((3 * rx) ^ ry);
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
}
