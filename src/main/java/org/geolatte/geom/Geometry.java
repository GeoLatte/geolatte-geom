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

import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * The base class for <code>Geometry</code>s.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */

//TODO: explain topological relations in the class javadoc

public abstract class Geometry<P extends Position<P>> implements Serializable {

    private static GeometryEquality geomEq = new GeometryPointEquality();

    private final GeometryOperations<P> geometryOperations;

    protected final PositionSequence<P> positions;

    @SuppressWarnings("unchecked")
    public static <Q extends Position<Q>> Geometry<Q> forceToCrs(Geometry<?> geometry, CoordinateReferenceSystem<Q> crs) {
        if (crs == null || geometry == null) return (Geometry<Q>)geometry;
        if (crs.equals(geometry.getCoordinateReferenceSystem())) return (Geometry<Q>)geometry;
        if (geometry instanceof Simple) {
            Simple simple = (Simple)geometry;
            PositionSequence<Q> positions = Positions.copy(geometry.getPositions(), crs);
            return Geometries.mkGeometry(simple.getClass(), positions);
        }else {
            Complex<?,?> complex = (Complex<?,?>)geometry;
            if (complex.getNumGeometries() == 0) {
                return Geometries.mkGeometry(complex.getClass(), crs);
            }
            Geometry<Q>[] targetParts = (Geometry<Q>[])Array.newInstance(complex.getComponentType(), complex.getNumGeometries());//new Geometry[complex.getNumGeometries()];
            int idx = 0;
            for (Geometry<?> part: complex) {
                targetParts[idx++] = forceToCrs(part, crs);
            }
            return Geometries.mkGeometry(complex.getClass(), targetParts);
        }
    }

    /**
     * Creates an empty Geometry
     * @param crs  the CoordinateReferenceSystem to use
     */
    protected Geometry(CoordinateReferenceSystem<P> crs){
        this.positions = PositionSequenceBuilders.fixedSized(0, crs).toPositionSequence();
        this.geometryOperations = DefaultGeometryOperationsFactory.getOperations(getPositionClass());
    }

    protected Geometry(PositionSequence<P> positions, GeometryOperations<P> geometryOperations) {
        if (positions == null) throw new IllegalArgumentException("Null Positions argument not allowd.");
        this.positions = positions;
        this.geometryOperations = geometryOperations != null ?
                geometryOperations :
                DefaultGeometryOperationsFactory.getOperations(getPositionClass());
    }

    // Helper method to extract first Ops object from an array of Geometries.
    //TODO -- check that all geoms in array have the SAME geomOps
    protected static <T extends Position<T>> GeometryOperations<T> getGeometryOperations(Geometry<T>[] geometries) {
        if (geometries == null || geometries.length == 0) {
            return null;
        }
        return geometries[0].getGeometryOperations();
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Position<T>> PositionSequence<T> nestPositionSequences(Geometry<T>[] geometries) {
        if (geometries == null || geometries.length == 0) {
            return null;
        }
        PositionSequence<T>[] sequences = (PositionSequence<T>[]) (new PositionSequence[geometries.length]);
        int i = 0;
        for (Geometry<T> g : geometries) {
            sequences[i++] = g.getPositions();
        }
        return new NestedPositionSequence<T>(sequences);
    }

    //TODO -- check that all geoms have the SAME CRS
    @SuppressWarnings("unchecked")
    protected static <T extends Position<T>> CoordinateReferenceSystem<T> getCrs(Geometry<T>[] geometries) {
        if (geometries == null || geometries.length == 0) {
            throw new IllegalArgumentException("Expecting non-null, non-empty array of Geometry.");
        }
        return geometries[0].getCoordinateReferenceSystem();
    }


    /**
     * Returns the coordinate dimension of this <code>Geometry</code>
     * <p/>
     * <p>The coordinate dimension is the number of components in the coordinates of the points in
     * this <code>Geometry</code>. </p>
     *
     * @return the coordinate dimension
     */
    public int getCoordinateDimension() {
        return getPositions().getCoordinateDimension();
    }

    /**
     * Returns the coordinate reference system of this <code>Geometry</code>
     *
     * @return
     */
    public CoordinateReferenceSystem<P> getCoordinateReferenceSystem() {
        return getPositions().getCoordinateReferenceSystem();
    }

    /**
     * Returns the numeric identifier of the coordinate reference system of this <code>Geometry</code>.
     * <p/>
     * <p>A SRID is usually interpreted as meaning the EPSG-code for the coordinate reference system. In this
     * implementation, this is not enforced.</p>
     *
     * @return
     */
    public int getSRID() {
        return getCoordinateReferenceSystem().getCrsId().getCode();
    }

    /**
     * Tests whether this <code>Geometry</code> corresponds to the empty set.
     *
     * @return
     */
    public boolean isEmpty() {
        return this.getPositions().isEmpty();
    }

    /**
     * Returns the number of points in the <code>PointSequence</code> of this <code>Geometry</code>.
     *
     * @return
     */
    public int getNumPositions() {
        return getPositions().size();
    }

    public Class<P> getPositionClass() {
        return getPositions().getCoordinateReferenceSystem().getPositionClass();
    }

    /**
     * Returns the position at the specified index in the <code>PointSequence</code> of this <code>Geometry</code>.
     *
     * @param index the position in the <code>PointSequence</code> (first point is at index 0).
     * @return
     */
    public P getPositionN(int index) {
        if (index >= getPositions().size()) {
            throw new IndexOutOfBoundsException();
        }
        double[] coords = new double[getCoordinateDimension()];
        getPositions().getCoordinates(index, coords);
        return Positions.mkPosition(getCoordinateReferenceSystem(), coords);
    }

    /**
     * Returns the <code>PositionSequence</code> of this instance
     *
     * @return
     */
    public PositionSequence<P> getPositions() {
        return this.positions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !Geometry.class.isAssignableFrom(o.getClass())) return false;
        if (!this.getPositionClass().equals(((Geometry) o).getPositionClass())) return false;
        Geometry<P> otherGeometry = (Geometry<P>) o; //safe cast because we first check for position class equality
        return geomEq.equals(this, otherGeometry);
    }

