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
public class Point extends Geometry {

    static final Point EMPTY = new Point(EmptyPointSequence.INSTANCE, null);

    private final PointSequence points;

    public static Point createEmpty() {
        return EMPTY;
    }

    public Point(PointSequence sequence, GeometryOperations geometryOperations) {
        super(geometryOperations);
        this.points = sequence;
    }

    public Point(PointSequence sequence) {
        this(sequence, null);
    }

    public Point(Point point) {
        this(point.getPoints());
    }

    @Override
    public PointSequence getPoints() {
        return points;
    }

    /**
     * Returns the X-coordinate of this <code>Point</code>.
     *
     * <p>If this <code>Point</code> is empty, it returns <code>Double.NaN</code>.</p>
     *
     * @return the X-coordinate of this <code>Point</code>
     */
    public double getX() {
        return isEmpty() ? Double.NaN : getPoints().getX(0);
    }

    /**
     * Returns the Y-coordinate of this <code>Point</code>.
     *
     * <p>If this <code>Point</code> is empty, it returns <code>Double.NaN</code>.</p>
     *
     * @return the Y-coordinate of this <code>Point</code>
     */
    public double getY() {
        return isEmpty() ? Double.NaN : getPoints().getY(0);
    }

    /**
     * Returns the Z-coordinate of this <code>Point</code>.
     *
     * <p>If this <code>Point</code> is empty, it returns <code>Double.NaN</code>.</p>
     *
     * @return the Z-coordinate of this <code>Point</code>
     */
    public double getZ() {
        return isEmpty() ? Double.NaN : getPoints().getZ(0);
    }

    /**
     * Returns the M-coordinate of this <code>Point</code>.
     *
     * <p>If this <code>Point</code> is empty, it returns <code>Double.NaN</code>.</p>
     *
     * @return the M-coordinate of this <code>Point</code>
     */
    public double getM() {
        return isEmpty() ? Double.NaN : getPoints().getM(0);
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
    public Geometry getBoundary() {
        return EMPTY;
    }

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
