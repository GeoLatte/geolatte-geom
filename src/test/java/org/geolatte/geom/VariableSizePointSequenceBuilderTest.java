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
/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/11
 */
public class VariableSizePointSequenceBuilderTest {


    @Test
    public void test() throws Exception {
        PositionSequenceBuilder<C2D> builder = new VariableSizePositionSequenceBuilder<C2D>(C2D.class);

        for (int i = 0; i < 100; i++){
            builder.add(getRandomPoint());
        }

        PositionSequence<C2D> sequence = builder.toPositionSequence();
        assertEquals(100, sequence.size());

    }

    @Test
    public void testNumPointsLessThanInitialCapacity() throws Exception {
        PositionSequenceBuilder<C2D> builder = new VariableSizePositionSequenceBuilder<C2D>(C2D.class);

        for (int i = 0; i < 4; i++){
            builder.add(getRandomPoint());
        }

        PositionSequence<C2D> sequence = builder.toPositionSequence();
        assertEquals(4, sequence.size());

    }

    public double[] getRandomPoint(){
          return new double[]{Math.random(), Math.random()};
    }
}
