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

/**
 * A convenience factory for <code>Point</code>s.
 */
public class Points implements Serializable {

    /**
     * Creates a 2D <code>Point</code> using the specified coordinates and coordinate reference system
     *
     * @param x     the X-coordinate of the <code>Point</code>
     * @param y     the Y-coordinate of the <code>Point</code>
     * @param crsId the <code>CrsId</code> of the coordinate reference system of the <code>Point</code>
     * @return a <code>Point</code> with the specified X- and Y-coordinates and coordinate reference system
     */
    public static Point create(double x, double y, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y}, DimensionalFlag.d2D), crsId);
    }

    /**
     * Creates a 3D <code>Point</code> using the specified coordinates and coordinate reference system
     *
     * @param x     the X-coordinate of the <code>Point</code>
     * @param y     the Y-coordinate of the <code>Point</code>
     * @param z     the Z-coordinate of the <code>Point</code>
     * @param crsId the <code>CrsId</code> of the coordinate reference system of the <code>Point</code>
     * @return a <code>Point</code> with the specified X-, Y- and Z-coordinates and coordinate reference system
     */
    public static Point create3D(double x, double y, double z, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y, z}, DimensionalFlag.d3D), crsId);
    }

    /**
     * Creates a 2DM <code>Point</code> using the specified coordinates and coordinate reference system
     *
     * @param x     the X-coordinate of the <code>Point</code>
     * @param y     the Y-coordinate of the <code>Point</code>
     * @param m     the M-coordinate of the <code>Point</code>
     * @param crsId the <code>CrsId</code> of the coordinate reference system of the <code>Point</code>
     * @return a <code>Point</code> with the specified X-, Y- and M-coordinates and coordinate reference system
     */
    public static Point createMeasured(double x, double y, double m, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y, m}, DimensionalFlag.d2DM), crsId);
    }

    /**
     * Creates a 3DM <code>Point</code> using the specified coordinates and coordinate reference system
     *
     * @param x     the X-coordinate of the <code>Point</code>
     * @param y     the Y-coordinate of the <code>Point</code>
     * @param z     the Z-coordinate of the <code>Point</code>
     * @param m     the M-coordinate of the <code>Point</code>
     * @param crsId the <code>CrsId</code> of the coordinate reference system of the <code>Point</code>
     * @return a <code>Point</code> with the specified X-, Y-, Z- and M-coordinates and coordinate reference system
     */
    public static Point create(double x, double y, double z, double m, CrsId crsId) {
        return new Point(new PackedPointSequence(new double[]{x, y, z, m}, DimensionalFlag.d3DM), crsId);
    }

    /**
     * Creates a 2D <code>Point</code> using the specified coordinates, and an undefined coordinate reference system.
     *
     * @param x the X-coordinate of the <code>Point</code>
     * @param y the Y-coordinate of the <code>Point</code>
     * @return a <code>Point</code> with the specified X- and Y-coordinates and undefined coordinate reference system
     */
    public static Point create(double x, double y) {
        return new Point(new PackedPointSequence(new double[]{x, y}, DimensionalFlag.d2D), CrsId.UNDEFINED);
    }

    /**
     * Creates a 3D <code>Point</code> using the specified coordinates, and an undefined coordinate reference system.
     *
     * @param x the X-coordinate of the <code>Point</code>
     * @param y the Y-coordinate of the <code>Point</code>
     * @param z the Z-coordinate of the <code>Point</code>
     * @return a <code>Point</code> with the specified X-, Y- and Z-coordinates and undefined coordinate reference system
     */
    public static Point create3D(double x, double y, double z) {
        return new Point(new PackedPointSequence(new double[]{x, y, z}, DimensionalFlag.d3D), CrsId.UNDEFINED);
    }

    /**
     * Creates a 2DM <code>Point</code> using the specified coordinates, and an undefined coordinate reference system.
     *
     * @param x the X-coordinate of the <code>Point</code>
     * @param y the Y-coordinate of the <code>Point</code>
     * @param m the M-coordinate of the <code>Point</code>
     * @return a <code>Point</code> with the specified X-, Y- and M-coordinates and undefined coordinate reference system
     */
    public static Point createMeasured(double x, double y, double m) {
        return new Point(new PackedPointSequence(new double[]{x, y, m}, DimensionalFlag.d2DM), CrsId.UNDEFINED);
    }

    /**
     * Creates a 4D <code>Point</code> using the specified coordinates, and an undefined coordinate reference system.
     *
     * @param x the X-coordinate of the <code>Point</code>
     * @param y the Y-coordinate of the <code>Point</code>
     * @param z the Z-coordinate of the <code>Point</code>
     * @param m the M-coordinate of the <code>Point</code>
     * @return a <code>Point</code> with the specified X-, Y- Z- and M-coordinates and undefined coordinate reference system
     */
    public static Point create(double x, double y, double z, double m) {
        return new Point(new PackedPointSequence(new double[]{x, y, z, m}, DimensionalFlag.d3DM), CrsId.UNDEFINED);
    }

    /**
     * Creates a <code>Point</code> using the specified coordinates and coordinate reference system.
     *
     * @param coordinates     the coordinates of the <code>Point</code>
     * @param dimensionalFlag the <code>DimensionalFlag</code> for the <code>Point</code>
     * @param crsId           the <code>CrsId</code> of the coordinate reference system of the <code>Point</code>
     * @return a <code>Point</code> with the specified coordinates, dimensionalflag and coordinate reference system
     */
    static Point create(double[] coordinates, DimensionalFlag dimensionalFlag, CrsId crsId) {
        if (coordinates == null || coordinates.length == 0) {
            return Point.createEmpty();
        }
        return new Point(new PackedPointSequence(Arrays.copyOf(coordinates, coordinates.length), dimensionalFlag), crsId);
    }

    /**
     * Creates an empty <code>Point</code>
     *
     * @return an empty <code>Point</code>
     */
    public static Point createEmpty() {
        return Point.createEmpty();
    }
}