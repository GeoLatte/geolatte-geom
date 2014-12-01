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

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.junit.Assert;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;
/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PointTest {

    private static CoordinateReferenceSystem<C2D> l72 = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

//    PositionSequence seq2D = new PackedPositionSequence(new double[]{1,2} , DimensionalFlag.d2D, wgs84);
//    PositionSequence seq3D = new PackedPositionSequence(new double[]{1,2, -3} , DimensionalFlag.d3D, wgs84);
//    PositionSequence seq2DM = new PackedPositionSequence(new double[]{1,2, 3} , DimensionalFlag.d2DM, wgs84);
//    PositionSequence seq3DM = new PackedPositionSequence(new double[]{1,2, 3, 4} , DimensionalFlag.d3DM, wgs84);

    Point<C2D> point2D = point(crs, c(1, 2));
    Point<C3D> point3D =  point(crsZ, c(1, 2, -3));
    Point<C2DM> point2DM = point(crsM, cM(1, 2, 3));
    Point<C3DM> point3DM = point(crsZM, c(1, 2, 3, 4));
    Point<C2D> emptyPoint = new Point<C2D>(crs);

    @Test
    public void testGetX() throws Exception {
        assertEquals(1, point2D.getPosition().getX(), Math.ulp(10d));
        assertEquals(1, point2DM.getPosition().getX(), Math.ulp(10d));
        assertEquals(1, point3D.getPosition().getX(), Math.ulp(10d));
        assertEquals(1, point3DM.getPosition().getX(), Math.ulp(10d));
    }

    @Test
    public void testGetY() throws Exception {
        assertEquals(2, point2D.getPosition().getY(), Math.ulp(10d));
        assertEquals(2, point2DM.getPosition().getY(), Math.ulp(10d));
        assertEquals(2, point3D.getPosition().getY(), Math.ulp(10d));
        assertEquals(2, point3DM.getPosition().getY(), Math.ulp(10d));
    }

    @Test
    public void testGetZ() throws Exception {
        assertEquals(-3, point3D.getPosition().getZ(), Math.ulp(10d));
        assertEquals(3, point3DM.getPosition().getZ(), Math.ulp(10d));
    }

    @Test
    public void testGetM() throws Exception {
        assertEquals(3, point2DM.getPosition().getM(), Math.ulp(10d));
        assertEquals(4, point3DM.getPosition().getM(), Math.ulp(10d));
    }

    @Test
    public void testDimension() throws Exception {
        assertEquals(0, point2D.getDimension());
    }

    @Test
    public void testGeometryType() throws Exception {
        Assert.assertEquals(GeometryType.POINT, point3D.getGeometryType());
    }

    @Test
    public void testCoordinateDimension() throws Exception {
        assertEquals(3, point3D.getCoordinateDimension());
        assertEquals(3, point2DM.getCoordinateDimension());
        assertEquals(2, point2D.getCoordinateDimension());
        assertEquals(4, point3DM.getCoordinateDimension());
        assertEquals(2, emptyPoint.getCoordinateDimension());
    }

    @Test
    public void testSrid() throws Exception {
        assertEquals(-1, point2D.getSRID());
//        assertEquals(4326, emptyPoint3DM.getCoordinateReferenceSystem());
    }


    @Test
    public void testIsEmpty() throws Exception {
        assertFalse(point2D.isEmpty());
        assertTrue(emptyPoint.isEmpty());
    }


    @Test
    public void testEmptyPointsAreAlwaysEqual(){
        Point<C2D> empty = new Point<C2D>(crs);
        assertTrue(empty.equals(emptyPoint));
    }

    @Test
    public void testEqualsAndHashCode(){
        Point<C2D> test2D = point(crs, c(1, 2));
        assertTrue(point2D.equals(test2D));
        assertEquals(point2D.hashCode() , test2D.hashCode());
        Point<C3D> test3D = point(crsZ, c(1, 2, -3));
        assertTrue(point3D.equals(test3D));
        assertEquals(point3D.hashCode() , test3D.hashCode());
        Point<C2DM> test2DM = point(crsM, cM(1, 2, 3));

        assertTrue(point2DM.equals(test2DM));
        assertEquals(point2DM.hashCode() , test2DM.hashCode());
        Point<C3DM> test3DM = point(crsZM, c(1, 2, 3, 4));
        assertTrue(point3DM.equals(test3DM));
        assertEquals(point3DM.hashCode() , test3DM.hashCode());
        assertFalse(point2D.equals( point(l72, c(1, 2))));
        assertFalse(point2D.equals(point(crsZ, c(1, 2, 3))));
        assertFalse(point2D.equals(point(crsM, cM(1, 2, 3))));
    }

    @Test
    public void testPointEquality() {
        GeometryPointEquality eq2D = new GeometryPointEquality(new ExactPositionEquality());
        assertTrue(point3DM.equals(point(crsZM, c(1, 2, 3, 4))));
        assertTrue(eq2D.equals(point2D, point(crs, c(1, 2))));
        assertFalse(point2D.equals(emptyPoint));
    }


    @Test
    public void testEqualsAndHashCodeOnEmptyPoints(){
        Point<C2D> empty1 = new Point<C2D>(crs);
        Point<C2D> empty2 = new Point<C2D>(crs);
        assertEquals(empty1, empty2);
        Point<C2D> empty3 = new Point<C2D>(crs);
        assertEquals(empty1, empty3);
    }
}


