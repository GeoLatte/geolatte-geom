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

package org.geolatte.geom;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/13/12
 */
public class PointCollectionPointEqualityTest {

    PointSequence ps1 = PointSequenceBuilders.fixedSized(3, DimensionalFlag.d3DM)
            .add(1, 2, 3, 4)
            .add(2, 3, 4, 5)
            .add(3, 4, 5, 6).toPointSequence();

    PointSequence ps1_2D = PointSequenceBuilders.fixedSized(3, DimensionalFlag.d2D)
            .add(1, 2)
            .add(2, 3)
            .add(3, 4).toPointSequence();


    PointSequence ps2 = PointSequenceBuilders.fixedSized(3, DimensionalFlag.d3DM)
            .add(1, 2, 3, 4)
            .add(2, 3, 4, 5)
            .add(3, 4, 5, 6).toPointSequence();

    PointSequence ps3 = PointSequenceBuilders.fixedSized(3, DimensionalFlag.d3DM)
            .add(1, 2, 4, 5)
            .add(2, 3, 3, 2)
            .add(3, 4, 5, 2).toPointSequence();

    PointSequence ps4 = PointSequenceBuilders.fixedSized(3, DimensionalFlag.d3DM)
            .add(2, 2, 4, 5)
            .add(2, 4, 3, 2)
            .add(3, 4, 5, 2).toPointSequence();

    PointCollectionPointEquality eq = new PointCollectionPointEquality();
    PointCollectionPointEquality eq2D = new PointCollectionPointEquality(new ExactCoordinatePointEquality(
            DimensionalFlag.d2D
    ));

    @Test
    public void testNullArgumentsAreEqual() {
        assertTrue(eq.equals(null, null));
        assertFalse(eq.equals(null, ps1));
        assertFalse(eq.equals(ps1,null));
    }

    @Test
    public void testEmptyPointSequencesAreEqual(){
        assertTrue(eq.equals(
                PointSequenceBuilders.fixedSized(0, DimensionalFlag.d2D).toPointSequence(),
                PointSequenceBuilders.fixedSized(0, DimensionalFlag.d3DM).toPointSequence()));

        assertFalse(eq.equals(
                PointSequenceBuilders.fixedSized(0, DimensionalFlag.d2D).toPointSequence(),
                ps1));
    }

    @Test
    public void testSelfEquality() {
        assertTrue(eq.equals(ps1,ps1));
        assertTrue(eq.equals(ps2,ps2));
        assertTrue(eq.equals(ps3,ps3));
        assertTrue(eq.equals(ps1_2D,ps1_2D));
    }

    @Test
    public void testEquality() {
        assertTrue(eq.equals(ps1, ps2));
        assertFalse(eq.equals(ps1, ps3));
    }

    @Test
    public void testEquality2D() {
        assertTrue(eq2D.equals(ps1, ps2));
        assertTrue(eq2D.equals(ps1, ps1_2D));
        assertTrue(eq2D.equals(ps1, ps3));
        assertFalse(eq2D.equals(ps1, ps4));
    }


}
