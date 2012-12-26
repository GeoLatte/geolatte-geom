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
 *         creation-date: 4/18/12
 */
public class TestExactCoordinatePointEquality {

    Point pnt1 = Points.create3DM(1, 2, 3, 4, CrsId.parse("EPSG:4326"));
    Point pnt2 = Points.create3DM(1, 2, 3, 4, CrsId.parse("EPSG:4326"));
    Point pnt3 = Points.create3DM(1, 2, 3, 4, CrsId.parse("EPSG:31370"));
    Point pnt4 = Points.create3DM(2, 3, 3, 4, CrsId.parse("EPSG:4326"));
    Point pnt1XY = Points.create2D(1, 2, CrsId.parse("EPSG:4326"));
    Point pnt2XY = Points.create2D(2, 3, CrsId.parse("EPSG:4326"));


    PointEquality eq = new ExactCoordinatePointEquality();

    @Test
    public void testBasicCasesForPoints() {
        assertTrue(eq.equals(pnt1, pnt1));
        assertTrue(eq.equals(pnt1, pnt2));
        assertFalse(eq.equals(pnt1, pnt3));
        assertFalse(eq.equals(pnt1, pnt4));
        assertFalse(eq.equals(pnt1XY, pnt1));
    }

    @Test
    public void testBasicCasesForPointCoordinates() {
        assertTrue(eq.equals(new double[]{1,2,3,4}, DimensionalFlag.d3DM, new double[]{1,2,3,4}, DimensionalFlag.d3DM));
        assertTrue(eq.equals(new double[]{1,2}, DimensionalFlag.d2D, new double[]{1,2}, DimensionalFlag.d2D));
        assertFalse(eq.equals(new double[]{1, 2, 3}, DimensionalFlag.d3D, new double[]{1, 2, 3}, DimensionalFlag.d2DM));
        assertFalse(eq.equals(new double[]{1, 2, 3}, DimensionalFlag.d3D, new double[]{1, 2, 3, 4}, DimensionalFlag.d3DM));
        assertFalse(eq.equals(new double[]{1, 2}, DimensionalFlag.d2D, new double[]{2, 3}, DimensionalFlag.d2D));
    }

    @Test
    public void testNullPoints() {
        assertTrue(eq.equals(null, null));
        assertFalse(eq.equals(pnt1, null));
        assertFalse(eq.equals(null, pnt1));
    }

    @Test
    public void testDimLimitedEquality() {
        PointEquality eq2D = new ExactCoordinatePointEquality(DimensionalFlag.d2D);
        assertTrue(eq2D.equals(pnt1, pnt1XY));
        assertTrue(eq2D.equals(pnt4, pnt2XY));
        assertFalse(eq2D.equals(pnt1XY, pnt4));
        assertFalse(eq2D.equals(pnt3, pnt1XY));
    }

    @Test
    public void testDimLimitedForPointCoordinates() {
        PointEquality eq2D = new ExactCoordinatePointEquality(DimensionalFlag.d2D);
        assertTrue(eq2D.equals(new double[]{1, 2, 3, 4}, DimensionalFlag.d3DM, new double[]{1, 2, 3, 4}, DimensionalFlag.d3DM));
        assertTrue(eq2D.equals(new double[]{1, 2, 3}, DimensionalFlag.d3D, new double[]{1, 2, 3, 4}, DimensionalFlag.d3DM));
        assertFalse(eq2D.equals(new double[]{1, 2}, DimensionalFlag.d2D, new double[]{2, 3}, DimensionalFlag.d2D));
    }


}
