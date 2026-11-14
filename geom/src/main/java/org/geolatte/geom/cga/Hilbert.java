package org.geolatte.geom.cga;

import java.util.Arrays;

/**
 * Ordering of points along a Hilbert curve, for spatial locality.
 */
final class Hilbert {

    private Hilbert() {
    }

    // the point indices sorted along a Hilbert curve over the bounding box (on a 2^16 x 2^16 grid). Rounding
    // errors in the grid coordinates only affect the order, not the result.
    static int[] order(double[] xs, double[] ys) {
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
}
