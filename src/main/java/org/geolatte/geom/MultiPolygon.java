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

import org.geolatte.geom.jts.JTS;

/**
 * A <code>GeometryCollection</code> that contains only <code>Polygon</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/20/11
 */
public class MultiPolygon extends GeometryCollection {

    static final MultiPolygon EMPTY = new MultiPolygon(new Polygon[0]);

    /**
     * Constructs an empty <code>MultiPolygon</code>.
     *
     * @return an empty <code>MultiPolygon</code>.
     */
    public static MultiPolygon createEmpty() {
        return EMPTY;
    }

    /**
     * Constructs a <code>MultiPolygon</code> from the specified array of <code>Polygon</code>s.
     *
     * @param polygons the element <code>Polygon</code>s for the constructed <code>MultiPolygon</code>
     */
    public MultiPolygon(Polygon[] polygons) {
        super(polygons);
    }

    @Override
    public Polygon getGeometryN(int num) {
        return (Polygon) super.getGeometryN(num);
    }

    /**
     * Returns the sum of the areas of all element <code>Polygon</code>s.
     *
     * @return the sum of the areas of all element <code>Polygon</code>s.
     */
    public double area() {
        double totalArea = 0;
        for (int i = 0; i < getNumGeometries(); i++) {
            totalArea += getGeometryN(i).getArea();
        }
        return totalArea;
    }

    /**
     * Returns the mathematical centroid for this <code>MultiPolygon</code>.
     *
     * @return the mathematical centroid for this <code>MultiPolygon</code>.
     */
    public Point centroid() {
        if (this.isEmpty()) return Point.EMPTY;
        return (Point) JTS.from(JTS.to(this).getCentroid());
    }

    /**
     * Returns a <code>Point</code> guaranteed to be on this <code>MultiPolygon</code>.
     *
     * @return a <code>Point</code> guaranteed to be on this <code>MultiPolygon</code>.
     */
    public Point pointOnSurface() {
        for (int i = 0; i < getNumGeometries(); i++) {
            if (!getGeometryN(i).isEmpty()) {
                return getGeometryN(i).getPointOnSurface();
            }
        }
        return Point.EMPTY;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_POLYGON;
    }

}
