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

/**
 * A set of operations on measured <code>Geometry</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public interface MeasureGeometryOperations {

    /**
     * Creates a <code>GeometryOperation</code> to calculate the measure value
     * at the specified point
     * @param geometry a linear <code>Geometry</code>
     * @param point a <code>Point</code> on the geometry
     * @return a <code>GeometryOperation</code> that returns the measure value at the specified point on the specified geometry
     */
    public GeometryOperation<Double> createGetMeasureOp(Geometry geometry, Point point);

    /**
     * Creates a <code>GeometryOperation</code> that creates a new Geometry
     * that has the same 2D/3D-coordinates as the specified <code>Geometry</code>, and
     * with measure values that correspond with the length along it (or begin-measure + length).
     *
     * <p>The length is calculated in the 2-dimensional X/Y-plane.</p>
     *
     * @param geometry the <code>Geometry</code> for which to build measures
     * @param keepBeginMeasure if true, than the measure of the first coordinate is used as start-value
     * @return a <code>GeometryOperation</code> that returns a Geometry with measures increasing with length
     */
    public GeometryOperation<Geometry> createMeasureOnLengthOp(Geometry geometry, boolean keepBeginMeasure);

//    /**
//     * Creates a <code>GeometryOperation</code> that checks whether the specified <code>Geometry</code>
//     * coordinates increase or decrease monotonically.
//     *
//     * @param geometry the <code>Geometry</code> to check.
//     * @return a <code>GeometryOperation</code> that returns true of the measures in-/decrease monotonically.
//     */
//    public GeometryOperation<Boolean> createCheckMonotoneOp(Geometry geometry);

}
