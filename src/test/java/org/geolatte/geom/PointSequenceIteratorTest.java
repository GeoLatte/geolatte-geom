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

import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PointSequenceIteratorTest {

    PointSequence sequence = new PackedPointSequence(new double[]{0,0, 1, 2, 3, 4}, DimensionalFlag.XY);
    PointSequence emptySequence = EmptyPointSequence.INSTANCE;

    PointSequenceIterator itSeq;
    PointSequenceIterator itEmpty;


    @Before
    public void setUp() throws Exception {
        itSeq = new PointSequenceIterator(sequence);
        itEmpty = new PointSequenceIterator(emptySequence);
    }

    @Test
    public void testNullArgumentInConstructor(){
        PointSequenceIterator it = new PointSequenceIterator(null);
        assertFalse(it.hasNext());
        try {
            it.next();
            fail();
        }catch(NoSuchElementException e){}
    }

    @Test
    public void test_iteration() throws Exception {
        int i = 0;
        while (itSeq.hasNext()){
            Point received = itSeq.next();
            assertEquals(sequence.getX(i), received.getX(), Math.ulp(10d));
            assertEquals(sequence.getY(i), received.getY(), Math.ulp(10d));
            i++;
        }
        assertEquals(3, i);

    }

    @Test
    public void test_iteration_over_empty_sequence() throws Exception {
        int i = 0;
        while (itEmpty.hasNext()){
            itEmpty.next();
            i++;
        }
        assertEquals(0, i);

    }

    @Test
    public void testRemove() throws Exception {
        try {
            itSeq.remove();
            fail();
        } catch (UnsupportedOperationException e){}
    }
}
