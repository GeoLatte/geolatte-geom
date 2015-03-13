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

/**
 * A {@link PositionEquality} that considers <code>Point</code>s to be equal when their coordinates
 * are within a specified tolerance of each other.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/12
 */
public class WithinTolerancePositionEquality extends AbstractPositionEquality {

    private final double tolerance;

    /**
     * Constructs an instance
     * @param tolerance the tolerance within which coordinates are considered equal
     */
    public WithinTolerancePositionEquality(double tolerance) {
        this.tolerance = tolerance;
    }

     /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equals(double co1, double co2) {
        return Math.abs(co1-co2) <= tolerance;
    }

}
