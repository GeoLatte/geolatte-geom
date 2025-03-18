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

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.Measured;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.C3DM;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.WktDecodeException;
import org.geolatte.geom.codec.WktDecoder;
import org.geolatte.geom.codec.testcases.PostgisJDBCUnitTestInputs;
import org.geolatte.geom.codec.testcases.PostgisJDBCWithSRIDTestInputs;
import org.geolatte.geom.codec.testcases.PostgisTestCases;
import org.geolatte.geom.codec.testcases.WktWkbCodecTestBase;
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
        Assert.assertTrue(CoordinateXYZM.class.isInstance(jtsGeometry.getCoordinates()[0]));
        CoordinateXYZM dc = (CoordinateXYZM) jtsGeometry.getCoordinates()[0];
        assertEquals(dc.getM(), 2, Math.ulp(2));

        Geometry<?> geom2 = JTS.from(jtsGeometry);
        checkCoordinateDimension(geom2, 4, true);
    }

    @Test
    public void test_measured_2d() {
        PostgisTestCases testCases = new PostgisTestCases();
        Geometry<?> geometry = testCases.getExpected(PostgisTestCases.LINESTRING_2DM);
        org.locationtech.jts.geom.Geometry jtsGeometry = JTS.to(geometry);
        Assert.assertTrue(CoordinateXYM.class.isInstance(jtsGeometry.getCoordinates()[0]));
        CoordinateXYM dc = (CoordinateXYM) jtsGeometry.getCoordinates()[0];
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

    @Test
    public void test_transform() throws FactoryException, TransformException {
        String wktStringC3dmLambert72 = "SRID=31370;MULTILINESTRING((161020.812 191200.732 0 22.931,161020.135 191200.408 0 23.682,160997.118 191190.48 0 48.748,160966.688 191179.292 0 81.17,160936.497 191168.623 0 113.191,160932.78 191167.31 0 117.133,160929.266 191166.126 0 120.841),(160890.884 191144.82 0 0,160883.296 191148.378 0 8.381,160879 191150.391 0 13.125,160868.328 191155.579 0 24.991,160837.437 191170.688 0 59.379,160818.608 191179.772 0 80.285,160804.953 191186.36 0 95.446),(160804.953 191186.36 0 0,160790.964 191193.112 0 15.533,160785.625 191195.688 0 21.461))";
        int lambert2008Srid = 3812;
        String lambert2008Crs = "EPSG:" + lambert2008Srid;
        @SuppressWarnings("unchecked")
        MultiLineString<C3DM> geometryC3dmLambert = (MultiLineString<C3DM>) Wkt.fromWkt(wktStringC3dmLambert72);
        org.locationtech.jts.geom.Geometry jtsGeometry = org.geolatte.geom.jts.JTS.to(geometryC3dmLambert);
        CoordinateReferenceSystem sourceCrs =
                org.geotools.referencing.CRS.decode(
                        geometryC3dmLambert.getCoordinateReferenceSystem().getCrsId().toString()
                );
        CoordinateReferenceSystem targetCrs = org.geotools.referencing.CRS.decode(lambert2008Crs);
        MathTransform mathTransform = org.geotools.referencing.CRS.findMathTransform(sourceCrs, targetCrs);
        org.locationtech.jts.geom.Geometry transformedGeometryJts =
                org.geotools.geometry.jts.JTS.transform(jtsGeometry, mathTransform);
        transformedGeometryJts.setSRID(lambert2008Srid);
        assertEquals("SRID must match Lambert2008", lambert2008Srid, transformedGeometryJts.getSRID());
        @SuppressWarnings("unchecked")
        MultiLineString<C3DM> transformedGeometryGeolatte =
                (MultiLineString<C3DM>) org.geolatte.geom.jts.JTS.from(transformedGeometryJts);
        assertEquals("SRID must match Lambert2008", lambert2008Srid, transformedGeometryGeolatte.getSRID());
        for (int i = 0; i < geometryC3dmLambert.getNumGeometries(); i++) {
            org.geolatte.geom.LineString<C3DM> originalLineString = geometryC3dmLambert.getGeometryN(i);
            org.geolatte.geom.LineString<C3DM> transformedLineString = transformedGeometryGeolatte.getGeometryN(i);
            assertEquals("SRID must match Lambert2008", lambert2008Srid, transformedLineString.getSRID());
            // Check the number of points.
            assertEquals("the number of points in LineString " + i + " must match",
                    originalLineString.getPositions().size(),
                    transformedLineString.getPositions().size());

            for (int j = 0; j < originalLineString.getPositions().size(); j++) {
                C3DM originalPosition = originalLineString.getPositions().getPositionN(j);
                C3DM transformedPosition = transformedLineString.getPositions().getPositionN(j);
                // X en Y coördinates must be larger than 500000 in the Lambert2008 CRS
                assertTrue("X coördinate at point " + j + " in LineString " + i + " must be > 500000",
                        transformedPosition.getX() > 500000);
                assertTrue("Y coördinate at point " + j + " in LineString " + i + " must be > 500000",
                        transformedPosition.getY() > 500000);
                // the Z-coördinate must remain the same
                assertEquals("Z coördinate at point " + j + " in LineString " + i + " must be the same",
                        originalPosition.getZ(),
                        transformedPosition.getZ(),
                        0.0001);
                // the M-coördinate must remain the same
                assertEquals("M coördinate at point " + j + " in LineString " + i + " must be the same",
                        originalPosition.getM(),
                        transformedPosition.getM(),
                        0.0001);
            }
        }
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
                        ((AbstractGeometryCollection) geolatteGeom).getNumGeometries());
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
