package org.geolatte.geom.cga.exact;

import org.geolatte.geom.C2D;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.PositionSequenceBuilder;
import org.geolatte.geom.PositionSequenceBuilders;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests the predicates, and each of their evaluation stages separately, against independent
 * {@code BigDecimal} oracles.
 */
public class PredicatesTest {

    private final Random random = new Random(1234L);

    // ---------------------------------------------------------------------------------------------------
    // Orientation
    // ---------------------------------------------------------------------------------------------------

    @Test
    public void orientationOfSimpleCases() {
        assertEquals(1, Predicates.orientation(0, 0, 1, 0, 0, 1));
        assertEquals(-1, Predicates.orientation(0, 0, 0, 1, 1, 0));
        assertEquals(0, Predicates.orientation(0, 0, 1, 1, 2, 2));
        assertEquals(0, Predicates.orientation(1, 1, 1, 1, 1, 1));
    }

    /**
     * Near-collinear points in typical projected coordinates: plain floating point evaluation gets most of these
     * wrong.
     */
    @Test
    public void orientationNearCollinearProjected() {
        Counts counts = new Counts();
        for (int k = 0; k < 100_000; k++) {
            double ax = 150000 + random.nextDouble() * 1000, ay = 200000 + random.nextDouble() * 1000;
            double bx = ax + random.nextDouble() * 500, by = ay + random.nextDouble() * 500;
            double t = random.nextDouble() * 3 - 1;
            double cx = ax + t * (bx - ax), cy = ay + t * (by - ay);
            checkOrientation(counts, ax, ay, bx, by, cx, cy);
        }
        assertTrue(counts.undecided > 100);
    }

