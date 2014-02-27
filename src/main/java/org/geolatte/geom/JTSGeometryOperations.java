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
import org.geolatte.geom.codec.Wkb;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.jts.JTS;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 5/3/11
 */
class JTSGeometryOperations<P extends Position<P>> implements GeometryOperations<P> {

    private boolean envelopeIntersect(Geometry<P> geometry1, Geometry<P> geometry2) {
        return (geometry1.getEnvelope().intersects(geometry2.getEnvelope()));
    }

    /**
     * Throws an <code>IllegalArgumentException</code> when class of parameter is <code>GeometryCollection</code>.
     * Subclasses of <code>GeometryCollection</code> do not trigger the Exception.
     *
     * @param geom
     */
    private void checkNotGeometryCollection(Geometry<P> geom) {
        if (GeometryCollection.class.equals(geom.getClass())) {
            throw new IllegalArgumentException("GeometryCollection is not allowed");
        }
    }

    private void checkCompatibleCRS(Geometry geometry, Geometry other) {
        if (!geometry.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem())) {
            throw new IllegalArgumentException("Geometries have different CRS's");
        }
    }

    @Override
    public GeometryOperation<Boolean> createIsSimpleOp(final Geometry<P> geometry) {
        final IsSimpleOp isSimpleOp = new IsSimpleOp(JTS.to(geometry));
        return new GeometryOperation<Boolean>() {
            @Override
            public Boolean execute() {
                return isSimpleOp.isSimple();
            }
        };
    }

    @Override
    public GeometryOperation<Geometry<P>> createBoundaryOp(final Geometry<P> geometry) {
        final BoundaryOp boundaryOp = new BoundaryOp(JTS.to(geometry));
        final CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                return JTS.from(boundaryOp.getBoundary(), crs);
            }
        };
    }

    @Override
    public GeometryOperation<Envelope<P>> createEnvelopeOp(final Geometry<P> geometry) {
        GeometryOperation<Envelope<P>> envelopeGeometryOperation = new GeometryOperation<Envelope<P>>() {

            @Override
            public Envelope<P> execute() {
                if (geometry.isEmpty()) return new Envelope<>(geometry.getCoordinateReferenceSystem());
                PositionSequence<P> positions = geometry.getPositions();
                EnvelopeVisitor<P> visitor = new EnvelopeVisitor<P>(geometry.getCoordinateReferenceSystem());
                positions.accept(visitor);
                return visitor.result();
            }
        };
        return envelopeGeometryOperation;
    }

    @Override
    public GeometryOperation<Boolean> createIntersectsOp(final Geometry<P> geometry, final Geometry<P> other) {
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
    public GeometryOperation<Boolean> createTouchesOp(final Geometry<P> geometry, final Geometry<P> other) {
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
    public GeometryOperation<Boolean> createCrossesOp(final Geometry<P> geometry, final Geometry<P> other) {
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
    public GeometryOperation<Boolean> createContainsOp(final Geometry<P> geometry, final Geometry<P> other) {
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
    public GeometryOperation<Boolean> createOverlapsOp(final Geometry<P> geometry, final Geometry<P> other) {
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
    public GeometryOperation<Boolean> createRelateOp(final Geometry<P> geometry, final Geometry<P> other, final String matrix) {
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

    //TODO -- remove these methods, and place in
    @Override
    public GeometryOperation<Geometry<P>> createLocateAlongOp(final Geometry<P> geometry, final double mValue) {
        return createLocateBetweenOp(geometry, mValue, mValue);
    }

    @Override
    public GeometryOperation<Geometry<P>> createLocateBetweenOp(final Geometry<P> geometry, final double startMeasure, final double endMeasure) {
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                if (geometry == null) throw new IllegalArgumentException("Null geometries not allowed.");
                if (geometry.isEmpty()) return new Point<P>(geometry.getCoordinateReferenceSystem());
                if ( Projected.class.isAssignableFrom( geometry.getPositionClass()) &&
                     Measured.class.isAssignableFrom( geometry.getPositionClass())) {
                    MeasureInterpolatingVisitor visitor = new MeasureInterpolatingVisitor(geometry, startMeasure, endMeasure);
                                    geometry.accept((GeometryVisitor<P>)visitor);
                                    return (Geometry<P>)visitor.result();
                }
                throw new IllegalArgumentException("Requires projected coordinates");
            }
        };
    }

    @Override
    public GeometryOperation<Double> createDistanceOp(final Geometry<P> geometry, final Geometry<P> other) {
        return new GeometryOperation<Double>() {
            @Override
            public Double execute() {
                final DistanceOp op = new DistanceOp(JTS.to(geometry), JTS.to(other));
                return op.distance();
            }
        };
    }

    @Override
    public GeometryOperation<Geometry<P>> createBufferOp(final Geometry<P> geometry, final double distance) {
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                final BufferOp op = new BufferOp(JTS.to(geometry));
                return JTS.from(op.getResultGeometry(distance), geometry.getCoordinateReferenceSystem());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry<P>> createConvexHullOp(final Geometry<P> geometry) {
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                final ConvexHull convexHull = new ConvexHull(JTS.to(geometry));
                return JTS.from(convexHull.getConvexHull(), geometry.getCoordinateReferenceSystem());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry<P>> createIntersectionOp(final Geometry<P> geometry, final Geometry<P> other) {
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                checkCompatibleCRS(geometry, other);
                if (geometry.isEmpty() || other.isEmpty()) return new Point<P>(geometry.getCoordinateReferenceSystem());
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                com.vividsolutions.jts.geom.Geometry intersection = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.INTERSECTION);
                return JTS.from(intersection, geometry.getCoordinateReferenceSystem());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry<P>> createUnionOp(final Geometry<P> geometry, final Geometry<P> other) {
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                checkCompatibleCRS(geometry, other);
                if (geometry.isEmpty()) return other;
                if (other.isEmpty()) return geometry;
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                com.vividsolutions.jts.geom.Geometry union = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.UNION);
                return JTS.from(union, geometry.getCoordinateReferenceSystem());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry<P>> createDifferenceOp(final Geometry<P> geometry, final Geometry<P> other) {
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                checkCompatibleCRS(geometry, other);
                if (geometry.isEmpty()) return new Point<P>(geometry.getCoordinateReferenceSystem());
                if (other.isEmpty()) return geometry;
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                com.vividsolutions.jts.geom.Geometry difference = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.DIFFERENCE);
                return JTS.from(difference, geometry.getCoordinateReferenceSystem());
            }
        };
    }

    @Override
    public GeometryOperation<Geometry<P>> createSymDifferenceOp(final Geometry<P> geometry, final Geometry<P> other) {
        return new GeometryOperation<Geometry<P>>() {
            @Override
            public Geometry<P> execute() {
                checkCompatibleCRS(geometry, other);
                if (geometry.isEmpty()) return other;
                if (other.isEmpty()) return geometry;
                checkNotGeometryCollection(geometry);
                checkNotGeometryCollection(other);
                com.vividsolutions.jts.geom.Geometry symDifference = SnapIfNeededOverlayOp.overlayOp(JTS.to(geometry), JTS.to(other), OverlayOp.SYMDIFFERENCE);
                return JTS.from(symDifference, geometry.getCoordinateReferenceSystem());
            }
        };
    }

    @Override
    public GeometryOperation<String> createToWktOp(final Geometry<P> geometry) {
        return new GeometryOperation<String>() {
            @Override
            public String execute() {
                return Wkt.toWkt(geometry);
            }
        };
    }

    @Override
    public GeometryOperation<ByteBuffer> createToWkbOp(final Geometry<P> geometry) {
        return new GeometryOperation<ByteBuffer>() {
            @Override
            public ByteBuffer execute() {
                return Wkb.toWkb(geometry);
            }
        };
    }

    @Override
    public <G extends Geometry<P> & Linear<P>> GeometryOperation<Double> createGetLengthOp(final G geometry) {
        return new GeometryOperation<Double>() {
            @Override
            public Double execute() {
                return JTS.to(geometry).getLength();
            }
        };
    }

    @Override
    public <G extends Geometry<P> & Polygonal<P>> GeometryOperation<Double> createGetAreaOp(final G geometry) {
        return new GeometryOperation<Double>(){
            @Override
            public Double execute() {
                return JTS.to(geometry).getArea();
            }
        };
    }

    @Override
    //@SuppressWarnings("unchecked")
    public <G extends Geometry<P> & Polygonal<P>> GeometryOperation<Point<P>> createGetCentroidOp(final G geometry) {
        return new GeometryOperation<Point<P>>(){
            @Override
            public Point<P> execute() {
                return (Point<P>)JTS.from(JTS.to(geometry).getCentroid());
            }
        };
    }


    private static class EnvelopeVisitor<P extends Position<P>> implements PositionVisitor<P> {

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


}
