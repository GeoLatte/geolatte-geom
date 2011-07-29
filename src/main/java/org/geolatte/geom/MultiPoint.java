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

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class MultiPoint extends GeometryCollection {


    protected static final MultiPoint EMPTY = new MultiPoint(new Point[0],0);

    public static MultiPoint create(Point[] points, int SRID) {
        return new MultiPoint(points, SRID);
    }

    public static MultiPoint createEmpty() {
        return EMPTY;
    }

    MultiPoint(Point[] points, int SRID){
        super(points, SRID);
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_POINT;
    }

    @Override
    public Geometry getBoundary() {
        return MultiPoint.EMPTY;
    }

    @Override
    public Point getGeometryN(int num) {
        return (Point)super.getGeometryN(num);
    }

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
