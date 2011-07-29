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

package org.geolatte.geom.crs;

import org.geolatte.geom.CoordinateAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/2/11
 */
class XLatitudeEastingAccessorMapping extends AccessorToAxisMap {

    private final static Map<CoordinateAccessor, CoordinateSystemAxis[]> X_LAT_E = new HashMap<CoordinateAccessor, CoordinateSystemAxis[]>();

    static {

        X_LAT_E.put(CoordinateAccessor.X, new CoordinateSystemAxis[]{
                CoordinateSystemAxis.X,
                CoordinateSystemAxis.GEOCENTRIC_X,
                CoordinateSystemAxis.LATITUDE,
                CoordinateSystemAxis.E});

        X_LAT_E.put(CoordinateAccessor.Y, new CoordinateSystemAxis[]{
                CoordinateSystemAxis.Y,
                CoordinateSystemAxis.GEOCENTRIC_Y,
                CoordinateSystemAxis.LONGITUDE,
                CoordinateSystemAxis.N});

        X_LAT_E.put(CoordinateAccessor.Z, new CoordinateSystemAxis[]{
                CoordinateSystemAxis.Z,
                CoordinateSystemAxis.GEOCENTRIC_Z,
                CoordinateSystemAxis.ELLIPSOIDAL_HEIGTH,
                CoordinateSystemAxis.GEOCENTRIC_RADIUS});

        X_LAT_E.put(CoordinateAccessor.M, new CoordinateSystemAxis[]{
                CoordinateSystemAxis.M});

        X_LAT_E.put(CoordinateAccessor.Lat, new CoordinateSystemAxis[]{
                CoordinateSystemAxis.X,
                CoordinateSystemAxis.GEOCENTRIC_X,
                CoordinateSystemAxis.LATITUDE,
                CoordinateSystemAxis.E});

        X_LAT_E.put(CoordinateAccessor.Lat, new CoordinateSystemAxis[]{
                CoordinateSystemAxis.X,
                CoordinateSystemAxis.GEOCENTRIC_X,
                CoordinateSystemAxis.LATITUDE,
                CoordinateSystemAxis.E});

        X_LAT_E.put(CoordinateAccessor.Lon, new CoordinateSystemAxis[]{
                CoordinateSystemAxis.Y,
                CoordinateSystemAxis.GEOCENTRIC_Y,
                CoordinateSystemAxis.LONGITUDE,
                CoordinateSystemAxis.N});

        X_LAT_E.put(CoordinateAccessor.M, new CoordinateSystemAxis[]{CoordinateSystemAxis.M});
    }

    XLatitudeEastingAccessorMapping() {
        super(X_LAT_E);
    }
}
