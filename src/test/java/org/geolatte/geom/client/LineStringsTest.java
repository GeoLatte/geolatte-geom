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

package org.geolatte.geom.client;

import org.geolatte.geom.LineString;
import org.geolatte.geom.builder.LineStrings;
import org.geolatte.geom.Points;
import org.geolatte.geom.crs.CrsId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * LineStringTest class.
 *
 * <p>This class is in a separate package to test the public API exclusively.<p>
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class LineStringsTest {

    @Test
    public void test2D() {

        LineString lineString = LineStrings.create2D(CrsId.valueOf(4326))
                .add(0.0, 0.0)
                .add(1, 1).build();

        assertNotNull(lineString);
        assertEquals(2, lineString.getPoints().size());
        assertEquals(Points.create2D(0.0, 0.0, CrsId.valueOf(4326)), lineString.getPointN(0));
        assertEquals(Points.create2D(1.0, 1.0, CrsId.valueOf(4326)), lineString.getPointN(1));
        assertEquals(CrsId.valueOf(4326), lineString.getCrsId());
    }

    @Test
    public void test3D(){
        LineString lineString = LineStrings.create3D(CrsId.valueOf(4326))
                .add(0.0, 0.0, 0.0)
                .add(1, 2, 3).build();

        assertNotNull(lineString);
        assertEquals(2, lineString.getPoints().size());
        assertEquals(Points.create3D(0.0, 0.0, 0.0, CrsId.valueOf(4326)), lineString.getPointN(0));
        assertEquals(Points.create3D(1.0, 2.0, 3.0, CrsId.valueOf(4326)), lineString.getPointN(1));
        assertEquals(CrsId.valueOf(4326), lineString.getCrsId());
    }

    @Test
    public void testEmpty(){
        LineString lineString = LineStrings.create2D(CrsId.valueOf(4326)).build();
        assertNotNull(lineString);
        assertEquals(lineString, LineString.createEmpty());
        assertEquals(CrsId.valueOf(4326), lineString.getCrsId());
    }

    @Test
    public void testNullCrsId(){
        LineString lineString = LineStrings.create2D(null).add(1,0).add(2,1).build();
        assertEquals(CrsId.UNDEFINED, lineString.getCrsId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooFewPoints() {
        LineString lineString = LineStrings.create2D(null).add(1, 0).build();
        System.out.println(lineString);
    }
}
