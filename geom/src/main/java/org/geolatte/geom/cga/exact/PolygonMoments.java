package org.geolatte.geom.cga.exact;

import java.util.ArrayList;
import java.util.List;

/**
 * The correctly rounded area and centroid of a set of polygon rings.
 *
 * <p>Rings are added one by one, as shells or holes. A shell adds the area it encloses, and a hole subtracts it,
 * whatever the orientation of the ring: the area is {@code sum |A(shell)| - sum |A(hole)|}, with A the signed
 * (shoelace) area of a ring. For valid polygons, this is the area of the polygons. A ring whose signed area is
 * exactly zero contributes nothing. The centroid is the centroid of the area, weighted in the same way:</p>
 * <pre>
 *   A  = 1/2 sum f_r sum_i c_i,                    c_i = x_i y_(i+1) - x_(i+1) y_i
 *   Cx = sum f_r sum_i (x_i + x_(i+1)) c_i / (6 A),   and similarly for Cy
 * </pre>
 * <p>with {@code f_r = +1} or {@code -1} the sign with which ring r is counted. Both are rational in the
 * coordinates, so they can be rounded correctly (round to nearest, ties to even), which is what this class does.
 * The result is the double nearest to the exact value, for any input; it does not depend on the order of the rings
 * or of their positions, nor on the orientation of the rings.</p>
 *
 * <p>The computation is staged:</p>
 * <ol>
 *     <li>each ring is accumulated in double-word arithmetic with a rigorous error bound, relative to its first
 *     position when all coordinate differences with that position are exactly representable (which is the case
 *     for typical projected data, where it removes the cancellation due to large coordinates). The totals are
 *     enclosed in intervals of width about 2^-100 times their magnitude; when no rounding boundary lies in the
 *     interval, the rounding is decided. This is the case for practically all inputs;</li>
 *     <li>otherwise the sums are computed exactly with a {@link Summation} and rounded with {@code BigInteger}
 *     arithmetic.</li>
 * </ol>
 * <p>The sign with which a ring is counted needs the exact sign of its area. The first stage almost always decides
 * it; otherwise {@link Predicates#ringOrientation(double[], double[], int)} does.</p>
 */
public final class PolygonMoments {

    private static final double U = 0x1p-53;
    // relative error bound of double-word multiplication and division (see Constructions)
    private static final double DW_REL_ERROR = 32 * U * U;
    // the first stage is used only for coordinate differences of zero or with a magnitude in this range, so that
    // all products are free of underflow and overflow
    private static final double MIN_FILTER = 0x1p-200;
    private static final double MAX_FILTER = 0x1p200;
    // and only for results with a magnitude in this range
    private static final double MIN_RESULT = 0x1p-900;
    private static final double MAX_RESULT = 0x1p900;
    private static final double MAX_REL_ERROR = 0x1p-20;

    private final boolean withCentroid;

    // the totals of the first stage: the doubled area 2A (S), and the centroid numerators Nx, Ny and denominator 6A
    private final Accumulator s = new Accumulator();
    private final Accumulator d = new Accumulator();
    private final Accumulator nx = new Accumulator();
    private final Accumulator ny = new Accumulator();
    private boolean filterFailed = false;

    // the rings with a non-zero area, for the exact stage
    private final List<Ring> rings = new ArrayList<>();

    // the results, computed when first asked
    private int areaSign = 2;
    private double area = Double.NaN;
    private double[] centroid;

    private static final class Ring {
        final double[] xs, ys;
        final int n, sign;

        Ring(double[] xs, double[] ys, int n, int sign) {
            this.xs = xs;
            this.ys = ys;
            this.n = n;
            this.sign = sign;
        }
    }

    /**
     * @param withCentroid true if the centroid is needed. Otherwise only the area is computed, which is faster.
     */
    public PolygonMoments(boolean withCentroid) {
        this.withCentroid = withCentroid;
    }

