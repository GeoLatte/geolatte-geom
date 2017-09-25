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
 * Describes the units of a <code>CoordinateSystemAxis</code>.
 *
 */
abstract public class Unit extends CrsIdentifiable {


    //common units
    public final static AngularUnit DEGREE = new AngularUnit(new CrsId("EPSG",9122), "degree", 0.01745329251994328);
    public final static AngularUnit RADIAN = new AngularUnit(new CrsId("EPSG", 9101), "radian", 1);
    public final static LinearUnit METER = new LinearUnit(new CrsId("EPSG", 9001), "metre", 1);

    //this is used when the units can't be determined.
    public final static LinearUnit UNKNOWN_LINEAR = new LinearUnit(CrsId.UNDEFINED, "unknown", 1);
    public final static AngularUnit UNKNOWN_Angular = new AngularUnit(CrsId.UNDEFINED, "unknown", 1);

    private final double conversionFactor;

    /**
     * Creates an instance.
     *
     * @param crsId
     * @param name
     * @param conversionFactor
     */
    public Unit(CrsId crsId, String name, double conversionFactor) {
        super(crsId, name);
        this.conversionFactor = conversionFactor;
    }

    /**
     * Returns the fundamental unit for this type of <code>Unit</code>.
     *
     * @return Meter for linear, Radian for angular units.
     */
    public abstract Unit getFundamentalUnit();

    /**
     * Returns the conversion factor: the scalar value that converts a value of this <code>Unit</code> to the fundamental unit
     * for this type of unit.
     *
     * @return
     */
    public double getConversionFactor() {
        return conversionFactor;
    }

    /**
     * Returns true if this <code>Unit</code> is angular.
     *
     * @return
     */
    public abstract boolean isAngular();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Unit unit = (Unit) o;

        if (Double.compare(unit.conversionFactor, conversionFactor) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(conversionFactor);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "SRID=" + getCrsId().toString() +
                ", conversionFactor=" + conversionFactor +
                '}';
    }
}
