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

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CrsId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class LinearRingsTest {


    @Test
    public void test2D() {

        LinearRing ring = LinearRings.create2D(CrsId.valueOf(4326))
                .add(0.0, 0.0)
                .add(0, 1)
                .add(1,1)
                .add(1,0)
                .add(0,0).build();

        assertNotNull(ring);
        assertEquals(5, ring.getPoints().size());
        assertEquals(Points.create2D(0.0, 0.0, CrsId.valueOf(4326)), ring.getPointN(0));
        assertEquals(Points.create2D(0.0, 1.0, CrsId.valueOf(4326)), ring.getPointN(1));
        assertEquals(CrsId.valueOf(4326), ring.getCrsId());
    }

    @Test
    public void test3D() {
        LinearRing ring = LinearRings.create3D(CrsId.valueOf(4326))
                .add(0.0, 0.0, 0)
                .add(0, 1, 1)
                .add(1,1, 1)
                .add(1,0,1)
                .add(0,0, 0).build();

        assertNotNull(ring);
        assertEquals(5, ring.getPoints().size());
        assertEquals(Points.create3D(0.0, 0.0, 0.0, CrsId.valueOf(4326)), ring.getPointN(0));
        assertEquals(Points.create3D(0.0, 1.0, 1.0, CrsId.valueOf(4326)), ring.getPointN(1));
        assertEquals(CrsId.valueOf(4326), ring.getCrsId());
    }

    @Test
    public void testEmpty() {
        LinearRing ring = LinearRings.create2D(CrsId.valueOf(4326)).build();
        assertNotNull(ring);
        assertEquals(ring, LinearRing.createEmpty());
        assertEquals(CrsId.valueOf(4326), ring.getCrsId());
    }

    @Test
    public void testNullCrsId() {
        LinearRing ring = LinearRings.create2D(null).add(0, 0).add(0, 1).add(1,1).add(0,0).build();
        assertEquals(CrsId.UNDEFINED, ring.getCrsId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooFewPoints1() {
        LineString lineString = LineStrings.create2D(null).add(1, 0).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooFewPoints2() {
        LinearRings.create2D(null).add(1, 0).add(1,0).build();
    }
}