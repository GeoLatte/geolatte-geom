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
 * A factory for <code>GeometryOperation</code>s.
 * <p/>
 * <p>The semantics of the operations is as specified in
 * <a href="http://portal.opengeospatial.org/files/?artifact_id=25355">Simple Feature Access - Part 1: common architecture</a>
 * </p>
 *
 * @author Karel Maesen, Geovise BVBA
 */
public interface GeometryOperations {

    /**
     * Creates an operation to test the simplicity of the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> to test for simplicity.
     * @return a <code>GeometryOperation</code> that checks if the specified <code>Geometry</code> is simple.
     */
    GeometryOperation<Boolean> createIsSimpleOp(final Geometry geometry);

    /**
     * Creates an operation to calculate the boundary of the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> for which to calculate the boundary.
     * @return a <code>GeometryOperation</code> that calculates a <code>Geometry</code> representing the boundary of the specified <code>Geometry</code>.
     */
    GeometryOperation<Geometry> createBoundaryOp(final Geometry geometry);

    /**
     * Creates an operation to calculate the <code>Envelope</code> of the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> for which to calculate the envelope.
     * @return a <code>GeometryOperation</code> that calculates the <code>Envelope</code> of the specified <code>Geometry</code>.
     */
    GeometryOperation<Envelope> createEnvelopeOp(final Geometry geometry);

    /**
     * Creates an operation to check if the specified <code>Geometry</code>s intersect.
     *
     * @param geometry the first <code>Geometry</code> operand of the intersection test
     * @param other    the second <code>Geometry</code> operand of the intersection test
     * @return a <code>GeometryOperation</code> that checks if the specified <code>Geometry</code>s spatially intersect
     */
    GeometryOperation<Boolean> createIntersectsOp(final Geometry geometry, final Geometry other);

    /**
     * Creates an operation to check if the specified <code>Geometry</code>s touch.
     *
     * @param geometry the first <code>Geometry</code> operand of the touch test
     * @param other    the second <code>Geometry</code> operand of the touch test
     * @return a <code>GeometryOperation</code> that checks if the specified <code>Geometry</code>s spatially touch
     */
    GeometryOperation<Boolean> createTouchesOp(final Geometry geometry, final Geometry other);

    /**
     * Creates an operation to check if the specified <code>Geometry</code>s cross.
     *
     * @param geometry the first <code>Geometry</code> operand of the cross test
     * @param other    the second <code>Geometry</code> operand of the cross test
     * @return a <code>GeometryOperation</code> that checks if the specified <code>Geometry</code>s spatially cross
     */
    GeometryOperation<Boolean> createCrossesOp(final Geometry geometry, final Geometry other);

    /**
     * Creates an operation to check if the first specified <code>Geometry</code> spatially
     * contains the second.
     *
     * @param geometry the first <code>Geometry</code> operand of the containment test
     * @param other    the second <code>Geometry</code> operand of the containment test
     * @return a <code>GeometryOperation</code> that checks if the first specified <code>Geometry</code> spatially contains the second
     */
    GeometryOperation<Boolean> createContainsOp(final Geometry geometry, final Geometry other);

    /**
     * Creates an operation to check if the specified <code>Geometry</code>s overlap.
     *
     * @param geometry the first <code>Geometry</code> operand of the overlap test
     * @param other    the second <code>Geometry</code> operand of the overlap test
     * @return a <code>GeometryOperation</code> that checks if the specified <code>Geometry</code>s spatially overlap
     */
    GeometryOperation<Boolean> createOverlapsOp(final Geometry geometry, final Geometry other);

    /**
     * Creates an operation to check if the specified <code>Geometry</code>s are spatially related by testing
     * for intersections between the interior, boundary and exterior of the two geometric objects as specified by
     * the values in the intersection pattern matrix. This returns false if all the tested intersections are empty except
     * exterior (this) intersect exterior (another).
     *
     * @param geometry the first <code>Geometry</code> operand of the relate test
     * @param other    the second <code>Geometry</code> operand of the relate test
     * @param matrix   the intersection pattern matrix
     * @return a <code>GeometryOperation</code> that checks if this instance intersects the specifed other <code>Geometry</code>
     */
    GeometryOperation<Boolean> createRelateOp(final Geometry geometry, final Geometry other, final String matrix);

