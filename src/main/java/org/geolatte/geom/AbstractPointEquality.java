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
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

abstract class AbstractPointEquality implements PointEquality {

    final private DimensionalFlag dimensionalFlag;

    public AbstractPointEquality(DimensionalFlag dimensionalFlag) {
        this.dimensionalFlag = dimensionalFlag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Point first, Point second) {
        if (first == second) return true;   //Also if first and second are null?
                                            // Or should null arguments trigger IllegalArgumentException
        if ((first == null || second == null)) return false;
        if (first.isEmpty() && second.isEmpty()) return true;
        if (first.isEmpty() || second.isEmpty()) return false;
        if (!first.getCrsId().equals(second.getCrsId())) return false;
        if ( !equals(first.getX(), second.getX()) ) return false;
        if ( !equals(first.getY(), second.getY()) ) return false;

        if (dimensionalFlag.is3D()
                && (first.is3D() || second.is3D())
                && !equals(first.getZ(), second.getZ())) return false;

        if (dimensionalFlag.isMeasured()
                && (first.isMeasured() || second.isMeasured())
                && !equals(first.getM(), second.getM())) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(double[] first, DimensionalFlag df1, double[] second, DimensionalFlag df2) {
        if (first == null || df1 == null || second == null ||
                df2 == null ) throw new IllegalArgumentException("Null objects not allowed here.");

        if (first.length < df1.getCoordinateDimension()
                || second.length < df2.getCoordinateDimension()) {
            throw new IllegalArgumentException("Coordinate arrays are inconsistent with passed dimensional flags.");
        }
        if ( !equals(first[0], second[0]) ) return false;
        if ( !equals(first[1], second[1]) ) return false;
        if (dimensionalFlag.is3D()
                && (df1.is3D() || df2.is3D())
                && !equals(get(first, df1.Z), get(second, df2.Z))) return false;


        if (dimensionalFlag.isMeasured()
                && (df1.isMeasured() || df2.isMeasured())
                && !equals(get(first, df1.M), get(second, df2.M))) return false;

        return true;
    }

    protected double get(double[] coordinates, int index) {
        if (index == -1 || index >= coordinates.length) return Double.NaN;
        return coordinates[index];
    }

    protected DimensionalFlag getDimensionalFlag() {
        return this.dimensionalFlag;
    }

    protected abstract boolean equals(double co1, double co2);


}