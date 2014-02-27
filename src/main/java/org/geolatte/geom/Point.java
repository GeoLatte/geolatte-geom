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
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Point<P extends Position<P>> extends Geometry<P> implements Simple {

    public Point(CoordinateReferenceSystem<P> crs) {
        super(crs);
    }

    public Point(PositionSequence<P> sequence, GeometryOperations<P> geometryOperations) {
        super(sequence, geometryOperations);
    }

    public Point(P position) {
        this(position, (GeometryOperations<P>)DefaultGeometryOperationsFactory.getOperations(position.getClass()));
    }

    public Point(P position, GeometryOperations<P> geometryOperations) {
        this(new PackedPositionSequence<P>(position.getCoordinateReferenceSystem(), position.toArray(null)), geometryOperations);
    }

    public Point(PositionSequence<P> sequence) {
        this(sequence, DefaultGeometryOperationsFactory.getOperations(sequence.getPositionClass()));
    }

    public Point(Point<P> point) {
        this(point.getPositions());
    }

    /**
     * Returns the <code>Position</code> of this <code>Point</code>.
     *
     * @return the <code>Position</code> of this <code>Point</code>.
     */
    public P getPosition() {
        return positions.getPositionN(0) ;
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
    public boolean isSimple() {
        return true;
    }

    @Override
    public Geometry<P> getBoundary() {
        return new Point<P>(new PackedPositionSequence<P>(this.getCoordinateReferenceSystem(), null));
    }

    @Override
    public void accept(GeometryVisitor<P> visitor) {
        visitor.visit(this);
    }
}
