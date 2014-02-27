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

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A <code>GeometryCollection</code> that contains only <code>Polygon</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/20/11
 */
public class MultiPolygon<P extends Position<P>> extends GeometryCollection<P, Polygon<P>> implements Polygonal<P> {

    /**
     * Constructs a <code>MultiPolygon</code> from the specified array of <code>Polygon</code>s.
     *
     * @param polygons the element <code>Polygon</code>s for the constructed <code>MultiPolygon</code>
     */
    @SafeVarargs
    public MultiPolygon(Polygon<P>... polygons) {
        super(polygons);
    }

    public MultiPolygon(CoordinateReferenceSystem<P> crs) {
            super(crs);
        }

//    @Override
//    public Polygon getGeometryN(int num) {
//        return (Polygon) super.getGeometryN(num);
//    }

    /**
     * Returns the sum of the areas of all element <code>Polygon</code>s.
     *
     * @return the sum of the areas of all element <code>Polygon</code>s.
     */
    public double getArea() {
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
    public Point<P> getCentroid() {
        return getGeometryOperations().createGetCentroidOp(this).execute();
    }

    /**
     * Returns a <code>Point</code> guaranteed to be on this <code>MultiPolygon</code>.
     *
     * @return a <code>Point</code> guaranteed to be on this <code>MultiPolygon</code>.
     */
    public Point<P> getPointOnSurface() {
        return new Point<>(getPositionN(0));
    }

    @Override
    public Class<? extends Geometry> getComponentType() {
        return Polygon.class;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_POLYGON;
    }

}
