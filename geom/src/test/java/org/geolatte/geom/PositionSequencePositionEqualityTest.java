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
public class PositionSequencePositionEqualityTest {


    PositionSequence<C3DM> ps1 = PositionSequenceBuilders.fixedSized(3, C3DM.class)
            .add(1, 2, 3, 4)
            .add(2, 3, 4, 5)
            .add(3, 4, 5, 6).toPositionSequence();

    PositionSequence<C2D> ps1_2D = PositionSequenceBuilders.fixedSized(3, C2D.class)
            .add(1, 2)
            .add(2, 3)
            .add(3, 4).toPositionSequence();


    PositionSequence<C3DM> ps2 = PositionSequenceBuilders.fixedSized(3, C3DM.class)
            .add(1, 2, 3, 4)
            .add(2, 3, 4, 5)
            .add(3, 4, 5, 6).toPositionSequence();

    PositionSequence<C3DM> ps3 = PositionSequenceBuilders.fixedSized(3, C3DM.class)
            .add(1, 2, 4, 5)
            .add(2, 3, 3, 2)
            .add(3, 4, 5, 2).toPositionSequence();

    PositionSequence<C3DM> ps4 = PositionSequenceBuilders.fixedSized(3, C3DM.class)
            .add(2, 2, 4, 5)
            .add(2, 4, 3, 2)
            .add(3, 4, 5, 2).toPositionSequence();

    PositionSequencePositionEquality eq = new PositionSequencePositionEquality();



    @Test
    public void testNullArgumentsAreEqual() {
        assertTrue(eq.equals(null, null));
        assertFalse(eq.equals(null, ps1));
        assertFalse(eq.equals(ps1, null));
    }

    @Test
    public void testEmptyPointSequencesAreEqual() {
        assertTrue(eq.equals(
                PositionSequenceBuilders.fixedSized(0, C2D.class).toPositionSequence(),
                PositionSequenceBuilders.fixedSized(0, C2D.class).toPositionSequence()));

        assertFalse(eq.equals(
                PositionSequenceBuilders.fixedSized(0, C2D.class).toPositionSequence(),
                ps1_2D));
    }

    @Test
    public void testSelfEquality() {
        assertTrue(eq.equals(ps1, ps1));
        assertTrue(eq.equals(ps2, ps2));
        assertTrue(eq.equals(ps3, ps3));
        assertTrue(eq.equals(ps1_2D, ps1_2D));
    }

    @Test
    public void testEquality() {
        assertTrue(eq.equals(ps1, ps2));
        assertFalse(eq.equals(ps1, ps3));
    }


}
