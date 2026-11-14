package org.geolatte.geom.cga.exact;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.SegmentIntersection;

import java.math.BigDecimal;

/**
 * Exact geometric predicates on double precision coordinates.
 *
 * <p>All predicates return the exact result for the coordinates as given (i.e. as doubles), so that
 * decisions are never falsified by rounding errors. They follow the scheme recommended by Ratschek &amp; Rokne
 * (<em>Geometric Computations with Interval and New Robust Methods</em>, Sec. 4.6 and 8.3), preceded by a
 * cheaper floating point filter for the most frequently used predicates:</p>
 * <ol>
 *     <li>for {@code orientation} and {@code ringOrientation}: evaluate the expression in plain floating point,
 *     together with a bound on its rounding error (following Shewchuk, "Adaptive Precision Floating-Point
 *     Arithmetic and Fast Robust Geometric Predicates", 1997). If the computed value is further from zero than
 *     the error bound, its sign is the exact sign. This decides practically all cases at almost the cost of a
 *     plain floating point evaluation;</li>
 *     <li>evaluate a numerically stable form of the expression in machine interval arithmetic ({@link Interval}).
 *     If the resulting interval does not contain zero (or is {@code [0, 0]}), its sign is the exact sign;</li>
 *     <li>otherwise, expand the expression into a sum of products, write these exactly as a list of doubles
 *     ({@link ExactSum}), and determine the exact sign of that sum with {@link Essa};</li>
 *     <li>for coordinates of extreme magnitude, where step 3 can not be performed exactly due to underflow or
 *     overflow, fall back to {@link BigDecimal} arithmetic.</li>
 * </ol>
 *
 * <p>Only the X and Y coordinates (coordinate indices 0 and 1) of {@code Position}s are used. All predicates
 * throw an {@code IllegalArgumentException} when a used coordinate is NaN or infinite.</p>
 */
public final class Predicates {

    /**
     * Returned by the filter stages if the filter could not decide
     */
    static final int UNDECIDED = Interval.UNDECIDED;

    // Shewchuk's error bound for orientation: (3 + 16 eps) eps, with eps = 2^-53
    private static final double CCW_ERRBOUND = (3.0 + 16.0 * 0x1p-53) * 0x1p-53;

    // Shewchuk's error bound for in-circle: (10 + 96 eps) eps
    private static final double ICC_ERRBOUND = (10.0 + 96.0 * 0x1p-53) * 0x1p-53;

    // The in-circle error bound is only used if all non-zero coordinate differences lie in this range: then no
    // product of up to four differences (nor a difference of such products, multiplied by a lift) underflows or
    // overflows.
    private static final double ICC_MIN_DIFFERENCE = 0x1p-200;
    private static final double ICC_MAX_DIFFERENCE = 0x1p200;

    // Shewchuk's error bounds assume that no underflow occurs. The absolute rounding error of a product that
    // underflows is at most 2^-1075; this margin covers that generously.
    private static final double UNDERFLOW_MARGIN = 0x1p-1060;

    private Predicates() {
    }

    // ---------------------------------------------------------------------------------------------------------
    // Orientation
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Returns the orientation of the triangle (a, b, c).
     *
     * <p>This is the sign of the determinant</p>
     * <pre>
     *     | ax ay 1 |
     *     | bx by 1 |
     *     | cx cy 1 |
     * </pre>
     *
     * @return 1 if (a, b, c) is counterclockwise (c lies to the left of the directed line a-&gt;b), -1 if
     * clockwise (c lies to the right) and 0 if the positions are collinear.
     */
    public static int orientation(double ax, double ay, double bx, double by, double cx, double cy) {
        int s = orientationErrorBound(ax, ay, bx, by, cx, cy);
        if (s != UNDECIDED) return s;
        return orientationExact(ax, ay, bx, by, cx, cy);
    }

    /**
     * Floating point evaluation with error bound. Returns the exact sign, or UNDECIDED.
     *
     * <p>If any coordinate is NaN or infinite, the determinant is NaN or infinite (a non-finite difference is a
     * factor in one of the products), and UNDECIDED is returned. So a decided result implies finite input.</p>
     */
    static int orientationErrorBound(double ax, double ay, double bx, double by, double cx, double cy) {
        double l1 = bx - ax, l2 = cy - ay;
        double r1 = cx - ax, r2 = by - ay;
        double detLeft = l1 * l2;
        double detRight = r1 * r2;
        double det = detLeft - detRight;
        double errBound = CCW_ERRBOUND * (Math.abs(detLeft) + Math.abs(detRight)) + UNDERFLOW_MARGIN;
        if (det > errBound) return 1;
        if (det < -errBound) return -1;
        // Comparisons with NaN are false, and errBound is infinite if det is, so we get here for non-finite det.
        if (Double.isFinite(det)) {
            // The signs of the differences are exact, so the sign of each product is known exactly. This
            // decides the cases where a product is zero, e.g. axis-aligned collinear positions.
            int signLeft = sign(l1) * sign(l2);
            int signRight = sign(r1) * sign(r2);
            if (signLeft != signRight) return Integer.signum(signLeft - signRight);
            if (signLeft == 0) return 0;
            // Coinciding positions (e.g. the shared endpoint of adjacent segments): the determinant is exactly
            // zero, which no error bound can decide.
            if ((cx == ax && cy == ay) || (cx == bx && cy == by) || (ax == bx && ay == by)) return 0;
        }
        return UNDECIDED;
    }

    private static int orientationExact(double ax, double ay, double bx, double by, double cx, double cy) {
        checkFinite(ax, ay, bx, by);
        checkFinite(cx, cy);
        int s = orientationFilter(ax, ay, bx, by, cx, cy);
        if (s != UNDECIDED) return s;
        ExactSum sum = orientationTerms(ax, ay, bx, by, cx, cy);
        if (sum.isExact()) return sum.signum();
        return orientationBig(ax, ay, bx, by, cx, cy);
    }

    /**
     * Returns the orientation of the triangle (a, b, c)
     *
     * @see #orientation(double, double, double, double, double, double)
     */
    public static int orientation(Position a, Position b, Position c) {
        return orientation(x(a), y(a), x(b), y(b), x(c), y(c));
    }

    static int orientationFilter(double ax, double ay, double bx, double by, double cx, double cy) {
        return orientationInterval(ax, ay, bx, by, cx, cy).signum();
    }

    private static Interval orientationInterval(double ax, double ay, double bx, double by, double cx, double cy) {
        Interval l = Interval.difference(bx, ax).mul(Interval.difference(cy, ay));
        Interval r = Interval.difference(cx, ax).mul(Interval.difference(by, ay));
        return l.sub(r);
    }

    // ax*by + bx*cy + cx*ay - ax*cy - bx*ay - cx*by
    static ExactSum orientationTerms(double ax, double ay, double bx, double by, double cx, double cy) {
        return addOrientationTerms(new ExactSum(12), 1, ax, ay, bx, by, cx, cy);
    }

    private static ExactSum addOrientationTerms(ExactSum sum, int sign, double ax, double ay, double bx, double by, double cx, double cy) {
        return sum.addProduct(sign * ax, by)
                .addProduct(sign * bx, cy)
                .addProduct(sign * cx, ay)
                .addProduct(-sign * ax, cy)
                .addProduct(-sign * bx, ay)
                .addProduct(-sign * cx, by);
    }

