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
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PointTest {

    PointSequence seq2D = new PackedPointSequence(new double[]{1,2} , DimensionalFlag.d2D);
    PointSequence seq3D = new PackedPointSequence(new double[]{1,2, -3} , DimensionalFlag.d3D);
    PointSequence seq2DM = new PackedPointSequence(new double[]{1,2, 3} , DimensionalFlag.d2DM);
    PointSequence seq3DM = new PackedPointSequence(new double[]{1,2, 3, 4} , DimensionalFlag.d3DM);

    CrsId wgs84 = CrsId.valueOf(4326);
    Point point2D =  new Point(seq2D, wgs84);
    Point point3D =  new Point(seq3D, wgs84);
    Point point2DM = new Point(seq2DM, wgs84);
    Point point3DM = new Point(seq3DM, wgs84);
    Point emptyPoint = Points.createEmpty();

    @Test
    public void testGetX() throws Exception {
        assertEquals(1, point2D.getX(), Math.ulp(10d));
        assertEquals(1, point2DM.getX(), Math.ulp(10d));
        assertEquals(1, point3D.getX(), Math.ulp(10d));
        assertEquals(1, point3DM.getX(), Math.ulp(10d));
        assertTrue(Double.isNaN(emptyPoint.getX()));
    }

    @Test
    public void testGetY() throws Exception {
        assertEquals(2, point2D.getY(), Math.ulp(10d));
        assertEquals(2, point2DM.getY(), Math.ulp(10d));
        assertEquals(2, point3D.getY(), Math.ulp(10d));
        assertEquals(2, point3DM.getY(), Math.ulp(10d));
        assertTrue(Double.isNaN(emptyPoint.getY()));

    }

    @Test
    public void testGetZ() throws Exception {
        assertTrue(Double.isNaN(point2D.getZ()));
        assertTrue(Double.isNaN(point2DM.getZ()));
        assertEquals(-3, point3D.getZ(), Math.ulp(10d));
        assertEquals(3, point3DM.getZ(), Math.ulp(10d));
        assertTrue(Double.isNaN(emptyPoint.getZ()));

    }

    @Test
    public void testGetM() throws Exception {
        assertTrue(Double.isNaN(point2D.getM()));
        assertTrue(Double.isNaN(point3D.getM()));
        assertEquals(3, point2DM.getM(), Math.ulp(10d));
        assertEquals(4, point3DM.getM(), Math.ulp(10d));
        assertTrue(Double.isNaN(emptyPoint.getM()));

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
        assertEquals(4326, point2D.getSRID());
//        assertEquals(4326, emptyPoint3DM.getCrsId());
    }


    @Test
    public void testIsEmpty() throws Exception {
        assertFalse(point2D.isEmpty());
        assertTrue(emptyPoint.isEmpty());
    }

    @Test
    public void testIsSimple() throws Exception {
        assertTrue(point3D.isSimple());
        assertTrue(emptyPoint.isSimple());
    }

    @Test
    public void testIs3D() throws Exception {
        assertTrue(point3D.is3D());
        assertTrue(point3DM.is3D());
        assertFalse(point2D.is3D());
        assertFalse(point2DM.is3D());
        assertFalse(emptyPoint.is3D());
    }

    @Test
    public void testIsMeasured() throws Exception {
        assertTrue(point3DM.isMeasured());
        assertTrue(point2DM.isMeasured());
        assertFalse(emptyPoint.isMeasured());
        assertFalse(point2D.isMeasured());
        assertFalse(point3D.isMeasured());
    }

    @Test
    public void testBoundaryIsUnsupported() throws Exception {
        try{
            point2D.getBoundary();
        }catch(UnsupportedOperationException e){}
    }


    @Test
    public void testEmptyPointsAreAlwaysEqual(){
        Point empty = Points.createEmpty();
        assertTrue(empty.equals(emptyPoint));
    }

    @Test
    public void testEqualsAndHashCode(){
        Point test2D = Points.create(new double[]{1, 2}, DimensionalFlag.d2D, wgs84);
        assertTrue(point2D.equals(test2D));
        assertEquals(point2D.hashCode() , test2D.hashCode());
        Point test3D = Points.create(new double[]{1, 2, -3}, DimensionalFlag.d3D, wgs84);
        assertTrue(point3D.equals(test3D));
        assertEquals(point3D.hashCode() , test3D.hashCode());
        Point test2DM = Points.create(new double[]{1, 2, 3}, DimensionalFlag.d2DM, wgs84);
        assertTrue(point2DM.equals(test2DM));
        assertEquals(point2DM.hashCode() , test2DM.hashCode());
        Point test3DM = Points.create(new double[]{1, 2, 3, 4}, DimensionalFlag.d3DM, wgs84);
        assertTrue(point3DM.equals(test3DM));
        assertEquals(point3DM.hashCode() , test3DM.hashCode());
        assertFalse(point2D.equals(Points.create(new double[]{1, 2}, DimensionalFlag.d2D, CrsId.UNDEFINED)));
        assertFalse(point2D.equals(Points.create(new double[]{1, 2, 3}, DimensionalFlag.d3D, wgs84)));
        assertFalse(point2D.equals(Points.create(new double[]{1, 2, 3}, DimensionalFlag.d2DM, wgs84)));
    }

    @Test
    public void testPointEquality() {
        GeometryPointEquality eq2D = new GeometryPointEquality(new ExactCoordinatePointEquality(DimensionalFlag.d2D));
        assertTrue(point3DM.equals(Points.create(1, 2, 3, 4, wgs84)));
        assertTrue(eq2D.equals(point3DM, Points.create(1, 2, wgs84)));
        assertFalse(eq2D.equals(point3DM, Points.create(2, 1, wgs84)));
        assertFalse(eq2D.equals(point3DM, Points.create(1, 2, CrsId.UNDEFINED)));
        assertFalse(point2D.equals(Points.createEmpty()));
    }


    @Test
    public void testEqualsAndHashCodeOnEmptyPoints(){
        Point empty1 = Points.createEmpty();
        Point empty2 = Points.createEmpty();
        assertEquals(empty1, empty2);
        Point empty3 = new Point(EmptyPointSequence.INSTANCE,CrsId.UNDEFINED, null);
        assertEquals(empty1, empty3);
    }
}


