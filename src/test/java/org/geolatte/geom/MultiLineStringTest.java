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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/8/11
 */
public class MultiLineStringTest {

    private MultiLineString ml1;
    private MultiLineString ml2;
    private MultiLineString empty;
    private MultiLineString closedSimple;

    double[] c1 = new double[]{0,0,1, 1,1,2, 2,2, 3};
    double[] c2 = new double[]{3,2,1, 4,2,2, 5,1, 3};
    double[] c3 = new double[]{5,1,3,  6,2,4}; //c2 and c3 touch in point (5,1,3)
    double[] cClosedSimple = new double[]{0,0,1, 0,1,1, 1,1,2, 1,0,3, 0,0,4};
    double[] cClosedNonSimple = new double[]{1,1,1, 1,-1,2, -1,1,3, -1,-1,4, 1,1,5 };

    LineString ls1 = LineString.create(new PackedPointSequence(c1, DimensionalFlag.XYM), CrsId.UNDEFINED);
    LineString ls2 = LineString.create(new PackedPointSequence(c2, DimensionalFlag.XYM), CrsId.UNDEFINED);
    LineString ls3 = LineString.create(new PackedPointSequence(c3, DimensionalFlag.XYM), CrsId.UNDEFINED);
    LineString lcs = LineString.create(new PackedPointSequence(cClosedSimple, DimensionalFlag.XYM), CrsId.UNDEFINED);
    LineString lcns = LineString.create(new PackedPointSequence(cClosedNonSimple, DimensionalFlag.XYM), CrsId.UNDEFINED);


    @Before
    public void setUp() {
        ml1 = MultiLineString.create(new LineString[]{ls1, ls2}, CrsId.UNDEFINED);
        ml2 = MultiLineString.create(new LineString[]{ls2, ls3}, CrsId.UNDEFINED);
        empty = MultiLineString.createEmpty();
        closedSimple = MultiLineString.create(new LineString[]{lcs}, CrsId.UNDEFINED);
        MultiLineString closedNonSimple = MultiLineString.create(new LineString[]{lcs,lcns}, CrsId.UNDEFINED);
    }

    @Test
    public void testConstituentsHaveSameDimension(){
        for (int i = 0; i < ml1.getNumGeometries(); i++) {
            assertEquals(((LineString) ml1.getGeometryN(i)).getPoints().getDimensionalFlag(), ml1.getPoints().getDimensionalFlag());
        }
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
        System.out.println("ml1 = " + ml1);
    }

    @Test
    public void testGetLength() throws Exception {
        assertEquals(0.0d, empty.getLength(), Math.ulp(1d));
        assertEquals(1+3 * Math.sqrt(2), ml1.getLength(), Math.ulp(1f));
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
    public void testGeometryVisitor() throws Exception {
        LengthCollectingVisitor visitor =  new LengthCollectingVisitor();
        ml1.accept(visitor);
        assertEquals(ml1.getLength(), visitor.length, Math.ulp(visitor.length));

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

    static class LengthCollectingVisitor implements GeometryVisitor {
            double length = 0.0d;

        @Override
        public void visit(Point point) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void visit(LineString lineString) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void visit(Polygon polygon) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void visit(GeometryCollection collection) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void visit(MultiLineString multiLineString) {
                for(Geometry g : multiLineString){
                    length += ((LineString)g).getLength();
                }
            }

        @Override
        public void visit(MultiPoint multiPoint) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void visit(MultiPolygon multiPolygon) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void visit(LinearRing linearRing) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void visit(PolyHedralSurface surface) {
            //To change body of implemented methods use File | Settings | File Templates.
        }


    }
}
