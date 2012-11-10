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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/7/12
 */
public class TestVector {

    @Test
    public void testDotProduct2D(){
        Point p0 = Points.create2D(2, 4);
        Point p1 = Points.create2D(3, 5);
        double received = Vector.dot(p0, p1);
        assertEquals("Wrong result vor dot-product of (2,4).(3,5): " , (26d), received, Math.ulp(10));

    }

    @Test
    public void testDotProductWithEmptyPointReturnsNaN(){
        Point p0 = Points.create2D(2, 3);
        Point p1 = Point.EMPTY;
        double v = Vector.dot(p0, p1);
        assertTrue("Dot-product with EMPTY point fails to return NaN: ", Double.isNaN(v));
    }

    @Test
    public void testDotProduct3D(){
        Point p0 = Points.create3D(2, 4, 3);
        Point p1 = Points.create3D(3, 5, 2);
        double received = Vector.dot(p0, p1);
        assertEquals("Wrong result vor dot-product of (2,4,3).(3,5,2): " , (32d), received, Math.ulp(10));

    }

    @Test
    public void testDotProduct3DLimitedTo2D(){
        Point p0 = Points.create3D(2,4,3);
        Point p1 = Points.create3D(3,5,2);
        double received = Vector.dot(p0, p1,true);
        assertEquals("Wrong result vor dot-product of (2,4,3).(3,5,2) with limitTo2D set: " , (26d), received, Math.ulp(10));
    }

    @Test
    public void testPointToSegmentBasicCases() {
        Point p0 = Points.create2D(0, 0);
        Point p1 = Points.create2D(2, 1);
        Point y = Points.create2D(1, 0.5);
        double[] received = Vector.pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 0.0d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 0.5, received[1], Math.ulp(100));

        y = Points.create2D(0, 0);
        received = Vector.pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 0.0d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 0, received[1], Math.ulp(100));

        y = Points.create2D(2, 1);
        received = Vector.pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 0.0d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 1.0d, received[1], Math.ulp(100));

        y = Points.create2D(-2, -1);
        received = Vector.pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 5d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", -1.0d, received[1], Math.ulp(100));

        y = Points.create2D(4, 2);
        received = Vector.pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 5d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 2.0d, received[1], Math.ulp(100));

        y = Points.create2D(1, 1);
        received = Vector.pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ",  0.2d*0.2d + 0.4d*0.4d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 0.6d, received[1], Math.ulp(100));
    }

}

