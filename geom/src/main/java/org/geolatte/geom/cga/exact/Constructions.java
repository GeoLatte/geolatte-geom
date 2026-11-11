package org.geolatte.geom.cga.exact;

import org.geolatte.geom.LineSegment;
import org.geolatte.geom.Position;
import org.geolatte.geom.Positions;
import org.geolatte.geom.SegmentIntersectionResult;

import java.math.BigInteger;

/**
 * Geometric constructions with a guaranteed accuracy.
 *
 * <p>Unlike predicates, constructions produce new coordinates, which in general are not representable as
 * doubles. The constructions here therefore guarantee the best possible result: each computed coordinate is
 * the <em>correctly rounded</em> value of the exact coordinate (round to nearest, ties to even), i.e. it has
 * an error of at most half a unit in the last place.</p>
 *
 * <h2>Intersection point of crossing segments</h2>
 *
 * <p>For segments a&rarr;b and c&rarr;d, the exact intersection point is {@code x = Nx / D},
 * {@code y = Ny / D} with</p>
 * <pre>
 *   D  = (bx - ax)(dy - cy) - (by - ay)(dx - cx)
 *   Nx = (bx - ax)(cx dy - dx cy) - (dx - cx)(ax by - bx ay)
 *   Ny = (by - ay)(cx dy - dx cy) - (dy - cy)(ax by - bx ay)
 * </pre>
 * <p>Each coordinate is rounded in up to three stages:</p>
 * <ol>
 *     <li>a filter computes the coordinate in double-word arithmetic (about 106 bits), as
 *     {@code x = ax + (bx - ax) * alpha / (alpha - beta)} with {@code alpha} and {@code beta} the orientations of
 *     a and b with respect to c&rarr;d, together with a rigorous bound on its error. When the resulting
 *     enclosure does not contain a rounding boundary (the midpoint between two consecutive doubles), the
 *     rounding is decided. This is the case for practically all inputs;</li>
 *     <li>otherwise, the side of the midpoint on which the exact coordinate lies is decided by the exact sign of
 *     a polynomial expression, using {@link ExactSum} and {@link Essa};</li>
 *     <li>if that is not possible (for an ill conditioned or extreme input), the quotient is rounded exactly using
 *     {@link BigInteger} arithmetic.</li>
 * </ol>
 *
 * <p>The double-word algorithms and error bounds follow M. Joldes, J.-M. Muller and V. Popescu, "Tight and
 * rigorous error bounds for basic building blocks of double-word arithmetic", ACM TOMS 44(2), 2017. The bounds
 * used here are derived for the specific algorithms below, and are applied with a safety margin.</p>
 *
 * <h2>Circumcenter</h2>
 *
 * <p>The center of the circle through a, b and c is {@code a + o}, with (writing b' = b - a, c' = c - a)</p>
 * <pre>
 *   ox = (c'y |b'|² - b'y |c'|²) / D,   oy = (b'x |c'|² - c'x |b'|²) / D,   D = 2 (b'x c'y - b'y c'x)
 * </pre>
 * <p>The coordinates {@code a + o} are enclosed in double-word arithmetic with a rigorous error bound, in the
 * same way as the crossing point (stage 1 above). When the enclosure does not contain a rounding boundary, the
 * rounding is decided; otherwise (for nearly collinear points, extreme magnitudes, or a coordinate extremely close
 * to a rounding boundary) the coordinate is rounded exactly with {@link BigInteger} arithmetic.</p>
 */
public final class Constructions {

    private static final double U = 0x1p-53;

    // relative error bound of double-word multiplication (6.1 u^2) plus division (13.1 u^2), rounded up
    private static final double DW_REL_ERROR = 32 * U * U;

    // The filter is applied only to coordinates in this range (or zero), so that all products of (parts of)
    // coordinate differences are free of underflow and overflow.
    private static final double MIN_FILTER_COORDINATE = 0x1p-300;
    private static final double MAX_FILTER_COORDINATE = 0x1p300;

    // Lower bound for the magnitude of intermediate double-word results in the filter. Ensures that the
    // remainder computations are exact, and that underflow errors are negligible relative to the error bounds.
    private static final double MIN_FILTER_RESULT = 0x1p-900;

    // The filter is abandoned if the relative error of the orientations exceeds this value
    private static final double MAX_REL_ERROR = 0x1p-20;

    private Constructions() {
    }

