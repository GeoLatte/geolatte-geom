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
 * A Test for <code>Position</code> equality.
 *
 * <p>Implementations must be thread-safe. </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/13/12
 */
public interface PositionEquality {

    /**
     * Tests whether the specified <code>Coordinates</code>s are equal.
     *
     * @param first first <code>Point</code>
     * @param second second <code>Point</code>
     * @return true iff the first is equal to the second <code>Positoin</code>.
     */
    public <P extends Position> boolean equals(P first, P second);

    /**
     * Tests whether the specified <code>Coordinates</code>s are equal in the base 2D-CoordinateReferenceSystem.
     * @param first first <code>Point</code>
     * @param second second <code>Point</code>
     * @return true iff the first is equal to the second <code>Position</code>.
     */
    public <P extends Position> boolean equals2D(P first, P second);


    /**
     * Tests whether the specified coordinates represent the same point.
     *
     *
     * @param first the first coordinate array
     * @param second the second coordinate array
     * @return true iff the <code>coordinates</code>s are equal
     * @throws IllegalArgumentException when a NULL-argument is passed, or a coordinate array is
     * passed that is smaller than the coordinate dimensions of its corresponding Position Reference System.
     */
    public <P extends Position> boolean equals(double[] first, double[] second);

}
