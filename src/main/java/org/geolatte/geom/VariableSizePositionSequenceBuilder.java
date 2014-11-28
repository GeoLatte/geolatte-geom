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

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/11
 */
class VariableSizePositionSequenceBuilder<P extends Position> extends AbstractPositionSequenceBuilder<P> {

    private double[] coordinates;
    private int index = 0;

    VariableSizePositionSequenceBuilder(Class<P> clazz) {
        this(Positions.getFactoryFor(clazz));
    }

    VariableSizePositionSequenceBuilder(PositionFactory<P> descriptor) {
        super(descriptor);
        this.coordinates = new double[descriptor.getCoordinateDimension() * 10];
    }

    @Override
    protected void addCoordinate(double val) {
        ensureCapacity();
        coordinates[index++] = val;
    }

    private void ensureCapacity() {
        if (index < this.coordinates.length) return;
        int newCapacity = (this.coordinates.length * 3) / 2 + 1;
        this.coordinates = Arrays.copyOf(this.coordinates, newCapacity);
    }

    @Override
    public PositionSequence<P> toPositionSequence() {
        return new PackedPositionSequence<P>(factory, Arrays.copyOf(coordinates, index));
    }
}
