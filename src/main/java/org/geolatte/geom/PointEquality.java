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
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/13/12
 */
public interface PointEquality {

    /**
     * Tests whether the specified <code>Point</code>s are equal.
     *
     * @param first first <code>Point</code>
     * @param second second <code>Point</code>
     * @return true iff the first is equal to the second <code>Point</code>.
     */
    public boolean equals(Point first, Point second);

    /**
     * Tests whether the specified coordinates represent the same point.
     *
     * <p>The coordinates are assumed to be in the same Coordinate Reference System.</p>
     *
     * @param first the coordinates of the first <code>Point</code>
     * @param firstDF the <code>DimensionalFlag</code> for the first coordinate array
     * @param second the coordinates second <code>Point</code> to test
     * @param secondDF the <code>DimensionalFlag</code> for the second coordinate array
     * @return true iff the <code>Point</code>s represented by the coordinates are equal
     * @throws IllegalArgumentException when a NULL-argument is passed, or a coordinate array is
     * passed that is smaller than the coordinate dimensions of its corresponding dimensional flag.
     */
    public boolean equals(double[] first, DimensionalFlag firstDF, double[] second, DimensionalFlag secondDF);

}
