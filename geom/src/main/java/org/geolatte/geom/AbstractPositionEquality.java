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

abstract class AbstractPositionEquality implements PositionEquality {

    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends Position> boolean equals(P first, P second) {
        if (first == second) return true;   //Also if first and second are null?
        // Or should null arguments trigger IllegalArgumentException
        if ((first == null || second == null)) return false;
        if (first.isEmpty() && second.isEmpty()) return true;
        if (first.isEmpty() || second.isEmpty()) return false;
        return equals(first.toArray(null), second.toArray(null));
    }

    @Override
    public <P extends Position> boolean equals2D(P first, P second) {
        if (first == second) return true;   //Also if first and second are null?
        // Or should null arguments trigger IllegalArgumentException
        return !(first == null || second == null) &&
                internalEquals(first.toArray(null),
                        2,
                        second.toArray(null),
                        2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends Position> boolean equals(double[] first, double[] second) {
        return internalEquals(first, first.length, second, second.length);
    }

    private boolean internalEquals(double[] first, int l1, double[] second, int l2) {
        if (first == null || second == null)
            throw new IllegalArgumentException("Null objects not allowed here.");

        if ((first.length > 0 && first.length < l1)
                || (second.length > 0 && second.length < l2)) {
            throw new IllegalArgumentException("Position arrays are inconsistent with passed coordinate systems.");
        }
        if (l1 != l2) return false;
        if (first.length == 0 && second.length == 0) return true;
        if (first.length == 0 || second.length == 0) return false;
        for (int i = 0; i < l1; i++) {
            if (!equals(first[i], second[i])) return false;
        }
        return true;
    }


    protected abstract boolean equals(double co1, double co2);


}