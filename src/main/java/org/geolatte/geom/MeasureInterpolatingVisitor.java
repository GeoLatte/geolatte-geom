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

import org.geolatte.geom.crs.CrsId;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>GeometryVisitor</code> that derives a <code>Geometry</code>
 * by interpolating on the measure values of the visited <code>Geometries</code>.
 * <p/>
 * TODO -- rewrite (is copied from SQL/MM specs)
 * <p>Interpolation is used to determine any points on the 1-dimensional geometry with an m coordinate value
 * between SM and EM inclusively. The implementation-defined interpolation algorithm is used to estimate
 * values between measured values, usually using a mathematical function. For example, given a measure
 * of 6 and a 2-point linestring where the m coordinate value of the start point is 4 and the m coordinate
 * value of the end point is 8, since 6 is halfway between 4 and 8, the interpolation algorithm would be a
 * point on the linestring halfway between the start and end points. The interpolation is within a line
 * segment and not across line segments in an ST_Curve. The interpolation is within an ST_Curve element
 * and not across ST_Curve elements in an ST_MultiCurve.
 * The results are produced in a geometry collection. If there are consecutive points in the 1-dimensional
 * geometry with an m coordinate value between SM and EM inclusively, then a curve value element is
 * added to the geometry collection to represent the curve elements between these consecutive points. Any
 * disconnected points in the 1-dimensional geometry value with m coordinate values between SM and EM
 * inclusively are also added to the geometry collection. If no matching m coordinate values are found, then
 * an empty set of type ST_Point is returned.
 * </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/10/12
 */
public class MeasureInterpolatingVisitor implements GeometryVisitor {

    private static final String INVALID_TYPE_MSG =
            "Operation only valid on Point, MultiPoint, LineString, and MultiLineString Geometries.";


    private final double startMeasure;
    private final double endMeasure;
    private final CrsId crsId;
    private final GeometryOperations ops;
    private final DimensionalFlag dimFlag;

    //data structures for the result
    private final List<PointSequence> pointSequences = new ArrayList<PointSequence>();
    private PointSequenceBuilder currentBuilder;
    private boolean sequenceIsEmpty = true;

    public MeasureInterpolatingVisitor(Geometry geometry, double startMeasure, double endMeasure) {
        if (startMeasure <= endMeasure) {
            this.startMeasure = startMeasure;
            this.endMeasure = endMeasure;
        } else {
            this.startMeasure = endMeasure;
            this.endMeasure = startMeasure;
        }

        this.crsId = geometry.getCrsId();
        this.ops = geometry.getGeometryOperations();
        this.dimFlag = DimensionalFlag.valueOf(geometry.is3D(), geometry.isMeasured());
    }

    @Override
    public void visit(Point point) {
        if (point.getM() >= startMeasure && point.getM() <= endMeasure) {
            pointSequences.add(point.getPoints());
        }
    }

    //TODO -- make this more robust against rounding errors

    @Override
    public void visit(LineString lineString) {
        currentBuilder = PointSequenceBuilders.variableSized(dimFlag, crsId);
        Point lastAddedPoint = null;
        LineSegments segments = new LineSegments(lineString.getPoints());
        for (LineSegment segment : segments) {
            Point p0 = segment.getStartPoint();
            Point p1 = segment.getEndPoint();

            //determine the interpolation factors
            //Note: rs and re can be Infinite (when p1 and p0 have the same M-value).
            double rs = (startMeasure - p0.getM()) / (p1.getM() - p0.getM());
            double re = (endMeasure - p0.getM()) / (p1.getM() - p0.getM());

            // set rs and re in the coordinate-order (if r1,r2 in (0,1)
            // so order will be p0 -- p(r1) -- p(r2) -- p1, if
            // r1, r2 in [0, 1]
            double r1 = Math.min(rs, re);
            double r2 = Math.max(rs, re);

            if (p0.getM() >= startMeasure && p0.getM() <= endMeasure) {
                lastAddedPoint = addIfNotEqualLast(lastAddedPoint, p0);
            } else {
                //p0 not within [startMeasure, endMeasure], so next point to add will not be consecutive with
                // previously added points (if any!)
                startNewPointSequenceIfNotEmpty();

                if (r1 > 0 && r1 < 1) {
                    lastAddedPoint = addIfNotEqualLast(lastAddedPoint, interpolate(p0, p1, r1));
                }
            }

            if (p1.getM() >= startMeasure && p1.getM() <= endMeasure) {
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

        PointSequence last = currentBuilder.toPointSequence();
        if (!last.isEmpty()) {
            pointSequences.add(last);
        }

    }

    private void startNewPointSequenceIfNotEmpty() {
        if (!sequenceIsEmpty) {
            pointSequences.add(currentBuilder.toPointSequence());
            currentBuilder = PointSequenceBuilders.variableSized(dimFlag, crsId);
            sequenceIsEmpty = true;
        }
    }

    private Point addIfNotEqualLast(Point lastPoint, Point newPnt) {
        assert (newPnt != null);
        if (!newPnt.equals(lastPoint)) {
            currentBuilder.add(newPnt);
            lastPoint = newPnt;
            sequenceIsEmpty = false;
        }
        return lastPoint;
    }

    private Point interpolate(Point p0, Point p1, double r) {
        double x = p0.getX() + r * (p1.getX() - p0.getX());
        double y = p0.getY() + r * (p1.getY() - p0.getY());
        double m = p0.getM() + r * (p1.getM() - p0.getM());
        if (p0.is3D()) {
            double z = p0.getZ() + r * (p1.getZ() - p0.getZ());
            return Points.create(x, y, z, m);
        } else {
            return Points.createMeasured(x, y, m);
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

    public Geometry result() {
        int number0Dimensional = 0;
        int number1Dimensional = 0;

        for (PointSequence ps : pointSequences) {
            assert (!ps.isEmpty());
            if (ps.size() > 1) {
                number1Dimensional++;
            } else {
                number0Dimensional++;
            }
        }

        if (number0Dimensional == 0 && number1Dimensional == 0) {
            return Point.EMPTY;
        }

        if (number0Dimensional > 1 && number1Dimensional == 0) {
            Point[] pnts = new Point[number0Dimensional];
            int i = 0;
            for (PointSequence ps : pointSequences) {
                pnts[i++] = new Point(ps, this.ops);
            }
            return new MultiPoint(pnts);
        }

        if (number0Dimensional == 1 && number1Dimensional == 0) {
            return new MultiPoint(
                    new Point[]{new Point(pointSequences.get(0), this.ops)}
            );
        }

        if (number0Dimensional == 0 && number1Dimensional >= 1) {
            LineString[] lineStrings = new LineString[number1Dimensional];
            int i = 0;
            for (PointSequence ps : pointSequences) {
                lineStrings[i++] = new LineString(ps,  this.ops);
            }
            return new MultiLineString(lineStrings);
        }

        if (number0Dimensional > 0 && number1Dimensional > 0) {
            Geometry[] geometries = new Geometry[number1Dimensional + number0Dimensional];
            int i = 0;
            for (PointSequence ps : pointSequences) {
                if (ps.size() == 1) {
                    geometries[i++] = new Point(ps, this.ops);
                } else {
                    geometries[i++] = new LineString(ps, this.ops);
                }
            }
            return new GeometryCollection(geometries);
        }


        throw new IllegalStateException(String.format(
                "Programming error: Case of % d 0-Dim. en %d 1-Dim not properly handled",
                number0Dimensional, number1Dimensional));
    }
}
