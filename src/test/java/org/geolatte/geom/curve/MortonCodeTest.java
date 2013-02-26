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
 * Copyright (C) 2010 - 2013 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.curve;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link MortonCode}
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/21/13
 */
public class MortonCodeTest {

    Envelope extent = new Envelope(point(0, c(0.0, 0.0)), point(0, c(100.0, 100.0)));
    MortonContext ctxtLvl1 = new MortonContext(extent, 1);
    MortonContext ctxtLvl2 = new MortonContext(extent, 2);
    MortonCode mcLevel1 = new MortonCode(ctxtLvl1);
    MortonCode mcLevel2 = new MortonCode(ctxtLvl2);

    @Test
    public void testLevel1() {
        assertEquals("0", mcLevel1.ofPoint(point(0, c(0, 0))));
        assertEquals("0", mcLevel1.ofPoint(point(0, c(10, 49))));
        assertEquals("1", mcLevel1.ofPoint(point(0, c(0, 50))));
        assertEquals("1", mcLevel1.ofPoint(point(0, c(49, 50))));
        assertEquals("2", mcLevel1.ofPoint(point(0, c(50, 0))));
        assertEquals("2", mcLevel1.ofPoint(point(0, c(99, 49))));
        assertEquals("3", mcLevel1.ofPoint(point(0, c(50, 50))));
        assertEquals("3", mcLevel1.ofPoint(point(0, c(100, 100))));
    }

    @Test
    public void testLevel2() {
        assertEquals("00", mcLevel2.ofPoint(point(0, c(0, 0))));
        assertEquals("01", mcLevel2.ofPoint(point(0, c(10, 40))));
        assertEquals("02", mcLevel2.ofPoint(point(0, c(30, 20))));
        assertEquals("03", mcLevel2.ofPoint(point(0, c(45, 45))));

        assertEquals("10", mcLevel2.ofPoint(point(0, c(10, 55))));
        assertEquals("11", mcLevel2.ofPoint(point(0, c(10, 90))));
        assertEquals("12", mcLevel2.ofPoint(point(0, c(49, 55))));
        assertEquals("13", mcLevel2.ofPoint(point(0, c(49, 80))));

        assertEquals("20", mcLevel2.ofPoint(point(0, c(50, 0))));
        assertEquals("21", mcLevel2.ofPoint(point(0, c(74, 30))));
        assertEquals("22", mcLevel2.ofPoint(point(0, c(76, 24))));
        assertEquals("23", mcLevel2.ofPoint(point(0, c(76, 49))));

        assertEquals("30", mcLevel2.ofPoint(point(0, c(50, 50))));
        assertEquals("31", mcLevel2.ofPoint(point(0, c(74, 76))));
        assertEquals("32", mcLevel2.ofPoint(point(0, c(76, 74))));
        assertEquals("33", mcLevel2.ofPoint(point(0, c(76, 79))));

    }

    @Test
    public void testPolygonLevel2(){
        Polygon pg = polygon(0, ring(c(10,10), c(10, 40), c(40, 40), c(40,10), c(10,10)));
        assertEquals("0", mcLevel2.ofGeometry(pg));

        pg = polygon(0, ring(c(60,60), c(60, 90), c(90, 90), c(90,60), c(60,60)));
        assertEquals("3", mcLevel2.ofGeometry(pg));

        pg = polygon(0, ring(c(10,10), c(10, 90), c(90, 90), c(90,10), c(10,10)));
        assertTrue(mcLevel2.ofGeometry(pg).isEmpty());

    }

    @Test
    public void testofPointandofGeometryGiveSameResultForPoints(){
        Point pnt = point(0, c(0, 0));
        assertEquals(mcLevel2.ofGeometry(pnt), mcLevel2.ofPoint(pnt));

        pnt = point(0, c(10, 55));
        assertEquals(mcLevel2.ofGeometry(pnt), mcLevel2.ofPoint(pnt));

        pnt = point(0, c(76, 24));
        assertEquals(mcLevel2.ofGeometry(pnt), mcLevel2.ofPoint(pnt));

        pnt = point(0, c(76, 79));
        assertEquals(mcLevel2.ofGeometry(pnt), mcLevel2.ofPoint(pnt));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCoordinateWrongCrsIdOfGeometry() {
        mcLevel1.ofGeometry(point(4326, c(1, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCoordinateWrongCrsIdOfPoint() {
        mcLevel1.ofPoint(point(4326, c(1, 1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCoordinateToRightOfExtentPoint() {
        mcLevel1.ofPoint(point(0, c(101, 101)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCoordinateToRightOfExtentGeometry() {
        mcLevel1.ofGeometry(point(0, c(101, 101)));
    }


}
