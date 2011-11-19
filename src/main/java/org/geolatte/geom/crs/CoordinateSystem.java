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

import java.util.Arrays;

/**
 * A coordinate system.
 *
 * <p>A coordinate system is characterized by its <code>CoordinateSystemAxis</code>es (in order).</p>
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/29/11
 */
public class CoordinateSystem {

    protected final CoordinateSystemAxis[] axes;

    /**
     * Constructs an <code>CoordinateSystem</code>.
     *
     * <p><code>CoordinateSystem</code>s are characterized by their <code>CoordinateSystemAxis</code>es. </p>
     *
     * @param axes the sequence (at least two) of its <code>CoordinateSystem</code>es.
     * @throws IllegalArgumentException when less than two axes are specified.
     */
    public CoordinateSystem(CoordinateSystemAxis... axes) {
        if (axes == null || axes.length < 2) {
            throw new IllegalArgumentException("Require at least 2 axes");
        }
        this.axes = axes;
    }

    /**
     * Returns the <code>CoordinateSystemAxis</code>es of this <code>CoordinateSystem</code> (in order).
     * @return
     */
    public CoordinateSystemAxis[] getAxes() {
        return Arrays.copyOf(axes, axes.length);
    }

    /**
     * Returns the coordinate dimension, i.e. the number of axes in this coordinate system.
     * @return
     */
    public int getCoordinateDimension() {
        return this.axes.length;
    }

    /**
     * Returns the position of the specified <code>CoordinateSystemAxis</code> in this <code>CoordinateSystem</code>.
     * @param axis
     * @return
     */
    public int getAxisIndex(CoordinateSystemAxis axis) {
        int i = 0;
        for (CoordinateSystemAxis a : axes) {
            if (a == axis) return i;
            i++;
        }
        return -1;
    }

    /**
     * Returns the <code>CoordinateSystemAxis</code> at the specified position.
     *
     * @param index
     * @return
     */
    public CoordinateSystemAxis getAxis(int index) {
        return this.axes[index];
    }

    /**
     * Returns the <code>Unit</code> of the axis at the specified position.
     *
     * @param index
     * @return
     */
    public Unit getAxisUnit(int index){
        return this.axes[index].getUnit();
    }

}
