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

public class Unit {

    /**
     * @author Karel Maesen, Geovise BVBA
     *         creation-date: 8/4/11
     */
    public enum Type {

        LINEAR(false),
        ANGULAR(true);

        private final boolean isAngular;

        private Type(boolean isAngular) {
            this.isAngular = isAngular;
        }

        boolean isAngular() {
            return this.isAngular;
        }

    }

    public final static Unit DEGREE = new Unit(9122, "degree", Type.ANGULAR, 0.01745329251994328);
    public final static Unit METER = new Unit(9001, "metre", Type.LINEAR, 1);


    private final int srid;

    private final String name;

    private final Type type;

    private final double conversionFactor;

    public Unit(int srid, String name, Type type, double conversionFactor) {
        this.srid = srid;
        this.name = name;
        this.type = type;
        this.conversionFactor = conversionFactor;
    }

    public double getConversionFactor() {
        return conversionFactor;
    }

    public String getName() {
        return name;
    }

    public int getSrid() {
        return srid;
    }

    public boolean isAngular(){
        return this.type.isAngular();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Unit)) return false;

        Unit unit = (Unit) o;

        if (Double.compare(unit.conversionFactor, conversionFactor) != 0) return false;
        if (srid != unit.srid) return false;
        if (name != null ? !name.equals(unit.name) : unit.name != null) return false;
        if (type != unit.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = srid;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        temp = conversionFactor != +0.0d ? Double.doubleToLongBits(conversionFactor) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
