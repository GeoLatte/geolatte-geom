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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

/**
 * Encapsulates common operations on {@code Geometry}s in a projected Coordinate reference system.
 *
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 7/7/14
 */
public class DefaultProjectedGeometryOps<P extends Projected<P>> {

    final private ProjectedGeometryOperations<P> factory = new JTSGeometryOperations<>();


    /**
     * Tests if the specified <code>Geometry</code> is simple; i.e. has no anomalous geometric points such as
     * self-intersections or self-tangency.
     *
     * @param geometry the operand {@code Geometry}
     * @return
     */
    public boolean isSimple(Geometry<P> geometry) {
        GeometryOperation<Boolean> op = factory().createIsSimpleOp(geometry);
        return op.execute();
    }

    /**
     * Returns the boundary of the <code>Geometry</code>.
     *
     * @param geometry the operand {@code Geometry}
     * @return
     */
    public Geometry<P> getBoundary(Geometry<P> geometry) {
        GeometryOperation<Geometry<P>> operation = factory().createBoundaryOp(geometry);
        return operation.execute();
    }


    /**
     * Tests whether the <code>Geometry</code> is spatially disjoint from the specified <code>Geometry</code>.
     *
     * @param geometry the first operand {@code Geometry}
     * @param other    the second operand {@code Geometry}
     * @return true if the instance is disjoint from other
     */
    public boolean disjoint(Geometry<P> geometry, Geometry<P> other) {
        return !intersects(geometry, other);
    }

    /**
     * Tests whether the <code>Geometry</code> spatially intersects the specified <code>Geometry</code>.
     *
     * @param geometry the first operand {@code Geometry}
     * @param other    the second operand {@code Geometry}
     * @return true if the instance intersects the specifed other <code>Geometry</code>
     */
    public boolean intersects(Geometry<P> geometry, Geometry<P> other) {
        GeometryOperation<Boolean> operation = factory().createIntersectsOp(geometry, other);
        return operation.execute();
    }

