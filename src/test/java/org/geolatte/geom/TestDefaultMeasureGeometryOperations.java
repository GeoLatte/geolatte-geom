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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public class TestDefaultMeasureGeometryOperations {

    DefaultMeasureGeometryOperations measureOps = new DefaultMeasureGeometryOperations();
    MeasuredTestCases tc = new MeasuredTestCases();

    @Test
    public void testCreateMeasureOnLengthOpThrowsExceptionOnNullParameter() {
        try {
            measureOps.createMeasureOnLengthOp(null, false);
            fail("createMeasureOnLengthOp created on null parameter. Should throw exception");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testCreateMeasureOnLengthOpReturnsParameterOnEmptyGeometry() {
        GeometryOperation<Geometry> onLengthOp = measureOps.createMeasureOnLengthOp(tc.emptyLineString, false);
        Geometry geometry = onLengthOp.execute();
        assertEquals("MeasureOnLengthOp returns non-empty geometry on empty geometry.", geometry, tc.emptyLineString);
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularLineString() {
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 0.0, tc.measuredLineString2D.getPointN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 1.0, tc.measuredLineString2D.getPointN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 2.0, tc.measuredLineString2D.getPointN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 3.0, tc.measuredLineString2D.getPointN(3).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularMLineString() {
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 0.0, tc.measuredMultiLineString2D.getPointN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 1.0, tc.measuredMultiLineString2D.getPointN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 2.0, tc.measuredMultiLineString2D.getPointN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 3.0, tc.measuredMultiLineString2D.getPointN(3).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 4:", 3.0, tc.measuredMultiLineString2D.getPointN(4).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 5:", 4.0, tc.measuredMultiLineString2D.getPointN(5).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 6:", 5.0, tc.measuredMultiLineString2D.getPointN(6).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularMLineStringWithKeepInitial() {
        Geometry measured = measureOps.createMeasureOnLengthOp(tc.lineString3DM, true).execute();
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 5.0, measured.getPointN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 6.0, measured.getPointN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 7.0, measured.getPointN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 8.0, measured.getPointN(3).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpPreserversCrsId() {
        assertEquals("MeasureOnLenthOp does not preserve CrsId: ", CrsId.parse("EPSG:4326"), tc.measuredLineString2D.getCrsId());
    }

    @Test
    public void testCreateMeasureOnLengthOpPreserves3D() {
        assertEquals("MeasureOnLenthOp does not preserve 3D status: ", tc.lineString2d.is3D(), tc.measuredLineString2D.is3D());
        assertEquals("MeasureOnLenthOp does not preserve 3D status: ", tc.lineString3DM.is3D(), tc.measuredLineString3DM.is3D());
    }

    @Test
    public void testCreateMeasureOpPreservesAllNonMvalues() {
        checkXYZcoordinateEquality(tc.lineString2d, tc.measuredLineString2D);
        checkXYZcoordinateEquality(tc.lineString3DM, tc.measuredLineString3DM);
    }

    @Test
    public void testCreateMeasureOnLengthFailsForNonEmptyNonLinealGeometries() {
        Point point = Points.create2D(3, 4);
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
            measureOps.createGetMeasureOp(null, Points.create2D(1, 2)).execute();
            fail("createGetMeasureOp created on null parameter. Should throw exception");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            measureOps.createGetMeasureOp(tc.measuredLineString2D, null).execute();
            fail("createGetMeasureOp created on null parameter. Should throw exception");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testGetMeasureOpReturnsNaNOnEmptyGeometry() {
        double m = measureOps.createGetMeasureOp(LineString.EMPTY, Points.create2D(1, 2)).execute();
        assertTrue(Double.isNaN(m));
    }

    @Test
    public void testGetMeasureOpReturnsNaNWhenPointNotOnMeasuredGeometry() {
        double m = measureOps.createGetMeasureOp(tc.measuredLineString2D, Points.create2D(5, 5)).execute();
        assertTrue(Double.isNaN(m));
    }

    @Test
    public void testGetMeasureOpOnRegularLine() {
        double m = measureOps.createGetMeasureOp(tc.measuredLineString2D, Points.create2D(1.5, 1)).execute();
        assertEquals(2.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiLine() {
        double m = measureOps.createGetMeasureOp(tc.measuredMultiLineString2D, Points.create2D(4.5, 1)).execute();
        assertEquals(4.5, m, Math.ulp(10));

        m = measureOps.createGetMeasureOp(tc.measuredMultiLineString2D, Points.create2D(2.5, 1)).execute();
        assertTrue(Double.isNaN(m));

    }

    @Test
    public void testGetMeasureOpOnRegularLinearRing() {
        double m = measureOps.createGetMeasureOp(tc.measuredLinearRing, Points.create2D(0, 0.5)).execute();
        assertEquals(3.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiPoint() {
        double m = measureOps.createGetMeasureOp(tc.measuredMultiPoint, Points.create2D(1, 2)).execute();
        assertEquals(2d, m, Math.ulp(10));
    }


}
