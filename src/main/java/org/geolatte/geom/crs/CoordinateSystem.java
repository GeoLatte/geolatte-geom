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

import java.util.Arrays;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
public class CoordinateSystem {


    protected final AccessorToAxisMap accessorToAxisMap;
    protected final CoordinateSystemAxis[] axes;

    public CoordinateSystem(AccessorToAxisMap accessorToAxisMap, CoordinateSystemAxis... axes) {
        if (axes == null || axes.length < 2) {
            throw new IllegalArgumentException("Require at least 2 axes");
        }
        this.accessorToAxisMap = accessorToAxisMap;
        this.accessorToAxisMap.initialize(axes);
        this.axes = axes;
    }

    public CoordinateSystem(CoordinateSystemAxis... axes) {
        this(AccessorToAxisMap.createDefault(), axes);
    }

    public CoordinateSystemAxis[] getAxes() {
        return Arrays.copyOf(axes, axes.length);
    }

    public int getCoordinateDimension() {
        return this.axes.length;
    }

    public boolean is3D() {
        return getIndex(CoordinateAccessor.Z) > -1;
    }

    public boolean isMeasured() {
        return getIndex(CoordinateAccessor.M) > -1;
    }

    /**
     * Returns the index in the coordinates of a position (an array) that is used
     * by the specified <code>CoordinateAccessor</code>.
     *
     * @param accessor a enum value that represent a coordinate accessor method.
     * @return the index in the coordinates to be used by the specified accessor.
     */
    public int getIndex(CoordinateAccessor accessor) {
        return this.accessorToAxisMap.getIndex(accessor);
    }

    public int getAxisIndex(CoordinateSystemAxis axis) {
        int i = 0;
        for (CoordinateSystemAxis a : axes) {
            if (a == axis) return i;
            i++;
        }
        return -1;
    }

    public CoordinateSystemAxis getAxis(int index) {
        return this.axes[index];
    }

    public Unit getAxisUnits(int index){
        return this.axes[index].getUnit();
    }

}
