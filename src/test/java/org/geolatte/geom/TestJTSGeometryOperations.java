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
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/27/11
 */
public class TestJTSGeometryOperations {

    JTSGeometryOperations ops = new JTSGeometryOperations();

    //retrieve the test geometries from the TestDefaultMeasureGeometryOperations class
    MeasuredTestCases tc = new MeasuredTestCases();

    @Test
    public void testCreateEnvelopeOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(4, DimensionalFlag.XY);
        psBuilder.add(-1, 3).add(2, 5).add(10, -8).add(9, -1);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        assertEquals(new Envelope(-1, -8, 10, 5), ops.createEnvelopeOp(lineString).execute());
    }

    @Test
    public void testCreateEnvelopeOpOnlyNegative() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(4, DimensionalFlag.XY);
        psBuilder.add(-101, -97).add(-98, -95).add(-90, -108).add(-91, -101);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        assertEquals(new Envelope(-101, -108, -90, -95), ops.createEnvelopeOp(lineString).execute());
    }

    @Test
    public void testCreateEnvelopeOpOnlyPositive() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(4, DimensionalFlag.XY);
        psBuilder.add(99, 103).add(102, 105).add(110, 92).add(109, 99);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        assertEquals(new Envelope(99, 92, 110, 105), ops.createEnvelopeOp(lineString).execute());
    }

    @Test
    public void testCreateEnvelopeOpOnEmpty() {
        LineString lineString = LineString.EMPTY;
        assertEquals(new Envelope(Double.NaN, Double.NaN, Double.NaN, Double.NaN), ops.createEnvelopeOp(lineString).execute());
        assertEquals(Envelope.EMPTY, ops.createEnvelopeOp(lineString).execute());
    }

    @Test
    public void testIntersectsOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(1, 1).add(2, 2);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(3, 3).add(4, 4);
        LineString lineString2 = new LineString(psBuilder.toPointSequence(), null);
        assertFalse(ops.createIntersectsOp(lineString, lineString2).execute());
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(2, 2).add(4, 4);
        LineString lineString3 = new LineString(psBuilder.toPointSequence(), null);
        assertTrue(ops.createIntersectsOp(lineString, lineString3).execute());
    }

    @Test
    public void testTouchesOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(1, 1).add(2, 2);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(3, 3).add(4, 4);
        LineString lineString2 = new LineString(psBuilder.toPointSequence(), null);
        assertFalse(ops.createTouchesOp(lineString, lineString2).execute());
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(2, 2).add(4, 4);
        LineString lineString3 = new LineString(psBuilder.toPointSequence(), null);
        assertTrue(ops.createTouchesOp(lineString, lineString3).execute());
    }

    @Test
    public void testCrossesOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(1, 1).add(1, 3);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(3, 3).add(4, 4);
        LineString lineString2 = new LineString(psBuilder.toPointSequence(), null);
        assertFalse(ops.createCrossesOp(lineString, lineString2).execute());
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 2).add(3, 2);
        LineString lineString3 = new LineString(psBuilder.toPointSequence(), null);
        assertTrue(ops.createCrossesOp(lineString, lineString3).execute());
    }


    @Test
    public void testContainsOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(1, 1).add(1, 2);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 0).add(0, 4).add(4, 4).add(4, 0).add(0, 0);
        Polygon polygon = new Polygon(psBuilder.toPointSequence(), null);
        assertFalse(ops.createContainsOp(lineString, polygon).execute());
        assertTrue(ops.createContainsOp(polygon, lineString).execute());
    }


    @Test
    public void testOverlapsOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(-1, -1).add(-1, 2).add(2, 2).add(2, -1).add(-1, -1);
        Polygon polygon = new Polygon(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 0).add(0, 4).add(4, 4).add(4, 0).add(0, 0);
        Polygon polygon2 = new Polygon(psBuilder.toPointSequence(), null);
        assertTrue(ops.createOverlapsOp(polygon, polygon2).execute());
        assertTrue(ops.createOverlapsOp(polygon2, polygon).execute());
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(10, 10).add(10, 14).add(14, 14).add(14, 10).add(10, 10);
        Polygon polygon3 = new Polygon(psBuilder.toPointSequence(), null);
        assertFalse(ops.createOverlapsOp(polygon3, polygon2).execute());
        assertFalse(ops.createOverlapsOp(polygon2, polygon3).execute());
    }

    @Test
    public void testRelateOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(-1, -1).add(-1, 2).add(2, 2).add(2, -1).add(-1, -1);
        Polygon polygon = new Polygon(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 0).add(0, 4).add(4, 4).add(4, 0).add(0, 0);
        Polygon polygon2 = new Polygon(psBuilder.toPointSequence(), null);
        assertTrue(ops.createRelateOp(polygon, polygon2, "T*T***T**").execute());
        assertTrue(ops.createRelateOp(polygon2, polygon, "T*T***T**").execute());
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(10, 10).add(10, 14).add(14, 14).add(14, 10).add(10, 10);
        Polygon polygon3 = new Polygon(psBuilder.toPointSequence(), null);
        assertFalse(ops.createRelateOp(polygon3, polygon2, "T*T***T**").execute());
        assertFalse(ops.createRelateOp(polygon2, polygon3, "T*T***T**").execute());
    }

    @Test
    public void testDistanceOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(1, 1).add(1, 3);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(3, 1).add(3, 4);
        LineString lineString2 = new LineString(psBuilder.toPointSequence(), null);
        assertEquals(2., ops.createDistanceOp(lineString, lineString2).execute(), 0.00001d);
    }

    @Test
    public void testBufferOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 0).add(1, 1);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        Geometry buffer = ops.createBufferOp(lineString, 10d).execute();
        assertTrue(buffer.contains(lineString));
        assertEquals(JTS.from(JTS.to(lineString).buffer(10d), CrsId.UNDEFINED), buffer);
    }


    @Test
    public void testConvexHullOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 0).add(1, 1);
        LineString lineString = new LineString(psBuilder.toPointSequence(), null);
        Geometry hull = ops.createConvexHullOp(lineString).execute();
        assertTrue(hull.contains(lineString));
        assertEquals(JTS.from(JTS.to(lineString).convexHull(), CrsId.UNDEFINED), hull);
    }

    @Test
    public void testIntersectionOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 0).add(5, 0).add(5, 5).add(0, 5).add(0, 0);
        Polygon polygon1 = new Polygon(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(3, 3).add(7, 3).add(7, 7).add(3, 7).add(3, 3);
        Polygon polygon2 = new Polygon(psBuilder.toPointSequence(), null);
        Polygon polygon3 = new Polygon(psBuilder.toPointSequence(), CrsId.valueOf(4326));
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(3, 5).add(5, 5).add(5, 3).add(3, 3).add(3, 5);
        Polygon expected = new Polygon(psBuilder.toPointSequence(), null);
        assertEquals("Intersection returned incorrect result", expected, ops.createIntersectionOp(polygon1, polygon2).execute());

        try {
            ops.createIntersectionOp(polygon1, polygon3).execute();
            fail("Intersection did not check for compatible CRSs");
        } catch (IllegalArgumentException e) {
        }

        Polygon polygon4 = Polygon.createEmpty();
        assertEquals("Intersection with empty is not a GeometryCollection", GeometryCollection.createEmpty(), ops.createIntersectionOp(polygon1, polygon4).execute());
    }

    @Test
    public void testUnionOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(0, 0).add(5, 0).add(5, 5).add(0, 5).add(0, 0);
        Polygon polygon1 = new Polygon(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(3, 3).add(7, 3).add(7, 7).add(3, 7).add(3, 3);
        Polygon polygon2 = new Polygon(psBuilder.toPointSequence(), null);


        psBuilder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        psBuilder.add(5, 3).add(5, 0).add(0, 0).add(0, 5).add(3, 5).add(3, 7).add(7, 7).add(7, 3).add(5, 3);
        Polygon expected = new Polygon(psBuilder.toPointSequence(), null);
        assertEquals(expected, ops.createUnionOp(polygon1, polygon2).execute());
    }

    @Test
    public void testDifferenceOp() {
        PointSequenceBuilder builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(0, 0).add(5, 0).add(5, 5).add(0, 5).add(0, 0);
        Polygon pg1 = new Polygon(builder.toPointSequence(), null);
        builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(0, 3).add(0, 6).add(5, 6).add(5, 3).add(0, 3);
        Polygon pg2 = new Polygon(builder.toPointSequence(), null);
        builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(5, 3).add(5, 0).add(0, 0).add(0, 3).add(5, 3);
        Polygon expected = new Polygon(builder.toPointSequence(), null);
        assertEquals(expected, ops.createDifferenceOp(pg1, pg2).execute());
        assertEquals(pg1, ops.createDifferenceOp(pg1, Polygon.createEmpty()).execute());
        assertEquals(GeometryCollection.createEmpty(), ops.createDifferenceOp(Polygon.createEmpty(), pg2).execute());
    }

    @Test
    public void testSymDifferenceOp() {
        PointSequenceBuilder builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(0, 0).add(6, 0).add(6, 6).add(0, 6).add(0, 0);
        Polygon pg1 = new Polygon(builder.toPointSequence(), null);
        builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(0, 3).add(6, 3).add(6, 8).add(0, 8).add(0, 3);
        Polygon pg2 = new Polygon(builder.toPointSequence(), null);
        builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(6, 3).add(6, 0).add(0, 0).add(0, 3).add(6, 3);
        Polygon expected1 = new Polygon(builder.toPointSequence(), null);
        builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(6, 6).add(0, 6).add(0, 8).add(6, 8).add(6, 6);
        Polygon expected2 = new Polygon(builder.toPointSequence(), null);
        MultiPolygon expected = new MultiPolygon(new Polygon[]{expected1, expected2});
        assertEquals(expected, ops.createSymDifferenceOp(pg1, pg2).execute());
        assertEquals(pg1, ops.createSymDifferenceOp(pg1, Polygon.createEmpty()).execute());
        assertEquals(pg2, ops.createSymDifferenceOp(Polygon.createEmpty(), pg2).execute());
    }

    @Test
    public void testWktOp() {
        PointSequenceBuilder builder = PointSequenceBuilders.variableSized(DimensionalFlag.XY);
        builder.add(0, 0).add(6, 0).add(6, 6).add(0, 6).add(0, 0);
        Polygon pg1 = new Polygon(builder.toPointSequence(), null);
        String expected = "POLYGON((0 0,6 0,6 6,0 6,0 0))";
        assertEquals(expected, ops.createToWktOp(pg1).execute());
    }

    @Test
    public void testLocateAlongOpOnEmptyLineStringReturnsEmptyGeometry() {
        Geometry result = ops.createLocateAlongOp(tc.emptyLineString, 10d).execute();
        assertTrue("Non-empty result on empty Geometry.", result.isEmpty());
    }

    @Test
    public void testLocateAlongOpOnNullThrowsIllegalArgument() {
        try {
            ops.createLocateAlongOp(null, 10d).execute();
            fail("No IllegalArgumentException thrown on NULL argument");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testLocateBetweenOpOnNullThrowsIllegalArgument() {
        try {
            ops.createLocateBetweenOp(null, 10d, 12d).execute();
            fail("No IllegalArgumentException thrown on NULL argument");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    @Test
    public void testLocateBetweenOpOnEmptyLineStringReturnsEmptyGeometry() {
        Geometry result = ops.createLocateBetweenOp(tc.emptyLineString, 10d, 12d).execute();
        assertTrue("Non-empty result on empty Geometry.", result.isEmpty());
    }

    /**
     * The next test method implements the test cases for 0-Dim. geometries
     * listed in section 4.2.1.7.3 of the SQL/MM-Spatial Data spec.
     */
    @Test
    public void testLocateAlongOn0DimensionalGeometries() {
        Geometry result = ops.createLocateAlongOp(tc.caseD0A, 4d).execute();
        assertEquals("Error in case a) for 0-Dim. geometry", tc.expectedForD0A, result);

        result = ops.createLocateBetweenOp(tc.caseD0B, 2d, 4d).execute();
        assertEquals("Error in case b) for 0-Dim. geometry", tc.expectedForD0B, result);

        result = ops.createLocateBetweenOp(tc.caseD0C, 1d, 4d).execute();
        assertEquals("Error in case c) for 0-Dim. geometry", tc.expectedForD0C, result);

        result = ops.createLocateBetweenOp(tc.caseD0D, 7d, 7d).execute();
        assertEquals("Error in case d) for 0-Dim. geometry", tc.expectedForD0D, result);

    }

    /**
     * The next test method implements the test cases for 0-Dim. geometries
     * listed in section 4.2.1.7.4 of the SQL/MM-Spatial Data spec.
     */
    @Test
    public void testLocateOn1DimensionalGeometriesSQLMMCompliant() {
        Geometry result = ops.createLocateAlongOp(tc.caseD1A, 4d).execute();
        assertEquals("Error in case a) for 1-Dim. geometry", tc.expectedForD1A, result);

        result = ops.createLocateBetweenOp(tc.caseD1B, 2d, 4d).execute();
        assertEquals("Error in case b) for 1-Dim. geometry", tc.expectedForD1B, result);

        result = ops.createLocateBetweenOp(tc.caseD1B, 4d, 2d).execute();
        assertEquals("Error in case b) for 1-Dim. geometry when start/end reversed", tc.expectedForD1B, result);

        result = ops.createLocateBetweenOp(tc.caseD1C, 6d, 9d).execute();
        assertEquals("Error in case c) for 1-Dim. geometry", tc.expectedForD1C, result);

        result = ops.createLocateBetweenOp(tc.caseD1D, 1d, 2d).execute();
        assertEquals("Error in case d) for 1-Dim. geometry", tc.expectedForD1D, result);

        result = ops.createLocateBetweenOp(tc.caseD1E, 2d, 4d).execute();
        assertEquals("Error in case e) for 1-Dim. geometry", tc.expectedForD1E, result);

        result = ops.createLocateBetweenOp(tc.caseD1F, 1d, 3d).execute();
        assertEquals("Error in case f) for 1-Dim. geometry", tc.expectedForD1F, result);

        result = ops.createLocateBetweenOp(tc.caseD1G, 7d, 9d).execute();
        assertEquals("Error in case g) for 1-Dim. geometry", tc.expectedForD1G, result);


    }

    @Test
    public void testLocateOn1DimensionalGeometries() {
        Geometry result = ops.createLocateBetweenOp(tc.caseLS1, 0.7d, 2.3d).execute();
        assertEquals("Error in case 1-Dim. geometry with interpolation", tc.expectedForLS1, result);

        result = ops.createLocateBetweenOp(tc.caseLS2, 0.7d, 2.5d).execute();
        assertEquals("Error in case 1-Dim. geometry with interpolation on decreasing M", tc.expectedForLS2, result);

        result = ops.createLocateBetweenOp(tc.caseLS3, -0.7d, -2.3d).execute();
        assertEquals("Error in case 1-Dim. geometry with interpolation on decreasing M", tc.expectedForLS3, result);

        result = ops.createLocateAlongOp(tc.caseLS1, 0.7d).execute();
        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("MultipointM((0.7 0 0.7))"), result);

        result = ops.createLocateAlongOp(tc.caseLS1, 1.0d).execute();
        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("MultipointM((1 0 1))"), result);

        result = ops.createLocateAlongOp(tc.caseLS1, 0.0d).execute();
        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("MultipointM((0 0 0))"), result);


        result = ops.createLocateAlongOp(tc.caseLS1, 4.0d).execute();
        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("Point EMPTY"), result);

        result = ops.createLocateBetweenOp(tc.caseLS4, 2.0d, 3.0d).execute();
        assertEquals("Error in locateAlong on caseLS4", tc.expectedForLS4, result);

        result = ops.createLocateBetweenOp(tc.caseLS5, 1.3d, 2.8d).execute();
        assertEquals("Error in locateAlong on caseLS5", tc.expectedForLS5, result);

        //TODO --add test cases for mixed type GeometryCollections as input for locateBetween/locateAlong

    }


    @Test
    public void testLocateBetweenisNumericallyStable() {
        Geometry result = ops.createLocateBetweenOp(tc.caseD1D, 1d, 2d - 10 * Math.ulp(1.d)).execute();
        GeometryPointEquality geomEq = new GeometryPointEquality(
                new CoordinateWithinTolerancePointEquality(DimensionalFlag.XY, Math.ulp(10d)));
        assertTrue("Error in case d) for 1-Dim. geometry",
                geomEq.equals(
                        Wkt.fromWkt("MULTILINESTRINGM((0.0 0.0 1.0 ,0.9999999999999978 0.9999999999999978 1.9999999999999978))"),
                        result));

    }


}
