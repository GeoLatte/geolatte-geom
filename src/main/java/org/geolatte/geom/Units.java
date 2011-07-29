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
 * The units of
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
public class Units {

    //TODO -- Use SFS 1.2.1 Annex B to complete this

    public final static Units DECIMAL_DEGREE = new Units("deg.", false, Math.PI/180d);
    public final static Units METER = new Units("m", true, 1d);
    public final static Units INTL_FOOT = new Units("ft", true, 0.3048);
    public final static Units CENTIMETER = new Units("cm", true, 0.1d);
    public final static Units MILLIIMETER = new Units("mm", true, 0.01d);
    public final static Units RADIAN = new Units("rad", false, 1d);
    public final static Units INDETERMINATE = new Units("", true, 1d);

    private final String abbreviation;
    private final boolean isLinear;
    private final double baseUnitsPerUnit;

    private Units(String abbreviation, boolean isLinear, double baseUnitsperUnit){
        this.abbreviation = abbreviation;
        this.baseUnitsPerUnit = baseUnitsperUnit;
        this.isLinear = isLinear;

    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public boolean isAngular() {
        return !isLinear;
    }

    public double getBaseUnitsPerUnit(){
        return baseUnitsPerUnit;
    }

    public Units getBaseUnit(){
        if (this.isAngular()){
            return RADIAN;
        }
        return METER;
    }
}
