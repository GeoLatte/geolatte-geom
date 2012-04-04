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
     * Returns the squared distance and projectionfactor of the point y
     * on the linesegment defined by points p0 and p1
     * <p>The projection factor is the value for t that determines the projection
     * of y on the line p0 + t*(p1-p0).
     * </p>
     *
     * @param p0 the start point of the line segment
     * @param p1 the end point of the line segment
     * @param y  the point to project onto the linesegment
     * @return an array of length 2, with first element being the squared distance of Point y to the linesegment,
     *         and second element the projection factor
     */
    public static double[] pointToSegment2D(Point p0, Point p1, Point y) {
        //for algorithm, see "Geometric Tools for Computer Graphics", Ch. 6.
        Point d = Vector.substract(p1, p0);
        Point ymp0 = Vector.substract(y, p0);
        double t = Vector.dot(d, ymp0, true);
        double dd = Vector.dot(d, d);
        if (t <= 0) {
            return new double[]{Vector.dot(ymp0,ymp0),t/dd};
        }
        if (t >= dd){
            Point ymp1 = Vector.substract(y,p1);
            return new double[]{Vector.dot(ymp1, ymp1), t/dd};
        }
        return new double[]{Vector.dot(ymp0, ymp0) - (t*t/dd), t/dd};
    }

    /**
     * Returns the dot-product of the specified <code>Point</code>s.
     *
     * <p>If both <code>Point</code>s are 3D, then the dot-product will be  </p>
     * @param p0
     * @param p1
     * @return
     */
    public static double dot(Point p0, Point p1) {
        return dot(p0, p1, false);
    }

    /**
     * Returns the dot-product of the specified <code>Point</code>s, but ignoring any Z-coordinate values.
     *
     * @param p0
     * @param p1
     * @param limit2D
     * @return
     */
    public static double dot(Point p0, Point p1, boolean limit2D) {
        if (limit2D || !p0.is3D() || !p1.is3D()){
           return p0.getX()*p1.getX() + p0.getY()*p1.getY();
        } else {
            return p0.getX()*p1.getX() + p0.getY()*p1.getY() + p0.getZ()*p1.getZ();
        }
    }

    /**
     *Adds two <code>Point</code>s.
     *
     */
    public static Point add(Point p0, Point p1){
        if (p0.is3D() && p1.is3D()) {
            return Points.create3D(p0.getX() + p1.getX(), p0.getY() + p1.getY(), p0.getZ() + p1.getZ());
        } else {
            return Points.create(p0.getX() + p1.getX(), p0.getY() + p1.getY());
        }
    }

    public static Point substract(Point p0, Point p1){
        if (p0.is3D() && p1.is3D()) {
            return Points.create3D(p0.getX() - p1.getX(), p0.getY() - p1.getY(), p0.getZ() - p1.getZ());
        } else {
            return Points.create(p0.getX() - p1.getX(), p0.getY() - p1.getY());
        }
    }

}