    /**
     * Creates an operation to calculate the <code>GeometryCollection</code> that matches the specified M-coordinate value.
     * <p/>
     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
     * <p>The semantics implemented here are specified by SFA 1.2.1, § 6.1.2.6.</p>
     *
     * @param geometry the geometry on which to perform the calculation
     * @param mValue   the specified M-coordinate value
     * @return a <code>GeometryOperation</code> that calculates the <code>GeometryCollection</code> matching
     *         the specified M-coordinate value.
     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
     */
    GeometryOperation<Geometry> createLocateAlongOp(final Geometry geometry, final double mValue);

    /**
     * Creates an operation to calculate the <code>GeometryCollection</code> that matches the specified range of M-coordinate value
     * inclusively.
     * <p/>
     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
     * <p>The semantics implemented here are specified by SFA 1.2.1, § 6.1.2.6.</p>
     *
     * @param geometry     the geometry on which to perform the calculation
     * @param startMeasure the start of the specified range of M-coordinate values
     * @param endMeasure   the end of the specified range of M-coordinate values
     * @return a <code>GeometryOperation</code> that calculates the <code>GeometryCollection</code> matching the
     *         specified range of M-coordinate values.
     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
     */
    GeometryOperation<Geometry> createLocateBetweenOp(final Geometry geometry, final double startMeasure, final double endMeasure);

    /**
     * Returns an operation to calculate the shortest distance between any two points in the two <code>Geometry</code>s in the
     * coordinate reference system of this <code>Geometry</code>. Only the X/Y-coordinates are used in the distance
     * calculation; M- and Z-coordinates are ignored.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>GeometryOperation</code> that calculates the shortest distance between the two specified <code>Geometries</code>
     */
    GeometryOperation<Double> createDistanceOp(final Geometry geometry, final Geometry other);

    /**
     * Returns an operation to calculate a <code>Geometry</code> that represents all points whose distance from the specified
     * <code>Geometry</code> is less than or equal the specified distance.
     * <p/>
     * <p>Calculations are in the <code>CoordinateReferenceSystem</code> of this
     * <code>Geometry</code>.</p>
     * <p>Z- or M-coordinates are ignored in the buffering operation; and the result will always be a 2D geometry.</p>
     *
     * @param geometry the <code>Geometry</code> for which to calculate the buffer
     * @param distance the buffer distance
     * @return a <code>GeometryOperation</code> that calculates a 2D <code>Geometry</code> representing the
     *         buffer of the specified <code>Geometry</code> with the specified distance.
     */
    GeometryOperation<Geometry> createBufferOp(final Geometry geometry, final double distance);

    /**
     * Returns an operation to calculate the convex hull of the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> for which to calculate the convex hull.
     * @return a <code>GeometryOperation</code> that calculates the convex hull for the specified <code>Geometry</code>.
     */
    GeometryOperation<Geometry> createConvexHullOp(final Geometry geometry);

    /**
     * Returns an operation to calculate the point set intersection of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>GeometryOperation</code> that calculates the point set intersection between the two specified <code>Geometries</code>
     */
    GeometryOperation<Geometry> createIntersectionOp(final Geometry geometry, final Geometry other);

    /**
     * Returns an operation to calculate the point set union of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>GeometryOperation</code> that calculates the point set union between the two specified <code>Geometries</code>
     */
    GeometryOperation<Geometry> createUnionOp(final Geometry geometry, final Geometry other);

    /**
     * Returns an operation to calculate the point set difference of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>GeometryOperation</code> that calculates the point set difference between the two specified <code>Geometries</code>
     */
    GeometryOperation<Geometry> createDifferenceOp(final Geometry geometry, final Geometry other);

    /**
     * Returns an operation to calculate the point set symmetric difference of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>GeometryOperation</code> that calculates the point set symmetric difference between the two specified <code>Geometries</code>
     */
    GeometryOperation<Geometry> createSymDifferenceOp(final Geometry geometry, final Geometry other);

    /**
     * Creates an operation to encode the specified <code>Geometry</code> to Well-Known Text (WKT).
     *
     * <p>Which "dialect" of WKT is used, is implementation defined. </p>
     *
     * @param geometry the <code>Geometry</code> to encode into WKT
     * @return An operation that encodes the specified <code>Geometry</code> to WKT
     */
    GeometryOperation<String> createToWktOp(final Geometry geometry);

    /**
      * Creates an operation to encode the specified <code>Geometry</code> to Well-Known Binary format (WKB).
      *
      * <p>Which "dialect" of WKB is used, is implementation defined. </p>
      *
      * @param geometry the <code>Geometry</code> to encode to WKB
      * @return An operation that encodes the specified <code>Geometry</code> to WKB
      */
     GeometryOperation<ByteBuffer> createToWkbOp(final Geometry geometry);
}