    /**
     * Adds a ring. The ring is closed implicitly: a last position equal to the first one is allowed, but not needed.
     * The arrays are not copied; they must not be changed while this object is used.
     *
     * @param xs   the X coordinates
     * @param ys   the Y coordinates
     * @param n    the number of positions (the first n of the arrays)
     * @param hole false if the area of the ring is added (a shell), true if it is subtracted (a hole)
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public void addRing(double[] xs, double[] ys, int n, boolean hole) {
        if (n < 3) return;
        areaSign = 2;
        area = Double.NaN;
        centroid = null;

        Accumulator rs = new Accumulator();
        Accumulator rx = withCentroid ? new Accumulator() : null;
        Accumulator ry = withCentroid ? new Accumulator() : null;
        // relative to the first position, or else to the origin
        double ox = xs[0], oy = ys[0];
        boolean filtered = inRange(ox) && inRange(oy) && accumulate(xs, ys, n, ox, oy, rs, rx, ry);
        if (!filtered) {
            ox = 0;
            oy = 0;
            filtered = accumulate(xs, ys, n, 0, 0, rs, rx, ry);
        }
        if (!filtered) {
            for (int i = 0; i < n; i++) {
                if (!Double.isFinite(xs[i]) || !Double.isFinite(ys[i])) {
                    throw new IllegalArgumentException("Coordinates must be finite");
                }
            }
        }

        int sign = filtered ? rs.sign() : 0;
        if (!filtered || sign == 2) sign = Predicates.ringOrientation(xs, ys, n);
        int f = hole ? -sign : sign;
        if (f == 0) return;
        rings.add(new Ring(xs, ys, n, f));
        if (!filtered) {
            filterFailed = true;
            return;
        }
        boolean ok = s.addScaled(f, 1, rs);
        if (withCentroid) {
            ok &= d.addScaled(f, 3, rs);
            ok &= nx.addScaled(f, 1, rx) && nx.addScaled(f * ox, 3, rs);
            ok &= ny.addScaled(f, 1, ry) && ny.addScaled(f * oy, 3, rs);
        }
        if (!ok) filterFailed = true;
    }

    /**
     * @return the exact sign of the area: -1, 0 or 1
     */
    public int areaSignum() {
        if (areaSign == 2) {
            areaSign = filterFailed ? 2 : s.sign();
            if (areaSign == 2) areaSign = exactS().signum();
        }
        return areaSign;
    }

    /**
     * @return the area, correctly rounded. It is negative if the holes enclose more area than the shells.
     */
    public double area() {
        if (Double.isNaN(area)) {
            double a = filterFailed ? Double.NaN : roundHalf(s);
            area = Double.isNaN(a) ? exactS().round(-1) : a;
        }
        return area;
    }

