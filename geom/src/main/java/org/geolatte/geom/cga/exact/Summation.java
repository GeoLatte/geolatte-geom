package org.geolatte.geom.cga.exact;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * The exact sum of doubles and of products of doubles, with correctly rounded results.
 *
 * <p>The sum is kept as a list of non-overlapping partial sums (Shewchuk's GROW-EXPANSION, as in Python's
 * {@code math.fsum}). Products are split into exactly representable parts with the error-free product
 * ({@code p = a * b}, {@code e = fma(a, b, -p)}). A term that cannot be handled exactly in floating point (because
 * a product would underflow or a value is huge) is added to an exact {@link BigInteger} part instead. The value of
 * the sum is therefore always exact, whatever the magnitudes of the terms.</p>
 *
 * <p>The number of partial sums stays small (at most about 40, since they do not overlap), so adding a term takes
 * constant time. {@link #round()} and {@link #roundQuotient(Summation, Summation)} round the exact value correctly
 * (round to nearest, ties to even) with {@link BigInteger} arithmetic on the partial sums.</p>
 *
 * <p>This is the exact stage behind correctly rounded measures, such as the area and the centroid of a polygon:
 * these are rational functions of the coordinates.</p>
 */
public final class Summation {

    // the smallest magnitude of a product whose rounding error is exactly representable
    private static final double MIN_PRODUCT = 0x1p-968;
    // terms of at least this magnitude go to the BigInteger part, so that the partial sums cannot overflow
    private static final double MAX_TERM = 0x1p960;

    private double[] partials = new double[8];
    private int size = 0;

    // the part that is not representable by partials: big * 2^bigExp
    private BigInteger big = BigInteger.ZERO;
    private int bigExp = 0;

    /**
     * Adds a term
     *
     * @throws IllegalArgumentException if the term is NaN or infinite
     */
    public Summation add(double a) {
        checkFinite(a);
        if (a == 0) return this;
        if (Math.abs(a) >= MAX_TERM) {
            addBig(significand(a), exponent(a));
        } else {
            grow(a);
        }
        return this;
    }

    /**
     * Adds the exact product {@code a * b}
     *
     * @throws IllegalArgumentException if a factor is NaN or infinite
     */
    public Summation addProduct(double a, double b) {
        checkFinite(a);
        checkFinite(b);
        if (a == 0 || b == 0) return this;
        double p = a * b;
        double abs = Math.abs(p);
        if (abs >= MIN_PRODUCT && abs < MAX_TERM) {
            grow(p);
            double e = Math.fma(a, b, -p);
            if (e != 0) grow(e);
        } else {
            addBig(significand(a).multiply(significand(b)), exponent(a) + exponent(b));
        }
        return this;
    }

    /**
     * Adds the exact product {@code a * b * c}
     *
     * @throws IllegalArgumentException if a factor is NaN or infinite
     */
    public Summation addProduct(double a, double b, double c) {
        checkFinite(a);
        checkFinite(b);
        checkFinite(c);
        if (a == 0 || b == 0 || c == 0) return this;
        double p = a * b;
        double abs = Math.abs(p);
        if (abs >= MIN_PRODUCT && abs < MAX_TERM) {
            double e = Math.fma(a, b, -p);
            addProduct(p, c);
            if (e != 0) addProduct(e, c);
        } else {
            addBig(significand(a).multiply(significand(b)).multiply(significand(c)),
                    exponent(a) + exponent(b) + exponent(c));
        }
        return this;
    }

    /**
     * Adds (sign &gt; 0) or subtracts (sign &lt; 0) the value of another sum
     */
    public Summation add(int sign, Summation other) {
        for (int i = 0; i < other.size; i++) grow(sign < 0 ? -other.partials[i] : other.partials[i]);
        if (other.big.signum() != 0) addBig(sign < 0 ? other.big.negate() : other.big, other.bigExp);
        return this;
    }

    /**
     * @return the exact sign of the sum: -1, 0 or 1
     */
    public int signum() {
        if (big.signum() == 0) {
            // the partials do not overlap, so the largest one determines the sign
            return size == 0 ? 0 : (int) Math.signum(partials[size - 1]);
        }
        return exactValue().v.signum();
    }

    /**
     * @return the sum, correctly rounded (round to nearest, ties to even)
     */
    public double round() {
        return round(0);
    }

    /**
     * @return the sum multiplied by {@code 2^scale}, correctly rounded (round to nearest, ties to even). A value
     * whose magnitude exceeds the largest double is infinite.
     */
    public double round(int scale) {
        Dyadic value = exactValue();
        if (value.v.signum() == 0) return 0;
        return Constructions.roundQuotient(value.v, BigInteger.ONE, scale + value.exp);
    }

    /**
     * @return {@code num / den}, correctly rounded (round to nearest, ties to even). A value whose magnitude
     * exceeds the largest double is infinite.
     * @throws ArithmeticException if {@code den} is zero
     */
    public static double roundQuotient(Summation num, Summation den) {
        Dyadic d = den.exactValue();
        if (d.v.signum() == 0) throw new ArithmeticException("Division by zero");
        Dyadic n = num.exactValue();
        if (n.v.signum() == 0) return 0;
        return Constructions.roundQuotient(n.v, d.v, n.exp - d.exp);
    }

    // ---------------------------------------------------------------------------------------------------------

    // Shewchuk's GROW-EXPANSION with zero elimination; |a| < MAX_TERM, so no partial sum can overflow
    private void grow(double x) {
        int i = 0;
        for (int j = 0; j < size; j++) {
            double y = partials[j];
            if (Math.abs(x) < Math.abs(y)) {
                double t = x;
                x = y;
                y = t;
            }
            double hi = x + y;
            double lo = y - (hi - x);
            if (lo != 0) partials[i++] = lo;
            x = hi;
        }
        if (x != 0) {
            if (i == partials.length) partials = Arrays.copyOf(partials, 2 * i);
            partials[i++] = x;
        }
        size = i;
        if (Math.abs(x) >= MAX_TERM) {
            // move everything to the BigInteger part; this needs about 2^60 terms close to MAX_TERM
            for (int j = 0; j < size; j++) addBig(significand(partials[j]), exponent(partials[j]));
            size = 0;
        }
    }

    // adds s * 2^exp to the BigInteger part
    private void addBig(BigInteger s, int exp) {
        if (big.signum() == 0) {
            big = s;
            bigExp = exp;
        } else if (exp >= bigExp) {
            big = big.add(s.shiftLeft(exp - bigExp));
        } else {
            big = big.shiftLeft(bigExp - exp).add(s);
            bigExp = exp;
        }
    }

    // v * 2^exp
    private static final class Dyadic {
        final BigInteger v;
        final int exp;

        Dyadic(BigInteger v, int exp) {
            this.v = v;
            this.exp = exp;
        }
    }

    // the exact value of the sum
    private Dyadic exactValue() {
        int exp = big.signum() == 0 ? Integer.MAX_VALUE : bigExp;
        for (int i = 0; i < size; i++) exp = Math.min(exp, exponent(partials[i]));
        if (exp == Integer.MAX_VALUE) return new Dyadic(BigInteger.ZERO, 0);
        BigInteger value = big.signum() == 0 ? BigInteger.ZERO : big.shiftLeft(bigExp - exp);
        for (int i = 0; i < size; i++) {
            value = value.add(significand(partials[i]).shiftLeft(exponent(partials[i]) - exp));
        }
        return new Dyadic(value, exp);
    }

    // a finite, non-zero double is significand(v) * 2^exponent(v)
    private static BigInteger significand(double v) {
        long bits = Double.doubleToRawLongBits(v);
        long mantissa = bits & 0xfffffffffffffL;
        long significand = ((bits >>> 52) & 0x7ff) == 0 ? mantissa : mantissa | (1L << 52);
        return BigInteger.valueOf(v < 0 ? -significand : significand);
    }

    private static int exponent(double v) {
        int biased = (int) ((Double.doubleToRawLongBits(v) >>> 52) & 0x7ff);
        return biased == 0 ? -1074 : biased - 1075;
    }

    private static void checkFinite(double v) {
        if (!Double.isFinite(v)) throw new IllegalArgumentException("Values must be finite");
    }
}