    /**
     * Computes the intersection of a segment with another segment.
     *
     * <p>The kind of intersection is determined exactly (see
     * {@link Predicates#segmentIntersection(Position, Position, Position, Position)}). Then:</p>
     * <ul>
     *     <li>if the segments touch or overlap, the common point or the endpoints of the common segment are endpoints
     *     of the input segments. Their X and Y coordinates are returned exactly;</li>
     *     <li>if the segments cross, the X and Y coordinates of the crossing point are the correctly rounded values
     *     of the exact coordinates (see {@link #crossingPoint(double, double, double, double, double, double,
     *     double, double)}).</li>
     * </ul>
     *
     * <p>Further coordinates (Z, M) are interpolated linearly along {@code segment}. Positions of
     * {@code segment} are reused when they are (an endpoint of) the intersection.</p>
     *
     * @param segment the segment that determines the type of the positions in the result, and along which further
     *                coordinates are interpolated
     * @param other   the other segment
     * @param <P>     the position type
     * @return the intersection
     * @throws IllegalArgumentException if an X or Y coordinate is NaN or infinite
     */
    public static <P extends Position> SegmentIntersectionResult<P> intersection(LineSegment<P> segment,
                                                                                 LineSegment<?> other) {
        P a = segment.getStartPosition();
        P b = segment.getEndPosition();
        Position c = other.getStartPosition();
        Position d = other.getEndPosition();
        double ax = x(a), ay = y(a), bx = x(b), by = y(b), cx = x(c), cy = y(c), dx = x(d), dy = y(d);
        int code = Predicates.classifySegments(ax, ay, bx, by, cx, cy, dx, dy);
        switch (Predicates.kind(code)) {
            case CROSSING:
                int signD = (code & Predicates.CROSSING_POSITIVE) != 0 ? 1 : -1;
                double[] xy = crossingPoint(ax, ay, bx, by, cx, cy, dx, dy, signD);
                return SegmentIntersectionResult.crossing(positionOn(a, b, xy[0], xy[1]));
            case TOUCHING:
                return SegmentIntersectionResult.touching(endpoint(Predicates.firstEndpoint(code), a, b, c, d));
            case OVERLAPPING:
                return SegmentIntersectionResult.overlapping(new LineSegment<>(
                        endpoint(Predicates.firstEndpoint(code), a, b, c, d),
                        endpoint(Predicates.lastEndpoint(code), a, b, c, d)));
            default:
                return SegmentIntersectionResult.disjoint();
        }
    }

    /**
     * Computes the crossing point of the segments (a, b) and (c, d).
     *
     * <p>Both coordinates of the result are the correctly rounded (round to nearest, ties to even) values of the
     * exact coordinates of the crossing point. Consequently:</p>
     * <ul>
     *     <li>the result is unique: it does not depend on the order or the orientation of the segments;</li>
     *     <li>if the exact crossing point is representable, it is returned;</li>
     *     <li>the result lies in the bounding boxes of both segments;</li>
     *     <li>the result generally does not lie exactly on either segment (it is the nearest representable
     *     point).</li>
     * </ul>
     *
     * @return the X and Y coordinate of the crossing point
     * @throws IllegalArgumentException if the segments do not cross (see
     *                                  {@link org.geolatte.geom.SegmentIntersection#CROSSING}), or a coordinate is NaN
     *                                  or infinite
     */
    public static double[] crossingPoint(double ax, double ay, double bx, double by,
                                         double cx, double cy, double dx, double dy) {
        int code = Predicates.classifySegments(ax, ay, bx, by, cx, cy, dx, dy);
        if ((code & Predicates.KIND_MASK) != Predicates.CROSSING_CODE) {
            throw new IllegalArgumentException("Segments do not cross, but are " + Predicates.kind(code));
        }
        int signD = (code & Predicates.CROSSING_POSITIVE) != 0 ? 1 : -1;
        return crossingPoint(ax, ay, bx, by, cx, cy, dx, dy, signD);
    }

    // signD is the sign of D, which is known from the classification
    private static double[] crossingPoint(double ax, double ay, double bx, double by,
                                          double cx, double cy, double dx, double dy, int signD) {
        double[] enclosure = new double[6];
        double x = Double.NaN, y = Double.NaN;
        if (enclose(ax, ay, bx, by, cx, cy, dx, dy, signD, enclosure)) {
            x = round(false, enclosure[0], enclosure[1], enclosure[2], signD, ax, ay, bx, by, cx, cy, dx, dy);
            y = round(true, enclosure[3], enclosure[4], enclosure[5], signD, ax, ay, bx, by, cx, cy, dx, dy);
        }
        if (Double.isNaN(x)) x = roundExactly(false, ax, ay, bx, by, cx, cy, dx, dy);
        if (Double.isNaN(y)) y = roundExactly(true, ax, ay, bx, by, cx, cy, dx, dy);
        // normalize -0.0 to 0.0, so that the result is unique
        return new double[]{x + 0.0, y + 0.0};
    }

