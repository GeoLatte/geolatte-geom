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

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>GeometryVisitor</code> that derives a <code>Geometry</code>
 * by interpolating on the measure values of the visited <code>Geometries</code>.
 * <p/>
 * <p>Interpolation is used to determine any points on the 1-dimensional geometry with an m coordinate value
 * between SM and EM inclusively. The implementation-defined interpolation algorithm is used to estimate
 * values between measured values, usually using a mathematical function. For example, given a measure
 * of 6 and a 2-point linestring where the m coordinate value of the start point is 4 and the m coordinate
 * value of the end point is 8, since 6 is halfway between 4 and 8, the interpolation algorithm would be a
 * point on the linestring halfway between the start and end points.
 * </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/10/12
 */
public class MeasureInterpolatingVisitor<P extends C2D & Measured> implements GeometryVisitor<P> {

    private static final String INVALID_TYPE_MSG =
            "Operation only valid on Point, MultiPoint, LineString, and MultiLineString Geometries.";


    private final Geometry<P> geometry;
    private final double startMeasure;
    private final double endMeasure;


    //data structures for the result
    private final List<PositionSequence<P>> positionSequences = new ArrayList<PositionSequence<P>>();
    private PositionSequenceBuilder<P> currentBuilder;
    private boolean sequenceIsEmpty = true;

    public MeasureInterpolatingVisitor(Geometry<P> geometry, double startMeasure, double endMeasure) {
        if (startMeasure <= endMeasure) {
            this.startMeasure = startMeasure;
            this.endMeasure = endMeasure;
        } else {
            this.startMeasure = endMeasure;
            this.endMeasure = startMeasure;
        }

        this.geometry = geometry;
    }

    @Override
    public void visit(Point<P> point) {
        P pos = point.getPosition();
        if (pos.getM() >= startMeasure && pos.getM() <= endMeasure) {
            positionSequences.add(point.getPositions());
        }
    }


    @Override
    public void visit(LineString<P> lineString) {
        currentBuilder = PositionSequenceBuilders.variableSized(this.geometry.getPositionClass());
        P lastAddedPoint = null;
        LineSegments<P> segments = new LineSegments<P>(lineString.getPositions());
        for (LineSegment<P> segment : segments) {
            P p0 = segment.getStartPosition();
            P p1 = segment.getEndPosition();

            //determine the interpolation factors
            //Note: rs and re can be Infinite (when p1 and p0 have the same M-value).
            double rs = (startMeasure - p0.getM()) / (p1.getM() - p0.getM());
            double re = (endMeasure - p0.getM()) / (p1.getM() - p0.getM());

            // set rs and re in the coordinate-order (if r1,r2 in (0,1)
            // so order will be p0 -- p(r1) -- p(r2) -- p1, if
            // r1, r2 in [0, 1]
            double r1 = Math.min(rs, re);
            double r2 = Math.max(rs, re);

            if (startMeasure <= p0.getM() && p0.getM() <= endMeasure) {
                lastAddedPoint = addIfNotEqualLast(lastAddedPoint, p0);
            } else {
                //p0 not within [startMeasure, endMeasure], so next point to add will not be consecutive with
                // previously added points (if any!)
                startNewPointSequenceIfNotEmpty();

                if (r1 > 0 && r1 < 1) {
                    lastAddedPoint = addIfNotEqualLast(lastAddedPoint, interpolate(p0, p1, r1));
                }
            }

            if (startMeasure <= p1.getM()  && p1.getM() <= endMeasure) {
                lastAddedPoint = addIfNotEqualLast(lastAddedPoint, p1);
            } else {
                if (r2 > 0 && r2 < 1) {
                    lastAddedPoint = addIfNotEqualLast(lastAddedPoint, interpolate(p0, p1, r2));
                }
                //p1 not within [startMeasure, endMeasure], so next point to add will not be consecutive with
                // previously added points (if any!)
                startNewPointSequenceIfNotEmpty();
            }
        }

        PositionSequence<P> last = currentBuilder.toPositionSequence();
        if (!last.isEmpty()) {
            positionSequences.add(last);
        }

    }

