package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests {@link Constructions#crossingPoint(double, double, double, double, double, double, double, double)}.
 *
 * <p>The oracle is independent of the implementation: it uses the formulation
 * {@code x = ax + (bx - ax) alpha / (alpha - beta)}, with alpha and beta the orientations of a and b with respect to
 * (c, d), in exact {@code BigDecimal} arithmetic, and verifies that a result r is correctly rounded by checking that
 * the exact value lies within half a gap from r (with ties resolved to the even neighbour).</p>
 */
public class ConstructionsTest {

    private final Random random = new Random(17L);

    // per coordinate: how often was it decided by the filter, by ESSA, by exact rational arithmetic?
    private long byFilter, byEssa, byRational;

    @Test
    public void simpleCrossings() {
        assertPoint(1, 1, crossing(0, 0, 2, 2, 0, 2, 2, 0));
        assertPoint(0, 0, crossing(-1, -1, 1, 1, -1, 1, 1, -1));
        assertPoint(1.5, 0, crossing(1, -1, 2, 1, 0, 0, 3, 0));
        assertPoint(1, 2, crossing(1, 0, 1, 5, 0, 2, 3, 2));   // axis parallel
        assertPoint(1.0 / 3, 1.0 / 3, crossing(0, 0, 1, 1, 0, 1, 0.5, 0)); // 1/3 is rounded
    }

    @Test(expected = IllegalArgumentException.class)
    public void notCrossingIsRejected() {
        Constructions.crossingPoint(0, 0, 2, 2, 2, 2, 3, 0);  // touching
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonFiniteIsRejected() {
        Constructions.crossingPoint(0, 0, 2, 2, 0, 2, Double.NaN, 0);
    }

    /**
     * Segments of up to 1000 m in projected coordinates: almost all coordinates must be decided by the filter.
     */
    @Test
    public void projectedCoordinates() {
        int n = 0;
        while (n < 100_000) {
            double ox = 150000 + 50000 * random.nextDouble(), oy = 200000 + 50000 * random.nextDouble();
            double len = 1000 * random.nextDouble();
            if (checkIfCrossing(ox + len * random.nextDouble(), oy + len * random.nextDouble(),
                    ox + len * random.nextDouble(), oy + len * random.nextDouble(),
                    ox + len * random.nextDouble(), oy + len * random.nextDouble(),
                    ox + len * random.nextDouble(), oy + len * random.nextDouble())) {
                n++;
            }
        }
        assertTrue("Undecided by filter: " + (byEssa + byRational), byEssa + byRational <= 2 * n / 10_000);
    }

    /**
     * Geographic coordinates with short segments
     */
    @Test
    public void geographicCoordinates() {
        int n = 0;
        while (n < 50_000) {
            double ox = -180 + 360 * random.nextDouble(), oy = -90 + 180 * random.nextDouble();
            double len = 0.01 * random.nextDouble();
            if (checkIfCrossing(ox + len * random.nextDouble(), oy + len * random.nextDouble(),
                    ox + len * random.nextDouble(), oy + len * random.nextDouble(),
                    ox + len * random.nextDouble(), oy + len * random.nextDouble(),
                    ox + len * random.nextDouble(), oy + len * random.nextDouble())) {
                n++;
            }
        }
        assertTrue("Undecided by filter: " + (byEssa + byRational), byEssa + byRational <= 2 * n / 10_000);
    }

    /**
     * Segments around the origin, where the crossing point is small relative to the segment lengths
     */
    @Test
    public void segmentsAroundOrigin() {
        int n = 0;
        while (n < 50_000) {
            if (checkIfCrossing(rnd(1), rnd(1), rnd(1), rnd(1), rnd(1), rnd(1), rnd(1), rnd(1))) n++;
        }
    }

    @Test
    public void allScales() {
        int n = 0;
        while (n < 20_000) {
            double scale = Math.scalb(1.0, random.nextInt(1900) - 1000);
            double offset = random.nextBoolean() ? 0 : rnd(Math.scalb(scale, random.nextInt(60)));
            if (checkIfCrossing(offset + rnd(scale), offset + rnd(scale), offset + rnd(scale), offset + rnd(scale),
                    offset + rnd(scale), offset + rnd(scale), offset + rnd(scale), offset + rnd(scale))) {
                n++;
            }
        }
        assertTrue(byRational > 0);
    }

    @Test
    public void extremeMagnitudes() {
        double tiny = Double.MIN_VALUE;
        check(-tiny, -tiny, tiny, tiny, -tiny, tiny, tiny, -tiny);
        check(0, 0, 3 * tiny, 3 * tiny, 0, 3 * tiny, 2 * tiny, 0);
        check(1e-310, 0, 3e-310, 5e-310, 0, 4e-310, 4e-310, 1e-310);
        double big = Double.MAX_VALUE / 4;
        check(-big, -big, big, big, -big, big, big, -big);
        check(-big, -big, big, big * 0.75, -big, big, big, -big * 0.3);
        check(1e300, 1e-300, 2e300, 3e-300, 1.5e300, 0, 1.2e300, 5e-300);
    }

    /**
     * The book's nearly parallel configuration (Fig. 5.4a), but with crossing segments, and including very small
     * angles.
     */
    @Test
    public void nearlyParallelSegments() {
        for (int k = 0; k <= 60; k++) {
            double angle = Math.scalb(1.0, -k);
            for (int i = 0; i < 300; i++) {
                double ox = 150000 * random.nextDouble(), oy = 200000 * random.nextDouble();
                double len = 1 + 1000 * random.nextDouble();
                double ax = ox, ay = oy, bx = ox + len, by = oy + len;
                double t = random.nextDouble();
                double mx = ax + t * (bx - ax), my = ay + t * (by - ay);
                double cx = mx - len, cy = my - len * (1 + angle);
                double dx = mx + len, dy = my + len * (1 + angle);
                checkIfCrossing(ax, ay, bx, by, cx, cy, dx, dy);
            }
        }
        assertTrue(byFilter > 0);
    }

    /**
     * Crossing points exactly on, or just next to, the midpoint between two consecutive doubles, also around powers
     * of 2 (where the gaps to the neighbours differ) and for subnormal results.
     */
    @Test
    public void tiesAndNearTies() {
        double[] qs = {1, 3, 1024, Math.nextDown(1024.0), Math.nextUp(1024.0), 150000.1, 0x1p52 - 1, -7.25,
                -Math.nextDown(2.0), 1e-300, Double.MIN_NORMAL, 0x1p-1060, 0, 1e300};
        for (double q0 : qs) {
            for (double q : new double[]{q0, Math.nextUp(q0), Math.nextDown(q0)}) {
                double up = Math.nextUp(q);
                double h = Math.max(1, Math.ulp(q) * 0x1p60);  // half-length of the horizontal segment
                for (double y : new double[]{0, Double.MIN_VALUE, -Double.MIN_VALUE, 1e-300, -1e-300, 0x1p-80}) {
                    // ab from (q, -1) to (nextUp(q), 1) crosses y = 0 at the midpoint of q and nextUp(q)
                    check(q, -1, up, 1, q - h, y, q + h, y);
                    check(q, -1, up, 1, q - h, -y, q + h, y);
                    // the same for the y coordinate
                    check(-1, q, 1, up, y, q - h, y, q + h);
                }
            }
        }
    }

    @Test
    public void exactlyRepresentableCrossingPoints() {
        for (int i = 0; i < 20_000; i++) {
            // integer coordinates; the crossing point is often an integer or a simple fraction
            double ax = random.nextInt(21) - 10, ay = random.nextInt(21) - 10;
            double bx = random.nextInt(21) - 10, by = random.nextInt(21) - 10;
            double cx = random.nextInt(21) - 10, cy = random.nextInt(21) - 10;
            double dx = random.nextInt(21) - 10, dy = random.nextInt(21) - 10;
            if (Predicates.segmentIntersection(ax, ay, bx, by, cx, cy, dx, dy) != org.geolatte.geom.SegmentIntersection.CROSSING) {
                continue;
            }
            check(ax, ay, bx, by, cx, cy, dx, dy);
            // a symmetric cross at the exact point a
            if (bx * dy - by * dx != 0) {
                assertPoint(ax, ay, crossing(ax - bx, ay - by, ax + bx, ay + by, ax - dx, ay - dy, ax + dx, ay + dy));
                check(ax - bx, ay - by, ax + bx, ay + by, ax - dx, ay - dy, ax + dx, ay + dy);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // The individual stages
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Every stage that decides must produce the same result as the exact rational computation.
     */
    @Test
    public void stagesAgree() {
        for (int i = 0; i < 20_000; i++) {
            double s = i % 2 == 0 ? 1 : 1000;
            double o = i % 3 == 0 ? 0 : 1e5;
            double ax = o + rnd(s), ay = o + rnd(s), bx = o + rnd(s), by = o + rnd(s);
            double cx = o + rnd(s), cy = o + rnd(s), dx = o + rnd(s), dy = o + rnd(s);
            int code = Predicates.classifySegments(ax, ay, bx, by, cx, cy, dx, dy);
            if ((code & Predicates.KIND_MASK) != Predicates.CROSSING_CODE) continue;
            int signD = (code & Predicates.CROSSING_POSITIVE) != 0 ? 1 : -1;
            assertEquals(signD, Constructions.denominatorTerms(ax, ay, bx, by, cx, cy, dx, dy).signum());
            double[] enc = new double[6];
            assertTrue(Constructions.enclose(ax, ay, bx, by, cx, cy, dx, dy, signD, enc));
            for (int axis = 0; axis < 2; axis++) {
                boolean yAxis = axis == 1;
                double exact = Constructions.roundExactly(yAxis, ax, ay, bx, by, cx, cy, dx, dy);
                assertCorrectlyRounded(exact, yAxis, ax, ay, bx, by, cx, cy, dx, dy);
                double q = enc[3 * axis], l = enc[3 * axis + 1], e = enc[3 * axis + 2];
                // the enclosure contains the exact value
                assertEnclosed(q, l, e, yAxis, ax, ay, bx, by, cx, cy, dx, dy);
                double filtered = Constructions.roundFromEnclosure(q, l, e);
                if (!Double.isNaN(filtered)) assertSame(exact, filtered);
                double essa = Constructions.roundWithEssa(yAxis, q, l, e, signD, ax, ay, bx, by, cx, cy, dx, dy);
                if (!Double.isNaN(essa)) assertSame(exact, essa);
                // ESSA stage, forced to test the lower and the upper midpoint around the correct result
                double up = Math.nextUp(exact) - exact, down = exact - Math.nextDown(exact);
                double e8 = Math.min(up, down) / 8;
                assertSame(exact, Constructions.roundWithEssa(yAxis, exact, -down / 2, e8, signD,
                        ax, ay, bx, by, cx, cy, dx, dy));
                assertSame(exact, Constructions.roundWithEssa(yAxis, exact, up / 2, e8, signD,
                        ax, ay, bx, by, cx, cy, dx, dy));
            }
        }
    }

    @Test
    public void roundQuotient() {
        assertSame(1.0 / 3, Constructions.roundQuotient(BigInteger.ONE, BigInteger.valueOf(3), 0));
        assertSame(-2.0 / 3, Constructions.roundQuotient(BigInteger.valueOf(-2), BigInteger.valueOf(3), 0));
        assertSame(0.1, Constructions.roundQuotient(BigInteger.ONE, BigInteger.TEN, 0));
        assertSame(1e-310, Constructions.roundQuotient(BigInteger.ONE, BigInteger.TEN.pow(310), 0));
        // subnormal ties: 1.5 and 0.5 times MIN_VALUE round to the even neighbour
        assertSame(2 * Double.MIN_VALUE, Constructions.roundQuotient(BigInteger.valueOf(3), BigInteger.valueOf(2), -1074));
        assertSame(0.0, Constructions.roundQuotient(BigInteger.ONE, BigInteger.valueOf(2), -1074));
        assertSame(Double.MIN_VALUE, Constructions.roundQuotient(BigInteger.valueOf(4), BigInteger.valueOf(3), -1074));
        assertSame(3 * Double.MIN_VALUE, Constructions.roundQuotient(BigInteger.valueOf(3), BigInteger.valueOf(2), -1073));
        // ties to even at 53 bits
        BigInteger odd = BigInteger.ONE.shiftLeft(53).add(BigInteger.ONE);   // 2^53 + 1, between 2^53 and 2^53 + 2
        assertSame(0x1p53, Constructions.roundQuotient(odd, BigInteger.ONE, 0));
        BigInteger odd3 = BigInteger.ONE.shiftLeft(53).add(BigInteger.valueOf(3));
        assertSame(0x1p53 + 4, Constructions.roundQuotient(odd3, BigInteger.ONE, 0));
        for (int i = 0; i < 10_000; i++) {
            double a = rnd(Math.scalb(1.0, random.nextInt(200) - 100));
            double b = rnd(Math.scalb(1.0, random.nextInt(200) - 100));
            if (b == 0) continue;
            // a / b is correctly rounded in IEEE arithmetic
            BigInteger[] fa = fraction(a), fb = fraction(b);
            assertSame(a / b, Constructions.roundQuotient(fa[0].multiply(fb[1]), fa[1].multiply(fb[0]), 0));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    private static double[] crossing(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        return Constructions.crossingPoint(ax, ay, bx, by, cx, cy, dx, dy);
    }

    private static void assertPoint(double x, double y, double[] p) {
        assertEquals(x, p[0], 0);
        assertEquals(y, p[1], 0);
    }

    private static void assertSame(double expected, double actual) {
        assertEquals(Double.doubleToLongBits(expected), Double.doubleToLongBits(actual));
    }

    private boolean checkIfCrossing(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        if (Predicates.segmentIntersection(ax, ay, bx, by, cx, cy, dx, dy) != org.geolatte.geom.SegmentIntersection.CROSSING) {
            return false;
        }
        check(ax, ay, bx, by, cx, cy, dx, dy);
        return true;
    }

    /**
     * Checks the crossing point of crossing segments: correctly rounded, independent of order and orientation of
     * the segments, within both envelopes. Records which stage decided.
     */
    private void check(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        double[] p = crossing(ax, ay, bx, by, cx, cy, dx, dy);
        assertCorrectlyRounded(p[0], false, ax, ay, bx, by, cx, cy, dx, dy);
        assertCorrectlyRounded(p[1], true, ax, ay, bx, by, cx, cy, dx, dy);
        double[][] variants = {
                crossing(bx, by, ax, ay, cx, cy, dx, dy),
                crossing(ax, ay, bx, by, dx, dy, cx, cy),
                crossing(cx, cy, dx, dy, ax, ay, bx, by),
                crossing(dx, dy, cx, cy, bx, by, ax, ay)};
        for (double[] v : variants) {
            assertSame(p[0], v[0]);
            assertSame(p[1], v[1]);
        }
        assertTrue(Math.max(Math.min(ax, bx), Math.min(cx, dx)) <= p[0]);
        assertTrue(Math.min(Math.max(ax, bx), Math.max(cx, dx)) >= p[0]);
        assertTrue(Math.max(Math.min(ay, by), Math.min(cy, dy)) <= p[1]);
        assertTrue(Math.min(Math.max(ay, by), Math.max(cy, dy)) >= p[1]);
        recordStages(ax, ay, bx, by, cx, cy, dx, dy);
    }

    private void recordStages(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        int code = Predicates.classifySegments(ax, ay, bx, by, cx, cy, dx, dy);
        int signD = (code & Predicates.CROSSING_POSITIVE) != 0 ? 1 : -1;
        double[] enc = new double[6];
        boolean enclosed = Constructions.enclose(ax, ay, bx, by, cx, cy, dx, dy, signD, enc);
        for (int axis = 0; axis < 2; axis++) {
            if (!enclosed) {
                byRational++;
                continue;
            }
            double q = enc[3 * axis], l = enc[3 * axis + 1], e = enc[3 * axis + 2];
            assertEnclosed(q, l, e, axis == 1, ax, ay, bx, by, cx, cy, dx, dy);
            if (!Double.isNaN(Constructions.roundFromEnclosure(q, l, e))) {
                byFilter++;
            } else if (!Double.isNaN(Constructions.roundWithEssa(axis == 1, q, l, e, signD, ax, ay, bx, by, cx, cy, dx, dy))) {
                byEssa++;
            } else {
                byRational++;
            }
        }
    }

    // the exact coordinate of the crossing point as num / den
    private static BigDecimal[] exactCoordinate(boolean yAxis, double ax, double ay, double bx, double by,
                                                double cx, double cy, double dx, double dy) {
        BigDecimal sx = big(dx).subtract(big(cx)), sy = big(dy).subtract(big(cy));
        BigDecimal alpha = sx.multiply(big(ay).subtract(big(cy))).subtract(sy.multiply(big(ax).subtract(big(cx))));
        BigDecimal beta = sx.multiply(big(by).subtract(big(cy))).subtract(sy.multiply(big(bx).subtract(big(cx))));
        BigDecimal den = alpha.subtract(beta);
        BigDecimal pa = big(yAxis ? ay : ax), pb = big(yAxis ? by : bx);
        BigDecimal num = pa.multiply(den).add(pb.subtract(pa).multiply(alpha));
        return new BigDecimal[]{num, den};
    }

    private static void assertCorrectlyRounded(double r, boolean yAxis, double ax, double ay, double bx, double by,
                                               double cx, double cy, double dx, double dy) {
        assertTrue(Double.isFinite(r));
        BigDecimal[] nd = exactCoordinate(yAxis, ax, ay, bx, by, cx, cy, dx, dy);
        BigDecimal diff = nd[0].subtract(big(r).multiply(nd[1]));   // (exact - r) * den
        int side = diff.signum() * nd[1].signum();
        if (side == 0) return;
        double gap = side > 0 ? Math.nextUp(r) - r : r - Math.nextDown(r);
        BigDecimal halfGap = big(gap).multiply(new BigDecimal("0.5")).multiply(nd[1].abs());
        int c = diff.abs().compareTo(halfGap);
        String msg = String.format("%s coordinate %s is not correctly rounded for (%s, %s)-(%s, %s) x (%s, %s)-(%s, %s)",
                yAxis ? "Y" : "X", r, ax, ay, bx, by, cx, cy, dx, dy);
        assertTrue(msg, c < 0 || (c == 0 && (Double.doubleToRawLongBits(r) & 1) == 0));
    }

    private static void assertEnclosed(double q, double l, double e, boolean yAxis, double ax, double ay, double bx, double by,
                                       double cx, double cy, double dx, double dy) {
        BigDecimal[] nd = exactCoordinate(yAxis, ax, ay, bx, by, cx, cy, dx, dy);
        BigDecimal approx = big(q).add(big(l));
        // |exact - approx| <= e  <=>  |num - approx * den| <= e * |den|
        BigDecimal err = nd[0].subtract(approx.multiply(nd[1])).abs();
        assertTrue("Exact value not in enclosure", err.compareTo(big(e).multiply(nd[1].abs())) <= 0);
    }

    private static BigInteger[] fraction(double v) {
        BigDecimal b = big(v);
        if (b.scale() <= 0) return new BigInteger[]{b.toBigIntegerExact(), BigInteger.ONE};
        return new BigInteger[]{b.unscaledValue(), BigInteger.TEN.pow(b.scale())};
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }

    private double rnd(double scale) {
        return scale * (2 * random.nextDouble() - 1);
    }
}
