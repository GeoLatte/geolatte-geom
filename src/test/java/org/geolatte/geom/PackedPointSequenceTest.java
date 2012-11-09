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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import org.geolatte.geom.crs.CrsId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PackedPointSequenceTest {

    PackedPointSequence testSeq2D;
    PackedPointSequence testSeq3D;
    PackedPointSequence testSeq2DM;
    PackedPointSequence testSeq3DM;
    PointSequence testEmpty;


    @Before
    public void setUp() {
        testSeq2D = new PackedPointSequence(new double[]{0, 0, 1, -1, 2, -2}, DimensionalFlag.d2D, CrsId.UNDEFINED);
        testSeq3D = new PackedPointSequence(new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}, DimensionalFlag.d3D, CrsId.UNDEFINED);
        testSeq2DM = new PackedPointSequence(new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}, DimensionalFlag.d2DM, CrsId.UNDEFINED);
        testSeq3DM = new PackedPointSequence(new double[]{0, 0, 0, 1, 1, -1, 1, 2, 2, -2, 2, 3}, DimensionalFlag.d3DM, CrsId.UNDEFINED);
        testEmpty = EmptyPointSequence.INSTANCE;
    }


    @Test
    public void testConstructorThrowsIllegalArgumentOnWrongNumberOfCoordinates() {
        try {
            new PackedPointSequence(new double[]{0, 0, 1, 1}, DimensionalFlag.d3D, CrsId.UNDEFINED);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new PackedPointSequence(new double[]{0, 0, 0, 1, 1, 1, 1}, DimensionalFlag.d3D, CrsId.UNDEFINED);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new PackedPointSequence(new double[]{0, 0, 0, 1, 1, 1, 1, 1}, DimensionalFlag.d3D, CrsId.UNDEFINED);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }


    @Test
    public void testIs3D() throws Exception {

        assertFalse(testSeq2D.is3D());
        assertTrue(testSeq3D.is3D());
        assertFalse(testSeq2DM.is3D());
        assertTrue(testSeq3DM.is3D());
        assertFalse(testEmpty.is3D());

    }

    @Test
    public void testIsMeasured() throws Exception {
        assertFalse(testSeq2D.isMeasured());
        assertFalse(testSeq3D.isMeasured());
        assertTrue(testSeq2DM.isMeasured());
        assertTrue(testSeq3DM.isMeasured());
        assertFalse(testEmpty.isMeasured());
    }

    @Test
    public void testGetDimension() throws Exception {
        assertEquals(2, testSeq2D.getDimension());
        assertEquals(3, testSeq3D.getDimension());
        assertEquals(3, testSeq2DM.getDimension());
        assertEquals(4, testSeq3DM.getDimension());
    }

    @Test
    public void testGetCoordinate() throws Exception {
        // test2D
        assertEquals(new Coordinate(0, 0), testSeq2D.getCoordinate(0));
        assertEquals(new Coordinate(1, -1), testSeq2D.getCoordinate(1));
        assertEquals(new Coordinate(2, -2), testSeq2D.getCoordinate(2));

        assertEquals(new Coordinate(0, 0, 0), testSeq3D.getCoordinate(0));
        assertEquals(new Coordinate(1, -1, 1), testSeq3D.getCoordinate(1));
        assertEquals(new Coordinate(2, -2, 2), testSeq3D.getCoordinate(2));

        assertEquals(new Coordinate(0, 0), testSeq2DM.getCoordinate(0));
        assertEquals(new Coordinate(1, -1), testSeq2DM.getCoordinate(1));
        assertEquals(new Coordinate(2, -2), testSeq2DM.getCoordinate(2));

        assertEquals(new Coordinate(0, 0, 0), testSeq3DM.getCoordinate(0));
        assertEquals(new Coordinate(1, -1, 1), testSeq3DM.getCoordinate(1));
        assertEquals(new Coordinate(2, -2, 2), testSeq3DM.getCoordinate(2));

        try {
            testSeq2D.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3D.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq2DM.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3DM.getCoordinate(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

    }

    @Test
    public void testGetCoordinates() throws Exception {
        // test2D
        double[] result;
        result = new double[2];
        testSeq2D.getCoordinates(result, 0);
        assertTrue(Arrays.equals(new double[]{0, 0}, result));
        testSeq2D.getCoordinates(result, 1);
        assertTrue(Arrays.equals(new double[]{1, -1}, result));
        testSeq2D.getCoordinates(result, 2);
        assertTrue(Arrays.equals(new double[]{2, -2}, result));

        result = new double[3];
        testSeq3D.getCoordinates(result, 0);
        assertTrue(Arrays.equals(new double[]{0, 0, 0}, result));
        testSeq3D.getCoordinates(result, 1);
        assertTrue(Arrays.equals(new double[]{1, -1, 1}, result));
        testSeq3D.getCoordinates(result, 2);
        assertTrue(Arrays.equals(new double[]{2, -2, 2}, result));


        testSeq2DM.getCoordinates(result, 0);
        assertTrue(Arrays.equals(new double[]{0, 0, 0}, result));
        testSeq2DM.getCoordinates(result, 1);
        assertTrue(Arrays.equals(new double[]{1, -1, 1}, result));
        testSeq2DM.getCoordinates(result, 2);
        assertTrue(Arrays.equals(new double[]{2, -2, 2}, result));

        result = new double[4];
        testSeq3DM.getCoordinates(result, 0);
        assertTrue(Arrays.equals(new double[]{0, 0, 0, 1}, result));
        testSeq3DM.getCoordinates(result, 1);
        assertTrue(Arrays.equals(new double[]{1, -1, 1, 2}, result));
        testSeq3DM.getCoordinates(result, 2);
        assertTrue(Arrays.equals(new double[]{2, -2, 2, 3}, result));

        try {
            testSeq2D.getCoordinates(result, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3D.getCoordinates(result, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq2DM.getCoordinates(result, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3DM.getCoordinates(result, 3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testEmpty.getCoordinates(result, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        result = new double[2];
        try {
            testSeq3D.getCoordinates(result, 1);
            fail();
        } catch (IllegalArgumentException e) {
        }

    }

    @Test
    public void testGetCoordinateCopy() throws Exception {
        //geom independance: changing returned coordinate does not change sequence.
        Coordinate co = testSeq2D.getCoordinateCopy(1);
        co.x = co.x + 1;
        assertFalse(co.equals(testSeq2D.getCoordinateCopy(1)));
    }


    @Test
    public void testSize() throws Exception {
        assertEquals(3, testSeq2D.size());
        assertEquals(3, testSeq3D.size());
        assertEquals(3, testSeq2DM.size());
        assertEquals(3, testSeq3DM.size());
        assertEquals(0, testEmpty.size());
    }


    @Test
    public void testGetX() {
        assertEquals(0d, testSeq2D.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq2D.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq2D.getX(2), Math.ulp(10d));
        assertEquals(0d, testSeq2DM.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq2DM.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq2DM.getX(2), Math.ulp(10d));
        assertEquals(0d, testSeq3D.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq3D.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq3D.getX(2), Math.ulp(10d));
        assertEquals(0d, testSeq3DM.getX(0), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getX(1), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getX(2), Math.ulp(10d));

        try {
            testSeq3D.getX(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetY() {
        assertEquals(0d, testSeq2D.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq2D.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq2D.getY(2), Math.ulp(10d));
        assertEquals(0d, testSeq2DM.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq2DM.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq2DM.getY(2), Math.ulp(10d));
        assertEquals(0d, testSeq3D.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq3D.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq3D.getY(2), Math.ulp(10d));
        assertEquals(0d, testSeq3DM.getY(0), Math.ulp(10d));
        assertEquals(-1d, testSeq3DM.getY(1), Math.ulp(10d));
        assertEquals(-2d, testSeq3DM.getY(2), Math.ulp(10d));

        try {
            testSeq3D.getY(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetZ() {
        assertTrue(Double.isNaN(testSeq2D.getZ(0)));
        assertTrue(Double.isNaN(testSeq2D.getZ(1)));
        assertTrue(Double.isNaN(testSeq2D.getZ(2)));
        assertTrue(Double.isNaN(testSeq2DM.getZ(0)));
        assertTrue(Double.isNaN(testSeq2DM.getZ(1)));
        assertTrue(Double.isNaN(testSeq2DM.getZ(2)));
        assertEquals(0d, testSeq3D.getZ(0), Math.ulp(10d));
        assertEquals(1d, testSeq3D.getZ(1), Math.ulp(10d));
        assertEquals(2d, testSeq3D.getZ(2), Math.ulp(10d));
        assertEquals(0d, testSeq3DM.getZ(0), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getZ(1), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getZ(2), Math.ulp(10d));

        try {
            testSeq3D.getZ(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetM() {
        assertTrue(Double.isNaN(testSeq2D.getM(0)));
        assertTrue(Double.isNaN(testSeq2D.getM(1)));
        assertTrue(Double.isNaN(testSeq2D.getM(2)));
        assertTrue(Double.isNaN(testSeq3D.getM(0)));
        assertTrue(Double.isNaN(testSeq3D.getM(1)));
        assertTrue(Double.isNaN(testSeq3D.getM(2)));
        assertEquals(0d, testSeq2DM.getM(0), Math.ulp(10d));
        assertEquals(1d, testSeq2DM.getM(1), Math.ulp(10d));
        assertEquals(2d, testSeq2DM.getM(2), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getM(0), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getM(1), Math.ulp(10d));
        assertEquals(3d, testSeq3DM.getM(2), Math.ulp(10d));

        try {
            testSeq3D.getX(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetOrdinate() {

        for (int i = 0; i < 3; i++) {
            //X
            Assert.assertEquals(testSeq2D.getX(i), testSeq2D.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));
            Assert.assertEquals(testSeq2DM.getX(i), testSeq2DM.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));
            Assert.assertEquals(testSeq3D.getX(i), testSeq3D.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getX(i), testSeq3DM.getOrdinate(i, CoordinateSequence.X), Math.ulp(10d));

            //Y
            Assert.assertEquals(testSeq2D.getY(i), testSeq2D.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));
            Assert.assertEquals(testSeq2DM.getY(i), testSeq2DM.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));
            Assert.assertEquals(testSeq3D.getY(i), testSeq3D.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getY(i), testSeq3DM.getOrdinate(i, CoordinateSequence.Y), Math.ulp(10d));

            //Z
            Assert.assertEquals(testSeq2D.getZ(i), testSeq2D.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));
            Assert.assertEquals(testSeq2DM.getZ(i), testSeq2DM.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));
            Assert.assertEquals(testSeq3D.getZ(i), testSeq3D.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getZ(i), testSeq3DM.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));

            //M
            Assert.assertEquals(testSeq2D.getM(i), testSeq2D.getOrdinate(i, CoordinateSequence.M), Math.ulp(10d));
            Assert.assertEquals(testSeq2DM.getM(i), testSeq2DM.getOrdinate(i, CoordinateSequence.M), Math.ulp(10d));
            Assert.assertEquals(testSeq3D.getM(i), testSeq3D.getOrdinate(i, CoordinateSequence.M), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getM(i), testSeq3DM.getOrdinate(i, CoordinateSequence.M), Math.ulp(10d));


            try {
                testSeq2D.getOrdinate(i, 5);
                fail();
            } catch (IllegalArgumentException e) {
                //OK
            }

        }
        try {
            testSeq2D.getOrdinate(2, 5);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            testSeq2D.getOrdinate(4, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            //OK
        }

        try {
            testSeq3D.getOrdinate(3, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

    }

    @Test
    public void testEquals() {
        assertEquals(testSeq2D, new PackedPointSequence(new double[]{0, 0, 1, -1, 2, -2}, DimensionalFlag.d2D, CrsId.UNDEFINED));
        assertEquals(testSeq3D, new PackedPointSequence(new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}, DimensionalFlag.d3D, CrsId.UNDEFINED));
        assertEquals(testSeq2DM, new PackedPointSequence(new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}, DimensionalFlag.d2DM, CrsId.UNDEFINED));
        assertEquals(testSeq3DM, new PackedPointSequence(new double[]{0, 0, 0, 1, 1, -1, 1, 2, 2, -2, 2, 3}, DimensionalFlag.d3DM, CrsId.UNDEFINED));
        assertFalse(testSeq2DM.equals(testSeq3D));
        assertFalse(testSeq2D.equals(new PackedPointSequence(new double[]{0, 1, 1, -1, 2, -2}, DimensionalFlag.d2D, CrsId.UNDEFINED)));

    }


    //TODO -- correct unit tests
//    @Test
//    public void testEqualsDimensionallyLimited(){
//        assertTrue(testSeq3D.equals(new PackedPointSequence(new double[]{0, 0, 1, -1,2, -2}, DimensionalFlag.d2D), DimensionalFlag.d2D));
//        assertTrue(testSeq3D.equals(new PackedPointSequence(new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}, DimensionalFlag.d3D), DimensionalFlag.d2D));
//        assertTrue(testSeq3D.equals(new PackedPointSequence(new double[]{0, 0, 1, 1, -1, 2, 2, -2, 3}, DimensionalFlag.d3D), DimensionalFlag.d2D));
//        assertFalse(testSeq3D.equals(new PackedPointSequence(new double[]{0, 0, 1, 2, -1, 2, 2, -2, 3}, DimensionalFlag.d3D), DimensionalFlag.d2D));
//    }


    @Test
    public void testClone() {
        Assert.assertEquals(testSeq2D, testSeq2D.clone());
        Assert.assertEquals(testSeq2DM, testSeq2DM.clone());
        Assert.assertEquals(testSeq3D, testSeq3D.clone());
        Assert.assertEquals(testSeq3DM, testSeq3DM.clone());
    }

}
