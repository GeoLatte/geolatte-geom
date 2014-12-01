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


import org.geolatte.geom.builder.DSL;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasVerticalAxis;
import static org.junit.Assert.*;
import static org.geolatte.geom.CrsMock.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public class TestDefaultMeasureGeometryOperations {



    DefaultMeasureGeometryOperations measureOps = new DefaultMeasureGeometryOperations();
    DefaultMeasureGeometryOperations measureOps3D = new DefaultMeasureGeometryOperations();

    MeasuredTestCases tc = new MeasuredTestCases();

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMeasureOnLengthOpThrowsExceptionOnNullParameter() {
        measureOps.measureOnLength(null, C2DM.class, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMeasureOnLengthOpWithInconsistentTypeMarker() {
        measureOps.measureOnLength(linestring(crs, DSL.c(1, 2), DSL.c(3, 4)), C3DM.class, false);
    }

    @Test
    public void testCreateMeasureOnLengthOpReturnsParameterOnEmptyGeometry() {
        Geometry geometry = measureOps.measureOnLength(tc.emptyLineString, C2DM.class, false);
        assertEquals("MeasureOnLengthOp returns non-empty geometry on empty geometry.", tc.emptyMeasuredLineString, geometry);
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
        Geometry<C3DM> measured = measureOps.measureOnLength(tc.lineString3DM, C3DM.class, true);
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
                hasVerticalAxis(tc.lineString2d.getCoordinateReferenceSystem()),
                hasVerticalAxis(tc.measuredLineString2D.getCoordinateReferenceSystem()));
        assertEquals("MeasureOnLenthOp does not preserve 3D status: ",
                hasVerticalAxis(tc.lineString3DM.getCoordinateReferenceSystem()),
                hasVerticalAxis(tc.measuredLineString3DM.getCoordinateReferenceSystem()));
    }

    @Test
    public void testCreateMeasureOpPreservesAllNonMvalues() {
        checkXYZcoordinateEquality(tc.lineString2d, tc.measuredLineString2D);
        checkXYZcoordinateEquality(tc.lineString3DM, tc.measuredLineString3DM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateMeasureOnLengthFailsForNonEmptyNonLinealGeometries() {
        Point point = point(crs, DSL.c(3, 4));
        measureOps.measureOnLength(point, C2DM.class, false);
    }

    private void checkXYZcoordinateEquality(Geometry<? extends C2D> expected, Geometry<? extends C2D> received) {
        for (int i = 0; i < expected.getNumPositions(); i++) {
            assertEquals("MeasureOnLenthOp does not preserve X-coordinate:", expected.getPositionN(i).getX(), received.getPositionN(i).getX(), Math.ulp(10));
            assertEquals("MeasureOnLenthOp does not preserve Y-coordinate:", expected.getPositionN(i).getY(), received.getPositionN(i).getY(), Math.ulp(10));
            if (hasVerticalAxis(expected.getCoordinateReferenceSystem())) {
                assertEquals("MeasureOnLenthOp does not preserve Z-coordinate:",
                        (expected.getPositionN(i)).getCoordinate(2),
                        (received.getPositionN(i)).getCoordinate(2), Math.ulp(10));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateGetMeasureOpThrowsExceptionOnNullGeometry() {
        measureOps.measureAt((Geometry<C2DM>) null, new C2DM(1, 2, 0), 1);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateGetMeasureOpThrowsExceptionOnNullPosition() {
        measureOps.measureAt(tc.measuredLineString2D, null, 1);

    }

    @Test
    public void testGetMeasureOpReturnsNaNOnEmptyGeometry() {
        double m = measureOps.measureAt(new LineString<C2DM>(crsM), new C2DM(1, 2, 0), 0.1);
        assertTrue(Double.isNaN(m));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMeasureThrowsExceptionWhenPointNotOnMeasuredGeometry() {
        double m = measureOps.measureAt(tc.measuredLineString2D, new C2DM(5, 5, 0), 0.001);
    }

    @Test
    public void testGetMeasureOpOnRegularLine() {
        double m = measureOps.measureAt(tc.measuredLineString2D, new C2DM(1.5, 1, 0), 0.001);
        assertEquals(2.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiLine() {
        double m = measureOps.measureAt(tc.measuredMultiLineString2D, new C2DM(4.5, 1, 0), 0.001);
        assertEquals(4.5, m, Math.ulp(10));

        try {
            m = measureOps.measureAt(tc.measuredMultiLineString2D, new C2DM(2.5, 1, 0), 0.001);
            fail();
        } catch(IllegalArgumentException e) {}
    }

    @Test
    public void testGetMeasureOpOnRegularLinearRing() {
        double m = measureOps.measureAt(tc.measuredLinearRing, new C2DM(0, 0.5, 0), 0.001);
        assertEquals(3.5, m, Math.ulp(10));
    }

    @Test
    public void testGetMeasureOpOnRegularMultiPoint() {
        double m = measureOps.measureAt(tc.measuredMultiPoint, new C2DM(1, 2, 0), 0.001);
        assertEquals(2d, m, Math.ulp(10));
    }

    @Test
    public void testGetMinimumMeasureOpOnRegularMultiLineString() {
        double m = measureOps.minimumMeasure(tc.lineString3DM);
        assertEquals(5d, m, Math.ulp(10));

        m = measureOps.minimumMeasure(tc.caseD1A);
        assertEquals(0d, m, Math.ulp(10));

        m = measureOps.minimumMeasure(tc.emptyMeasuredLineString);
        assertTrue(Double.isNaN(m));

    }

    @Test
    public void testGetMaximumMeasureOpOnRegularMultiLineString() {
        double m = measureOps.maximumMeasure(tc.lineString3DM);
        assertEquals(30d, m, Math.ulp(10));

        m = measureOps.maximumMeasure(tc.caseD1A);
        assertEquals(4d, m, Math.ulp(10));

        m = measureOps.maximumMeasure(tc.emptyMeasuredLineString);
        assertTrue(Double.isNaN(m));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMinimumMeasureOpOnNull() {
        double m = measureOps.minimumMeasure((LineString<C2DM>)null);
    }
}
