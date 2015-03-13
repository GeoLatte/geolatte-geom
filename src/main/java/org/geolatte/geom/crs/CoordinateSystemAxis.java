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
 * An axis of a <code>CoordinateSystem.</code>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
abstract public class CoordinateSystemAxis {

    private final String axisName;
    private final CoordinateSystemAxisDirection coordinateSystemAxisDirection;
    private final Unit unit;
    private final int normalOrder;

    public static GeodeticLongitudeCSAxis mkLonAxis() {
        return new GeodeticLongitudeCSAxis("Longitude", Unit.DEGREE);
    }

    public static GeodeticLatitudeCSAxis mkLatAxis() {
        return new GeodeticLatitudeCSAxis("Latitude", Unit.DEGREE);
    }

    public static StraightLineAxis mkXAxis() {
        return new StraightLineAxis("X", CoordinateSystemAxisDirection.EAST, Unit.METER);
    }

    public static StraightLineAxis mkYAxis() {
        return new StraightLineAxis("Y", CoordinateSystemAxisDirection.NORTH, Unit.METER);
    }

    public static StraightLineAxis mkZAxis() {
        return new StraightLineAxis("Z", CoordinateSystemAxisDirection.UP, Unit.METER);
    }


    /**
     * Creates an instance
     *
     * @param axisName                      the name for this axis
     * @param coordinateSystemAxisDirection the direction for this axis
     * @param unit                          the unit of this axis
     */
    CoordinateSystemAxis(String axisName, CoordinateSystemAxisDirection coordinateSystemAxisDirection, Unit unit) {
        this.axisName = axisName;
        this.coordinateSystemAxisDirection = coordinateSystemAxisDirection;
        this.unit = unit;
        this.normalOrder = coordinateSystemAxisDirection.getDefaultNormalOrder();
    }

    /**
     * Creates an instance with
     *
     * @param axisName                      the name for this axis
     * @param coordinateSystemAxisDirection the direction for this axis
     * @param unit                          the unit of this axis
     */
    CoordinateSystemAxis(String axisName, CoordinateSystemAxisDirection coordinateSystemAxisDirection, int normalOrder, Unit unit) {
        this.axisName = axisName;
        this.coordinateSystemAxisDirection = coordinateSystemAxisDirection;
        this.unit = unit;
        this.normalOrder = normalOrder;
    }


    /**
     * Returns the name of this axis.
     *
     * @return
     */
    public String getAxisName() {
        return axisName;
    }

    public CoordinateSystemAxisDirection getAxisDirection() {
        return coordinateSystemAxisDirection;
    }

    public Unit getUnit() {
        return unit;
    }

    /**
     * Indicates the position for the Axis in the normalized order of axes.
     *
     * <p>The coordinates of {@code Position}s in a specific {@code CoordinateSystem} needs to respect this invariant:
     * if the axes are sorted according to their normalOrder property, then this gives the order in which the corresponding
     * coordinate elements are stored in the position.</p>
     */
    public int getNormalOrder() {
        return normalOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;

        CoordinateSystemAxis that = (CoordinateSystemAxis) o;

        if (axisName != null ? !axisName.equals(that.axisName) : that.axisName != null) return false;
        if (coordinateSystemAxisDirection != that.coordinateSystemAxisDirection) return false;
        if (normalOrder != that.normalOrder) return false;
        return !(unit != null ? !unit.equals(that.unit) : that.unit != null);
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
