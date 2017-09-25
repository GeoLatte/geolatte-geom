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
 * A set of utility functions for basic
 * 2D vector functions.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/7/12
 */
public class Vector {


    /**
     * Returns the squared distance and projectionfactor of the position y
     * on the linesegment defined by positions p0 and p1
     * <p>The projection factor is the value for t that determines the projection
     * of y on the line p0 + t*(p1-p0).
     * </p>
     *
     * @param p0 the start position of the line segment
     * @param p1 the end position of the line segment
     * @param y  the position to project onto the linesegment
     * @return an array of length 2, with first element being the squared distance of Position y to the linesegment,
     * and second element the projection factor
     */
    public static <P extends C2D> double[] positionToSegment2D(P p0, P p1, P y) {
        //for algorithm, see "Geometric Tools for Computer Graphics", Ch. 6.
        P d = Vector.substract(p1, p0);
        P ymp0 = Vector.substract(y, p0);
        double t = Vector.dot(d, ymp0);
        double dd = Vector.dot(d, d);
        if (t <= 0) {
            // p0 is closest to y
            return new double[]{Vector.dot(ymp0, ymp0), t / dd};
        }
        if (t >= dd) {
            //p1 is closest to y
            P ymp1 = Vector.substract(y, p1);
            return new double[]{Vector.dot(ymp1, ymp1), t / dd};
        }
        //we return the abs value: due to rounding errors the formula may return very small negative numbers.
        double dist = Math.abs(Vector.dot(ymp0, ymp0) - (t * t / dd));
        return new double[]{dist, t / dd};
    }


    /**
     * Returns the dot-product of the specified <code>Position</code>s
     * <p/>
     *
     * @param p0 first operand
     * @param p1 second operand
     * @return the dot-product of p0 and p1.
     */
    public static <P extends C2D> double dot(P p0, P p1) {
        if (p0.isEmpty() || p1.isEmpty()) return Double.NaN;
        return p0.getX() * p1.getX() + p0.getY() * p1.getY();
    }

    /**
     * Adds two <code>Position</code>s.
     * <p/>
     * <p>If any of the parameters are 2D, the operation is performed in 2D.</p>
     *
     * @param p0 first operand
     * @param p1 second operand
     * @return the sum of p0 and p1.
     */
    public static <P extends C2D> P add(P p0, P p1) {
        int dim = p0.getCoordinateDimension();
        double[] result = new double[dim];
        for (int i = 0; i < dim; i++) {
            result[i] = p0.getCoordinate(i) + p1.getCoordinate(i);
        }
        return (P) Positions.mkPosition(p0.getClass(), result);
    }

    /**
     * Subtracts two <code>Position</code>s.
     * <p/>
     * <p>If any of the parameters are 2D, the operation is performed in 2D.</p>
     *
     * @param p0 first operand
     * @param p1 second operand
     * @return the Position x = p0 - p1.
     */
    public static <P extends C2D> P substract(P p0, P p1) {
        int dim = p0.getCoordinateDimension();
        double[] result = new double[dim];
        for (int i = 0; i < dim; i++) {
            result[i] = p0.getCoordinate(i) - p1.getCoordinate(i);
        }
        return (P) Positions.mkPosition(p0.getClass(), result);
    }

    /**
     * Hill's "perp" operator.
     * <p>The application of this operator on a vector <code>P</code> returns the vector perpendicular at 90 deg.
     * counterclockwise
     * from <code>P</code> in the 2D (X/Y) plane.</p>
     *
     * @param p a vector represented by a Position.
     * @return the vector perpendicular to p in the 2D-plane, at 90 deg. counterclockwise.
     */
    public static <P extends C2D> P perp(P p) {
        if (p == null || p.isEmpty()) return p;
        double[] crds = p.toArray(null);
        double h = crds[0];
        crds[0] = -crds[1];
        crds[1] = h;
        return (P) Positions.mkPosition(p.getClass(), crds);
    }


    /**
     * Applies the perp dot-operation on the specified <code>Position</code>s
     * <p/>
     * <p>The perp dot operation on vectors <code>P</code>, <code>Q</code> is defined as
     * <code>dot(perp(P),Q)</code>.</p>
     * <p/>
     * <p>This operation will be performed in 2D only.</p>
     *
     * @param p0 first operand
     * @param p1 second operand
     * @return the Perp dot of p0 and p1.
     */
    public static <P extends C2D> double perpDot(P p0, P p1) {
        if (p0 == null || p1 == null || p0.isEmpty() | p1.isEmpty()) {
            throw new IllegalArgumentException("Null or empty Position passed.");
        }
        return -p0.getY() * p1.getX() + p0.getX() * p1.getY();

    }

}
