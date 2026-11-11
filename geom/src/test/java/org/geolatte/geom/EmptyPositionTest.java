package org.geolatte.geom;

import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;

/**
 * Position-valued accessors on empty geometries return an empty {@code Position}; the geometry-valued
 * accessors {@code getStartPoint()} and {@code getEndPoint()} return an empty {@code Point}, as
 * ST_StartPoint and ST_EndPoint in ISO/IEC 13249-3 denote the empty set.
 */
public class EmptyPositionTest {

    private final LineString<C3DM> emptyLine = new LineString<>(crsZM);
    private final MultiLineString<C3DM> emptyMultiLine = new MultiLineString<>(crsZM);
    private final Point<C3DM> emptyPoint = new Point<>(crsZM);
    private final LineString<C3DM> line = linestring(crsZM, c(0, 0, 1, 10), c(1, 0, 2, 11), c(1, 1, 3, 12));
    private final MultiLineString<C3DM> multiLine = multilinestring(line, linestring(crsZM, c(5, 5, 4, 13), c(6, 6, 5, 14)));

    @Test
    public void emptyLineHasEmptyStartAndEndPosition() {
        assertTrue(emptyLine.getStartPosition().isEmpty());
        assertTrue(emptyLine.getEndPosition().isEmpty());
        assertEquals(C3DM.class, emptyLine.getStartPosition().getClass());
        assertTrue(Double.isNaN(emptyLine.getStartPosition().getX()));
    }

    @Test
    public void emptyMultiLineHasEmptyStartAndEndPosition() {
        assertTrue(emptyMultiLine.getStartPosition().isEmpty());
        assertTrue(emptyMultiLine.getEndPosition().isEmpty());
        assertEquals(C3DM.class, emptyMultiLine.getEndPosition().getClass());
    }

    @Test
    public void emptyPointHasEmptyPosition() {
        assertTrue(emptyPoint.getPosition().isEmpty());
        assertEquals(C3DM.class, emptyPoint.getPosition().getClass());
        assertTrue(new Point<>(crs).getPosition().isEmpty());
    }

    @Test
    public void startAndEndPointOfEmptyLinearGeometriesAreEmptyPoints() {
        for (Linear<C3DM> linear : new Linear[]{emptyLine, emptyMultiLine}) {
            assertTrue(linear.getStartPoint().isEmpty());
            assertTrue(linear.getEndPoint().isEmpty());
            assertEquals(crsZM, linear.getStartPoint().getCoordinateReferenceSystem());
            assertEquals(crsZM, linear.getEndPoint().getCoordinateReferenceSystem());
        }
        assertEquals("POINT ZM EMPTY", org.geolatte.geom.codec.Wkt.toWkt(emptyLine.getStartPoint(), org.geolatte.geom.codec.Wkt.Dialect.SFA_1_2_1));
    }

    @Test
    public void startAndEndPointIncludeZAndM() {
        assertEquals(point(crsZM, c(0, 0, 1, 10)), line.getStartPoint());
        assertEquals(point(crsZM, c(1, 1, 3, 12)), line.getEndPoint());
        assertEquals(point(crsZM, c(0, 0, 1, 10)), multiLine.getStartPoint());
        assertEquals(point(crsZM, c(6, 6, 5, 14)), multiLine.getEndPoint());
        assertEquals(line.getStartPosition(), line.getStartPoint().getPosition());
    }

    @Test
    public void emptyPositionsEqualOnlyEmptyPositionsOfTheSameClass() {
        assertEquals(new C2D(), new C2D());
        assertEquals(new C2D().hashCode(), new C2D().hashCode());
        assertNotEquals(new C2D(), new G2D());
        assertNotEquals(new C2D(), new C3D());
        assertNotEquals(new C2D(), new C2D(1, 2));
        assertNotEquals(new C2D(1, 2), new C2D());
        assertEquals(emptyLine.getStartPosition(), emptyLine.getEndPosition());
    }

    @Test
    public void equals2DTreatsEmptyPositionsAsEqual() {
        assertTrue(Positions.equals2D(new C2D(), new C2D()));
        assertTrue(Positions.equals2D(new C2D(), new C3DM()));
        assertFalse(Positions.equals2D(new C2D(), new C2D(1, 2)));
        assertFalse(Positions.equals2D(new C2D(1, 2), new C2D()));
        assertTrue(Positions.equals2D(new C2D(1, 2), new C3D(1, 2, 3)));
    }

    @Test
    public void emptyLineIsNotClosed() {
        assertFalse(emptyLine.isClosed());
        assertFalse(emptyLine.isRing());
        assertFalse(emptyMultiLine.isClosed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void variableSizedBuilderRejectsEmptyPosition() {
        PositionSequenceBuilders.variableSized(C2D.class).add(new C2D());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fixedSizedBuilderRejectsEmptyPosition() {
        PositionSequenceBuilders.fixedSized(1, C2D.class).add(new C2D());
    }

    @Test
    public void emptyPositionCannotLeakIntoAGeometryAsAVertex() {
        try {
            linestring(crs, new C2D(), c(1, 1));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // ok
        }
        // but it does make an empty Point
        assertTrue(new Point<>(new C2D(), crs).isEmpty());
    }

    @Test
    public void indexedAccessorsReportTheIndexAndSize() {
        try {
            line.getPositionN(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 3 out of bounds for a geometry with 3 positions", e.getMessage());
        }
        try {
            emptyLine.getPositionN(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 0 out of bounds for a geometry with 0 positions", e.getMessage());
        }
        try {
            multiLine.getGeometryN(2);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 2 out of bounds for a collection with 2 elements", e.getMessage());
        }
        Polygon<C2D> polygon = polygon(crs, ring(c(0, 0), c(1, 0), c(1, 1), c(0, 0)));
        try {
            polygon.getInteriorRingN(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 0 out of bounds for a polygon with 0 interior rings", e.getMessage());
        }
        try {
            new Polygon<>(crs).getInteriorRingN(0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertEquals("Index 0 out of bounds for a polygon with 0 interior rings", e.getMessage());
        }
    }
}
