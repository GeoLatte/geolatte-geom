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

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/27/11
 */
public class TestJTSGeometryOperations {
    
    private static CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;
    private static CoordinateReferenceSystem<C2D> l72 = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

    JTSGeometryOperations ops = new JTSGeometryOperations();

    //retrieve the test geometries from the TestDefaultMeasureGeometryOperations class
    MeasuredTestCases tc = new MeasuredTestCases();



    @Test
    public void testIntersectsOp() {
        LineString<C2D> lineString = linestring(crs, c(1, 1), c(2, 2));
        LineString<C2D> lineString2 = linestring(crs, c(3, 3), c(4, 4));
        assertFalse(ops.intersects(lineString, lineString2));
        LineString<C2D> lineString3 = linestring(crs, c(2, 2), c(4, 4));
        assertTrue(ops.intersects(lineString, lineString3));
    }

    @Test
    public void testTouchesOp() {
        LineString<C2D> lineString = linestring(crs, c(1, 1), c(2, 2));
        LineString<C2D> lineString2 = linestring(crs, c(3, 3), c(4, 4));
        assertFalse(ops.touches(lineString, lineString2));
        LineString<C2D> lineString3 = linestring(crs, c(2, 2), c(4, 4));
        assertTrue(ops.touches(lineString, lineString3));
    }

    @Test
    public void testCrossesOp() {
        LineString<C2D> lineString = linestring(crs, c(1, 1), c(1, 3));
        LineString lineString2 = linestring(crs, c(3, 3), c(4, 4));
        assertFalse(ops.crosses(lineString, lineString2));
        LineString lineString3 = linestring(crs, c(0, 2), c(3, 2));
        assertTrue(ops.crosses(lineString, lineString3));
    }


    @Test
    public void testContainsOp() {
        LineString<C2D> lineString = linestring(crs, c(1, 1), c(1, 2));
        Polygon<C2D> polygon = polygon(ring(crs, c(0, 0), c(0, 4), c(4, 4), c(4, 0), c(0, 0)));
        assertFalse(ops.contains(lineString, polygon));
        assertTrue(ops.contains(polygon, lineString));
    }


    @Test
    public void testOverlapsOp() {
        Polygon<C2D> polygon = polygon(ring(crs, c(-1, -1), c(-1, 2), c(2, 2), c(2, -1), c(-1, -1)));
        Polygon<C2D> polygon2 = polygon(ring(crs, c(0, 0), c(0, 4), c(4, 4), c(4, 0), c(0, 0)));
        assertTrue(ops.overlaps(polygon, polygon2));
        assertTrue(ops.overlaps(polygon2, polygon));
        Polygon<C2D> polygon3 = polygon(ring(crs, c(10, 10), c(10, 14), c(14, 14), c(14, 10), c(10, 10)));
        assertFalse(ops.overlaps(polygon3, polygon2));
        assertFalse(ops.overlaps(polygon2, polygon3));
    }

    @Test
    public void testRelateOp() {
        Polygon<C2D> polygon = polygon(ring(crs, c(-1, -1), c(-1, 2), c(2, 2), c(2, -1), c(-1, -1)));
        Polygon<C2D> polygon2 = polygon(ring(crs, c(0, 0), c(0, 4), c(4, 4), c(4, 0), c(0, 0)));
        assertTrue(ops.relates(polygon, polygon2, "T*T***T**"));
        assertTrue(ops.relates(polygon2, polygon, "T*T***T**"));
        Polygon<C2D> polygon3 = polygon(ring(crs, c(10, 10), c(10, 14), c(14, 14), c(14, 10), c(10, 10)));
        assertFalse(ops.relates(polygon3, polygon2, "T*T***T**"));
        assertFalse(ops.relates(polygon2, polygon3, "T*T***T**"));
    }

    @Test
    public void testDistanceOp() {
        LineString<C2D> lineString = linestring(crs, c(1, 1), c(1, 3));
        LineString<C2D> lineString2 = linestring(crs, c(3, 1), c(3, 4));
        assertEquals(2., ops.distance(lineString, lineString2), 0.00001d);
    }

    @Test
    public void testBufferOp() {
        LineString<C2D> lineString = linestring(crs, c(0, 0), c(1, 1));
        Geometry<C2D> buffer = ops.buffer(lineString, 10d);
        assertTrue(ops.contains(buffer, lineString));
        assertEquals(JTS.from(JTS.to(lineString).buffer(10d), crs), buffer);
    }


    @Test
    public void testConvexHullOp() {
        LineString<C2D> lineString = linestring(crs, c(0, 0), c(1, 1));
        Geometry<C2D> hull = ops.convexHull(lineString);
        assertTrue(ops.contains(hull, lineString));
        assertEquals(JTS.from(JTS.to(lineString).convexHull(), crs), hull);
    }

