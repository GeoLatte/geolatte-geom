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
import org.geolatte.geom.crs.CrsId;
import org.junit.Test;

import java.util.*;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class DslTest {

    static private double DELTA = 0.00000001;

    @Test
    public void testLineString2D() {
        LineString ls = linestring(4326, c(0, 0), c(1, 0), c(2, 0));

        assertEquals(ls.getDimensionalFlag(), DimensionalFlag.d2D);
        assertEquals(ls.getCrsId(), CrsId.valueOf(4326));

        ArrayList<Point> points = new ArrayList<Point>();
        CollectionUtils.addAll(points, ls.getPoints().iterator());
        assertEquals(points.get(0).getX(), 0, DELTA);
        assertEquals(points.get(0).getY(), 0, DELTA);
        assertEquals(points.get(1).getX(), 1, DELTA);
        assertEquals(points.get(1).getY(), 0, DELTA);
        assertEquals(points.get(2).getX(), 2, DELTA);
        assertEquals(points.get(2).getY(), 0, DELTA);
    }

    @Test
    public void testEmptyLineString2D() {
        LineString ls = linestring(4326, empty());
        assertTrue(ls.isEmpty());
        assertEquals(ls.getDimensionalFlag(), DimensionalFlag.d2D);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLineString() {
        LineString ls = linestring(4326, c(0, 0));
    }

    @Test
    public void testLinearRing3D(){
        LinearRing lr = ring(4326, cZ(0, 0, 0), cZ(1, 0, 0), cZ(1, 1, 0), cZ(0, 1, 0), cZ(0, 0, 0));
        assertEquals(DimensionalFlag.d3D, lr.getDimensionalFlag());

        ArrayList<Point> points = new ArrayList<Point>();
        CollectionUtils.addAll(points, lr.getPoints().iterator());
        assertEquals(points.get(0).getX(), 0, DELTA);
        assertEquals(points.get(0).getY(), 0, DELTA);
        assertEquals(points.get(0).getZ(), 0, DELTA);
        assertEquals(points.get(1).getX(), 1, DELTA);
        assertEquals(points.get(1).getY(), 0, DELTA);
        assertEquals(points.get(1).getZ(), 0, DELTA);
        assertEquals(points.get(2).getX(), 1, DELTA);
        assertEquals(points.get(2).getY(), 1, DELTA);
        assertEquals(points.get(2).getZ(), 0, DELTA);
        assertEquals(points.get(3).getX(), 0, DELTA);
        assertEquals(points.get(3).getY(), 1, DELTA);
        assertEquals(points.get(3).getZ(), 0, DELTA);
        assertEquals(points.get(4).getX(), 0, DELTA);
        assertEquals(points.get(4).getY(), 0, DELTA);
        assertEquals(points.get(4).getZ(), 0, DELTA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLinearRing3D(){
        LinearRing lr = ring(4326, cZ(0,0,0), cZ(1,0,0), cZ(1,1,0), cZ(0,1,0));
    }

    @Test
    public void testValidPolygon() {
        Polygon p = polygon(4326, ring(c(0, 0), c(0, 1), c(1, 1), c(1, 0), c(0, 0)));
        assertEquals(p.getSRID(), 4326);
        assertEquals(p.getNumPoints(), 5);

        assertEquals(p.getPoints().getX(0), 0, DELTA);
        assertEquals(p.getPoints().getY(0), 0, DELTA);
        assertEquals(p.getPoints().getX(1), 0, DELTA);
        assertEquals(p.getPoints().getY(1), 1, DELTA);
        assertEquals(p.getPoints().getX(2), 1, DELTA);
        assertEquals(p.getPoints().getY(2), 1, DELTA);
        assertEquals(p.getPoints().getX(3), 1, DELTA);
        assertEquals(p.getPoints().getY(3), 0, DELTA);
        assertEquals(p.getPoints().getX(4), 0, DELTA);
        assertEquals(p.getPoints().getY(4), 0, DELTA);
    }

    @Test
    public void testValidPolygon2DM(){
        Polygon p = polygon(32100, ring(cM(0,0,2), cM(0,1,3), cM(1,1, 4), cM(1,0,3), cM(0, 0, 2)));
        assertEquals(0, p.getNumInteriorRing());
        assertEquals(DimensionalFlag.d2DM, p.getDimensionalFlag());
        assertEquals(32100, p.getSRID());

        assertEquals(p.getPoints().getX(0), 0, DELTA);
        assertEquals(p.getPoints().getY(0), 0, DELTA);
        assertEquals(p.getPoints().getM(0), 2, DELTA);
        assertEquals(p.getPoints().getX(1), 0, DELTA);
        assertEquals(p.getPoints().getY(1), 1, DELTA);
        assertEquals(p.getPoints().getM(1), 3, DELTA);
        assertEquals(p.getPoints().getX(2), 1, DELTA);
        assertEquals(p.getPoints().getY(2), 1, DELTA);
        assertEquals(p.getPoints().getM(2), 4, DELTA);
        assertEquals(p.getPoints().getX(3), 1, DELTA);
        assertEquals(p.getPoints().getY(3), 0, DELTA);
        assertEquals(p.getPoints().getM(3), 3, DELTA);
        assertEquals(p.getPoints().getX(4), 0, DELTA);
        assertEquals(p.getPoints().getY(4), 0, DELTA);
        assertEquals(p.getPoints().getM(4), 2, DELTA);
    }

    @Test
    public void testvalidPolygonWithInteriorRing() {
        Polygon p = polygon(4326, ring(c(0, 0), c(0, 10), c(10, 10), c(10, 0), c(0, 0)), ring(c(3, 3), c(3, 6), c(6, 6), c(6, 3), c(3, 3)));
        assertEquals(p.getSRID(), 4326);
        assertEquals(p.getNumPoints(), 10);
        assertEquals(p.getNumInteriorRing(), 1);

        assertEquals(p.getInteriorRingN(0).getPointN(0).getX(), 3, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(0).getY(), 3, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(1).getX(), 3, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(1).getY(), 6, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(2).getX(), 6, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(2).getY(), 6, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(3).getX(), 6, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(3).getY(), 3, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(0).getX(), 3, DELTA);
        assertEquals(p.getInteriorRingN(0).getPointN(0).getY(), 3, DELTA);

        assertEquals(p.getExteriorRing().getPointN(0).getX(), 0, DELTA);
        assertEquals(p.getExteriorRing().getPointN(0).getY(), 0, DELTA);
        assertEquals(p.getExteriorRing().getPointN(1).getX(), 0, DELTA);
        assertEquals(p.getExteriorRing().getPointN(1).getY(), 10, DELTA);
        assertEquals(p.getExteriorRing().getPointN(2).getX(), 10, DELTA);
        assertEquals(p.getExteriorRing().getPointN(2).getY(), 10, DELTA);
        assertEquals(p.getExteriorRing().getPointN(3).getX(), 10, DELTA);
        assertEquals(p.getExteriorRing().getPointN(3).getY(), 0, DELTA);
        assertEquals(p.getExteriorRing().getPointN(4).getX(), 0, DELTA);
        assertEquals(p.getExteriorRing().getPointN(4).getY(), 0, DELTA);
    }

    @Test
    public void testvalidPoint2D() {
        Point pnt = point(4326, c(1, 2));
        assertEquals(pnt, Points.create2D(1, 2, CrsId.valueOf(4326)));
    }

    @Test
    public void testvalidPoint3D() {
        Point pnt = point(4326, cZ(1, 2, 3));
        assertEquals(pnt, Points.create3D(1, 2, 3, CrsId.valueOf(4326)));
    }

    @Test
    public void testvalidPoint2DM() {
        Point pnt = point(4326, cM(1, 2, 3));
        assertEquals(pnt, Points.create2DM(1, 2, 3, CrsId.valueOf(4326)));
    }

    @Test
    public void testvalidPoint3DM() {
        Point pnt = point(4326, c(1, 2, 3, 4));
        assertEquals(pnt, Points.create3DM(1, 2, 3, 4, CrsId.valueOf(4326)));
    }

    @Test
    public void testValidGeometryCollection(){
        GeometryCollection gc = geometrycollection(4326,
                point(c(1, 2)),
                linestring(c(0, 0), c(1, 1), c(2, 1)),
                polygon(ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0)))
        );
        assertEquals(3, gc.getNumGeometries());
        assertEquals(DimensionalFlag.d2D, gc.getDimensionalFlag());

        GeometryCollection gc3D = geometrycollection(31370,
                point(cZ(1, 2, 3)), linestring(cZ(1, 2, 3), cZ(2, 3, 4)));
        assertEquals(DimensionalFlag.d3D, gc3D.getDimensionalFlag());
    }

    @Test
    public void testValidGeometryCollectionOfGeometryCollections(){
        GeometryCollection gc = geometrycollection(4326,
                point(c(1, 2)),
                linestring(c(0, 0), c(1, 1), c(2, 1)),
                polygon(ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0))),
                geometrycollection(point(c(1, 1)),
                linestring(c(1, 2), c(2, 3)))
        );
        assertEquals(4,gc.getNumGeometries());
        assertEquals(GeometryType.GEOMETRY_COLLECTION, gc.getGeometryN(3).getGeometryType());
    }

    @Test
    public void testValidMultiPoint() {
        MultiPoint mp = multipoint(4326, point(c(2, 1, 3, 4)), point(c(3, 1, 5, 6)));
        assertEquals(GeometryType.MULTI_POINT, mp.getGeometryType());
        assertEquals(2,mp.getNumGeometries());
        assertEquals(4326, mp.getSRID());
        assertEquals(DimensionalFlag.d3DM, mp.getDimensionalFlag());
    }

    @Test
    public void testMultiPointCanBeEmbeddedInGeometryCollection() {
        GeometryCollection geometryCollection = geometrycollection(4326, multipoint(point(c(1, 2)), point(c(3, 4))));
        assertEquals(GeometryType.MULTI_POINT, geometryCollection.getGeometryN(0).getGeometryType());
    }

    @Test
    public void testValidMultiLineString() {
        MultiLineString mls = multilinestring(4326, linestring(cZ(1, 2, 5), cZ(3, 4, 2)), linestring(cZ(4, 5, 1), cZ(6, 5, 0)));
        assertEquals(GeometryType.MULTI_LINE_STRING, mls.getGeometryType());
        assertEquals(2,mls.getNumGeometries());
        assertEquals(4326, mls.getSRID());
        assertEquals(DimensionalFlag.d3D, mls.getDimensionalFlag());
    }

    @Test
    public void testMultiLineStringCanBeEmbeddedInGeometryCollection() {
        GeometryCollection geometryCollection = geometrycollection(4326, multilinestring(linestring(cZ(1, 2, 5), cZ(3, 4, 2)), linestring(cZ(4, 5, 1), cZ(6, 5, 0))));
        assertEquals(GeometryType.MULTI_LINE_STRING, geometryCollection.getGeometryN(0).getGeometryType());
    }

    @Test
    public void testValidMultiPolygonString() {
        MultiPolygon mp = multipolygon(4326, polygon(ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0))),
                polygon(ring(c(0, 0), c(10, 0), c(10, 10), c(0, 10), c(0, 0))));
        assertEquals(GeometryType.MULTI_POLYGON, mp.getGeometryType());
        assertEquals(2,mp.getNumGeometries());
        assertEquals(4326, mp.getSRID());
        assertEquals(DimensionalFlag.d2D, mp.getDimensionalFlag());
    }

    @Test
    public void testMultiPolygonCanBeEmbeddedInGeometryCollection() {
        GeometryCollection geometryCollection = geometrycollection(4326, multipolygon(polygon(ring(c(0, 0), c(1, 0), c(1, 1), c(0, 1), c(0, 0))),
                polygon(ring(c(0, 0), c(10, 0), c(10, 10), c(0, 10), c(0, 0)))));
        assertEquals(GeometryType.MULTI_POLYGON, geometryCollection.getGeometryN(0).getGeometryType());
    }

}
