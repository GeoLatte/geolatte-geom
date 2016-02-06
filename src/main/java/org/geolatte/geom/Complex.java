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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

/**
 * Interface for {@code Geometry}s that are composed of {@code Geometry}s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/16/14
 */
public interface Complex<P extends Position, G extends Geometry<P>> extends Iterable<G> {

    /**
     * Returns the number of constituent {@code Geometry}s
     *
     * @return the number of constituent {@code Geometry}s
     */
    public int getNumGeometries();

    /**
     * Returns the {@code Class} of which all constituent {@code Geometry}s are instances.
     *
     * @return the {@code Class} of which all constituent {@code Geometry}s are instances.
     */
    public Class<? extends Geometry> getComponentType();

    /**
     * Returns the components
     * @return an array containing all component objects
     *
     */
    public G[] components();

}
