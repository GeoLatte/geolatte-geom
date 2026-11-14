package org.geolatte.geom.cga.exact;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The sum of the lengths of segments, rounded faithfully, and in practice correctly.
 *
 * <p>A length is a square root, and a sum of square roots is in general irrational and cannot be computed exactly
 * with a finite number of operations. Each length is therefore computed in double-word arithmetic: the coordinate
 * differences are exact, their squares are summed exactly up to a tiny error, and the square root is taken with one
 * Newton step, which gives a relative error below 2^-100. The lengths are summed with TwoSum, with a rigorous bound
 * on the error.</p>
 *
 * <p>{@link #correctlyRounded()} rounds the resulting enclosure of the total length, if it contains no rounding
 * boundary. This is the case unless the exact length lies within about 2^-100 (relatively) of the midpoint between
 * two doubles, or is exactly such a midpoint. {@link #value()} returns that result, and otherwise one of the two
 * doubles next to the exact length (<b>faithful rounding</b>).</p>
 *
 * <p>An exact accumulator ({@link #exact()}) computes every length with {@link BigDecimal} arithmetic: exactly when
 * it is rational (a power of 2 times an integer), and with at least 60 significant digits otherwise. A positive sum
 * of square roots is rational only if every term is, so the sum of exact lengths is rounded correctly, ties
 * included, and a sum with an irrational term cannot be a midpoint. Its {@link #value()} is <b>correctly
 * rounded</b>, unless the exact length lies within about 10^-58 (relatively) of a midpoint. It is slow, and meant
 * for a second pass when {@link #correctlyRounded()} cannot decide.</p>
 *
 * <p>Segments whose coordinate differences have extreme magnitudes (below 2^-450 or above 2^450) are always
 * computed with {@link BigDecimal} arithmetic; {@link #correctlyRounded()} then returns NaN, except in an exact
 * accumulator.</p>
 */
public final class LengthSum {

    private static final double U = 0x1p-53;
    // relative error bound of a double-word length: the low part of the sum of squares has an error below about
    // 40 u² (halved by the square root), the Newton step adds about 12 u²; with a margin
    private static final double LENGTH_REL = 64 * U * U;
    private static final double MIN_PART = 0x1p-450;
    private static final double MAX_PART = 0x1p450;
    private static final int DIGITS = 60;

    // the running sum of the high parts, the floating point sum of the error terms and of the low parts, the sum of
    // their magnitudes and their number, and the bound on the errors of the lengths
    private double s, err, abs, lengthErrors;
    private int k;
    // the sum of the lengths that were computed with BigDecimal arithmetic
    private BigDecimal slow;
    // whether all lengths are computed with BigDecimal arithmetic
    private final boolean exact;

    /**
     * Creates an accumulator that computes the lengths in double-word arithmetic
     */
    public LengthSum() {
        this(false);
    }

    private LengthSum(boolean exact) {
        this.exact = exact;
    }

    /**
     * @return an accumulator that computes every length with {@link BigDecimal} arithmetic, exactly when it is
     * rational
     */
    public static LengthSum exact() {
        return new LengthSum(true);
    }

    /**
     * Adds the length of the segment (x0, y0) - (x1, y1)
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public LengthSum add(double x0, double y0, double x1, double y1) {
        if (!Double.isFinite(x0) || !Double.isFinite(y0) || !Double.isFinite(x1) || !Double.isFinite(y1)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
        double xh = x1 - x0, xl = twoSumError(x1, -x0, xh);
        double yh = y1 - y0, yl = twoSumError(y1, -y0, yh);
        if (xh == 0 && yh == 0) return this;
        if (exact || !(inRange(xh) && inRange(xl) && inRange(yh) && inRange(yl))) {
            addSlow(x0, y0, x1, y1);
            return this;
        }
        // q = xh² + yh² + (2 xh xl + xl² + 2 yh yl + yl²): the first two exactly, as qh + ql
        double px = xh * xh, ex = Math.fma(xh, xh, -px);
        double py = yh * yh, ey = Math.fma(yh, yh, -py);
        double qh = px + py, ql = twoSumError(px, py, qh) + (ex + ey) + (2 * xh * xl + xl * xl)
                + (2 * yh * yl + yl * yl);
        // sqrt(qh + ql), one Newton step
        double r = Math.sqrt(qh);
        double t = (Math.fma(-r, r, qh) + ql) / (2 * r);
        double lh = r + t, ll = t - (lh - r);
        // add lh + ll
        double sum = s + lh;
        double e = twoSumError(s, lh, sum);
        s = sum;
        err += e + ll;
        abs += Math.abs(e) + Math.abs(ll);
        k += 2;
        lengthErrors += lh;
        return this;
    }

    /**
     * @return the sum of the lengths, correctly rounded (the double nearest to the exact sum, ties to even), or NaN
     * if that cannot be decided (see the class documentation)
     */
    public double correctlyRounded() {
        if (exact) return slow == null ? 0 : slow.doubleValue();
        if (slow != null) return Double.NaN;
        double h = s + err, l = twoSumError(s, err, h);
        if (h == 0) return 0;
        double bound = ((k + 4) * 0x1p-51 * abs + LENGTH_REL * lengthErrors) * (1 + 0x1p-20);
        return Constructions.roundFromEnclosure(h, l, bound);
    }

    /**
     * @return the sum of the lengths: {@link #correctlyRounded()} if that is decided, and otherwise one of the two
     * doubles next to the exact sum
     */
    public double value() {
        if (exact) return correctlyRounded();
        double h = s + err, l = twoSumError(s, err, h);
        if (slow != null) {
            return slow.add(new BigDecimal(h)).add(new BigDecimal(l)).doubleValue();
        }
        double result = correctlyRounded();
        return Double.isNaN(result) ? h : result;
    }

    private void addSlow(double x0, double y0, double x1, double y1) {
        BigDecimal dx = new BigDecimal(x1).subtract(new BigDecimal(x0));
        BigDecimal dy = new BigDecimal(y1).subtract(new BigDecimal(y0));
        BigDecimal length;
        if (dx.signum() == 0 || dy.signum() == 0) {
            length = dx.abs().add(dy.abs());
        } else {
            // a rational square root (of a dyadic rational) has at most about half the digits of its square; it is
            // then returned exactly
            BigDecimal square = dx.multiply(dx).add(dy.multiply(dy));
            length = square.sqrt(new MathContext(Math.max(DIGITS, square.precision() / 2 + 2)));
        }
        slow = slow == null ? length : slow.add(length);
    }

    private static boolean inRange(double v) {
        double a = Math.abs(v);
        return a == 0 || (a >= MIN_PART && a <= MAX_PART);
    }

    // Knuth's TwoSum: (a + b) - s computed without rounding error
    private static double twoSumError(double a, double b, double s) {
        double bv = s - a;
        double av = s - bv;
        return (a - av) + (b - bv);
    }
}
