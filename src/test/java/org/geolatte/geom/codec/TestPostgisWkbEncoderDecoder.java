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
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Nov 11, 2010
 */
public class TestPostgisWkbEncoderDecoder {


    CodecTestInputs testcases = new CodecTestInputs();

    @Test
    public void test_point_2d() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.POINT_2D);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYZ() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.POINT_3D);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_3D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYZM() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.POINT_3DM);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_3DM), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYM() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.POINT_2DM);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_2DM), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYZM_WITH_SRID() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.POINT_WITH_SRID);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POINT_WITH_SRID), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_linestring_2d() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.LINESTRING_2D);
        Geometry geom = decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.LINESTRING_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_Polygon_2d_no_interior_rings() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.POLYGON_2D_NO_INNER_RINGS);
        Geometry geom = decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POLYGON_2D_NO_INNER_RINGS), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_Polygon_2d_with_interior_ring() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.POLYGON_2D_INNER_RINGS);
        Polygon geom = (Polygon) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.POLYGON_2D_INNER_RINGS), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_geometrycollection() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.GEOM_COLL_2D_POINTS);
        GeometryCollection geom = (GeometryCollection) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.GEOMETRY_COLLECTION, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.GEOM_COLL_2D_POINTS), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_empty_geometrycollection() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.EMPTY_GEOM_COLL);
        GeometryCollection geom = (GeometryCollection) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.GEOMETRY_COLLECTION, geom.getGeometryType());
        assertTrue(geom.isEmpty());
        assertEquals(testcases.getExpected(CodecTestInputs.EMPTY_GEOM_COLL), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multipoint() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.MULTIPOINT_2D);
        MultiPoint geom = (MultiPoint) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTIPOINT_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multipoint_with_srid() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.MULTIPOINT_2D_WITH_SRID);
        MultiPoint geom = (MultiPoint) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTIPOINT_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multilinestring() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.MULTILINESTRING_2D);
        MultiLineString geom = (MultiLineString) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTILINESTRING_2D), geom);

        testEncoding(byteBuffer, geom);
    }


    @Test
    public void test_multilinestring_with_srid() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.MULTILINESTRING_2D_WITH_SRID);
        MultiLineString geom = (MultiLineString) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTILINESTRING_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        for (Geometry part : geom) {
            assertEquals(4326, geom.getSRID());
        }
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multipolygon() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.MULTIPOLYGON_2D);
        MultiPolygon geom = (MultiPolygon) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestInputs.MULTIPOLYGON_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void testReuseDecoder() throws Exception {
        PostgisWkbDecoder decoder = new PostgisWkbDecoder();

        ByteBuffer pointWithNoSridBuffer = testcases.getWKB(CodecTestInputs.POINT_2D);
        Geometry decodedBefore = decoder.decode(pointWithNoSridBuffer);

        //Use decoder to decode point with srid
        decoder.decode(testcases.getWKB(CodecTestInputs.POINT_WITH_SRID));

        Geometry decodedAfter = decoder.decode(pointWithNoSridBuffer);
        assertEquals(decodedBefore, decodedAfter);
    }

    @Test(expected = WkbDecodeException.class)
    public void test_invalid_point() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.INVALID_POINT);
        Geometry geom = decode(byteBuffer);
        System.out.println(geom.toString());
    }

    @Test(expected = WkbDecodeException.class)
    public void test_invalid_polygon() {
        ByteBuffer byteBuffer = testcases.getWKB(CodecTestInputs.INVALID_POLYGON);
        Geometry geom = decode(byteBuffer);
        System.out.println(geom.toString());
    }

    private void testEncoding(ByteBuffer byteBuffer, Geometry geom) {
        ByteBuffer out = Wkb.toWkb(geom);
        assertTrue(String.format("Expected: %s, Received: %s", byteBuffer.toString(), out.toString()),
                byteBuffer.hasSameContent(out));
    }


    private Geometry decode(ByteBuffer byteBuffer) {
        return Wkb.fromWkb(byteBuffer);
    }

}
