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

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.operation.BoundaryOp;
import com.vividsolutions.jts.operation.IsSimpleOp;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.operation.overlay.OverlayOp;
import com.vividsolutions.jts.operation.overlay.snap.SnapIfNeededOverlayOp;
import com.vividsolutions.jts.operation.relate.RelateOp;
import org.geolatte.geom.codec.ByteBuffer;
import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.jts.JTS;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/3/11
 */
class JTSGeometryOperations implements GeometryOperations {

    private static boolean envelopeIntersect(Geometry geometry1, Geometry geometry2) {
        return(geometry1.getEnvelope().intersects(geometry2.getEnvelope()));
    }

    /**
     * Throws an <code>IllegalArgumentException</code> when class of parameter is <code>GeometryCollection</code>.
     * Subclasses of <code>GeometryCollection</code> do not trigger the Exception.
     * @param geom
     */
    private static void checkNotGeometryCollection(Geometry geom){
        if (GeometryCollection.class.equals(geom.getClass())) {
            throw new IllegalArgumentException("GeometryCollection is not allowed");
        }
    }

    private static void checkCompatibleCRS(Geometry geometry, Geometry other) {
        if (! geometry.getCrsId().equals(other.getCrsId())) {
            throw new IllegalArgumentException("Geometries have different CRS's");
        }
    }

    @Override
    public GeometryOperation<Boolean> createIsSimpleOp(final Geometry geometry) {
        final IsSimpleOp isSimpleOp = new IsSimpleOp(JTS.to(geometry));
        return new GeometryOperation<Boolean>(){
            @Override
            public Boolean execute() {
                return isSimpleOp.isSimple();
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createBoundaryOp(final Geometry geometry) {
        final BoundaryOp boundaryOp = new BoundaryOp(JTS.to(geometry));
        final CrsId crsId = geometry.getCrsId();
        return new GeometryOperation<Geometry>(){
            @Override
            public Geometry execute() {
                return JTS.from(boundaryOp.getBoundary(), crsId);
            }
        };
    }

    @Override
    public GeometryOperation<Envelope> createEnvelopeOp(final Geometry geometry) {
        return new GeometryOperation<Envelope> (){

            @Override
            public Envelope execute() {
                PointSequence ps = geometry.getPoints();
                EnvelopeVisitor visitor = new EnvelopeVisitor(geometry.getCrsId());
                ps.accept(visitor);
                return visitor.result();
            }
        };
    }

    @Override
    public GeometryOperation<Boolean> createIntersectsOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Boolean>() {
            @Override
            public Boolean execute() {
                if (geometry.isEmpty() || other.isEmpty()) return Boolean.FALSE;
                checkCompatibleCRS(geometry, other);
                if (!envelopeIntersect(geometry, other)) return Boolean.FALSE;
                RelateOp relateOp = new RelateOp(JTS.to(geometry), JTS.to(other));
                return relateOp.getIntersectionMatrix().isIntersects();
            }
        };
    }

    @Override
    public GeometryOperation<Boolean> createTouchesOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Boolean>() {
            @Override
            public Boolean execute() {
                if (geometry.isEmpty() || other.isEmpty()) return Boolean.FALSE;
                checkCompatibleCRS(geometry, other);
                if (!envelopeIntersect(geometry, other)) return Boolean.FALSE;
                final RelateOp relateOp = new RelateOp(JTS.to(geometry), JTS.to(other));
                return relateOp.getIntersectionMatrix().isTouches(geometry.getDimension(), other.getDimension());
            }
        };
    }

    @Override
    public GeometryOperation<Boolean> createCrossesOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Boolean>() {
            @Override
            public Boolean execute() {
                if (geometry.isEmpty() || other.isEmpty()) return Boolean.FALSE;
                checkCompatibleCRS(geometry, other);
                if (!envelopeIntersect(geometry, other)) return Boolean.FALSE;
                final RelateOp relateOp = new RelateOp(JTS.to(geometry), JTS.to(other));
                return relateOp.getIntersectionMatrix().isCrosses(geometry.getDimension(), other.getDimension());
            }
        };
    }

