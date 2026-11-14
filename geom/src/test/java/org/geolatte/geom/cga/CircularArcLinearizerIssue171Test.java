package org.geolatte.geom.cga;

import org.geolatte.geom.C2D;
import org.geolatte.geom.C3D;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for issue #171: linearizing arcs through nearly collinear positions caused an OutOfMemoryError.
 */
public class CircularArcLinearizerIssue171Test {

    private static final double THRESHOLD = 0.0001;

    // the arc from issue #171 (projected coordinates). Its radius is about 1.7e12 m.
    private static final C2D P0 = new C2D(33375945.536, 6017549.333);
    private static final C2D P1 = new C2D(33375937.816, 6017570.934);
    private static final C2D P2 = new C2D(33375930.096, 6017592.535);

    @Test(timeout = 5000)
    public void testNearlyCollinearArcIsNotLinearized() {
        PositionSequence<C2D> result = new CircularArcLinearizer<>(P0, P1, P2, THRESHOLD).linearize();
        assertEquals(3, result.size());
        assertEquals(P0, result.getPositionN(0));
        assertEquals(P1, result.getPositionN(1));
        assertEquals(P2, result.getPositionN(2));
    }

    @Test(timeout = 5000)
    public void testNearlyCollinearArcInReverseDirectionIsNotLinearized() {
        PositionSequence<C2D> result = new CircularArcLinearizer<>(P2, P1, P0, THRESHOLD).linearize();
        assertEquals(3, result.size());
        assertEquals(P2, result.getPositionN(0));
        assertEquals(P1, result.getPositionN(1));
        assertEquals(P0, result.getPositionN(2));
    }

    @Test(timeout = 5000)
    public void testNearlyCollinearArc3DIsNotLinearized() {
        C3D p0 = new C3D(33375945.536, 6017549.333, 0);
        C3D p1 = new C3D(33375937.816, 6017570.934, 0);
        C3D p2 = new C3D(33375930.096, 6017592.535, 0);
        PositionSequence<C3D> result = new CircularArcLinearizer<>(p0, p1, p2, THRESHOLD).linearize();
        assertEquals(3, result.size());
        assertEquals(p0, result.getPositionN(0));
        assertEquals(p1, result.getPositionN(1));
        assertEquals(p2, result.getPositionN(2));
    }

    @Test(timeout = 5000, expected = IllegalArgumentException.class)
    public void testArcRequiringTooManyPointsThrowsException() {
        // nearly collinear, but p1 does not lie between p0 and p2: almost the full circle with a radius of about 1e9
        new CircularArcLinearizer<>(new C2D(0, 0), new C2D(2, 1e-9), new C2D(1, 0), THRESHOLD).linearize();
    }

    @Test
    public void testSemiCircle() {
        double r = 10;
        C2D p0 = new C2D(r, 0);
        C2D p1 = new C2D(0, r);
        C2D p2 = new C2D(-r, 0);
        PositionSequence<C2D> result = new CircularArcLinearizer<>(p0, p1, p2, THRESHOLD).linearize();
        assertEquals(expectedSize(PI / 2, PI / 2, r), result.size());
        assertEquals(p0, result.getPositionN(0));
        assertEquals(p2, result.getPositionN(result.size() - 1));
        verifyOnArc(result, 0, 0, r);
    }

    @Test
    public void testArcLargerThanPiWithShortChord() {
        // p0 and p2 are close to each other, so the sagitta of the chord p0-p2 is below threshold,
        // but the arc runs almost around the full circle and must be linearized
        double r = 10;
        C2D p0 = new C2D(r * Math.cos(-0.001), r * Math.sin(-0.001));
        C2D p1 = new C2D(-r, 0);
        C2D p2 = new C2D(r * Math.cos(0.001), r * Math.sin(0.001));
        PositionSequence<C2D> result = new CircularArcLinearizer<>(p0, p1, p2, THRESHOLD).linearize();
        assertEquals(expectedSize(PI - 0.001, PI - 0.001, r), result.size());
        assertEquals(p0, result.getPositionN(0));
        assertEquals(p2, result.getPositionN(result.size() - 1));
        verifyOnArc(result, 0, 0, r);
    }

    @Test
    public void testNormalArcStepCountUnchanged() {
        // the arc used in CircularArcLinearizerTest.testArcClockwise() (circle with center (20, 20) and radius 10)
        C2D p0 = new C2D(29.8128, 21.9259);
        C2D p1 = new C2D(28.6305, 14.9489);
        C2D p2 = new C2D(23.6725, 10.6988);
        CircularArcLinearizer<C2D> linearizer = new CircularArcLinearizer<>(p0, p1, p2, THRESHOLD);
        Circle c = linearizer.getCircle();
        double a0 = Math.atan2(p0.getY() - c.y, p0.getX() - c.x);
        double a1 = Math.atan2(p1.getY() - c.y, p1.getX() - c.x);
        double a2 = Math.atan2(p2.getY() - c.y, p2.getX() - c.x);
        PositionSequence<C2D> result = linearizer.linearize();
        assertEquals(expectedSize(Math.abs(a0 - a1), Math.abs(a1 - a2), c.radius), result.size());
        verifyOnArc(result, c.x, c.y, c.radius);
    }