    static int orientationBig(double ax, double ay, double bx, double by, double cx, double cy) {
        return orientationBigValue(ax, ay, bx, by, cx, cy).signum();
    }

    private static BigDecimal orientationBigValue(double ax, double ay, double bx, double by, double cx, double cy) {
        BigDecimal ax_ = big(ax), ay_ = big(ay);
        return big(bx).subtract(ax_).multiply(big(cy).subtract(ay_))
                .subtract(big(cx).subtract(ax_).multiply(big(by).subtract(ay_)));
    }

    // ---------------------------------------------------------------------------------------------------------
    // In-circle
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Determines the position of d relative to the circle through a, b and c.
     *
     * <p>This is the sign of the determinant</p>
     * <pre>
     *     | ax ay ax²+ay² 1 |
     *     | bx by bx²+by² 1 |
     *     | cx cy cx²+cy² 1 |
     *     | dx dy dx²+dy² 1 |
     * </pre>
     *
     * <p>Note that this convention differs in sign from the one used in the book (Sec. 8.3.5), which assumes
     * a, b, c to be in clockwise order.</p>
     *
     * @return if a, b, c are in counterclockwise order: 1 if d lies inside the circle, -1 if outside, 0 if on the
     * circle. The signs are reversed if a, b, c are in clockwise order. If a, b, c are collinear, the result is the
     * orientation of the line through a, b and c with respect to d (0 if d is on the line too).
     */
    public static int inCircle(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        int fast = inCircleErrorBound(ax, ay, bx, by, cx, cy, dx, dy);
        if (fast != UNDECIDED) return fast;
        int adaptive = inCircleExpansion(ax, ay, bx, by, cx, cy, dx, dy);
        if (adaptive != UNDECIDED) return adaptive;
        checkFinite(ax, ay, bx, by);
        checkFinite(cx, cy, dx, dy);
        int s = inCircleFilter(ax, ay, bx, by, cx, cy, dx, dy);
        if (s != UNDECIDED) return s;
        ExactSum sum = inCircleTerms(ax, ay, bx, by, cx, cy, dx, dy);
        if (sum.isExact()) return sum.signum();
        return inCircleBig(ax, ay, bx, by, cx, cy, dx, dy);
    }

    /**
     * Determines the position of d relative to the circle through a, b and c.
     *
     * @see #inCircle(double, double, double, double, double, double, double, double)
     */
    public static int inCircle(Position a, Position b, Position c, Position d) {
        return inCircle(x(a), y(a), x(b), y(b), x(c), y(c), x(d), y(d));
    }

    /**
     * Floating point evaluation with Shewchuk's error bound. Returns the exact sign, or UNDECIDED (also for
     * non-finite input, and when a coordinate difference is outside the range in which the bound is valid).
     */
    static int inCircleErrorBound(double ax, double ay, double bx, double by, double cx, double cy,
                                  double dx, double dy) {
        double adx = ax - dx, ady = ay - dy, bdx = bx - dx, bdy = by - dy, cdx = cx - dx, cdy = cy - dy;
        if (!(inIccRange(adx) && inIccRange(ady) && inIccRange(bdx) && inIccRange(bdy)
                && inIccRange(cdx) && inIccRange(cdy))) {
            return UNDECIDED;
        }
        double bdxcdy = bdx * cdy, cdxbdy = cdx * bdy;
        double cdxady = cdx * ady, adxcdy = adx * cdy;
        double adxbdy = adx * bdy, bdxady = bdx * ady;
        double alift = adx * adx + ady * ady;
        double blift = bdx * bdx + bdy * bdy;
        double clift = cdx * cdx + cdy * cdy;
        double det = alift * (bdxcdy - cdxbdy) + blift * (cdxady - adxcdy) + clift * (adxbdy - bdxady);
        double permanent = (Math.abs(bdxcdy) + Math.abs(cdxbdy)) * alift
                + (Math.abs(cdxady) + Math.abs(adxcdy)) * blift
                + (Math.abs(adxbdy) + Math.abs(bdxady)) * clift;
        double errBound = ICC_ERRBOUND * permanent;
        if (det > errBound) return 1;
        if (det < -errBound) return -1;
        return UNDECIDED;
    }

    /**
     * Exact evaluation with floating point expansions (Shewchuk). The coordinate differences are computed exactly
     * as expansions of one or two components (for nearby points they are usually exact doubles, by Sterbenz's
     * lemma), and the determinant is evaluated from them with exact expansion arithmetic. Returns UNDECIDED for
     * non-finite input, or when a non-zero component of a difference lies outside [2^-200, 2^200].
     *
     * <p>In that range the result is exact: every non-zero component is at least 2^-252 in magnitude and has an
     * ulp of at least 2^-252, so all exact intermediate values (sums of products of at most four components)
     * are multiples of 2^-1008, and no rounding error underflows; magnitudes stay far below the overflow
     * threshold.</p>
     */
    static int inCircleExpansion(double ax, double ay, double bx, double by, double cx, double cy,
                                 double dx, double dy) {
        double[] adx = Expansion.difference(ax, dx), ady = Expansion.difference(ay, dy);
        double[] bdx = Expansion.difference(bx, dx), bdy = Expansion.difference(by, dy);
        double[] cdx = Expansion.difference(cx, dx), cdy = Expansion.difference(cy, dy);
        if (!(inIccRange(adx) && inIccRange(ady) && inIccRange(bdx) && inIccRange(bdy)
                && inIccRange(cdx) && inIccRange(cdy))) {
            return UNDECIDED;
        }
        double[] alift = Expansion.sum(Expansion.product(adx, adx), Expansion.product(ady, ady));
        double[] blift = Expansion.sum(Expansion.product(bdx, bdx), Expansion.product(bdy, bdy));
        double[] clift = Expansion.sum(Expansion.product(cdx, cdx), Expansion.product(cdy, cdy));
        double[] bc = Expansion.productDifference(bdx, cdy, cdx, bdy);
        double[] ca = Expansion.productDifference(cdx, ady, adx, cdy);
        double[] ab = Expansion.productDifference(adx, bdy, bdx, ady);
        double[] det = Expansion.sum(Expansion.sum(Expansion.product(alift, bc), Expansion.product(blift, ca)),
                Expansion.product(clift, ab));
        return Expansion.signum(det);
    }

    private static boolean inIccRange(double[] e) {
        for (double c : e) {
            if (!inIccRange(c)) return false;
        }
        return true;
    }

    // NaN and infinite differences fail the test
    private static boolean inIccRange(double d) {
        double a = Math.abs(d);
        return a == 0 || (a >= ICC_MIN_DIFFERENCE && a <= ICC_MAX_DIFFERENCE);
    }

    static int inCircleFilter(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        Interval adx = Interval.difference(ax, dx), ady = Interval.difference(ay, dy);
        Interval bdx = Interval.difference(bx, dx), bdy = Interval.difference(by, dy);
        Interval cdx = Interval.difference(cx, dx), cdy = Interval.difference(cy, dy);
        Interval alift = adx.sqr().add(ady.sqr());
        Interval blift = bdx.sqr().add(bdy.sqr());
        Interval clift = cdx.sqr().add(cdy.sqr());
        return alift.mul(bdx.mul(cdy).sub(cdx.mul(bdy)))
                .add(blift.mul(cdx.mul(ady).sub(adx.mul(cdy))))
                .add(clift.mul(adx.mul(bdy).sub(bdx.mul(ady))))
                .signum();
    }