    /**
     * The classroom example of Kettner et al. ("Classroom examples of robustness problems in geometric
     * computations", 2008): p = (0.5 + i u, 0.5 + j u), q = (12, 12), r = (24, 24)
     */
    @Test
    public void orientationKettnerGrid() {
        Counts counts = new Counts();
        double u = Math.ulp(0.5);
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                checkOrientation(counts, 0.5 + i * u, 0.5 + j * u, 12, 12, 24, 24);
                checkOrientation(counts, 0.5 + i * u, 0.5 + j * u, 8.800000000000001, 8.800000000000001, 12.1, 12.1);
            }
        }
        assertTrue(counts.undecided > 0);
    }

    @Test
    public void orientationNearDegenerateAllScales() {
        Counts counts = new Counts();
        for (int k = 0; k < 100_000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(300) - 150);
            double ax = rnd(scale), ay = rnd(scale), bx = rnd(scale), by = rnd(scale);
            double t = random.nextDouble() * 4 - 2;
            double cx = ax + t * (bx - ax), cy = ay + t * (by - ay);
            if (k % 3 == 0) cx = Math.nextUp(cx);
            if (k % 7 == 0) cy = Math.nextDown(cy);
            checkOrientation(counts, ax, ay, bx, by, cx, cy);
        }
        assertTrue(counts.undecided > 1000);
    }

    @Test
    public void orientationExactlyCollinear() {
        Counts counts = new Counts();
        for (int k = 0; k < 10_000; k++) {
            double ox = random.nextInt(1 << 20) * 0.25, oy = random.nextInt(1 << 20) * 0.25;
            double dx = random.nextInt(1000) - 500, dy = random.nextInt(1000) - 500;
            int i = random.nextInt(100) - 50, j = random.nextInt(100) - 50;
            checkOrientation(counts, ox, oy, ox + i * dx, oy + i * dy, ox + j * dx, oy + j * dy);
            assertEquals(0, Predicates.orientation(ox, oy, ox + i * dx, oy + i * dy, ox + j * dx, oy + j * dy));
        }
    }

    @Test
    public void orientationExtremeMagnitudesUseFallback() {
        Counts counts = new Counts();
        for (double scale : new double[]{1e-170, 1e-160, 1e160, 1e200, 1e300}) {
            for (int k = 0; k < 2_000; k++) {
                double ax = rnd(scale), ay = rnd(scale), bx = rnd(scale), by = rnd(scale);
                double t = random.nextDouble();
                double cx = ax + t * (bx - ax), cy = ay + t * (by - ay);
                checkOrientation(counts, ax, ay, bx, by, cx, cy);
            }
        }
        assertTrue(counts.inexact > 0);
    }

    /**
     * Near-collinear, exactly collinear and axis-aligned positions, at all scales from subnormal (where products
     * underflow) up to 2^100.
     */
    @Test
    public void orientationAdversarialAllScales() {
        Counts counts = new Counts();
        for (int k = 0; k < 100_000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(1200) - 1100);
            double ax = rnd(scale), ay = rnd(scale), bx = rnd(scale), by = rnd(scale);
            double t = random.nextDouble() * 4 - 2;
            double cx = ax + t * (bx - ax), cy = ay + t * (by - ay);
            switch (k % 5) {
                case 0: cx = Math.nextUp(cx); break;
                case 1: cy = Math.nextDown(cy); break;
                case 2: cx = ax; bx = ax; break;                               // vertical
                case 3: cy = ay; by = random.nextBoolean() ? ay : by; break;   // horizontal, or nearly so
                default: break;
            }
            checkOrientation(counts, ax, ay, bx, by, cx, cy);
        }
        assertTrue(counts.boundUndecided > 1000);
        assertTrue(counts.inexact > 1000);
    }

    @Test
    public void orientationErrorBoundDecidesAxisAlignedCollinear() {
        assertEquals(0, Predicates.orientationErrorBound(150000.1, 200000.1, 150000.1, 200500.7, 150000.1, 199000.3));
        assertEquals(0, Predicates.orientationErrorBound(150000.1, 200000.1, 150500.7, 200000.1, 149000.3, 200000.1));
        assertEquals(0, Predicates.orientationErrorBound(1, 2, 1, 2, 5, 7));
        assertEquals(-1, Predicates.orientationErrorBound(0, 0, 0, 1, 1e-300, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void orientationRejectsNaN() {
        Predicates.orientation(0, 0, 1, Double.NaN, 2, 2);
    }

    private void checkOrientation(Counts counts, double ax, double ay, double bx, double by, double cx, double cy) {
        int expected = det(new double[][]{{ax, ay, 1}, {bx, by, 1}, {cx, cy, 1}}).signum();
        String msg = String.format("orientation(%s, %s, %s, %s, %s, %s)", ax, ay, bx, by, cx, cy);
        assertEquals(msg, expected, Predicates.orientation(ax, ay, bx, by, cx, cy));
        int bound = Predicates.orientationErrorBound(ax, ay, bx, by, cx, cy);
        if (bound == Predicates.UNDECIDED) {
            counts.boundUndecided++;
        } else {
            assertEquals(msg + " [error bound]", expected, bound);
        }
        int filter = Predicates.orientationFilter(ax, ay, bx, by, cx, cy);
        if (filter == Predicates.UNDECIDED) {
            counts.undecided++;
        } else {
            assertEquals(msg + " [filter]", expected, filter);
        }
        ExactSum terms = Predicates.orientationTerms(ax, ay, bx, by, cx, cy);
        if (terms.isExact()) {
            assertEquals(msg + " [essa]", expected, terms.signum());
        } else {
            counts.inexact++;
        }
        assertEquals(msg + " [big]", expected, Predicates.orientationBig(ax, ay, bx, by, cx, cy));
    }

    // ---------------------------------------------------------------------------------------------------
    // In-circle
    // ---------------------------------------------------------------------------------------------------

    @Test
    public void inCircleSimpleCases() {
        // a, b, c counterclockwise on the unit circle
        assertEquals(1, Predicates.inCircle(1, 0, 0, 1, -1, 0, 0, 0));
        assertEquals(-1, Predicates.inCircle(1, 0, 0, 1, -1, 0, 2, 2));
        assertEquals(0, Predicates.inCircle(1, 0, 0, 1, -1, 0, 0, -1));
        // clockwise: signs reversed
        assertEquals(-1, Predicates.inCircle(-1, 0, 0, 1, 1, 0, 0, 0));
    }

    @Test
    public void inCircleNearCocircular() {
        Counts counts = new Counts();
        for (int k = 0; k < 20_000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(120) - 60);
            double cx = rnd(scale) * 1000, cy = rnd(scale) * 1000, r = scale * (0.5 + random.nextDouble());
            double[] p = new double[8];
            for (int i = 0; i < 4; i++) {
                double theta = random.nextDouble() * 2 * Math.PI;
                p[2 * i] = cx + r * Math.cos(theta);
                p[2 * i + 1] = cy + r * Math.sin(theta);
            }
            checkInCircle(counts, p);
        }
        assertTrue(counts.undecided > 100);
    }

    @Test
    public void inCircleExactlyCocircular() {
        double[][] pts = {{5, 0}, {0, 5}, {-5, 0}, {0, -5}, {3, 4}, {4, 3}, {-3, 4}, {4, -3}, {-4, -3}};
        Counts counts = new Counts();
        for (int k = 0; k < 5_000; k++) {
            double ox = random.nextInt(1 << 24), oy = random.nextInt(1 << 24);
            double f = Math.scalb(1.0, random.nextInt(20) - 10);
            double[] p = new double[8];
            for (int i = 0; i < 4; i++) {
                double[] q = pts[random.nextInt(pts.length)];
                p[2 * i] = (ox + q[0]) * f;
                p[2 * i + 1] = (oy + q[1]) * f;
            }
            checkInCircle(counts, p);
            assertEquals(0, Predicates.inCircle(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        }
        // with directed rounding, exact zeros remain point intervals [0, 0], so the filter decides
        assertEquals(0, counts.undecided);
    }

    @Test
    public void inCircleExtremeMagnitudesUseFallback() {
        Counts counts = new Counts();
        for (double scale : new double[]{1e-80, 1e-60, 1e70, 1e100}) {
            for (int k = 0; k < 500; k++) {
                double cx = rnd(scale), cy = rnd(scale), r = scale;
                double[] p = new double[8];
                for (int i = 0; i < 4; i++) {
                    double theta = random.nextDouble() * 2 * Math.PI;
                    p[2 * i] = cx + r * Math.cos(theta);
                    p[2 * i + 1] = cy + r * Math.sin(theta);
                }
                checkInCircle(counts, p);
            }
        }
        assertTrue(counts.inexact > 0);
    }

    private void checkInCircle(Counts counts, double[] p) {
        double[][] m = new double[4][];
        for (int i = 0; i < 4; i++) {
            double x = p[2 * i], y = p[2 * i + 1];
            m[i] = new double[]{x, y, Double.NaN, 1};
        }
        int expected = detLifted(m).signum();
        String msg = "inCircle" + java.util.Arrays.toString(p);
        assertEquals(msg, expected, Predicates.inCircle(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        int filter = Predicates.inCircleFilter(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        if (filter == Predicates.UNDECIDED) {
            counts.undecided++;
        } else {
            assertEquals(msg + " [filter]", expected, filter);
        }
        ExactSum terms = Predicates.inCircleTerms(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        if (terms.isExact()) {
            assertEquals(msg + " [essa]", expected, terms.signum());
        } else {
            counts.inexact++;
        }
        assertEquals(msg + " [big]", expected, Predicates.inCircleBig(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
    }

    // ---------------------------------------------------------------------------------------------------
    // Ring orientation
    // ---------------------------------------------------------------------------------------------------

    @Test
    public void ringOrientationSimpleCases() {
        assertEquals(1, Predicates.ringOrientation(new double[]{0, 1, 1, 0, 0}, new double[]{0, 0, 1, 1, 0}, 5));
        assertEquals(-1, Predicates.ringOrientation(new double[]{0, 0, 1, 1, 0}, new double[]{0, 1, 1, 0, 0}, 5));
        // unclosed ring
        assertEquals(1, Predicates.ringOrientation(new double[]{0, 1, 1, 0}, new double[]{0, 0, 1, 1}, 4));
        assertEquals(0, Predicates.ringOrientation(new double[]{0, 1, 2, 0}, new double[]{0, 1, 2, 0}, 4));
        assertEquals(0, Predicates.ringOrientation(new double[]{0, 1}, new double[]{0, 1}, 2));
    }

    @Test
    public void ringOrientationOfPositionSequence() {
        PositionSequence<C2D> seq = PositionSequenceBuilders.variableSized(C2D.class)
                .add(150000.1, 200000.1).add(150010.1, 200000.1).add(150010.1, 200010.1).add(150000.1, 200000.1)
                .toPositionSequence();
        assertEquals(1, Predicates.ringOrientation(seq));
    }

    /**
     * Thin rings: a "triangle" with an apex very close to the base line, in projected coordinates; plus degenerate
     * rings that go back and forth along a line.
     */
    @Test
    public void ringOrientationNearDegenerate() {
        Counts counts = new Counts();
        for (int k = 0; k < 20_000; k++) {
            int n = 3 + random.nextInt(20);
            double ax = 150000 + random.nextDouble() * 1000, ay = 200000 + random.nextDouble() * 1000;
            double bx = ax + random.nextDouble() * 500, by = ay + random.nextDouble() * 500;
            double[] xs = new double[n + 1], ys = new double[n + 1];
            for (int i = 0; i < n; i++) {
                double t = random.nextDouble();
                xs[i] = ax + t * (bx - ax);
                ys[i] = ay + t * (by - ay);
                if (k % 2 == 0 && i == n / 2) xs[i] = Math.nextUp(xs[i]);
            }
            xs[n] = xs[0];
            ys[n] = ys[0];
            checkRing(counts, xs, ys, n + 1);
        }
        assertTrue(counts.undecided > 0);
    }

    /**
     * Thin rings at all scales, closed or not.
     */
    @Test
    public void ringOrientationAdversarialAllScales() {
        Counts counts = new Counts();
        for (int k = 0; k < 20_000; k++) {
            double scale = Math.scalb(1.0, random.nextInt(1100) - 1050);
            double offset = random.nextBoolean() ? 0 : scale * 1000;
            int n = 3 + random.nextInt(20);
            double ax = offset + rnd(scale), ay = offset + rnd(scale);
            double bx = ax + rnd(scale), by = ay + rnd(scale);
            boolean closed = random.nextBoolean();
            int size = closed ? n + 1 : n;
            double[] xs = new double[size], ys = new double[size];
            for (int i = 0; i < n; i++) {
                double t = random.nextDouble();
                xs[i] = ax + t * (bx - ax);
                ys[i] = ay + t * (by - ay);
                if (k % 3 == 0 && i == n / 2) ys[i] = Math.nextUp(ys[i]);
            }
            if (closed) {
                xs[n] = xs[0];
                ys[n] = ys[0];
            }
            checkRing(counts, xs, ys, size);
        }
        assertTrue(counts.boundUndecided > 1000);
    }

    @Test
    public void ringOrientationRejectsNonFinite() {
        for (double v : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            for (int i = 0; i < 8; i++) {
                double[] xs = {0, 1, 1, 0}, ys = {0, 0, 1, 1};
                if (i < 4) xs[i] = v; else ys[i - 4] = v;
                assertThrows(IllegalArgumentException.class, () -> Predicates.ringOrientation(xs, ys, 4));
            }
        }
    }

    @Test
    public void ringOrientationExtremeMagnitudes() {
        Counts counts = new Counts();
        for (int k = 0; k < 2_000; k++) {
            double scale = k % 2 == 0 ? 1e-160 : 1e200;
            double[] xs = {rnd(scale), rnd(scale), 0, 0};
            double[] ys = {rnd(scale), rnd(scale), 0, 0};
            xs[2] = xs[0] + 0.5 * (xs[1] - xs[0]);
            ys[2] = ys[0] + 0.5 * (ys[1] - ys[0]);
            xs[3] = xs[0];
            ys[3] = ys[0];
            checkRing(counts, xs, ys, 4);
        }
        assertTrue(counts.inexact > 0);
    }

    private void checkRing(Counts counts, double[] xs, double[] ys, int n) {
        // Newell's form, as oracle
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            sum = sum.add(big(xs[i]).subtract(big(xs[j])).multiply(big(ys[i]).add(big(ys[j]))));
        }
        int expected = sum.signum();
        String msg = "ring" + java.util.Arrays.toString(xs) + java.util.Arrays.toString(ys);
        assertEquals(msg, expected, Predicates.ringOrientation(xs, ys, n));
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.variableSized(C2D.class);
        for (int i = 0; i < n; i++) builder.add(xs[i], ys[i]);
        PositionSequence<C2D> seq = builder.toPositionSequence();
        assertEquals(msg + " [sequence]", expected, Predicates.ringOrientation(seq));
        int bound = Predicates.ringOrientationErrorBound(xs, ys, n);
        assertEquals(msg + " [error bound variants]", bound, Predicates.ringOrientationErrorBound(seq));
        if (bound == Predicates.UNDECIDED) {
            counts.boundUndecided++;
        } else {
            assertEquals(msg + " [error bound]", expected, bound);
        }
        int filter = Predicates.ringOrientationFilter(xs, ys, n);
        if (filter == Predicates.UNDECIDED) {
            counts.undecided++;
        } else {
            assertEquals(msg + " [filter]", expected, filter);
        }
        ExactSum terms = Predicates.ringOrientationTerms(xs, ys, n);
        if (terms.isExact()) {
            assertEquals(msg + " [essa]", expected, terms.signum());
        } else {
            counts.inexact++;
        }
        assertEquals(msg + " [big]", expected, Predicates.ringOrientationBig(xs, ys, n));
    }

    // ---------------------------------------------------------------------------------------------------
    // Distance comparison
    // ---------------------------------------------------------------------------------------------------

    @Test
    public void compareDistanceSimpleCases() {
        // segment (0,0)-(10,0)
        assertEquals(0, Predicates.compareDistanceToSegment(0, 0, 10, 0, 3, 5, 7, -5));    // both interior
        assertEquals(0, Predicates.compareDistanceToSegment(0, 0, 10, 0, -3, 4, 5, 5));    // region A vs interior
        assertEquals(0, Predicates.compareDistanceToSegment(0, 0, 10, 0, -3, 4, 13, -4));  // region A vs region B
        assertEquals(-1, Predicates.compareDistanceToSegment(0, 0, 10, 0, 5, 1, 5, 2));
        assertEquals(1, Predicates.compareDistanceToSegment(0, 0, 10, 0, -2, 0, 5, 1));
        // degenerate segment
        assertEquals(0, Predicates.compareDistanceToSegment(1, 1, 1, 1, 4, 5, 1, -4));
        assertEquals(-1, Predicates.compareDistanceToSegment(1, 1, 1, 1, 2, 2, 1, -4));
    }

    @Test
    public void compareDistanceNearTies() {
        Counts counts = new Counts();
        for (int k = 0; k < 30_000; k++) {
            double scale = k % 2 == 0 ? 1000 : Math.scalb(1.0, random.nextInt(100) - 50);
            double offset = k % 2 == 0 ? 150000 : 0;
            double ax = offset + rnd(scale), ay = offset + rnd(scale);
            double bx = ax + rnd(scale), by = ay + rnd(scale);
            double dx = bx - ax, dy = by - ay;
            double len = Math.hypot(dx, dy);
            double nx = -dy / len, ny = dx / len;
            double dist = Math.abs(rnd(scale));
            double[] p = pointAtDistance(ax, ay, bx, by, nx, ny, dist, random.nextInt(3));
            double[] q = pointAtDistance(ax, ay, bx, by, nx, ny, dist, random.nextInt(3));
            checkDistance(counts, ax, ay, bx, by, p[0], p[1], q[0], q[1]);
        }
        assertTrue(counts.undecided > 1000);
    }

    @Test
    public void compareDistanceExactTiesWithLargeOffsets() {
        Counts counts = new Counts();
        for (int k = 0; k < 5_000; k++) {
            double ox = random.nextInt(1 << 22), oy = random.nextInt(1 << 22);
            checkDistance(counts, ox, oy, ox + 10, oy, ox + 3, oy + 5, ox + 7, oy - 5);
            checkDistance(counts, ox, oy, ox + 10, oy, ox - 3, oy + 4, ox + 5, oy + 5);
            checkDistance(counts, ox, oy, ox + 10, oy, ox - 3, oy + 4, ox + 13, oy - 4);
            checkDistance(counts, ox, oy, ox + 8, oy + 6, ox + 4 - 3 * 2, oy + 3 + 4 * 2, ox + 4 + 3 * 2, oy + 3 - 4 * 2);
        }
    }

    @Test
    public void compareDistanceExtremeMagnitudes() {
        Counts counts = new Counts();
        for (int k = 0; k < 2_000; k++) {
            double scale = k % 2 == 0 ? 1e-70 : 1e60;
            double ax = rnd(scale), ay = rnd(scale), bx = ax + rnd(scale), by = ay + rnd(scale);
            double dx = bx - ax, dy = by - ay;
            double len = Math.hypot(dx, dy);
            double[] p = pointAtDistance(ax, ay, bx, by, -dy / len, dx / len, scale, random.nextInt(3));
            double[] q = pointAtDistance(ax, ay, bx, by, -dy / len, dx / len, scale, random.nextInt(3));
            checkDistance(counts, ax, ay, bx, by, p[0], p[1], q[0], q[1]);
        }
    }

    // returns a point at (approximately) distance dist from the segment, in region A (0), B (1) or interior (2)
    private double[] pointAtDistance(double ax, double ay, double bx, double by, double nx, double ny, double dist, int region) {
        double side = random.nextBoolean() ? 1 : -1;
        switch (region) {
            case 0: {
                double theta = Math.PI / 2 + random.nextDouble() * Math.PI;
                double ux = (bx - ax), uy = (by - ay), l = Math.hypot(ux, uy);
                ux /= l;
                uy /= l;
                return new double[]{ax + dist * (Math.cos(theta) * ux - Math.sin(theta) * uy),
                        ay + dist * (Math.sin(theta) * ux + Math.cos(theta) * uy)};
            }
            case 1: {
                double theta = -Math.PI / 2 + random.nextDouble() * Math.PI;
                double ux = (bx - ax), uy = (by - ay), l = Math.hypot(ux, uy);
                ux /= l;
                uy /= l;
                return new double[]{bx + dist * (Math.cos(theta) * ux - Math.sin(theta) * uy),
                        by + dist * (Math.sin(theta) * ux + Math.cos(theta) * uy)};
            }
            default: {
                double t = random.nextDouble();
                return new double[]{ax + t * (bx - ax) + side * dist * nx, ay + t * (by - ay) + side * dist * ny};
            }
        }
    }

    private void checkDistance(Counts counts, double ax, double ay, double bx, double by,
                               double px, double py, double qx, double qy) {
        BigDecimal[] dp = squaredDistance(ax, ay, bx, by, px, py);
        BigDecimal[] dq = squaredDistance(ax, ay, bx, by, qx, qy);
        int expected = dp[0].multiply(dq[1]).subtract(dq[0].multiply(dp[1])).signum();
        String msg = String.format("compareDistance(%s, %s, %s, %s, %s, %s, %s, %s)", ax, ay, bx, by, px, py, qx, qy);
        assertEquals(msg, expected, Predicates.compareDistanceToSegment(ax, ay, bx, by, px, py, qx, qy));
        assertEquals(msg + " [symmetric]", -expected, Predicates.compareDistanceToSegment(ax, ay, bx, by, qx, qy, px, py));

        Predicates.DistanceComparison cmp = new Predicates.DistanceComparison(ax, ay, bx, by,
                px, py, Predicates.region(ax, ay, bx, by, px, py),
                qx, qy, Predicates.region(ax, ay, bx, by, qx, qy));
        int filter = cmp.filter();
        if (filter == Predicates.UNDECIDED) {
            counts.undecided++;
        } else {
            assertEquals(msg + " [filter]", expected, filter);
        }
        ExactSum terms = cmp.terms();
        if (terms.isExact()) {
            assertEquals(msg + " [essa]", expected, terms.signum());
        } else {
            counts.inexact++;
        }
        assertEquals(msg + " [big]", expected, cmp.bigDecimal());
    }

    // squared distance of p to segment ab, as fraction {numerator, denominator}, via projection (Pythagoras)
    private static BigDecimal[] squaredDistance(double ax, double ay, double bx, double by, double px, double py) {
        BigDecimal dx = big(bx).subtract(big(ax)), dy = big(by).subtract(big(ay));
        BigDecimal wx = big(px).subtract(big(ax)), wy = big(py).subtract(big(ay));
        BigDecimal len2 = dx.multiply(dx).add(dy.multiply(dy));
        BigDecimal t = wx.multiply(dx).add(wy.multiply(dy));
        BigDecimal w2 = wx.multiply(wx).add(wy.multiply(wy));
        if (len2.signum() == 0 || t.signum() <= 0) {
            return new BigDecimal[]{w2, BigDecimal.ONE};
        }
        if (t.compareTo(len2) >= 0) {
            BigDecimal vx = big(px).subtract(big(bx)), vy = big(py).subtract(big(by));
            return new BigDecimal[]{vx.multiply(vx).add(vy.multiply(vy)), BigDecimal.ONE};
        }
        return new BigDecimal[]{w2.multiply(len2).subtract(t.multiply(t)), len2};
    }

    // ---------------------------------------------------------------------------------------------------
    // Oracle helpers
    // ---------------------------------------------------------------------------------------------------

    private static class Counts {
        int boundUndecided;
        int undecided;
        int inexact;
    }

    private double rnd(double scale) {
        return (random.nextDouble() * 2 - 1) * scale;
    }

    private static BigDecimal big(double v) {
        return new BigDecimal(v);
    }

    // determinant of rows (x, y, x² + y², 1), all exact
    private static BigDecimal detLifted(double[][] rows) {
        BigDecimal[][] m = new BigDecimal[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            BigDecimal x = big(rows[i][0]), y = big(rows[i][1]);
            m[i] = new BigDecimal[]{x, y, x.multiply(x).add(y.multiply(y)), BigDecimal.ONE};
        }
        return det(m);
    }

    private static BigDecimal det(double[][] rows) {
        BigDecimal[][] m = new BigDecimal[rows.length][rows.length];
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < rows.length; j++) {
                m[i][j] = big(rows[i][j]);
            }
        }
        return det(m);
    }

    // Laplace expansion along the first row
    private static BigDecimal det(BigDecimal[][] m) {
        int n = m.length;
        if (n == 1) return m[0][0];
        BigDecimal result = BigDecimal.ZERO;
        for (int c = 0; c < n; c++) {
            BigDecimal[][] minor = new BigDecimal[n - 1][n - 1];
            for (int i = 1; i < n; i++) {
                for (int j = 0, mj = 0; j < n; j++) {
                    if (j != c) minor[i - 1][mj++] = m[i][j];
                }
            }
            BigDecimal term = m[0][c].multiply(det(minor));
            result = c % 2 == 0 ? result.add(term) : result.subtract(term);
        }
        return result;
    }
}
