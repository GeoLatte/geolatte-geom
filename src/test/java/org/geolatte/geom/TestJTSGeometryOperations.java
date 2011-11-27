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
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/27/11
 */
public class TestJTSGeometryOperations {

    JTSGeometryOperations ops = new JTSGeometryOperations();

    @Test
    public void testCreateEnvelopeOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(4, DimensionalFlag.XY);
        psBuilder
                .add(-1, 3)
                .add(2, 5)
                .add(10, -8)
                .add(9, -1);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        assertEquals(new Envelope(-1, -8, 10, 5), ops.createEnvelopeOp(lineString).execute());
    }

    @Test
    public void testIntersectsOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(1, 1)
                .add(2, 2);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(3, 3)
                .add(4, 4);
        LineString lineString2 = LineString.create(psBuilder.toPointSequence(), null);

        assertFalse(ops.createIntersectsOp(lineString, lineString2).execute());

        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(2, 2)
                .add(4, 4);
        LineString lineString3 = LineString.create(psBuilder.toPointSequence(), null);

        assertTrue(ops.createIntersectsOp(lineString, lineString3).execute());


    }

    @Test
    public void testTouchesOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(1, 1)
                .add(2, 2);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(3, 3)
                .add(4, 4);
        LineString lineString2 = LineString.create(psBuilder.toPointSequence(), null);

        assertFalse(ops.createTouchesOp(lineString, lineString2).execute());

        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(2, 2)
                .add(4, 4);
        LineString lineString3 = LineString.create(psBuilder.toPointSequence(), null);

        assertTrue(ops.createTouchesOp(lineString, lineString3).execute());


    }

    @Test
    public void testCrossesOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(1, 1)
                .add(1, 3);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(3, 3)
                .add(4, 4);
        LineString lineString2 = LineString.create(psBuilder.toPointSequence(), null);

        assertFalse(ops.createCrossesOp(lineString, lineString2).execute());

        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(0, 2)
                .add(3, 2);
        LineString lineString3 = LineString.create(psBuilder.toPointSequence(), null);

        assertTrue(ops.createCrossesOp(lineString, lineString3).execute());


    }


    @Test
    public void testContainsOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(1, 1)
                .add(1, 2);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(0, 0)
                .add(0, 4)
                .add(4, 4)
                .add(4, 0)
                .add(0, 0);
        Polygon polygon = Polygon.create(psBuilder.toPointSequence(), null);
        assertFalse(ops.createContainsOp(lineString, polygon).execute());
        assertTrue(ops.createContainsOp(polygon, lineString).execute());
    }


    @Test
    public void testOverlapsOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(-1, -1)
                .add(-1, 2)
                .add(2, 2)
                .add(2, -1)
                .add(-1, -1);
        Polygon polygon = Polygon.create(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(0, 0)
                .add(0, 4)
                .add(4, 4)
                .add(4, 0)
                .add(0, 0);
        Polygon polygon2 = Polygon.create(psBuilder.toPointSequence(), null);
        assertTrue(ops.createOverlapsOp(polygon, polygon2).execute());
        assertTrue(ops.createOverlapsOp(polygon2, polygon).execute());

        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(10, 10)
                .add(10, 14)
                .add(14, 14)
                .add(14, 10)
                .add(10, 10);
        Polygon polygon3 = Polygon.create(psBuilder.toPointSequence(), null);
        assertFalse(ops.createOverlapsOp(polygon3, polygon2).execute());
        assertFalse(ops.createOverlapsOp(polygon2, polygon3).execute());
    }

    @Test
    public void testRelateOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(-1, -1)
                .add(-1, 2)
                .add(2, 2)
                .add(2, -1)
                .add(-1, -1);
        Polygon polygon = Polygon.create(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(0, 0)
                .add(0, 4)
                .add(4, 4)
                .add(4, 0)
                .add(0, 0);
        Polygon polygon2 = Polygon.create(psBuilder.toPointSequence(), null);
        assertTrue(ops.createRelateOp(polygon, polygon2, "T*T***T**").execute());
        assertTrue(ops.createRelateOp(polygon2, polygon, "T*T***T**").execute());

        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(10, 10)
                .add(10, 14)
                .add(14, 14)
                .add(14, 10)
                .add(10, 10);
        Polygon polygon3 = Polygon.create(psBuilder.toPointSequence(), null);
        assertFalse(ops.createRelateOp(polygon3, polygon2, "T*T***T**").execute());
        assertFalse(ops.createRelateOp(polygon2, polygon3, "T*T***T**").execute());
    }

    @Test
    public void testDistanceOp() {
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(1, 1)
                .add(1, 3);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(3, 1)
                .add(3, 4);
        LineString lineString2 = LineString.create(psBuilder.toPointSequence(), null);

        assertEquals(2, ops.createDistanceOp(lineString, lineString2).execute(), 0.00001d);

    }

    @Test
    public void testBufferOp(){
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(0,0)
                .add(1, 1);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        Geometry buffer = ops.createBufferOp(lineString, 10d).execute();
        assertTrue(buffer.contains(lineString));
        assertEquals(JTS.from(JTS.to(lineString).buffer(10d), CrsId.UNDEFINED), buffer);

    }


    @Test
    public void testConvexHullOp(){
        PointSequenceBuilder psBuilder = PointSequenceBuilderFactory.newVariableSizePointSequenceBuilder(DimensionalFlag.XY);
        psBuilder
                .add(0,0)
                .add(1, 1);
        LineString lineString = LineString.create(psBuilder.toPointSequence(), null);
        Geometry hull = ops.createConvexHullOp(lineString).execute();
        assertTrue(hull.contains(lineString));
        assertEquals(JTS.from(JTS.to(lineString).convexHull(), CrsId.UNDEFINED), hull);

    }



}
