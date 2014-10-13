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
 * A test for equality of <code>Geometry</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/13/12
 */
public interface GeometryEquality {

    /**
     * Checks whether the first geometry equals
     * the second, for some definition of equality.
     *
     * @param first a <code>Geometry</code>
     * @param second a second <code>Geometry</code>
     * @return true if and only if both both <code>Geometry</code>s are equal.
     */
    public <P extends Position> boolean equals(Geometry<P> first, Geometry<P> second);

}