    @Override
    public GeometryOperation<Boolean> createContainsOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Boolean>() {
                    @Override
                    public Boolean execute() {
                        if (geometry.isEmpty() || other.isEmpty()) return Boolean.FALSE;
                        checkCompatibleCRS(geometry, other);
                        if (!geometry.getEnvelope().contains(other.getEnvelope())) return Boolean.FALSE;
                        final RelateOp relateOp = new RelateOp(JTS.to(geometry), JTS.to(other));
                        return relateOp.getIntersectionMatrix().isContains();
                    }
                };
    }

    @Override
    public GeometryOperation<Boolean> createOverlapsOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Boolean>() {
            @Override
            public Boolean execute() {
                if (geometry.isEmpty() || other.isEmpty()) return Boolean.FALSE;
                checkCompatibleCRS(geometry, other);
                if (!envelopeIntersect(geometry, other)) return Boolean.FALSE;
                final RelateOp relateOp = new RelateOp(JTS.to(geometry), JTS.to(other));
                return relateOp.getIntersectionMatrix().isOverlaps(geometry.getDimension(), other.getDimension());
            }
        };
    }

    @Override
    public GeometryOperation<Boolean> createRelateOp(final Geometry geometry, final Geometry other, final String matrix) {
        return new GeometryOperation<Boolean>() {
            @Override
            public Boolean execute() {
                if (geometry.isEmpty() || other.isEmpty()) return Boolean.FALSE;
                checkCompatibleCRS(geometry, other);
                final RelateOp relateOp = new RelateOp(JTS.to(geometry), JTS.to(other));
                return relateOp.getIntersectionMatrix().matches(matrix);
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createLocateAlongOp(Geometry geometry, double mValue) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Geometry> createLocateBetween(Geometry geometry, double mStart, double mEnd) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public GeometryOperation<Double> createDistanceOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Double>(){
            @Override
            public Double execute() {
                final DistanceOp op =  new DistanceOp(JTS.to(geometry), JTS.to(other));
                return op.distance();
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createBufferOp(final Geometry geometry, final double distance) {
        return new GeometryOperation<Geometry>() {
            @Override
            public Geometry execute() {
                final BufferOp op = new BufferOp(JTS.to(geometry));
                return JTS.from(op.getResultGeometry(distance), geometry.getCrsId());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createConvexHullOp(final Geometry geometry) {
        return new GeometryOperation<Geometry>(){
            @Override
            public Geometry execute() {
                final ConvexHull convexHull = new ConvexHull(JTS.to(geometry));
                return JTS.from(convexHull.getConvexHull(), geometry.getCrsId());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createIntersectionOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Geometry>(){
            @Override
            public Geometry execute() {
                if(geometry.isEmpty() || other.isEmpty()) return GeometryCollection.createEmpty();
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                checkCompatibleCRS(geometry, other);
                com.vividsolutions.jts.geom.Geometry intersection = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.INTERSECTION);
                return JTS.from( intersection, geometry.getCrsId());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createUnionOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Geometry>(){
            @Override
            public Geometry execute() {
                if (geometry.isEmpty()) return other;
                if (other.isEmpty()) return geometry;
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                checkCompatibleCRS(geometry, other);
                com.vividsolutions.jts.geom.Geometry union = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.UNION);
                return JTS.from( union, geometry.getCrsId());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createDifferenceOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Geometry>(){
            @Override
            public Geometry execute() {
                if (geometry.isEmpty()) return GeometryCollection.createEmpty();
                if (other.isEmpty()) return geometry;
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                checkCompatibleCRS(geometry, other);
                com.vividsolutions.jts.geom.Geometry difference = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.DIFFERENCE);
                return JTS.from( difference, geometry.getCrsId());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry> createSymDifferenceOp(final Geometry geometry, final Geometry other) {
        return new GeometryOperation<Geometry>(){
            @Override
            public Geometry execute() {
                if (geometry.isEmpty()) return other;
                if (other.isEmpty()) return geometry;
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                checkCompatibleCRS(geometry, other);
                com.vividsolutions.jts.geom.Geometry symDifference = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.SYMDIFFERENCE);
                return JTS.from( symDifference, geometry.getCrsId());
            }
        };
    }

    @Override
    public GeometryOperation<String> createToWktOp(final Geometry geometry) {
        return new GeometryOperation<String>(){
            @Override
            public String execute() {
                return Wkt.toWkt(geometry);
            }
        };
    }

    @Override
    public GeometryOperation<ByteBuffer> createToWkbOp(final Geometry geometry) {
        return new GeometryOperation<ByteBuffer>(){
            @Override
            public ByteBuffer execute() {
                return Wkb.toWkb(geometry);
            }
        };
    }


    private static class EnvelopeVisitor implements PointVisitor {

        double xMin = Double.POSITIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        CrsId crsId;

        EnvelopeVisitor(CrsId crsId){
            this.crsId = crsId;
        }


        @Override
        public void visit(double[] coordinates) {
            xMin = Math.min(xMin, coordinates[0]);
            xMax = Math.max(xMax, coordinates[0]);
            yMin = Math.min(yMin, coordinates[1]);
            yMax = Math.max(yMax, coordinates[1]);
        }

        public Envelope result() {
            return new Envelope(xMin, yMin, xMax, yMax, crsId);
        }
    }


}
