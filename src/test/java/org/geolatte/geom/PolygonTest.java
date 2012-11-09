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
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.crs.CrsId;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 12/9/11
 */
public class PolygonTest {


    PointSequence shellPoints = PointCollectionFactory.create(new double[]{0, 0, 10, 0, 10, 10, 0, 10, 0, 0}, DimensionalFlag.d2D, CrsId.UNDEFINED);
    PointSequence innerPoints = PointCollectionFactory.create(new double[]{1, 1, 9, 1, 9, 9, 1, 9, 1, 1}, DimensionalFlag.d2D, CrsId.UNDEFINED);

    PointSequence shellPoints2 = PointCollectionFactory.create(new double[]{0, 0, 10, 0, 10, 10, 0, 10, 0, 0}, DimensionalFlag.d2D, CrsId.UNDEFINED);
    PointSequence innerPoints2 = PointCollectionFactory.create(new double[]{1, 1, 9, 1, 9, 9, 1, 9, 1, 1}, DimensionalFlag.d2D, CrsId.UNDEFINED);

    PointSequence shellPoints3 = PointCollectionFactory.create(new double[]{1, 1, 10, 0, 10, 10, 0, 10, 1, 1}, DimensionalFlag.d2D, CrsId.UNDEFINED);


    @Test
    public void testEmptyRingsThrowIllegalArgumentException(){
        try {
            LinearRing shell = LinearRing.createEmpty();
            new Polygon(new LinearRing[]{shell});
            fail("Polygon with empty shell should throw IllegalArgumentException.");
        } catch(IllegalArgumentException e){}

        try {
            LinearRing shell = new LinearRing(shellPoints);
            LinearRing emptyInner = LinearRing.createEmpty();
            new Polygon(new LinearRing[]{shell, emptyInner});
            fail("Polygon with empty inner ring should throw IllegalArgumentException.");
        } catch(IllegalArgumentException e){}
    }

    @Test
    public void testPolygonEquality() {
        LinearRing shell = new LinearRing(shellPoints);
        LinearRing inner = new LinearRing(innerPoints);
        Polygon polygon1 = new Polygon(new LinearRing[]{shell, inner});

        shell = new LinearRing(shellPoints2);
        inner = new LinearRing(innerPoints2);
        Polygon polygon2 = new Polygon(new LinearRing[]{shell, inner});

        shell = new LinearRing(shellPoints3);
        Polygon polygon3 = new Polygon(new LinearRing[]{shell, inner});


        assertTrue(polygon1.equals(polygon1));
        assertTrue(new GeometryPointEquality().equals(null, null));
        assertFalse(polygon1.equals(null));

        assertTrue(polygon1.equals(polygon2));
        assertTrue(polygon2.equals(polygon1));

        assertFalse(polygon1.equals(polygon3));
        assertFalse(polygon3.equals(polygon1));

        assertFalse(polygon1.equals(Points.create(1, 2)));

    }


}
