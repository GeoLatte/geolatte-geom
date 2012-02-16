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
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/8/11
 */
public class MultiLineString extends GeometryCollection {


    public static final MultiLineString EMPTY = new MultiLineString(new LineString[0]);

    public static MultiLineString createEmpty() {
        return EMPTY;
    }

    public MultiLineString(LineString[] geometries) {
        super(geometries);
    }

    @Override
    public LineString getGeometryN(int i){
        return (LineString)super.getGeometryN(i);
    }

    public double getLength() {
        double l = 0.0d;
        for (int i = 0; i < this.getNumGeometries(); i++) {
            l += getGeometryN(i).getLength();
        }
        return l;
    }

    public boolean isClosed() {
        for(int i = 0; i < this.getNumGeometries(); i++) {
            if (!getGeometryN(i).isClosed()) return false;
        }
        return true;
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_LINE_STRING;
    }

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
