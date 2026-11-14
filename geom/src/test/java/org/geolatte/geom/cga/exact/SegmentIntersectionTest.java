package org.geolatte.geom.cga.exact;

import org.geolatte.geom.SegmentIntersection;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.geolatte.geom.SegmentIntersection.*;
import static org.junit.Assert.*;

/**
 * Tests {@link Predicates#segmentIntersection(double, double, double, double, double, double, double, double)}
 * against an oracle that follows the formulation of Ratschek &amp; Rokne (Sec. 5.2): Cramer's rule for the
 * parameters t and s, evaluated exactly with {@code BigDecimal}.
 */
public class SegmentIntersectionTest {

    private final Random random = new Random(52L);

    private final Map<SegmentIntersection, Integer> seen = new EnumMap<>(SegmentIntersection.class);

    @Test
    public void simpleCases() {
        assertEquals(CROSSING, classify(0, 0, 2, 2, 0, 2, 2, 0));
        assertEquals(DISJOINT, classify(0, 0, 1, 1, 0, 2, 0.9, 1.1));
        assertEquals(TOUCHING, classify(0, 0, 2, 2, 1, 1, 3, 0));      // endpoint of cd in interior of ab
        assertEquals(TOUCHING, classify(0, 0, 2, 2, 2, 2, 3, 0));      // shared endpoint
        assertEquals(DISJOINT, classify(0, 0, 1, 0, 0, 1, 1, 1));      // parallel
        assertEquals(OVERLAPPING, classify(0, 0, 2, 2, 1, 1, 3, 3));   // collinear, overlapping
        assertEquals(OVERLAPPING, classify(0, 0, 4, 4, 1, 1, 2, 2));   // collinear, containment
        assertEquals(OVERLAPPING, classify(0, 0, 2, 2, 2, 2, 0, 0));   // identical, reversed
        assertEquals(TOUCHING, classify(0, 0, 2, 2, 2, 2, 3, 3));      // collinear, touching at an endpoint
        assertEquals(DISJOINT, classify(0, 0, 1, 1, 2, 2, 3, 3));      // collinear, disjoint
        assertEquals(OVERLAPPING, classify(1, 0, 1, 5, 1, 3, 1, -2));  // vertical, overlapping
        assertEquals(TOUCHING, classify(1, 1, 1, 1, 0, 0, 2, 2));      // point on segment
        assertEquals(DISJOINT, classify(1, 1.5, 1, 1.5, 0, 0, 2, 2));  // point off segment
        assertEquals(TOUCHING, classify(3, 4, 3, 4, 3, 4, 3, 4));      // equal points
        assertEquals(DISJOINT, classify(3, 4, 3, 4, 4, 3, 4, 3));      // different points
    }

    /**
     * The book's first test configuration (Fig. 5.4a, Table 5.1): nearly parallel segments, with the endpoints
     * perturbed by 2^-k. Floating point computation of the intersection point fails for most of these.
     */
    @Test
    public void nearlyParallelSegments() {
        for (int k = 0; k <= 52; k++) {
            double pert = Math.scalb(1.0, -k);
            for (int i = 0; i < 300; i++) {
                double ox = 150000 * random.nextDouble(), oy = 200000 * random.nextDouble();
                double len = 1 + 1000 * random.nextDouble();
                double ax = ox + p(pert), ay = oy + p(pert);
                double bx = ox + len + p(pert), by = oy + len + p(pert);
                double cx = ox + 0.25 * len + p(pert), cy = oy + 0.25 * len + p(pert);
                double dx = ox + 1.25 * len + p(pert), dy = oy + 1.25 * len + p(pert);
                check(ax, ay, bx, by, cx, cy, dx, dy);
            }
        }
    }

    /**
     * The book's second test configuration (Fig. 5.4b, Table 5.2): nearly perpendicular segments, where the
     * endpoint of one segment lies (nearly) on the other segment.
     */
    @Test
    public void nearlyPerpendicularSegments() {
        for (int k = 0; k <= 52; k++) {
            double pert = Math.scalb(1.0, -k);
            for (int i = 0; i < 300; i++) {
                double ox = 150000 * random.nextDouble(), oy = 200000 * random.nextDouble();
                double len = 1 + 1000 * random.nextDouble();
                double ax = ox, ay = oy, bx = ox + len, by = oy + len;
                double t = random.nextDouble();
                // c lies (nearly) on ab, d lies away from it, perpendicular
                double cx = ax + t * (bx - ax) + p(pert), cy = ay + t * (by - ay) + p(pert);
                double dx = cx + len, dy = cy - len;
                check(ax, ay, bx, by, cx, cy, dx, dy);
            }
        }
    }

