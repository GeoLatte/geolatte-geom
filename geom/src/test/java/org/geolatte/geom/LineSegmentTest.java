package org.geolatte.geom;

import org.junit.Test;

import java.util.Optional;

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

    @Test
    public void intersectionOfCrossingSegments() {
        SegmentIntersectionResult<C2D> r = seg(0, 0, 10, 10).intersection(seg(0, 10, 10, 0));
        assertEquals(CROSSING, r.kind());
        assertEquals(new C2D(5, 5), r.point().get());
        assertFalse(r.overlap().isPresent());
    }

    @Test
    public void crossingPointIsCorrectlyRoundedAndIndependentOfOrder() {
        LineSegment<C2D> s = seg(0, 0, 3, 1);
        LineSegment<C2D> o = seg(0, 1, 1, 0);
        // exact crossing point: (3/4, 1/4)
        assertEquals(new C2D(0.75, 0.25), s.intersection(o).point().get());
        LineSegment<C2D> s2 = seg(0.1, 0.2, 7.3, 3.1);
        LineSegment<C2D> o2 = seg(0.3, 2.9, 6.1, 0.7);
        C2D p = s2.intersection(o2).point().get();
        assertEquals(p, o2.intersection(s2).point().get());
        assertEquals(p, reverse(s2).intersection(reverse(o2)).point().get());
    }

    @Test
    public void touchingReturnsExistingEndpoint() {
        LineSegment<C2D> s = seg(0, 0, 10, 10);
        SegmentIntersectionResult<C2D> atOwnEndpoint = s.intersection(seg(10, 10, 20, 0));
        assertEquals(TOUCHING, atOwnEndpoint.kind());
        assertSame(s.getEndPosition(), atOwnEndpoint.point().get());

        double y = 200000.1;
        LineSegment<C2D> base = seg(150000.1, y, 150500.1, y);
        SegmentIntersectionResult<C2D> atOtherEndpoint = base.intersection(seg(150250.1, y, 150250.1, 200100.1));
        assertEquals(TOUCHING, atOtherEndpoint.kind());
        assertEquals(new C2D(150250.1, y), atOtherEndpoint.point().get());
    }

    @Test
    public void overlapFollowsDirectionOfReceiver() {
        LineSegment<C2D> s = seg(0, 0, 10, 10);
        LineSegment<C2D> o = seg(20, 20, 5, 5);
        LineSegment<C2D> overlap = s.intersection(o).overlap().get();
        assertEquals(new C2D(5, 5), overlap.getStartPosition());
        assertSame(s.getEndPosition(), overlap.getEndPosition());

        SegmentIntersectionResult<C2D> reversed = reverse(s).intersection(o);
        assertEquals(OVERLAPPING, reversed.kind());
        assertFalse(reversed.point().isPresent());
        assertEquals(new C2D(10, 10), reversed.overlap().get().getStartPosition());
        assertEquals(new C2D(5, 5), reversed.overlap().get().getEndPosition());
    }

    @Test
    public void disjointHasNoPoint() {
        SegmentIntersectionResult<C2D> r = seg(0, 0, 10, 10).intersection(seg(0, 1, 10, 11));
        assertEquals(DISJOINT, r.kind());
        assertEquals(Optional.empty(), r.point());
        assertEquals(Optional.empty(), r.overlap());
    }

    @Test
    public void zAndMAreInterpolatedAlongReceiver() {
        LineSegment<C3DM> s = new LineSegment<>(new C3DM(0, 0, 0, 100), new C3DM(10, 0, 10, 200));
        assertEquals(new C3DM(4, 0, 4, 140), s.intersection(seg(4, -1, 4, 1)).point().get());
        // touching at an endpoint of the other segment
        assertEquals(new C3DM(5, 0, 5, 150), s.intersection(seg(5, 0, 5, 5)).point().get());
        // overlap with an endpoint of the other segment
        LineSegment<C3DM> overlap = s.intersection(seg(2, 0, 20, 0)).overlap().get();
        assertEquals(new C3DM(2, 0, 2, 120), overlap.getStartPosition());
        assertSame(s.getEndPosition(), overlap.getEndPosition());
        // receiver without Z: Z of the other segment is ignored
        LineSegment<C3D> other = new LineSegment<>(new C3D(4, -1, 7), new C3D(4, 1, 7));
        assertEquals(new C2D(4, 0), seg(0, 0, 10, 0).intersection(other).point().get());
    }

    @Test
    public void resultHasPositionTypeOfReceiver() {
        LineSegment<G2D> geographic = new LineSegment<>(new G2D(3.0, 51.0), new G2D(4.0, 52.0));
        SegmentIntersectionResult<G2D> r = geographic.intersection(seg(3.0, 52.0, 4.0, 51.0));
        assertEquals(new G2D(3.5, 51.5), r.point().get());
    }

    private static <P extends Position> LineSegment<P> reverse(LineSegment<P> s) {
        return new LineSegment<>(s.getEndPosition(), s.getStartPosition());
    }
}
