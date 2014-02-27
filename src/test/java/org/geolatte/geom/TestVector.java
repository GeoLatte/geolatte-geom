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

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LengthUnit;
import org.junit.Test;

import static org.geolatte.geom.Vector.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/7/12
 */
public class TestVector {

    private static CoordinateReferenceSystem<P2D> crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
    private static CoordinateReferenceSystem<P3D> crsZ = crs.addVerticalAxis(LengthUnit.METER);
    private static CoordinateReferenceSystem<P2DM> crsM = crs.addMeasureAxis(LengthUnit.METER);


    @Test
    public void testDotProduct2D() {
        P2D p0 = new P2D(crs, 2, 4);
        P2D p1 = new P2D(crs, 3, 5);
        double received = dot(p0, p1);
        assertEquals("Wrong result vor dot-product of (2,4).(3,5): ", (26d), received, Math.ulp(10));

    }

    @Test
    public void testDotProductWithEmptyPointReturnsNaN() {
        P2D p0 = new P2D(crs, 2, 3);
        P2D p1 = new P2D(crs);
        double v = dot(p0, p1);
        assertTrue("Dot-product with EMPTY P2D fails to return NaN: ", Double.isNaN(v));
    }

    @Test
    public void testDotProduct3D() {
        P3D p0 = new P3D(crsZ, 2, 4, 3);
        P3D p1 = new P3D(crsZ, 3, 5, 2);
        double received = dot(p0, p1);
        assertEquals("Wrong result vor dot-product of (2,4,3).(3,5,2): ", (32d), received, Math.ulp(10));

    }

    @Test
    public void testDotProduct3DLimitedTo2D() {
        P3D p0 = new P3D(crsZ, 2, 4, 3);
        P3D p1 = new P3D(crsZ, 3, 5, 2);
        double received = dot(p0, p1, true);
        assertEquals("Wrong result vor dot-product of (2,4,3).(3,5,2) with limitTo2D set: ", (26d), received, Math.ulp(10));
    }

    @Test
    public void testPointToSegmentBasicCases() {
        P2D p0 = new P2D(crs, 0, 0);
        P2D p1 = new P2D(crs, 2, 1);
        P2D y = new P2D(crs, 1, 0.5);
        double[] received = pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 0.0d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 0.5, received[1], Math.ulp(100));

        y = new P2D(crs, 0, 0);
        received = pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 0.0d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 0, received[1], Math.ulp(100));

        y = new P2D(crs, 2, 1);
        received = pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 0.0d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 1.0d, received[1], Math.ulp(100));

        y = new P2D(crs, -2, -1);
        received = pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 5d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", -1.0d, received[1], Math.ulp(100));

        y = new P2D(crs, 4, 2);
        received = pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 5d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 2.0d, received[1], Math.ulp(100));

        y = new P2D(crs, 1, 1);
        received = pointToSegment2D(p0, p1, y);
        assertEquals("Squared distance of y to p0-p1 should be: ", 0.2d * 0.2d + 0.4d * 0.4d, received[0], Math.ulp(100));
        assertEquals("Projection should be: ", 0.6d, received[1], Math.ulp(100));
    }

    @Test
    public void testPerpBasicCases() {
        P2DM p = new P2DM(crsM, 1, 0.2, 3);
        P2DM expected = new P2DM(crsM, -0.2, 1, 3);
        assertEquals(expected, perp(p));
        assertEquals(new P2DM(crsM, -1, -0.2, 3), perp(perp(p)));
    }

    @Test
    public void testNullPoint() {
        assertNull(perp(null));
    }

    @Test
    public void testEmptyPoint() {
        assertEquals(new P2D(crs), perp(new P2D(crs)));
    }

    @Test
    public void testPerpDotBasicCase() {
        P2D p0 = new P2D(crs, 1, 1);
        P2D p1 = new P2D(crs, 1, 1.2);
        P2D p2 = new P2D(crs, 1, 0.8);

        //check definition
        assertEquals(dot(perp(p0), p1), perpDot(p0, p1), Math.ulp(10));
        assertEquals(dot(perp(p1), p0), perpDot(p1, p0), Math.ulp(10));
        assertEquals(dot(perp(p0), p2), perpDot(p0, p2), Math.ulp(10));

        //p1 is to the left of p0, so positive sign
        assertTrue(perpDot(p0, p1) > 0);

        //p2 is to the right of p1, so negative sign
        assertTrue(perpDot(p0, p2) < 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentPerpDot() {
        perpDot(null, new P2D(crs));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPointArgument() {
        perpDot(new P2D(crs), new P2D(crs, 1, 2));
    }

}