    @Override
    public int hashCode() {
        int result = getGeometryType().hashCode();
        result = 31 * result + this.getPositions().hashCode();
        return result;
    }

    /**
     * Returns the type of this <code>Geometry</code>.
     *
     * @return the <code>GeometryType</code> of this instance.
     */
    public abstract GeometryType getGeometryType();

    /**
     * Tests if this <code>Geometry</code> is simple; i.e. has no anomalous geometric points such as
     * self-intersections or self-tangency.
     *
     * @return
     */
    public boolean isSimple() {
        GeometryOperation<Boolean> op = getGeometryOperations().createIsSimpleOp(this);
        return op.execute();
    }

    /**
     * Returns the boundary of this <code>Geometry</code>.
     *
     * @return
     */
    public Geometry<P> getBoundary() {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createBoundaryOp(this);
        return operation.execute();
    }

    /**
     * Returns the <code>Envelope</code>, or minimum bounding box, for this <code>Geometry</code>.
     *
     * @return
     */
    public Envelope<P> getEnvelope() {
        GeometryOperation<Envelope<P>> operation = getGeometryOperations().createEnvelopeOp(this);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> is spatially disjoint from the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance is disjoint from other
     */
    public boolean disjoint(Geometry<P> other) {
        return !intersects(other);
    }

    /**
     * Tests whether this <code>Geometry</code> spatially intersects the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance intersects the specifed other <code>Geometry</code>
     */
    public boolean intersects(Geometry<P> other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createIntersectsOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> spatially touches the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance touches the specifed other <code>Geometry</code>
     */
    public boolean touches(Geometry<P> other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createTouchesOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> spatially crosses the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance crosses the specifed other <code>Geometry</code>
     */
    public boolean crosses(Geometry<P> other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createCrossesOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> is spatially within the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance is spatially within the specifed other <code>Geometry</code>
     */
    public boolean within(Geometry<P> other) {
        return other.contains(this);
    }

    /**
     * Tests whether this <code>Geometry</code> spatially contains the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance contains the specifed other <code>Geometry</code>
     */
    public boolean contains(Geometry<P> other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createContainsOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> spatially overlaps the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance overlaps the specifed other <code>Geometry</code>
     */
    public boolean overlaps(Geometry<P> other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createOverlapsOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> is spatially related to the specified <code>Geometry</code> by testing
     * for intersections between the interior, boundary and exterior of the two geometric objects as specified by
     * the values in the intersection pattern matrix. This returns false if all the tested intersections are empty except
     * exterior (this) intersect exterior (another).
     *
     * @param other  the <code>Geometry</code> to test against
     * @param matrix the intersection pattern matrix
     * @return true if this instance intersects the specifed other <code>Geometry</code>
     */
    public boolean relate(Geometry<P> other, String matrix) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createRelateOp(this, other, matrix);
        return operation.execute();
    }

    /**
     * Returns a derived <code>GeometryCollection</code> value that matches the specified M-coordinate value.
     * <p/>
     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
     * <p>The semantics implemented here are specified by SFA 1.2.1, § 6.1.2.6.</p>
     *
     * @param mValue the specified M-coordinate value
     * @return a <code>GeometryCollection</code> matching the specified M-value.
     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
     */
    public Geometry<P> locateAlong(double mValue) {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createLocateAlongOp(this, mValue);
        return operation.execute();
    }

    /**
     * Returns a derived <code>GeometryCollection</code> value that matches the specified range of M-coordinate values
     * inclusively.
     * <p/>
     * <p>This method is only valid if executed on 0- or 1-dimensional objects or collections thereof.</p>
     * <p>The semantics implemented here are specified by SFA 1.2.1, § 6.1.2.6.</p>
     *
     * @param mStart the start of the range of M-coordinate values
     * @param mEnd   the end of the range of M-coordinate values
     * @throws IllegalArgumentException if this method is executed on 2-dimensional <code>Geometry</code>s.
     */
    public Geometry<P> locateBetween(double mStart, double mEnd) {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createLocateBetweenOp(this, mStart, mEnd);
        return operation.execute();
    }

    /**
     * Returns the shortest distance between any two points in the two <code>Geometry</code>s as calculated in the
     * coordinate reference system of this <code>Geometry</code>. Only the X/Y-coordinates are used in the distance
     * calculation; M- and Z-coordinates are ignored.
     * <p/>
     * <p>The current implementation assumes that both <code>Geometry</code>s are in a Cartesian coordinate
     * reference system. Using this method on <code>Geometry</code>s in a geocentric or geographic coordinate reference
     * system returns a meaningless value.</p>
     *
     * @param other the <code>Geometry</code> to which the min. distance is calculated.
     * @return the distance between this and the specified other <code>Geometry</code>.
     */
    public double distance(Geometry<P> other) {
        GeometryOperation<Double> operation = getGeometryOperations().createDistanceOp(this, other);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents all points whose distance from this <code>Geometry</code> is less
     * than or equal the specified distance. Calculations are in the <code>CoordinateReferenceSystem</code> of this
     * <code>Geometry</code>.
     * <p/>
     * <p>Z- or M-coordinates are ignored in the buffering operation; and the result will always be a 2D geometry.</p>
     *
     * @param distance the buffer distance
     * @return a 2D <code>Geometry</code> representing this object buffered with the specified distance.
     */
    public Geometry<P> buffer(double distance) {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createBufferOp(this, distance);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the convex hull of this <code>Geometry</code>.
     *
     * @return the convex hull of this instance.
     */
    public Geometry<P> convexHull() {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createConvexHullOp(this);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the point set intersection of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to intersect with
     * @return a <code>Geometry</code> representing the point set intersection
     */
    public Geometry<P> intersection(Geometry<P> other) {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createIntersectionOp(this, other);
        return operation.execute();
    }

    /**
     * Returns the <code>Geometry</code> that represents the point set union of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to union with
     * @return a <code>Geometry</code> representing the point set union.
     */
    public Geometry<P> union(Geometry<P> other) {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createUnionOp(this, other);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the point set difference of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> with which to calculate the difference
     * @return a <code>Geometry</code> representing the point set difference.
     */
    public Geometry<P> difference(Geometry<P> other) {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createDifferenceOp(this, other);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the point set symmetric difference of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> with which to calculate the symmetric difference
     * @return a <code>Geometry</code> representing the point set symmetric difference.
     */
    public Geometry<P> symDifference(Geometry<P> other) {
        GeometryOperation<Geometry<P>> operation = getGeometryOperations().createSymDifferenceOp(this, other);
        return operation.execute();
    }

    /**
     * Returns a Well-Known Text (WKT) representation of this <code>Geometry</code>.
     *
     * @return a Well-Known Text (WKT) representation of this <code>Geometry</code>.
     */
    public String asText() {
        GeometryOperation<String> operation = getGeometryOperations().createToWktOp(this);
        return operation.execute();
    }

    /**
     * Returns a Well-Known Binary (WKB) representation of this <code>Geometry</code>.
     *
     * @return a byte array containt the WKB of this <code>Geometry</code>.
     */
    public byte[] asBinary() {
        GeometryOperation<ByteBuffer> operation = getGeometryOperations().createToWkbOp(this);
        return operation.execute().toByteArray();
    }

    /**
     * Returns the Well-Known Text (WKT) representation of this <code>Geometry</code>.
     * <p/>
     * <p>This method is synonymous with {@link #asText()}. </p>
     *
     * @return
     */
    public String toString() {
        return asText();
    }

    /**
     * Returns the topological dimension of this instance. In non-homogenous collections, this will return the largest
     * topological dimension of the contained <code>Geometries</code>.
     *
     * @return
     */
    public abstract int getDimension();

    /**
     * Accepts a <code>GeometryVisitor</code>.
     * <p>If this <code>Geometry</code> instance is a <code>GeometryCollection</code> then it will pass the
     * visitor to its contained <code>Geometries</code>.</p>
     *
     * @param visitor
     */
    public abstract void accept(GeometryVisitor<P> visitor);

    /**
     * Returns the <code>GeometryOperations</code> instance used by this instance.
     *
     * @return
     */
    public GeometryOperations<P> getGeometryOperations() {
        return this.geometryOperations;
    }
}
