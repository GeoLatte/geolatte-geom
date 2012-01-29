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
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.crs.CrsId;

import java.io.Serializable;
import java.util.Arrays;

public class Points implements Serializable {

    public static Point create(double x, double y, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y}, DimensionalFlag.XY), crsId);
    }

    public static Point create3D(double x, double y, double z, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y, z}, DimensionalFlag.XYZ), crsId);
    }

    public static Point createMeasured(double x, double y, double m, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y, m}, DimensionalFlag.XYM), crsId);
    }

    public static Point create(double x, double y, double z, double m, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y, z, m}, DimensionalFlag.XYZM), crsId);
    }

    public static Point create(double x, double y) {
        return new Point(new PackedPointSequence(new double[]{x, y}, DimensionalFlag.XY), CrsId.UNDEFINED);
    }

    public static Point create3D(double x, double y, double z) {
        return new Point(new PackedPointSequence(new double[]{x, y, z}, DimensionalFlag.XYZ), CrsId.UNDEFINED);
    }

    public static Point createMeasured(double x, double y, double m) {
        return new Point(new PackedPointSequence(new double[]{x, y, m}, DimensionalFlag.XYM), CrsId.UNDEFINED);
    }

    public static Point create(double x, double y, double z, double m) {
        return new Point(new PackedPointSequence(new double[]{x, y, z, m}, DimensionalFlag.XYZM), CrsId.UNDEFINED);
    }

    static Point create(double[] coordinates, DimensionalFlag dimensionalFlag, CrsId crsId) {
        if (coordinates == null || coordinates.length == 0) {
            return Point.createEmpty();
        }
        return new Point(new PackedPointSequence(Arrays.copyOf(coordinates, coordinates.length), dimensionalFlag), crsId);
    }

    public static Point createEmpty() {
        return Point.createEmpty();
    }
}