package org.geolatte.geom;

import org.junit.Test;

import static org.geolatte.geom.SegmentIntersection.*;
import static org.junit.Assert.*;

public class LineSegmentTest {

    private static LineSegment<C2D> seg(double x0, double y0, double x1, double y1) {
        return new LineSegment<>(new C2D(x0, y0), new C2D(x1, y1));
    }

    @Test
    public void classifyIntersection() {
        LineSegment<C2D> s = seg(0, 0, 10, 10);
        assertEquals(CROSSING, s.classifyIntersection(seg(0, 10, 10, 0)));
        assertEquals(TOUCHING, s.classifyIntersection(seg(10, 10, 20, 0)));
        assertEquals(TOUCHING, s.classifyIntersection(seg(5, 5, 20, 0)));
        assertEquals(OVERLAPPING, s.classifyIntersection(seg(5, 5, 20, 20)));
        assertEquals(DISJOINT, s.classifyIntersection(seg(11, 11, 20, 20)));
        assertEquals(DISJOINT, s.classifyIntersection(seg(0, 1, 10, 11)));
    }

    @Test
    public void intersects() {
        LineSegment<C2D> s = seg(0, 0, 10, 10);
        assertTrue(s.intersects(seg(0, 10, 10, 0)));
        assertTrue(s.intersects(seg(10, 10, 20, 0)));
        assertFalse(s.intersects(seg(0, 1, 10, 11)));
    }

    /**
     * An endpoint that lies 1 ulp off the other segment: floating point computation of the intersection point
     * typically reports these as intersecting.
     */
    @Test
    public void nearlyTouchingSegments() {
        double y = Math.nextUp(200000.1);
        LineSegment<C2D> base = seg(150000.1, 200000.1, 150500.1, 200000.1);
        assertEquals(DISJOINT, base.classifyIntersection(seg(150250.1, y, 150250.1, 200100.1)));
        assertEquals(TOUCHING, base.classifyIntersection(seg(150250.1, 200000.1, 150250.1, 200100.1)));
        assertEquals(CROSSING, base.classifyIntersection(seg(150250.1, Math.nextDown(200000.1), 150250.1, 200100.1)));
    }

    @Test
    public void worksAcrossPositionTypes() {
        LineSegment<G2D> geographic = new LineSegment<>(new G2D(3.0, 51.0), new G2D(4.0, 52.0));
        LineSegment<C2D> projected = seg(3.0, 52.0, 4.0, 51.0);
        assertEquals(CROSSING, geographic.classifyIntersection(projected));
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyPositionsAreRejected() {
        seg(0, 0, 1, 1).classifyIntersection(new LineSegment<>(new C2D(), new C2D(1, 0)));
    }
}
