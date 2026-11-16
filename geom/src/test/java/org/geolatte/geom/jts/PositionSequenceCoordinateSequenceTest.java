package org.geolatte.geom.jts;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.util.AffineTransformation;

import static org.junit.Assert.*;

public class PositionSequenceCoordinateSequenceTest {

    PositionSequenceCoordinateSequence testSeq2D;
    PositionSequenceCoordinateSequence testSeq3D;
    PositionSequenceCoordinateSequence testSeq2DM;
    PositionSequenceCoordinateSequence testSeq3DM;

    private static <P extends Position> PositionSequence<P> sequence(Class<P> type, int dim, double... coordinates) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(coordinates.length / dim, type);
        for (int i = 0; i < coordinates.length; i += dim) {
            builder.add(java.util.Arrays.copyOfRange(coordinates, i, i + dim));
        }
        return builder.toPositionSequence();
    }

    @Before
    public void setUp() {
        testSeq2D = new PositionSequenceCoordinateSequence(sequence(C2D.class, 2, 0, 0, 1, -1, 2, -2));
        testSeq3D = new PositionSequenceCoordinateSequence(sequence(C3D.class, 3, 0, 0, 0, 1, -1, 1, 2, -2, 2));
        testSeq2DM = new PositionSequenceCoordinateSequence(sequence(C2DM.class, 3, 0, 0, 0, 1, -1, 1, 2, -2, 2));
        testSeq3DM = new PositionSequenceCoordinateSequence(
                sequence(C3DM.class, 4, 0, 0, 0, 1, 1, -1, 1, 2, 2, -2, 2, 3));
    }

    @Test
    public void testGetDimension() {
        assertEquals(2, testSeq2D.getDimension());
        assertEquals(3, testSeq3D.getDimension());
        assertEquals(3, testSeq2DM.getDimension());
        assertEquals(4, testSeq3DM.getDimension());
        assertFalse(testSeq2D.hasZ() || testSeq2D.hasM());
        assertTrue(testSeq3D.hasZ() && !testSeq3D.hasM());
        assertTrue(!testSeq2DM.hasZ() && testSeq2DM.hasM());
        assertTrue(testSeq3DM.hasZ() && testSeq3DM.hasM());
    }

    @Test
    public void testGetCoordinate() {
        // test2D
        assertEquals(new Coordinate(0, 0), testSeq2D.getCoordinate(0));
        assertEquals(new Coordinate(1, -1), testSeq2D.getCoordinate(1));
        assertEquals(new Coordinate(2, -2), testSeq2D.getCoordinate(2));

        assertEquals(new Coordinate(0, 0, 0), testSeq3D.getCoordinate(0));
        assertEquals(new Coordinate(1, -1, 1), testSeq3D.getCoordinate(1));
        assertEquals(new Coordinate(2, -2, 2), testSeq3D.getCoordinate(2));

        assertEquals(new Coordinate(0, 0), testSeq2DM.getCoordinate(0));
        assertEquals(new Coordinate(1, -1), testSeq2DM.getCoordinate(1));
        assertEquals(new Coordinate(2, -2), testSeq2DM.getCoordinate(2));

        assertEquals(new Coordinate(0, 0, 0), testSeq3DM.getCoordinate(0));
        assertEquals(new Coordinate(1, -1, 1), testSeq3DM.getCoordinate(1));
        assertEquals(new Coordinate(2, -2, 2), testSeq3DM.getCoordinate(2));

        try {
            testSeq2D.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3D.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq2DM.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3DM.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

    }

    @Test
    public void testGetCoordinateCopy() throws Exception {
        //geom independance: changing returned coordinate does not change sequence.
        Coordinate co = testSeq2D.getCoordinateCopy(1);
        co.x = co.x + 1;
        assertFalse(co.equals(testSeq2D.getCoordinateCopy(1)));
    }

    @Test
    public void testGetX() {
        assertEquals(0d, testSeq2D.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq2D.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq2D.getX(2), Math.ulp(10d));
        assertEquals(0d, testSeq2DM.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq2DM.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq2DM.getX(2), Math.ulp(10d));
        assertEquals(0d, testSeq3D.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq3D.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq3D.getX(2), Math.ulp(10d));
        assertEquals(0d, testSeq3DM.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getX(2), Math.ulp(10d));

        try {
            testSeq3D.getX(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetY() {
        assertEquals(0d, testSeq2D.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq2D.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq2D.getY(2), Math.ulp(10d));
        assertEquals(0d, testSeq2DM.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq2DM.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq2DM.getY(2), Math.ulp(10d));
        assertEquals(0d, testSeq3D.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq3D.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq3D.getY(2), Math.ulp(10d));
        assertEquals(0d, testSeq3DM.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq3DM.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq3DM.getY(2), Math.ulp(10d));

        try {
            testSeq3D.getY(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetZ() {
        assertEquals(0d, testSeq3D.getZ(0), Math.ulp(10d));
        assertEquals(1d, testSeq3D.getZ(1), Math.ulp(10d));
        assertEquals(2d, testSeq3D.getZ(2), Math.ulp(10d));
        assertEquals(0d, testSeq3DM.getZ(0), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getZ(1), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getZ(2), Math.ulp(10d));

        try {
            testSeq3D.getZ(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetM() {
        assertEquals(0d, testSeq2DM.getM(0), Math.ulp(10d));
        assertEquals(1d, testSeq2DM.getM(1), Math.ulp(10d));
        assertEquals(2d, testSeq2DM.getM(2), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getM(0), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getM(1), Math.ulp(10d));
        assertEquals(3d, testSeq3DM.getM(2), Math.ulp(10d));

        try {
            testSeq3D.getX(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetMeasures() {
        assertEquals(0, testSeq2D.getMeasures());
        assertEquals(0, testSeq3D.getMeasures());
        assertEquals(1, testSeq2DM.getMeasures());
        assertEquals(1, testSeq3DM.getMeasures());
    }

    @Test
    public void testGetOrdinate() {

        for (int i = 0; i < 3; i++) {
            //X
            Assert.assertEquals(testSeq2D.getX(i), testSeq2D.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));
            Assert.assertEquals(testSeq2DM.getX(i), testSeq2DM.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));
            Assert.assertEquals(testSeq3D.getX(i), testSeq3D.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getX(i), testSeq3DM.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));

            //Y
            Assert.assertEquals(testSeq2D.getY(i), testSeq2D.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));
            Assert.assertEquals(testSeq2DM.getY(i), testSeq2DM.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));
            Assert.assertEquals(testSeq3D.getY(i), testSeq3D.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getY(i), testSeq3DM.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));

            //Z
            Assert.assertEquals(testSeq3D.getZ(i), testSeq3D.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getZ(i), testSeq3DM.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));

            //M
            Assert.assertEquals(testSeq3DM.getM(i), testSeq3DM.getOrdinate(i, CoordinateSequence.M), Math.ulp(10d));


            try {
                testSeq2D.getOrdinate(i, 5);
                fail();
            } catch (IllegalArgumentException e) {
                //OK
            }

        }
        try {
            testSeq2D.getOrdinate(2, 5);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            testSeq2D.getOrdinate(4, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //OK
        }

        try {
            testSeq3D.getOrdinate(3, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

    }

    @Test
    public void testGetCoordinateIntoCoordinate() {
        CoordinateXYZM xyzm = new CoordinateXYZM();
        testSeq3DM.getCoordinate(2, xyzm);
        assertEquals(new CoordinateXYZM(2, -2, 2, 3).toString(), xyzm.toString());
        // only the ordinates both have
        Coordinate xyz = new Coordinate();
        testSeq2DM.getCoordinate(1, xyz);
        assertEquals(1, xyz.x, 0);
        assertTrue(Double.isNaN(xyz.getZ()));
        CoordinateXYM xym = new CoordinateXYM();
        testSeq3DM.getCoordinate(1, xym);
        assertEquals(new CoordinateXYM(1, -1, 2).toString(), xym.toString());
        assertTrue(testSeq3DM.getCoordinate(0) instanceof CoordinateXYZM);
        assertTrue(testSeq2DM.getCoordinate(0) instanceof CoordinateXYM);
        assertEquals(3, testSeq3DM.toCoordinateArray().length);
        assertEquals(new Envelope(0, 2, -2, 0), testSeq3D.expandEnvelope(new Envelope()));
    }

    @Test
    public void testWritesCopyAndLeaveThePositionsUnchanged() {
        PositionSequence<?> source = testSeq2D.unmodifiedSource();
        assertNotNull(source);
        CoordinateSequence copy = testSeq2D.copy();
        testSeq2D.setOrdinate(0, CoordinateSequence.X, 3);
        assertEquals(3, testSeq2D.getX(0), 0);
        assertEquals(1, testSeq2D.getX(1), 0);
        assertNull(testSeq2D.unmodifiedSource());
        assertEquals(0, source.getCoordinate(0, 0), 0);
        assertEquals(0, copy.getX(0), 0);
        // a copy of a written sequence is independent too
        CoordinateSequence copy2 = testSeq2D.copy();
        testSeq2D.setOrdinate(1, CoordinateSequence.Y, 7);
        assertEquals(-1, copy2.getY(1), 0);
        assertThrows(IllegalArgumentException.class, () -> testSeq2D.setOrdinate(0, 2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> testSeq2D.setOrdinate(3, 0, 1));
    }

    @Test
    public void testJtsCannotChangeAGeolatteGeometry() {
        Geometry<C2D> line = Wkt.fromWkt("LINESTRING (5 5, 0 0, 3 1)", CoordinateReferenceSystems.PROJECTED_2D_METER);
        Geometry<C2D> same = Wkt.fromWkt("LINESTRING (5 5, 0 0, 3 1)", CoordinateReferenceSystems.PROJECTED_2D_METER);
        org.locationtech.jts.geom.Geometry jts = JTS.to(line);
        jts.apply(AffineTransformation.translationInstance(100, 0));
        assertEquals(same, line);
        Geometry<C2D> moved = JTS.from(jts, CoordinateReferenceSystems.PROJECTED_2D_METER);
        assertEquals(Wkt.fromWkt("LINESTRING (105 5, 100 0, 103 1)", CoordinateReferenceSystems.PROJECTED_2D_METER),
                moved);
    }

    @Test
    public void testRoundTripSharesThePositions() {
        Geometry<C3DM> polygon = Wkt.fromWkt("POLYGON ZM ((0 0 1 2, 1 0 1 2, 1 1 1 2, 0 0 1 2))",
                CoordinateReferenceSystems.PROJECTED_3DM_METER);
        Geometry<C3DM> back = JTS.from(JTS.to(polygon), CoordinateReferenceSystems.PROJECTED_3DM_METER);
        assertEquals(polygon, back);
        assertSame(((Polygon<C3DM>) polygon).getExteriorRing().getPositions(),
                ((Polygon<C3DM>) back).getExteriorRing().getPositions());
    }
}
