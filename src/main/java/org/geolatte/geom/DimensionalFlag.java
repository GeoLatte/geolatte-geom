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
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/6/11
 *
 */
public enum DimensionalFlag {

    XY(2),
    XYZ(3),
    XYM(3),
    XYZM(4);

    private final int dimension;

    private DimensionalFlag(int dimension) {
        this.dimension = dimension;
    }


    public static DimensionalFlag parse(boolean is3D, boolean isMeasured) {
        if (!isMeasured && !is3D) return XY;
        if (!isMeasured && is3D) return XYZ;
        if (isMeasured && !is3D) return XYM;
        return XYZM;
    }

    public int getCoordinateDimension() {
        return this.dimension;
    }

    public boolean is3D() {
        return (this == XYZ ||this == XYZM);
    }

    public boolean isMeasured() {
        return (this == XYM ||this == XYZM);
    }


    public int getIndex(CoordinateAccessor accessor) {
        switch (accessor) {
            case X:
                return 0;
            case Y:
                return 1;
            case Z:
                return ( this == XYZ || this == XYZM) ? 2 : -1;
            case M:
                if (this == XYM)
                    return 2;
                else if (this == XYZM)
                    return 3;
                else
                    return -1;
        }
        return -1;
    }
}
