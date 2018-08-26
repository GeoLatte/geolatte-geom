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

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.Measured;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.WktDecodeException;
import org.geolatte.geom.codec.WktDecoder;
import org.geolatte.geom.support.PostgisJDBCUnitTestInputs;
import org.geolatte.geom.support.PostgisJDBCWithSRIDTestInputs;
import org.geolatte.geom.support.PostgisTestCases;
import org.geolatte.geom.support.WktWkbCodecTestBase;
import org.junit.Assert;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the JTS conversion
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/21/11
 */
public class TestJTS {


    WktDecoder wktDecoder = Wkt.newDecoder();
    WKTReader jtsWktDecoder = new WKTReader();


    @Test
    public void test_postgis_cases() {
        testInputs(new PostgisJDBCUnitTestInputs());
    }

    @Test
    public void test_postgis_cases_with_srid() {
        testInputs(new PostgisJDBCWithSRIDTestInputs());
    }

    @Test
    public void test_codec_cases() {
        try {
            testInputs(new PostgisTestCases());
        } catch (WktDecodeException e) {
            // ignore. The CodecTestInputs contain invalid WKT's to test exception handling. We only need the valid examples.
        }
    }

    @Test
    public void test_measured_3d() {
        PostgisTestCases testCases = new PostgisTestCases();
        Geometry<?> geometry = testCases.getExpected(PostgisTestCases.LINESTRING_3DM);
        org.locationtech.jts.geom.Geometry jtsGeometry = JTS.to(geometry);
        Assert.assertTrue(DimensionalCoordinate.class.isInstance(jtsGeometry.getCoordinates()[0]));
        DimensionalCoordinate dc = (DimensionalCoordinate) jtsGeometry.getCoordinates()[0];
        assertEquals(dc.getM(), 2, Math.ulp(2));

        Geometry<?> geom2 = JTS.from(jtsGeometry);
        checkCoordinateDimension(geom2, 4, true);
    }

    @Test
    public void test_measured_2d() {
        PostgisTestCases testCases = new PostgisTestCases();
        Geometry<?> geometry = testCases.getExpected(PostgisTestCases.LINESTRING_2DM);
        org.locationtech.jts.geom.Geometry jtsGeometry = JTS.to(geometry);
        Assert.assertTrue(DimensionalCoordinate.class.isInstance(jtsGeometry.getCoordinates()[0]));
        DimensionalCoordinate dc = (DimensionalCoordinate) jtsGeometry.getCoordinates()[0];
        assertEquals(dc.getM(), 2, Math.ulp(2));
        assertTrue(Double.isNaN(dc.getZ()));

        Geometry<?> geom2 = JTS.from(jtsGeometry);
        checkCoordinateDimension(geom2, 3, true);
    }

    @Test
    public void test_empty_polygon() {
        test_empty( polygon(crsZ));
    }

    @Test
    public void test_empty_point() {
        test_empty( new Point(crsZ) );
    }

    @Test
    public void test_empty_linestring() {
        test_empty(linestring(crsZ));
    }

    @Test
    public void test_empty_linearRing() {
        test_empty(ring(crsZ));
    }

    @Test
    public void test_empty_geometryCollection() {
        test_empty(geometrycollection(crsZ));
    }

    @Test
    public void test_empty_multipoint() {
        test_empty(multipoint(crsZ));
    }

    @Test
    public void test_empty_multilinestring() {
        test_empty(multilinestring(crsZ));
    }