    /**
     * Endpoints that lie on the other segment up to rounding, or exactly at an endpoint of it.
     */
    @Test
    public void touchingConfigurations() {
        for (int i = 0; i < 50_000; i++) {
            double scale = Math.scalb(1.0, random.nextInt(60) - 30);
            double ax = rnd(scale), ay = rnd(scale), bx = rnd(scale), by = rnd(scale);
            double t = random.nextDouble();
            double cx, cy;
            switch (i % 4) {
                case 0:
                    cx = bx;
                    cy = by;
                    break;
                case 1:
                    cx = ax + t * (bx - ax);
                    cy = ay + t * (by - ay);
                    break;
                case 2:
                    cx = Math.nextUp(ax + t * (bx - ax));
                    cy = ay + t * (by - ay);
                    break;
                default:
                    cx = ax + t * (bx - ax);
                    cy = Math.nextDown(ay + t * (by - ay));
            }
            double dx = rnd(scale), dy = rnd(scale);
            check(ax, ay, bx, by, cx, cy, dx, dy);
        }
        assertTrue(seen.get(TOUCHING) > 10_000);
        assertTrue(seen.get(CROSSING) > 1000);
        assertTrue(seen.get(DISJOINT) > 1000);
    }

    /**
     * Collinear segments, with exactly representable positions on a line with large offset: all relative
     * arrangements (disjoint, touching, overlapping, containment, identical).
     */
    @Test
    public void exactlyCollinearSegments() {
        for (int i = 0; i < 50_000; i++) {
            double ox = random.nextInt(1 << 24), oy = random.nextInt(1 << 24);
            double ux, uy;
            switch (i % 3) {
                case 0: ux = 1; uy = 0; break;   // horizontal
                case 1: ux = 0; uy = 1; break;   // vertical
                default: ux = random.nextInt(200) - 100; uy = random.nextInt(200) - 100;
            }
            double f = Math.scalb(1.0, random.nextInt(10) - 5);
            int[] t = {random.nextInt(8), random.nextInt(8), random.nextInt(8), random.nextInt(8)};
            check(ox + t[0] * ux * f, oy + t[0] * uy * f, ox + t[1] * ux * f, oy + t[1] * uy * f,
                    ox + t[2] * ux * f, oy + t[2] * uy * f, ox + t[3] * ux * f, oy + t[3] * uy * f);
        }
        for (SegmentIntersection si : SegmentIntersection.values()) {
            if (si != CROSSING) assertTrue(si + " not covered", seen.getOrDefault(si, 0) > 1000);
        }
    }

    /**
     * Nearly collinear segments: positions on a line, rounded to doubles.
     */
    @Test
    public void nearlyCollinearSegments() {
        for (int i = 0; i < 50_000; i++) {
            double scale = Math.scalb(1.0, random.nextInt(40) - 20);
            double ax = 1000 * scale + rnd(scale), ay = 1000 * scale + rnd(scale);
            double ux = rnd(scale), uy = rnd(scale);
            double[] t = {random.nextDouble() * 2, random.nextDouble() * 2, random.nextDouble() * 2};
            check(ax, ay, ax + t[0] * ux, ay + t[0] * uy, ax + t[1] * ux, ay + t[1] * uy, ax + t[2] * ux, ay + t[2] * uy);
        }
    }

    @Test
    public void degenerateSegments() {
        for (int i = 0; i < 20_000; i++) {
            double ax = rnd(100), ay = rnd(100), bx = rnd(100), by = rnd(100);
            double t = random.nextDouble();
            double px = ax + t * (bx - ax), py = ay + t * (by - ay);
            if (i % 3 == 0) {
                px = ax;
                py = ay;
            }
            check(px, py, px, py, ax, ay, bx, by);
            check(ax, ay, bx, by, px, py, px, py);
            check(px, py, px, py, px, py, px, py);
            check(px, py, px, py, ax, ay, ax, ay);
        }
    }

    @Test
    public void extremeMagnitudes() {
        for (double scale : new double[]{1e-300, 1e-170, 1e-160, 1e150, 1e200, 1e300}) {
            for (int i = 0; i < 2_000; i++) {
                double ax = rnd(scale), ay = rnd(scale), bx = rnd(scale), by = rnd(scale);
                double t = random.nextDouble();
                double cx = ax + t * (bx - ax), cy = ay + t * (by - ay);
                check(ax, ay, bx, by, cx, cy, rnd(scale), rnd(scale));
                check(ax, ay, bx, by, cx, cy, ax + 2 * t * (bx - ax), ay + 2 * t * (by - ay));
            }
        }
    }

