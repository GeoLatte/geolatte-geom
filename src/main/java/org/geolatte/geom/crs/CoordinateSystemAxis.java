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

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
public class CoordinateSystemAxis {


    //Common axes for ellipsoidal and spherical Coordinate Systems
    public static final CoordinateSystemAxis LONG = new CoordinateSystemAxis("Lon", CoordinateSystemAxisDirection.EAST, Unit.DEGREE);
    public static final CoordinateSystemAxis LAT = new CoordinateSystemAxis("Lat", CoordinateSystemAxisDirection.NORTH, Unit.DEGREE);
    public static final CoordinateSystemAxis ELLIPSOIDAL_HEIGTH = new CoordinateSystemAxis("ellipsoidal height", CoordinateSystemAxisDirection.UP, Unit.METER);
    //TODO -- check the axis direction for radius.
    public static final CoordinateSystemAxis GEOCENTRIC_RADIUS = new CoordinateSystemAxis("geocentric radius", CoordinateSystemAxisDirection.GeocentricZ, Unit.METER);

    public static final CoordinateSystemAxis GEOCENTRIC_X = new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.GeocentricX, Unit.METER);
    public static final CoordinateSystemAxis GEOCENTRIC_Y = new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.GeocentricY, Unit.METER);
    public static final CoordinateSystemAxis GEOCENTRIC_Z = new CoordinateSystemAxis("Z", CoordinateSystemAxisDirection.GeocentricZ, Unit.METER);

    //Common axes for cartesian projected coordinate systems
    public static final CoordinateSystemAxis X = new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.EAST, Unit.METER);
    public static final CoordinateSystemAxis Y = new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.NORTH, Unit.METER);
    public static final CoordinateSystemAxis Z = new CoordinateSystemAxis("Z", CoordinateSystemAxisDirection.UP, Unit.METER);
    public static final CoordinateSystemAxis M = new CoordinateSystemAxis("M", CoordinateSystemAxisDirection.UP, Unit.METER);


    //Common axes for map projection Coordinate Systems
    public static final CoordinateSystemAxis N = new CoordinateSystemAxis("Northing", CoordinateSystemAxisDirection.NORTH, Unit.METER);
    public static final CoordinateSystemAxis S = new CoordinateSystemAxis("Southing", CoordinateSystemAxisDirection.SOUTH, Unit.METER);
    public static final CoordinateSystemAxis E = new CoordinateSystemAxis("Easting", CoordinateSystemAxisDirection.EAST, Unit.METER);
    public static final CoordinateSystemAxis W = new CoordinateSystemAxis("Westing", CoordinateSystemAxisDirection.WEST, Unit.METER);




    private final String axisName;
    private final CoordinateSystemAxisDirection coordinateSystemAxisDirection;
    private final Unit unit;

    public CoordinateSystemAxis(String axisName, CoordinateSystemAxisDirection coordinateSystemAxisDirection, Unit unit) {
        this.axisName = axisName;
        this.coordinateSystemAxisDirection = coordinateSystemAxisDirection;
        this.unit = unit;
    }

    public String getAxisName() {
        return axisName;
    }

    public CoordinateSystemAxisDirection getAxisDirection() {
        return coordinateSystemAxisDirection;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoordinateSystemAxis)) return false;

        CoordinateSystemAxis that = (CoordinateSystemAxis) o;

        if (axisName != null ? !axisName.equals(that.axisName) : that.axisName != null) return false;
        if (coordinateSystemAxisDirection != that.coordinateSystemAxisDirection) return false;
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = axisName != null ? axisName.hashCode() : 0;
        result = 31 * result + (coordinateSystemAxisDirection != null ? coordinateSystemAxisDirection.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoordinateSystemAxis{" +
                "axisName='" + axisName + '\'' +
                ", coordinateSystemAxisDirection=" + coordinateSystemAxisDirection +
                ", unit=" + unit +
                '}';
    }
}
