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

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.Unit;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasMeasureAxis;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.hasVerticalAxis;

/**
 * Default implementation of {@link MeasureGeometryOperations}.
 * <p/>
 * <p>This implementation conforms to the SQL/MM and SFA 1.2.1 specifications. See
 * <a href="http://portal.opengeospatial.org/files/?artifact_id=25355">Simple Feature Access -
 * Part 1: common architecture</a>, sec. 6.1.2.6 </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public class DefaultMeasureGeometryOperations implements MeasureGeometryOperations {

    private final static PositionEquality pntEq = new ExactPositionEquality();

    @Override
    public <P extends C2D & Measured> Geometry<P> locateAlong(final Geometry<P> geometry, final double mValue) {
        return locateBetween(geometry, mValue, mValue);
    }

    @Override
    public <P extends C2D & Measured> Geometry<P> locateBetween(final Geometry<P> geometry, final double startMeasure, final double endMeasure) {
        if (geometry == null) throw new IllegalArgumentException("Null geometries not allowed.");
        if (geometry.isEmpty()) return new Point<P>(geometry.getCoordinateReferenceSystem());
        if (C2D.class.isAssignableFrom(geometry.getPositionClass()) &&
                Measured.class.isAssignableFrom(geometry.getPositionClass())) {
            MeasureInterpolatingVisitor visitor = new MeasureInterpolatingVisitor(geometry, startMeasure, endMeasure);
            geometry.accept((GeometryVisitor<P>) visitor);
            return (Geometry<P>) visitor.result();
        }
        throw new IllegalArgumentException("Requires projected coordinates");
    }

    /**
     * @inheritDoc
     */
    @Override
    public <P extends C2D & Measured> double measureAt(final Geometry<P> geometry, final P pos, double tolerance) {
        if (geometry == null || pos == null) throw new IllegalArgumentException("Parameters must not be NULL");
        if (geometry.isEmpty()) return Double.NaN;
        InterpolatingVisitor<P> visitor = new InterpolatingVisitor<P>(pos, tolerance);
        geometry.accept(visitor);
        return visitor.m();
    }

    /**
     * @inheritDoc
     */
    @Override
    public <P extends C2D, M extends C2D & Measured> Geometry<M> measureOnLength(
            final Geometry<P> geometry, final Class<M> positionTypeMarker, final boolean keepBeginMeasure) {
        if (geometry == null) throw new IllegalArgumentException("Geometry parameter must not be NULL");
        if (positionTypeMarker == null)
            throw new IllegalArgumentException("PositionTypeMarker parameter must not be NULL");
        if (geometry.getGeometryType() != GeometryType.LINESTRING
                && geometry.getGeometryType() != GeometryType.MULTILINESTRING) {
            throw new IllegalArgumentException("Geometry parameter must be of type LineString or MultiLineString");
        }
        final CoordinateReferenceSystem<P> sourceCRS = geometry.getCoordinateReferenceSystem();

        final CoordinateReferenceSystem<M> measuredVariant = !hasMeasureAxis(sourceCRS) ?
                (CoordinateReferenceSystem<M>) addLinearSystem(sourceCRS, Unit.METER) :
                (CoordinateReferenceSystem<M>)sourceCRS;


        if (!measuredVariant.getPositionClass().equals(positionTypeMarker)) {
            throw new IllegalArgumentException(String.format(
                    "Inconsistent types: measured CRS has position type %s,  but positionTypeMarker is %s.",
                    measuredVariant.getPositionClass().getName(),
                    positionTypeMarker.getName()));
        }
        return new OnLengthMeasureOp<M>(geometry, measuredVariant, keepBeginMeasure).execute();
    }

    /**
     * @inheritDoc
     */
    @Override
    public <P extends Position & Measured> double minimumMeasure(Geometry<P> geometry) {
        return createGetExtrMeasureOp(geometry, true).execute();
    }

    /**
     * @inheritDoc
     */
    @Override
    public <P extends Position & Measured> double maximumMeasure(Geometry<P> geometry) {
        return createGetExtrMeasureOp(geometry, false).execute();
    }

    private <P extends Position & Measured> GeometryOperation<Double> createGetExtrMeasureOp(final Geometry<P> geometry, final boolean min) {
        return new GeometryOperation<Double>() {

            @Override
            public Double execute() {
                if (geometry == null) {
                    throw new IllegalArgumentException("Operation expects a non-empty geometry");
                }
                if (geometry.isEmpty()) {
                    return Double.NaN;
                }

                FindExtremumMeasureVisitor<P> visitor = new FindExtremumMeasureVisitor<P>(min);
                geometry.getPositions().accept(visitor);
                return visitor.extremum;
            }
        };
    }

    private static class InterpolatingVisitor<P extends C2D & Measured> implements GeometryVisitor<P> {

        public static final String INVALID_TYPE_MSG = "Operation only valid on LineString, MultiPoint and MultiLineString Geometries.";
        public static final String OUTSIDE_TOL_MSG = "Search point not within tolerance: distance to geometry is %f > %f";

        final P searchPosition;
        final double tolerance;
        double mValue = Double.NaN;
        double distToSearchPoint = Double.MAX_VALUE;

        InterpolatingVisitor(P pnt, double tolerance) {
            if (pnt == null) throw new IllegalArgumentException("Null point is not allowed.");
            searchPosition = pnt;
            this.tolerance = Math.abs(tolerance);
        }

        double m() {
            if (distToSearchPoint <= tolerance) {
                return mValue;
            }
            throw new IllegalArgumentException(String.format(OUTSIDE_TOL_MSG, distToSearchPoint, tolerance));
        }

        @Override
        public void visit(Point<P> point) {
            // Note that this is also used when visiting MultiPoints
            P pos = point.getPosition();
            double dts = Math.hypot(pos.getX() - searchPosition.getX(), pos.getY() - searchPosition.getY());
            if (dts <= distToSearchPoint) {
                mValue = point.getPosition().getM();
                distToSearchPoint = dts;
            }
        }

        @Override
        public void visit(LineString<P> lineString) {
            LineSegments<P> lineSegments = new LineSegments<P>(lineString.getPositions());
            for (LineSegment<P> segment : lineSegments) {
                P p0 = segment.getStartPosition();
                P p1 = segment.getEndPosition();
                double[] dAndR = Vector.positionToSegment2D(p0, p1, searchPosition);
                double d = Math.sqrt(dAndR[0]);
                if (d <= distToSearchPoint ) {
                    double r = dAndR[1];
                    if (r <= 0) {
                        mValue = p0.getM();
                    } else if (r >= 1) {
                        mValue = p1.getM();
                    } else {
                        mValue = p0.getM() + r * (p1.getM() - p0.getM());
                    }
                    distToSearchPoint = d;
                }
            }
        }

        @Override
        public void visit(Polygon<P> polygon) {
            throw new IllegalArgumentException(INVALID_TYPE_MSG);
        }

        @Override
        public <G extends Geometry<P>> void visit(GeometryCollection<P, G> collection) {
        }

    }

    private static class FindExtremumMeasureVisitor<P extends Position & Measured> implements PositionVisitor<P> {

        final boolean findMinimum;
        double extremum;

        FindExtremumMeasureVisitor(boolean minimum) {
            findMinimum = minimum;
            extremum = minimum ? Double.MAX_VALUE : Double.MIN_VALUE;
        }

        @Override
        public void visit(P position) {
            //assume measure value is last component (must be guaranteed by calling method).
            double m = position.getM();
            if (findMinimum) {
                extremum = m < extremum ? m : extremum;
            } else {
                extremum = m > extremum ? m : extremum;
            }
        }

    }

    private static class OnLengthMeasureOp<M extends C2D & Measured> implements GeometryOperation<Geometry<M>> {
        private double length = 0;

        final private Geometry<?> geometry;
        final private CoordinateReferenceSystem<M> measuredVariant;
        final private boolean keepBeginMeasure;

        OnLengthMeasureOp(Geometry<?> geometry, CoordinateReferenceSystem<M> measuredVariant, boolean keepBeginMeasure) {
            this.geometry = geometry;
            this.measuredVariant = measuredVariant;
            this.keepBeginMeasure = keepBeginMeasure;
        }

        @Override
        public Geometry<M> execute() {
            Geometry<M> measured = Geometry.forceToCrs(geometry, measuredVariant);
            if (measured.isEmpty()) return measured;
            if (keepBeginMeasure) {
                double initialValue = measured.getPositionN(0).getM();
                length = (Double.isNaN(initialValue) ? 0 : initialValue);
            }
            if (measured instanceof LineString) {
                return measure((LineString<M>) measured);
            } else if (geometry instanceof MultiLineString) {
                return measure((MultiLineString<M>) measured);
            } else {
                throw new IllegalStateException(
                        String.format("Requires a LineString or MultiLineString, but received %s",
                                geometry.getClass().getName()));
            }
        }

        //TODO -- the measure() functions can probably be simplified

        @SuppressWarnings("unchecked")
        private <T extends C2D & Measured> MultiLineString<T> measure(MultiLineString<T> geometry) {
            LineString<T>[] measuredParts = (LineString<T>[]) new LineString[geometry.getNumGeometries()];
            for (int part = 0; part < geometry.getNumGeometries(); part++) {
                LineString<T> lineString = geometry.getGeometryN(part);
                measuredParts[part] = measure(lineString);
            }
            return new MultiLineString<T>(measuredParts);
        }

        private <T extends C2D & Measured> LineString<T> measure(LineString<T> geometry) {
            CoordinateReferenceSystem<T> crs = geometry.getCoordinateReferenceSystem();
            PositionSequence originalPoints = geometry.getPositions();
            PositionSequenceBuilder<T> builder = PositionSequenceBuilders.fixedSized(originalPoints.size(),
                    geometry.getPositionClass());
            int mIdx = hasVerticalAxis(crs) ? 3 :2;
            double[] coordinates = new double[geometry.getCoordinateDimension()];
            double[] prevCoordinates = new double[geometry.getCoordinateDimension()];
            for (int i = 0; i < originalPoints.size(); i++) {
                originalPoints.getCoordinates(i, coordinates);
                if (i > 0) {
                    length += Math.hypot(coordinates[0] - prevCoordinates[0], coordinates[1] - prevCoordinates[1]);
                }
                coordinates[mIdx] = length;
                builder.add(coordinates);
                prevCoordinates[0] = coordinates[0];
                prevCoordinates[1] = coordinates[1];

            }
            return new LineString<T>(builder.toPositionSequence(), crs);
        }


    }
}