    @Test
    public void nonFiniteCoordinatesAreRejected() {
        for (double v : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            for (int i = 0; i < 8; i++) {
                double[] c = {0, 0, 2, 2, 0, 2, 2, 0};
                c[i] = v;
                assertThrows(IllegalArgumentException.class,
                        () -> Predicates.segmentIntersection(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]));
            }
            // also when a segment is degenerate
            assertThrows(IllegalArgumentException.class, () -> Predicates.segmentIntersection(1, 1, 1, 1, 0, 0, v, 2));
            assertThrows(IllegalArgumentException.class, () -> Predicates.segmentIntersection(v, 1, v, 1, 0, 0, 2, 2));
        }
    }

    // ---------------------------------------------------------------------------------------------------

    private SegmentIntersection classify(double ax, double ay, double bx, double by,
                                         double cx, double cy, double dx, double dy) {
        return Predicates.segmentIntersection(ax, ay, bx, by, cx, cy, dx, dy);
    }

    /**
     * Checks the result against the oracle, and checks that it does not depend on the order of the segments or
     * the direction of the segments.
     */
    private void check(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
        SegmentIntersection expected = oracle(ax, ay, bx, by, cx, cy, dx, dy);
        String msg = String.format("segments (%s %s, %s %s) and (%s %s, %s %s)", ax, ay, bx, by, cx, cy, dx, dy);
        assertEquals(msg, expected, classify(ax, ay, bx, by, cx, cy, dx, dy));
        assertEquals(msg + " [swapped]", expected, classify(cx, cy, dx, dy, ax, ay, bx, by));
        assertEquals(msg + " [reversed]", expected, classify(bx, by, ax, ay, dx, dy, cx, cy));
        seen.merge(expected, 1, Integer::sum);
    }

    private static SegmentIntersection oracle(double ax_, double ay_, double bx_, double by_,
                                              double cx_, double cy_, double dx_, double dy_) {
        BigDecimal ax = big(ax_), ay = big(ay_), bx = big(bx_), by = big(by_);
        BigDecimal cx = big(cx_), cy = big(cy_), dx = big(dx_), dy = big(dy_);
        BigDecimal rx = bx.subtract(ax), ry = by.subtract(ay);   // r = b - a
        BigDecimal qx = dx.subtract(cx), qy = dy.subtract(cy);   // q = d - c
        boolean rZero = rx.signum() == 0 && ry.signum() == 0;
        boolean qZero = qx.signum() == 0 && qy.signum() == 0;
        if (rZero && qZero) {
            return ax.compareTo(cx) == 0 && ay.compareTo(cy) == 0 ? TOUCHING : DISJOINT;
        }
        if (rZero) return pointOnSegment(cx, cy, qx, qy, ax, ay);
        if (qZero) return pointOnSegment(ax, ay, rx, ry, cx, cy);

        // a + t r = c + s q  <=>  t r - s q = w, with w = c - a
        BigDecimal wx = cx.subtract(ax), wy = cy.subtract(ay);
        BigDecimal u = cross(rx, ry, qx, qy);
        if (u.signum() != 0) {
            // Cramer's rule: t = D1 / U, s = D2 / U
            BigDecimal d1 = cross(wx, wy, qx, qy);
            BigDecimal d2 = cross(wx, wy, rx, ry);
            int t0 = d1.signum() * u.signum();                 // sign of t
            int t1 = d1.subtract(u).signum() * u.signum();     // sign of t - 1
            int s0 = d2.signum() * u.signum();
            int s1 = d2.subtract(u).signum() * u.signum();
            if (t0 < 0 || t1 > 0 || s0 < 0 || s1 > 0) return DISJOINT;
            if (t0 == 0 || t1 == 0 || s0 == 0 || s1 == 0) return TOUCHING;
            return CROSSING;
        }
        if (cross(wx, wy, rx, ry).signum() != 0) return DISJOINT; // parallel, on different lines
        // collinear: parameters of c and d along a + t r, scaled by r.r
        BigDecimal rr = dot(rx, ry, rx, ry);
        BigDecimal tc = dot(wx, wy, rx, ry);
        BigDecimal td = dot(dx.subtract(ax), dy.subtract(ay), rx, ry);
        BigDecimal lo = tc.min(td).max(BigDecimal.ZERO);
        BigDecimal hi = tc.max(td).min(rr);
        int cmp = lo.compareTo(hi);
        return cmp > 0 ? DISJOINT : (cmp == 0 ? TOUCHING : OVERLAPPING);
    }

    // p on segment from a with direction r (r != 0)?
    private static SegmentIntersection pointOnSegment(BigDecimal ax, BigDecimal ay, BigDecimal rx, BigDecimal ry,
                                                      BigDecimal px, BigDecimal py) {
        BigDecimal wx = px.subtract(ax), wy = py.subtract(ay);
        if (cross(wx, wy, rx, ry).signum() != 0) return DISJOINT;
        BigDecimal t = dot(wx, wy, rx, ry);
        return t.signum() >= 0 && t.compareTo(dot(rx, ry, rx, ry)) <= 0 ? TOUCHING : DISJOINT;
    }

    private static BigDecimal cross(BigDecimal ax, BigDecimal ay, BigDecimal bx, BigDecimal by) {
        return ax.multiply(by).subtract(ay.multiply(bx));
    }

    private static BigDecimal dot(BigDecimal ax, BigDecimal ay, BigDecimal bx, BigDecimal by) {
        return ax.multiply(bx).add(ay.multiply(by));
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }

    private double rnd(double scale) {
        return (random.nextDouble() * 2 - 1) * scale;
    }

    private double p(double pert) {
        return (random.nextDouble() * 2 - 1) * pert;
    }
}
