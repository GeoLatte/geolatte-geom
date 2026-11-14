package org.geolatte.geom.cga.exact;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * The correctly rounded minimum of a set of point-point and point-segment distances.
 *
 * <p>The squared distance of a point p to a point q is {@code |q - p|²}; that to a segment (a, b) is
 * {@code |p - a|²} or {@code |p - b|²} if the projection of p on the line through a and b falls outside the segment,
 * and {@code ((b - a) x (p - a))² / |b - a|²} otherwise. Which case applies is decided exactly
 * ({@link Predicates#dotSign}). Squared distances are therefore rational in the coordinates, and the distance, the
 * square root of a rational, can be rounded correctly: {@link #distance()} returns the double nearest to the exact
 * minimum distance (round to nearest, ties to even).</p>
 *
 * <p>The computation is staged:</p>
 * <ol>
 *     <li>each squared distance is enclosed in double-word arithmetic (relative width about 2^-100). Candidates that
 *     cannot be the minimum are dropped as they are added;</li>
 *     <li>the minimum lies within the enclosures of the remaining candidates. If its square root, computed in
 *     double-word arithmetic, has an enclosure without a rounding boundary, the rounding is decided. This is the
 *     case for practically all inputs;</li>
 *     <li>otherwise (exact ties with a midpoint, extreme magnitudes), the remaining candidates are compared exactly
 *     with {@link BigInteger} arithmetic, and the square root of the smallest is rounded exactly.</li>
 * </ol>
 */
public final class MinimumDistance {

    private static final double U = 0x1p-53;
    // relative error bounds of the double-word operations (Joldes, Muller & Popescu 2017: 5 u², 9.8 u², and a few
    // u² for the square root), with a margin
    private static final double MUL = 8 * U * U;
    private static final double DIV = 16 * U * U;
    private static final double SQRT = 16 * U * U;
    // parts of coordinate differences must be zero or in this range, so that the double-word computations are free
    // of underflow and overflow
    private static final double MIN_PART = 0x1p-200;
    private static final double MAX_PART = 0x1p200;
    private static final double MAX_REL = 0x1p-20;

    private static final byte POINT = 0, SEGMENT = 1;

    // per candidate: the kind, the coordinates (p and q, or p, a and b), and the enclosure of the squared distance:
    // v (1 - rel) .. v (1 + rel), with v = h + l. rel is infinite if the double-word stage could not be applied.
    private byte[] kinds = new byte[8];
    private double[] coordinates = new double[48];
    private double[] h = new double[8], l = new double[8], rel = new double[8];
    private int size = 0;
    private int compactedSize = 16;
    // the smallest upper bound of a squared distance
    private double bestUpper = Double.POSITIVE_INFINITY;

    /**
     * Adds the distance between the points p and q
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public void addPointPoint(double px, double py, double qx, double qy) {
        checkFinite(px, py, qx, qy);
        double xh = qx - px, xl = twoSumError(qx, -px, xh);
        double yh = qy - py, yl = twoSumError(qy, -py, yh);
        double vh, vl, r;
        if (xh == 0 && yh == 0) {
            vh = 0;
            vl = 0;
            r = 0;
        } else if (inRange(xh) && inRange(xl) && inRange(yh) && inRange(yl)) {
            Sum sum = new Sum();
            sum.addSquare(xh, xl);
            sum.addSquare(yh, yl);
            vh = sum.high();
            vl = sum.low();
            r = sum.bound() / Math.abs(vh);
        } else {
            vh = xh * xh + yh * yh;
            vl = 0;
            r = Double.POSITIVE_INFINITY;
        }
        add(POINT, vh, vl, r, px, py, qx, qy, 0, 0);
    }

    /**
     * Adds the distance between the point p and the segment (a, b)
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public void addPointSegment(double px, double py, double ax, double ay, double bx, double by) {
        checkFinite(px, py, ax, ay);
        checkFinite(bx, by, bx, by);
        if (ax == bx && ay == by) {
            addPointPoint(px, py, ax, ay);
            return;
        }
        // (p - a) . (b - a) <= 0: a is nearest; (p - b) . (a - b) <= 0: b is nearest
        if (Predicates.dotSign(ax, ay, px, py, ax, ay, bx, by) <= 0) {
            addPointPoint(px, py, ax, ay);
            return;
        }
        if (Predicates.dotSign(bx, by, px, py, bx, by, ax, ay) <= 0) {
            addPointPoint(px, py, bx, by);
            return;
        }
        // d² = c² / L², c = (b - a) x (p - a), L² = |b - a|²; differences exact as double-words
        double exh = bx - ax, exl = twoSumError(bx, -ax, exh), eyh = by - ay, eyl = twoSumError(by, -ay, eyh);
        double fxh = px - ax, fxl = twoSumError(px, -ax, fxh), fyh = py - ay, fyl = twoSumError(py, -ay, fyh);
        double vh = Double.NaN, vl = 0, r = Double.POSITIVE_INFINITY;
        if (inRange(exh) && inRange(exl) && inRange(eyh) && inRange(eyl)
                && inRange(fxh) && inRange(fxl) && inRange(fyh) && inRange(fyl)) {
            Sum c = new Sum();
            c.addProduct(exh, fyh);
            c.addProduct(exh, fyl);
            c.addProduct(exl, fyh);
            c.addProduct(exl, fyl);
            c.addProduct(-eyh, fxh);
            c.addProduct(-eyh, fxl);
            c.addProduct(-eyl, fxh);
            c.addProduct(-eyl, fxl);
            double ch = c.high(), cl = c.low(), cRel = c.bound() / Math.abs(ch);
            Sum len = new Sum();
            len.addSquare(exh, exl);
            len.addSquare(eyh, eyl);
            double lh = len.high(), ll = len.low(), lRel = len.bound() / Math.abs(lh);
            if (cRel <= MAX_REL && lRel <= MAX_REL && Math.abs(ch) >= 0x1p-400 && Math.abs(lh) >= 0x1p-400) {
                double[] c2 = mul(ch, cl, ch, cl);
                double[] q = div(c2[0], c2[1], lh, ll);
                vh = q[0];
                vl = q[1];
                // (1 + cRel)^2 (1 + MUL) (1 + lRel) (1 + DIV), to first order, with a margin
                r = (2 * cRel + lRel + MUL + DIV) * (1 + 0x1p-20);
            }
        }
        if (Double.isNaN(vh)) {
            double cx = (bx - ax) * (py - ay) - (by - ay) * (px - ax);
            vh = cx * cx / ((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
            r = Double.POSITIVE_INFINITY;
        }
        add(SEGMENT, vh, vl, r, px, py, ax, ay, bx, by);
    }

    /**
     * @return true if no distance was added
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * @return the minimum of the distances, correctly rounded; NaN if no distance was added
     */
    public double distance() {
        compact();
        if (size == 0) return Double.NaN;
        int m = -1;
        double maxRel = 0;
        for (int i = 0; i < size; i++) {
            maxRel = Math.max(maxRel, rel[i]);
            if (m < 0 || h[i] + l[i] < h[m] + l[m]) m = i;
        }
        if (h[m] == 0 && l[m] == 0 && rel[m] == 0) return 0;
        if (maxRel <= MAX_REL && h[m] >= 0x1p-900 && h[m] <= 0x1p900) {
            // the minimum lies in v_m (1 -/+ maxRel): its square root in sqrt(v_m) (1 -/+ maxRel / 2)
            double[] s = sqrt(h[m], l[m]);
            double e = (maxRel / 2 + SQRT) * Math.abs(s[0]) * (1 + 0x1p-20);
            double result = Constructions.roundFromEnclosure(s[0], s[1], e);
            if (!Double.isNaN(result)) return result;
        }
        return exactDistance();
    }

    // ---------------------------------------------------------------------------------------------------------
    // candidates
    // ---------------------------------------------------------------------------------------------------------

    private void add(byte kind, double vh, double vl, double r, double c0, double c1, double c2, double c3,
                     double c4, double c5) {
        double v = vh + vl;
        double lower = r == Double.POSITIVE_INFINITY ? 0 : v * (1 - r) * (1 - 0x1p-50);
        double upper = r == Double.POSITIVE_INFINITY ? Double.POSITIVE_INFINITY : v * (1 + r) * (1 + 0x1p-50);
        if (lower > bestUpper) return;
        if (upper < bestUpper) bestUpper = upper;
        if (size == kinds.length) {
            int capacity = 2 * size;
            kinds = Arrays.copyOf(kinds, capacity);
            coordinates = Arrays.copyOf(coordinates, 6 * capacity);
            h = Arrays.copyOf(h, capacity);
            l = Arrays.copyOf(l, capacity);
            rel = Arrays.copyOf(rel, capacity);
        }
        kinds[size] = kind;
        h[size] = vh;
        l[size] = vl;
        rel[size] = r;
        int o = 6 * size;
        coordinates[o] = c0;
        coordinates[o + 1] = c1;
        coordinates[o + 2] = c2;
        coordinates[o + 3] = c3;
        coordinates[o + 4] = c4;
        coordinates[o + 5] = c5;
        size++;
        if (size >= 2 * compactedSize) {
            compact();
            compactedSize = Math.max(16, size);
        }
    }

    // removes the candidates that cannot be the minimum
    private void compact() {
        int kept = 0;
        for (int i = 0; i < size; i++) {
            double lower = rel[i] == Double.POSITIVE_INFINITY ? 0 : (h[i] + l[i]) * (1 - rel[i]) * (1 - 0x1p-50);
            if (lower > bestUpper) continue;
            kinds[kept] = kinds[i];
            h[kept] = h[i];
            l[kept] = l[i];
            rel[kept] = rel[i];
            System.arraycopy(coordinates, 6 * i, coordinates, 6 * kept, 6);
            kept++;
        }
        size = kept;
    }

    // ---------------------------------------------------------------------------------------------------------
    // the exact stage
    // ---------------------------------------------------------------------------------------------------------

    // the exact squared distance of candidate i, as num / den * 2^exp
    private static final class Rational {
        final BigInteger num, den;
        final int exp;

        Rational(BigInteger num, BigInteger den, int exp) {
            this.num = num;
            this.den = den;
            this.exp = exp;
        }

        // the sign of this - other
        int compareTo(Rational other) {
            BigInteger left = num.multiply(other.den), right = other.num.multiply(den);
            if (exp > other.exp) {
                left = left.shiftLeft(exp - other.exp);
            } else {
                right = right.shiftLeft(other.exp - exp);
            }
            return left.compareTo(right);
        }
    }

    private Rational exact(int i) {
        int o = 6 * i;
        int n = kinds[i] == POINT ? 4 : 6;
        int k = 0;
        for (int j = 0; j < n; j++) {
            double v = coordinates[o + j];
            if (v != 0) k = Math.max(k, -Constructions.lowestBitExponent(v));
        }
        BigInteger px = Constructions.scaled(coordinates[o], k), py = Constructions.scaled(coordinates[o + 1], k);
        BigInteger ax = Constructions.scaled(coordinates[o + 2], k), ay = Constructions.scaled(coordinates[o + 3], k);
        if (kinds[i] == POINT) {
            BigInteger dx = ax.subtract(px), dy = ay.subtract(py);
            return new Rational(dx.multiply(dx).add(dy.multiply(dy)), BigInteger.ONE, -2 * k);
        }
        BigInteger bx = Constructions.scaled(coordinates[o + 4], k), by = Constructions.scaled(coordinates[o + 5], k);
        BigInteger ex = bx.subtract(ax), ey = by.subtract(ay), fx = px.subtract(ax), fy = py.subtract(ay);
        BigInteger c = ex.multiply(fy).subtract(ey.multiply(fx));
        // c is scaled by 2^2k, the squared length by 2^2k
        return new Rational(c.multiply(c), ex.multiply(ex).add(ey.multiply(ey)), -2 * k);
    }

    private double exactDistance() {
        Rational min = null;
        for (int i = 0; i < size; i++) {
            Rational r = exact(i);
            if (min == null || r.compareTo(min) < 0) min = r;
        }
        return sqrtQuotient(min.num, min.den, min.exp);
    }

    /**
     * @return {@code sqrt(num / den * 2^exp)}, correctly rounded (round to nearest, ties to even)
     */
    static double sqrtQuotient(BigInteger num, BigInteger den, int exp) {
        if (num.signum() == 0) return 0;
        if ((exp & 1) != 0) {
            num = num.shiftLeft(1);
            exp--;
        }
        // q = floor(num 2^s / den) with at least 120 bits, s even; then t = floor(sqrt(q)) has at least 60 bits
        int s = Math.max(0, 122 - (num.bitLength() - den.bitLength()));
        s += s & 1;
        BigInteger[] qr = num.shiftLeft(s).divideAndRemainder(den);
        BigInteger t = qr[0].sqrt();
        boolean sticky = qr[1].signum() != 0 || !t.multiply(t).equals(qr[0]);
        // the result is (t + f) 2^((exp - s) / 2), 0 <= f < 1, f > 0 iff sticky
        return round(t, (exp - s) / 2, sticky);
    }

    // (t + f) 2^e rounded to nearest (ties to even), for t with at least 60 bits, 0 <= f < 1, f > 0 iff sticky
    private static double round(BigInteger t, int e, boolean sticky) {
        int top = t.bitLength() - 1 + e;    // floor(log2 of the value)
        int ulpExponent = Math.max(top - 52, -1074);
        int shift = ulpExponent - e;        // at least 7
        if (top > 1023) return Double.POSITIVE_INFINITY;
        BigInteger significand = t.shiftRight(shift);
        BigInteger remainder = t.subtract(significand.shiftLeft(shift));
        BigInteger half = BigInteger.ONE.shiftLeft(shift - 1);
        int c = remainder.compareTo(half);
        if (c > 0 || (c == 0 && (sticky || significand.testBit(0)))) significand = significand.add(BigInteger.ONE);
        return Math.scalb(significand.doubleValue(), ulpExponent);   // exact: at most 2^53
    }

    // ---------------------------------------------------------------------------------------------------------
    // double-word arithmetic
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Sums products of doubles exactly into a running TwoSum sum, with a rigorous bound on the error of the
     * floating point sum of the error terms
     */
    private static final class Sum {
        double s, err, abs;
        int k;

        void addProduct(double a, double b) {
            if (a == 0 || b == 0) return;
            double p = a * b;
            double t = s + p;
            addError(twoSumError(s, p, t));
            s = t;
            addError(Math.fma(a, b, -p));
        }

        // adds (h + l)^2
        void addSquare(double h, double l) {
            addProduct(h, h);
            addProduct(2 * h, l);
            addProduct(l, l);
        }

        private void addError(double e) {
            err += e;
            abs += Math.abs(e);
            k++;
        }

        double high() {
            return s + err;
        }

        double low() {
            return twoSumError(s, err, s + err);
        }

        double bound() {
            return (k + 4) * 0x1p-51 * abs;
        }
    }

    // (xh + xl) (yh + yl) in double-word arithmetic (Joldes et al., Alg. 12)
    private static double[] mul(double xh, double xl, double yh, double yl) {
        double ch = xh * yh;
        double cl1 = Math.fma(xh, yh, -ch);
        double tl0 = xl * yl;
        double tl1 = Math.fma(xh, yl, tl0);
        double cl2 = Math.fma(xl, yh, tl1);
        double cl3 = cl1 + cl2;
        double zh = ch + cl3;
        return new double[]{zh, cl3 - (zh - ch)};
    }

    // (nh + nl) / (dh + dl) in double-word arithmetic, as in Constructions
    private static double[] div(double nh, double nl, double dh, double dl) {
        double q1 = nh / dh;
        double r1 = Math.fma(-q1, dh, nh);
        double r2 = r1 + nl;
        double r3 = Math.fma(-q1, dl, r2);
        double q2 = r3 / dh;
        double th = q1 + q2;
        return new double[]{th, q2 - (th - q1)};
    }

    // sqrt(h + l) in double-word arithmetic, for h > 0: one Newton step from the rounded square root of h
    private static double[] sqrt(double h, double l) {
        double s = Math.sqrt(h);
        double r = Math.fma(-s, s, h) + l;
        double t = r / (2 * s);
        double zh = s + t;
        return new double[]{zh, t - (zh - s)};
    }

    private static boolean inRange(double v) {
        double a = Math.abs(v);
        return a == 0 || (a >= MIN_PART && a <= MAX_PART);
    }

    private static void checkFinite(double a, double b, double c, double d) {
        if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c) || !Double.isFinite(d)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
    }

    // Knuth's TwoSum: (a + b) - s computed without rounding error
    private static double twoSumError(double a, double b, double s) {
        double bv = s - a;
        double av = s - bv;
        return (a - av) + (b - bv);
    }
}