    @Test
    public void testIntersectionOp() {
        Polygon<C2D> polygon1 = polygon(ring(crs, c(0, 0), c(5, 0), c(5, 5), c(0, 5), c(0, 0)));
        Polygon<C2D> polygon2 = polygon(ring(crs, c(3, 3), c(7, 3), c(7, 7), c(3, 7), c(3, 3)));
        Polygon<C2D> polygon3 = polygon(ring(l72, c(3, 3), c(7, 3), c(7, 7), c(3, 7), c(3, 3)));
        Polygon<C2D> expected = polygon(ring(crs, c(3, 5), c(5, 5), c(5, 3), c(3, 3), c(3, 5)));
        assertEquals("Intersection returned incorrect result", expected,
                ops.intersection(polygon1, polygon2));

        try {
            ops.intersection(polygon1, polygon3);
            fail("Intersection did not check for compatible CRSs");
        } catch (IllegalArgumentException e) {
        }

        Polygon<C2D> polygon4 = new Polygon(crs);
        assertEquals("Intersection with empty is not a GeometryCollection", new GeometryCollection<C2D, Geometry<C2D>>(crs),
                ops.intersection(polygon1, polygon4));
    }

    @Test
    public void testUnionOp() {
        Polygon<C2D> polygon1 = polygon(ring(crs, c(0, 0), c(5, 0), c(5, 5), c(0, 5), c(0, 0)));
        Polygon<C2D> polygon2 = polygon(ring(crs, c(3, 3), c(7, 3), c(7, 7), c(3, 7), c(3, 3)));
        Polygon<C2D> expected = polygon(ring(crs, c(5, 3), c(5, 0), c(0, 0), c(0, 5), c(3, 5),
                c(3, 7), c(7, 7), c(7, 3), c(5, 3)));
        assertEquals(expected, ops.union(polygon1, polygon2));
    }

    @Test
    public void testDifferenceOp() {
        Polygon<C2D> pg1 = polygon(ring(crs, c(0, 0), c(5, 0), c(5, 5), c(0, 5), c(0, 0)));
        Polygon<C2D> pg2 = polygon(ring(crs, c(0, 3), c(0, 6), c(5, 6), c(5, 3), c(0, 3)));
        Polygon<C2D> expected = polygon(ring(crs, c(5, 3), c(5, 0), c(0, 0), c(0, 3), c(5, 3)));
        assertEquals(expected, ops.difference(pg1, pg2));
        assertEquals(pg1, ops.difference(pg1, new Polygon<C2D>(crs)));
        assertEquals(new GeometryCollection<C2D, Geometry<C2D>>(crs),
                ops.difference(new Polygon<C2D>(crs), pg2));
    }

    @Test
    public void testSymDifferenceOp() {
        Polygon<C2D> pg1 = polygon(ring(crs, c(0, 0), c(6, 0), c(6, 6), c(0, 6), c(0, 0)));
        Polygon<C2D> pg2 = polygon(ring(crs, c(0, 3), c(6, 3), c(6, 8), c(0, 8), c(0, 3)));
        Polygon<C2D> expected1 = polygon(ring(crs, c(6, 3), c(6, 0), c(0, 0), c(0, 3), c(6, 3)));
        Polygon<C2D> expected2 = polygon(ring(crs, c(6, 6), c(0, 6), c(0, 8), c(6, 8), c(6, 6)));
        MultiPolygon expected = new MultiPolygon(new Polygon[]{expected1, expected2});
        assertEquals(expected, ops.symmetricDifference(pg1, pg2));
        assertEquals(pg1, ops.symmetricDifference(pg1, new Polygon<C2D>(crs)));
        assertEquals(pg2, ops.symmetricDifference(new Polygon<C2D>(crs), pg2));
    }

    @Test
    public void testReverse() {
        LineString<C2D> l = linestring(crs, c(0, 0), c(1, 0), c(1, 3));
        assertEquals(linestring(crs, (c(1, 3)), c(1, 0), c(0, 0)), ops.reverse(l));
        assertEquals(l, ops.reverse(ops.reverse(l)));

        LineString<C2D> l2 = linestring(crs, c(1, 10), c(3, 30), c(5, 40));
        MultiLineString<C2D> ml = new MultiLineString(l, l2);

        LineString<C2D> rev2 = ops.reverse(l2);
        LineString<C2D> rev1 = ops.reverse(l);
        MultiLineString<C2D> expectedMl = new MultiLineString(rev2, rev1);
        assertEquals(expectedMl, ops.reverse(ml));
        assertEquals(ml, ops.reverse(ops.reverse(ml)));

        Polygon<C2D> p = polygon(crs, ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0)));
        assertEquals(polygon(crs, ring(c(0, 0), c(0, 1), c(1, 1), c(1, 0), c(0, 0))), ops.reverse(p));

        GeometryCollection<C2D, Geometry<C2D>> coll = geometrycollection(l, p, ml);
        assertEquals(geometrycollection(ops.reverse(ml), ops.reverse(p), ops.reverse(l)), ops.reverse(coll));

    }

    //TODO -- fix these unit tests (they no longer belong in this class).