    private void startNewPointSequenceIfNotEmpty() {
        if (!sequenceIsEmpty) {
            positionSequences.add(currentBuilder.toPositionSequence());
            currentBuilder = PositionSequenceBuilders.variableSized(this.geometry.getPositionClass());
            sequenceIsEmpty = true;
        }
    }

    private P addIfNotEqualLast(P lastPoint, P newPnt) {
        assert (newPnt != null);
        if (!newPnt.equals(lastPoint)) {
            currentBuilder.add(newPnt);
            lastPoint = newPnt;
        }
        return lastPoint;
    }

    private P  interpolate(P p0, P p1, double r) {
        int dim = getCrs().getCoordinateDimension();
        double result[] = new double[dim];
        for (int i = 0; i < dim; i++) {
            result[i] = p0.getCoordinate(i) + r * (p1.getCoordinate(i) - p0.getCoordinate(i));
        }
        return Positions.mkPosition(getCrs(), result);
    }

    @Override
    public void visit(Polygon polygon) {
        throw new IllegalArgumentException(INVALID_TYPE_MSG);
    }

    @Override
    public void visit(GeometryCollection collection) {

    }

    @SuppressWarnings("unchecked")
    public Geometry<P> result() {
        int number0Dimensional = 0;
        int number1Dimensional = 0;

        for (PositionSequence ps : positionSequences) {
            assert (!ps.isEmpty());
            if (ps.size() > 1) {
                number1Dimensional++;
            } else {
                number0Dimensional++;
            }
        }

        if (number0Dimensional == 0 && number1Dimensional == 0) {
            Position p = Positions.mkPosition(getCrs(), Double.NaN, Double.NaN);
            return new Point(p, getCrs());
        }

        if (number0Dimensional > 1 && number1Dimensional == 0) {
            Point<P>[] pnts = (Point<P>[])new Point[number0Dimensional];
            int i = 0;
            for (PositionSequence<P> ps : positionSequences) {
                pnts[i++] = new Point<P>(ps, this.geometry.getCoordinateReferenceSystem());
            }
            return new MultiPoint<P>(pnts);
        }

        if (number0Dimensional == 1 && number1Dimensional == 0) {
            return new MultiPoint<P>(
                    new Point[]{new Point<P>(positionSequences.get(0), this.geometry.getCoordinateReferenceSystem())}
            );
        }

        if (number0Dimensional == 0 && number1Dimensional >= 1) {
            LineString<P>[] lineStrings = (LineString<P>[])new LineString[number1Dimensional];
            int i = 0;
            for (PositionSequence ps : positionSequences) {
                lineStrings[i++] = new LineString<P>(ps, this.geometry.getCoordinateReferenceSystem());
            }
            return new MultiLineString<P>(lineStrings);
        }

        if (number0Dimensional > 0 && number1Dimensional > 0) {
            Geometry<P>[] geometries = (Geometry<P>[])new Geometry[number1Dimensional + number0Dimensional];
            int i = 0;
            for (PositionSequence<P> ps : positionSequences) {
                if (ps.size() == 1) {
                    geometries[i++] = new Point<P>(ps, this.geometry.getCoordinateReferenceSystem());
                } else {
                    geometries[i++] = new LineString<P>(ps, this.geometry.getCoordinateReferenceSystem());
                }
            }
            return new GeometryCollection<P, Geometry<P>>(geometries);
        }


        throw new IllegalStateException(String.format(
                "Programming error: Case of % d 0-Dim. en %d 1-Dim not properly handled",
                number0Dimensional, number1Dimensional));
    }

    private CoordinateReferenceSystem<P> getCrs(){
        return this.geometry.getCoordinateReferenceSystem();
    }

}
