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
 * Indicates the coordinate dimension of the points of a <code>PointSequence</code> or <code>Geometry</code> and whether
 * the coordinates have Z- and/or M-components.
 *
 * <p> The coordinate dimension is the number of components in a coordinate. Points are 2-, 3- or 4-dimensional.</p>

 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/6/11
 *
 */
public enum DimensionalFlag {
    /**
     * Indicates 2 dimensions (X,Y)
     */
    XY(2, -1, -1),

    /**
     * Indicates 3 dimensions (X,Y,Z). The Z-coordinate is typically used to represent altitude.
     */
    XYZ(3, 2, -1),
    /**
     * Indicates 3 dimensions (X,Y,M). The M-coordinate represents a measurement.
     */
    XYM(3, -1, 2),

    /**
     * Indicates 4 dimensions (X,Y,M,Z).
     */
    XYZM(4, 2, 3);

    private final int dimension;
    public final int X = 0;
    public final int Y = 1;
    public final int Z;
    public final int M;

    private DimensionalFlag(int dimension, int zComponent, int mComponent) {
        this.dimension = dimension;
        this.Z = zComponent;
        this.M = mComponent;
    }


    /**
     * Returns an appropriate <code>DimensionalFlag</code> depending on whether coordinates have
     * a Z- and/or M-coordinate component
     *
     * @param is3D specifies that coordinates have a Z-component
     * @param isMeasured specifies that coordinates have an M-component
     * @return the appropriate DimensionalFlag depending on whether coordinates have a Z- and/or M-coordinate component
     */
    public static DimensionalFlag valueOf(boolean is3D, boolean isMeasured) {
        if (isMeasured) {
            return is3D ? XYZM : XYM;
        } else {
            return is3D ? XYZ : XY;
        }
    }

    /**
     * Returns the coordinate dimension of this <code>DimensionalFlag</code>.
     *
     * <p>The coordinate dimension is the number of components in a coordinate.</p>
     * @return 2 for XY, 3 for XYZ or XYM, 4 for XYZM.
     */
    public int getCoordinateDimension() {
        return this.dimension;
    }

    /**
     * Returns true for <code>DimensionalFlag</code>s for coordinates with a Z-component.
     * @return true for XYZ or XYZM
     */
    public boolean is3D() {
        return (this == XYZ ||this == XYZM);
    }

    /**
     * Returns true for <code>DimensionalFlag</code>s for coordinates with an M-component.
     *
     * @return true for XYM or XYZM
     */
    public boolean isMeasured() {
        return (this == XYM ||this == XYZM);
    }


    /**
     * Returns the (zero-based) position in a coordinate tuple of the specified <code>CoordinateComponent</code>.
     *
     * @param component the compoment for which to return the position
     * @return
     */
    public int index(CoordinateComponent component) {
        switch (component) {
            case X:
                return 0;
            case Y:
                return 1;
            case Z:
                return Z;
            case M:
                return M;
        }
        return -1;
    }
}
