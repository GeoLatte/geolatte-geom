package org.geolatte.geom.cga.exact;

import java.util.Arrays;

/**
 * A list of doubles whose exact (unrounded) sum represents the value of a polynomial expression.
 *
 * <p>ESSA can only determine the sign of a sum of machine numbers. The book (Sec. 4.6) therefore multiplies out
 * a determinant into a sum of products, and requires that each product is exactly representable as one or more
 * machine numbers. With double precision input, this is achieved with the error-free product: for doubles
 * {@code a} and {@code b}, with {@code p = a * b} and {@code e = fma(a, b, -p)}, we have {@code a * b = p + e}
 * exactly. Products of more factors, and products of two {@code ExactSum}s are expanded in the same way.</p>
 *
 * <p>The error-free product fails in case of underflow (the rounding error is not representable) or overflow.
 * Such cases are detected and reported by {@link #isExact()}; callers should then use a slower fallback such as
 * {@link java.math.BigDecimal} arithmetic. This only happens for coordinates of extreme magnitudes (roughly
 * below 1e-60 or above 1e60, depending on the degree of the expression).</p>
 */
public final class ExactSum {

    // A product with a magnitude below this value might have an error term that is not exactly representable
    private static final double UNDERFLOW_GUARD = 0x1p-968;

    private double[] terms;
    private int size = 0;
    private boolean exact = true;

    public ExactSum() {
        this(16);
    }

    public ExactSum(int initialCapacity) {
        terms = new double[Math.max(initialCapacity, 4)];
    }

    /**
     * Adds the term {@code a}
     */
    public ExactSum add(double a) {
        if (a != 0) {
            push(a);
        }
        return this;
    }

    /**
     * Adds the exact product {@code a * b} (as at most 2 terms)
     */
    public ExactSum addProduct(double a, double b) {
        if (a == 0 || b == 0) return this;
        double p = a * b;
        if (!(Math.abs(p) >= UNDERFLOW_GUARD)) {
            exact = false;
            return this;
        }
        push(p);
        double e = Math.fma(a, b, -p);
        if (e != 0) push(e);
        return this;
    }

    /**
     * Adds the exact product {@code -(a * b)}
     */
    public ExactSum subProduct(double a, double b) {
        return addProduct(-a, b);
    }

    /**
     * Adds the exact product {@code a * b * c * d} (as at most 8 terms)
     */
    public ExactSum addProduct(double a, double b, double c, double d) {
        if (a == 0 || b == 0 || c == 0 || d == 0) return this;
        double p1 = a * b;
        double p2 = c * d;
        if (!(Math.abs(p1) >= UNDERFLOW_GUARD) || !(Math.abs(p2) >= UNDERFLOW_GUARD)
                || !(Math.abs(p1) < Essa.MAX_SUMMAND) || !(Math.abs(p2) < Essa.MAX_SUMMAND)) {
            exact = false;
            return this;
        }
        double e1 = Math.fma(a, b, -p1);
        double e2 = Math.fma(c, d, -p2);
        addProduct(p1, p2);
        addProduct(p1, e2);
        addProduct(e1, p2);
        addProduct(e1, e2);
        return this;
    }

    /**
     * Adds the exact product {@code -(a * b * c * d)}
     */
    public ExactSum subProduct(double a, double b, double c, double d) {
        return addProduct(-a, b, c, d);
    }

    /**
     * Adds (sign = 1) or subtracts (sign = -1) all terms of {@code other}
     */
    public ExactSum add(int sign, ExactSum other) {
        exact &= other.exact;
        for (int i = 0; i < other.size; i++) {
            push(sign < 0 ? -other.terms[i] : other.terms[i]);
        }
        return this;
    }

    /**
     * Adds the exact product of the values represented by {@code x} and {@code y}
     * (as at most {@code 2 * x.size() * y.size()} terms)
     *
     * @param sign 1 to add, -1 to subtract the product
     */
    public ExactSum addProduct(int sign, ExactSum x, ExactSum y) {
        exact &= x.exact & y.exact;
        for (int i = 0; i < x.size; i++) {
            double xi = sign < 0 ? -x.terms[i] : x.terms[i];
            for (int j = 0; j < y.size; j++) {
                addProduct(xi, y.terms[j]);
            }
        }
        return this;
    }

    /**
     * @return false if one of the operations could not be performed exactly due to underflow or overflow (this
     * includes terms with magnitude of at least {@link Essa#MAX_SUMMAND}).
     * In that case the sum of the terms does not represent the value of the expression.
     */
    public boolean isExact() {
        return exact;
    }

    /**
     * @return the number of terms
     */
    public int size() {
        return size;
    }

    /**
     * @return a copy of the terms
     */
    public double[] terms() {
        return Arrays.copyOf(terms, size);
    }

    /**
     * Returns the exact sign of the sum of the terms, computed with {@link Essa}
     *
     * @return -1, 0, 1
     * @throws ArithmeticException if this sum is not exact (see {@link #isExact()})
     */
    public int signum() {
        if (!exact) {
            throw new ArithmeticException("Sum is not exact due to underflow or overflow");
        }
        return Essa.sign(terms, size);
    }

    private void push(double t) {
        if (!(Math.abs(t) < Essa.MAX_SUMMAND)) {
            exact = false; // overflow, or too large for ESSA
            return;
        }
        if (size == terms.length) {
            terms = Arrays.copyOf(terms, terms.length * 2);
        }
        terms[size++] = t;
    }
}