    private static double round(boolean yAxis, double q, double l, double e, int signD,
                                double ax, double ay, double bx, double by,
                                double cx, double cy, double dx, double dy) {
        double r = roundFromEnclosure(q, l, e);
        if (Double.isNaN(r)) {
            r = roundWithEssa(yAxis, q, l, e, signD, ax, ay, bx, by, cx, cy, dx, dy);
        }
        return r;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Stage 1: double-word enclosure
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Computes enclosures of the crossing point's coordinates in double-word arithmetic: the exact X coordinate
     * lies in {@code [out[0] + out[1] - out[2], out[0] + out[1] + out[2]]}, with {@code out[0]} the double nearest
     * to {@code out[0] + out[1]}, and similarly for the Y coordinate in {@code out[3..5]}.
     *
     * @return false if the filter could not be applied (extreme magnitudes, ill-conditioned input)
     */
    static boolean enclose(double ax, double ay, double bx, double by,
                           double cx, double cy, double dx, double dy, int signD, double[] out) {
        if (!inFilterRange(ax) || !inFilterRange(ay) || !inFilterRange(bx) || !inFilterRange(by)
                || !inFilterRange(cx) || !inFilterRange(cy) || !inFilterRange(dx) || !inFilterRange(dy)) {
            return false;
        }
        // coordinate differences, exactly as double-words
        double sxh = dx - cx, sxl = twoSumError(dx, -cx, sxh);
        double syh = dy - cy, syl = twoSumError(dy, -cy, syh);
        double exh = ax - cx, exl = twoSumError(ax, -cx, exh);
        double eyh = ay - cy, eyl = twoSumError(ay, -cy, eyh);
        double fxh = bx - cx, fxl = twoSumError(bx, -cx, fxh);
        double fyh = by - cy, fyl = twoSumError(by, -cy, fyh);

        // alpha = orientation(c, d, a) = s x e, beta = orientation(c, d, b) = s x f. D = alpha - beta.
        Accumulator acc = new Accumulator();
        acc.addCross(sxh, sxl, syh, syl, exh, exl, eyh, eyl);
        double alphaH = acc.high(), alphaL = acc.low(), alphaErr = acc.errorBound();
        acc.reset();
        acc.addCross(sxh, sxl, syh, syl, fxh, fxl, fyh, fyl);
        double betaH = acc.high(), betaL = acc.low(), betaErr = acc.errorBound();
        // For crossing segments, alpha and beta have opposite signs, the sign of alpha being the sign of D
        if (!(alphaErr <= MAX_REL_ERROR * Math.abs(alphaH)) || !(betaErr <= MAX_REL_ERROR * Math.abs(betaH))
                || Math.signum(alphaH) != signD || Math.signum(betaH) != -signD) {
            return false;
        }
        acc.reset();
        acc.add(alphaH);
        acc.add(alphaL);
        acc.add(-betaH);
        acc.add(-betaL);
        double denH = acc.high(), denL = acc.low();
        double denErr = acc.errorBound() + alphaErr + betaErr; // no cancellation: alpha and -beta have equal signs

        // relative error bound of alpha / (alpha - beta), including the double-word multiplication and division
        double rel = alphaErr / Math.abs(alphaH) + denErr / Math.abs(denH) + DW_REL_ERROR;
        return encloseCoordinate(ax, bx, cx, dx, alphaH, alphaL, denH, denL, rel, out, 0)
                && encloseCoordinate(ay, by, cy, dy, alphaH, alphaL, denH, denL, rel, out, 3);
    }

    // encloses p = pa + (pb - pa) * alpha / den
    private static boolean encloseCoordinate(double pa, double pb, double pc, double pd,
                                             double alphaH, double alphaL, double denH, double denL, double rel,
                                             double[] out, int offset) {
        if (pa == pb || pc == pd) {
            // one of the segments is orthogonal to this axis: the coordinate is exact
            out[offset] = pa == pb ? pa : pc;
            out[offset + 1] = 0;
            out[offset + 2] = 0;
            return true;
        }
        double rh = pb - pa, rl = twoSumError(pb, -pa, rh);

        // n = r * alpha in double-word arithmetic (Joldes et al., Alg. 12).
        // Relative error <= 6.1 u^2 (for normalized double-word operands and no underflow).
        double ch = rh * alphaH;
        double cl1 = Math.fma(rh, alphaH, -ch);
        double tl0 = rl * alphaL;
        double tl1 = Math.fma(rh, alphaL, tl0);
        double cl2 = Math.fma(rl, alphaH, tl1);
        double cl3 = cl1 + cl2;
        double nh = ch + cl3;
        double nl = cl3 - (nh - ch);
        if (!(Math.abs(nh) >= MIN_FILTER_RESULT)) return false;
        return encloseSumOfQuotient(pa, nh, nl, denH, denL, rel, out, offset);
    }

    /**
     * Encloses {@code pa + n / den} for normalized double-words n and den, in the form {@code [out[offset] +
     * out[offset + 1] -/+ out[offset + 2]]}. The relative error bound rel must cover the errors of n and den, and
     * the double-word division (13.1 u^2).
     *
     * @return false if an intermediate result is too small for the error bounds to hold
     */
    private static boolean encloseSumOfQuotient(double pa, double nh, double nl, double denH, double denL,
                                                double rel, double[] out, int offset) {
        // t = n / den in double-word arithmetic. With q1 = RN(nh / denh), the remainder nh - q1 * denh is
        // representable and computed exactly by fma. Relative error <= 13.1 u^2.
        double q1 = nh / denH;
        double r1 = Math.fma(-q1, denH, nh);
        double r2 = r1 + nl;
        double r3 = Math.fma(-q1, denL, r2);
        double q2 = r3 / denH;
        double th = q1 + q2;
        double tl = q2 - (th - q1);
        if (!(Math.abs(th) >= MIN_FILTER_RESULT)) return false;

        // p = pa + t, with one rounding error (of r)
        double s = pa + th;
        double r = twoSumError(pa, th, s) + tl;
        double ph = s + r;
        double pl = twoSumError(s, r, ph);
        // error of t (relative error rel, amplified by at most 1.04 for rel < 2^-19) and of r; the factors 4 and 2
        // also cover the rounding errors in computing this bound.
        out[offset] = ph;
        out[offset + 1] = pl;
        out[offset + 2] = 4 * rel * Math.abs(th) + 2 * U * Math.abs(r);
        return true;
    }

    private static boolean inFilterRange(double v) {
        double a = Math.abs(v);
        return a == 0 || (a >= MIN_FILTER_COORDINATE && a <= MAX_FILTER_COORDINATE);
    }

    /**
     * Determines the correctly rounded value of a number that lies in {@code [q + l - e, q + l + e]}, with q
     * the double nearest to {@code q + l}.
     *
     * @return the correctly rounded value, or NaN if the enclosure contains a rounding boundary.
     */
    static double roundFromEnclosure(double q, double l, double e) {
        double up = Math.nextUp(q) - q;     // the gaps to the neighbours of q, which differ if q is a power of 2
        double down = q - Math.nextDown(q);
        double hi = Interval.addUp(l, e);
        double lo = Interval.addDown(l, -e);
        // the exact value lies strictly between the midpoints q - down/2 and q + up/2 (multiplying by 2 is exact)
        if (2 * hi < up && 2 * lo > -down) return q;
        return Double.NaN;
    }

    /**
     * Sums products of doubles, in double-word precision with a rigorous error bound.
     *
     * <p>Each product is split exactly into p + e (with fma); the values p are summed exactly with TwoSum, keeping
     * the leading sum s; all errors (of TwoSum and fma) are summed in floating point. The exact sum is
     * {@code s + (sum of errors)}, and the floating point sum of k errors has an error of at most
     * {@code gamma(k-1) * (sum of their absolute values)}.</p>
     */
    private static final class Accumulator {
        private double s, err, abs;
        private int k;

        void reset() {
            s = err = abs = 0;
            k = 0;
        }

        void add(double p) {
            double t = s + p;
            addError(twoSumError(s, p, t));
            s = t;
        }

        void addProduct(double a, double b) {
            if (a == 0 || b == 0) return;
            double p = a * b;
            add(p);
            addError(Math.fma(a, b, -p));
        }

        // adds (h + l)^2
        void addSquare(double h, double l) {
            addProduct(h, h);
            addProduct(2 * h, l);
            addProduct(l, l);
        }

        // adds (sx, sy) x (ex, ey) for double-word operands
        void addCross(double sxh, double sxl, double syh, double syl,
                      double exh, double exl, double eyh, double eyl) {
            addProduct(sxh, eyh);
            addProduct(sxh, eyl);
            addProduct(sxl, eyh);
            addProduct(sxl, eyl);
            addProduct(-syh, exh);
            addProduct(-syh, exl);
            addProduct(-syl, exh);
            addProduct(-syl, exl);
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

        // gamma(k-1) / (1 - gamma(k-1)) <= 2 (k-1) u for small k; the factor 4 k covers the rounding of abs and
        // of this product
        double errorBound() {
            return k * 0x1p-51 * abs;
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Stage 2: exact sign tests
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Rounds the X (or Y) coordinate of the crossing point using exact sign tests, given the enclosure
     * {@code [q + l - e, q + l + e]}, with q the double nearest to {@code q + l}.
     *
     * @return the correctly rounded coordinate, or NaN if not decided
     */
    static double roundWithEssa(boolean yAxis, double q, double l, double e, int signD,
                                double ax, double ay, double bx, double by,
                                double cx, double cy, double dx, double dy) {
        double up = Math.nextUp(q) - q;
        double down = q - Math.nextDown(q);
        ExactSum den = denominatorTerms(ax, ay, bx, by, cx, cy, dx, dy);
        ExactSum sum = new ExactSum(96);
        addNumeratorTerms(sum, 2, yAxis, ax, ay, bx, by, cx, cy, dx, dy);   // 2N
        sum.addProduct(-2 * q, den);                                        // 2N - 2qD
        if (4 * e < Math.min(up, down)) {
            // The enclosure contains at most one midpoint m = q + g/2, on the side of l. The coordinate is N / D,
            // and sign(N/D - m) = sign(D) * sign(2N - 2qD - gD).
            boolean above = 2 * Interval.addUp(l, e) >= up;
            double g = above ? up : -down;
            sum.addProduct(-g, den);
            if (!sum.isExact()) return Double.NaN;
            int side = sum.signum() * signD;
            double neighbour = above ? Math.nextUp(q) : Math.nextDown(q);
            if (side == 0) {
                return isEven(q) ? q : neighbour; // a tie
            }
            return (side > 0) == above ? neighbour : q;
        }
        // The enclosure is wide (ill-conditioned input). Check whether q is the exact value.
        if (sum.isExact() && sum.signum() == 0) return q;
        return Double.NaN;
    }

    private static boolean isEven(double q) {
        return (Double.doubleToRawLongBits(q) & 1) == 0;
    }

    // D = (bx - ax)(dy - cy) - (by - ay)(dx - cx)
    static ExactSum denominatorTerms(double ax, double ay, double bx, double by,
                                     double cx, double cy, double dx, double dy) {
        return new ExactSum(16)
                .addProduct(bx, dy).addProduct(-bx, cy).addProduct(-ax, dy).addProduct(ax, cy)
                .addProduct(-by, dx).addProduct(by, cx).addProduct(ay, dx).addProduct(-ay, cx);
    }

    // adds factor * N, with N = (pb - pa)(cx dy - dx cy) - (pd - pc)(ax by - bx ay), p = x or y
    static void addNumeratorTerms(ExactSum sum, double factor, boolean yAxis,
                                  double ax, double ay, double bx, double by,
                                  double cx, double cy, double dx, double dy) {
        double pa = factor * (yAxis ? ay : ax);
        double pb = factor * (yAxis ? by : bx);
        double pc = factor * (yAxis ? cy : cx);
        double pd = factor * (yAxis ? dy : dx);
        sum.addProduct(pb, cx, dy).addProduct(-pb, dx, cy).addProduct(-pa, cx, dy).addProduct(pa, dx, cy)
                .addProduct(-pd, ax, by).addProduct(pd, bx, ay).addProduct(pc, ax, by).addProduct(-pc, bx, ay);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Stage 3: exact rational arithmetic
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Returns the center of the circle through a, b and c, with each coordinate correctly rounded (round to
     * nearest, ties to even).
     *
     * <p>Since the result is the unique correctly rounded value of the exact circumcenter, it does not depend on
     * the order of a, b and c: all triangles with the same circumcircle get exactly the same center.</p>
     *
     * @return {@code {x, y}}; a coordinate whose magnitude exceeds the largest double is infinite
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or if a, b and c are collinear
     */
    public static double[] circumcenter(double ax, double ay, double bx, double by, double cx, double cy) {
        for (double v : new double[]{ax, ay, bx, by, cx, cy}) {
            if (!Double.isFinite(v)) throw new IllegalArgumentException("Coordinates must be finite");
        }
        if (Predicates.orientation(ax, ay, bx, by, cx, cy) == 0) {
            throw new IllegalArgumentException("Points are collinear");
        }
        double[] result = circumcenterDoubleWord(ax, ay, bx, by, cx, cy);
        if (Double.isNaN(result[0])) result[0] = circumcenterExactly(false, ax, ay, bx, by, cx, cy);
        if (Double.isNaN(result[1])) result[1] = circumcenterExactly(true, ax, ay, bx, by, cx, cy);
        return result;
    }

    /**
     * The correctly rounded coordinates of the circumcenter of (non-collinear) a, b, c, as far as they can be
     * decided from a double-word enclosure; NaN for an undecided coordinate.
     *
     * <p>The differences b' = b - a and c' = c - a are computed exactly as double-words. D, |b'|² and |c'|² are
     * sums of products of their parts, accumulated with a rigorous error bound; the numerators are then
     * accumulated from the parts of the differences and of the (inexact) squared lengths, adding the propagated
     * error of the latter. The quotient and the final sum use double-word arithmetic (as for the crossing point),
     * which gives an enclosure of width about u² times the offset.</p>
     *
     * <p>The stage is only applied when every non-zero part of a difference lies in [2^-200, 2^200]: then every
     * product of up to four such parts is a multiple of 2^-1008 and far from overflow, so all error-free
     * transformations are exact.</p>
     */
    static double[] circumcenterDoubleWord(double ax, double ay, double bx, double by, double cx, double cy) {
        double[] result = {Double.NaN, Double.NaN};
        double bxh = bx - ax, bxl = twoSumError(bx, -ax, bxh), byh = by - ay, byl = twoSumError(by, -ay, byh);
        double cxh = cx - ax, cxl = twoSumError(cx, -ax, cxh), cyh = cy - ay, cyl = twoSumError(cy, -ay, cyh);
        if (!(inProductRange(bxh) && inProductRange(bxl) && inProductRange(byh) && inProductRange(byl)
                && inProductRange(cxh) && inProductRange(cxl) && inProductRange(cyh) && inProductRange(cyl))) {
            return result;
        }
        Accumulator acc = new Accumulator();
        // D = b' x c', doubled below
        acc.addCross(bxh, bxl, byh, byl, cxh, cxl, cyh, cyl);
        double dh = acc.high(), dl = acc.low(), dErr = acc.errorBound();
        if (!(dErr <= MAX_REL_ERROR * Math.abs(dh))) return result;
        // |b'|² and |c'|²
        acc.reset();
        acc.addSquare(bxh, bxl);
        acc.addSquare(byh, byl);
        double b2h = acc.high(), b2l = acc.low(), b2Err = acc.errorBound();
        acc.reset();
        acc.addSquare(cxh, cxl);
        acc.addSquare(cyh, cyl);
        double c2h = acc.high(), c2l = acc.low(), c2Err = acc.errorBound();

        // ox = (c'y |b'|² - b'y |c'|²) / 2D,   oy = (b'x |c'|² - c'x |b'|²) / 2D
        result[0] = circumcenterCoordinate(acc, ax, cyh, cyl, b2h, b2l, b2Err, byh, byl, c2h, c2l, c2Err, dh, dl, dErr);
        result[1] = circumcenterCoordinate(acc, ay, bxh, bxl, c2h, c2l, c2Err, cxh, cxl, b2h, b2l, b2Err, dh, dl, dErr);
        return result;
    }

    // the correctly rounded value of pa + (p q - r s) / 2D, for exact double-words p and r, and double-words q and s
    // with error bounds; NaN if undecided
    private static double circumcenterCoordinate(Accumulator acc, double pa,
                                                 double ph, double pl, double qh, double ql, double qErr,
                                                 double rh, double rl, double sh, double sl, double sErr,
                                                 double dh, double dl, double dErr) {
        acc.reset();
        acc.addProduct(ph, qh);
        acc.addProduct(ph, ql);
        acc.addProduct(pl, qh);
        acc.addProduct(pl, ql);
        acc.addProduct(-rh, sh);
        acc.addProduct(-rh, sl);
        acc.addProduct(-rl, sh);
        acc.addProduct(-rl, sl);
        double nh = acc.high(), nl = acc.low();
        // the errors of q and s, multiplied by |p| <= |ph| (1 + u) and |r|; the factor covers the rounding of this
        // bound
        double nErr = (acc.errorBound() + (Math.abs(ph) * qErr + Math.abs(rh) * sErr)) * (1 + 0x1p-40);
        if (nh == 0 && nErr == 0) return pa;   // the offset is exactly zero
        if (!(Math.abs(nh) >= MIN_FILTER_RESULT) || !(nErr <= MAX_REL_ERROR * Math.abs(nh))) return Double.NaN;
        double rel = nErr / Math.abs(nh) + dErr / Math.abs(dh) + DW_REL_ERROR;
        double[] enclosure = new double[3];
        if (!encloseSumOfQuotient(pa, nh, nl, 2 * dh, 2 * dl, rel, enclosure, 0)) return Double.NaN;
        return roundFromEnclosure(enclosure[0], enclosure[1], enclosure[2]);
    }

    private static boolean inProductRange(double v) {
        double a = Math.abs(v);
        return a == 0 || (a >= 0x1p-200 && a <= 0x1p200);
    }

    /**
     * Computes the correctly rounded X (or Y) coordinate of the circumcenter with exact integer arithmetic.
     */
    static double circumcenterExactly(boolean yAxis, double ax, double ay, double bx, double by, double cx, double cy) {
        int k = 0;
        for (double v : new double[]{ax, ay, bx, by, cx, cy}) {
            if (v != 0) k = Math.max(k, -lowestBitExponent(v));
        }
        BigInteger iax = scaled(ax, k), iay = scaled(ay, k);
        BigInteger bpx = scaled(bx, k).subtract(iax), bpy = scaled(by, k).subtract(iay);
        BigInteger cpx = scaled(cx, k).subtract(iax), cpy = scaled(cy, k).subtract(iay);
        BigInteger den = bpx.multiply(cpy).subtract(bpy.multiply(cpx)).shiftLeft(1);
        BigInteger b2 = bpx.multiply(bpx).add(bpy.multiply(bpy)), c2 = cpx.multiply(cpx).add(cpy.multiply(cpy));
        BigInteger offset = yAxis
                ? bpx.multiply(c2).subtract(cpx.multiply(b2))
                : cpy.multiply(b2).subtract(bpy.multiply(c2));
        // coordinate = a + offset / den; offset is scaled by 2^3k, den and a * den by 2^2k and 2^3k
        BigInteger num = (yAxis ? iay : iax).multiply(den).add(offset);
        return roundQuotient(num, den, -k);
    }

    /**
     * Computes the correctly rounded X (or Y) coordinate of the crossing point with exact integer arithmetic.
     */
    static double roundExactly(boolean yAxis, double ax, double ay, double bx, double by,
                               double cx, double cy, double dx, double dy) {
        // scale all coordinates by 2^k to integers
        int k = 0;
        for (double v : new double[]{ax, ay, bx, by, cx, cy, dx, dy}) {
            if (v != 0) k = Math.max(k, -lowestBitExponent(v));
        }
        BigInteger iax = scaled(ax, k), iay = scaled(ay, k), ibx = scaled(bx, k), iby = scaled(by, k);
        BigInteger icx = scaled(cx, k), icy = scaled(cy, k), idx = scaled(dx, k), idy = scaled(dy, k);
        BigInteger den = ibx.subtract(iax).multiply(idy.subtract(icy))
                .subtract(iby.subtract(iay).multiply(idx.subtract(icx)));
        BigInteger cd = icx.multiply(idy).subtract(idx.multiply(icy));
        BigInteger ab = iax.multiply(iby).subtract(ibx.multiply(iay));
        BigInteger num = yAxis
                ? iby.subtract(iay).multiply(cd).subtract(idy.subtract(icy).multiply(ab))
                : ibx.subtract(iax).multiply(cd).subtract(idx.subtract(icx).multiply(ab));
        // num is scaled by 2^3k, den by 2^2k
        return roundQuotient(num, den, -k);
    }

    /**
     * @return {@code num / den * 2^scale}, correctly rounded (round to nearest, ties to even)
     */
    static double roundQuotient(BigInteger num, BigInteger den, int scale) {
        if (den.signum() == 0) throw new ArithmeticException("Division by zero");
        if (num.signum() == 0) return 0;
        boolean negative = num.signum() != den.signum();
        BigInteger n = num.abs();
        BigInteger d = den.abs();
        // e = floor(log2(n / d))
        int e = n.bitLength() - d.bitLength();
        if ((e >= 0 ? n.compareTo(d.shiftLeft(e)) : n.shiftLeft(-e).compareTo(d)) < 0) e--;
        // the exponent of the unit in the last place of the result (at least that of the subnormals)
        int ulpExponent = Math.max(e + scale - 52, -1074);
        int shift = scale - ulpExponent;
        BigInteger[] qr = shift >= 0
                ? n.shiftLeft(shift).divideAndRemainder(d)
                : n.divideAndRemainder(d.shiftLeft(-shift));
        long significand = qr[0].longValueExact();
        int c = qr[1].shiftLeft(1).compareTo(shift >= 0 ? d : d.shiftLeft(-shift));
        if (c > 0 || (c == 0 && (significand & 1) == 1)) significand++;
        double r = Math.scalb((double) significand, ulpExponent); // exact: significand <= 2^53
        return negative ? -r : r;
    }

    // the exponent of the least significant non-zero bit of a finite, non-zero double
    private static int lowestBitExponent(double v) {
        long bits = Double.doubleToRawLongBits(v);
        int biased = (int) ((bits >>> 52) & 0x7ff);
        long mantissa = bits & 0xfffffffffffffL;
        long significand = biased == 0 ? mantissa : mantissa | (1L << 52);
        int exponent = biased == 0 ? -1074 : biased - 1075;
        return exponent + Long.numberOfTrailingZeros(significand);
    }

    // v * 2^k as an integer (k must be large enough)
    private static BigInteger scaled(double v, int k) {
        if (v == 0) return BigInteger.ZERO;
        long bits = Double.doubleToRawLongBits(v);
        int biased = (int) ((bits >>> 52) & 0x7ff);
        long mantissa = bits & 0xfffffffffffffL;
        long significand = biased == 0 ? mantissa : mantissa | (1L << 52);
        int exponent = biased == 0 ? -1074 : biased - 1075;
        BigInteger r = BigInteger.valueOf(significand).shiftLeft(exponent + k);
        return v < 0 ? r.negate() : r;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    // The endpoint with the given index, as a position of the type of a and b. Endpoints of (c, d) take the X
    // and Y coordinates from the endpoint, other coordinates are interpolated along (a, b).
    private static <P extends Position> P endpoint(int index, P a, P b, Position c, Position d) {
        switch (index) {
            case 0:
                return a;
            case 1:
                return b;
            case 2:
                return positionOn(a, b, x(c), y(c));
            default:
                return positionOn(a, b, x(d), y(d));
        }
    }

    // A position with the given X and Y coordinates (which lie on, or close to, segment (a, b)), and other
    // coordinates linearly interpolated along (a, b)
    @SuppressWarnings("unchecked")
    private static <P extends Position> P positionOn(P a, P b, double x, double y) {
        int dim = a.getCoordinateDimension();
        double[] coordinates = new double[dim];
        coordinates[0] = x;
        coordinates[1] = y;
        if (dim > 2) {
            double t = parameterOn(a, b, x, y);
            for (int i = 2; i < dim; i++) {
                double v0 = a.getCoordinate(i);
                double v1 = b.getCoordinate(i);
                coordinates[i] = v0 == v1 ? v0 : v0 + t * (v1 - v0);
            }
        }
        return Positions.mkPosition((Class<P>) a.getClass(), coordinates);
    }

    // the parameter (in [0, 1]) of the point (x, y) along (a, b), measured along the dominant axis
    private static double parameterOn(Position a, Position b, double x, double y) {
        double dxab = x(b) - x(a);
        double dyab = y(b) - y(a);
        double t;
        if (Math.abs(dxab) >= Math.abs(dyab)) {
            t = dxab == 0 ? 0 : (x - x(a)) / dxab;
        } else {
            t = (y - y(a)) / dyab;
        }
        return t > 0 ? Math.min(t, 1) : 0;
    }

    // Knuth's TwoSum: (a + b) - s computed without rounding error (barring overflow)
    private static double twoSumError(double a, double b, double s) {
        double bv = s - a;
        double av = s - bv;
        return (a - av) + (b - bv);
    }

    private static double x(Position p) {
        return p.getCoordinate(0);
    }

    private static double y(Position p) {
        return p.getCoordinate(1);
    }
}
