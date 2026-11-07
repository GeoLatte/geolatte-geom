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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 12/9/11
 */
public class LinearRingTest {

    private static CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    PositionSequence<C2D> validPoints = PositionSequenceBuilders.variableSized(crs.getPositionClass())
            .add(0, 0).add(10, 0).add(10, 10).add(0, 10).add(0, 0).toPositionSequence();

    PositionSequence<C2D> tooFewPoints = PositionSequenceBuilders.variableSized(crs.getPositionClass())
            .add(0, 0).add(10, 0).toPositionSequence();

    PositionSequence<C2D> notClosedPoints = PositionSequenceBuilders.variableSized(crs.getPositionClass())
            .add(0, 0).add(10, 0).add(10, 10).add(0, 10).toPositionSequence();


    @Test
    public void testValidLinearRing() {
        LinearRing<C2D> valid = new LinearRing<C2D>(validPoints, crs);
        assertNotNull(valid);
        assertFalse(valid.isEmpty());
        assertEquals(crs, valid.getCoordinateReferenceSystem());
    }


    @Test
    public void testLinearRingShouldHaveAtLeast4Points() {
        try {
            new LinearRing<C2D>(tooFewPoints, crs);
            fail("Non-empty linearRing should have at least 4 points.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLinearRingFromLineStringShouldHaveAtLeast4Points() {
        try {
            LineString<C2D> l = new LineString<C2D>(tooFewPoints, crs);
            new LinearRing<C2D>(l);
            fail("Non-empty linearRing should have at least 4 points.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClosedLinearRingWithThreePointsIsRejected() {
        PositionSequence<C2D> threePoints = PositionSequenceBuilders.variableSized(crs.getPositionClass())
                .add(0, 0).add(10, 0).add(0, 0).toPositionSequence();
        new LinearRing<C2D>(threePoints, crs);
    }

    @Test
    public void testLinearRingClosedInXYWithDifferentMeasuresIsAccepted() {
        CoordinateReferenceSystem<C2DM> crsM = CrsMock.crsM;
        LinearRing<C2DM> ring = DSL.ring(crsM, DSL.cM(0, 0, 0), DSL.cM(10, 0, 1), DSL.cM(10, 10, 2), DSL.cM(0, 0, 3));
        assertTrue(ring.isClosed());
        assertTrue(ring.isRing());
    }

    @Test
    public void testNonSimpleLinearRingIsAcceptedButIsNotARing() {
        LinearRing<C2D> bowTie = DSL.ring(crs, DSL.c(0, 0), DSL.c(10, 10), DSL.c(10, 0), DSL.c(0, 10), DSL.c(0, 0));
        assertFalse(bowTie.isRing());
    }

    @Test
    public void testLinearRingMustBeClosed() {
        try {
            new LinearRing<C2D>(notClosedPoints, crs);
            fail("Non-empty linearRing should be closed.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLinearRingFromLineStringMustBeClosed() {
        try {
            LineString<C2D> l = new LineString<C2D>(notClosedPoints, crs);
            new LinearRing<C2D>(l);
            fail("Non-empty linearRing should be closed.");
        } catch (IllegalArgumentException e) {
        }
    }

}
