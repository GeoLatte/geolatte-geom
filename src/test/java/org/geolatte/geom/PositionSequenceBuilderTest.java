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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 12/9/11
 */
public class PositionSequenceBuilderTest {

    @Test
    public void testMethodInvocationInconsistentWithDimensionThrowsIllegalStateException() {
        PositionSequenceBuilder builder = PositionSequenceBuilders.fixedSized(2, C2D.class);
        builder.add(1.0, 1.0);
        try {
            builder.add(1.0, 1.0, 1.0);
            fail("Adding 3D point to 2D PointSequence should throw IllegalStateException");
        }catch(IllegalArgumentException e){
        }

    }

    @Test
    public void testAdding3DOr3DMCoordinates() {
        PositionSequenceBuilder builder = PositionSequenceBuilders.fixedSized(2, C3D.class);
        builder.add(1.0, 1.0, 1.0);
        try {
            builder.add(1.0, 1.0);
            fail("Adding 2D point to 3D PointSequence should throw IllegalStateException");
        }catch(IllegalArgumentException e){
        }
        builder = PositionSequenceBuilders.fixedSized(2, C2DM.class);
        builder.add(1.0, 1.0, 1.0);
        try {
            builder.add(1.0, 1.0);
            fail("Adding 2D point to 3D PointSequence should throw IllegalStateException");
        }catch(IllegalArgumentException e){
        }

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFixedSizePSBuilderThrowsIndexOutOfBoundsExcpIfFilledBeyondCapacity() {
        PositionSequenceBuilder builder = PositionSequenceBuilders.fixedSized(2, C2D.class);
        builder.add(2, 3);
        builder.add(1, 2);
        builder.add(4, 4);
        builder.toPositionSequence();
    }

}
