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
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.Unit;
import org.geolatte.geom.support.PostgisTestCases;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Nov 11, 2010
 */
public class TestPostgisWkbEncoderDecoder {


    PostgisTestCases testcases = new PostgisTestCases();

    @Test
    public void test_point_2d() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.POINT_2D);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYZ() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.POINT_3D);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_3D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYZM() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.POINT_3DM);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_3DM), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYM() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.POINT_2DM);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_2DM), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_point_XYZM_WITH_SRID() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.POINT_WITH_SRID);
        Geometry geom = decode(byteBuffer);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POINT_WITH_SRID), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_linestring_2d() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.LINESTRING_2D);
        Geometry geom = decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.LINESTRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.LINESTRING_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_Polygon_2d_no_interior_rings() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.POLYGON_2D_NO_INNER_RINGS);
        Geometry geom = decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POLYGON_2D_NO_INNER_RINGS), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_Polygon_2d_with_interior_ring() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.POLYGON_2D_INNER_RINGS);
        Polygon geom = (Polygon) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.POLYGON_2D_INNER_RINGS), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_geometrycollection() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.GEOM_COLL_2D_POINTS);
        GeometryCollection geom = (GeometryCollection) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.GEOMETRYCOLLECTION, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.GEOM_COLL_2D_POINTS), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_empty_geometrycollection() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.EMPTY_GEOM_COLL);
        GeometryCollection geom = (GeometryCollection) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.GEOMETRYCOLLECTION, geom.getGeometryType());
        assertTrue(geom.isEmpty());
        assertEquals(testcases.getExpected(PostgisTestCases.EMPTY_GEOM_COLL), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multipoint() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.MULTIPOINT_2D);
        MultiPoint geom = (MultiPoint) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTIPOINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTIPOINT_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multipoint_with_srid() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.MULTIPOINT_2D_WITH_SRID);
        MultiPoint geom = (MultiPoint) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTIPOINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTIPOINT_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multilinestring() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.MULTILINESTRING_2D);
        MultiLineString geom = (MultiLineString) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTILINESTRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTILINESTRING_2D), geom);

        testEncoding(byteBuffer, geom);
    }


    @Test
    public void test_multilinestring_with_srid() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.MULTILINESTRING_2D_WITH_SRID);
        MultiLineString<?> geom = (MultiLineString<?>) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTILINESTRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTILINESTRING_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        for (Geometry<?> part : geom) {
            assertEquals(4326, part.getSRID());
        }
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void test_multipolygon() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.MULTIPOLYGON_2D);
        MultiPolygon geom = (MultiPolygon) decode(byteBuffer);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTIPOLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(PostgisTestCases.MULTIPOLYGON_2D), geom);
        testEncoding(byteBuffer, geom);
    }

    @Test
    public void testReuseDecoder() throws Exception {
        PostgisWkbDecoder decoder = new PostgisWkbDecoder();

        ByteBuffer pointWithNoSridBuffer = testcases.getWKB(PostgisTestCases.POINT_2D);
        Geometry decodedBefore = decoder.decode(pointWithNoSridBuffer);

        //Use decoder to decode point with srid
        decoder.decode(testcases.getWKB(PostgisTestCases.POINT_WITH_SRID));

        Geometry decodedAfter = decoder.decode(pointWithNoSridBuffer);
        assertEquals(decodedBefore, decodedAfter);
    }

    @Test(expected = WkbDecodeException.class)
    public void test_invalid_point() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.INVALID_POINT);
        Geometry geom = decode(byteBuffer);
    }

    @Test(expected = WkbDecodeException.class)
    public void test_invalid_polygon() {
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.INVALID_POLYGON);
        Geometry geom = decode(byteBuffer);
    }

    @Test
    public void test_empty_point() {
        Geometry<G2D> g = new Point<G2D>(CoordinateReferenceSystems.mkGeographic(Unit.DEGREE));
        ByteBuffer buf = Wkb.toWkb(g);
        ByteBuffer byteBuffer = testcases.getWKB(PostgisTestCases.EMPTY_POINT);
        Geometry geom = decode(byteBuffer);
        assertTrue(geom.isEmpty());
        testEncoding(byteBuffer, geom);
    }


    private void testEncoding(ByteBuffer byteBuffer, Geometry geom) {
        ByteBuffer out = Wkb.toWkb(geom);
        assertTrue(String.format("Expected: %s, Received: %s", byteBuffer.toString(), out.toString()),
                byteBuffer.hasSameContent(out));
    }


    private Geometry<?> decode(ByteBuffer byteBuffer) {
        return Wkb.fromWkb(byteBuffer);
    }

}
