package org.geolatte.geom.cga.exact;

/**
 * A machine interval: a closed interval {@code [lo, hi]} whose bounds are doubles.
 *
 * <p>All operations satisfy the inclusion principle of machine interval arithmetic (Ratschek &amp; Rokne,
 * <em>Geometric Computations with Interval and New Robust Methods</em>, Sec. 2.4): if {@code a} is in {@code A}
 * and {@code b} is in {@code B}, then the exact real value {@code a * b} is in the computed interval
 * {@code A.mul(B)} (and analogously for the other operations).</p>
 *
 * <p>Java offers no control over the IEEE rounding mode, so instead of switching to truncation or directed
 * rounding (as proposed in the book), each bound is computed in round-to-nearest and then corrected with an
 * error-free transformation (TwoSum for sums, {@link Math#fma} for products). This yields the exact directed
 * rounding of each bound, which has an important consequence: an operation whose result is exactly
 * representable produces a point interval. Hence an expression that is exactly zero (e.g. the orientation of
 * collinear points with "nice" coordinates) evaluates to {@code [0, 0]} and is decided without further
 * exact computation.</p>
 *
 * <p>Instances are immutable.</p>
 */
public final class Interval {

    /**
     * Returned by {@link #signum()} when the interval contains zero but is not {@code [0, 0]}
     */
    public static final int UNDECIDED = 2;

    // products whose magnitude is below this threshold might have suffered from underflow, so the rounding
    // error of the product can not be computed exactly with fma.
    private static final double UNDERFLOW_GUARD = 0x1p-968;

    public static final Interval ZERO = new Interval(0, 0);

    private final double lo;
    private final double hi;

    private Interval(double lo, double hi) {
        this.lo = lo;
        this.hi = hi;
    }

    /**
     * Creates the point interval {@code [v, v]}
     *
     * @param v a finite double
     * @return the point interval containing exactly v
     */
    public static Interval of(double v) {
        return new Interval(v, v);
    }

    /**
     * Creates the interval {@code [lo, hi]}
     *
     * @param lo lower bound
     * @param hi upper bound
     * @return the interval {@code [lo, hi]}
     * @throws IllegalArgumentException if lo &gt; hi, or either bound is NaN
     */
    public static Interval of(double lo, double hi) {
        if (!(lo <= hi)) {
            throw new IllegalArgumentException(String.format("Invalid interval [%s, %s]", lo, hi));
        }
        return new Interval(lo, hi);
    }

    /**
     * Returns the narrowest machine interval containing the exact difference {@code a - b}
     */
    public static Interval difference(double a, double b) {
        return new Interval(addDown(a, -b), addUp(a, -b));
    }

    /**
     * Returns the narrowest machine interval containing the exact product {@code a * b}
     */
    public static Interval product(double a, double b) {
        return new Interval(mulDown(a, b), mulUp(a, b));
    }

    public double lo() {
        return lo;
    }

    public double hi() {
        return hi;
    }

    /**
     * @return true iff the specified value lies in this interval
     */
    public boolean contains(double v) {
        return lo <= v && v <= hi;
    }

    /**
     * Returns the sign of all values contained in this interval, if it can be decided.
     *
     * @return 1 if all values are positive, -1 if all values are negative, 0 if this interval is {@code [0,0]},
     * and {@link #UNDECIDED} otherwise (including when any bound is NaN).
     */
    public int signum() {
        if (lo > 0) return 1;
        if (hi < 0) return -1;
        if (lo == 0 && hi == 0) return 0;
        return UNDECIDED;
    }

    public Interval add(Interval o) {
        return new Interval(addDown(lo, o.lo), addUp(hi, o.hi));
    }

    public Interval sub(Interval o) {
        return new Interval(addDown(lo, -o.hi), addUp(hi, -o.lo));
    }

    public Interval negate() {
        return new Interval(-hi, -lo);
    }

    public Interval mul(Interval o) {
        // general case; using the sign-based case distinction would save multiplications, but
        // this keeps the implementation obviously correct.
        double l = Math.min(Math.min(mulDown(lo, o.lo), mulDown(lo, o.hi)), Math.min(mulDown(hi, o.lo), mulDown(hi, o.hi)));
        double h = Math.max(Math.max(mulUp(lo, o.lo), mulUp(lo, o.hi)), Math.max(mulUp(hi, o.lo), mulUp(hi, o.hi)));
        return new Interval(l, h);
    }

    /**
     * Returns an inclusion of the range of {@code x^2} over this interval.
     *
     * <p>This is narrower than {@code this.mul(this)} when the interval contains zero.</p>
     */
    public Interval sqr() {
        if (lo >= 0) return new Interval(mulDown(lo, lo), mulUp(hi, hi));
        if (hi <= 0) return new Interval(mulDown(hi, hi), mulUp(lo, lo));
        double m = Math.max(-lo, hi);
        return new Interval(0, mulUp(m, m));
    }

    // Directed rounding by means of error-free transformations. -------------------------------------------

    /**
     * @return the largest double that is &lt;= a + b
     */
    static double addDown(double a, double b) {
        double s = a + b;
        if (s == Double.POSITIVE_INFINITY && a != s && b != s) {
            return Double.MAX_VALUE; // overflow of a finite sum
        }
        return twoSumError(a, b, s) < 0 ? Math.nextDown(s) : s;
    }

    /**
     * @return the smallest double that is &gt;= a + b
     */
    static double addUp(double a, double b) {
        double s = a + b;
        if (s == Double.NEGATIVE_INFINITY && a != s && b != s) {
            return -Double.MAX_VALUE; // overflow of a finite sum
        }
        return twoSumError(a, b, s) > 0 ? Math.nextUp(s) : s;
    }

    /**
     * @return the largest double that is &lt;= a * b
     */
    static double mulDown(double a, double b) {
        double p = a * b;
        if (Math.abs(p) < UNDERFLOW_GUARD) {
            // fma can not reliably detect the rounding error. Round outward unconditionally, but when the
            // product underflows to zero, use that its sign is known.
            if (a == 0 || b == 0) return 0;
            if (p == 0) return (a > 0) == (b > 0) ? 0 : -Double.MIN_VALUE;
            return Math.nextDown(p);
        }
        return Math.fma(a, b, -p) < 0 ? Math.nextDown(p) : p;
    }

    /**
     * @return the smallest double that is &gt;= a * b
     */
    static double mulUp(double a, double b) {
        double p = a * b;
        if (Math.abs(p) < UNDERFLOW_GUARD) {
            if (a == 0 || b == 0) return 0;
            if (p == 0) return (a > 0) == (b > 0) ? Double.MIN_VALUE : 0;
            return Math.nextUp(p);
        }
        return Math.fma(a, b, -p) > 0 ? Math.nextUp(p) : p;
    }

    // Knuth's TwoSum: (a + b) - s computed without rounding error (barring overflow)
    private static double twoSumError(double a, double b, double s) {
        double bv = s - a;
        double av = s - bv;
        return (a - av) + (b - bv);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval)) return false;
        Interval other = (Interval) o;
        return Double.compare(lo, other.lo) == 0 && Double.compare(hi, other.hi) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(lo) + Double.hashCode(hi);
    }

    @Override
    public String toString() {
        return "[" + lo + ", " + hi + "]";
    }
}
