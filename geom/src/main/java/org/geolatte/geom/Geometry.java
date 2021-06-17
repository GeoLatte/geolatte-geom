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

import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.WkbEncoder;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;

import static java.lang.String.format;

/**
 * The base class for <code>Geometry</code>s.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */

public abstract class Geometry<P extends Position> implements Serializable {

    private static final long serialVersionUID = 6884205871950410216L;
    private static final GeometryEquality geomEq = new GeometryPositionEquality();

    private final PositionSequence<P> positions;
    private final CoordinateReferenceSystem<P> crs;


    /**
     * Creates a new {@code Geometry} with the positions from the specified geometry and having the specified
     * {@code CoordinateReferenceSystem}.
     *
     * When forcing from a smaller to a larger coordinate dimension, the coordinateValue specified will be used
     * for filling the missing coordinate values.
     *
     * @param geometry source {@code Geometry} that supplies the {@code Position}s for the returned {@code Geometry}
     * @param crs the {@code CoordinateReferenceSystem} for the returned {@code Geometry}
     * @param coordinateValue the default to fill in when for missing coordinate values (if any)
     * @param <Q> the {@code Position} type for the returned Geometry*
     * @return a {@code Geometry} with the positions of the specified geometry and having
     * the specified {@code CoordinateReferenceSystem}
     */
    @SuppressWarnings("unchecked")
    public static <Q extends Position> Geometry<Q> forceToCrs(Geometry<?> geometry, CoordinateReferenceSystem<Q> crs, double coordinateValue) {
        if (crs == null || geometry == null) return (Geometry<Q>) geometry;
        if (crs.equals(geometry.getCoordinateReferenceSystem())) return (Geometry<Q>) geometry;
        if (geometry instanceof Simple) {
            Simple simple = (Simple) geometry;
            PositionSequence<Q> positions = Positions.copy(geometry.getPositions(), crs.getPositionClass(), coordinateValue);
            return Geometries.mkGeometry(simple.getClass(), positions, crs);
        } else {
            Complex<?, ?> complex = (Complex<?, ?>) geometry;
            if (complex.getNumGeometries() == 0) {
                return Geometries.mkGeometry(complex.getClass(), crs);
            }
            Geometry<Q>[] targetParts = (Geometry<Q>[]) Array.newInstance(
                    complex.getComponentType(), complex.getNumGeometries()
            );
            int idx = 0;
            for (Geometry<?> part : complex) {
                targetParts[idx++] = forceToCrs(part, crs);
            }
            return Geometries.mkGeometry(complex.getClass(), targetParts);
        }
    }

    /**
     * Creates a new {@code Geometry} with the positions from the specified geometry and having the specified
     * {@code CoordinateReferenceSystem}.
     *
     * When forcing from a smaller to a larger coordinate dimension, the NaN will be used for the missing coordinate
     * values.
     *
     * @param geometry source {@code Geometry} that supplies the {@code Position}s for the returned {@code Geometry}
     * @param crs the {@code CoordinateReferenceSystem} for the returned {@code Geometry}
     * @param <Q> the {@code Position} type for the returned Geometry*
     * @return a {@code Geometry} with the positions of the specified geometry and having
     * the specified {@code CoordinateReferenceSystem}
     */
    public static <Q extends Position> Geometry<Q> forceToCrs(Geometry<?> geometry, CoordinateReferenceSystem<Q> crs) {
        return forceToCrs(geometry, crs, Double.NaN);
    }

    /**
     * Creates an empty Geometry
     *
     * @param crs the CoordinateReferenceSystem to use
     */
    protected Geometry(CoordinateReferenceSystem<P> crs) {
        if (crs == null) throw new IllegalArgumentException("Received null CRS argument");
        this.crs = crs;
        this.positions = PositionSequenceBuilders.fixedSized(0, crs.getPositionClass()).toPositionSequence();
    }

