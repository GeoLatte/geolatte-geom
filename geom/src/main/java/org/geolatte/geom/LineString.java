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
 * A LineString is a 1-dimensional <code>Geometry</code> consisting of the <code>LineSegment</code>s defined by
 * consecutive pairs of <code>Point</code>s of a <code>PointSequence</code>.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class LineString<P extends Position> extends Geometry<P> implements Linear<P>, Simple {

    public LineString(CoordinateReferenceSystem<P> crs) {
        super(crs);
    }

    /**
     * This constructor has been added to speed up object creation of LinearRings
     *
     * @param base
     */
    protected LineString(LineString<P> base) {
        super(base.getPositions(), base.getCoordinateReferenceSystem());
    }

    /**
     * Constructs a <code>LineString</code> from the specified positions
     * and <code>ProjectedGeometryOperations</code> implementation.
     *
     * @param positions the {@code PositionSequence} that determines this geometry
     * @throws IllegalArgumentException if the passed <code>PointSequence</code> is non-empty and of size < 2
     */
    public LineString(PositionSequence<P> positions, CoordinateReferenceSystem<P> crs) {
        super(positions, crs);
        if (positions.size() != 0 && positions.size() < 2) {
            throw new IllegalArgumentException("Require at least two points for a linestring");
        }
    }


    /**
     * Returns the first <code>Position</code> of this <code>LineString</code>.
     *
     * @return the first <code>Position</code> of this <code>LineString</code>.
     */
    public P getStartPosition() {
        return getPositionN(0);
    }


    /**
     * Returns the last <code>Position</code> of this <code>LineString</code>.
     *
     * @return the last <code>Position</code> of this <code>LineString</code>.
     */
    public P getEndPosition() {
        return getPositionN(getNumPositions() - 1);
    }

    /**
     * Checks whether this <code>LineString</code> is closed, i.e. the first <code>Point</code> equals the last.
     *
     * @return true if this <code>LineString</code> is closed.
     */
    public boolean isClosed() {
        return !isEmpty() && getStartPosition().equals(getEndPosition());
    }

    /**
     * Checks whether this <code>LineString</code> is a ring, i.e. is closed and non-empty.
     *
     * <p>Note that this implementation allows rings thate are non-simple.</p>
     *
     * @return true if this <code>LineString</code> is a ring.
     */
    public boolean isRing() {
        return !isEmpty() && isClosed();
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LINESTRING;
    }

    @Override
    public void accept(GeometryVisitor<P> visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the {@code LineSegments} that define this instance.
     *
     * @return an Iterable of this instance's {@code LineSegment}s
     */
    Iterable<LineSegment<P>> lineSegments() {
        return new LineSegments<P>(getPositions());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q extends Position> LineString<Q> as(Class<Q> castToType){
        return (LineString<Q>)this;
    }

}
