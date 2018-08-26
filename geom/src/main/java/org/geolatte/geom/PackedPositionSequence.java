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

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PackedPositionSequence<P extends Position> extends AbstractPositionSequence<P> {

    private final double[] coordinates;


    PackedPositionSequence(PositionFactory<P> factory, double[] coordinates) {
        super(factory);
        if (coordinates == null) {
            this.coordinates = new double[0];
        } else {
            this.coordinates = coordinates;
        }
        if ((this.coordinates.length % getCoordinateDimension()) != 0)
            throw new IllegalArgumentException(String.format("coordinate array size should be a multiple of %d. Current size = %d", getCoordinateDimension(), this.coordinates.length));
    }

    @Override
    public boolean isEmpty() {
        return this.coordinates.length == 0;
    }

    @Override
    public void accept(PositionVisitor<P> visitor) {
        for (P p : this) {
            visitor.visit(p);
        }
    }

    @Override
    public void accept(LLAPositionVisitor visitor) {
        double[] buffer = new double[getCoordinateDimension()];
        for (int i = 0; i < size(); i++) {
            getCoordinates(i, buffer);
            visitor.visit(buffer);
        }
    }

    /**
     * Creates a new <code>PositionSequence</code> with positions in reverse order.
     *
     * @return
     */
    @Override
    public PositionSequence<P> reverse() {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(size(), getPositionClass());
        double[] coords = new double[getCoordinateDimension()];
        for (int i = size() - 1; i >= 0 ; i--) {
            getCoordinates(i, coords);
            builder.add(coords);
        }
        return builder.toPositionSequence();
    }

    @Override
    public int size() {
        return this.coordinates.length / getCoordinateDimension();
    }


    @Override
    public Iterator<P> iterator() {
        return new PositionSequenceIterator<P>(this);
    }

    /** @deprecated in {@link org.locationtech.jts.geom.CoordinateSequence }.*/
    @Deprecated
    @Override
    public PositionSequence<P> clone() {
        return new PackedPositionSequence<>(getPositionFactory(), Arrays.copyOf(this.coordinates, this.coordinates.length));
    }

    @Override
    public PackedPositionSequence<P> copy() {
        return new PackedPositionSequence<>(getPositionFactory(), Arrays.copyOf(this.coordinates, this.coordinates.length));
    }

    @Override
    public void setOrdinate(int index, int ordinateIndex, double value) {
        int dim = getCoordinateDimension();
        this.coordinates[index * dim + ordinateIndex] = value;
    }

    @Override
    public void getCoordinates(int position, double[] coordinates) {
        int dim = getCoordinateDimension();
        if(coordinates.length < dim) {
            throw new IllegalArgumentException("Position argument must be an array of size at least " + dim);
        }
        for(int i = 0; i < getCoordinateDimension(); i++) {
            coordinates[i] = this.coordinates[position * dim + i];
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o.getClass().equals(this.getClass()))) return false;

        PackedPositionSequence that = (PackedPositionSequence) o;

        if (!getPositionClass().equals(that.getPositionClass())) return false;
        return new PositionSequencePositionEquality().equals(this, that);
    }


    @Override
    public int hashCode() {
        int result = Arrays.hashCode(coordinates);
        result = 31 * result + getPositionClass().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < size(); i++) {
            if (i > 0) builder.append(" ,");
            addCoordinate(i, builder);
        }
        builder.append("]");
        return builder.toString();
    }

    private void addCoordinate(int index, StringBuilder builder) {
        double[] c = new double[getCoordinateDimension()];
        this.getCoordinates(index, c);
        for (int i = 0; i < getCoordinateDimension(); i ++) {
            builder.append(c[i])
                    .append(" ");
        }
    }



}
