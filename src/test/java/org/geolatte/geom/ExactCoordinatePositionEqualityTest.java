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
 *         creation-date: 4/25/12
 */
public class ExactCoordinatePositionEqualityTest {

    PositionEquality eq = new ExactPositionEquality();

    G3DM pos1 = new G3DM(1d, 2d, 3d, 4d);
    G2D pos1_2d = new G2D(1, 2);
    G3DM pos2 = new G3DM(1, 2, 3, 4);
    G3DM pos3 = new G3DM(4, 3, 2, 1);
    G3DM pos4 = new G3DM(1, 2, 4, 3);
    G3DM pos5 = new G3DM(1, 2, 3, 5);
    G3DM pos6 = new G3DM(1, 2, 3, 5);


    @Test
    public void testNullPositionsAreNotEqualUnlessBothNull() {
        assertTrue(eq.equals((C2D)null, null));
        assertFalse(eq.equals(pos1, null));
        assertFalse(eq.equals(null, pos1));

    }

    @Test
    public void testEmptyPositionAlwaysEqual() {
        G2D empty1 = new G2D();
        G2D empty2 = new G2D();
        assertTrue(eq.equals(empty1, empty2));
        assertFalse(eq.equals(empty1, pos1_2d));
    }

    @Test
    public void testPointsAreSelfEqual() {
        assertTrue(eq.equals(pos1, pos1));
        assertTrue(eq.equals(pos1_2d, pos1_2d));
        assertTrue(eq.equals(pos4, pos4));
    }

    @Test
    public void testPositionsEqualIfSameCoordinatesAndCrs() {
        assertTrue(eq.equals(pos1, pos2));
        assertFalse(eq.equals(pos1, pos3));
        // this case is no longer valid -- assertFalse(eq.equals(pos1.toArray(null), WGS84, pos1_2d.toArray(null), WGS84));
        assertTrue(eq.equals(pos5, pos6));
    }

//    @Test
//    public void testPointsEqual2DIfSameCoordinatesIn2DAndSameCrs() {
//        assertTrue(eq2D.equals(pos1, pos1_2d));
//        assertTrue(eq2D.equals(pos1, pos2));
//        assertFalse(eq2D.equals(pos1, pos3));
//        assertTrue(eq2D.equals(pos1, pos4));
//        assertTrue(eq2D.equals(pos1, pos5));
//        assertFalse(eq2D.equals(pos5, pos6));
//    }

//    @Test
//    public void testPointsEqual3DIfSameCoordinatesIn2DAndSameCrs() {
//        assertFalse(eq3D.equals(pos1, pos1_2d));
//        assertTrue(eq3D.equals(pos1, pos2));
//        assertFalse(eq3D.equals(pos1, pos3));
//        assertFalse(eq3D.equals(pos1, pos4));
//        assertTrue(eq3D.equals(pos1, pos5));
//        assertFalse(eq3D.equals(pos5, pos6));
//    }


}
