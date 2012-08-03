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

import org.geolatte.geom.crs.CrsId;

import java.io.Serializable;

/**
 * The base class for <code>Geometry</code>s.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */

//TODO: explain topological relations in the class javadoc

public abstract class Geometry implements Serializable {

    private final static GeometryEquality geomEq = new GeometryPointEquality();

    private final CrsId crsId;

    private final GeometryOperations geometryOperations;


    /**
     * Collects all PointSets in the Geometry array into a (complex) PointCollection.
     *
     * <p>This implementation assumes that the array does not contain NULL values. This condition
     * should be tested before constructing the PointCollection.</p>
     * @param geometries
     * @return
     */
    protected static PointCollection collectPointSets(Geometry[] geometries) {
        if (geometries == null || geometries.length == 0) return EmptyPointSequence.INSTANCE;

        PointCollection[] collections = new PointCollection[geometries.length];
        for (int i = 0; i < collections.length; i++){
            assert(geometries[i] != null);
            collections[i] = geometries[i].getPoints();
        }
        return new NestedPointCollection(collections);
    }

    protected Geometry(CrsId crsId, GeometryOperations geometryOperations) {
        this.crsId = (crsId == null ? CrsId.UNDEFINED : crsId);
        this.geometryOperations = (geometryOperations == null ? new JTSGeometryOperations() : geometryOperations);
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
        return getPoints().getCoordinateDimension();
    }

    /**
     * Returns the reference to the coordinate reference system of this <code>Geometry</code>
     *
     * @return
     */
    public CrsId getCrsId() {
        return crsId;
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
        return crsId.getCode();
    }

    /**
     * Tests whether this <code>Geometry</code> has Z-coordinates.
     *
     * @return true if this instance has Z-coordinates.
     */
    public boolean is3D() {
        return getPoints().is3D();
    }

    /**
     * Returns the <code>DimensionalFlag</code> of the Geometry
     *
     * @return the <code>DimensionalFlag</code> of its <code>PointSequence</code>
     */
    public DimensionalFlag getDimensionalFlag() {
        return getPoints().getDimensionalFlag();
    }

    /**
     * Tests  whether this <code>Geometry</code> has M-coordinates.
     *
     * @return true if this instance has M-coordinates.
     */
    public boolean isMeasured() {
        return getPoints().isMeasured();
    }

    /**
     * Tests whether this <code>Geometry</code> corresponds to the empty set.
     *
     * @return
     */
    public boolean isEmpty() {
        return this.getPoints().isEmpty();
    }

    /**
     * Returns the number of points in the <code>PointCollection</code> of this <code>Geometry</code>.
     *
     * @return
     */
    public int getNumPoints() {
        return getPoints().size();
    }

    /**
     * Returns the point at the specified index in the <code>PointCollection</code> of this <code>Geometry</code>.
     *
     * @param index the position in the <code>PointSequence</code> (first point is at index 0).
     * @return
     */
    public Point getPointN(int index) {
        if (index >= getPoints().size()) {
            throw new IndexOutOfBoundsException();
        }
        double[] coords = new double[getCoordinateDimension()];
        getPoints().getCoordinates(coords, index);
        return Points.create(coords, DimensionalFlag.valueOf(is3D(), isMeasured()), getCrsId());
    }

    /**
     * Extracts the first <code>CrsId</code> from an array of <code>Geometry</code>s if
     * the array is non-null and not empty. Otherwise returns <code>CrsId.UNDEFINED</code>.
     */
    protected static CrsId getCrsId(Geometry[] geometries) {
        if (geometries == null || geometries.length == 0 || geometries[0] == null) {
            return CrsId.UNDEFINED;
        }
        return geometries[0].getCrsId();
    }

    /**
     * Extracts the first <code>GeometryOperations</code> from an array of <code>Geometry</code>s if
     * the array is non-null and not empty. Otherwise returns <code>Null</code>.
     */
    protected static GeometryOperations getGeometryOperations(Geometry[] geometries) {
        if (geometries == null || geometries.length == 0) {
            return null;
        }
        return geometries[0].getGeometryOperations();
    }


