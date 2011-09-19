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

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/6/11
 */
public class DimensionalCoordinate extends Coordinate {

    public double m;
    private final DimensionalFlag dimensionalFlag;

    DimensionalCoordinate(DimensionalFlag dimensionalFlag){
        super();
        this.m = Double.NaN;
        this.dimensionalFlag = dimensionalFlag;
    }

    DimensionalCoordinate(Coordinate c, double m, DimensionalFlag dimensionalFlag) {
        super(c);
        this.m = m;
        this.dimensionalFlag = dimensionalFlag;
    }

    public double getZ(){
        return  (this.dimensionalFlag.is3D()) ? this.z : Double.NaN;
    }

    public double getM(){
        return (this.dimensionalFlag.isMeasured()) ? this.m : Double.NaN;
    }

    public boolean is3D(){
        return this.dimensionalFlag.is3D();
    }

    public boolean isMeasured(){
        return this.dimensionalFlag.isMeasured();
    }

    public DimensionalFlag getDimensionalFlag(){
        return this.dimensionalFlag;
    }


}
