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
 * A {@link PointEquality} implementation that considers two <code>Point</code>s to be equal if and only if
 * the have the same coordinates. Optionally, the coordinates considered in the test can be limited to specified
 * dimensions.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/13/12
 */
public class ExactCoordinatePointEquality extends AbstractPointEquality {

    /**
     * Creates an instance that takes only the coordinates into account specified in the dimensionalFlag.
     *
     * @param dimensionalFlag specifies the dimensions that are considered in the equality test
     */
    public ExactCoordinatePointEquality(DimensionalFlag dimensionalFlag) {
        super(dimensionalFlag);
    }

    /**
     * Creates an instance that takes all coordinate values into account.
     */
    public ExactCoordinatePointEquality(){
        super(DimensionalFlag.XYZM);
    }

    @Override
    protected boolean equals(double co1, double co2) {
        return co1 == co2;
    }


}
