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

package org.geolatte.geom.jts;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.WktDecodeException;
import org.geolatte.geom.codec.WktDecoder;
import org.geolatte.geom.support.CodecTestInputs;
import org.geolatte.geom.support.PostgisJDBCUnitTestInputs;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the JTS conversion
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/21/11
 */
public class TestJTS {


    WktDecoder<Geometry> wktDecoder = Wkt.newWktDecoder();
    WKTReader jtsWktDecoder = new WKTReader();


    @Test
    public void test_postgis_cases() {
        testInputs(new PostgisJDBCUnitTestInputs());
    }

    @Test
    public void test_codec_cases() {
        try {
            testInputs(new CodecTestInputs());
        } catch (WktDecodeException e) {
            // ignore. The CodecTestInputs contain invalid WKT's to test exception handling. We only need the valid examples.
        }
    }

    @Test
    public void test_measured_2d() {
        CodecTestInputs testInputs = new CodecTestInputs();
        Geometry geometry = testInputs.getExpected(testInputs.LINESTRING_3DM);
        com.vividsolutions.jts.geom.Geometry jtsGeometry = JTS.to(geometry);
        assertTrue(DimensionalCoordinate.class.isInstance(jtsGeometry.getCoordinates()[0]));
        DimensionalCoordinate dc = (DimensionalCoordinate) jtsGeometry.getCoordinates()[0];
        assertEquals(DimensionalFlag.XYZM, dc.getDimensionalFlag());
        assertEquals(dc.getM(), 2, Math.ulp(2));
    }

    @Test
    public void test_empty_polygon() {
        test_empty(Polygon.createEmpty());
    }

    @Test
    public void test_empty_point() {
        test_empty(Point.createEmpty());
    }

    @Test
    public void test_empty_linestring() {
        test_empty(LineString.createEmpty());
    }

    @Test
    public void test_empty_linearRing() {
        test_empty(LinearRing.createEmpty());
    }

    @Test
    public void test_empty_geometryCollection() {
        test_empty(GeometryCollection.createEmpty());
    }

    @Test
    public void test_empty_multipoint() {
        test_empty(MultiPoint.createEmpty());
    }

    @Test
    public void test_empty_multilinestring() {
        test_empty(MultiLineString.createEmpty());
    }

    @Test
    public void test_empty_multipolygon() {
        test_empty(MultiPolygon.createEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_arguments_to() {
        JTS.to((Geometry) null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_arguments_from() {
        JTS.from((com.vividsolutions.jts.geom.Geometry) null);

    }

    private void test_empty(Geometry empty) {
        com.vividsolutions.jts.geom.Geometry jts = JTS.to(empty);
        assertTrue(jts.isEmpty());
        assertEquals(empty, JTS.from(jts));
    }


    // Note that the XYM and XYZM cases are ignored, because the JTS WKTReader cannot parse the relevant EWKT forms.
    private void testInputs(CodecTestInputs testInputs) {
        for (Integer testCase : testInputs.getCases()) {
            String failureMsg = "Failure in testcase " + testCase;
            String wkt = testInputs.getWKT(testCase);
            Geometry geolatteGeom = wktDecoder.decode(wkt);
            com.vividsolutions.jts.geom.Geometry jtsGeom = null;
            jtsGeom = parseWKTtoJTS(wkt, jtsGeom);
            if (jtsGeom == null) {
                //some EWKT forms cannot be parse, so we skip these cases
                continue;
            }
            if (com.vividsolutions.jts.geom.GeometryCollection.class.isInstance(jtsGeom)) {
                // JTS's equals method cannot handle GeometryCollection classes, so we just test
                // if the types and number of items are equal
                assertEquals(failureMsg, JTS.getCorrespondingGeolatteClass(jtsGeom.getClass()), geolatteGeom.getClass());
                assertEquals(((com.vividsolutions.jts.geom.GeometryCollection) jtsGeom).getNumGeometries(),
                        ((GeometryCollection) geolatteGeom).getNumGeometries());
                continue;
            }
            if (jtsGeom.isEmpty()) {
                //JTS equals() fails when comparing empty geometries. So check if the o.g.g.Geometry is also empty
                // and of the same Geometry type.
                assertTrue(failureMsg, geolatteGeom.isEmpty());
                assertEquals(failureMsg, JTS.getCorrespondingGeolatteClass(jtsGeom.getClass()), geolatteGeom.getClass());
                continue;
            }
            assertEquals(failureMsg, geolatteGeom, JTS.from(jtsGeom));
            //note: this can't be changed to an assertEquals: the c.v.j.g.Geometry.equals(Geometry) method must be called
            // not Object.equals(Object)
            assertTrue(failureMsg, jtsGeom.equals(JTS.to(geolatteGeom)));
        }
    }

    private com.vividsolutions.jts.geom.Geometry parseWKTtoJTS(String wkt, com.vividsolutions.jts.geom.Geometry jtsGeom) {
        try {
            jtsGeom = jtsWktDecoder.read(wkt);
        } catch (ParseException e) {
            //ignore
        }
        return jtsGeom;
    }


}
