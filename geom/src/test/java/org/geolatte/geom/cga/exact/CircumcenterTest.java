package org.geolatte.geom.cga.exact;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests {@link Constructions#circumcenter(double, double, double, double, double, double)} against exact rational
 * arithmetic.
 */
public class CircumcenterTest {

    private final Random random = new Random(23L);

    @Test
    public void simpleCases() {
        assertArrayEquals(new double[]{1, 1}, Constructions.circumcenter(0, 0, 2, 0, 0, 2), 0);
        assertArrayEquals(new double[]{0, 0}, Constructions.circumcenter(1, 0, 0, 1, -1, 0), 0);
        // 1/3 is not representable
        double[] c = Constructions.circumcenter(0, 0, 1, 0, 0, 2.0 / 3);
        check(c, 0, 0, 1, 0, 0, 2.0 / 3);
    }

    @Test
    public void collinearOrInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Constructions.circumcenter(0, 0, 1, 1, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> Constructions.circumcenter(0, 0, 0, 0, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> Constructions.circumcenter(0, 0, 1, Double.NaN, 2, 3));
    }

    @Test
    public void cocircularTrianglesShareTheCenter() {
        // the four triangles of a rotated "square" whose corners are not exactly cocircular in decimal terms
        double[][] p = {{0.1, 0.3}, {0.7, 0.1}, {0.9, 0.7}, {0.3, 0.9}};
        double[] c0 = Constructions.circumcenter(p[0][0], p[0][1], p[1][0], p[1][1], p[2][0], p[2][1]);
        for (int skip = 0; skip < 4; skip++) {
            double[][] t = new double[3][];
            for (int i = 0, j = 0; i < 4; i++) if (i != skip) t[j++] = p[i];
            double[] c = Constructions.circumcenter(t[0][0], t[0][1], t[1][0], t[1][1], t[2][0], t[2][1]);
            check(c, t[0][0], t[0][1], t[1][0], t[1][1], t[2][0], t[2][1]);
        }
        check(c0, p[0][0], p[0][1], p[1][0], p[1][1], p[2][0], p[2][1]);
    }

    @Test
    public void randomTrianglesAllScales() {
        for (int i = 0; i < 20000; i++) {
            double scale = Math.scalb(1.0, random.nextInt(1200) - 600);
            double offset = random.nextBoolean() ? 0 : scale * Math.scalb(1.0, random.nextInt(40));
            double ax = offset + rnd(scale), ay = offset + rnd(scale);
            double bx = offset + rnd(scale), by = offset + rnd(scale);
            double cx, cy;
            if (random.nextInt(4) == 0) {
                // nearly collinear: c close to the line through a and b
                double t = random.nextDouble() * 3 - 1;
                cx = ax + t * (bx - ax) + rnd(scale * 1e-12);
                cy = ay + t * (by - ay) + rnd(scale * 1e-12);
            } else {
                cx = offset + rnd(scale);
                cy = offset + rnd(scale);
            }
            if (Predicates.orientation(ax, ay, bx, by, cx, cy) == 0) continue;
            double[] c = Constructions.circumcenter(ax, ay, bx, by, cx, cy);
            check(c, ax, ay, bx, by, cx, cy);
            // independent of the order of the points
            assertArrayEquals(c, Constructions.circumcenter(cx, cy, ax, ay, bx, by), 0);
            assertArrayEquals(c, Constructions.circumcenter(bx, by, ax, ay, cx, cy), 0);
            // the stages agree
            double[] filtered = Constructions.circumcenterFromEnclosure(ax, ay, bx, by, cx, cy);
            for (int axis = 0; axis < 2; axis++) {
                double e = Constructions.circumcenterExactly(axis == 1, ax, ay, bx, by, cx, cy);
                assertEquals(0, Double.compare(e, c[axis]));
                if (!Double.isNaN(filtered[axis])) assertEquals(0, Double.compare(filtered[axis], c[axis]));
            }
        }
    }

    @Test
    public void gisCoordinates() {
        for (int i = 0; i < 20000; i++) {
            double x0 = 150000 + 1000 * random.nextDouble(), y0 = 200000 + 1000 * random.nextDouble();
            double ax = x0 + rnd(10), ay = y0 + rnd(10), bx = x0 + rnd(10), by = y0 + rnd(10);
            double cx = x0 + rnd(10), cy = y0 + rnd(10);
            if (Predicates.orientation(ax, ay, bx, by, cx, cy) == 0) continue;
            check(Constructions.circumcenter(ax, ay, bx, by, cx, cy), ax, ay, bx, by, cx, cy);
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    private static void check(double[] c, double ax, double ay, double bx, double by, double cx, double cy) {
        for (int axis = 0; axis < 2; axis++) {
            BigDecimal[] nd = exact(axis == 1, ax, ay, bx, by, cx, cy);
            assertCorrectlyRounded(c[axis], nd[0], nd[1]);
        }
    }

    // the exact coordinate as num / den
    private static BigDecimal[] exact(boolean yAxis, double ax, double ay, double bx, double by, double cx, double cy) {
        BigDecimal bpx = big(bx).subtract(big(ax)), bpy = big(by).subtract(big(ay));
        BigDecimal cpx = big(cx).subtract(big(ax)), cpy = big(cy).subtract(big(ay));
        BigDecimal den = bpx.multiply(cpy).subtract(bpy.multiply(cpx)).multiply(BigDecimal.valueOf(2));
        BigDecimal b2 = bpx.multiply(bpx).add(bpy.multiply(bpy)), c2 = cpx.multiply(cpx).add(cpy.multiply(cpy));
        BigDecimal offset = yAxis
                ? bpx.multiply(c2).subtract(cpx.multiply(b2))
                : cpy.multiply(b2).subtract(bpy.multiply(c2));
        return new BigDecimal[]{big(yAxis ? ay : ax).multiply(den).add(offset), den};
    }

    private static void assertCorrectlyRounded(double r, BigDecimal num, BigDecimal den) {
        if (Double.isInfinite(r)) return;   // overflow
        BigDecimal diff = num.subtract(big(r).multiply(den));   // (exact - r) * den
        int side = diff.signum() * den.signum();
        if (side == 0) return;
        double gap = side > 0 ? Math.nextUp(r) - r : r - Math.nextDown(r);
        BigDecimal halfGap = big(gap).multiply(new BigDecimal("0.5")).multiply(den.abs());
        int c = diff.abs().compareTo(halfGap);
        assertTrue("not correctly rounded: " + r, c < 0 || (c == 0 && (Double.doubleToRawLongBits(r) & 1) == 0));
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }

    private double rnd(double scale) {
        return scale * (2 * random.nextDouble() - 1);
    }
}