    // Expansion of the 4x4 determinant along the lifted column:
    // la*O(b,c,d) - lb*O(a,c,d) + lc*O(a,b,d) - ld*O(a,b,c), where li = xi² + yi² and O is the orientation
    // determinant. This gives 48 products of 4 factors, i.e. at most 384 terms.
    static ExactSum inCircleTerms(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        ExactSum sum = new ExactSum(384);
        addLiftedOrientation(sum, 1, ax, ay, bx, by, cx, cy, dx, dy);
        addLiftedOrientation(sum, -1, bx, by, ax, ay, cx, cy, dx, dy);
        addLiftedOrientation(sum, 1, cx, cy, ax, ay, bx, by, dx, dy);
        addLiftedOrientation(sum, -1, dx, dy, ax, ay, bx, by, cx, cy);
        return sum;
    }

    // adds sign * (lx² + ly²) * O(p, q, r)
    private static void addLiftedOrientation(ExactSum sum, int sign,
                                             double lx, double ly,
                                             double px, double py, double qx, double qy, double rx, double ry) {
        double[][] monomials = {
                {px, qy, 1}, {qx, ry, 1}, {rx, py, 1}, {px, ry, -1}, {qx, py, -1}, {rx, qy, -1}
        };
        for (double[] m : monomials) {
            double s = sign * m[2];
            sum.addProduct(s * lx, lx, m[0], m[1]);
            sum.addProduct(s * ly, ly, m[0], m[1]);
        }
    }

    static int inCircleBig(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        BigDecimal adx = big(ax).subtract(big(dx)), ady = big(ay).subtract(big(dy));
        BigDecimal bdx = big(bx).subtract(big(dx)), bdy = big(by).subtract(big(dy));
        BigDecimal cdx = big(cx).subtract(big(dx)), cdy = big(cy).subtract(big(dy));
        BigDecimal alift = adx.multiply(adx).add(ady.multiply(ady));
        BigDecimal blift = bdx.multiply(bdx).add(bdy.multiply(bdy));
        BigDecimal clift = cdx.multiply(cdx).add(cdy.multiply(cdy));
        return alift.multiply(bdx.multiply(cdy).subtract(cdx.multiply(bdy)))
                .add(blift.multiply(cdx.multiply(ady).subtract(adx.multiply(cdy))))
                .add(clift.multiply(adx.multiply(bdy).subtract(bdx.multiply(ady))))
                .signum();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Ring orientation
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Returns the orientation of a ring, i.e. the sign of its signed area.
     *
     * <p>The ring is the closed polyline through the positions of the sequence. It does not matter whether
     * the last position repeats the first.</p>
     *
     * @param positions the positions of the ring
     * @return 1 if the signed area is positive (counterclockwise), -1 if it is negative (clockwise), and 0 if the
     * signed area is zero (e.g. when all positions are collinear, or the sequence has fewer than 3 positions).
     */
    public static int ringOrientation(PositionSequence<?> positions) {
        int s = ringOrientationErrorBound(positions);
        if (s != UNDECIDED) return s;
        int n = positions.size();
        double[] xs = new double[n];
        double[] ys = new double[n];
        double[] buf = new double[positions.getCoordinateDimension()];
        for (int i = 0; i < n; i++) {
            positions.getCoordinates(i, buf);
            xs[i] = buf[0];
            ys[i] = buf[1];
        }
        return ringOrientationExact(xs, ys, n);
    }

    /**
     * Returns the orientation of the ring through the first n positions given by the coordinate arrays.
     *
     * @see #ringOrientation(PositionSequence)
     */
    public static int ringOrientation(double[] xs, double[] ys, int n) {
        int s = ringOrientationErrorBound(xs, ys, n);
        if (s != UNDECIDED) return s;
        return ringOrientationExact(xs, ys, n);
    }

    /*
     * Floating point evaluation with error bound, using Newell's method (see Graphics Gems III, V. 5) relative to
     * the first position to reduce cancellation: the sum of (x_i - x_(i+1)) * (y'_i + y'_(i+1)), with
     * y'_i = y_i - y_0, including the closing edge. This equals twice the signed area.
     *
     * Error bound: with eps = 2^-53, each term has a relative error of at most gamma_4 with respect to
     * M_i = |x_i - x_(i+1)| * (|y'_i| + |y'_(i+1)|), and summing n terms adds at most gamma_(n-1) relative to the
     * sum of the absolute values of the terms. Hence the total error is at most gamma_(n+4) * sum(M_i), where
     * gamma_k = k eps / (1 - k eps). The bound used doubles this (to cover the rounding errors in computing the
     * bound itself), and adds a margin for underflow in the products.
     *
     * Every coordinate occurs in a factor of some term, so if any coordinate is NaN or infinite, the sum is NaN or
     * infinite, and UNDECIDED is returned.
     */
    static int ringOrientationErrorBound(PositionSequence<?> positions) {
        int n = positions.size();
        if (n < 3) return UNDECIDED;
        double[] c = new double[positions.getCoordinateDimension()];
        positions.getCoordinates(0, c);
        double x0 = c[0], y0 = c[1];
        double x1 = x0, y1 = 0;
        double d = 0, m = 0;
        for (int i = 1; i <= n; i++) {
            double x2, y2;
            if (i < n) {
                positions.getCoordinates(i, c);
                x2 = c[0];
                y2 = c[1] - y0;
            } else {
                // closing edge (zero if the last position equals the first)
                x2 = x0;
                y2 = 0;
            }
            double dx = x1 - x2;
            d += dx * (y1 + y2);
            m += Math.abs(dx) * (Math.abs(y1) + Math.abs(y2));
            x1 = x2;
            y1 = y2;
        }
        return ringErrorBoundSign(d, m, n);
    }

    static int ringOrientationErrorBound(double[] xs, double[] ys, int n) {
        if (n < 3) return UNDECIDED;
        double x0 = xs[0], y0 = ys[0];
        double x1 = x0, y1 = 0;
        double d = 0, m = 0;
        for (int i = 1; i <= n; i++) {
            double x2 = i < n ? xs[i] : x0;
            double y2 = i < n ? ys[i] - y0 : 0;
            double dx = x1 - x2;
            d += dx * (y1 + y2);
            m += Math.abs(dx) * (Math.abs(y1) + Math.abs(y2));
            x1 = x2;
            y1 = y2;
        }
        return ringErrorBoundSign(d, m, n);
    }

    private static int ringErrorBoundSign(double d, double m, int n) {
        double errBound = (n + 6) * 0x1p-52 * m + (n + 1) * UNDERFLOW_MARGIN;
        if (d > errBound) return 1;
        if (d < -errBound) return -1;
        return UNDECIDED;
    }

    private static int ringOrientationExact(double[] xs, double[] ys, int n) {
        for (int i = 0; i < n; i++) {
            checkFinite(xs[i], ys[i]);
        }
        int s = ringOrientationFilter(xs, ys, n);
        if (s != UNDECIDED) return s;
        ExactSum sum = ringOrientationTerms(xs, ys, n);
        if (sum.isExact()) return sum.signum();
        return ringOrientationBig(xs, ys, n);
    }

    // sum of cross(p_i - p_0, p_(i+1) - p_0), relative to p_0 to keep magnitudes (and interval widths) small.
    static int ringOrientationFilter(double[] xs, double[] ys, int n) {
        if (n < 3) return 0;
        Interval sum = Interval.ZERO;
        for (int i = 1; i < n - 1; i++) {
            sum = sum.add(orientationInterval(xs[0], ys[0], xs[i], ys[i], xs[i + 1], ys[i + 1]));
        }
        return sum.signum();
    }

    // Shoelace: sum of x_i * y_(i+1) - x_(i+1) * y_i, with wrap-around
    static ExactSum ringOrientationTerms(double[] xs, double[] ys, int n) {
        ExactSum sum = new ExactSum(4 * n);
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            sum.addProduct(xs[i], ys[j]);
            sum.subProduct(xs[j], ys[i]);
        }
        return sum;
    }

