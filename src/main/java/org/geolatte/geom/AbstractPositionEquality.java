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

import org.geolatte.geom.crs.CoordinateReferenceSystem;

abstract class AbstractPositionEquality implements PositionEquality {

    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends Position<P>> boolean equals(P first, P second) {
        if (first == second) return true;   //Also if first and second are null?
        // Or should null arguments trigger IllegalArgumentException
        if ((first == null || second == null)) return false;
        if (first.isEmpty() && second.isEmpty()) return true;
        if (first.isEmpty() || second.isEmpty()) return false;
        return equals(first.toArray(null), first.getCoordinateReferenceSystem(), second.toArray(null), second.getCoordinateReferenceSystem());
    }

    @Override
    public <P extends Position<P>> boolean equals2D(P first, P second) {
        if (first == second) return true;   //Also if first and second are null?
        // Or should null arguments trigger IllegalArgumentException
        return !(first == null || second == null) &&
                internalEquals(first.toArray(null),
                        first.getCoordinateReferenceSystem().getBaseCoordinateReferenceSystem(),
                        second.toArray(null),
                        second.getCoordinateReferenceSystem().getBaseCoordinateReferenceSystem());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends Position<P>> boolean equals(double[] first, CoordinateReferenceSystem<P> crs1, double[] second, CoordinateReferenceSystem<P> crs2) {
        return internalEquals(first, crs1, second, crs2);
    }

    private boolean internalEquals(double[] first, CoordinateReferenceSystem<?> crs1, double[] second, CoordinateReferenceSystem<?> crs2) {
        if (first == null || crs1 == null || second == null ||
                crs2 == null) throw new IllegalArgumentException("Null objects not allowed here.");

        if ((first.length > 0 && first.length < crs1.getCoordinateDimension())
                || (second.length > 0 && second.length < crs2.getCoordinateDimension())) {
            throw new IllegalArgumentException("Position arrays are inconsistent with passed coordinate systems.");
        }
        if (!crs1.equals(crs2)) return false;
        if (first.length == 0 && second.length == 0) return true;
        if (first.length == 0 || second.length == 0) return false;
        for (int i = 0; i < crs1.getCoordinateDimension(); i++) {
            if (!equals(first[i], second[i])) return false;
        }
        return true;
    }


    protected abstract boolean equals(double co1, double co2);


}