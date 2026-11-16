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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PackedPointSequenceTest {

    private final static PositionFactory<C2D> des2D = Positions.getFactoryFor(C2D.class);

    private final static PositionFactory<C3D> des3D = Positions.getFactoryFor(C3D.class);
    private final static PositionFactory<C2DM> des2DM = Positions.getFactoryFor(C2DM.class);
    private final static PositionFactory<C3DM> des3DM = Positions.getFactoryFor(C3DM.class);

    PackedPositionSequence<C2D> testSeq2D;
    PackedPositionSequence<C3D> testSeq3D;
    PackedPositionSequence<C2DM> testSeq2DM;
    PackedPositionSequence<C3DM> testSeq3DM;
    PositionSequence<C2D> testEmpty;


    @Before
    public void setUp() {
        testSeq2D = new PackedPositionSequence<>(des2D, new double[]{0, 0, 1, -1, 2, -2});
        testSeq3D = new PackedPositionSequence<>(des3D, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2});
        testSeq2DM = new PackedPositionSequence<>(des2DM, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2});
        testSeq3DM = new PackedPositionSequence<>(des3DM, new double[]{0, 0, 0, 1, 1, -1, 1, 2, 2, -2, 2, 3});
        testEmpty = new PackedPositionSequence<>(des2D, new double[0]);
    }


    @Test
    public void testConstructorThrowsIllegalArgumentOnWrongNumberOfCoordinates() {
        try {
            new PackedPositionSequence<>(des2D, new double[]{0, 0, 1, 1, 3});
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new PackedPositionSequence<>(des3D, new double[]{0, 0, 0, 1, 1, 1, 1});
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new PackedPositionSequence<>(des3D, new double[]{0, 0, 0, 1, 1, 1, 1, 1});
            fail();
        } catch (IllegalArgumentException e) {
        }
    }


    @Test
    public void testGetDimension() {
        assertEquals(2, testSeq2D.getCoordinateDimension());
        assertEquals(3, testSeq3D.getCoordinateDimension());
        assertEquals(3, testSeq2DM.getCoordinateDimension());
        assertEquals(4, testSeq3DM.getCoordinateDimension());
    }

    @Test
    public void testGetCoordinates() throws Exception {
        // test2D
        double[] result;
        result = new double[2];
        testSeq2D.getCoordinates(0, result);
        assertArrayEquals(new double[]{0, 0}, result, 0.0);
        testSeq2D.getCoordinates(1, result);
        assertArrayEquals(new double[]{1, -1}, result, 0.0);
        testSeq2D.getCoordinates(2, result);
        assertArrayEquals(new double[]{2, -2}, result, 0.0);

        result = new double[3];
        testSeq3D.getCoordinates(0, result);
        assertArrayEquals(new double[]{0, 0, 0}, result, 0.0);
        testSeq3D.getCoordinates(1, result);
        assertArrayEquals(new double[]{1, -1, 1}, result, 0.0);
        testSeq3D.getCoordinates(2, result);
        assertArrayEquals(new double[]{2, -2, 2}, result, 0.0);


        testSeq2DM.getCoordinates(0, result);
        assertArrayEquals(new double[]{0, 0, 0}, result, 0.0);
        testSeq2DM.getCoordinates(1, result);
        assertArrayEquals(new double[]{1, -1, 1}, result, 0.0);
        testSeq2DM.getCoordinates(2, result);
        assertArrayEquals(new double[]{2, -2, 2}, result, 0.0);

        result = new double[4];
        testSeq3DM.getCoordinates(0, result);
        assertArrayEquals(new double[]{0, 0, 0, 1}, result, 0.0);
        testSeq3DM.getCoordinates(1, result);
        assertArrayEquals(new double[]{1, -1, 1, 2}, result, 0.0);
        testSeq3DM.getCoordinates(2, result);
        assertArrayEquals(new double[]{2, -2, 2, 3}, result, 0.0);

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
    public void testSize() throws Exception {
        assertEquals(3, testSeq2D.size());
        assertEquals(3, testSeq3D.size());
        assertEquals(3, testSeq2DM.size());
        assertEquals(3, testSeq3DM.size());
        assertEquals(0, testEmpty.size());
    }


    @Test
    public void testEquals() {
        assertEquals(testSeq2D, new PackedPositionSequence<>(des2D, new double[]{0, 0, 1, -1, 2, -2}));
        assertEquals(testSeq3D, new PackedPositionSequence<>(des3D, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}));
        assertEquals(testSeq2DM, new PackedPositionSequence<>(des2DM, new double[]{0, 0, 0, 1, -1, 1, 2, -2, 2}));
        assertEquals(testSeq3DM, new PackedPositionSequence<>(des3DM, new double[]{0, 0, 0, 1, 1, -1, 1, 2, 2, -2, 2, 3}));
        assertNotEquals(testSeq2D, new PackedPositionSequence<>(des2D, new double[]{0, 1, 1, -1, 2, -2}));

    }


    @Test
    public void testClone() {
        Assert.assertEquals(testSeq2D, testSeq2D.clone());
        Assert.assertEquals(testSeq2DM, testSeq2DM.clone());
        Assert.assertEquals(testSeq3D, testSeq3D.clone());
        Assert.assertEquals(testSeq3DM, testSeq3DM.clone());
    }

    @Test
    public void testGetCoordinateByIndex() {
        double[] buffer = new double[4];
        PositionSequence<?>[] sequences = {testSeq2D, testSeq3D, testSeq2DM, testSeq3DM};
        for (PositionSequence<?> seq : sequences) {
            for (int i = 0; i < seq.size(); i++) {
                seq.getCoordinates(i, buffer);
                for (int k = 0; k < seq.getCoordinateDimension(); k++) {
                    assertEquals(buffer[k], seq.getCoordinate(i, k), 0);
                }
            }
            assertThrows(IndexOutOfBoundsException.class, () -> seq.getCoordinate(3, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> seq.getCoordinate(-1, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> seq.getCoordinate(0, seq.getCoordinateDimension()));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> testEmpty.getCoordinate(0, 0));
    }

    @Test
    public void testCloneReturnsEqualSequence() {
        PositionSequence<?> clone = testSeq2D.clone();
        assertEquals(testSeq2D, clone);
        assertNotSame(testSeq2D, clone);
    }

}
