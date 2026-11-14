package org.geolatte.geom.cga.exact;

/**
 * The Exact Sign of Sum Algorithm (ESSA).
 *
 * <p>Determines the exact sign of a finite sum of doubles, using only double precision arithmetic. The algorithm
 * is due to Ratschek &amp; Rokne (see <em>Geometric Computations with Interval and New Robust Methods</em>, Ch. 4,
 * Algorithm 9). The summands are split into a max-heap of positive values and a max-heap of absolute values of
 * negative values. In each step, the leading elements of both heaps are replaced by values with the same
 * difference (all operations are exact), until one heap dominates the other. Usually only a small fraction of
 * the summands needs to be processed before the sign is known.</p>
 *
 * <p>Requirements on the summands (the book leaves overflow and underflow to the caller):</p>
 * <ul>
 *     <li>all summands are finite, and</li>
 *     <li>their absolute values are less than {@link #MAX_SUMMAND}.</li>
 * </ul>
 * <p>Subnormal summands are handled correctly.</p>
 */
public final class Essa {

    /**
     * Upper bound (exclusive) on the absolute value of summands
     */
    public static final double MAX_SUMMAND = 0x1p1020;

    private Essa() {
    }

    /**
     * Returns the exact sign of the sum of the specified summands
     *
     * @param summands the summands
     * @return -1, 0, or 1 if the exact sum is negative, zero, or positive.
     * @throws IllegalArgumentException if a summand is not finite or its absolute value is not less than
     *                                  {@link #MAX_SUMMAND}
     */
    public static int sign(double... summands) {
        return sign(summands, summands.length);
    }

    /**
     * Returns the exact sign of the sum of the first {@code length} elements in the specified array
     *
     * <p>The array is not modified.</p>
     *
     * @param summands the summands
     * @param length   the number of summands to use
     * @return -1, 0, or 1 if the exact sum is negative, zero, or positive.
     * @throws IllegalArgumentException if a summand is not finite or its absolute value is not less than
     *                                  {@link #MAX_SUMMAND}
     */
    public static int sign(double[] summands, int length) {
        // Preprocessing: split into positive summands (a) and absolute values of negative summands (b).
        // A summand creates at most one extra element in its heap per iteration, but the heap sizes never
        // exceed the original number of summands + 1, see Step 4 analysis below.
        double[] a = new double[length + 2];
        double[] b = new double[length + 2];
        int m = 0;
        int n = 0;
        for (int i = 0; i < length; i++) {
            double s = summands[i];
            if (!(Math.abs(s) < MAX_SUMMAND)) {
                throw new IllegalArgumentException("Summand not finite or too large: " + s);
            }
            if (s > 0) {
                a[m++] = s;
            } else if (s < 0) {
                b[n++] = -s;
            }
        }
        heapify(a, m);
        heapify(b, n);

        while (true) {
            // Step 1: termination criteria
            if (m == 0 && n == 0) return 0;
            if (n == 0) return 1;
            if (m == 0) return -1;
            double a1 = a[0];
            double b1 = b[0];
            int e = exponent(a1);
            int f = exponent(b1);
            // a1 dominates when a1 >= n * 2^F, which is larger than the sum of all b's.
            if (a1 >= Math.scalb((double) n, f)) return 1;
            if (b1 >= Math.scalb((double) m, e)) return -1;

            // Steps 2 and 3: replace a1, b1 by a', a'' resp. b', b'' such that the sum is unchanged.
            double as = 0, ass = 0, bs = 0, bss = 0;
            if (e == f) {
                // Sterbenz: the difference is exact
                if (a1 >= b1) {
                    as = a1 - b1;
                } else {
                    bs = b1 - a1;
                }
            } else if (e > f) {
                double uu = Math.scalb(1.0, f - 1);
                double u = (b1 == uu) ? uu : 2 * uu;
                as = a1 - u;
                ass = u - b1;
            } else {
                double uu = Math.scalb(1.0, e - 1);
                double v = (a1 == uu) ? uu : 2 * uu;
                bs = b1 - v;
                bss = v - a1;
            }

            // Step 4: rearrange the heaps.
            // Each list loses its leading element and gains at most two new elements, so either a list
            // shrinks, or it grows by one while the other list shrinks by one: the total never increases.
            m = replaceTop(a, m, as, ass);
            n = replaceTop(b, n, bs, bss);
        }
    }

    /**
     * Returns the exponent E of the positive double v, such that v = 0.1xxxx * 2^E, i.e. 2^(E-1) &lt;= v &lt; 2^E.
     */
    static int exponent(double v) {
        int exp = Math.getExponent(v);
        if (exp == Double.MIN_EXPONENT - 1) {
            // subnormal
            exp = Math.getExponent(v * 0x1p64) - 64;
        }
        return exp + 1;
    }

    // Replaces the top of the max-heap h (of size size) with the non-zero values among x and y; returns new size
    private static int replaceTop(double[] h, int size, double x, double y) {
        if (x == 0) {
            x = y;
            y = 0;
        }
        if (x == 0) {
            // remove top
            size--;
            if (size > 0) {
                h[0] = h[size];
                siftDown(h, size, 0);
            }
            return size;
        }
        h[0] = x;
        siftDown(h, size, 0);
        if (y != 0) {
            h[size] = y;
            siftUp(h, size);
            size++;
        }
        return size;
    }

    private static void heapify(double[] h, int size) {
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(h, size, i);
        }
    }

    private static void siftDown(double[] h, int size, int i) {
        double v = h[i];
        int half = size >>> 1;
        while (i < half) {
            int c = 2 * i + 1;
            int r = c + 1;
            if (r < size && h[r] > h[c]) c = r;
            if (v >= h[c]) break;
            h[i] = h[c];
            i = c;
        }
        h[i] = v;
    }

    private static void siftUp(double[] h, int i) {
        double v = h[i];
        while (i > 0) {
            int p = (i - 1) >>> 1;
            if (h[p] >= v) break;
            h[i] = h[p];
            i = p;
        }
        h[i] = v;
    }
}
