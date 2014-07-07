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


import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LengthUnit;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.p;
import static org.geolatte.geom.builder.DSL.point;
import static org.junit.Assert.*;


/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public class TestDefaultMeasureGeometryOperations {

    private static CoordinateReferenceSystem<P2D> crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
        private static CoordinateReferenceSystem<P3D> crsZ = crs.addVerticalAxis(LengthUnit.METER);
        private static CoordinateReferenceSystem<P2DM> crsM = crs.addMeasureAxis(LengthUnit.METER);
        private static CoordinateReferenceSystem<P3DM> crsZM = crsZ.addMeasureAxis(LengthUnit.METER);


    DefaultMeasureGeometryOperations measureOps = new DefaultMeasureGeometryOperations();
    DefaultMeasureGeometryOperations measureOps3D = new DefaultMeasureGeometryOperations();

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
        GeometryOperation<Geometry<?>> onLengthOp = measureOps.createMeasureOnLengthOp(tc.emptyLineString, false);
        Geometry geometry = onLengthOp.execute();
        assertEquals("MeasureOnLengthOp returns non-empty geometry on empty geometry.",tc.emptyMeasuredLineString, geometry);
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularLineString() {
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 0.0, tc.measuredLineString2D.getPositionN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 1.0, tc.measuredLineString2D.getPositionN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 2.0, tc.measuredLineString2D.getPositionN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 3.0, tc.measuredLineString2D.getPositionN(3).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularMLineString() {
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 0.0, tc.measuredMultiLineString2D.getPositionN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 1.0, tc.measuredMultiLineString2D.getPositionN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 2.0, tc.measuredMultiLineString2D.getPositionN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 3.0, tc.measuredMultiLineString2D.getPositionN(3).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 4:", 3.0, tc.measuredMultiLineString2D.getPositionN(4).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 5:", 4.0, tc.measuredMultiLineString2D.getPositionN(5).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 6:", 5.0, tc.measuredMultiLineString2D.getPositionN(6).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpOnRegularMLineStringWithKeepInitial() {
        Geometry<P3DM> measured = (Geometry<P3DM>)measureOps.createMeasureOnLengthOp(tc.lineString3DM, true).execute();
        assertEquals("MeasureOnLengthOp creates wrong result at point 0:", 5.0, measured.getPositionN(0).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 1:", 6.0, measured.getPositionN(1).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 2:", 7.0, measured.getPositionN(2).getM(), Math.ulp(10));
        assertEquals("MeasureOnLengthOp creates wrong result at point 3:", 8.0, measured.getPositionN(3).getM(), Math.ulp(10));
    }

    @Test
    public void testCreateMeasureOnLengthOpPreserversCrsId() {
        assertEquals("MeasureOnLenthOp does not preserve CrsId: ", -1, tc.measuredLineString2D.getSRID());
    }

    @Test
    public void testCreateMeasureOnLengthOpPreserves3D() {
        assertEquals("MeasureOnLenthOp does not preserve 3D status: ",
                tc.lineString2d.getCoordinateReferenceSystem().hasVerticalAxis(),
                tc.measuredLineString2D.getCoordinateReferenceSystem().hasVerticalAxis());
        assertEquals("MeasureOnLenthOp does not preserve 3D status: ",
                tc.lineString3DM.getCoordinateReferenceSystem().hasVerticalAxis(),
                tc.measuredLineString3DM.getCoordinateReferenceSystem().hasVerticalAxis());
    }

    @Test
    public void testCreateMeasureOpPreservesAllNonMvalues() {
        checkXYZcoordinateEquality(tc.lineString2d, tc.measuredLineString2D);
        checkXYZcoordinateEquality(tc.lineString3DM, tc.measuredLineString3DM);
    }

    @Test
    public void testCreateMeasureOnLengthFailsForNonEmptyNonLinealGeometries() {
        Point point = point(crs, p(3, 4));
        try {
            measureOps.createMeasureOnLengthOp(point, false);
            fail("createMeasureOnLengthOp should not accept a Point argument.");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    private void checkXYZcoordinateEquality(Geometry<? extends Projected> expected, Geometry<? extends Projected> received) {
        for (int i = 0; i < expected.getNumPositions(); i++) {
            assertEquals("MeasureOnLenthOp does not preserve X-coordinate:", expected.getPositionN(i).getX(), received.getPositionN(i).getX(), Math.ulp(10));
            assertEquals("MeasureOnLenthOp does not preserve Y-coordinate:", expected.getPositionN(i).getY(), received.getPositionN(i).getY(), Math.ulp(10));
            if (expected.getCoordinateReferenceSystem().hasVerticalAxis()) {
                assertEquals("MeasureOnLenthOp does not preserve Z-coordinate:",
                        ((Vertical)expected.getPositionN(i)).getAltitude(),
                        ((Vertical)received.getPositionN(i)).getAltitude(), Math.ulp(10));
            }
        }
    }

    @Test
    public void testCreateGetMeasureOpThrowsExceptionOnNull() {
        try {
            measureOps.createGetMeasureOp((Geometry<P2DM>)null, new P2DM(crsM, 1, 2, 0)).execute();
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
        double m = measureOps.createGetMeasureOp(new LineString<>(crsM), new P2DM(crsM, 1, 2, 0)).execute();
        assertTrue(Double.isNaN(m));
    }

    @Test
    public void testGetMeasureOpReturnsNaNWhenPointNotOnMeasuredGeometry() {
        double m = measureOps.createGetMeasureOp(tc.measuredLineString2D, new P2DM(crsM, 5, 5, 0)).execute();
        assertTrue(Double.isNaN(m));
    }

    @Test
    public void testGetMeasureOpOnRegularLine() {
        double m = measureOps.createGetMeasureOp(tc.measuredLineString2D, new P2DM(crsM, 1.5, 1, 0)).execute();
        assertEquals(2.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiLine() {
        double m = measureOps.createGetMeasureOp(tc.measuredMultiLineString2D, new P2DM(crsM, 4.5, 1, 0)).execute();
        assertEquals(4.5, m, Math.ulp(10));

        m = measureOps.createGetMeasureOp(tc.measuredMultiLineString2D, new P2DM(crsM, 2.5, 1, 0)).execute();
        assertTrue(Double.isNaN(m));

    }

    @Test
    public void testGetMeasureOpOnRegularLinearRing() {
        double m = measureOps.createGetMeasureOp(tc.measuredLinearRing, new P2DM(crsM, 0, 0.5, 0)).execute();
        assertEquals(3.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiPoint() {
        double m = measureOps.createGetMeasureOp(tc.measuredMultiPoint, new P2DM(crsM, 1, 2, 0)).execute();
        assertEquals(2d, m, Math.ulp(10));
    }

    @Test
    public void testGetMinimumMeasureOpOnRegularMultiLineString() {
        double m = measureOps.createGetMinimumMeasureOp(tc.lineString3DM).execute();
        assertEquals(5d, m, Math.ulp(10));

        m = measureOps.createGetMinimumMeasureOp(tc.caseD1A).execute();
        assertEquals(0d, m, Math.ulp(10));

        m = measureOps.createGetMinimumMeasureOp(tc.emptyMeasuredLineString).execute();
        assertTrue(Double.isNaN(m));

    }

    @Test
    public void testGetMaximumMeasureOpOnRegularMultiLineString() {
        double m = measureOps.createGetMaximumMeasureOp(tc.lineString3DM).execute();
        assertEquals(30d, m, Math.ulp(10));

        m = measureOps.createGetMaximumMeasureOp(tc.caseD1A).execute();
        assertEquals(4d, m, Math.ulp(10));

        m = measureOps.createGetMaximumMeasureOp(tc.emptyMeasuredLineString).execute();
        assertTrue(Double.isNaN(m));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMinimumMeasureOpOnNull() {
        double m = measureOps.createGetMinimumMeasureOp(null).execute();
    }
}
