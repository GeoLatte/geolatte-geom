package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests {@link Predicates#crossProductSign} and {@link Predicates#orientationOfCrossing}, and each of their
 * evaluation stages, against independent {@code BigDecimal} oracles.
 */
public class CrossingPredicatesTest {

    private final Random random = new Random(4321L);

    // ---------------------------------------------------------------------------------------------------------
    // crossProductSign
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void crossProductSimpleCases() {
        assertEquals(1, Predicates.crossProductSign(0, 0, 1, 0, 5, 5, 5, 6));
        assertEquals(-1, Predicates.crossProductSign(0, 0, 1, 0, 5, 5, 5, 4));
        assertEquals(0, Predicates.crossProductSign(0, 0, 1, 1, 5, 5, 7, 7));
        assertEquals(0, Predicates.crossProductSign(0, 0, 1, 1, 5, 5, 3, 3));
        assertEquals(0, Predicates.crossProductSign(0, 0, 0, 0, 5, 5, 3, 1));
        // with c = a, this is the orientation
        assertEquals(Predicates.orientation(0.1, 0.2, 0.7, 0.3, 0.4, 0.9),
                Predicates.crossProductSign(0.1, 0.2, 0.7, 0.3, 0.1, 0.2, 0.4, 0.9));
    }

    @Test
    public void crossProductNearParallelAllScales() {
        int undecided = 0, inexact = 0;
        for (int k = 0; k < 100_000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(1200) - 1100);
            double ax = rnd(scale), ay = rnd(scale), bx = rnd(scale), by = rnd(scale);
            double cx = rnd(scale), cy = rnd(scale);
            double t = random.nextDouble() * 4 - 2;
            double dx = cx + t * (bx - ax), dy = cy + t * (by - ay);
            switch (k % 5) {
                case 0: dx = Math.nextUp(dx); break;
                case 1: dy = Math.nextDown(dy); break;
                case 2: bx = ax; dx = cx; break;   // both vertical
                case 3: by = ay; dy = cy; break;   // both horizontal
                default: break;
            }
            int expected = big(bx).subtract(big(ax)).multiply(big(dy).subtract(big(cy)))
                    .subtract(big(by).subtract(big(ay)).multiply(big(dx).subtract(big(cx)))).signum();
            String msg = String.format("cross(%s, %s, %s, %s, %s, %s, %s, %s)", ax, ay, bx, by, cx, cy, dx, dy);
            assertEquals(msg, expected, Predicates.crossProductSign(ax, ay, bx, by, cx, cy, dx, dy));
            int bound = Predicates.crossProductErrorBound(ax, ay, bx, by, cx, cy, dx, dy);
            if (bound == Predicates.UNDECIDED) undecided++;
            else assertEquals(msg + " [error bound]", expected, bound);
            int filter = Predicates.crossProductFilter(ax, ay, bx, by, cx, cy, dx, dy);
            if (filter != Predicates.UNDECIDED) assertEquals(msg + " [filter]", expected, filter);
            ExactSum terms = Predicates.crossProductTerms(ax, ay, bx, by, cx, cy, dx, dy);
            if (terms.isExact()) assertEquals(msg + " [essa]", expected, terms.signum());
            else inexact++;
            assertEquals(msg + " [big]", expected, Predicates.crossProductBig(ax, ay, bx, by, cx, cy, dx, dy));
        }
        assertTrue(undecided > 1000);
        assertTrue(inexact > 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void crossProductRejectsInfinity() {
        Predicates.crossProductSign(0, 0, 1, 1, 0, 0, Double.POSITIVE_INFINITY, 1);
    }

    // ---------------------------------------------------------------------------------------------------------
    // orientationOfCrossing
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void orientationOfCrossingSimpleCases() {
        // the diagonals of the unit square cross at (0.5, 0.5)
        assertEquals(1, Predicates.orientationOfCrossing(0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0));
        assertEquals(-1, Predicates.orientationOfCrossing(1, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0));
        assertEquals(0, Predicates.orientationOfCrossing(0, 0.5, 1, 0.5, 0, 0, 1, 1, 0, 1, 1, 0));
        // (0, 0)-(1, 1) and (1, 0)-(-1, 1) cross at (1/3, 1/3), which is not representable; so does (0, 1)-(1, -1)
        assertEquals(0, Predicates.orientationOfCrossing(0, 1, 1, -1, 0, 0, 1, 1, 1, 0, -1, 1));
        assertEquals(-1, Predicates.orientationOfCrossing(0, 1, Math.nextUp(1.0), -1, 0, 0, 1, 1, 1, 0, -1, 1));
        // the lines do not need to be segments that cross: the crossing of the lines is used
        assertEquals(1, Predicates.orientationOfCrossing(0, 0, 1, 0, 0, 0, 1, 1, 5, 0, 6, -1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void orientationOfCrossingRejectsParallelLines() {
        Predicates.orientationOfCrossing(0, 0, 1, 0, 0, 0, 1, 1, 2, 0, 3, 1);
    }

    /**
     * Three lines through a common rational point that is (in general) not representable, the third one
     * perturbed by an ulp in some cases.
     */
    @Test
    public void orientationOfCrossingConcurrentLines() {
        int zeros = 0;
        for (int k = 0; k < 20_000; k++) {
            long d = 1 + random.nextInt(1000);
            long n1 = random.nextInt(2_000_000) - 1_000_000, n2 = random.nextInt(2_000_000) - 1_000_000;
            double scale = Math.scalb(1.0, random.nextInt(40) - 20);
            double[] l1 = lineThrough(n1, n2, d, scale), l2 = lineThrough(n1, n2, d, scale);
            double[] l3 = lineThrough(n1, n2, d, scale);
            if (k % 3 == 1) l3[2] = Math.nextUp(l3[2]);
            if (k % 3 == 2) l3[3] = Math.nextDown(l3[3]);
            if (crossSign(l1, l2) == 0) continue;
            int expected = checkOrientationOfCrossing(l3, l1, l2);
            if (expected == 0) zeros++;
        }
        assertTrue(zeros > 5000);
    }

    @Test
    public void orientationOfCrossingRandomAllScales() {
        int undecided = 0;
        for (int k = 0; k < 20_000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(600) - 300);
            double offset = k % 2 == 0 ? 0 : scale * 1e5;
            double[] s = {offset + rnd(scale), offset + rnd(scale), offset + rnd(scale), offset + rnd(scale)};
            double[] t = {offset + rnd(scale), offset + rnd(scale), offset + rnd(scale), offset + rnd(scale)};
            if (crossSign(s, t) == 0) continue;
            // a line through (approximately) the crossing point
            double[] x = approximateCrossing(s, t);
            double[] l = {x[0] + rnd(scale), x[1] + rnd(scale), x[0], x[1]};
            if (k % 4 == 1) l[2] = Math.nextUp(l[2]);
            int expected = checkOrientationOfCrossing(l, s, t);
            if (Predicates.crossingNumeratorFilter(l[0], l[1], l[2], l[3], s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3])
                    == Predicates.UNDECIDED) {
                undecided++;
            }
            assertTrue(expected >= -1 && expected <= 1);
        }
        assertTrue(undecided > 100);
    }

    // checks all stages; returns the expected sign
    private int checkOrientationOfCrossing(double[] l, double[] s, double[] t) {
        String msg = String.format("orientationOfCrossing(%s, %s, %s, %s; %s, %s, %s, %s; %s, %s, %s, %s)",
                l[0], l[1], l[2], l[3], s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]);
        int expected = oracle(l, s, t);
        assertEquals(msg, expected, Predicates.orientationOfCrossing(l[0], l[1], l[2], l[3],
                s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]));
        // the numerator stages; the numerator has the sign of expected * sign(alpha - beta)
        int numerator = expected * crossSign(s, t);
        int filter = Predicates.crossingNumeratorFilter(l[0], l[1], l[2], l[3], s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]);
        if (filter != Predicates.UNDECIDED) assertEquals(msg + " [filter]", numerator, filter);
        ExactSum terms = Predicates.crossingNumeratorTerms(l[0], l[1], l[2], l[3], s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]);
        if (terms.isExact()) assertEquals(msg + " [essa]", numerator, terms.signum());
        assertEquals(msg + " [big]", numerator,
                Predicates.crossingNumeratorBig(l[0], l[1], l[2], l[3], s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]));
        return expected;
    }

    /**
     * The orientation of (a, b, X), X = p + (q - p) alpha / (alpha - beta), alpha = o(r, w, p), beta = o(r, w, q):
     * the sign of (b - a) x ((p - a)(alpha - beta) + (q - p) alpha), times the sign of alpha - beta.
     */
    private static int oracle(double[] l, double[] s, double[] t) {
        BigDecimal ax = big(l[0]), ay = big(l[1]), bx = big(l[2]), by = big(l[3]);
        BigDecimal px = big(s[0]), py = big(s[1]), qx = big(s[2]), qy = big(s[3]);
        BigDecimal rx = big(t[0]), ry = big(t[1]), wx = big(t[2]), wy = big(t[3]);
        BigDecimal alpha = cross(wx.subtract(rx), wy.subtract(ry), px.subtract(rx), py.subtract(ry));
        BigDecimal beta = cross(wx.subtract(rx), wy.subtract(ry), qx.subtract(rx), qy.subtract(ry));
        BigDecimal den = alpha.subtract(beta);
        BigDecimal vx = px.subtract(ax).multiply(den).add(qx.subtract(px).multiply(alpha));
        BigDecimal vy = py.subtract(ay).multiply(den).add(qy.subtract(py).multiply(alpha));
        return cross(bx.subtract(ax), by.subtract(ay), vx, vy).signum() * den.signum();
    }

    private static BigDecimal cross(BigDecimal ux, BigDecimal uy, BigDecimal vx, BigDecimal vy) {
        return ux.multiply(vy).subtract(uy.multiply(vx));
    }

    private static int crossSign(double[] s, double[] t) {
        return Predicates.crossProductSign(s[0], s[1], s[2], s[3], t[0], t[1], t[2], t[3]);
    }

    // a segment through (n1 / d, n2 / d) with representable endpoints (scaled by a power of 2, which is exact)
    private double[] lineThrough(long n1, long n2, long d, double scale) {
        long x0 = random.nextInt(2_000_000) - 1_000_000, y0 = random.nextInt(2_000_000) - 1_000_000;
        long k = 1 + random.nextInt(3);
        long x1 = x0 + k * (n1 - d * x0), y1 = y0 + k * (n2 - d * y0);
        return new double[]{x0 * scale, y0 * scale, x1 * scale, y1 * scale};
    }

    private static double[] approximateCrossing(double[] s, double[] t) {
        double dx = s[2] - s[0], dy = s[3] - s[1], ex = t[2] - t[0], ey = t[3] - t[1];
        double u = ((t[0] - s[0]) * ey - (t[1] - s[1]) * ex) / (dx * ey - dy * ex);
        return new double[]{s[0] + u * dx, s[1] + u * dy};
    }

    private double rnd(double scale) {
        return (random.nextDouble() * 2 - 1) * scale;
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }
}
