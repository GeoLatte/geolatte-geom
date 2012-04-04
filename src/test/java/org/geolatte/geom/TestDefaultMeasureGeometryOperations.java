/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.crs.CrsId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public class TestDefaultMeasureGeometryOperations {

    DefaultMeasureGeometryOperations measureOps = new DefaultMeasureGeometryOperations();

    LineString test2DLine;
    MultiLineString test2DMultiLine;
    LineString test3DMLine;
    LineString emptyLineString;
    LineString ringLine;

    Geometry measuredTest2DLine;
    Geometry measuredTest2DMultiline;
    Geometry measuredTest3DMLine;
    Geometry measuredRingLine;
    MultiPoint measuredMultiPoint;

    @Before
    public void setUp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(4, DimensionalFlag.XY);
        psBuilder.add(0, 0)
                .add(1, 0)
                .add(1, 1)
                .add(2, 1);
        test2DLine = new LineString(psBuilder.toPointSequence(), CrsId.parse("EPSG:4326"));

        psBuilder = PointSequenceBuilders.fixedSized(3, DimensionalFlag.XY);
        psBuilder.add(3, 1).add(4, 1).add(5, 1);
        LineString test2DLine2 = new LineString(psBuilder.toPointSequence(), CrsId.parse("EPSG:4326"));
        test2DMultiLine = new MultiLineString(new LineString[]{test2DLine, test2DLine2});


        psBuilder = PointSequenceBuilders.fixedSized(4, DimensionalFlag.XYZM);
        psBuilder.add(0, 0, 2, 5)
                .add(1, 0, 3, 10)
                .add(1, 1, 4, 20)
                .add(2, 1, 5, 30);
        test3DMLine = new LineString(psBuilder.toPointSequence(), CrsId.parse("EPSG:4326"));

        measuredMultiPoint = new MultiPoint(new Point[]{
                Points.createMeasured(0,0,1, CrsId.UNDEFINED),
                Points.createMeasured(1,2,2, CrsId.UNDEFINED)
                });


        psBuilder = PointSequenceBuilders.fixedSized(5, DimensionalFlag.XYM);
        psBuilder.add(0, 0, 0)
                .add(1, 0, 0)
                .add(1, 1, 0)
                .add(0, 1, 0)
                .add(0, 0, 0);
        ringLine = new LineString(psBuilder.toPointSequence(), null);



        emptyLineString = LineString.createEmpty();

        //measured lines
        measuredTest2DLine = measureOps.createMeasureOnLengthOp(test2DLine, false).execute();
        measuredTest2DMultiline = measureOps.createMeasureOnLengthOp(test2DMultiLine, false).execute();
        measuredTest3DMLine = measureOps.createMeasureOnLengthOp(test3DMLine, false).execute();
        measuredRingLine = measureOps.createMeasureOnLengthOp(ringLine, false).execute();
    }

    @Test
    public void testCreateMeasureOnLengthOpThrowsExceptionOnNullParameter() {
        try {
            GeometryOperation<Geometry> onLengthOp = measureOps.createMeasureOnLengthOp(null, false);
            fail("createMeasureOnLengthOp created on null parameter. Should throw exception");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testCreateMeasureOnLengthOpReturnsParameterOnEmptyGeometry() {
        GeometryOperation<Geometry> onLengthOp = measureOps.createMeasureOnLengthOp(emptyLineString, false);
        Geometry geometry = onLengthOp.execute();
        assertEquals("MeasureOnLengthOp returns non-empty geometry on empty geometry.", geometry, emptyLineString);
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularLineString() {
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 0.0, measuredTest2DLine.getPointN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 1.0, measuredTest2DLine.getPointN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 2.0, measuredTest2DLine.getPointN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 3.0, measuredTest2DLine.getPointN(3).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularMLineString() {
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 0.0, measuredTest2DMultiline.getPointN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 1.0, measuredTest2DMultiline.getPointN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 2.0, measuredTest2DMultiline.getPointN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 3.0, measuredTest2DMultiline.getPointN(3).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 4:", 3.0, measuredTest2DMultiline.getPointN(4).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 5:", 4.0, measuredTest2DMultiline.getPointN(5).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 6:", 5.0, measuredTest2DMultiline.getPointN(6).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularMLineStringWithKeepInitial() {
        Geometry measured = measureOps.createMeasureOnLengthOp(test3DMLine, true).execute();
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 5.0, measured.getPointN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 6.0, measured.getPointN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 7.0, measured.getPointN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 8.0, measured.getPointN(3).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpPreserversCrsId() {
        assertEquals("MeasureOnLenthOp does not preserve CrsId: ", CrsId.parse("EPSG:4326"), measuredTest2DLine.getCrsId());
    }

    @Test
    public void testCreateMeasureOnLengthOpPreserves3D() {
        assertEquals("MeasureOnLenthOp does not preserve 3D status: ", test2DLine.is3D(), measuredTest2DLine.is3D());
        assertEquals("MeasureOnLenthOp does not preserve 3D status: ", test3DMLine.is3D(), measuredTest3DMLine.is3D());
    }

    @Test
    public void testCreateMeasureOpPreservesAllNonMvalues() {
        checkXYZcoordinateEquality(test2DLine, measuredTest2DLine);
        checkXYZcoordinateEquality(test3DMLine, measuredTest3DMLine);
    }

    @Test
    public void testCreateMeasureOnLengthFailsForNonEmptyNonLinealGeometries() {
        Point point = Points.create(3, 4);
        try {
            measureOps.createMeasureOnLengthOp(point, false);
            fail("createMeasureOnLengthOp should not accept a Point argument.");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    private void checkXYZcoordinateEquality(Geometry expected, Geometry received) {
        for (int i = 0; i < expected.getNumPoints(); i++) {
            assertEquals("MeasureOnLenthOp does not preserve X-coordinate:", expected.getPointN(i).getX(), received.getPointN(i).getX(), Math.ulp(10));
            assertEquals("MeasureOnLenthOp does not preserve Y-coordinate:", expected.getPointN(i).getY(), received.getPointN(i).getY(), Math.ulp(10));
            assertEquals("MeasureOnLenthOp does not preserve Z-coordinate:", expected.getPointN(i).getZ(), received.getPointN(i).getZ(), Math.ulp(10));
        }
    }

    @Test
    public void testCreateGetMeasureOpThrowsExceptionOnNull() {
        try {
            measureOps.createGetMeasureOp(null, Points.create(1, 2)).execute();
            fail("createGetMeasureOp created on null parameter. Should throw exception");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            measureOps.createGetMeasureOp(measuredTest2DLine, null).execute();
            fail("createGetMeasureOp created on null parameter. Should throw exception");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testGetMeasureOpReturnsNaNOnEmptyGeometry() {
        double m = measureOps.createGetMeasureOp(LineString.EMPTY, Points.create(1, 2)).execute();
        assertTrue(Double.isNaN(m));
    }

    @Test
    public void testGetMeasureOpReturnsNaNWhenPointNotOnMeasuredGeometry() {
        double m = measureOps.createGetMeasureOp(measuredTest2DLine, Points.create(5, 5)).execute();
        assertTrue(Double.isNaN(m));
    }

    @Test
    public void testGetMeasureOpOnRegularLine() {
        double m = measureOps.createGetMeasureOp(measuredTest2DLine, Points.create(1.5, 1)).execute();
        assertEquals(2.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiLine() {
        double m = measureOps.createGetMeasureOp(measuredTest2DMultiline, Points.create(4.5, 1)).execute();
        assertEquals(4.5, m, Math.ulp(10));

        m = measureOps.createGetMeasureOp(measuredTest2DMultiline, Points.create(2.5, 1)).execute();
        assertTrue(Double.isNaN(m));

    }

    @Test
    public void testGetMeasureOpOnRegularLinearRing() {
        double m = measureOps.createGetMeasureOp(measuredRingLine, Points.create(0, 0.5)).execute();
        assertEquals(3.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiPoint() {
        double m = measureOps.createGetMeasureOp(measuredMultiPoint, Points.create(1, 2)).execute();
        assertEquals(2d, m, Math.ulp(10));
    }


}
