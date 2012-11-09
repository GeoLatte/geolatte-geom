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
 * A builder for <code>PointSequence</code>s.
 *
 * <p>PointSequence</p>s are built by adding points in sequence.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public interface PointSequenceBuilder {

    /**
     * Adds a <code>Point</code> to the <code>PointSequence</code> being built.
     *
     * @param coordinates the coordinates of the <code>Point</code> that is added
     * @return this instance
     */
    PointSequenceBuilder add(double[] coordinates);

    /**
     * Adds a 2-dimensional point to the <code>PointSequence</code> being built.
     * @param x the X-coordinate of the of the <code>Point</code> that is added
     * @param y the Y-coordinate of the of the <code>Point</code> that is added
     * @throws IllegalArgumentException when the <code>DimensionalFlag</code> returned by {@link #getDimensionalFlag()}
     * is not equal to {@link DimensionalFlag#d2D}
     * @return this instance
     */
    PointSequenceBuilder add(double x, double y);

    /**
     * Adds a 3-dimensional point to the <code>PointSequence</code> being built. The <code>DimensionalFlag</code> determines
     * whether the third dimension is a Z- or M-coordinate.
     *
     * @param x the X-coordinate of the of the <code>Point</code> that is added
     * @param y the Y-coordinate of the of the <code>Point</code> that is added
     * @param zOrm the Z- or M-coordinate of the of the <code>Point</code> that is added
     * @throws IllegalArgumentException when the <code>DimensionalFlag</code> returned by {@link #getDimensionalFlag()}
     * is not equal to {@link DimensionalFlag#d3D} or {@link DimensionalFlag#d2DM}
     * @return this instance
     */
    PointSequenceBuilder add(double x, double y, double zOrm);

    /**
     * Adds a 4-dimensional point to the <code>PointSequence</code> being built.
     *
     * @param x the X-coordinate of the of the <code>Point</code> that is added
     * @param y the Y-coordinate of the of the <code>Point</code> that is added
     * @param z the Z-coordinate of the of the <code>Point</code> that is added
     * @param m the M-coordinate of the of the <code>Point</code> that is added
     * @throws IllegalArgumentException when the <code>DimensionalFlag</code> returned by {@link #getDimensionalFlag()}
     * is not equal to {@link DimensionalFlag#d3DM}
     * @return this instance
     */
    PointSequenceBuilder add(double x, double y, double z, double m);

    /**
     * Adds the specified <code>Point</code> to the <code>PointSequence</code> being built.
     *
     * @param pnt the <code>Point</code> that is added.
     * @return this instance
     * @throws IllegalArgumentException when the <code>DimensionalFlag</code> of the specified <code>Point</code> is not
     * equal to the flag returned by {@link #getDimensionalFlag()}
     */
    PointSequenceBuilder add(Point pnt);

    /**
     * Returns the <code>DimensionalFlag</code> of the <code>PointSequence</code> being built.
     *
     * @return the <code>DimensionalFlag</code> of the <code>PointSequence</code> being built.
     */
    DimensionalFlag getDimensionalFlag();

    /**
     * Returns the result of this builder.
     *
     * @return the <code>PointSequence</code> that has been built by this builder instance.
     * @throws IllegalStateException when the construction of the <code>PointSequence</code> has not yet been completed.
     */
    PointSequence toPointSequence();

}
