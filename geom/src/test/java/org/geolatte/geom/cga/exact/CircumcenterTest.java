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
            double[] filtered = Constructions.circumcenterDoubleWord(ax, ay, bx, by, cx, cy);
            for (int axis = 0; axis < 2; axis++) {
                double e = Constructions.circumcenterExactly(axis == 1, ax, ay, bx, by, cx, cy);
                assertEquals(0, Double.compare(e, c[axis]));
                if (!Double.isNaN(filtered[axis])) assertEquals(0, Double.compare(filtered[axis], c[axis]));
            }
        }
    }

    @Test
    public void doubleWordStageDecidesPracticalInputs() {
        // GIS coordinates (large offset, small triangles) and the unit square (offsets as large as the coordinates)
        for (boolean gis : new boolean[]{true, false}) {
            int undecided = 0;
            for (int i = 0; i < 20000; i++) {
                double x0 = gis ? 150000 + 1000 * random.nextDouble() : 0.5, y0 = gis ? 200000 + 1000 * random.nextDouble() : 0.5;
                double s = gis ? 10 : 0.5;
                double ax = x0 + rnd(s), ay = y0 + rnd(s), bx = x0 + rnd(s), by = y0 + rnd(s);
                double cx = x0 + rnd(s), cy = y0 + rnd(s);
                if (Predicates.orientation(ax, ay, bx, by, cx, cy) == 0) continue;
                double[] c = Constructions.circumcenter(ax, ay, bx, by, cx, cy);
                check(c, ax, ay, bx, by, cx, cy);
                double[] dw = Constructions.circumcenterDoubleWord(ax, ay, bx, by, cx, cy);
                for (int axis = 0; axis < 2; axis++) {
                    if (Double.isNaN(dw[axis])) undecided++;
                    else assertEquals(0, Double.compare(c[axis], dw[axis]));
                }
            }
            assertTrue("undecided: " + undecided, undecided < 10);
        }
    }

    @Test
    public void exactOffsets() {
        // the circumcenter of (1, 0), (0, 1), (2, 1) is (1, 1): the X offset from a is exactly zero; the double-word
        // stage decides both coordinates
        assertArrayEquals(new double[]{1, 1}, Constructions.circumcenterDoubleWord(1, 0, 0, 1, 2, 1), 0);
        assertArrayEquals(new double[]{1, 1}, Constructions.circumcenterDoubleWord(0, 1, 2, 1, 1, 0), 0);
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
