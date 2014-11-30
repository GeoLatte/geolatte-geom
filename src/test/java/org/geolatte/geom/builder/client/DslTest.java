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
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.builder.client;

import org.apache.commons.collections.CollectionUtils;
import org.geolatte.geom.*;
import org.junit.Test;

import java.util.ArrayList;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasMeasureAxis;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasVerticalAxis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.geolatte.geom.CrsMock.*;
/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class DslTest {

    static private double DELTA = 0.00000001;


    @Test
    public void testLineString2D() {

        LineString<G2D> ls = linestring(WGS84, g(0, 0), g(1, 0), g(2, 0));

        assertEquals(2, ls.getCoordinateDimension());
        assertEquals(WGS84, ls.getCoordinateReferenceSystem());

        ArrayList<G2D> points = new ArrayList<G2D>();
        CollectionUtils.addAll(points, ls.getPositions().iterator());
        assertEquals(points.get(0).getLon(), 0, DELTA);
        assertEquals(points.get(0).getLat(), 0, DELTA);
        assertEquals(points.get(1).getLon(), 1, DELTA);
        assertEquals(points.get(1).getLat(), 0, DELTA);
        assertEquals(points.get(2).getLon(), 2, DELTA);
        assertEquals(points.get(2).getLat(), 0, DELTA);
    }

    @Test
    public void testEmptyLineString2D() {
        LineString ls = linestring(WGS84);
        assertTrue(ls.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLineString() {
        LineString ls = linestring(WGS84, g(0, 0));
    }

    @Test
    public void testLinearRing3D() {

        LinearRing lr = ring(WGS84_Z, g(0, 0, 0), g(1, 0, 0), g(1, 1, 0), g(0, 1, 0), g(0, 0, 0));
        assertTrue(hasVerticalAxis(lr.getCoordinateReferenceSystem()));

        ArrayList<G3D> points = new ArrayList<G3D>();
        CollectionUtils.addAll(points, lr.getPositions().iterator());
        assertEquals(points.get(0).getLon(), 0, DELTA);
        assertEquals(points.get(0).getLat(), 0, DELTA);
        assertEquals(points.get(0).getHeight(), 0, DELTA);
        assertEquals(points.get(1).getLon(), 1, DELTA);
        assertEquals(points.get(1).getLat(), 0, DELTA);
        assertEquals(points.get(1).getHeight(), 0, DELTA);
        assertEquals(points.get(2).getLon(), 1, DELTA);
        assertEquals(points.get(2).getLat(), 1, DELTA);
        assertEquals(points.get(2).getHeight(), 0, DELTA);
        assertEquals(points.get(3).getLon(), 0, DELTA);
        assertEquals(points.get(3).getLat(), 1, DELTA);
        assertEquals(points.get(3).getHeight(), 0, DELTA);
        assertEquals(points.get(4).getLon(), 0, DELTA);
        assertEquals(points.get(4).getLat(), 0, DELTA);
        assertEquals(points.get(4).getHeight(), 0, DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLinearRing3D() {
        LinearRing lr = ring(WGS84_Z, g(0, 0, 0), g(1, 0, 0), g(1, 1, 0), g(0, 1, 0));
    }

    @Test
    public void testValidPolygon() {
        Polygon p = polygon(ring(WGS84, g(0, 0), g(0, 1), g(1, 1), g(1, 0), g(0, 0)));
        assertEquals(p.getSRID(), 4326);
        assertEquals(p.getNumPositions(), 5);

        double[] coords = new double[2];
        p.getPositions().getCoordinates(0, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);

        p.getPositions().getCoordinates(1, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(1d, coords[1], DELTA);

        p.getPositions().getCoordinates(2, coords);
        assertEquals(1d, coords[0], DELTA);
        assertEquals(1d, coords[1], DELTA);

        p.getPositions().getCoordinates(3, coords);
        assertEquals(1d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);

        p.getPositions().getCoordinates(4, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);
    }

    @Test
    public void testValidPolygonAlternativeSyntax() {
        Polygon p = polygon(ring(WGS84, g(0, 0), g(0, 1), g(1, 1), g(1, 0), g(0, 0)));
        Polygon p2 = polygon(WGS84, ring(g(0, 0), g(0, 1), g(1, 1), g(1, 0), g(0, 0)));
        assertEquals(p, p2);
    }


    @Test
    public void testValidPolygon2DM() {
        Polygon p = polygon(ring(WGS84_M, gM(0, 0, 2), gM(0, 1, 3), gM(1, 1, 4), gM(1, 0, 3), gM(0, 0, 2)));
        assertEquals(0, p.getNumInteriorRing());
        assertTrue(hasMeasureAxis(p.getCoordinateReferenceSystem()));
        assertEquals(4326, p.getSRID());

        double[] coords = new double[3];
        p.getPositions().getCoordinates(0, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);
        assertEquals(2d, coords[2], DELTA);


        p.getPositions().getCoordinates(1, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(1d, coords[1], DELTA);
        assertEquals(3d, coords[2], DELTA);

        p.getPositions().getCoordinates(2, coords);
        assertEquals(1d, coords[0], DELTA);
        assertEquals(1d, coords[1], DELTA);
        assertEquals(4d, coords[2], DELTA);

        p.getPositions().getCoordinates(3, coords);
        assertEquals(1d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);
        assertEquals(3d, coords[2], DELTA);

        p.getPositions().getCoordinates(4, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);
        assertEquals(2d, coords[2], DELTA);
    }

    @Test
    public void testvalidPolygonWithInteriorRing() {
        Polygon p = polygon(ring(WGS84, g(0, 0), g(0, 10), g(10, 10), g(10, 0), g(0, 0)), ring(WGS84, g(3, 3), g(3, 6), g(6, 6), g(6, 3), g(3, 3)));
        assertEquals(p.getSRID(), 4326);
        assertEquals(p.getNumPositions(), 10);
        assertEquals(p.getNumInteriorRing(), 1);

        double[] coords = new double[2];
        p.getExteriorRing().getPositions().getCoordinates(0, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);

        p.getExteriorRing().getPositions().getCoordinates(1, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(10d, coords[1], DELTA);

        p.getExteriorRing().getPositions().getCoordinates(2, coords);
        assertEquals(10d, coords[0], DELTA);
        assertEquals(10d, coords[1], DELTA);

        p.getExteriorRing().getPositions().getCoordinates(3, coords);
        assertEquals(10d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);

        p.getExteriorRing().getPositions().getCoordinates(4, coords);
        assertEquals(0d, coords[0], DELTA);
        assertEquals(0d, coords[1], DELTA);

        p.getInteriorRingN(0).getPositions().getCoordinates(0, coords);
        assertEquals(3d, coords[0], DELTA);
        assertEquals(3d, coords[1], DELTA);

        p.getInteriorRingN(0).getPositions().getCoordinates(1, coords);
        assertEquals(3d, coords[0], DELTA);
        assertEquals(6d, coords[1], DELTA);

        p.getInteriorRingN(0).getPositions().getCoordinates(2, coords);
        assertEquals(6d, coords[0], DELTA);
        assertEquals(6d, coords[1], DELTA);

        p.getInteriorRingN(0).getPositions().getCoordinates(3, coords);
        assertEquals(6d, coords[0], DELTA);
        assertEquals(3d, coords[1], DELTA);

        p.getInteriorRingN(0).getPositions().getCoordinates(4, coords);
        assertEquals(3d, coords[0], DELTA);
        assertEquals(3d, coords[1], DELTA);

    }

    public void testvalidPolygonWithInteriorRingAlternativeSyntax() {
        Polygon p = polygon(ring(WGS84, g(0, 0), g(0, 10), g(10, 10), g(10, 0), g(0, 0)), ring(WGS84, g(3, 3), g(3, 6), g(6, 6), g(6, 3), g(3, 3)));
        Polygon p2 = polygon(WGS84, ring(g(0, 0), g(0, 10), g(10, 10), g(10, 0), g(0, 0)), ring(g(3, 3), g(3, 6), g(6, 6), g(6, 3), g(3, 3)));
        assertEquals(p, p2);
    }

    @Test
    public void testvalidPoint2D() {
        Point pnt = point(WGS84, g(1, 2));
        assertEquals(pnt, new Point<G2D>(new G2D(1, 2), WGS84));
    }

    @Test
    public void testvalidPoint3D() {
        Point pnt = point(WGS84_Z, g(1, 2, 3));
        assertEquals(pnt, new Point<G3D>(new G3D(1, 2, 3), WGS84_Z));
    }

    @Test
    public void testvalidPoint2DM() {
        Point pnt = point(WGS84_M, gM(1, 2, 3));
        assertEquals(pnt, new Point<G2DM>(new G2DM(1, 2, 3), WGS84_M));
    }

    @Test
    public void testvalidPoint3DM() {
        Point pnt = point(WGS84_ZM, g(1, 2, 3, 4));
        assertEquals(pnt, new Point<G3DM>(new G3DM(1, 2, 3, 4), WGS84_ZM));
    }

    @Test
    public void testValidGeometryCollection() {
        GeometryCollection gc = geometrycollection(
                point(WGS84, g(1, 2)),
                linestring(WGS84, g(0, 0), g(1, 1), g(2, 1)),
                polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0)))
        );
        assertEquals(3, gc.getNumGeometries());
        assertEquals(2, gc.getCoordinateDimension());
    }

    @Test
    public void testValidGeometryCollectionAlternativeSyntax() {
        GeometryCollection gc = geometrycollection(
                point(WGS84, g(1, 2)),
                linestring(WGS84, g(0, 0), g(1, 1), g(2, 1)),
                polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0)))
        );
        GeometryCollection gc2 = geometrycollection(WGS84,
                point(g(1, 2)),
                linestring(g(0, 0), g(1, 1), g(2, 1)),
                polygon(ring(g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0)))
        );
        assertEquals(gc, gc2);

    }

//    @Test
//    public void testValidGeometryCollectionOfGeometryCollections() {
//        GeometryCollection gc = geometrycollection(4326,
//                point(c(1, 2)),
//                linestring(c(0, 0), c(1, 1), c(2, 1)),
//                polygon(ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0))),
//                geometrycollection(point(c(1, 1)),
//                        linestring(c(1, 2), c(2, 3)))
//        );
//        assertEquals(4, gc.getNumGeometries());
//        assertEquals(GeometryType.GEOMETRYCOLLECTION, gc.getGeometryN(3).getGeometryType());
//    }

    @Test
    public void testValidMultiPoint() {
        MultiPoint mp = multipoint(point(WGS84_ZM, g(2, 1, 3, 4)), point(WGS84_ZM, g(3, 1, 5, 6)));
        assertEquals(GeometryType.MULTIPOINT, mp.getGeometryType());
        assertEquals(2, mp.getNumGeometries());
        assertEquals(4326, mp.getSRID());
        assertEquals(4, mp.getCoordinateDimension());
    }

    @Test
    public void testValidMultiPointAlternativeSyntax() {
        MultiPoint mp = multipoint(point(WGS84_ZM, g(2, 1, 3, 4)), point(WGS84_ZM, g(3, 1, 5, 6)));
        MultiPoint mp2 = multipoint(WGS84_ZM, point(g(2, 1, 3, 4)), point(g(3, 1, 5, 6)));
        assertEquals(mp, mp2);
    }

    @Test
    public void testMultiPointCanBeEmbeddedInGeometryCollection() {
        GeometryCollection geometryCollection = geometrycollection(
                multipoint(point(WGS84, g(1, 2))),
                point(WGS84, g(3, 4))
        );
        assertEquals(GeometryType.MULTIPOINT, geometryCollection.getGeometryN(0).getGeometryType());
    }

    @Test
    public void testValidMultiLineString() {
        MultiLineString mls = multilinestring(
                linestring(WGS84_Z, g(1, 2, 5), g(3, 4, 2)),
                linestring(WGS84_Z, g(4, 5, 1), g(6, 5, 0)));
        assertEquals(GeometryType.MULTILINESTRING, mls.getGeometryType());
        assertEquals(2, mls.getNumGeometries());
        assertEquals(4326, mls.getSRID());
        assertEquals(3, mls.getCoordinateDimension());
    }

    @Test
    public void testValidMultiLineStringAlternativeSyntax() {
        MultiLineString mls = multilinestring(
                linestring(WGS84_Z, g(1, 2, 5), g(3, 4, 2)),
                linestring(WGS84_Z, g(4, 5, 1), g(6, 5, 0)));
        MultiLineString mls2 = multilinestring(WGS84_Z,
                linestring(g(1, 2, 5), g(3, 4, 2)),
                linestring(g(4, 5, 1), g(6, 5, 0)));
        assertEquals(mls, mls2);
    }


    @Test
    public void testMultiLineStringCanBeEmbeddedInGeometryCollection() {
        GeometryCollection geometryCollection = geometrycollection(
                multilinestring(
                        linestring(WGS84_Z, g(1, 2, 5), g(3, 4, 2)),
                        linestring(WGS84_Z, g(4, 5, 1), g(6, 5, 0))
                ),
                linestring(WGS84_Z, g(4, 5, 1), g(6, 5, 0))
        );
        assertEquals(GeometryType.MULTILINESTRING, geometryCollection.getGeometryN(0).getGeometryType());
    }

    @Test
    public void testValidMultiPolygonString() {
        MultiPolygon mp = multipolygon(
                polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0))),
                polygon(ring(WGS84, g(0, 0), g(10, 0), g(10, 10), g(0, 10), g(0, 0)))
        );
        assertEquals(GeometryType.MULTIPOLYGON, mp.getGeometryType());
        assertEquals(2, mp.getNumGeometries());
        assertEquals(4326, mp.getSRID());
        assertEquals(2, mp.getCoordinateDimension());
    }

    @Test
    public void testValidMultiPolygonStringAlternativeSyntax() {
        MultiPolygon mp = multipolygon(
                polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0))),
                polygon(ring(WGS84, g(0, 0), g(10, 0), g(10, 10), g(0, 10), g(0, 0)))
        );

        MultiPolygon mp2 = multipolygon(WGS84,
                polygon(ring(g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0))),
                polygon(ring(g(0, 0), g(10, 0), g(10, 10), g(0, 10), g(0, 0)))
        );
        assertEquals(mp, mp2);
    }

    @Test
    public void testMultiPolygonCanBeEmbeddedInGeometryCollection() {
        GeometryCollection geometryCollection =
                geometrycollection(
                        multipolygon(
                                polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0))),
                                polygon(ring(WGS84, g(0, 0), g(10, 0), g(10, 10), g(0, 10), g(0, 0)))
                        ),
                        polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0)))
                );
        assertEquals(GeometryType.MULTIPOLYGON, geometryCollection.getGeometryN(0).getGeometryType());
    }

    @Test
        public void testMultiPolygonCanBeEmbeddedInGeometryCollectionAlternative() {
            GeometryCollection g1=
                    geometrycollection(
                            multipolygon(
                                    polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0))),
                                    polygon(ring(WGS84, g(0, 0), g(10, 0), g(10, 10), g(0, 10), g(0, 0)))
                            ),
                            polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0)))
                    );

        GeometryCollection g2=
                            geometrycollection(WGS84,
                                    multipolygon(
                                            polygon(ring(g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0))),
                                            polygon(ring(g(0, 0), g(10, 0), g(10, 10), g(0, 10), g(0, 0)))
                                    ),
                                    polygon(ring(g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0)))
                            );
        assertEquals(g1, g2);
        }


}