    @Test
    public void testNormalArcUnchanged() {
        // expected positions were generated with the implementation before the fix for issue #171
        C2D p0 = new C2D(29.8128, 21.9259);
        C2D p1 = new C2D(28.6305, 14.9489);
        C2D p2 = new C2D(23.6725, 10.6988);
        PositionSequence<C2D> result = new CircularArcLinearizer<>(p0, p1, p2, 0.1).linearize();
        assertPositions(NORMAL_ARC_EXPECTED, result);
        result = new CircularArcLinearizer<>(p2, p1, p0, 0.1).linearize();
        assertPositions(NORMAL_ARC_REVERSED_EXPECTED, result);
    }

    @Test
    public void testSmallArcInGeographicCoordinates() {
        // radius of 0.001 degrees, the sagitta of this quarter circle is about 0.0003 > threshold
        double cx = 7.123456789;
        double cy = 51.123456789;
        double r = 0.001;
        C2D p0 = new C2D(cx + r, cy);
        C2D p1 = new C2D(cx + r * Math.cos(PI / 4), cy + r * Math.sin(PI / 4));
        C2D p2 = new C2D(cx, cy + r);
        PositionSequence<C2D> result = new CircularArcLinearizer<>(p0, p1, p2, THRESHOLD).linearize();
        assertTrue(result.size() > 3);
        assertEquals(p0, result.getPositionN(0));
        assertEquals(p2, result.getPositionN(result.size() - 1));
        verifyOnArc(result, cx, cy, r);
    }

    @Test
    public void testCircleWithRadiusBelowThresholdInGeographicCoordinates() {
        // threshold > 2 * radius, previously the angle increment was NaN
        double cx = 7.123456789;
        double cy = 51.123456789;
        double r = 0.00002;
        C2D p0 = new C2D(cx + r, cy);
        C2D p1 = new C2D(cx, cy + r);
        C2D p2 = new C2D(cx - r, cy);
        PositionSequence<C2D> result = new CircularArcLinearizer<>(p0, p1, p2, THRESHOLD).linearizeCircle();
        assertEquals(p0, result.getPositionN(0));
        assertEquals(p0, result.getPositionN(result.size() - 1));
        result = new CircularArcLinearizer<>(p0, p1, p2, THRESHOLD).linearize();
        assertEquals(3, result.size());
    }

    private static final double PI = Math.PI;

    // number of positions returned for an arc consisting of two sub-arcs with the specified angles
    private static int expectedSize(double angle01, double angle12, double radius) {
        double maxAngleIncr = Math.acos((radius - THRESHOLD) / radius);
        int steps01 = (int) Math.ceil(angle01 / maxAngleIncr);
        int steps12 = (int) Math.ceil(angle12 / maxAngleIncr);
        return 3 + (steps01 - 1) + (steps12 - 1);
    }

    // verifies that all positions lie on the circle and that the segments deviate at most threshold from the arc
    private static void verifyOnArc(PositionSequence<C2D> seq, double cx, double cy, double r) {
        C2D prev = null;
        for (C2D co : seq) {
            assertEquals(r, Math.hypot(co.getX() - cx, co.getY() - cy), 1e-9);
            if (prev != null) {
                double mx = (prev.getX() + co.getX()) / 2;
                double my = (prev.getY() + co.getY()) / 2;
                assertTrue(r - Math.hypot(mx - cx, my - cy) <= THRESHOLD + 1e-9);
            }
            prev = co;
        }
    }

    private static void assertPositions(double[][] expected, PositionSequence<? extends Position> actual) {
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i][0], actual.getPositionN(i).getCoordinate(0), 1e-9);
            assertEquals(expected[i][1], actual.getPositionN(i).getCoordinate(1), 1e-9);
        }
    }

    private static final double[][] NORMAL_ARC_EXPECTED = {
            {29.8128, 21.9259},
            {29.97317876249179, 20.731833944118655},
            {29.988796719000966, 19.527146776136483},
            {29.859427177499185, 18.32932432894406},
            {29.58694791525839, 17.155752795157074},
            {29.17531392322753, 16.02346636943646},
            {28.6305, 14.9489},
            {27.884174765889213, 13.84866594423403},
            {26.998496337425042, 12.857156966013966},
            {25.98911888244359, 11.991897764437391},
            {24.873882921366768, 11.268181602078965},
            {23.6725, 10.6988}
    };

    private static final double[][] NORMAL_ARC_REVERSED_EXPECTED = {
            {23.6725, 10.6988},
            {24.87388292136677, 11.268181602078963},
            {25.989118882443595, 11.991897764437391},
            {26.998496337425046, 12.857156966013967},
            {27.884174765889213, 13.848665944234032},
            {28.6305, 14.9489},
            {29.175313923227527, 16.02346636943646},
            {29.586947915258385, 17.155752795157074},
            {29.85942717749918, 18.32932432894406},
            {29.98879671900096, 19.527146776136483},
            {29.973178762491788, 20.731833944118655},
            {29.8128, 21.9259}
    };
}
