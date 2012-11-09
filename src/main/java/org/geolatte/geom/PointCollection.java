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

/**
 * A <code>PointCollection</code> holds all points that determine a <code>Geometry</code>.
 *
 * <p>A PointCollection is conceptually a list of (lists of) <code>Point</code>s</p>.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 7/25/12
 */
public interface PointCollection extends Cloneable, Serializable {


    /**
     * Returns true iff the <code>Point</code>s in this instance have a Z-coordinate.
     *
     * @return true iff the <code>Point</code>s in this instance have a Z-coordinate.
     */
    boolean is3D();

    /**
     * Returns true iff the <code>Point</code>s in this instance have an M-coordinate.
     *
     * @return true iff the <code>Point</code>s in this instance have an M-coordinate.
     */
    boolean isMeasured();

    /**
     * Returns the <code>DimensionalFlag</code> of this <code>PointCollection</code>.
     *
     * @return the <code>DimensionalFlag</code> of this <code>PointCollection</code>
     */
    DimensionalFlag getDimensionalFlag();

    /**
     * Returns the {@link CrsId} of this <code>PointCollection</code>.
     *
     * @return the <code>CrsId</code> of this <code>PointCollection</code>.
     */
    CrsId getCrsId();

    /**
     * Returns true iff this <code>PointCollection</code> contains no <code>Point</code>s
     *
     * @return true iff this <code>PointCollection</code> contains no <code>Point</code>s
     */
    boolean isEmpty();

    /**
     * Returns the coordinate dimension of this <code>PointCollection</code>.
     * <p/>
     * <p>The coordinate dimension is number of measurements or axes needed to describe a position of a <code>Point</code>
     * in a coordinate system.</p>
     *
     * @return the coordinate dimension of this <code>PointCollection</code>
     */
    int getCoordinateDimension();

    /**
     * Copies the coordinates of the <code>Point</code> at the specified (zero-based) position in this <code>PointCollection</code>
     * into the specified coordinate array.
     *
     * @param coordinates an array for the coordinates of the <code>Point</code> at the specified position
     * @param position the position of the <code>Point</code> in this <code>PointCollection</code> whose coordinates
     * are copied into the coordinates array
     */

    void getCoordinates(double[] coordinates, int position);

    /**
     * Returns the X-coordinate for the <code>Point</code> at the specified position in this <code>PointCollection</code>.
     *
     * @param position position of the <code>Point</code> in this <code>PointCollection</code>
     * @return the X-coordinate of the specified <code>Point</code>
     */
    double getX(int position);

    /**
     * Returns the Y-coordinate for the <code>Point</code> at the specified position in this <code>PointCollection</code>.
     *
     * @param position position of the <code>Point</code> in this <code>PointCollection</code>
     * @return the Y-coordinate of the specified <code>Point</code>
     */
    double getY(int position);

    /**
     * Returns the Z-coordinate for the <code>Point</code> at the specified position in this <code>PointCollection</code>.
     *
     * @param position position of the <code>Point</code> in this <code>PointCollection</code>
     * @return the Z-coordinate of the specified <code>Point</code>
     */
    double getZ(int position);

    /**
     * Returns the M-coordinate for the <code>Point</code> at the specified position in this <code>PointCollection</code>.
     *
     * @param position position of the <code>Point</code> in this <code>PointCollection</code>
     * @return the M-coordinate of the specified <code>Point</code>
     */
    double getM(int position);

    /**
     * Returns the specified coordinate for the <code>Point</code> at the specified position in this <code>PointCollection</code>.
     *
     * @param position position of the <code>Point</code> in this <code>PointCollection</code>
     * @param component the coordinate component for which to return the coordinate
     * @return the coordinate, specified by the component parameter, of the specified <code>Point</code>
     */
    double getCoordinate(int position, CoordinateComponent component);

    /**
     * Returns the number of <code>Point</code>s contained in this <code>PointCollection</code>.
     * @return the number of <code>Point</code>s contained in this <code>PointCollection</code>.
     */
    int size();

    /**
     * Accepts a <code>PointVisitor</code>.
     *
     * <p>This instance will pass the visitor to all of its <code>Point</code>s.</p>
     * @param visitor the visitor for this instance's <code>Point</code>s
     */
    void accept(PointVisitor visitor);
}
