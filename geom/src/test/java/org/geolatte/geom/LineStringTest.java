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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.junit.Assert.*;
/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class LineStringTest {


    double[] coordinates = new double[]{
            0, 0, 0, 0,
            1, -1, 1, 2,
            2, -2, 3, 4
    };

    LineString<C2D> linestr2d;
    LineString<C3D> linestr3d;
    LineString<C2DM> linestr2dm;
    LineString<C3DM> linestr3dm;
    LineString<C2D> emptyLine;
    LineString<C2D> simpleClosed;
    LineString<C2D> line2d;

    @Before
    public void setUp() {
        linestr2d = linestring(crs, c(0, 0), c(1, -1), c(2, -2));
        linestr3d = linestring(crsZ, c(0, 0, 0), c(1, -1, 1), c(2, -2, 3));
        linestr2dm = linestring(crsM, cM(0, 0, 0), cM(1, -1, 2), cM(2, -2, 4));
        linestr3dm = linestring(crsZM, c(0, 0, 0, 0), c(1, -1, 1, 2), c(2, -2, 3, 4));
        emptyLine = linestring(crs);

        simpleClosed = linestring(crs, c(0, 0), c(0, 1), c(1, 1), c(1, 0), c(0, 0));
//        nonSimpleClosed = linestring(crs, p(1, 1), p(-1, -1), p(1, -1), p(-1, 1), p(1,1));
        line2d = linestring(crs, c(0, 0), c(1, 1));

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
        Assert.assertEquals(new C2D(0, 0), linestr2d.getPositionN(0));
        assertEquals(new C3D(0, 0, 0), linestr3d.getPositionN(0));
        assertEquals(new C3DM(0, 0, 0, 0), linestr3dm.getPositionN(0));

        assertEquals(new C2D(2, -2), linestr2d.getPositionN(2));
        assertEquals(new C3D(2, -2, 3), linestr3d.getPositionN(2));
        assertEquals(new C3DM(2, -2, 3, 4), linestr3dm.getPositionN(2));

        try {
            linestr3dm.getPositionN(3);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

    }

    @Test
    public void testStartPoint() {
        assertEquals(new C2D(0, 0), linestr2d.getStartPosition());
        assertEquals(new C3D(0, 0, 0), linestr3d.getStartPosition());
        assertEquals(new C3DM(0, 0, 0, 0), linestr3dm.getStartPosition());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testStartPositionOnEmptyThrowsException() {
        emptyLine.getStartPosition();
    }

    @Test
    public void testEndPoint() {
        assertEquals(new C2D(2, -2), linestr2d.getEndPosition());
        assertEquals(new C3D(2, -2, 3), linestr3d.getEndPosition());
        assertEquals(new C3DM(2, -2, 3, 4), linestr3dm.getEndPosition());
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
        assertEquals(GeometryType.LINESTRING, linestr2d.getGeometryType());
        assertEquals(GeometryType.LINESTRING, emptyLine.getGeometryType());
        assertEquals(GeometryType.LINESTRING, simpleClosed.getGeometryType());
        assertEquals(GeometryType.LINESTRING, line2d.getGeometryType());
    }

    @Test
       public void testCreateEnvelopeOp() {
           LineString<C2D> lineString = linestring(crs, c(-1, 3), c(2, 5), c(10, -8), c(9, -1));
           assertEquals(new Envelope<C2D>(-1, -8, 10, 5, crs), lineString.getEnvelope());
       }

       @Test
       public void testCreateEnvelopeOpOnlyNegative() {
           LineString<C2D> lineString = linestring(crs, c(-101, -97), c(-98, -95), c(-90, -108), c(-91, -101));
           assertEquals(new Envelope<C2D>(-101, -108, -90, -95, crs), lineString.getEnvelope());
       }

       @Test
       public void testCreateEnvelopeOpOnlyPositive() {
           LineString<C2D> lineString = linestring(crs, c(99, 103), c(102, 105), c(110, 92), c(109, 99));
           assertEquals(new Envelope<C2D>(99, 92, 110, 105, crs), lineString.getEnvelope());
       }

       @Test
       public void testCreateEnvelopeOpOnEmpty() {
           LineString<C2D> lineString = new LineString<C2D>(crs);
           assertEquals(new Envelope<C2D>(Double.NaN, Double.NaN, Double.NaN, Double.NaN, crs), lineString.getEnvelope());
       }

}