    @Test
    public void test_empty_multipolygon() {
        test_empty(multipolygon(crsZ));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_arguments_to() {
        JTS.to((Geometry) null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_arguments_from() {
        JTS.from((org.locationtech.jts.geom.Geometry) null);

    }

    @Test
    public void test_from_with_srid() {
        org.locationtech.jts.geom.Geometry geometry = JTS.to(point(crs, c(1, 2)));
        geometry.setSRID(4326);
        assertEquals(4326, JTS.from(geometry).getSRID());

        geometry = JTS.to(polygon(ring(crs, c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0))));
        geometry.setSRID(4326);
        assertEquals(4326, JTS.from(geometry).getSRID());

    }

    @Test
    public void test_to_with_srid() {
        assertEquals(4326, JTS.to(point(WGS84, g(1.0, 2.0))).getSRID());
    }

    private void test_empty(Geometry empty) {
        org.locationtech.jts.geom.Geometry jts = JTS.to(empty);
        assertTrue(jts.isEmpty());
        assertEquals(empty, JTS.from(jts, empty.getCoordinateReferenceSystem()));
    }


    // We always need to use JTS.from(Geometry,CRS) because we use UNDEFINED 2D and 3D CRS's
    private void testInputs(WktWkbCodecTestBase testCases) {
        for (Integer testCase : testCases.getCases()) {
            String failureMsg = "Failure in testcase " + testCase;
            String wkt = testCases.getWKT(testCase);
            Geometry geolatteGeom = wktDecoder.decode(wkt);
            org.locationtech.jts.geom.Geometry jtsGeom = null;
            jtsGeom = parseWKTtoJTS(wkt, jtsGeom);
            if (jtsGeom == null) {
                //some EWKT forms cannot be parse, so in this case we just check that the To/From methods are consistent
                org.locationtech.jts.geom.Geometry jts = JTS.to(geolatteGeom);
                assertEquals(String.format("Error for case %d", testCase), geolatteGeom, JTS.from(jts, geolatteGeom.getCoordinateReferenceSystem()));
                continue;
            }
            if (org.locationtech.jts.geom.GeometryCollection.class.isInstance(jtsGeom)) {
                // JTS's equals method cannot handle GeometryCollection classes, so we just test
                // if the types and number of items are equal
                assertEquals(failureMsg, JTS.getCorrespondingGeolatteClass(jtsGeom.getClass()), geolatteGeom.getClass());
                assertEquals(((org.locationtech.jts.geom.GeometryCollection) jtsGeom).getNumGeometries(),
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
            assertEquals(failureMsg, geolatteGeom, JTS.from(jtsGeom, geolatteGeom.getCoordinateReferenceSystem()));
            //note: this can't be changed to an assertEquals: the c.v.j.g.Geometry.equals(Geometry) method must be called
            // not Object.equals(Object)
            assertTrue(failureMsg, jtsGeom.equals(JTS.to(geolatteGeom)));
//            assertTrue(failureMsg, (geolatteGeom.getCoordinateReferenceSystem() == CrsId.UNDEFINED && jtsGeom.getSRID() < 1)
//                    || (geolatteGeom.getSRID() == jtsGeom.getSRID()));
        }
    }

    private org.locationtech.jts.geom.Geometry parseWKTtoJTS(String wkt, org.locationtech.jts.geom.Geometry jtsGeom) {
        try {
            jtsGeom = jtsWktDecoder.read(wkt);
        } catch (ParseException e) {
            //ignore
        }
        return jtsGeom;
    }

    private void checkCoordinateDimension(Geometry<?> glGeom1, int expectedDimensions, boolean expectedMeasure) {
        Assert.assertEquals("wrong CoordinateDimension", expectedDimensions, glGeom1.getCoordinateDimension());

        Position pos = glGeom1.getPositionN(0);
        Assert.assertEquals("wrong CoordinateDimension", expectedDimensions, pos.getCoordinateDimension());

        boolean hasMeasure = (pos instanceof Measured);
        Assert.assertEquals("wrong hasMeasure", expectedMeasure, hasMeasure);

        PositionSequence<?> positionSequence = glGeom1.getPositions();
        for (Position position : positionSequence) {
            for (int i = 0; i < expectedDimensions; i++) {
                final double coordinateN = position.getCoordinate(i);
                final boolean isNaN = Double.isNaN(coordinateN);
                Assert.assertFalse("missing value for idx " + i, isNaN);
            }
        }
    }
}
