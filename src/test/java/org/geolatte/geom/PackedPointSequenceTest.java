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
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LengthUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PackedPointSequenceTest {

    private static CoordinateReferenceSystem<P2D> crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
    private static CoordinateReferenceSystem<P3D> crsZ = crs.addVerticalAxis(LengthUnit.METER);
    private static CoordinateReferenceSystem<P2DM> crsM = crs.addMeasureAxis(LengthUnit.METER);
    private static CoordinateReferenceSystem<P3DM> crsZM = crsZ.addMeasureAxis(LengthUnit.METER);

    PackedPositionSequence<P2D> testSeq2D;
    PackedPositionSequence<P3D> testSeq3D;
    PackedPositionSequence<P2DM> testSeq2DM;
    PackedPositionSequence<P3DM> testSeq3DM;
    PositionSequence<P2D> testEmpty;


    @Before
    public void setUp() {
        testSeq2D = new PackedPositionSequence<>(crs, new double[]{0, 0, 1, -1, 2, -2});
        testSeq3D = new PackedPositionSequence<>(crsZ, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2});
        testSeq2DM = new PackedPositionSequence<>(crsM, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2});
        testSeq3DM = new PackedPositionSequence<>(crsZM, new double[]{0, 0, 0, 1, 1, -1, 1, 2, 2, -2, 2, 3});
        testEmpty = new PackedPositionSequence<>(crs, new double[0]);
    }


    @Test  @Ignore("Determine whether this is still desired behavior")
    public void testConstructorThrowsIllegalArgumentOnWrongNumberOfCoordinates() {
        try {
            new PackedPositionSequence<P2D>(crs, new double[]{0, 0, 1, 1});
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new PackedPositionSequence<P3D>(crsZ, new double[]{0, 0, 0, 1, 1, 1, 1});
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new PackedPositionSequence<P3D>(crsZ, new double[]{0, 0, 0, 1, 1, 1, 1, 1});
            fail();
        } catch (IllegalArgumentException e) {
        }
    }


    @Test
    public void testGetDimension() throws Exception {
        assertEquals(2, testSeq2D.getCoordinateDimension());
        assertEquals(3, testSeq3D.getCoordinateDimension());
        assertEquals(3, testSeq2DM.getCoordinateDimension());
        assertEquals(4, testSeq3DM.getCoordinateDimension());
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
        testSeq2D.getCoordinates(0, result);
        assertTrue(Arrays.equals(new double[]{0, 0}, result));
        testSeq2D.getCoordinates(1, result);
        assertTrue(Arrays.equals(new double[]{1, -1}, result));
        testSeq2D.getCoordinates(2, result);
        assertTrue(Arrays.equals(new double[]{2, -2}, result));

        result = new double[3];
        testSeq3D.getCoordinates(0, result);
        assertTrue(Arrays.equals(new double[]{0, 0, 0}, result));
        testSeq3D.getCoordinates(1, result);
        assertTrue(Arrays.equals(new double[]{1, -1, 1}, result));
        testSeq3D.getCoordinates(2, result);
        assertTrue(Arrays.equals(new double[]{2, -2, 2}, result));


        testSeq2DM.getCoordinates(0, result);
        assertTrue(Arrays.equals(new double[]{0, 0, 0}, result));
        testSeq2DM.getCoordinates(1, result);
        assertTrue(Arrays.equals(new double[]{1, -1, 1}, result));
        testSeq2DM.getCoordinates(2, result);
        assertTrue(Arrays.equals(new double[]{2, -2, 2}, result));

        result = new double[4];
        testSeq3DM.getCoordinates(0, result);
        assertTrue(Arrays.equals(new double[]{0, 0, 0, 1}, result));
        testSeq3DM.getCoordinates(1, result);
        assertTrue(Arrays.equals(new double[]{1, -1, 1, 2}, result));
        testSeq3DM.getCoordinates(2, result);
        assertTrue(Arrays.equals(new double[]{2, -2, 2, 3}, result));

        try {
            testSeq2D.getCoordinates(3, result);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3D.getCoordinates(3, result);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq2DM.getCoordinates(3, result);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testSeq3DM.getCoordinates(3, result);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            testEmpty.getCoordinates(0, result);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        result = new double[2];
        try {
            testSeq3D.getCoordinates(1, result);
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
        assertEquals(0d, testSeq3D.getPositionN(0).getAltitude(), Math.ulp(10d));
        assertEquals(1d, testSeq3D.getPositionN(1).getAltitude(), Math.ulp(10d));
        assertEquals(2d, testSeq3D.getPositionN(2).getAltitude(), Math.ulp(10d));
        assertEquals(0d, testSeq3DM.getPositionN(0).getAltitude(), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getPositionN(1).getAltitude(), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getPositionN(2).getAltitude(), Math.ulp(10d));

        try {
            testSeq3D.getPositionN(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetM() {
        assertEquals(0d, testSeq2DM.getPositionN(0).getM(), Math.ulp(10d));
        assertEquals(1d, testSeq2DM.getPositionN(1).getM(), Math.ulp(10d));
        assertEquals(2d, testSeq2DM.getPositionN(2).getM(), Math.ulp(10d));
        assertEquals(1d, testSeq3DM.getPositionN(0).getM(), Math.ulp(10d));
        assertEquals(2d, testSeq3DM.getPositionN(1).getM(), Math.ulp(10d));
        assertEquals(3d, testSeq3DM.getPositionN(2).getM(), Math.ulp(10d));

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
            Assert.assertEquals(testSeq3D.getPositionN(i).getAltitude(), testSeq3D.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));
            Assert.assertEquals(testSeq3DM.getPositionN(i).getAltitude(), testSeq3DM.getOrdinate(i, CoordinateSequence.Z), Math.ulp(10d));

            //M
            Assert.assertEquals(testSeq2DM.getPositionN(i).getM(), testSeq2DM.getOrdinate(i, CoordinateSequence.M), Math.ulp(10d));

            Assert.assertEquals(testSeq3DM.getPositionN(i).getM(), testSeq3DM.getOrdinate(i, CoordinateSequence.M), Math.ulp(10d));


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
        assertEquals(testSeq2D, new PackedPositionSequence<>(crs, new double[]{0, 0, 1, -1, 2, -2}));
        assertEquals(testSeq3D, new PackedPositionSequence<>(crsZ, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}));
        assertEquals(testSeq2DM, new PackedPositionSequence<>(crsM, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}));
        assertEquals(testSeq3DM, new PackedPositionSequence<>(crsZM,new double[]{0, 0, 0, 1, 1, -1, 1, 2, 2, -2, 2, 3}));
        assertFalse(testSeq2DM.equals(testSeq3D));
        assertFalse(testSeq2D.equals(new PackedPositionSequence<>(crs, new double[]{0, 1, 1, -1, 2, -2})));

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
