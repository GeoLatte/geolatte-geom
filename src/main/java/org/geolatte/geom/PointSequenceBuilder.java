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
     * Adds the specified coordinates to the <code>PointSequence</code> being built.
     *
     * @param coordinates the coordinates the
     * @return the
     */
    PointSequenceBuilder add(double[] coordinates);

    /**
     * @param x
     * @param y
     * @return
     */
    PointSequenceBuilder add2D(double x, double y);

    PointSequenceBuilder add3D(double x, double y, double z);

    PointSequenceBuilder add2DM(double x, double y, double m);

    PointSequenceBuilder add3DM(double x, double y, double z, double m);

    PointSequence toPointSequence();

}
