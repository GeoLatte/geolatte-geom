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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An <code>Iterator</code> over the <code>Position</code>s of a <code>PositionSequence</code>.
 *
 * <p>This implementation does not support the remove operation.</p>
 * <p>This implementation is not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PositionSequenceIterator<P extends Position> implements Iterator<P> {

    private final PositionSequence<P> sequence;
    private int index = 0;

    PositionSequenceIterator(PositionSequence<P> sequence){
        if (sequence == null) {
            throw new IllegalArgumentException("Null sequence argument");
        }
        this.sequence = sequence;
    }

    @Override
    public boolean hasNext() {
        return index < this.sequence.size();
    }

    @Override
    public P next() {
        double[] coordinates = new double[sequence.getCoordinateDimension()];
        if (hasNext()){
            sequence.getCoordinates(index++, coordinates);
            return Positions.mkPosition(sequence.getPositionClass(), coordinates);
        }
        throw new NoSuchElementException();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException when invoked.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove method not supported on " + getClass().getName());
    }
}
