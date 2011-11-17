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
public class TestPostgisv15WkbDecoder {


    CodecTestCases testcases = new CodecTestCases();

    @Test
    public void test_point_2d() {
        Bytes bytes = testcases.getWKB(CodecTestCases.POINT_2D);
        Geometry geom = decode(bytes);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.POINT_2D), geom);
        testEncoding(bytes, geom);

    }

    @Test
    public void test_point_XYZ() {
        Bytes bytes = testcases.getWKB(CodecTestCases.POINT_3D);
        Geometry geom = decode(bytes);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.POINT_3D), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_point_XYZM() {
        Bytes bytes = testcases.getWKB(CodecTestCases.POINT_3DM);
        Geometry geom = decode(bytes);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.POINT_3DM), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_point_XYM() {
        Bytes bytes = testcases.getWKB(CodecTestCases.POINT_2DM);
        Geometry geom = decode(bytes);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.POINT_2DM), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_point_XYZM_WITH_SRID() {
        Bytes bytes = testcases.getWKB(CodecTestCases.POINT_WITH_SRID);
        Geometry geom = decode(bytes);
        assertEquals(GeometryType.POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.POINT_WITH_SRID), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_linestring_2d() {
        Bytes bytes = testcases.getWKB(CodecTestCases.LINESTRING_2D);
        Geometry geom = decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.LINESTRING_2D), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_Polygon_2d_no_interior_rings() {
        Bytes bytes = testcases.getWKB(CodecTestCases.POLYGON_2D_NO_INNER_RINGS);
        Geometry geom = decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.POLYGON_2D_NO_INNER_RINGS), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_Polygon_2d_with_interior_ring() {
        Bytes bytes = testcases.getWKB(CodecTestCases.POLYGON_2D_INNER_RINGS);
        Polygon geom = (Polygon) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.POLYGON_2D_INNER_RINGS), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_geometrycollection() {
        Bytes bytes = testcases.getWKB(CodecTestCases.GEOM_COLL_2D_POINTS);
        GeometryCollection geom = (GeometryCollection) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.GEOMETRY_COLLECTION, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.GEOM_COLL_2D_POINTS), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_empty_geometrycollection() {
        Bytes bytes = testcases.getWKB(CodecTestCases.EMPTY_GEOM_COLL);
        GeometryCollection geom = (GeometryCollection) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.GEOMETRY_COLLECTION, geom.getGeometryType());
        assertTrue(geom.isEmpty());
        assertEquals(testcases.getExpected(CodecTestCases.EMPTY_GEOM_COLL), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_multipoint() {
        Bytes bytes = testcases.getWKB(CodecTestCases.MULTIPOINT_2D);
        MultiPoint geom = (MultiPoint) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.MULTIPOINT_2D), geom);
        testEncoding(bytes, geom);
    }

    @Test
    public void test_multipoint_with_srid() {
        Bytes bytes = testcases.getWKB(CodecTestCases.MULTIPOINT_2D_WITH_SRID);
        MultiPoint geom = (MultiPoint) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POINT, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.MULTIPOINT_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        testEncoding(bytes, geom);
    }

    @Test
    public void test_multilinestring() {
        Bytes bytes = testcases.getWKB(CodecTestCases.MULTILINESTRING_2D);
        MultiLineString geom = (MultiLineString) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.MULTILINESTRING_2D), geom);

        testEncoding(bytes, geom);
    }


    @Test
    public void test_multilinestring_with_srid() {
        Bytes bytes = testcases.getWKB(CodecTestCases.MULTILINESTRING_2D_WITH_SRID);
        MultiLineString geom = (MultiLineString) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_LINE_STRING, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.MULTILINESTRING_2D_WITH_SRID), geom);
        assertEquals(4326, geom.getSRID());
        for (Geometry part : geom) {
            assertEquals(4326, geom.getSRID());
        }
        testEncoding(bytes, geom);
    }

    @Test
    public void test_multipolygon() {
        Bytes bytes = testcases.getWKB(CodecTestCases.MULTIPOLYGON_2D);
        MultiPolygon geom = (MultiPolygon) decode(bytes);
        assertNotNull(geom);
        assertEquals(GeometryType.MULTI_POLYGON, geom.getGeometryType());
        assertEquals(testcases.getExpected(CodecTestCases.MULTIPOLYGON_2D), geom);
        testEncoding(bytes, geom);
    }


    private void testEncoding(Bytes bytes, Geometry geom) {
        Bytes out = Wkb.toWkb(geom);
        assertTrue(String.format("Expected: %s, Received: %s", bytes.toString(), out.toString()),
                bytes.hasSameContent(out));
    }


    private Geometry decode(Bytes bytes) {
        return Wkb.fromWkb(bytes);
    }

}