    protected Geometry(PositionSequence<P> positions, CoordinateReferenceSystem<P> crs) {
        if (crs == null) throw new IllegalArgumentException("Received null CRS argument");
        if (positions == null) throw new IllegalArgumentException("Null Positions argument not allowd.");
        this.crs = crs;
        this.positions = positions;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Position> PositionSequence<T> nestPositionSequences(Geometry<T>[] geometries) {
        if (geometries == null || geometries.length == 0) {
            return new NestedPositionSequence<>((PositionSequence<T>[])new PositionSequence[0]);
        }
        PositionSequence<T>[] sequences = (PositionSequence<T>[]) (new PositionSequence[geometries.length]);
        int i = 0;
        for (Geometry<T> g : geometries) {
            sequences[i++] = g.getPositions();
        }
        return new NestedPositionSequence<>(sequences);
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Position> CoordinateReferenceSystem<T> getCrs(Geometry<T>[] geometries) {
        if (geometries == null || geometries.length == 0) {
            return (CoordinateReferenceSystem<T>)CrsRegistry.getCoordinateReferenceSystemForEPSG(-1,
                    CoordinateReferenceSystems.PROJECTED_2D_METER);
        }
        return geometries[0].getCoordinateReferenceSystem();
    }

    /**
     * Returns the coordinate dimension of this <code>Geometry</code>
     *
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
     * @return the coordinate reference system of this <code>Geometry</code>
     */
    public CoordinateReferenceSystem<P> getCoordinateReferenceSystem() {
        return this.crs;
    }

    /**
     * Returns the numeric identifier of the coordinate reference system of this <code>Geometry</code>.
     *
     * <p>A SRID is usually interpreted as meaning the EPSG-code for the coordinate reference system. In this
     * implementation, this is not enforced.</p>
     *
     * @return the numeric identifier of the coordinate reference system of this <code>Geometry</code>.
     */
    public int getSRID() {
        return getCoordinateReferenceSystem().getCrsId().getCode();
    }

    /**
     * Tests whether this <code>Geometry</code> corresponds to the empty set.
     *
     * @return true if this <code>Geometry</code> corresponds to the empty set, false otherwise.
     */
    public boolean isEmpty() {
        return this.getPositions().isEmpty();
    }

    /**
     * Returns the number of positions in the <code>PositionSequence</code> of this <code>Geometry</code>.
     *
     * @return the number of positions in the <code>PositionSequence</code> of this <code>Geometry</code>.
     */
    public int getNumPositions() {
        return getPositions().size();
    }

    public Class<P> getPositionClass() {
        return !getPositions().isEmpty() ?
                getPositions().getPositionClass() :
                this.crs.getPositionClass();
    }

    /**
     * Returns the position at the specified index in the <code>PositionSequence</code> of this <code>Geometry</code>.
     *
     * @param index the position in the <code>PositionSequence</code> (first point is at index 0).
     * @return the position at the specified index in the <code>PositionSequence</code> of this <code>Geometry</code>.
     */
    public P getPositionN(int index) {
        if (index >= getPositions().size()) {
            throw new IndexOutOfBoundsException();
        }
        return getPositions().getPositionN(index);
    }

    /**
     * Checks whether a cast should succeed
     * @param castToType
     * @param <Q>
     */
    protected <Q extends Position> void checkCast(Class<Q> castToType) {
        if (! castToType.isAssignableFrom(getPositionClass()) ) {
            throw new ClassCastException(format("Can't cast a %s to a %s", getPositionClass().getName(),
                    castToType.getName()));
        }
    }

    @SuppressWarnings("unchecked")
    public <Q extends Position> Geometry<Q> as(Class<Q> castToType){
        checkCast(castToType);
        return (Geometry<Q>)this;
    }

    /**
     * Returns the <code>PositionSequence</code> of this instance
     *
     * @return the <code>PositionSequence</code> of this instance
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

    /**
     * Returns the 2D {@code Envelope} of this instance.
     * @return the {@code Envelope} of this instance.
     */
    public Envelope<P> getEnvelope() {
        if (isEmpty()) return new Envelope<P>(getCoordinateReferenceSystem());
        PositionSequence<P> positions = getPositions();
        EnvelopeVisitor<P> visitor = new EnvelopeVisitor<P>(getCoordinateReferenceSystem());
        positions.accept(visitor);
        return visitor.result();
    }

    /**
     * Returns the bounding {@code Box} of this instance
     * @return Returns a {@code Box} that encloses this geometry
     */
    public Box<P> getBoundingBox(){
        if (isEmpty()) return Box.mkEmpty(crs);
        BoxVisitor bv = new BoxVisitor<P>(crs);
        getPositions().accept(bv);
        return bv.build();
    }

    /**
     * Does this {@code Geometry} has Measure (M) coordinates
     * @return true if this {@code Geometry} has Measure coordinate values
     */
    public boolean hasM(){
        return getCoordinateReferenceSystem().hasM();
    }

    /**
     * Does this {@code Geometry} has vertical or Z coordinates
     * @return true if this {@code Geometry} has vertical coordinate values
     */
    public boolean hasZ(){
        return getCoordinateReferenceSystem().hasZ();
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
     * Returns the Well-Known Text (WKT) representation of this <code>Geometry</code>.
     *
     * @return the Well-Known Text (WKT) representation of this <code>Geometry</code>.
     */
    public String toString() {
        return Wkt.toWkt(this);
    }

    /**
     * Returns the topological dimension of this instance. In non-homogenous collections, this will return the largest
     * topological dimension of the contained <code>Geometries</code>.
     *
     * @return the topological dimension of this instance
     */
    public abstract int getDimension();

    /**
     * Returns a copy of this Geometry, but with the specified Coordinate Reference System.
     * This does <em>not</em> transform the coordinate values.
     * <p>
     * See {@link Geometry#forceToCrs}
     * </p>
     * @param crs the CRS for the returned Geometry
     * @param defaultCoordinateValue default value used in case the coordinate dimension is expanded
     * @param <Q>  Position type of the result
     * @return a copy of this Geometry with the specified Coordinate Reference System
     */
    public <Q extends Position> Geometry<Q> toCrs(CoordinateReferenceSystem<Q> crs, double defaultCoordinateValue) {
        return forceToCrs(this, crs, defaultCoordinateValue);
    }

    /**
     * Accepts a <code>GeometryVisitor</code>.
     * <p>If this <code>Geometry</code> instance is a <code>GeometryCollection</code> then it will pass the
     * visitor to its contained <code>Geometries</code>.</p>
     *
     * @param visitor
     */
    public abstract void accept(GeometryVisitor<P> visitor);

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("Require WKB serialization");
    }

    protected Object writeReplace() throws ObjectStreamException {
        WkbEncoder encoder = Wkb.newEncoder(Wkb.Dialect.POSTGIS_EWKB_2);
        ByteBuffer buffer = encoder.encode(this);
        return new SerializationProxy(buffer);
    }

    //see Effective Java, 2ed, Item 78
    private static class SerializationProxy implements Serializable {
        private final byte[] buffer;
        SerializationProxy(ByteBuffer buffer) {
            this.buffer = buffer.toByteArray();
        }

        private Object readResolve() throws ObjectStreamException {
            return Wkb.fromWkb(ByteBuffer.from(this.buffer));
        }
    }

    private static class EnvelopeVisitor<P extends Position> implements PositionVisitor<P> {

            double[] coordinates;
            double xMin = Double.POSITIVE_INFINITY;
            double yMin = Double.POSITIVE_INFINITY;
            double xMax = Double.NEGATIVE_INFINITY;
            double yMax = Double.NEGATIVE_INFINITY;
            final CoordinateReferenceSystem<P> crs;

            EnvelopeVisitor(CoordinateReferenceSystem<P> crs) {
                this.crs = crs;
                coordinates = new double[crs.getCoordinateDimension()];
            }


            @Override
            public void visit(P position) {
                position.toArray(coordinates);
                xMin = Math.min(xMin, coordinates[0]);
                xMax = Math.max(xMax, coordinates[0]);
                yMin = Math.min(yMin, coordinates[1]);
                yMax = Math.max(yMax, coordinates[1]);
            }

            public Envelope<P> result() {
                return new Envelope<P>(xMin, yMin, xMax, yMax, crs);
            }
        }

    private static class BoxVisitor<P extends Position> implements LLAPositionVisitor {
        private double[] lowerLeft;
        private double[] upperRight;
        final private CoordinateReferenceSystem<P> crs;

        BoxVisitor(CoordinateReferenceSystem<P> crs) {
            this.crs = crs;
            lowerLeft = new double[crs.getCoordinateDimension()];
            Arrays.fill(lowerLeft, Double.MAX_VALUE);
            upperRight = new double[crs.getCoordinateDimension()];
            Arrays.fill(upperRight, -Double.MAX_VALUE);
        }

        @Override
        public void visit(double[] coordinate) {
            for (int i = 0; i < coordinate.length; i++) {
                lowerLeft[i] = Math.min(lowerLeft[i], coordinate[i]);
                upperRight[i] = Math.max(upperRight[i], coordinate[i]);
            }
        }

        Box<P> build() {
            return new Box<>(Positions.mkPosition(crs, lowerLeft), Positions.mkPosition(crs, upperRight), crs);
        }
    }
}

