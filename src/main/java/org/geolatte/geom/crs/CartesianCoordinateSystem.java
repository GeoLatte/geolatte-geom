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
 *         creation-date: 4/6/11
 *
 * TODO -- remove this class. Base class should now be sufficient....
 */
public class CartesianCoordinateSystem extends CoordinateSystem {

    public final static CartesianCoordinateSystem XY = new CartesianCoordinateSystem(CoordinateSystemAxis.X, CoordinateSystemAxis.Y);
    public final static CartesianCoordinateSystem XYZ = new CartesianCoordinateSystem(CoordinateSystemAxis.X, CoordinateSystemAxis.Y, CoordinateSystemAxis.Z);
    public final static CartesianCoordinateSystem XYM = new CartesianCoordinateSystem(CoordinateSystemAxis.X, CoordinateSystemAxis.Y, CoordinateSystemAxis.M);
    public final static CartesianCoordinateSystem XYZM = new CartesianCoordinateSystem(CoordinateSystemAxis.X, CoordinateSystemAxis.Y, CoordinateSystemAxis.Z, CoordinateSystemAxis.M);

    public CartesianCoordinateSystem(AccessorToAxisMap accessorToAxisMap, CoordinateSystemAxis... axes) {
        super(accessorToAxisMap, axes);
    }

    public CartesianCoordinateSystem(CoordinateSystemAxis... axes) {
        this(AccessorToAxisMap.createDefault(), axes);
    }

    //TODO -- remove use of this function
    public static CartesianCoordinateSystem parse(boolean is3D, boolean isMeasured) {
        if (!isMeasured && !is3D) return XY;
        if (!isMeasured && is3D) return XYZ;
        if (isMeasured && !is3D) return XYM;
        return XYZM;
    }


}
