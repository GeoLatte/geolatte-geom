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
 * Default implementation of {@link MeasureGeometryOperations}.
 *
 * <p>This implementation conforms to the SQL/MM and SFA 1.2.1 specifications. See
 * <a href="http://portal.opengeospatial.org/files/?artifact_id=25355">Simple Feature Access -
 * Part 1: common architecture</a>, sec. 6.1.2.6 </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/4/12
 */
public class DefaultMeasureGeometryOperations implements MeasureGeometryOperations {

    private final static PointEquality pntEq = new ExactCoordinatePointEquality(DimensionalFlag.d2D);

    /**
     * @inheritDoc
     */
    @Override
    public GeometryOperation<Double> createGetMeasureOp(final Geometry geometry, final Point point) {
        if (geometry == null || point == null) throw new IllegalArgumentException("Parameters must not be NULL");
        return new GeometryOperation<Double>(){
            @Override
            public Double execute() {
                if(geometry.isEmpty() || point.isEmpty()) return Double.NaN;
                //TODO -- tolerance parameter into API
                InterpolatingVisitor visitor = new InterpolatingVisitor(point, Math.ulp(100));
                geometry.accept(visitor);
                return visitor.m();
            }
        };
    }

     /**
     * @inheritDoc
     */
    @Override
    public GeometryOperation<Geometry> createMeasureOnLengthOp(final Geometry geometry, final boolean keepBeginMeasure) {
        if (geometry == null) throw new IllegalArgumentException("Geometry parameter must not be NULL");
        if (geometry.getGeometryType() != GeometryType.LINE_STRING
                && geometry.getGeometryType() != GeometryType.MULTI_LINE_STRING) {
            throw new IllegalArgumentException("Geometry parameter must be of type LineString or MultiLineString");
        }
        return new GeometryOperation<Geometry>() {
            private double length = 0;
            @Override
            public Geometry execute() {
                if (geometry.isEmpty()) return geometry;
                if (keepBeginMeasure ) {
                    double initialValue = geometry.getPointN(0).getM();
                    length = (Double.isNaN(initialValue)? 0 : initialValue);
                }
                if (geometry instanceof LineString) {
                    return measure((LineString) geometry);
                } else if (geometry instanceof MultiLineString) {
                    return measure((MultiLineString) geometry);
                } else {
                    throw new IllegalStateException(
                            String.format("Requires a LineString or MultiLineString, but received %s",
                                    geometry.getClass().getName()));
                }
            }

            private MultiLineString measure(MultiLineString geometry) {
                LineString[] measuredParts = new LineString[geometry.getNumGeometries()];
                for (int part = 0; part < geometry.getNumGeometries(); part++) {
                    LineString lineString = geometry.getGeometryN(part);
                    measuredParts[part] = measure(lineString);
                }
                return new MultiLineString(measuredParts);
            }

            private LineString measure(LineString geometry) {
                PointSequence originalPoints = geometry.getPoints();
                DimensionalFlag dimFlag = DimensionalFlag.valueOf(geometry.is3D(), true);
                PointSequenceBuilder builder = PointSequenceBuilders.fixedSized(originalPoints.size(), dimFlag, originalPoints.getCrsId());
                double[] coordinates = new double[geometry.getCoordinateDimension() + 1];
                double[] prevCoordinates = new double[geometry.getCoordinateDimension()];
                for (int i = 0; i < originalPoints.size(); i++) {
                    originalPoints.getCoordinates(coordinates, i);
                    if (i > 0) {
                        length += Math.hypot(coordinates[0] - prevCoordinates[0], coordinates[1] - prevCoordinates[1]);
                    }
                    coordinates[dimFlag.M] = length;
                    builder.add(coordinates);
                    prevCoordinates[0] = coordinates[0]; prevCoordinates[1] = coordinates[1];
                }
                return new LineString(builder.toPointSequence(), geometry.getGeometryOperations());
            }

        };
    }

    private static class InterpolatingVisitor implements GeometryVisitor {

        public static final String INVALID_TYPE_MSG = "Operation only valid on LineString, MultiPoint and MultiLineString Geometries.";

        final Point searchPoint;
        final double tolerance;
        double mValue = Double.NaN;

        InterpolatingVisitor(Point pnt, double tolerance){
            if (pnt == null) throw new IllegalArgumentException("Null point is not allowed.");
            searchPoint = pnt;
            this.tolerance = Math.abs(tolerance);
        }

        double m(){
            return mValue;
        }

        @Override
        public void visit(Point point) {
            if ( pntEq.equals(searchPoint, point)) {
                mValue = point.getM();
            }
        }

        @Override
        public void visit(LineString lineString) {
            LineSegments lineSegments = new LineSegments(lineString.getPoints());
            for (LineSegment segment : lineSegments) {
                Point p0 = segment.getStartPoint();
                Point p1 = segment.getEndPoint();
                double[] dAndR = Vector.pointToSegment2D(p0, p1, searchPoint);
                if (dAndR[0] < this.tolerance*this.tolerance){
                    double r = dAndR[1];
                    mValue = p0.getM() + r*(p1.getM() - p0.getM());
                }
            }
        }

        @Override
        public void visit(Polygon polygon) {
            throw new IllegalArgumentException(INVALID_TYPE_MSG);
        }

        @Override
        public void visit(GeometryCollection collection) {
        }

        @Override
        public void visit(LinearRing linearRing) {
            visit((LineString) linearRing);
        }

        @Override
        public void visit(PolyHedralSurface surface) {
            throw new IllegalArgumentException(INVALID_TYPE_MSG);
        }
    }
}
