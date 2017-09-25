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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.geolatte.geom.CrsMock.*;
/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/8/11
 */
public class MultiLineStringTest {


    private static PositionFactory<C2D> d2D = Positions.getFactoryFor(crs.getPositionClass());
    private static PositionFactory<C2DM> d2DM = Positions.getFactoryFor(crsM.getPositionClass());

    private MultiLineString<C2DM> ml1;
    private MultiLineString<C2DM> ml2;
    private MultiLineString<C2DM> empty;
    private MultiLineString<C2DM> closedSimple;

    double[] c1 = new double[]{0,0,1, 1,1,2, 2,2, 3};
    double[] c2 = new double[]{3,2,1, 4,2,2, 5,1, 3};
    double[] c3 = new double[]{5,1,3,  6,2,4}; //c2 and c3 touch in point (5,1,3)
    double[] cClosedSimple = new double[]{0,0,1, 0,1,1, 1,1,2, 1,0,3, 0,0,4};
    double[] cClosedNonSimple = new double[]{1,1,1, 1,-1,2, -1,1,3, -1,-1,4, 1,1,5 };

    LineString<C2DM> ls1 = new LineString<C2DM>(new PackedPositionSequence<C2DM>(d2DM, c1), crsM);
    LineString<C2DM> ls2 = new LineString<C2DM>(new PackedPositionSequence<C2DM>(d2DM, c2), crsM);
    LineString<C2DM> ls3 = new LineString<C2DM>(new PackedPositionSequence<C2DM>(d2DM,c3), crsM);
    LineString<C2DM> lcs = new LineString<C2DM>(new PackedPositionSequence<C2DM>(d2DM, cClosedSimple), crsM);
    LineString<C2DM> lcns = new LineString<C2DM>(new PackedPositionSequence<C2DM>(d2DM, cClosedNonSimple), crsM);


    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        ml1 = new MultiLineString<C2DM>(ls1, ls2);
        ml2 = new MultiLineString<C2DM>(ls2, ls3);
        empty = new MultiLineString(crs);
        closedSimple = new MultiLineString<C2DM>(lcs);
        MultiLineString closedNonSimple = new MultiLineString(lcs,lcns);
    }

    @Test
    public void testGetNumGeometries() throws Exception {
        assertEquals(0, empty.getNumGeometries());
        assertEquals(1, closedSimple.getNumGeometries());
        assertEquals(2, ml1.getNumGeometries());
        assertEquals(2,ml2.getNumGeometries());
    }

    @Test
    public void testGetGeometryN() throws Exception {
        assertEquals(ls1, ml1.getGeometryN(0));
        assertEquals(ls2, ml1.getGeometryN(1));
    }


    @Test
    public void testIterator() throws Exception {
        int i =  0;
        for (Geometry geom : ml1) {
            assertNotNull(geom);
            i++;
        }
        assertEquals(2, i);
    }


    @Test
    public void testIsClosed() throws Exception {

    }

    @Test
    public void testGetDimension() throws Exception {

    }

    @Test
    public void testGetGeometryType() throws Exception {

    }

    @Test
    public void testGetBoundary() throws Exception {

    }

}
