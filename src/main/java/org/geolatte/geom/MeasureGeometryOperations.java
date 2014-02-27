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
 * A set of operations on measured (2DM/3DM) <code>Geometry</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public interface MeasureGeometryOperations {

    /**
     * Creates a <code>GeometryOperation</code> to calculate the measure value
     * at the specified point
     *
     * @param geometry a linear <code>Geometry</code>
     * @param pos    a <code>Position</code> on the geometry
     * @return a <code>GeometryOperation</code> that returns the measure value at the specified point on the specified geometry
     */
    public <P extends Projected<P> & Measured> GeometryOperation<Double> createGetMeasureOp(Geometry<P> geometry, P pos);

    /**
     * Creates a <code>GeometryOperation</code> that creates a new Geometry
     * that has the same 2D/3D-coordinates as the specified <code>Geometry</code>, and
     * with measure values that correspond with the length along it (or begin-measure + length).
     * <p/>
     * <p>The length is calculated in the 2-dimensional X/Y-plane.</p>
     *
     * @param geometry         the <code>Geometry</code> for which to build measures
     * @param keepBeginMeasure if true, than the measure of the first coordinate is used as start-value
     * @return a <code>GeometryOperation</code> that returns a Geometry with measures increasing with length
     */
    public <P extends Projected<P>> GeometryOperation<Geometry<?>> createMeasureOnLengthOp(Geometry<P> geometry, boolean keepBeginMeasure);

    /**
     * Creates a {@code GeometryOperation} that returns the minimum measure value of the points
     * of the specified Geometry.
     *
     * If the geometry is empty, this method returns Double.NaN
     *
     * @param geometry the geometry for which the minimum measure is sought
     * @return the minimum measure
     * @throws IllegalArgumentException if the geometry is not a measured geometry
     */
    public <P extends Position<P> & Measured> GeometryOperation<Double> createGetMinimumMeasureOp(Geometry<P> geometry);

    /**
     * Creates a {@code GeometryOperation} that returns the maximum measure value of the points
     * of the specified Geometry.
     *
     * If the geometry is empty, this method returns Double.NaN
     *
     * @param geometry the geometry for which the maximum measure is sought
     * @return the minimum measure
     * @throws IllegalArgumentException if the geometry is not a measured geometry
     */
    public <P extends Position<P> & Measured> GeometryOperation<Double> createGetMaximumMeasureOp(Geometry<P> geometry);


}
