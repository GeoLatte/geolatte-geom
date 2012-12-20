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

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class DslTest {

    @Test
    public void testLineString2D() {
        LineString ls = lineString(4326, c(0, 0), c(1, 0), c(2, 0));
    }

    @Test
    public void testEmptyLineString2D() {
        LineString ls = lineString(4326, empty());
        assertTrue(ls.isEmpty());
        assertEquals(ls.getDimensionalFlag(), DimensionalFlag.d2D);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLineString() {
        LineString ls = lineString(4326, c(0, 0));
    }

    @Test
    public void testLinearRing3D(){
        LinearRing lr = ring(4326, cZ(0, 0, 0), cZ(1, 0, 0), cZ(1, 1, 0), cZ(0, 1, 0), cZ(0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLinearRing3D(){
            LinearRing lr = ring(4326, cZ(0,0,0), cZ(1,0,0), cZ(1,1,0), cZ(0,1,0));
    }

    @Test
    public void testvalidPolygon() {
        Polygon p = polygon(4326, ring(c(0, 0), c(0, 1), c(1, 1), c(1, 0), c(0, 0)));
        assertEquals(p.getSRID(), 4326);
        assertEquals(p.getNumPoints(), 5);
    }

    @Test
    public void testvalidPolygonWithInteriorRing() {
        Polygon p = polygon(4326, ring(c(0, 0), c(0, 10), c(10, 10), c(10, 0), c(0, 0)), ring(c(3, 3), c(3, 6), c(6, 6), c(6, 3), c(3, 3)));
        assertEquals(p.getSRID(), 4326);
        assertEquals(p.getNumPoints(), 10);
        assertEquals(p.getNumInteriorRing(), 1);
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
        GeometryCollection gc = geometryCollection(4326,
                point(c(1, 2)), lineString(c(0, 0), c(1, 1), c(2, 1))
//                polygon(ring(c(0,0), c(1,0), c(1,1), c(0,1), c(0,0)))
        );
    }



}
