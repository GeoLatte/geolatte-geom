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

import static org.junit.Assert.fail;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 12/9/11
 */
public class PolygonTest {


    PointSequence shellPoints = PointSequenceFactory.create(new double[]{0,0,10,0,10,10,0,10,0,0}, DimensionalFlag.XY);
    PointSequence innerPoints = PointSequenceFactory.create(new double[]{1,1,9,1,9,9,1,9,1,1}, DimensionalFlag.XY);

    @Test
    public void testEmptyRingsThrowIllegalArgumentException(){
        try {
            LinearRing shell = LinearRing.createEmpty();
            Polygon polygon = Polygon.create(new LinearRing[]{shell}, CrsId.UNDEFINED);
            fail("Polygon with empty shell should throw IllegalArgumentException.");
        } catch(IllegalArgumentException e){}

        try {
            LinearRing shell = LinearRing.create(shellPoints, CrsId.UNDEFINED);
            LinearRing emptyInner = LinearRing.createEmpty();
            Polygon polygon = Polygon.create(new LinearRing[]{shell, emptyInner}, CrsId.UNDEFINED);
            fail("Polygon with empty inner ring should throw IllegalArgumentException.");
        } catch(IllegalArgumentException e){}
    }


}
