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
 * A <code>LineString</code> that is both closed and simple.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/14/11
 */
public class LinearRing<P extends Position> extends LineString<P> {

    public LinearRing(LineString<P> lineString) {
        super(lineString);
        checkIsClosed();
    }

    public LinearRing(CoordinateReferenceSystem<P> crs) {
        super(crs);
    }

    public LinearRing(PositionSequence<P> points, CoordinateReferenceSystem<P> crs) {
        super(points, crs);
        checkIsClosed();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LINEARRING;
    }

    private void checkIsClosed(){
        if (isEmpty() || (isClosed() && getNumPositions() > 3)) return;
        throw new IllegalArgumentException("Cannot create a LinearRing. PointSequence is not closed or contains < 4 points.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q extends Position> LinearRing<Q> as(Class<Q> castToType){
        return (LinearRing<Q>)this;
    }
}
