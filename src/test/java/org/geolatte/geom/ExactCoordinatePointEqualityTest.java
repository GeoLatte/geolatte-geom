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

import org.geolatte.geom.crs.CrsId;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/12
 */
public class ExactCoordinatePointEqualityTest {

    PointEquality eq = new ExactCoordinatePointEquality();
    PointEquality eq2D = new ExactCoordinatePointEquality(DimensionalFlag.d2D);
    PointEquality eq3D = new ExactCoordinatePointEquality(DimensionalFlag.d3D);

    Point pnt1 = Points.create(1, 2, 3, 4, CrsId.valueOf(4326));
    Point pnt1_2D = Points.create(1, 2, CrsId.valueOf(4326));
    Point pnt2 = Points.create(1, 2, 3, 4, CrsId.valueOf(4326));
    Point pnt3 = Points.create(4, 3, 2, 1, CrsId.valueOf(4326));
    Point pnt4 = Points.create(1, 2, 4, 3, CrsId.valueOf(4326));
    Point pnt5 = Points.create(1, 2, 3, 5, CrsId.valueOf(4326));
    Point pnt6 = Points.create(1, 2, 3, 5, CrsId.valueOf(4328));


    @Test
    public void testNullPointsAreNotEqualUnlessBothNull() {
        assertTrue(eq.equals(null, null));
        assertTrue(eq2D.equals(null, null));
        assertTrue(eq3D.equals(null, null));

        assertFalse(eq.equals(pnt1, null));
        assertFalse(eq.equals(null, pnt1));

        assertFalse(eq2D.equals(pnt1, null));
        assertFalse(eq2D.equals(null, pnt1));

        assertFalse(eq3D.equals(pnt1, null));
        assertFalse(eq3D.equals(null, pnt1));
    }

    @Test
    public void testEmptyPointsAlwaysEqual() {
        Point emptyPnt1 = Point.createEmpty();
        Point emptyPnt2 = new Point(PointSequenceBuilders.fixedSized(0, DimensionalFlag.d2D).toPointSequence(), CrsId.UNDEFINED);
        assertTrue(eq.equals(emptyPnt1, emptyPnt2));
        assertFalse(eq.equals(emptyPnt1, pnt2));
    }

    @Test
    public void testPointsAreSelfEqual() {
        assertTrue(eq.equals(pnt1, pnt1));
        assertTrue(eq.equals(pnt1_2D, pnt1_2D));
        assertTrue(eq.equals(pnt4, pnt4));

        assertTrue(eq2D.equals(pnt1, pnt1));
        assertTrue(eq2D.equals(pnt1_2D, pnt1_2D));
        assertTrue(eq2D.equals(pnt4, pnt4));

        assertTrue(eq2D.equals(pnt1, pnt1));
        assertTrue(eq2D.equals(pnt1_2D, pnt1_2D));
        assertTrue(eq2D.equals(pnt4, pnt4));
    }

    @Test
    public void testPointsEqualIfSameCoordinatesAndCrs() {
        assertTrue(eq.equals(pnt1, pnt2));
        assertFalse(eq.equals(pnt1, pnt3));
        assertFalse(eq.equals(pnt1, pnt1_2D));
        assertFalse(eq.equals(pnt5, pnt6));
    }

    @Test
    public void testPointsEqual2DIfSameCoordinatesIn2DAndSameCrs() {
        assertTrue(eq2D.equals(pnt1, pnt1_2D));
        assertTrue(eq2D.equals(pnt1, pnt2));
        assertFalse(eq2D.equals(pnt1, pnt3));
        assertTrue(eq2D.equals(pnt1, pnt4));
        assertTrue(eq2D.equals(pnt1, pnt5));
        assertFalse(eq2D.equals(pnt5, pnt6));
    }

    @Test
    public void testPointsEqual3DIfSameCoordinatesIn2DAndSameCrs() {
        assertFalse(eq3D.equals(pnt1, pnt1_2D));
        assertTrue(eq3D.equals(pnt1, pnt2));
        assertFalse(eq3D.equals(pnt1, pnt3));
        assertFalse(eq3D.equals(pnt1, pnt4));
        assertTrue(eq3D.equals(pnt1, pnt5));
        assertFalse(eq3D.equals(pnt5, pnt6));
    }


}
