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

package org.geolatte.geom.crs;

import org.junit.Test;

import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/21/11
 */
public class TestCoordinateReferenceSystem {

    //test Equality
    @Test
    public void testEquality(){
        CoordinateReferenceSystem crs1 = CrsRegistry.create(4326);
        CoordinateReferenceSystem crs2 = new GeographicCoordinateReferenceSystem(
                CrsId.parse("4326"), crs1.getName(),crs1.getAxes());
        assertEquals(crs1, crs2);
        assertEquals(crs1.hashCode(), crs2.hashCode());
    }

    //test Equality
    @Test
    public void testInEquality(){
        CoordinateReferenceSystem crs1 = CrsRegistry.create(4326);
        CoordinateReferenceSystem crs2 = new GeographicCoordinateReferenceSystem(
                CrsId.parse("4326"), "Other name",crs1.getAxes());
        assertNotSame(crs1, crs2);
        crs2 = new GeographicCoordinateReferenceSystem(
                CrsId.parse("4326"), crs1.getName(), crs1.getAxes()[1], crs1.getAxes()[0]);
        assertNotSame(crs1, crs2);
    }


}
