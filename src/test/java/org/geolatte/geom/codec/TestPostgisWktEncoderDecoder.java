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
import org.geolatte.geom.support.CodecTestInputs;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class TestPostgisWktEncoderDecoder {

    CodecTestInputs testcases = new CodecTestInputs();

    @Test
    public void testDecodeTwice() throws Exception {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_2D);

        PostgisWktDecoder postgisWktDecoder = new PostgisWktDecoder();
        Geometry decodedBefore = postgisWktDecoder.decode(wkt);

        //Use decoder to decode point with srid
        postgisWktDecoder.decode(testcases.getWKT(CodecTestInputs.POINT_WITH_SRID));

        Geometry decodedAfter = postgisWktDecoder.decode(wkt);
        assertEquals(decodedBefore, decodedAfter);
    }

    @Test
    public void testEncodeTwice() throws Exception {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_2D);
        Geometry geom = new PostgisWktDecoder().decode(wkt);

        PostgisWktEncoder postgisWktEncoder = new PostgisWktEncoder();
        String encodedBefore = postgisWktEncoder.encode(geom);

        postgisWktEncoder.encode(new PostgisWktDecoder().decode(testcases.getWKT(CodecTestInputs.POINT_WITH_SRID)));

        String encodedAfter = postgisWktEncoder.encode(geom);
        assertEquals(encodedBefore, encodedAfter);
    }

    @Test
    public void test_point_2d() {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_2D);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_2D), geom);
        testEncoding(wkt, geom);
    }

    private void testEncoding(String wkt, Geometry geom) {
        assertEquals(wkt, Wkt.toWkt(geom));
    }

    @Test
    public void test_point_3d() {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_3D);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_3D), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_point_2dm() {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_2DM);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_2DM), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_point_3dm() {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_3DM);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_3DM), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_linestring_2d() {
        String wkt = testcases.getWKT(CodecTestInputs.LINESTRING_2D);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.LINESTRING_2D), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_point_XYZM_WITH_SRID() {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_WITH_SRID);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertTrue("Result of 4-dim point wkt is not measured.", geom.isMeasured());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_WITH_SRID), geom);
        testEncoding(wkt, geom);
    }


    @Test
    public void test_polygon_2d_no_inner_rings() {
        String wkt = testcases.getWKT(CodecTestInputs.POLYGON_2D_NO_INNER_RINGS);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POLYGON_2D_NO_INNER_RINGS), geom);
        testEncoding(wkt, geom);

    }

    @Test
    public void test_polyogn_2d_inner_rings() {
        String wkt = testcases.getWKT(CodecTestInputs.POLYGON_2D_INNER_RINGS);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POLYGON_2D_INNER_RINGS), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_geometry_collection() {
        String wkt = testcases.getWKT(CodecTestInputs.GEOM_COLL_2D_POINTS);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.GEOMETRY_COLLECTION, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.GEOM_COLL_2D_POINTS), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_empty_geometry_collection() {
        String wkt = testcases.getWKT(CodecTestInputs.EMPTY_GEOM_COLL);
        Geometry geom = decode(wkt);
        assertEquals(GeometryType.GEOMETRY_COLLECTION, geom.getGeometryType());
        assertTrue(geom.isEmpty());
        testEncoding(wkt, geom);
    }

    @Test
    public void test_multipoint_with_srid() {
        String wkt = testcases.getWKT(CodecTestInputs.MULTIPOINT_2D_WITH_SRID);
        MultiPoint geom = (MultiPoint) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTIPOINT_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        testEncoding(wkt, geom);
    }


    @Test
    public void test_multilinestring() {
        String wkt = testcases.getWKT(CodecTestInputs.MULTILINESTRING_2D);
        MultiLineString geom = (MultiLineString) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTILINESTRING_2D), geom);
        testEncoding(wkt, geom);
    }


    @Test
    public void test_multilinestring_with_srid() {
        String wkt = testcases.getWKT(CodecTestInputs.MULTILINESTRING_2D_WITH_SRID);
        MultiLineString geom = (MultiLineString) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTILINESTRING_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        for (Geometry part : geom) {
            assertEquals(4326, geom.getSRID());
        }
        testEncoding(wkt, geom);
    }

    @Test
    public void test_multipolygon() {
        String wkt = testcases.getWKT(CodecTestInputs.MULTIPOLYGON_2D);
        MultiPolygon geom = (MultiPolygon) decode(wkt);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTIPOLYGON_2D), geom);
        testEncoding(wkt, geom);
    }

    @Test
    public void test_irregular_white_space(){
        String wkt = testcases.getWKT(CodecTestInputs.LINESTRING_IRREGULAR_WHITE_SPACE_1) ;
        LineString geom = (LineString)decode(wkt);
        assertNotNull(geom);
        String normalizedWkt = "LINESTRING(-29.261 66,-71.1074 -20.255)";
        assertEquals(testcases.getExpected(CodecTestInputs.LINESTRING_IRREGULAR_WHITE_SPACE_1), geom);
        testEncoding(normalizedWkt, geom);
    }

    @Test
    public void test_scientific_notation() {
        String wkt = testcases.getWKT(CodecTestInputs.POINT_SCIENTIFIC_NOTATION);
        Point pnt = (Point) decode(wkt);
        assertNotNull(pnt);
        GeometryPointEquality eq = new GeometryPointEquality(
                new CoordinateWithinTolerancePointEquality(DimensionalFlag.XY, 0.00000001));
        assertTrue(eq.equals(testcases.getExpected(CodecTestInputs.POINT_SCIENTIFIC_NOTATION), pnt));
    }

    @Test(expected= WktDecodeException.class)
    public void test_invalid_point() {
        String wkt = testcases.getWKT(CodecTestInputs.INVALID_POINT);
        decode(wkt);
    }

    @Test(expected= WktDecodeException.class)
    public void test_invalid_polygon() {
        String wkt = testcases.getWKT(CodecTestInputs.INVALID_POLYGON);
        decode(wkt);
    }

    private Geometry decode(String wkt) {
        return Wkt.fromWkt(wkt);
    }


}