    static int ringOrientationBig(double[] xs, double[] ys, int n) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            sum = sum.add(big(xs[i]).multiply(big(ys[j]))).subtract(big(xs[j]).multiply(big(ys[i])));
        }
        return sum.signum();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Segment intersection
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Determines how the line segments (a, b) and (c, d) intersect.
     *
     * <p>This is the line segment intersection test of Ratschek &amp; Rokne (Sec. 5.2, Algorithm 10). The book
     * solves {@code a + t (b - a) = c + s (d - c)} with Cramer's rule, and determines the signs of the
     * determinant {@code U} and of the numerators {@code D_i} and {@code D_i - U} exactly. Each of these
     * determinants is the orientation determinant of three of the endpoints, so the test is formulated here
     * directly in terms of the exact {@link #orientation(double, double, double, double, double, double)}
     * predicate. Compared to the book, the test additionally handles all configurations of collinear segments
     * (where the book only checks whether c lies strictly between a and b), and degenerate segments (a = b or
     * c = d), which are treated as points.</p>
     *
     * <p>The intersection point itself is not computed: the result is exact, even though the intersection
     * point is in general not representable in double precision. See {@link Constructions} for computing the
     * intersection.</p>
     *
     * @return the kind of intersection
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static SegmentIntersection segmentIntersection(double ax, double ay, double bx, double by,
                                                          double cx, double cy, double dx, double dy) {
        return KINDS[classifySegments(ax, ay, bx, by, cx, cy, dx, dy) & KIND_MASK];
    }

    /**
     * Determines how the line segments (a, b) and (c, d) intersect.
     *
     * @see #segmentIntersection(double, double, double, double, double, double, double, double)
     */
    public static SegmentIntersection segmentIntersection(Position a, Position b, Position c, Position d) {
        return segmentIntersection(x(a), y(a), x(b), y(b), x(c), y(c), x(d), y(d));
    }

    // Encoding of the result of classifySegments(). Bits 0-1 hold the ordinal of the SegmentIntersection. For
    // TOUCHING, bits 2-3 hold the index of an endpoint that is the common point (0 = a, 1 = b, 2 = c, 3 = d;
    // endpoints of (a, b) take precedence). For OVERLAPPING, bits 2-3 and 4-5 hold the indices of the endpoints
    // of the overlap, ordered in the direction from a to b. For CROSSING, bit 6 is set iff orientation(c, d, a)
    // is positive (which is the sign of the cross product (b - a) x (d - c)).
    static final int KIND_MASK = 3;
    static final int CROSSING_POSITIVE = 1 << 6;
    static final int DISJOINT_CODE = SegmentIntersection.DISJOINT.ordinal();
    static final int CROSSING_CODE = SegmentIntersection.CROSSING.ordinal();
    static final int TOUCHING_CODE = SegmentIntersection.TOUCHING.ordinal();
    static final int OVERLAPPING_CODE = SegmentIntersection.OVERLAPPING.ordinal();
    private static final SegmentIntersection[] KINDS = SegmentIntersection.values();
    private static final int A = 0, B = 1, C = 2, D = 3;

    static SegmentIntersection kind(int code) {
        return KINDS[code & KIND_MASK];
    }

    // the index of the touching endpoint, or of the first endpoint of the overlap
    static int firstEndpoint(int code) {
        return (code >> 2) & 3;
    }

    // the index of the last endpoint of the overlap
    static int lastEndpoint(int code) {
        return (code >> 4) & 3;
    }

    private static int touching(int endpoint) {
        return TOUCHING_CODE | endpoint << 2;
    }

    /**
     * Classifies the intersection of the segments (a, b) and (c, d), see
     * {@link #segmentIntersection(double, double, double, double, double, double, double, double)}, and also
     * identifies the endpoints that make up the intersection (see the encoding above).
     */
    static int classifySegments(double ax, double ay, double bx, double by,
                                double cx, double cy, double dx, double dy) {
        boolean abIsPoint = ax == bx && ay == by;
        boolean cdIsPoint = cx == dx && cy == dy;
        if (abIsPoint || cdIsPoint) {
            checkFinite(ax, ay, bx, by);
            checkFinite(cx, cy, dx, dy);
            if (abIsPoint && cdIsPoint) {
                return ax == cx && ay == cy ? touching(A) : DISJOINT_CODE;
            }
            if (abIsPoint) {
                return isOnSegment(cx, cy, dx, dy, ax, ay) ? touching(A) : DISJOINT_CODE;
            }
            return isOnSegment(ax, ay, bx, by, cx, cy) ? touching(C) : DISJOINT_CODE;
        }

        int o1 = orientation(ax, ay, bx, by, cx, cy);
        int o2 = orientation(ax, ay, bx, by, dx, dy);
        if (o1 * o2 > 0) return DISJOINT_CODE; // c and d strictly on the same side of line ab
        int o3 = orientation(cx, cy, dx, dy, ax, ay);
        int o4 = orientation(cx, cy, dx, dy, bx, by);
        if (o3 * o4 > 0) return DISJOINT_CODE; // a and b strictly on the same side of line cd

        if (o1 == 0 && o2 == 0) {
            return collinearIntersection(ax, ay, bx, by, cx, cy, dx, dy);
        }
        // The lines are not parallel, and each segment meets the line of the other: the segments have exactly
        // one point in common. If an endpoint lies on the line of the other segment, it is that point.
        if (o3 == 0) return touching(A);
        if (o4 == 0) return touching(B);
        if (o1 == 0) return touching(C);
        if (o2 == 0) return touching(D);
        return o3 > 0 ? CROSSING_CODE | CROSSING_POSITIVE : CROSSING_CODE;
    }

    // p lies on the (non-degenerate) segment (a, b)?
    private static boolean isOnSegment(double ax, double ay, double bx, double by, double px, double py) {
        if (orientation(ax, ay, bx, by, px, py) != 0) return false;
        return ax != bx ? isBetween(px, ax, bx) : isBetween(py, ay, by);
    }

    private static boolean isBetween(double v, double a, double b) {
        return a < b ? a <= v && v <= b : b <= v && v <= a;
    }

    // a, b, c, d lie on one line, a != b and c != d. On a non-vertical line, the order of points along the line is
    // the order of their x-coordinates (and on a vertical line, that of their y-coordinates), so the comparison
    // reduces to comparing intervals of exact double values.
    private static int collinearIntersection(double ax, double ay, double bx, double by,
                                             double cx, double cy, double dx, double dy) {
        double a, b, c, d;
        if (ax != bx) {
            a = ax; b = bx; c = cx; d = dx;
        } else {
            a = ay; b = by; c = cy; d = dy;
        }
        double lo = Math.max(Math.min(a, b), Math.min(c, d));
        double hi = Math.min(Math.max(a, b), Math.max(c, d));
        if (lo > hi) return DISJOINT_CODE;
        int loIndex = indexOf(lo, a, b, c);
        if (lo == hi) return touching(loIndex);
        int hiIndex = indexOf(hi, a, b, c);
        return a < b
                ? OVERLAPPING_CODE | loIndex << 2 | hiIndex << 4
                : OVERLAPPING_CODE | hiIndex << 2 | loIndex << 4;
    }

    // the index of the first of the endpoints a, b, c, d with value v (which is one of them)
    private static int indexOf(double v, double a, double b, double c) {
        if (v == a) return A;
        if (v == b) return B;
        return v == c ? C : D;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Distance comparison
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Compares the (Euclidean) distances of p and q to the line segment (a, b).
     *
     * <p>This is the comparison needed for exact line simplification (Ramer-Douglas-Peucker), see Sec. 8.4.3 of
     * the book. Distances are never computed (they generally aren't machine numbers): the comparison is reduced to
     * the signs of polynomial expressions in the coordinates.</p>
     *
     * <p>The segment may be degenerate (a equal to b), in which case distances to a are compared.</p>
     *
     * @return -1, 0 or 1 if the distance of p to segment (a, b) is less than, equal to, or greater than the
     * distance of q to segment (a, b).
     */
    public static int compareDistanceToSegment(double ax, double ay, double bx, double by,
                                               double px, double py, double qx, double qy) {
        checkFinite(ax, ay, bx, by);
        checkFinite(px, py, qx, qy);
        Region rp = region(ax, ay, bx, by, px, py);
        Region rq = region(ax, ay, bx, by, qx, qy);
        DistanceComparison cmp = new DistanceComparison(ax, ay, bx, by, px, py, rp, qx, qy, rq);
        int s = cmp.filter();
        if (s != UNDECIDED) return s;
        ExactSum sum = cmp.terms();
        if (sum.isExact()) return sum.signum();
        return cmp.bigDecimal();
    }

    /**
     * Compares the (Euclidean) distances of p and q to the line segment (a, b).
     *
     * @see #compareDistanceToSegment(double, double, double, double, double, double, double, double)
     */
    public static int compareDistanceToSegment(Position a, Position b, Position p, Position q) {
        return compareDistanceToSegment(x(a), y(a), x(b), y(b), x(p), y(p), x(q), y(q));
    }

    /**
     * Compares the (Euclidean) distance of p to the line segment (a, b) with the specified distance.
     *
     * <p>This is the tolerance test of exact line simplification (Ramer-Douglas-Peucker, Sec. 8.4.3 of the book).
     * The comparison is exact for the given coordinates and distance; neither distance is rounded (in particular,
     * {@code distance * distance} is not rounded).</p>
     *
     * @param distance a finite, non-negative distance
     * @return -1, 0 or 1 if the distance of p to segment (a, b) is less than, equal to, or greater than
     * {@code distance}
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or the distance is negative, NaN or
     *                                  infinite
     */
    public static int compareDistanceToSegment(double ax, double ay, double bx, double by,
                                               double px, double py, double distance) {
        checkFinite(ax, ay, bx, by);
        checkFinite(px, py);
        if (!(distance >= 0) || distance == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Distance must be finite and non-negative: " + distance);
        }
        Region r = region(ax, ay, bx, by, px, py);
        Interval d2 = Interval.product(distance, distance);
        int s;
        if (r == Region.INTERIOR) {
            // compare O(A,B,P)² / |B - A|² with distance²
            Interval segLen2 = Interval.difference(bx, ax).sqr().add(Interval.difference(by, ay).sqr());
            s = orientationInterval(ax, ay, bx, by, px, py).sqr().sub(d2.mul(segLen2)).signum();
        } else {
            double ex = r == Region.A ? ax : bx, ey = r == Region.A ? ay : by;
            s = Interval.difference(px, ex).sqr().add(Interval.difference(py, ey).sqr()).sub(d2).signum();
        }
        if (s != UNDECIDED) return s;

        ExactSum sum;
        ExactSum dist2 = new ExactSum(2).addProduct(distance, distance);
        if (r == Region.INTERIOR) {
            ExactSum o = orientationTerms(ax, ay, bx, by, px, py);
            ExactSum segLen2 = new ExactSum(12);
            addDiffSquare(segLen2, bx, ax);
            addDiffSquare(segLen2, by, ay);
            sum = new ExactSum(340).addProduct(1, o, o).addProduct(-1, dist2, segLen2);
        } else {
            double ex = r == Region.A ? ax : bx, ey = r == Region.A ? ay : by;
            sum = new ExactSum(16);
            addDiffSquare(sum, px, ex);
            addDiffSquare(sum, py, ey);
            sum.add(-1, dist2);
        }
        if (sum.isExact()) return sum.signum();

        BigDecimal bigD2 = sqr(big(distance));
        if (r == Region.INTERIOR) {
            BigDecimal segLen2 = sqr(big(bx).subtract(big(ax))).add(sqr(big(by).subtract(big(ay))));
            return sqr(orientationBigValue(ax, ay, bx, by, px, py)).subtract(bigD2.multiply(segLen2)).signum();
        }
        double ex = r == Region.A ? ax : bx, ey = r == Region.A ? ay : by;
        return sqr(big(px).subtract(big(ex))).add(sqr(big(py).subtract(big(ey)))).subtract(bigD2).signum();
    }

    /**
     * Compares the (Euclidean) distance of p to the line segment (a, b) with the specified distance.
     *
     * @see #compareDistanceToSegment(double, double, double, double, double, double, double)
     */
    public static int compareDistanceToSegment(Position a, Position b, Position p, double distance) {
        return compareDistanceToSegment(x(a), y(a), x(b), y(b), x(p), y(p), distance);
    }

    /**
     * Returns a machine interval that contains the squared (Euclidean) distance of p to the line segment (a, b).
     *
     * <p>The interval is computed with machine interval arithmetic, and is usually narrow. It serves to decide
     * comparisons of distances cheaply; when two intervals overlap, the comparison must be decided with
     * {@link #compareDistanceToSegment(double, double, double, double, double, double, double, double)}.</p>
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static Interval squaredDistanceToSegment(double ax, double ay, double bx, double by, double px, double py) {
        checkFinite(ax, ay, bx, by);
        checkFinite(px, py);
        Interval abx = Interval.difference(bx, ax), aby = Interval.difference(by, ay);
        Interval apx = Interval.difference(px, ax), apy = Interval.difference(py, ay);
        // the region of p: on the boundary of a region both distance formulae agree, so if the region can not
        // be decided, the hull of the candidate formulae contains the distance.
        Interval dotA = apx.mul(abx).add(apy.mul(aby));             // (p - a) . (b - a)
        if (dotA.hi() <= 0) return apx.sqr().add(apy.sqr());
        Interval bpx = Interval.difference(px, bx), bpy = Interval.difference(py, by);
        Interval dotB = bpx.mul(abx).add(bpy.mul(aby)).negate();    // (p - b) . (a - b)
        if (dotB.hi() <= 0) return bpx.sqr().add(bpy.sqr());
        Interval segLen2 = abx.sqr().add(aby.sqr());
        if (!(segLen2.lo() > 0)) return Interval.of(0, Double.POSITIVE_INFINITY); // underflow
        Interval interior = abx.mul(apy).sub(apx.mul(aby)).sqr().div(segLen2);
        if (dotA.lo() > 0 && dotB.lo() > 0) return interior;
        if (dotA.lo() <= 0) interior = interior.hull(apx.sqr().add(apy.sqr()));
        if (dotB.lo() <= 0) interior = interior.hull(bpx.sqr().add(bpy.sqr()));
        return interior;
    }

    /**
     * Encloses the squared (Euclidean) distances of the points {@code (xs[i], ys[i])}, {@code from <= i < to}, to
     * the line segment (a, b): the squared distance of point i lies in {@code [lo[i], hi[i]]}.
     *
     * <p>This is a fast variant of {@link #squaredDistanceToSegment(double, double, double, double, double, double)}
     * for many points: the distances are evaluated in plain floating point arithmetic, together with rigorous
     * bounds on the rounding errors (Shewchuk's bound for expressions of the form {@code (p - q)(r - s) +- (t -
     * u)(v - w)}, cf. {@link #orientation(double, double, double, double, double, double)}). Points for which this
     * is not possible (e.g. due to underflow or overflow) are enclosed with machine interval arithmetic.</p>
     *
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static void squaredDistancesToSegment(double ax, double ay, double bx, double by,
                                                 double[] xs, double[] ys, int from, int to,
                                                 double[] lo, double[] hi) {
        double ux = bx - ax, uy = by - ay;
        if (ux == 0 && uy == 0) {
            // degenerate segment (exactly): distances to a
            for (int i = from; i < to; i++) {
                double wx = xs[i] - ax, wy = ys[i] - ay;
                double l = wx * wx, r = wy * wy;
                if (!encloseSum(l, r, lo, hi, i)) fallback(ax, ay, bx, by, xs[i], ys[i], lo, hi, i);
            }
            return;
        }
        double ul = ux * ux, ur = uy * uy;
        double len2 = ul + ur;
        double len2Err = CCW_ERRBOUND * (ul + ur) + UNDERFLOW_MARGIN;
        double len2Lo = len2 - len2Err, len2Hi = len2 + len2Err;
        boolean usable = len2Lo >= 0x1p-1000 && len2Hi < Double.POSITIVE_INFINITY;
        for (int i = from; i < to; i++) {
            double px = xs[i], py = ys[i];
            if (!usable || !encloseOne(ax, ay, bx, by, ux, uy, len2Lo, len2Hi, px, py, lo, hi, i)) {
                fallback(ax, ay, bx, by, px, py, lo, hi, i);
            }
        }
    }

    // relative widening that covers the rounding errors of the few operations that compute a bound
    private static final double WIDEN_DOWN = 1 - 0x1p-48, WIDEN_UP = 1 + 0x1p-48;

    private static boolean encloseOne(double ax, double ay, double bx, double by, double ux, double uy,
                                      double len2Lo, double len2Hi, double px, double py,
                                      double[] lo, double[] hi, int i) {
        double wx = px - ax, wy = py - ay;
        // region tests: (p - a).(b - a) <= 0 (closest to a), (b - p).(b - a) <= 0 (closest to b)
        double dal = wx * ux, dar = wy * uy;
        double dotA = dal + dar;
        double dotAErr = CCW_ERRBOUND * (Math.abs(dal) + Math.abs(dar)) + UNDERFLOW_MARGIN;
        if (dotA <= -dotAErr) {
            return encloseSum(wx * wx, wy * wy, lo, hi, i);
        }
        double vx = bx - px, vy = by - py;
        double dbl = vx * ux, dbr = vy * uy;
        double dotB = dbl + dbr;
        double dotBErr = CCW_ERRBOUND * (Math.abs(dbl) + Math.abs(dbr)) + UNDERFLOW_MARGIN;
        if (dotB <= -dotBErr) {
            return encloseSum(vx * vx, vy * vy, lo, hi, i);
        }
        // interior formula: (u x w)² / |u|²
        double cl = ux * wy, cr = uy * wx;
        double c = Math.abs(cl - cr);
        double cErr = CCW_ERRBOUND * (Math.abs(cl) + Math.abs(cr)) + UNDERFLOW_MARGIN;
        double cLo = Math.max(c - cErr, 0), cHi = c + cErr;
        // c * (c / |u|²) rather than c² / |u|²: c² may underflow although the quotient is representable. If the
        // quotient c / |u|² underflows, c < 4 (as |u|² is finite), so the absolute error is below the margin.
        double l = cLo * (cLo / len2Hi) * WIDEN_DOWN;
        double h = cHi * (cHi / len2Lo) * WIDEN_UP + UNDERFLOW_MARGIN;
        if (!(h < Double.POSITIVE_INFINITY)) return false;   // also false for NaN
        // On the boundary of a region, the formulae agree. If the region is not decided, the distance lies in
        // the hull of the enclosures of the candidate formulae.
        if (!(dotA > dotAErr)) {
            if (!encloseSum(wx * wx, wy * wy, lo, hi, i)) return false;
            l = Math.min(l, lo[i]);
            h = Math.max(h, hi[i]);
        }
        if (!(dotB > dotBErr)) {
            if (!encloseSum(vx * vx, vy * vy, lo, hi, i)) return false;
            l = Math.min(l, lo[i]);
            h = Math.max(h, hi[i]);
        }
        lo[i] = l < 0x1p-1000 ? 0 : l;
        hi[i] = h;
        return true;
    }

    // encloses the sum of two squares of differences, given the computed squares l and r
    private static boolean encloseSum(double l, double r, double[] lo, double[] hi, int i) {
        double d = l + r;
        double err = CCW_ERRBOUND * d + UNDERFLOW_MARGIN;
        double h = (d + err) * WIDEN_UP;
        if (!(h < Double.POSITIVE_INFINITY)) return false;
        double low = (d - err) * WIDEN_DOWN;
        lo[i] = low < 0x1p-1000 ? 0 : low;
        hi[i] = h;
        return true;
    }

    private static void fallback(double ax, double ay, double bx, double by, double px, double py,
                                 double[] lo, double[] hi, int i) {
        Interval d = squaredDistanceToSegment(ax, ay, bx, by, px, py);
        lo[i] = d.lo();
        hi[i] = d.hi();
    }

    /**
     * Returns a machine interval that contains the squared distance of p to the line segment (a, b).
     *
     * @see #squaredDistanceToSegment(double, double, double, double, double, double)
     */
    public static Interval squaredDistanceToSegment(Position a, Position b, Position p) {
        return squaredDistanceToSegment(x(a), y(a), x(b), y(b), x(p), y(p));
    }

    /**
     * Returns the exact sign of the dot product {@code (p - a) . (r - q)}
     */
    public static int dotSign(double ax, double ay, double px, double py, double qx, double qy, double rx, double ry) {
        checkFinite(ax, ay, px, py);
        checkFinite(qx, qy, rx, ry);
        int s = Interval.difference(px, ax).mul(Interval.difference(rx, qx))
                .add(Interval.difference(py, ay).mul(Interval.difference(ry, qy)))
                .signum();
        if (s != UNDECIDED) return s;
        ExactSum sum = new ExactSum(16);
        addDiffProduct(sum, 1, px, ax, rx, qx);
        addDiffProduct(sum, 1, py, ay, ry, qy);
        if (sum.isExact()) return sum.signum();
        return big(px).subtract(big(ax)).multiply(big(rx).subtract(big(qx)))
                .add(big(py).subtract(big(ay)).multiply(big(ry).subtract(big(qy))))
                .signum();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Directions and crossing points
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Returns the exact sign of the cross product {@code (b - a) x (d - c)}, i.e. the orientation of the direction
     * of (c, d) relative to the direction of (a, b).
     *
     * <p>With {@code c = a}, this is {@link #orientation(double, double, double, double, double, double)}.</p>
     *
     * @return 1 if the direction d - c points to the left of the direction b - a (counterclockwise, less than half
     * a turn), -1 if to the right, 0 if the directions are parallel (or one of them is zero)
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static int crossProductSign(double ax, double ay, double bx, double by,
                                       double cx, double cy, double dx, double dy) {
        int s = crossProductErrorBound(ax, ay, bx, by, cx, cy, dx, dy);
        if (s != UNDECIDED) return s;
        checkFinite(ax, ay, bx, by);
        checkFinite(cx, cy, dx, dy);
        s = crossProductFilter(ax, ay, bx, by, cx, cy, dx, dy);
        if (s != UNDECIDED) return s;
        ExactSum sum = crossProductTerms(ax, ay, bx, by, cx, cy, dx, dy);
        if (sum.isExact()) return sum.signum();
        return crossProductBig(ax, ay, bx, by, cx, cy, dx, dy);
    }

    /**
     * Floating point evaluation with error bound. The expression has the same form as the orientation
     * determinant (the difference of two products of two coordinate differences), so Shewchuk's bound applies.
     */
    static int crossProductErrorBound(double ax, double ay, double bx, double by,
                                      double cx, double cy, double dx, double dy) {
        double l1 = bx - ax, l2 = dy - cy;
        double r1 = by - ay, r2 = dx - cx;
        double detLeft = l1 * l2;
        double detRight = r1 * r2;
        double det = detLeft - detRight;
        double errBound = CCW_ERRBOUND * (Math.abs(detLeft) + Math.abs(detRight)) + UNDERFLOW_MARGIN;
        if (det > errBound) return 1;
        if (det < -errBound) return -1;
        if (Double.isFinite(det)) {
            int signLeft = sign(l1) * sign(l2);
            int signRight = sign(r1) * sign(r2);
            if (signLeft != signRight) return Integer.signum(signLeft - signRight);
            if (signLeft == 0) return 0;
        }
        return UNDECIDED;
    }

    static int crossProductFilter(double ax, double ay, double bx, double by,
                                  double cx, double cy, double dx, double dy) {
        return Interval.difference(bx, ax).mul(Interval.difference(dy, cy))
                .sub(Interval.difference(by, ay).mul(Interval.difference(dx, cx)))
                .signum();
    }

    static ExactSum crossProductTerms(double ax, double ay, double bx, double by,
                                      double cx, double cy, double dx, double dy) {
        ExactSum sum = new ExactSum(16);
        addDiffProduct(sum, 1, bx, ax, dy, cy);
        addDiffProduct(sum, -1, by, ay, dx, cx);
        return sum;
    }

    static int crossProductBig(double ax, double ay, double bx, double by,
                               double cx, double cy, double dx, double dy) {
        return big(bx).subtract(big(ax)).multiply(big(dy).subtract(big(cy)))
                .subtract(big(by).subtract(big(ay)).multiply(big(dx).subtract(big(cx))))
                .signum();
    }

    /**
     * Returns the orientation of the triangle (a, b, X), where X is the intersection point of the lines through
     * (p, q) and (r, w).
     *
     * <p>The intersection point is not computed: the result is exact, although X is in general not representable
     * in double precision. With {@code X = p + t (q - p)}, {@code t = alpha / (alpha - beta)},
     * {@code alpha = orientation(r, w, p)} and {@code beta = orientation(r, w, q)}, and since orientation is affine
     * in its last argument, the orientation of (a, b, X) is</p>
     * <pre>
     *     (alpha * orientation(a, b, q) - beta * orientation(a, b, p)) / (alpha - beta)
     * </pre>
     * <p>where {@code alpha - beta = (q - p) x (w - r)}. The numerator is a polynomial of degree 4 in the
     * coordinates.</p>
     *
     * @return 1 if X lies to the left of the directed line a-&gt;b, -1 if to the right, 0 if on the line
     * @throws IllegalArgumentException if the lines through (p, q) and (r, w) are parallel (or one of them is
     *                                  degenerate), or if a coordinate is NaN or infinite
     */
    public static int orientationOfCrossing(double ax, double ay, double bx, double by,
                                            double px, double py, double qx, double qy,
                                            double rx, double ry, double wx, double wy) {
        int denominator = crossProductSign(px, py, qx, qy, rx, ry, wx, wy);
        if (denominator == 0) {
            throw new IllegalArgumentException("The lines are parallel");
        }
        checkFinite(ax, ay, bx, by);
        // the signs of the four orientations decide most cases
        int alpha = orientation(rx, ry, wx, wy, px, py);
        int beta = orientation(rx, ry, wx, wy, qx, qy);
        int oq = orientation(ax, ay, bx, by, qx, qy);
        int op = orientation(ax, ay, bx, by, px, py);
        int left = alpha * oq, right = beta * op;
        if (left != right) return Integer.signum(left - right) * denominator;
        if (left == 0) return 0;
        return crossingNumeratorSign(ax, ay, bx, by, px, py, qx, qy, rx, ry, wx, wy) * denominator;
    }

    // the exact sign of alpha * orientation(a, b, q) - beta * orientation(a, b, p)
    static int crossingNumeratorSign(double ax, double ay, double bx, double by,
                                     double px, double py, double qx, double qy,
                                     double rx, double ry, double wx, double wy) {
        int s = crossingNumeratorFilter(ax, ay, bx, by, px, py, qx, qy, rx, ry, wx, wy);
        if (s != UNDECIDED) return s;
        ExactSum sum = crossingNumeratorTerms(ax, ay, bx, by, px, py, qx, qy, rx, ry, wx, wy);
        if (sum.isExact()) return sum.signum();
        return crossingNumeratorBig(ax, ay, bx, by, px, py, qx, qy, rx, ry, wx, wy);
    }

    static int crossingNumeratorFilter(double ax, double ay, double bx, double by,
                                       double px, double py, double qx, double qy,
                                       double rx, double ry, double wx, double wy) {
        return orientationInterval(rx, ry, wx, wy, px, py).mul(orientationInterval(ax, ay, bx, by, qx, qy))
                .sub(orientationInterval(rx, ry, wx, wy, qx, qy).mul(orientationInterval(ax, ay, bx, by, px, py)))
                .signum();
    }

    static ExactSum crossingNumeratorTerms(double ax, double ay, double bx, double by,
                                           double px, double py, double qx, double qy,
                                           double rx, double ry, double wx, double wy) {
        ExactSum sum = new ExactSum(576);
        sum.addProduct(1, orientationTerms(rx, ry, wx, wy, px, py), orientationTerms(ax, ay, bx, by, qx, qy));
        sum.addProduct(-1, orientationTerms(rx, ry, wx, wy, qx, qy), orientationTerms(ax, ay, bx, by, px, py));
        return sum;
    }

    static int crossingNumeratorBig(double ax, double ay, double bx, double by,
                                    double px, double py, double qx, double qy,
                                    double rx, double ry, double wx, double wy) {
        return orientationBigValue(rx, ry, wx, wy, px, py).multiply(orientationBigValue(ax, ay, bx, by, qx, qy))
                .subtract(orientationBigValue(rx, ry, wx, wy, qx, qy).multiply(orientationBigValue(ax, ay, bx, by, px, py)))
                .signum();
    }

    /**
     * The part of the plane, relative to segment AB, in which a point lies; this determines how its distance to
     * the segment is computed.
     */
    enum Region {
        /**
         * Closest point of the segment is A
         */
        A,
        /**
         * Closest point of the segment is B
         */
        B,
        /**
         * Closest point is in the interior of the segment
         */
        INTERIOR
    }

    static Region region(double ax, double ay, double bx, double by, double px, double py) {
        // On the boundary of a region (dot product = 0), both distance formulae agree.
        if (dotSign(ax, ay, px, py, ax, ay, bx, by) <= 0) return Region.A;
        if (dotSign(bx, by, px, py, bx, by, ax, ay) <= 0) return Region.B;
        return Region.INTERIOR;
    }

    /**
     * Compares squared distances, represented as fractions num/den:
     * <ul>
     *     <li>for a point P in region A: num = |P - A|², den = 1 (analogously for region B)</li>
     *     <li>for a point P in the interior region: num = O(A, B, P)², den = |B - A|²</li>
     * </ul>
     * Computes the sign of numP * denQ - numQ * denP.
     */
    static final class DistanceComparison {
        final double ax, ay, bx, by, px, py, qx, qy;
        final Region rp, rq;

        DistanceComparison(double ax, double ay, double bx, double by,
                           double px, double py, Region rp, double qx, double qy, Region rq) {
            this.ax = ax;
            this.ay = ay;
            this.bx = bx;
            this.by = by;
            this.px = px;
            this.py = py;
            this.qx = qx;
            this.qy = qy;
            this.rp = rp;
            this.rq = rq;
        }

        int filter() {
            if (rp == Region.INTERIOR && rq == Region.INTERIOR) {
                // denominators are equal
                return orientationInterval(ax, ay, bx, by, px, py).sqr()
                        .sub(orientationInterval(ax, ay, bx, by, qx, qy).sqr())
                        .signum();
            }
            Interval segLen2 = Interval.difference(bx, ax).sqr().add(Interval.difference(by, ay).sqr());
            Interval np = numInterval(rp, px, py), nq = numInterval(rq, qx, qy);
            Interval dp = rp == Region.INTERIOR ? segLen2 : Interval.of(1);
            Interval dq = rq == Region.INTERIOR ? segLen2 : Interval.of(1);
            return np.mul(dq).sub(nq.mul(dp)).signum();
        }

        private Interval numInterval(Region r, double x, double y) {
            switch (r) {
                case A:
                    return Interval.difference(x, ax).sqr().add(Interval.difference(y, ay).sqr());
                case B:
                    return Interval.difference(x, bx).sqr().add(Interval.difference(y, by).sqr());
                default:
                    return orientationInterval(ax, ay, bx, by, x, y).sqr();
            }
        }

        ExactSum terms() {
            if (rp == Region.INTERIOR && rq == Region.INTERIOR) {
                // sign(|O(A,B,P)| - |O(A,B,Q)|)
                ExactSum sum = new ExactSum(24);
                int sp = orientation(ax, ay, bx, by, px, py);
                int sq = orientation(ax, ay, bx, by, qx, qy);
                addOrientationTerms(sum, sp, ax, ay, bx, by, px, py);
                addOrientationTerms(sum, -sq, ax, ay, bx, by, qx, qy);
                return sum;
            }
            if (rp != Region.INTERIOR && rq != Region.INTERIOR) {
                ExactSum sum = new ExactSum(24);
                addSquaredDistance(sum, 1, rp, px, py);
                addSquaredDistance(sum, -1, rq, qx, qy);
                return sum;
            }
            if (rp == Region.INTERIOR) {
                // sign(numP - numQ * segLen²), with numP = O(A,B,P)²
                return mixed(-1, qx, qy, rq, px, py);
            }
            return mixed(1, px, py, rp, qx, qy);
        }

        // sign * (|E - X|² * |B - A|² - O(A,B,I)²), with E the endpoint-region point and I the interior point
        private ExactSum mixed(int sign, double ex, double ey, Region re, double ix, double iy) {
            ExactSum numE = new ExactSum(12);
            addSquaredDistance(numE, 1, re, ex, ey);
            ExactSum segLen2 = new ExactSum(12);
            addDiffSquare(segLen2, bx, ax);
            addDiffSquare(segLen2, by, ay);
            ExactSum o = orientationTerms(ax, ay, bx, by, ix, iy);
            return new ExactSum(600)
                    .addProduct(sign, numE, segLen2)
                    .addProduct(-sign, o, o);
        }

        private void addSquaredDistance(ExactSum sum, int sign, Region r, double x, double y) {
            double ex = r == Region.A ? ax : bx;
            double ey = r == Region.A ? ay : by;
            ExactSum d = new ExactSum(12);
            addDiffSquare(d, x, ex);
            addDiffSquare(d, y, ey);
            sum.add(sign, d);
        }

        int bigDecimal() {
            BigDecimal segLen2 = sqr(big(bx).subtract(big(ax))).add(sqr(big(by).subtract(big(ay))));
            BigDecimal np = numBig(rp, px, py), nq = numBig(rq, qx, qy);
            BigDecimal dp = rp == Region.INTERIOR ? segLen2 : BigDecimal.ONE;
            BigDecimal dq = rq == Region.INTERIOR ? segLen2 : BigDecimal.ONE;
            return np.multiply(dq).subtract(nq.multiply(dp)).signum();
        }

        private BigDecimal numBig(Region r, double x, double y) {
            switch (r) {
                case A:
                    return sqr(big(x).subtract(big(ax))).add(sqr(big(y).subtract(big(ay))));
                case B:
                    return sqr(big(x).subtract(big(bx))).add(sqr(big(y).subtract(big(by))));
                default:
                    return sqr(orientationBigValue(ax, ay, bx, by, x, y));
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    // adds sign * (p - q) * (r - t) = sign * (pr - pt - qr + qt)
    private static void addDiffProduct(ExactSum sum, int sign, double p, double q, double r, double t) {
        sum.addProduct(sign * p, r)
                .addProduct(-sign * p, t)
                .addProduct(-sign * q, r)
                .addProduct(sign * q, t);
    }

    // adds (p - q)² = p² - 2pq + q²
    private static void addDiffSquare(ExactSum sum, double p, double q) {
        sum.addProduct(p, p)
                .addProduct(-2 * p, q)  // multiplication by 2 is exact (barring overflow, which ExactSum detects)
                .addProduct(q, q);
    }

    private static int sign(double v) {
        return v > 0 ? 1 : (v < 0 ? -1 : 0);
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }

    private static BigDecimal sqr(BigDecimal v) {
        return v.multiply(v);
    }

    private static double x(Position p) {
        return p.getCoordinate(0);
    }

    private static double y(Position p) {
        return p.getCoordinate(1);
    }

    private static void checkFinite(double a, double b) {
        if (!Double.isFinite(a) || !Double.isFinite(b)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
    }

    private static void checkFinite(double a, double b, double c, double d) {
        checkFinite(a, b);
        checkFinite(c, d);
    }
}
