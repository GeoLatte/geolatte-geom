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

import org.geolatte.geom.Units;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
public class CoordinateSystemAxis {


    //Common axes for ellipsoidal and spherical Coordinate Systems
    public static final CoordinateSystemAxis LONGITUDE = new CoordinateSystemAxis("longitude", "lon", CoordinateSystemAxisDirection.EAST, Units.DECIMAL_DEGREE);
    public static final CoordinateSystemAxis LATITUDE = new CoordinateSystemAxis("latitude", "lat", CoordinateSystemAxisDirection.NORTH, Units.DECIMAL_DEGREE);
    public static final CoordinateSystemAxis ELLIPSOIDAL_HEIGTH = new CoordinateSystemAxis("ellipsoidal height", "height", CoordinateSystemAxisDirection.UP, Units.METER);
    //TODO -- check the axis direction for radius.
    public static final CoordinateSystemAxis GEOCENTRIC_RADIUS = new CoordinateSystemAxis("geocentric radius", "radius", CoordinateSystemAxisDirection.GeocentricZ, Units.METER);

    public static final CoordinateSystemAxis GEOCENTRIC_X = new CoordinateSystemAxis("geocentric X", "X", CoordinateSystemAxisDirection.GeocentricX, Units.METER);
    public static final CoordinateSystemAxis GEOCENTRIC_Y = new CoordinateSystemAxis("geocentric Y", "Y", CoordinateSystemAxisDirection.GeocentricY, Units.METER);
    public static final CoordinateSystemAxis GEOCENTRIC_Z = new CoordinateSystemAxis("geocentric Z", "Z", CoordinateSystemAxisDirection.GeocentricZ, Units.METER);

    //Common axes for cartesian projected coordinate systems
    public static final CoordinateSystemAxis X = new CoordinateSystemAxis("X", "X", CoordinateSystemAxisDirection.EAST, Units.METER);
    public static final CoordinateSystemAxis Y = new CoordinateSystemAxis("Y", "Y", CoordinateSystemAxisDirection.NORTH, Units.METER);
    public static final CoordinateSystemAxis Z = new CoordinateSystemAxis("Z", "Z", CoordinateSystemAxisDirection.UP, Units.METER);
    public static final CoordinateSystemAxis M = new CoordinateSystemAxis("M", "M", CoordinateSystemAxisDirection.NOT_APPLICABLE, Units.INDETERMINATE);


    //Common axes for map projection Coordinate Systems
    public static final CoordinateSystemAxis N = new CoordinateSystemAxis("northing", "N", CoordinateSystemAxisDirection.NORTH, Units.METER);
    public static final CoordinateSystemAxis S = new CoordinateSystemAxis("southing", "S", CoordinateSystemAxisDirection.SOUTH, Units.METER);
    public static final CoordinateSystemAxis E = new CoordinateSystemAxis("easting", "E", CoordinateSystemAxisDirection.EAST, Units.METER);
    public static final CoordinateSystemAxis W = new CoordinateSystemAxis("westing", "W", CoordinateSystemAxisDirection.WEST, Units.METER);




    private final String axisName;
    private final String axisAbbreviation;
    private final CoordinateSystemAxisDirection coordinateSystemAxisDirection;
    private final Units units;

    public CoordinateSystemAxis(String axisName, String axisAbbreviation, CoordinateSystemAxisDirection coordinateSystemAxisDirection, Units units) {
        this.axisName = axisName;
        this.axisAbbreviation = axisAbbreviation;
        this.coordinateSystemAxisDirection = coordinateSystemAxisDirection;
        this.units = units;
    }

    public String getAxisName() {
        return axisName;
    }

    public String getAxisAbbreviation() {
        return axisAbbreviation;
    }

    public CoordinateSystemAxisDirection getAxisDirection() {
        return coordinateSystemAxisDirection;
    }

    public Units getUnits() {
        return units;
    }
}
