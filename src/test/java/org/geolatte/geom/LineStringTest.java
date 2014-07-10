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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class LineStringTest {

    private static CoordinateReferenceSystem<P2D> crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
    private static CoordinateReferenceSystem<P3D> crsZ = crs.addVerticalAxis(LengthUnit.METER);
    private static CoordinateReferenceSystem<P2DM> crsM = crs.addMeasureAxis(LengthUnit.METER);
    private static CoordinateReferenceSystem<P3DM> crsZM = crsZ.addMeasureAxis(LengthUnit.METER);

    double[] coordinates = new double[]{
            0, 0, 0, 0,
            1, -1, 1, 2,
            2, -2, 3, 4
    };

    LineString<P2D> linestr2d;
    LineString<P3D> linestr3d;
    LineString<P2DM> linestr2dm;
    LineString<P3DM> linestr3dm;
    LineString<P2D> emptyLine;
    LineString<P2D> simpleClosed;
    LineString<P2D> line2d;

    @Before
    public void setUp() {
        linestr2d = linestring(crs, p(0, 0), p(1, -1), p(2, -2));
        linestr3d = linestring(crsZ, p(0, 0, 0), p(1, -1, 1), p(2, -2, 3));
        linestr2dm = linestring(crsM, pM(0, 0, 0), pM(1, -1, 2), pM(2, -2, 4));
        linestr3dm = linestring(crsZM, p(0, 0, 0, 0), p(1, -1, 1, 2), p(2, -2, 3, 4));
        emptyLine = linestring(crs);

        simpleClosed = linestring(crs, p(0, 0), p(0, 1), p(1, 1), p(1, 0), p(0, 0));
//        nonSimpleClosed = linestring(crs, p(1, 1), p(-1, -1), p(1, -1), p(-1, 1), p(1,1));
        line2d = linestring(crs, p(0, 0), p(1, 1));

    }

    @Test
    public void testNumPoints() {
        assertEquals(3, linestr2d.getNumPositions());
        assertEquals(3, linestr3d.getNumPositions());
        assertEquals(3, linestr2dm.getNumPositions());
        assertEquals(3, linestr3dm.getNumPositions());
        assertEquals(0, emptyLine.getNumPositions());
    }

    @Test
    public void testPointN() {
        Assert.assertEquals(new P2D(0, 0), linestr2d.getPositionN(0));
        assertEquals(new P3D(0, 0, 0), linestr3d.getPositionN(0));
        assertEquals(new P3DM(0, 0, 0, 0), linestr3dm.getPositionN(0));

        assertEquals(new P2D(2, -2), linestr2d.getPositionN(2));
        assertEquals(new P3D(2, -2, 3), linestr3d.getPositionN(2));
        assertEquals(new P3DM(2, -2, 3, 4), linestr3dm.getPositionN(2));

        try {
            linestr3dm.getPositionN(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

    }

    @Test
    public void testStartPoint() {
        assertEquals(new P2D(0, 0), linestr2d.getStartPosition());
        assertEquals(new P3D(0, 0, 0), linestr3d.getStartPosition());
        assertEquals(new P3DM(0, 0, 0, 0), linestr3dm.getStartPosition());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testStartPositionOnEmptyThrowsException() {
        emptyLine.getStartPosition();
    }

    @Test
    public void testEndPoint() {
        assertEquals(new P2D(2, -2), linestr2d.getEndPosition());
        assertEquals(new P3D(2, -2, 3), linestr3d.getEndPosition());
        assertEquals(new P3DM(2, -2, 3, 4), linestr3dm.getEndPosition());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testEndPositionOnEmptyThrowsException() {
        emptyLine.getEndPosition();
    }


    @Test
    public void testIsClosed() {
        assertFalse(emptyLine.isClosed());
        assertFalse(linestr2d.isClosed());
        assertFalse(linestr3d.isClosed());
        assertTrue(simpleClosed.isClosed());
    }


    @Test
    public void testIsRing() {
        assertFalse(emptyLine.isRing());
        assertFalse(linestr2d.isRing());
        assertTrue(simpleClosed.isRing());
    }

    @Test
    public void testGetDimension() {
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
       public void testCreateEnvelopeOp() {
           LineString<P2D> lineString = linestring(crs, p(-1, 3), p(2, 5), p(10, -8), p(9, -1));
           assertEquals(new Envelope<>(-1, -8, 10, 5, crs), lineString.getEnvelope());
       }

       @Test
       public void testCreateEnvelopeOpOnlyNegative() {
           LineString<P2D> lineString = linestring(crs, p(-101, -97), p(-98, -95), p(-90, -108), p(-91, -101));
           assertEquals(new Envelope<>(-101, -108, -90, -95, crs), lineString.getEnvelope());
       }

       @Test
       public void testCreateEnvelopeOpOnlyPositive() {
           LineString<P2D> lineString = linestring(crs, p(99, 103), p(102, 105), p(110, 92), p(109, 99));
           assertEquals(new Envelope<>(99, 92, 110, 105, crs), lineString.getEnvelope());
       }

       @Test
       public void testCreateEnvelopeOpOnEmpty() {
           LineString<P2D> lineString = new LineString<>(crs);
           assertEquals(new Envelope<>(Double.NaN, Double.NaN, Double.NaN, Double.NaN, crs), lineString.getEnvelope());
       }

}
