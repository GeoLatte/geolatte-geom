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
 * An enumeration that corresponds to coordinate accessor methods.
 *
 * <p>The accessor names are used to associate a  <code>CoordinateSystemAxis</code> to a coordinate accessor method.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/30/11
 */
public enum CoordinateAccessor {
        X, Y, Z, M;

    //TODO -- clean out redundant code (see DimensionalFlag)
//    public int getIndex(DimensionalFlag flag) {
//        if (this == X) return 0;
//        if (this == Y) return 1;
//        if (this == Z)
//            return (flag.is3D() ? -1: 2);
//        if (this == M)
//            return flag.isMeasured() ?
//                     (flag.is3D() ? 3 : 2 ):
//                      -1;
//        return -1;
//        }
}
