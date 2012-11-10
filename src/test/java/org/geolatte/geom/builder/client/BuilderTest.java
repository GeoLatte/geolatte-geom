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

package org.geolatte.geom.builder.client;

import org.junit.Test;

import static org.geolatte.geom.builder.Builder.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class BuilderTest {

    @Test
    public void testLineString2D(){
        lineString(4326, pnt2D(0, 0), pnt2D(1, 0), pnt2D(2,0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidLineString(){
        lineString(4326, pnt2D(0, 0));
    }

    @Test
    public void testvalidPolygon(){
        polygon(4326, ring(pnt2D(0,0), pnt2D(0,1), pnt2D(1,1), pnt2D(1,0), pnt2D(0,0)));
    }

}
