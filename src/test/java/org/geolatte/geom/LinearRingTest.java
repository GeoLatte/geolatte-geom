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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 12/9/11
 */
public class LinearRingTest {

    PointSequence validPoints = PointSequenceFactory.create(new double[]{0,0,10,0,10,10,0,10,0,0}, DimensionalFlag.XY);

    PointSequence tooFewPoints = PointSequenceFactory.create(new double[]{0,0,0,0}, DimensionalFlag.XY);

    PointSequence notClosedPoints = PointSequenceFactory.create(new double[]{0,0,10,0,10,10,0,10,0.1,0}, DimensionalFlag.XY);


    @Test
    public void testValidLinearRing() {
        LinearRing valid = new LinearRing(validPoints, CrsId.UNDEFINED);
        assertNotNull(valid);
        assertFalse(valid.isEmpty());
        assertEquals(CrsId.UNDEFINED, valid.getCrsId());
    }


    @Test
    public void testLinearRingShouldHaveAtLeast4Points() {
        try {
            LinearRing invalid = new LinearRing(tooFewPoints, CrsId.UNDEFINED);
            fail("Non-empty linearRing should have at least 4 points.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLinearRingFromLineStringShouldHaveAtLeast4Points() {
        try {
            LineString l = new LineString(tooFewPoints, CrsId.UNDEFINED);
            new LinearRing(l);
            fail("Non-empty linearRing should have at least 4 points.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLinearRingMustBeClosed() {
        try {
            LinearRing invalid = new LinearRing(notClosedPoints, CrsId.UNDEFINED);
            fail("Non-empty linearRing should be closed.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLinearRingFromLineStringMustBeClosed() {
        try {
            LineString l = new LineString(notClosedPoints, CrsId.UNDEFINED);
            new LinearRing(l);
            fail("Non-empty linearRing should be closed.");
        } catch (IllegalArgumentException e) {
        }
    }

}