    /**
     * @return the X and Y coordinate of the centroid, each correctly rounded
     * @throws IllegalStateException    if this object was created without the centroid
     * @throws IllegalArgumentException if the area is zero
     */
    public double[] centroid() {
        if (!withCentroid) throw new IllegalStateException("Created without the centroid");
        if (areaSignum() == 0) throw new IllegalArgumentException("The area is zero");
        if (centroid == null) {
            double cx = filterFailed ? Double.NaN : roundQuotient(nx, d);
            double cy = filterFailed ? Double.NaN : roundQuotient(ny, d);
            if (Double.isNaN(cx) || Double.isNaN(cy)) {
                Summation[] exact = exactCentroid();
                if (Double.isNaN(cx)) cx = Summation.roundQuotient(exact[0], exact[2]);
                if (Double.isNaN(cy)) cy = Summation.roundQuotient(exact[1], exact[2]);
            }
            centroid = new double[]{cx + 0.0, cy + 0.0};
        }
        return centroid.clone();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Stage 1
    // ---------------------------------------------------------------------------------------------------------

    private static boolean inRange(double v) {
        double a = Math.abs(v);
        return a == 0 || (a >= MIN_FILTER && a <= MAX_FILTER);
    }

    /**
     * Accumulates S = sum c_i, and (if rx is not null) Mx = sum (x_i + x_(i+1)) c_i and My, for the coordinates
     * relative to (ox, oy).
     *
     * <p>Per vertex, the difference of the two leading products is formed exactly (TwoSum) before it is added to the
     * running sum, so that the running sum stays of the order of the partial results (it does not grow with the
     * number of vertices), and with it the rounding errors. The loops have no data-dependent branches.</p>
     *
     * @return false if a coordinate difference is not exact or not in the filter range (then the accumulators are
     * not set)
     */
    private static boolean accumulate(double[] xs, double[] ys, int n, double ox, double oy,
                                      Accumulator rs, Accumulator rx, Accumulator ry) {
        double x0 = xs[0] - ox, y0 = ys[0] - oy;
        boolean ok = inRange(x0) & inRange(y0);
        // the running sum, the sum of its TwoSum errors, the sum of the other small terms, and the magnitudes
        double ss = 0, se = 0, sl = 0, sm = 0;
        for (int i = 0; i < n; i++) {
            int j = i + 1 == n ? 0 : i + 1;
            double xj = xs[j], yj = ys[j];
            double x1 = xj - ox, y1 = yj - oy;
            ok &= (twoSumError(xj, -ox, x1) == 0) & (twoSumError(yj, -oy, y1) == 0) & inRange(x1) & inRange(y1);
            // c = x0 y1 - x1 y0 = (p1 - p2) + (e1 - e2) = dh + dl + (e1 - e2)
            double p1 = x0 * y1, e1 = Math.fma(x0, y1, -p1);
            double p2 = x1 * y0, e2 = Math.fma(x1, y0, -p2);
            double dh = p1 - p2, dl = twoSumError(p1, -p2, dh);
            double t = ss + dh;
            double e = twoSumError(ss, dh, t);
            ss = t;
            se += e;
            sl += dl + (e1 - e2);
            sm += Math.abs(e) + (Math.abs(dl) + Math.abs(e1) + Math.abs(e2));
            x0 = x1;
            y0 = y1;
        }
        if (!ok) return false;
        rs.set(ss, se + sl, sm, 2 * n);
        if (rx != null) {
            moment(xs, ys, n, ox, oy, 1, rx);
            // My = sum (y_i + y_(i+1)) c_i, and swapping X and Y negates c_i
            moment(ys, xs, n, oy, ox, -1, ry);
        }
        return true;
    }

    /**
     * Accumulates sign * Mx, Mx = sum (x_i + x_(i+1)) c_i, for coordinates relative to (ox, oy) whose differences are
     * exact and in the filter range.
     *
     * <p>(x0 + x1) c = (hh + hl) (p1 - p2 + cl), with x0 + x1 = hh + hl exactly and cl the rest of c. The products
     * p1 hh and p2 hh are formed exactly; the other terms are small.</p>
     */
    private static void moment(double[] xs, double[] ys, int n, double ox, double oy, int sign, Accumulator out) {
        double x0 = xs[0] - ox, y0 = ys[0] - oy;
        double ms = 0, me = 0, ml = 0, mm = 0;
        for (int i = 0; i < n; i++) {
            int j = i + 1 == n ? 0 : i + 1;
            double x1 = xs[j] - ox, y1 = ys[j] - oy;
            double p1 = x0 * y1, e1 = Math.fma(x0, y1, -p1);
            double p2 = x1 * y0, e2 = Math.fma(x1, y0, -p2);
            double dh = p1 - p2, dl = twoSumError(p1, -p2, dh);
            double ce = e1 - e2, absCe = Math.abs(e1) + Math.abs(e2);
            double cl = dl + ce, absC = Math.abs(dh) + Math.abs(dl) + absCe;

            double hh = x0 + x1, hl = twoSumError(x0, x1, hh);
            double q1 = p1 * hh, f1 = Math.fma(p1, hh, -q1);
            double q2 = p2 * hh, f2 = Math.fma(p2, hh, -q2);
            double gh = q1 - q2, gl = twoSumError(q1, -q2, gh);
            double t = ms + gh;
            double e = twoSumError(ms, gh, t);
            ms = t;
            me += e;
            ml += (gl + (f1 - f2)) + (ce * hh + (dh + cl) * hl);
            mm += Math.abs(e) + (Math.abs(gl) + Math.abs(f1) + Math.abs(f2))
                    + (absCe * Math.abs(hh) + absC * Math.abs(hl));
            x0 = x1;
            y0 = y1;
        }
        out.set(sign * ms, sign * (me + ml), mm, 2 * n);
    }

    // the correctly rounded value of S / 2, or NaN if not decided
    private static double roundHalf(Accumulator acc) {
        double h = acc.high(), l = acc.low(), b = acc.bound();
        if (b == 0 && h == 0) return 0;
        if (!(Math.abs(h) >= MIN_RESULT && Math.abs(h) <= MAX_RESULT)) return Double.NaN;
        double q = Constructions.roundFromEnclosure(h, l, b);
        return q / 2;   // exact, also for NaN
    }

    // the correctly rounded value of num / den, or NaN if not decided
    private static double roundQuotient(Accumulator num, Accumulator den) {
        double nh = num.high(), nl = num.low(), nb = num.bound();
        double dh = den.high(), dl = den.low(), db = den.bound();
        if (nh == 0 && nb == 0) return 0;
        if (!(Math.abs(nh) >= MIN_RESULT && Math.abs(nh) <= MAX_RESULT)
                || !(Math.abs(dh) >= MIN_RESULT && Math.abs(dh) <= MAX_RESULT)
                || !(nb <= MAX_REL_ERROR * Math.abs(nh)) || !(db <= MAX_REL_ERROR * Math.abs(dh))) {
            return Double.NaN;
        }
        double rel = nb / Math.abs(nh) + db / Math.abs(dh) + DW_REL_ERROR;
        double[] out = new double[3];
        if (!Constructions.encloseSumOfQuotient(0, nh, nl, dh, dl, rel, out, 0)) return Double.NaN;
        return Constructions.roundFromEnclosure(out[0], out[1], out[2]);
    }

    /**
     * Sums values in double-word precision with a rigorous error bound.
     *
     * <p>Main terms are summed exactly with TwoSum into s; their rounding errors and the small terms are summed in
     * floating point into err. Each small term is given with a bound on its magnitude (and on the magnitude of the
     * exact value it approximates); its computation must have an error of at most 8 u times that bound. Then the
     * exact value lies within {@link #bound()} of {@code s + err}.</p>
     */
    private static final class Accumulator {
        double s, err, abs, extra;
        int k;

        /**
         * Sets the value to s + err, where err is a floating point sum of k small values whose magnitudes sum to at
         * most abs
         */
        void set(double s, double err, double abs, int k) {
            this.s = s;
            this.err = err;
            this.abs = abs;
            this.k = k + 2;
            extra = 0;
        }

        void main(double p) {
            double t = s + p;
            double e = twoSumError(s, p, t);
            s = t;
            if (e != 0) small(e, Math.abs(e));
        }

        void small(double v, double magnitude) {
            err += v;
            abs += magnitude;
            k++;
        }

        // adds a * b exactly
        void product(double a, double b) {
            double p = a * b;
            main(p);
            double e = Math.fma(a, b, -p);
            if (e != 0) small(e, Math.abs(e));
        }

        /**
         * Adds c1 * c2 * (value of r), for c2 = 1 or 3
         *
         * @return false if a product might underflow or overflow
         */
        boolean addScaled(double c1, double c2, Accumulator r) {
            if (c1 == 0) return true;
            if (r.s != 0) {
                double p = c1 * r.s;
                double a = Math.abs(p);
                if (!(a >= MIN_RESULT && a <= MAX_RESULT)) return false;
                double e = Math.fma(c1, r.s, -p);
                product(p, c2);
                if (e != 0) small(e * c2, Math.abs(e * c2));
            }
            if (r.err != 0) {
                double v = c1 * c2 * r.err;
                small(v, Math.abs(v));
            }
            extra += Math.abs(c1 * c2) * r.bound();
            return true;
        }

        double high() {
            return s + err;
        }

        double low() {
            return twoSumError(s, err, s + err);
        }

        // gamma(k) for the floating point sum of the k small values, plus 8 u for the errors of their computation;
        // the margins cover the rounding of the bound itself
        double bound() {
            return ((k + 8) * 0x1p-51 * abs + extra) * (1 + 0x1p-40);
        }

        // the sign of the value, or 2 if undecided
        int sign() {
            double h = high(), b = bound();
            if (h == 0 && b == 0) return 0;
            return Math.abs(h) > 2 * b ? (h > 0 ? 1 : -1) : 2;
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Stage 2
    // ---------------------------------------------------------------------------------------------------------

    // the exact doubled area S
    private Summation exactS() {
        Summation sum = new Summation();
        for (Ring r : rings) {
            for (int i = 0; i < r.n; i++) {
                int j = i + 1 == r.n ? 0 : i + 1;
                sum.addProduct(r.sign * r.xs[i], r.ys[j]);
                sum.addProduct(-r.sign * r.xs[j], r.ys[i]);
            }
        }
        return sum;
    }

    // the exact numerators Nx, Ny and denominator 3 S of the centroid
    private Summation[] exactCentroid() {
        Summation sx = new Summation(), sy = new Summation(), den = new Summation();
        for (Ring r : rings) {
            for (int i = 0; i < r.n; i++) {
                int j = i + 1 == r.n ? 0 : i + 1;
                double x0 = r.sign * r.xs[i], y0 = r.ys[i], x1 = r.sign * r.xs[j], y1 = r.ys[j];
                // f c = x0 y1 - x1 y0, with the sign f in x0 and x1
                den.addProduct(x0, y1, 3).addProduct(-x1, y0, 3);
                // f (x_i + x_j) c and f (y_i + y_j) c, with the unsigned coordinates as the first factor
                double ux0 = r.xs[i], ux1 = r.xs[j];
                sx.addProduct(ux0, x0, y1).addProduct(-ux0, x1, y0).addProduct(ux1, x0, y1).addProduct(-ux1, x1, y0);
                sy.addProduct(y0, x0, y1).addProduct(-y0, x1, y0).addProduct(y1, x0, y1).addProduct(-y1, x1, y0);
            }
        }
        return new Summation[]{sx, sy, den};
    }

    // Knuth's TwoSum: (a + b) - s computed without rounding error
    private static double twoSumError(double a, double b, double s) {
        double bv = s - a;
        double av = s - bv;
        return (a - av) + (b - bv);
    }
}
