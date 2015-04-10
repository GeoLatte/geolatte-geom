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
     * Default implementation of this interface
     */
    public final MeasureGeometryOperations Default = new DefaultMeasureGeometryOperations();


    /**
     * Creates an operation to calculate the <code>GeometryCollection</code> that matches the specified range of M-coordinate value
     * inclusively.
     * <p/>
     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
     * <p>The semantics implemented here are specified by SFA 1.2.1, section 6.1.2.6.</p>
     *
     * @param geometry     the geometry on which to perform the calculation
     * @param startMeasure the start of the specified range of M-coordinate values
     * @param endMeasure   the end of the specified range of M-coordinate values
     * @return a <code>GeometryOperation</code> that calculates the <code>GeometryCollection</code> matching the
     * specified range of M-coordinate values.
     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
     */
    public <P extends C2D & Measured> Geometry<P> locateBetween(final Geometry<P> geometry, final double startMeasure, final double endMeasure);

    /**
     * Creates an operation to calculate the <code>GeometryCollection</code> that matches the specified M-coordinate value.
     * <p/>
     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
     * <p>The semantics implemented here are specified by SFA 1.2.1, section 6.1.2.6.</p>
     *
     * @param geometry the geometry on which to perform the calculation
     * @param mValue   the specified M-coordinate value
     * @return a <code>GeometryOperation</code> that calculates the <code>GeometryCollection</code> matching
     * the specified M-coordinate value.
     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
     */
    public <P extends C2D & Measured> Geometry<P> locateAlong(final Geometry<P> geometry, final double mValue);

    /**
     * Creates a <code>GeometryOperation</code> to calculate the measure value
     * at the specified point
     *
     * @param geometry a linear <code>Geometry</code>
     * @param pos      a <code>Position</code> within tolerance of the geometry
     * @param tolerance the maximum value allowed for distance between pos and geometry.
     * @return a <code>GeometryOperation</code> that returns the measure value at the specified point on the specified geometry
     */
    public <P extends C2D & Measured> double measureAt(Geometry<P> geometry, P pos, double tolerance);

    /**
     * Creates a <code>GeometryOperation</code> that creates a new Geometry
     * that has the same 2D/3D-coordinates as the specified <code>Geometry</code>, and
     * with measure values that correspond with the length along it (or begin-measure + length).
     * <p/>
     * <p>The positionTypeMarker is needed because the compiler can't figure our the relationship between the input CRS
     * and its measured variant.</p>
     *
     * @param geometry           the <code>Geometry</code> for which to build measures
     * @param keepBeginMeasure   if true, than the measure of the first coordinate is used as start-value
     * @param positionTypeMarker the type of {@code Position} for the result of the created operations
     * @return a <code>GeometryOperation</code> that returns a Geometry with measures increasing with length
     */
    public <P extends C2D, M extends C2D & Measured> Geometry<M> measureOnLength(
            Geometry<P> geometry, Class<M> positionTypeMarker, boolean keepBeginMeasure);

    /**
     * Creates a {@code GeometryOperation} that returns the minimum measure value of the {@code Position}s
     * of the specified Geometry.
     * <p/>
     * If the geometry is empty, this method returns Double.NaN
     *
     * @param geometry the geometry for which the minimum measure is sought
     * @return the minimum measure
     * @throws IllegalArgumentException if the geometry is not a measured geometry
     */
    public <P extends Position & Measured> double minimumMeasure(Geometry<P> geometry);

    /**
     * Creates a {@code GeometryOperation} that returns the maximum measure value of the {@code Position}s
     * of the specified Geometry.
     * <p/>
     * If the geometry is empty, this method returns Double.NaN
     *
     * @param geometry the geometry for which the maximum measure is sought
     * @return the minimum measure
     * @throws IllegalArgumentException if the geometry is not a measured geometry
     */
    public <P extends Position & Measured> double maximumMeasure(Geometry<P> geometry);


}
