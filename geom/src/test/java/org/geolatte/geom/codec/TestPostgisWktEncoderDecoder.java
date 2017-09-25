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

package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.ProjectedCoordinateReferenceSystem;
import org.geolatte.geom.crs.Unit;
import org.geolatte.geom.support.PostgisTestCases;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasMeasureAxis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class TestPostgisWktEncoderDecoder {

    PostgisTestCases testcases = new PostgisTestCases();

    @Test
    public void testDecodeTwice() throws Exception {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_2D);

        PostgisWktDecoder postgisWktDecoder = new PostgisWktDecoder();
        Geometry decodedBefore = postgisWktDecoder.decode(wkt);

        //Use decoder to decode point with srid
        postgisWktDecoder.decode(testcases.getWKT(PostgisTestCases.POINT_WITH_SRID));

        Geometry decodedAfter = postgisWktDecoder.decode(wkt);
        assertEquals(decodedBefore, decodedAfter);
    }

    @Test
    public void testEncodeTwice() throws Exception {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_2D);
        Geometry geom = new PostgisWktDecoder().decode(wkt);

        PostgisWktEncoder postgisWktEncoder = new PostgisWktEncoder();
        String encodedBefore = postgisWktEncoder.encode(geom);

        postgisWktEncoder.encode(new PostgisWktDecoder().decode(testcases.getWKT(PostgisTestCases.POINT_WITH_SRID)));

        String encodedAfter = postgisWktEncoder.encode(geom);
        assertEquals(encodedBefore, encodedAfter);
    }

    @Test
    public void testForceCRS() {
        String wkt = "SRID=31300;LINESTRING(10 10, 20 20, 30 30)";
        WktDecoder decoder = new PostgisWktDecoder();
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        Geometry<C2D> geometry = decoder.decode(wkt, crs);
        assertEquals(geometry.getGeometryType(), GeometryType.LINESTRING);
        assertEquals(geometry.getCoordinateReferenceSystem(), crs);
    }

    @Test
    public void testForceCRSWithMeasuredAxisWith2DWkt() {
        String wkt = "SRID=31300;LINESTRING(10 10, 20 20, 30 30)";
        WktDecoder decoder = new PostgisWktDecoder();
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        CoordinateReferenceSystem<C2DM> crsWithM = addLinearSystem(crs, C2DM.class, Unit.METER);

        Geometry<C2DM> geometry = decoder.decode(wkt, crsWithM);
        assertEquals(GeometryType.LINESTRING, geometry.getGeometryType());
        assertEquals(crsWithM, geometry.getCoordinateReferenceSystem());
    }

    @Test
    public void testForceCRSWithMeasuredAxisWith2DMWkt() {
        String wkt = "SRID=31300;LINESTRINGM(10 10 1, 20 20 2, 30 30 3)";
        WktDecoder decoder = new PostgisWktDecoder();
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        CoordinateReferenceSystem<C2DM> crsWithM =  addLinearSystem(crs, C2DM.class, Unit.METER);

        Geometry<C2DM> geometry = decoder.decode(wkt, crsWithM);
        assertEquals(GeometryType.LINESTRING, geometry.getGeometryType());
        assertEquals(crsWithM, geometry.getCoordinateReferenceSystem());
    }


    @Test(expected = WktDecodeException.class)
    public void testForceCRSWithMeasuredAxisButTargetBaseCRS() {
        String wkt = "SRID=31300;LINESTRINGM(10 10 1, 20 20 2, 30 30 3)";
        WktDecoder decoder = new PostgisWktDecoder();
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        Geometry<C2D> geometry = decoder.decode(wkt, crs);
        assertEquals(GeometryType.LINESTRING, geometry.getGeometryType());
        assertEquals(crs, geometry.getCoordinateReferenceSystem());
    }

    @Test
    public void testFromWktCRS() {
        String wkt = "SRID=31300;LINESTRING(10 10, 20 20, 30 30)";
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        Geometry<C2D> geometry = Wkt.fromWkt(wkt, crs);
        assertEquals(geometry.getGeometryType(), GeometryType.LINESTRING);
        assertEquals(geometry.getCoordinateReferenceSystem(), crs);
    }

    @Test
    public void testFromWktCRSWithMeasuredAxisWith2DWkt() {
        String wkt = "SRID=31300;LINESTRING(10 10, 20 20, 30 30)";
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        CoordinateReferenceSystem<C2DM> crsWithM =  addLinearSystem(crs, C2DM.class, Unit.METER);
        Geometry<C2DM> geometry = Wkt.fromWkt(wkt, crsWithM);
        assertEquals(GeometryType.LINESTRING, geometry.getGeometryType());
        assertEquals(crsWithM, geometry.getCoordinateReferenceSystem());
    }

    @Test
    public void testFromWktCRSWithMeasuredAxisWith2DMWkt() {
        String wkt = "SRID=31300;LINESTRINGM(10 10 1, 20 20 2, 30 30 3)";
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        CoordinateReferenceSystem<C2DM> crsWithM =  addLinearSystem(crs, C2DM.class, Unit.METER);

        Geometry<C2DM> geometry = Wkt.fromWkt(wkt, crsWithM);
        assertEquals(GeometryType.LINESTRING, geometry.getGeometryType());
        assertEquals(crsWithM, geometry.getCoordinateReferenceSystem());
    }


    @Test(expected = WktDecodeException.class)
    public void testFromWktCRSWithMeasuredAxisButTargetBaseCRS() {
        String wkt = "SRID=31300;LINESTRINGM(10 10 1, 20 20 2, 30 30 3)";
        ProjectedCoordinateReferenceSystem crs = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
        Geometry<C2D> geometry = Wkt.fromWkt(wkt, crs);
        assertEquals(GeometryType.LINESTRING, geometry.getGeometryType());
        assertEquals(crs, geometry.getCoordinateReferenceSystem());
    }

    @Test
    public void test_point_2d() {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_2D);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_2D), geom);
        testEncoding(wkt, geom);
    }


    @Test
    public void test_point_3d() {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_3D);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_3D), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_point_2dm() {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_2DM);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_2DM), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_point_3dm() {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_3DM);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_3DM), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_linestring_2d() {
        String wkt = testcases.getWKT(PostgisTestCases.LINESTRING_2D);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.LINESTRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.LINESTRING_2D), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_point_XYZM_WITH_SRID() {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_WITH_SRID);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertTrue("Result of 4-dim point wkt is not measured.", hasMeasureAxis(geom.getCoordinateReferenceSystem()));
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_WITH_SRID), geom);
        testEncoding(wkt, geom);
    }


    @Test
    public void test_polygon_2d_no_inner_rings() {
        String wkt = testcases.getWKT(PostgisTestCases.POLYGON_2D_NO_INNER_RINGS);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POLYGON_2D_NO_INNER_RINGS), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_polyogn_2d_inner_rings() {
        String wkt = testcases.getWKT(PostgisTestCases.POLYGON_2D_INNER_RINGS);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POLYGON_2D_INNER_RINGS), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_geometry_collection() {
        String wkt = testcases.getWKT(PostgisTestCases.GEOM_COLL_2D_POINTS);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.GEOMETRYCOLLECTION, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.GEOM_COLL_2D_POINTS), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_empty_geometry_collection() {
        String wkt = testcases.getWKT(PostgisTestCases.EMPTY_GEOM_COLL);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.GEOMETRYCOLLECTION, geom.getGeometryType());
        assertTrue(geom.isEmpty());
        testEncoding(wkt, geom);
    }

    @Test
    public void test_multipoint_with_srid() {
        String wkt = testcases.getWKT(PostgisTestCases.MULTIPOINT_2D_WITH_SRID);
        MultiPoint geom = (MultiPoint) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTIPOINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTIPOINT_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        testEncoding(wkt, geom);
    }


    @Test
    public void test_multilinestring() {
        String wkt = testcases.getWKT(PostgisTestCases.MULTILINESTRING_2D);
        MultiLineString geom = (MultiLineString) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTILINESTRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTILINESTRING_2D), geom);
        testEncoding(wkt, geom);
    }


    @Test
    public void test_multilinestring_with_srid() {
        String wkt = testcases.getWKT(PostgisTestCases.MULTILINESTRING_2D_WITH_SRID);
        MultiLineString<?> geom = (MultiLineString<?>) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTILINESTRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTILINESTRING_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        for (Geometry part : geom) {
            assertEquals(4326, part.getSRID());
        }
        testEncoding(wkt, geom);
    }

    @Test
    public void test_multipolygon() {
        String wkt = testcases.getWKT(PostgisTestCases.MULTIPOLYGON_2D);
        MultiPolygon geom = (MultiPolygon) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTIPOLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTIPOLYGON_2D), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_irregular_white_space() {
        String wkt = testcases.getWKT(PostgisTestCases.LINESTRING_IRREGULAR_WHITE_SPACE_1);
        LineString geom = (LineString) decode(wkt);
        assertNotNull(geom);
        String normalizedWkt = "LINESTRING(-29.261 66,-71.1074 -20.255)";
        assertEquals(testcases.getExpected(PostgisTestCases.LINESTRING_IRREGULAR_WHITE_SPACE_1), geom);
        testEncoding(normalizedWkt, geom);
    }

    @Test
    public void test_scientific_notation() {
        String wkt = testcases.getWKT(PostgisTestCases.POINT_SCIENTIFIC_NOTATION);
        Point pnt = (Point) decode(wkt);
        assertNotNull(pnt);
        GeometryPointEquality eq = new GeometryPointEquality(
                new WithinTolerancePositionEquality(0.00000001));
        assertTrue(eq.equals(testcases.getExpected(PostgisTestCases.POINT_SCIENTIFIC_NOTATION), pnt));
    }

    @Test(expected = WktDecodeException.class)
    public void test_invalid_point() {
        String wkt = testcases.getWKT(PostgisTestCases.INVALID_POINT);
        decode(wkt);
    }

    @Test(expected = WktDecodeException.class)
    public void test_invalid_polygon() {
        String wkt = testcases.getWKT(PostgisTestCases.INVALID_POLYGON);
        decode(wkt);
    }

    @Test
    public void test_empty_point() {
        String wkt = testcases.getWKT(PostgisTestCases.EMPTY_POINT);
        Geometry geom = decode(wkt);
        assertTrue(geom.isEmpty());
        assertTrue(geom.getGeometryType() == GeometryType.POINT);
    }

    private Geometry decode(String wkt) {
        return Wkt.fromWkt(wkt);
    }


    private void testEncoding(String wkt, Geometry geom) {
        assertEquals(wkt, Wkt.toWkt(geom));
    }


}
