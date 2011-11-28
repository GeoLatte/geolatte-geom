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

import org.geolatte.geom.codec.ByteBuffer;
import org.geolatte.geom.crs.CrsId;

import java.io.Serializable;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public abstract class Geometry implements Serializable {

    private final CrsId crsId;

    private final GeometryOperations geometryOperations;

    //TODO -- split checking of geometries array (used in GeomColl and Polygon) from creation of Points array
    protected static PointSequence createAndCheckPointSequence(Geometry[] geometries) {
        if (geometries == null || geometries.length == 0) return EmptyPointSequence.INSTANCE;
        PointSequence[] sequences = new PointSequence[geometries.length];
        boolean is3D = false;
        boolean isMeasured = false;
        CrsId srid = CrsId.UNDEFINED;
        for (int i = 0; i < geometries.length; i++) {
            if (geometries[i] == null)
                throw new IllegalArgumentException("Geometry array must not contain null-entries.");
            if (i == 0) {
                is3D = geometries[i].is3D();
                isMeasured = geometries[i].isMeasured();
                srid = geometries[i].getCrsId();
            } else if ((is3D != geometries[i].is3D()) || (isMeasured != geometries[i].isMeasured()) ||
                    (!srid.equals(geometries[i].getCrsId()))) {
                throw new IllegalArgumentException("Geometries must all have same srid and dimension");
            }
            sequences[i] = (geometries[i]).getPoints();
        }
        return new NestedPointSequence(sequences, DimensionalFlag.valueOf(is3D, isMeasured));
    }

    protected Geometry(CrsId crsId, GeometryOperations geometryOperations) {
        this.crsId = (crsId == null ? CrsId.UNDEFINED : crsId);
        this.geometryOperations = (geometryOperations == null ? new JTSGeometryOperations() : geometryOperations);
    }

    public int getCoordinateDimension() {
        return getPoints().getCoordinateDimension();
    }

    public CrsId getCrsId() {
        return crsId;
    }

    public int getSRID(){
        return crsId.getCode();
    }


    public boolean is3D() {
        return getPoints().is3D();
    }

    public boolean isMeasured() {
        return getPoints().isMeasured();
    }

    public boolean isEmpty() {
        return this.getPoints().isEmpty();
    }

    public int getNumPoints() {
        return getPoints().size();
    }

    public Point getPointN(int index) {
        if (index >= getPoints().size()) {
            throw new IndexOutOfBoundsException();
        }
        double[] coords = new double[getCoordinateDimension()];
        getPoints().getCoordinates(coords, index);
        return Point.create(coords, DimensionalFlag.valueOf(is3D(), isMeasured()), getCrsId());
    }

    public abstract PointSequence getPoints();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !Geometry.class.isAssignableFrom(o.getClass())) return false;
        //empty geometries are always true
        Geometry otherGeometry = (Geometry) o;
        if (isEmpty() && otherGeometry.isEmpty()) return true;

        if (this.getGeometryType() != otherGeometry.getGeometryType()) return false;
        if (!this.getCrsId().equals(otherGeometry.getCrsId())) return false;

        if (this instanceof GeometryCollection) {
            GeometryCollection thisGC = (GeometryCollection) this;
            GeometryCollection otherGC = (GeometryCollection) otherGeometry;
            if (thisGC.getNumGeometries() != otherGC.getNumGeometries()) return false;
            for (int i = 0; i < thisGC.getNumGeometries(); i++) {
                if (!thisGC.getGeometryN(i).equals(otherGC.getGeometryN(i))) return false;
            }
            return true;
        } else {
            if (this.getNumPoints() != otherGeometry.getNumPoints()) return false;
            return (this.getPoints().equals(otherGeometry.getPoints()));
        }
    }

    @Override
    public int hashCode() {
        int result = crsId != null ? crsId.hashCode() : 0;
        result = 31 * result + this.getPoints().hashCode();
        return result;
    }

    public abstract GeometryType getGeometryType();

    public boolean isSimple() {
//        IsSimpleOp op = new IsSimpleOp(JTS.to(this));
//        return op.isSimple();
        GeometryOperation<Boolean> op = getGeometryOperations().createIsSimpleOp(this);
        return op.execute();
    }

    public Geometry getBoundary() {
//        return JTS.from(JTS.to(this).getBoundary());
        GeometryOperation<Geometry> operation = getGeometryOperations().createBoundaryOp(this);
        return operation.execute();
    }

    public Envelope getEnvelope() {
        GeometryOperation<Envelope> operation = getGeometryOperations().createEnvelopeOp(this);
        return operation.execute();
    }

    public boolean disjoint(Geometry other) {
        return !intersects(other);
    }

    public boolean intersects(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createIntersectsOp(this, other);
        return operation.execute();
    }

    public boolean touches(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createTouchesOp(this, other);
        return operation.execute();
    }

    public boolean crosses(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createCrossesOp(this, other);
        return operation.execute();
    }

    public boolean within(Geometry other) {
        return other.contains(this);
    }

    public boolean contains(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createContainsOp(this, other);
        return operation.execute();
    }

    public boolean overlaps(Geometry other) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createOverlapsOp(this, other);
        return operation.execute();
    }

    public boolean relate(Geometry other, String matrix) {
        GeometryOperation<Boolean> operation = getGeometryOperations().createRelateOp(this, other, matrix);
        return operation.execute();
    }

    public Geometry locateAlong(double mValue) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createLocateAlongOp(this, mValue);
        return operation.execute();
    }

    public Geometry locateBetween(double mStart, double mEnd) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createLocateBetween(this, mStart, mEnd);
        return operation.execute();
    }

    public double distance(Geometry other) {
        GeometryOperation<Double> operation = getGeometryOperations().createDistanceOp(this, other);
        return operation.execute();
    }

    public Geometry buffer(double distance) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createBufferOp(this, distance);
        return operation.execute();
    }

    public Geometry convexHull() {
        GeometryOperation<Geometry> operation = getGeometryOperations().createConvexHullOp(this);
        return operation.execute();
    }

    public Geometry intersection(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createIntersectionOp(this, other);
        return operation.execute();
    }

    public Geometry union(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createUnionOp(this, other);
        return operation.execute();
    }

    public Geometry difference(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createDifferenceOp(this, other);
        return operation.execute();
    }

    public Geometry symDifference(Geometry other) {
        GeometryOperation<Geometry> operation = getGeometryOperations().createSymDifferenceOp(this, other);
        return operation.execute();
    }

    public String asText() {
        GeometryOperation<String> operation = getGeometryOperations().createToWktOp(this);
        return operation.execute();
    }

    public byte[] asBinary() {
        GeometryOperation<ByteBuffer> operation = getGeometryOperations().createToWkbOp(this);
        return operation.execute().toByteArray();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("SRID=")
                .append(getSRID())
                .append(";")
                .append(getGeometryType());
        if (isMeasured()) builder.append("M");
        builder.append(getPoints());
        return builder.toString();

    }

    public abstract int getDimension();

    public abstract void accept(GeometryVisitor visitor);

    protected GeometryOperations getGeometryOperations() {
        return this.geometryOperations;
    }
}
