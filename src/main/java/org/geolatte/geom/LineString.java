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

import org.geolatte.geom.crs.CrsId;

/**
 * A LineString is a 1-dimensional <code>Geometry</code> consisting of the <code>LineSegment</code>s defined by
 * consecutive pairs of <code>Point</code>s of a <code>PointSequence</code>.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class LineString extends Geometry {

    static final LineString EMPTY = new LineString(null, CrsId.UNDEFINED, null);

    private final PointSequence points;

    /**
     * Constructs an empty <code>LineString</code>.
     *
     * @return
     */
    public static LineString createEmpty() {
        return EMPTY;
    }

    /**
     * This constructor has been added to speed up object creation of LinearRings
     *
     * @param base
     */
    protected LineString(LineString base) {
        super(base.getCrsId(), base.getGeometryOperations());
        this.points = base.points;
    }

    /**
     * Constructs a <code>LineString</code> from the specified <code>PointSequence</code>, coordinate reference system
     * and <code>GeometryOperations</code> implementation.
     *
     * @param points             the <code>PointSequence</code>
     * @param crsId              the <code>CrsId</code> for the coordinate reference system
     * @param geometryOperations the <code>GeometryOperations</code> implementation.
     */
    public LineString(PointSequence points, CrsId crsId, GeometryOperations geometryOperations) {
        super(crsId, geometryOperations);
        if (points == null) {
            points = EmptyPointSequence.INSTANCE;
        }
        this.points = points;
    }

    /**
     * Constructs a <code>LineString</code> from the specified <code>PointSequence</code> and coordinate reference system
     *
     * @param points the <code>PointSequence</code>
     * @param crsId  the <code>CrsId</code> for the coordinate reference system
     */
    public LineString(PointSequence points, CrsId crsId) {
        this(points, crsId, null);
    }


    private boolean determineSimple() {
        return super.isSimple();
    }

    @Override
    public PointSequence getPoints() {
        return points;
    }

    /**
     * Returns the length of this <code>LineString</code> in its coordinate reference system.
     *
     * @return the length of this <code>LineString</code> in its coordinate reference system.
     */
    public double getLength() {
        double length = 0d;
        Point prev = null;
        for (Point pnt : getPoints()) {
            if (prev == null) {
                prev = pnt;
                continue;
            }
            length += Math.hypot(pnt.getX() - prev.getX(), pnt.getY() - prev.getY());
            prev = pnt;
        }
        return length;
    }


    /**
     * Returns the first <code>Point</code> of this <code>LineString</code>.
     *
     * @return the first <code>Point</code> of this <code>LineString</code>.
     */
    public Point getStartPoint() {
        return isEmpty() ?
                Points.createEmpty() :
                getPointN(0);
    }


    /**
     * Returns the last <code>Point</code> of this <code>LineString</code>.
     *
     * @return the last <code>Point</code> of this <code>LineString</code>.
     */
    public Point getEndPoint() {
        return isEmpty() ?
                Points.createEmpty() :
                getPointN(getNumPoints() - 1);
    }

    /**
     * Checks whether this <code>LineString</code> is closed, i.e. the first <code>Point</code> equals the last.
     *
     * @return true if this <code>LineString</code> is closed.
     */
    public boolean isClosed() {
        return !isEmpty() && getStartPoint().equals(getEndPoint());
    }

    /**
     * Checks whether this <code>LineString</code> is a ring, i.e. is closed and simple.
     *
     * @return true if this <code>LineString</code> is a ring.
     */
    public boolean isRing() {
        return isEmpty() ? false : isClosed() && isSimple();
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LINE_STRING;
    }

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
