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

import com.vividsolutions.jts.geom.CoordinateSequence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class LineStringImplTest {



    double[] coordinates = new double[]{
            0,0,0,0,
            1,-1,1,2,
            2,-2,3,4
    };

    LineString linestr2d;
    LineString linestr3d;
    LineString linestr2dm;
    LineString linestr3dm;
    LineString emptyLine;
    LineString simpleClosed;
    LineString nonSimpleClosed;
    LineString line2d;

    @Before
    public void setUp(){
        linestr2d = create(coordinates, false, false);
        linestr3d = create(coordinates, true, false);
        linestr2dm = create(coordinates, false, true);
        linestr3dm = create(coordinates, true, true);
        emptyLine = LineString.create(EmptyPointSequence.INSTANCE, 0);
        PointSequence closedSeq = new PackedPointSequence(new double[]{0,0,0,1,1,0,1,0,0,0,0,0}, DimensionalFlag.XYZ);
        simpleClosed = LineString.create(closedSeq, 0);
        nonSimpleClosed = LineString.create(createNonSimpleClosedPointSequence(),0);
        PointSequence lineSeq = new PackedPointSequence(new double[]{0.0, 0.0, 1.0, 1.0}, DimensionalFlag.XY);
        line2d = LineString.create(lineSeq, -1);

    }

    @Test
    public void testNumPoints(){
        assertEquals(3, linestr2d.getNumPoints());
        assertEquals(3, linestr3d.getNumPoints());
        assertEquals(3, linestr2dm.getNumPoints());
        assertEquals(3, linestr3dm.getNumPoints());
        assertEquals(0, emptyLine.getNumPoints());
    }

    @Test
    public void testPointN(){
        Assert.assertEquals(Point.create(new double[]{0, 0}, DimensionalFlag.XY, -1), linestr2d.getPointN(0));
        assertEquals(Point.create(new double[]{0, 0, 0}, DimensionalFlag.XYZ, -1) , linestr3d.getPointN(0));
        assertEquals(Point.create(new double[]{0, 0, 0, 0}, DimensionalFlag.XYZM, -1) , linestr3dm.getPointN(0));

        assertEquals(Point.create(new double[]{2, -2}, DimensionalFlag.XY, -1) , linestr2d.getPointN(2));
        assertEquals(Point.create(new double[]{2, -2, 3}, DimensionalFlag.XYZ, -1) , linestr3d.getPointN(2));
        assertEquals(Point.create(new double[]{2, -2, 3, 4}, DimensionalFlag.XYZM, -1) , linestr3dm.getPointN(2));

        try{
            linestr3dm.getPointN(3);
            fail();
        }catch(IndexOutOfBoundsException e){}

    }


    @Test
    public void testLength(){
        //empty line is of getLength 0
        assertEquals(0, emptyLine.getLength(), 10E-6);

        assertEquals(2*Math.sqrt(2), linestr2d.getLength(), 10E-6);
        //getLength calculation only takes into account X/Y coordinates
        //i.e. getLength calculated in 2D-plane
        assertEquals(2*Math.sqrt(2), linestr3d.getLength(), 10E-6);
        assertEquals(2*Math.sqrt(2), linestr2dm.getLength(), 10E-6);

    }

    @Test
    public void testStartPoint(){
        assertEquals(Point.createEmpty(), emptyLine.getStartPoint());

        assertEquals(Point.create(new PackedPointSequence(new double[]{0, 0}, DimensionalFlag.XY), -1), linestr2d.getStartPoint());
        assertEquals(Point.create(new PackedPointSequence(new double[]{0, 0, 0}, DimensionalFlag.XYZ), -1), linestr3d.getStartPoint());
        assertEquals(Point.create(new PackedPointSequence(new double[]{0, 0, 0, 0}, DimensionalFlag.XYZM), -1), linestr3dm.getStartPoint());
    }

    @Test
    public void testEndPoint(){
        assertEquals(Point.createEmpty(), emptyLine.getEndPoint());

        assertEquals(Point.create(new PackedPointSequence(new double[]{2, -2}, DimensionalFlag.XY), -1), linestr2d.getEndPoint());
        assertEquals(Point.create(new PackedPointSequence(new double[]{2, -2, 3}, DimensionalFlag.XYZ), -1), linestr3d.getEndPoint());
        assertEquals(Point.create(new PackedPointSequence(new double[]{2, -2, 3, 4}, DimensionalFlag.XYZM), -1), linestr3dm.getEndPoint());
    }

    @Test
    public void testIsClosed(){
        assertFalse(emptyLine.isClosed());
        assertFalse(linestr2d.isClosed());
        assertFalse(linestr3d.isClosed());
        assertTrue(simpleClosed.isClosed());
        assertTrue(nonSimpleClosed.isClosed());
    }

    @Test
    public void testIsSimple(){
        assertTrue(emptyLine.isSimple()) ;
        assertTrue(linestr2d.isSimple());
        assertTrue(linestr3d.isSimple());
        assertTrue(simpleClosed.isSimple());
        assertTrue(!nonSimpleClosed.isSimple());
    }

    @Test
    public void testIsRing(){
        assertFalse(emptyLine.isRing());
        assertFalse(linestr2d.isRing());
        assertFalse(nonSimpleClosed.isRing());
        assertTrue(simpleClosed.isRing());
    }

    @Test
    public void testGetDimension(){
        assertEquals(1, linestr2d.getDimension());
    }

    @Test
    public void testGetGeometryType() {
        assertEquals(GeometryType.LINE_STRING, linestr2d.getGeometryType());
        assertEquals(GeometryType.LINE_STRING, emptyLine.getGeometryType());
        assertEquals(GeometryType.LINE_STRING, simpleClosed.getGeometryType());
        assertEquals(GeometryType.LINE_STRING, line2d.getGeometryType());
    }

    @Test
    public void testGetBoundary() {
        assertEquals(MultiPoint.EMPTY, simpleClosed.getBoundary());
        assertEquals(MultiPoint.create(new Point[]{linestr2d.getStartPoint(), linestr2d.getEndPoint()}, -1), linestr2d.getBoundary());
    }

//    @Test
//    public void testLocateAlong() {
//        PointSequenceBuilder builder = new FixedSizePointSequenceBuilder(4, false, true);
//        builder.add(new double[]{0,0,0});
//        builder.add(new double[]{1,0,1});
//        builder.add(new double[]{2,0,2});
//        builder.add(new double[]{3,0,3});
//        LineString ls = LineString.create(builder.toPointSequence(), -1);
//        MultiLineString result = ls.locateBetween(0.5, 1.5);
//        assertFalse(result.isEmpty());
//        assertEquals(1, result.getNumGeometries());
//        LineString result1 = result.getGeometryN(0);
//        assertEquals(3, result1.getNumPoints());
//        assertEquals(Point.create(new double[]{0.5, 0, 0.5}, false, true, -1), result1.getStartPoint());
//        assertEquals(Point.create(new double[]{1.5, 0, 1.5}, false, true, -1), result1.getEndPoint());
//    }


    PointSequence createNonSimpleClosedPointSequence() {

        FixedSizePointSequenceBuilder builder = new FixedSizePointSequenceBuilder(5, DimensionalFlag.XY);
        double[] points = new double[2];
        points[0] = 1d;
        points[1] = 1d;
        builder.add(points);
        points[0] = -1d;
        points[1] = -1d;
        builder.add(points);
        builder.add2D(1, -1);
        builder.add2D(-1, 1);
        builder.add2D(1, 1);
        return builder.toPointSequence();
    }

    LineString create(double[] coordinates, boolean is3D, boolean isMeasured) {
        DimensionalFlag dimensionalFlag = DimensionalFlag.parse(is3D, isMeasured);
        FixedSizePointSequenceBuilder sequenceBuilder = new FixedSizePointSequenceBuilder(3, DimensionalFlag.parse(is3D, isMeasured));
        int dim = dimensionalFlag.getCoordinateDimension();
        double[] point = new double[dim];
        for (int i = 0; i < coordinates.length/4; i++){
            point[CoordinateSequence.X] = coordinates[i*4];
            point[CoordinateSequence.Y] = coordinates[i*4+1];
            if (is3D)
                point[CoordinateSequence.Z] = coordinates[i*4+2];
            if (isMeasured)
                point[is3D ? CoordinateSequence.M : CoordinateSequence.M -1] = coordinates[i*4+3];
            sequenceBuilder.add(point);
        }
        return LineString.create(sequenceBuilder.toPointSequence(),-1);
    }



}