//
//    @Test
//        public void testLocateAlongOpOnEmptyLineStringReturnsEmptyGeometry() {
//            Geometry result = ops.createLocateAlongOp(tc.emptyLineString, 10d);
//            assertTrue("Non-empty result on empty Geometry.", result.isEmpty());
//        }
//
//    @Test
//    public void testLocateBetweenOpOnEmptyLineStringReturnsEmptyGeometry() {
//        Geometry result = ops.createLocateBetweenOp(tc.emptyLineString, 10d, 12d);
//        assertTrue("Non-empty result on empty Geometry.", result.isEmpty());
//    }
//
//    /**
//     * The next test method implements the test cases for 0-Dim. geometries
//     * listed in section 4.2.1.7.3 of the SQL/MM-Spatial Data spec.
//     */
//    @Test
//    public void testLocateAlongOn0DimensionalGeometries() {
//        Geometry result = ops.createLocateAlongOp(tc.caseD0A, 4d);
//        assertEquals("Error in case a) for 0-Dim. geometry", tc.expectedForD0A, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD0B, 2d, 4d);
//        assertEquals("Error in case b) for 0-Dim. geometry", tc.expectedForD0B, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD0C, 1d, 4d);
//        assertEquals("Error in case c) for 0-Dim. geometry", tc.expectedForD0C, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD0D, 7d, 7d);
//        assertEquals("Error in case d) for 0-Dim. geometry", tc.expectedForD0D, result);
//
//    }
//
//    /**
//     * The next test method implements the test cases for 0-Dim. geometries
//     * listed in section 4.2.1.7.4 of the SQL/MM-Spatial Data spec.
//     */
//    @Test
//    public void testLocateOn1DimensionalGeometriesSQLMMCompliant() {
//        Geometry result = ops.createLocateAlongOp(tc.caseD1A, 4d);
//        assertEquals("Error in case a) for 1-Dim. geometry", tc.expectedForD1A, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD1B, 2d, 4d);
//        assertEquals("Error in case b) for 1-Dim. geometry", tc.expectedForD1B, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD1B, 4d, 2d);
//        assertEquals("Error in case b) for 1-Dim. geometry when start/end reversed", tc.expectedForD1B, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD1C, 6d, 9d);
//        assertEquals("Error in case c) for 1-Dim. geometry", tc.expectedForD1C, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD1D, 1d, 2d);
//        assertEquals("Error in case d) for 1-Dim. geometry", tc.expectedForD1D, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD1E, 2d, 4d);
//        assertEquals("Error in case e) for 1-Dim. geometry", tc.expectedForD1E, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD1F, 1d, 3d);
//        assertEquals("Error in case f) for 1-Dim. geometry", tc.expectedForD1F, result);
//
//        result = ops.createLocateBetweenOp(tc.caseD1G, 7d, 9d);
//        assertEquals("Error in case g) for 1-Dim. geometry", tc.expectedForD1G, result);
//
//
//    }
//
//    @Test
//    public void testLocateOn1DimensionalGeometries() {
//        Geometry result = ops.createLocateBetweenOp(tc.caseLS1, 0.7d, 2.3d);
//        assertEquals("Error in case 1-Dim. geometry with interpolation", tc.expectedForLS1, result);
//
//        result = ops.createLocateBetweenOp(tc.caseLS2, 0.7d, 2.5d);
//        assertEquals("Error in case 1-Dim. geometry with interpolation on decreasing M", tc.expectedForLS2, result);
//
//        result = ops.createLocateBetweenOp(tc.caseLS3, -0.7d, -2.3d);
//        assertEquals("Error in case 1-Dim. geometry with interpolation on decreasing M", tc.expectedForLS3, result);
//
//        result = ops.createLocateAlongOp(tc.caseLS1, 0.7d);
//        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("MultipointM((0.7 0 0.7))"), result);
//
//        result = ops.createLocateAlongOp(tc.caseLS1, 1.0d);
//        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("MultipointM((1 0 1))"), result);
//
//        result = ops.createLocateAlongOp(tc.caseLS1, 0.0d);
//        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("MultipointM((0 0 0))"), result);
//
//
//        result = ops.createLocateAlongOp(tc.caseLS1, 4.0d);
//        assertEquals("Error in locateAlong on caseLS1", Wkt.fromWkt("Point EMPTY"), result);
//
//        result = ops.createLocateBetweenOp(tc.caseLS4, 2.0d, 3.0d);
//        assertEquals("Error in locateAlong on caseLS4", tc.expectedForLS4, result);
//
//        result = ops.createLocateBetweenOp(tc.caseLS5, 1.3d, 2.8d);
//        assertEquals("Error in locateAlong on caseLS5", tc.expectedForLS5, result);
//
//        //TODO --add test cases for mixed type GeometryCollections as input for locateBetween/locateAlong
//
//    }
//
//
//    @Test
//    public void testLocateBetweenisNumericallyStable() {
//        Geometry result = ops.createLocateBetweenOp(tc.caseD1D, 1d, 2d - 10 * Math.ulp(1.d));
//        GeometryPointEquality geomEq = new GeometryPointEquality(
//                new WithinTolerancePositionEquality(Math.ulp(10d)));
//        assertTrue("Error in case d) for 1-Dim. geometry",
//                geomEq.equals(
//                        Wkt.fromWkt("MULTILINESTRINGM((0.0 0.0 1.0 ,0.9999999999999978 0.9999999999999978 1.9999999999999978))"),
//                        result));
//
//    }


}
