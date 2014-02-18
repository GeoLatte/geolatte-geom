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

/**
 * A PointSequenceBuilder for <code>PointSequence</code>s with known size.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class FixedSizePointSequenceBuilder extends AbstractPointSequenceBuilder {

    private int index = 0;
    private final double[] coordinates;

    FixedSizePointSequenceBuilder(int capacity, DimensionalFlag flag, CrsId crsId) {
        super(flag, crsId);
        this.coordinates = new double[capacity * flag.getCoordinateDimension()];
    }

    protected void add(double x) {
        this.coordinates[index++] = x;
    }

    @Override
    public PointSequence toPointSequence() {
        if( index != coordinates.length) {
            throw new IllegalStateException("PointSequence not filled to capacity.");
        }
        return new PackedPointSequence(this.coordinates, this.dimensionalFlag, this.crsId);
    }

}
