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
import org.geolatte.geom.crs.Unit;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/11
 */
public class LineSegmentsTest {

    private static CoordinateReferenceSystem<P2D> crs = CrsRegistry.getUndefinedProjectedCoordinateReferenceSystem();
    private static CoordinateReferenceSystem<P3D> crsZ = crs.addVerticalAxis(Unit.METER);
    private static CoordinateReferenceSystem<P2DM> crsM = crs.addMeasureAxis(Unit.METER);

    @Test
    public void testLineSegments() {
        PositionSequenceBuilder<P3D> builder = new FixedSizePositionSequenceBuilder<>(3, P3D.class);
        builder.add(1, 1, 1);
        builder.add(2, 2, 2);
        builder.add(3, 3, 3);
        PositionSequence<P3D> sequence = builder.toPositionSequence();
        int cnt = 0;
        double startX = 1.0d;
        for (LineSegment<P3D> ls : new LineSegments<>(sequence)) {
            assertEquals(startX, ls.getStartPosition().getX(), Math.ulp(1.0d));
            startX = ls.getEndPosition().getX();
            cnt++;
        }
        assertEquals(2, cnt);
    }

    @Test
    public void testLineSegmentsOnEmptyPointSequence() {
        PositionSequenceBuilder<P2D> builder = new FixedSizePositionSequenceBuilder<>(0, P2D.class);
        PositionSequence<P2D> sequence = builder.toPositionSequence();
        for (LineSegment<P2D> ls : new LineSegments<>(sequence)) {
            fail();
        }
    }


}
