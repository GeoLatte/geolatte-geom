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

import org.geolatte.geom.builder.DSL;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.geolatte.geom.PositionSequenceBuilders.variableSized;
import static org.geolatte.geom.builder.DSL.point;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 12/9/11
 */
public class PolygonTest {

    private static CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;


    PositionSequence<C2D> shellPoints = variableSized(C2D.class).add(0, 0).add(10, 0).add(10, 10).add( 0, 10).add( 0, 0).toPositionSequence();
    PositionSequence<C2D> innerPoints = variableSized(C2D.class).add(1, 1).add( 9, 1).add(9, 9).add(1, 9).add( 1, 1).toPositionSequence();

    PositionSequence<C2D> shellPoints2 = variableSized(C2D.class).add(0, 0).add(10, 0).add(10, 10).add(0, 10).add(0, 0).toPositionSequence();
    PositionSequence<C2D> innerPoints2 = variableSized(C2D.class).add(1, 1).add( 9, 1).add( 9, 9).add( 1, 9).add( 1, 1).toPositionSequence();

    PositionSequence<C2D> shellPoints3 = variableSized(C2D.class).add(1, 1).add(10, 0).add(10, 10).add(0, 10).add(1, 1).toPositionSequence();


    @Test
    public void testEmptyRingsThrowIllegalArgumentException(){
        try {
            LinearRing<C2D> shell = new LinearRing<C2D>(crs);
            new Polygon<C2D>(new LinearRing[]{shell});
            fail("Polygon with empty shell should throw IllegalArgumentException.");
        } catch(IllegalArgumentException e){}

        try {
            LinearRing<C2D> shell = new LinearRing<C2D>(shellPoints, crs);
            LinearRing<C2D> emptyInner = new LinearRing<C2D>(crs);
            new Polygon<C2D>(new LinearRing[]{shell, emptyInner});
            fail("Polygon with empty inner ring should throw IllegalArgumentException.");
        } catch(IllegalArgumentException e){}
    }

    @Test
    public void testPolygonEquality() {
        LinearRing<C2D> shell = new LinearRing<C2D>(shellPoints, crs);
        LinearRing<C2D> inner = new LinearRing<C2D>(innerPoints, crs);
        Polygon<C2D> polygon1 = new Polygon<C2D>(new LinearRing[]{shell, inner});

        shell = new LinearRing<C2D>(shellPoints2, crs);
        inner = new LinearRing<C2D>(innerPoints2, crs);
        Polygon<C2D> polygon2 = new Polygon<C2D>(new LinearRing[]{shell, inner});

        shell = new LinearRing(shellPoints3, crs);
        Polygon<C2D> polygon3 = new Polygon<C2D>(new LinearRing[]{shell, inner});


        assertTrue(polygon1.equals(polygon1));
        assertTrue(new GeometryPointEquality().equals(null, null));
        assertFalse(polygon1.equals(null));

        assertTrue(polygon1.equals(polygon2));
        assertTrue(polygon2.equals(polygon1));

        assertFalse(polygon1.equals(polygon3));
        assertFalse(polygon3.equals(polygon1));

        assertFalse(polygon1.equals(point(crs, DSL.c(1, 2))));

    }


}
