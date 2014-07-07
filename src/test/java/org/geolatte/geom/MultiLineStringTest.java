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

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LengthUnit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/8/11
 */
public class MultiLineStringTest {

    private static CoordinateReferenceSystem<P2D> crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
//    private static CoordinateReferenceSystem<P3D> crsZ = crs.addVerticalAxis(LengthUnit.METER);
    private static CoordinateReferenceSystem<P2DM> crsM = crs.addMeasureAxis(LengthUnit.METER);
//    private static CoordinateReferenceSystem<P3DM> crsZM = crsZ.addMeasureAxis(LengthUnit.METER);
//    private static CoordinateReferenceSystem<P2D> l72 = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);

    private MultiLineString<P2DM> ml1;
    private MultiLineString<P2DM> ml2;
    private MultiLineString<P2DM> empty;
    private MultiLineString<P2DM> closedSimple;

    double[] c1 = new double[]{0,0,1, 1,1,2, 2,2, 3};
    double[] c2 = new double[]{3,2,1, 4,2,2, 5,1, 3};
    double[] c3 = new double[]{5,1,3,  6,2,4}; //c2 and c3 touch in point (5,1,3)
    double[] cClosedSimple = new double[]{0,0,1, 0,1,1, 1,1,2, 1,0,3, 0,0,4};
    double[] cClosedNonSimple = new double[]{1,1,1, 1,-1,2, -1,1,3, -1,-1,4, 1,1,5 };

    LineString<P2DM> ls1 = new LineString<>(new PackedPositionSequence<>(crsM, c1));
    LineString<P2DM> ls2 = new LineString<>(new PackedPositionSequence<>(crsM, c2));
    LineString<P2DM> ls3 = new LineString<>(new PackedPositionSequence<>(crsM,c3));
    LineString<P2DM> lcs = new LineString<>(new PackedPositionSequence<>(crsM, cClosedSimple));
    LineString<P2DM> lcns = new LineString<>(new PackedPositionSequence<>(crsM, cClosedNonSimple));


    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        ml1 = new MultiLineString<>(ls1, ls2);
        ml2 = new MultiLineString<>(ls2, ls3);
        empty = new MultiLineString(crs);
        closedSimple = new MultiLineString<>(lcs);
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