    /**
     * Tests whether the <code>Geometry</code> spatially touches the specified <code>Geometry</code>.
     *
     * @param geometry the first operand {@code Geometry}
     * @param other    the second operand {@code Geometry}
     * @return true if the instance touches the specifed other <code>Geometry</code>
     */
    public boolean touches(Geometry<P> geometry, Geometry<P> other) {
        GeometryOperation<Boolean> operation = factory().createTouchesOp(geometry, other);
        return operation.execute();
    }

//    /**
//     * Tests whether the <code>Geometry</code> spatially crosses the specified <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> to test against
//     * @return true if the instance crosses the specifed other <code>Geometry</code>
//     */
//    public boolean crosses(Geometry<P> other) {
//        GeometryOperation<Boolean> operation = factory().createCrossesOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Tests whether the <code>Geometry</code> is spatially within the specified <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> to test against
//     * @return true if the instance is spatially within the specifed other <code>Geometry</code>
//     */
//    public boolean within(Geometry<P> other) {
//        return other.contains(this);
//    }
//
//    /**
//     * Tests whether the <code>Geometry</code> spatially contains the specified <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> to test against
//     * @return true if the instance contains the specifed other <code>Geometry</code>
//     */
//    public boolean contains(Geometry<P> other) {
//        GeometryOperation<Boolean> operation = factory().createContainsOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Tests whether the <code>Geometry</code> spatially overlaps the specified <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> to test against
//     * @return true if the instance overlaps the specifed other <code>Geometry</code>
//     */
//    public boolean overlaps(Geometry<P> other) {
//        GeometryOperation<Boolean> operation = factory().createOverlapsOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Tests whether the <code>Geometry</code> is spatially related to the specified <code>Geometry</code> by testing
//     * for intersections between the interior, boundary and exterior of the two geometric objects as specified by
//     * the values in the intersection pattern matrix. This returns false if all the tested intersections are empty except
//     * exterior (this) intersect exterior (another).
//     *
//     * @param other  the <code>Geometry</code> to test against
//     * @param matrix the intersection pattern matrix
//     * @return true if the instance intersects the specifed other <code>Geometry</code>
//     */
//    public boolean relate(Geometry<P> other, String matrix) {
//        GeometryOperation<Boolean> operation = factory().createRelateOp(this, other, matrix);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a derived <code>GeometryCollection</code> value that matches the specified M-coordinate value.
//     * <p/>
//     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
//     * <p>The semantics implemented here are specified by SFA 1.2.1, § 6.1.2.6.</p>
//     *
//     * @param mValue the specified M-coordinate value
//     * @return a <code>GeometryCollection</code> matching the specified M-value.
//     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
//     */
//    public Geometry<P> locateAlong(double mValue) {
//        GeometryOperation<Geometry<P>> operation = factory().createLocateAlongOp(this, mValue);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a derived <code>GeometryCollection</code> value that matches the specified range of M-coordinate values
//     * inclusively.
//     * <p/>
//     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
//     * <p>The semantics implemented here are specified by SFA 1.2.1, § 6.1.2.6.</p>
//     *
//     * @param mStart the start of the range of M-coordinate values
//     * @param mEnd   the end of the range of M-coordinate values
//     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
//     */
//    public Geometry<P> locateBetween(double mStart, double mEnd) {
//        GeometryOperation<Geometry<P>> operation = factory().createLocateBetweenOp(this, mStart, mEnd);
//        return operation.execute();
//    }
//
//    /**
//     * Returns the shortest distance between any two points in the two <code>Geometry</code>s as calculated in the
//     * coordinate reference system of the <code>Geometry</code>. Only the X/Y-coordinates are used in the distance
//     * calculation; M- and Z-coordinates are ignored.
//     * <p/>
//     * <p>The current implementation assumes that both <code>Geometry</code>s are in a Cartesian coordinate
//     * reference system. Using this method on <code>Geometry</code>s in a geocentric or geographic coordinate reference
//     * system returns a meaningless value.</p>
//     *
//     * @param other the <code>Geometry</code> to which the min. distance is calculated.
//     * @return the distance between the and the specified other <code>Geometry</code>.
//     */
//    public double distance(Geometry<P> other) {
//        GeometryOperation<Double> operation = factory().createDistanceOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a <code>Geometry</code> that represents all points whose distance from the <code>Geometry</code> is less
//     * than or equal the specified distance. Calculations are in the <code>CoordinateReferenceSystem</code> of this
//     * <code>Geometry</code>.
//     * <p/>
//     * <p>Z- or M-coordinates are ignored in the buffering operation; and the result will always be a 2D geometry.</p>
//     *
//     * @param distance the buffer distance
//     * @return a 2D <code>Geometry</code> representing the object buffered with the specified distance.
//     */
//    public Geometry<P> buffer(double distance) {
//        GeometryOperation<Geometry<P>> operation = factory().createBufferOp(this, distance);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a <code>Geometry</code> that represents the convex hull of the <code>Geometry</code>.
//     *
//     * @return the convex hull of the instance.
//     */
//    public Geometry<P> convexHull() {
//        GeometryOperation<Geometry<P>> operation = factory().createConvexHullOp(this);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a <code>Geometry</code> that represents the point set intersection of the <code>Geometry</code> with the
//     * specified other <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> to intersect with
//     * @return a <code>Geometry</code> representing the point set intersection
//     */
//    public Geometry<P> intersection(Geometry<P> other) {
//        GeometryOperation<Geometry<P>> operation = factory().createIntersectionOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Returns the <code>Geometry</code> that represents the point set union of the <code>Geometry</code> with the
//     * specified other <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> to union with
//     * @return a <code>Geometry</code> representing the point set union.
//     */
//    public Geometry<P> union(Geometry<P> other) {
//        GeometryOperation<Geometry<P>> operation = factory().createUnionOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a <code>Geometry</code> that represents the point set difference of the <code>Geometry</code> with the
//     * specified other <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> with which to calculate the difference
//     * @return a <code>Geometry</code> representing the point set difference.
//     */
//    public Geometry<P> difference(Geometry<P> other) {
//        GeometryOperation<Geometry<P>> operation = factory().createDifferenceOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a <code>Geometry</code> that represents the point set symmetric difference of the <code>Geometry</code> with the
//     * specified other <code>Geometry</code>.
//     *
//     * @param other the <code>Geometry</code> with which to calculate the symmetric difference
//     * @return a <code>Geometry</code> representing the point set symmetric difference.
//     */
//    public Geometry<P> symDifference(Geometry<P> other) {
//        GeometryOperation<Geometry<P>> operation = factory().createSymDifferenceOp(this, other);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a Well-Known Text (WKT) representation of the <code>Geometry</code>.
//     *
//     * @return a Well-Known Text (WKT) representation of the <code>Geometry</code>.
//     */
//    public String asText() {
//        GeometryOperation<String> operation = factory().createToWktOp(this);
//        return operation.execute();
//    }
//
//    /**
//     * Returns a Well-Known Binary (WKB) representation of the <code>Geometry</code>.
//     *
//     * @return a byte array containt the WKB of the <code>Geometry</code>.
//     */
//    public byte[] asBinary() {
//        GeometryOperation<ByteBuffer> operation = factory().createToWkbOp(this);
//        return operation.execute().toByteArray();
//    }
//
    public ProjectedGeometryOperations<P> factory() {
        return factory;
    }

}
