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

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/11
 */
abstract class AbstractPointSequenceBuilder implements PointSequenceBuilder {

    protected final DimensionalFlag dimensionalFlag;

    public AbstractPointSequenceBuilder(DimensionalFlag dimensionalFlag) {
        this.dimensionalFlag = dimensionalFlag;
    }

    @Override
    public PointSequenceBuilder add(double[] coordinates) {
        if (coordinates.length < dimensionalFlag.getCoordinateDimension())
            throw new IllegalArgumentException(String.format("Parameter must be array of getLength %d", dimensionalFlag.getCoordinateDimension()));
        for (int i = 0; i < dimensionalFlag.getCoordinateDimension(); i++) {
            add(coordinates[i]);
        }
        return this;
    }

    @Override
    public PointSequenceBuilder add2D(double x, double y) {
        if (dimensionalFlag != DimensionalFlag.XY)
            throw new IllegalStateException("Attempting to add 2D point to pointsequence of dimension " + dimensionalFlag);
        add(x);
        add(y);
        return this;
    }

    @Override
    public PointSequenceBuilder add3D(double x, double y, double z) {
        if (dimensionalFlag != DimensionalFlag.XYZ)
            throw new IllegalStateException("Attempting to add 3D point to pointsequence of dimension " + dimensionalFlag);
        add(x);
        add(y);
        add(z);
        return this;
    }

    @Override
    public PointSequenceBuilder add2DM(double x, double y, double m) {
        if (dimensionalFlag != DimensionalFlag.XYM)
            throw new IllegalStateException("Attempting to add 3D point to pointsequence of dimension " + dimensionalFlag);
        add(x);
        add(y);
        add(m);
        return this;
    }

    @Override
    public PointSequenceBuilder add3DM(double x, double y, double z, double m) {
        if (dimensionalFlag != DimensionalFlag.XYZM)
            throw new IllegalStateException("Attempting to add 3D point to pointsequence of dimension " + dimensionalFlag);
        add(x);
        add(y);
        add(z);
        add(m);
        return this;
    }

    protected abstract void add(double val);
}
