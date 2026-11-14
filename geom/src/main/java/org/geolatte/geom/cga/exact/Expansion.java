package org.geolatte.geom.cga.exact;

/**
 * Exact arithmetic on floating point expansions, after J. R. Shewchuk, "Adaptive Precision Floating-Point
 * Arithmetic and Fast Robust Geometric Predicates", Discrete Comput. Geom. 18, 1997.
 *
 * <p>An expansion is a sum of doubles (its components) that do not overlap, stored in order of increasing
 * magnitude, without zero components (except that the expansion of zero is {@code {0}}). Its value is the exact
 * sum of the components, and its sign is the sign of the largest (last) component.</p>
 *
 * <p>All operations are exact provided no underflow or overflow occurs in the error-free transformations
 * (TwoSum, and TwoProduct with {@link Math#fma}). Callers must guarantee this; see
 * {@link Predicates#inCircle(double, double, double, double, double, double, double, double)}. Shewchuk's
 * FastTwoSum steps are replaced by TwoSum, which is exact without preconditions.</p>
 */
final class Expansion {

    private Expansion() {
    }

    /**
     * @return the expansion of the exact difference a - b (1 or 2 components)
     */
    static double[] difference(double a, double b) {
        double x = a - b;
        double bv = a - x;
        double av = x + bv;
        double y = (a - av) + (bv - b);
        return y == 0 ? new double[]{x} : new double[]{y, x};
    }

    /**
     * @return the expansion of the sum of e and f (Shewchuk's FAST-EXPANSION-SUM with zero elimination, using
     * TwoSum)
     */
    static double[] sum(double[] e, double[] f) {
        double[] h = new double[e.length + f.length];
        int ei = 0, fi = 0, hi = 0;
        double q;
        if (smaller(e[ei], f[fi])) {
            q = e[ei++];
        } else {
            q = f[fi++];
        }
        while (ei < e.length || fi < f.length) {
            double next;
            if (fi == f.length || (ei < e.length && smaller(e[ei], f[fi]))) {
                next = e[ei++];
            } else {
                next = f[fi++];
            }
            double s = q + next;
            double err = twoSumError(q, next, s);
            if (err != 0) h[hi++] = err;
            q = s;
        }
        if (q != 0 || hi == 0) h[hi++] = q;
        return hi == h.length ? h : java.util.Arrays.copyOf(h, hi);
    }

    /**
     * @return the expansion of the product of e and b (Shewchuk's SCALE-EXPANSION with zero elimination)
     */
    static double[] scale(double[] e, double b) {
        double[] h = new double[2 * e.length];
        int hi = 0;
        double q = e[0] * b;
        double hh = Math.fma(e[0], b, -q);
        if (hh != 0) h[hi++] = hh;
        for (int i = 1; i < e.length; i++) {
            double p1 = e[i] * b;
            double p0 = Math.fma(e[i], b, -p1);
            double s = q + p0;
            double err = twoSumError(q, p0, s);
            if (err != 0) h[hi++] = err;
            q = p1 + s;
            err = twoSumError(p1, s, q);
            if (err != 0) h[hi++] = err;
        }
        if (q != 0 || hi == 0) h[hi++] = q;
        return hi == h.length ? h : java.util.Arrays.copyOf(h, hi);
    }

    /**
     * @return the expansion of the product of e and f
     */
    static double[] product(double[] e, double[] f) {
        double[] result = scale(e, f[0]);
        for (int i = 1; i < f.length; i++) {
            result = sum(result, scale(e, f[i]));
        }
        return result;
    }

    /**
     * @return the expansion of e * f - g * h
     */
    static double[] productDifference(double[] e, double[] f, double[] g, double[] h) {
        return sum(product(e, f), negate(product(g, h)));
    }

    static double[] negate(double[] e) {
        double[] r = new double[e.length];
        for (int i = 0; i < e.length; i++) r[i] = -e[i];
        return r;
    }

    /**
     * @return the sign of the value of the expansion
     */
    static int signum(double[] e) {
        return (int) Math.signum(e[e.length - 1]);
    }

    // is |a| smaller than |b|? (the order in which components are merged)
    private static boolean smaller(double a, double b) {
        return (b > a) == (b > -a);
    }

    // Knuth's TwoSum: (a + b) - s computed without rounding error
    private static double twoSumError(double a, double b, double s) {
        double bv = s - a;
        double av = s - bv;
        return (a - av) + (b - bv);
    }
}
