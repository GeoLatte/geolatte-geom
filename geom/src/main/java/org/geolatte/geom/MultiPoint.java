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
 * A {@code GeometryCollection} of {@code Point}s.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class MultiPoint<P extends Position> extends GeometryCollection<P, Point<P>> {


    /**
     * Constructs a <code>MultiPoint</code> from the specified <code>Point</code>s
     *
     * @param points the element <code>Point</code>s for the constructed <code>MultiPoint</code>
     */
//    @SafeVarargs
    public MultiPoint(Point<P>... points) {
        super(points);
    }

    public MultiPoint(CoordinateReferenceSystem<P> crs) {
        super(crs);
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTIPOINT;
    }


    @Override
    public Class<? extends Geometry> getComponentType() {
        return Point.class;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Position> MultiPoint<Q> as(Class<Q> castToType){
        return (MultiPoint<Q>)this;
    }


}
