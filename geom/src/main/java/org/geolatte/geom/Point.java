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
 * A 0-dimensional {@code Geometry}.
 * 
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Point<P extends Position> extends Geometry<P> implements Simple {

    public Point(CoordinateReferenceSystem<P> crs) {
        super(crs);
    }

    public Point(PositionSequence<P> sequence, CoordinateReferenceSystem<P> crs) {
        super(sequence, crs);
    }

    public Point(P position, CoordinateReferenceSystem<P> crs) {
        this(new PackedPositionSequence<P>(
                Positions.getFactoryFor(crs.getPositionClass()), position.coords), crs);
    }

    public Point(Point<P> point) {
        this(point.getPositions(), point.getCoordinateReferenceSystem());
    }

    /**
     * Returns the <code>Position</code> of this <code>Point</code>.
     *
     * @return the <code>Position</code> of this <code>Point</code>.
     */
    public P getPosition() {
        return getPositions().getPositionN(0) ;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q extends Position> Point<Q> as(Class<Q> castToType){
        return (Point<Q>)this;
    }

    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POINT;
    }

    @Override
    public void accept(GeometryVisitor<P> visitor) {
        visitor.visit(this);
    }
}