    /**
     * Returns the <code>PointCollection</code> that is associated with this instance
     *
     * @return
     */
    public abstract PointCollection getPoints();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !Geometry.class.isAssignableFrom(o.getClass())) return false;

        Geometry otherGeometry = (Geometry) o;
        return geomEq.equals(this, otherGeometry);
    }

    @Override
    public int hashCode() {
        int result = crsId != null ? crsId.hashCode() : 0;
        result = 31 * result + this.getPoints().hashCode();
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
    public Geometry getBoundary() {
        GeometryOperation<Geometry> operation = getGeometryOperations().createBoundaryOp(this);
        return operation.execute();
    }

    /**
     * Returns the <code>Envelope</code>, or minimum bounding box, for this <code>Geometry</code>.
     *
     * @return
     */
    public Envelope getEnvelope() {
        GeometryOperation<Envelope> operation = getGeometryOperations().createEnvelopeOp(this);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> is spatially disjoint from the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance is disjoint from other
     */
    public boolean disjoint(Geometry other) {
        return !intersects(other);
    }

    /**
     * Tests whether this <code>Geometry</code> spatially intersects the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance intersects the specifed other <code>Geometry</code>
     */
    public boolean intersects(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createIntersectsOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> spatially touches the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance touches the specifed other <code>Geometry</code>
     */
    public boolean touches(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createTouchesOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> spatially crosses the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance crosses the specifed other <code>Geometry</code>
     */
    public boolean crosses(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createCrossesOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> is spatially within the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance is spatially within the specifed other <code>Geometry</code>
     */
    public boolean within(Geometry other) {
        return other.contains(this);
    }

    /**
     * Tests whether this <code>Geometry</code> spatially contains the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance contains the specifed other <code>Geometry</code>
     */
    public boolean contains(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createContainsOp(this, other);
        return operation.execute();
    }

    /**
     * Tests whether this <code>Geometry</code> spatially overlaps the specified <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to test against
     * @return true if this instance overlaps the specifed other <code>Geometry</code>
     */
    public boolean overlaps(Geometry other) {
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
    public boolean relate(Geometry other, String matrix) {
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
    public Geometry locateAlong(double mValue) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createLocateAlongOp(this, mValue);
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
    public Geometry locateBetween(double mStart, double mEnd) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createLocateBetweenOp(this, mStart, mEnd);
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
    public double distance(Geometry other) {
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
    public Geometry buffer(double distance) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createBufferOp(this, distance);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the convex hull of this <code>Geometry</code>.
     *
     * @return the convex hull of this instance.
     */
    public Geometry convexHull() {
        GeometryOperation<Geometry> operation = getGeometryOperations().createConvexHullOp(this);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the point set intersection of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to intersect with
     * @return a <code>Geometry</code> representing the point set intersection
     */
    public Geometry intersection(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createIntersectionOp(this, other);
        return operation.execute();
    }

    /**
     * Returns the <code>Geometry</code> that represents the point set union of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     *
     * @param other the <code>Geometry</code> to union with
     * @return a <code>Geometry</code> representing the point set union.
     */
    public Geometry union(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createUnionOp(this, other);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the point set difference of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     * @param other the <code>Geometry</code> with which to calculate the difference
     * @return a <code>Geometry</code> representing the point set difference.
     */
    public Geometry difference(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createDifferenceOp(this, other);
        return operation.execute();
    }

    /**
     * Returns a <code>Geometry</code> that represents the point set symmetric difference of this <code>Geometry</code> with the
     * specified other <code>Geometry</code>.
     * @param other the <code>Geometry</code> with which to calculate the symmetric difference
     * @return a <code>Geometry</code> representing the point set symmetric difference.

     */
    public Geometry symDifference(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createSymDifferenceOp(this, other);
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
     *
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
    public abstract void accept(GeometryVisitor visitor);

    /**
     * Returns the <code>GeometryOperations</code> instance used by this instance.
     *
     * @return
     */
    protected GeometryOperations getGeometryOperations() {
        return this.geometryOperations;
    }
}
