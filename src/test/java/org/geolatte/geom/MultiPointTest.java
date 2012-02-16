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

package org.geolatte.geom;

import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.jts.JTS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/8/11
 */
public class MultiPointTest {

    MultiPoint pnt1;
    MultiPoint pnt2;
    CrsId wgs84 = CrsId.valueOf(4326);

    @Before
    public void setUp(){

        this.pnt1 = new MultiPoint(createPointsAllDifferent(5, wgs84));
        this.pnt2 = new MultiPoint(createPointsNotAllDifferent(5, wgs84));
    }

    @Test
    public void testAsJTS() throws Exception {
        com.vividsolutions.jts.geom.Geometry jtsMP = JTS.to(pnt1);
        assertEquals(jtsMP.getNumGeometries(), pnt1.getNumGeometries());
        for (int i = 0; i < pnt1.getNumGeometries(); i++) {
            assertEquals(jtsMP.getGeometryN(i).getCoordinate().x, pnt1.getGeometryN(i).getX(), Math.ulp(100));
            assertEquals(jtsMP.getGeometryN(i).getCoordinate().y, pnt1.getGeometryN(i).getY(), Math.ulp(100));
        }
    }

    @Test
    public void testGetDimension() throws Exception {
        assertEquals(0, pnt1.getDimension());

    }

    @Test
    public void testGetGeometryType() throws Exception {
        Assert.assertEquals(GeometryType.MULTI_POINT, pnt1.getGeometryType());

    }

    @Test
    public void testGetBoundary() throws Exception {
        assertEquals(MultiPoint.EMPTY, pnt1.getBoundary());
    }

    @Test
    public void testGetGeometryN() throws Exception {
        for (int i = 0; i < pnt1.getNumGeometries(); i++){
            Assert.assertEquals(Points.create(i, i, wgs84), pnt1.getGeometryN(i));
        }
    }

    @Test
    public void testIsSimple() throws Exception{
        assertTrue(pnt1.isSimple());
        assertFalse(pnt2.isSimple());
    }

    private Point[] createPointsNotAllDifferent(int size, CrsId crsId) {
        if (size < 4 ) throw new IllegalArgumentException("Size must be at least 4");
        Point[] points = new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = Points.create(i, i, crsId);
        }
        points[0] = points[size -1];
        return points;
    }

    private Point[] createPointsAllDifferent(int size, CrsId crsId) {
        Point[] points = new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = Points.create(i, i, crsId);
        }
        return points;
    }
}
