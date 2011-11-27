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
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class LineString extends Geometry {

    public static final LineString EMPTY =  new LineString(null, CrsId.UNDEFINED, null);

    private final PointSequence points;


    public static LineString create(PointSequence points, CrsId crsId, GeometryOperations geometryOperations) {
        return new LineString(points, crsId, geometryOperations);
    }

    public static LineString create(PointSequence points, CrsId SRID){
        return new LineString(points, SRID, null);
    }

    public static LineString createEmpty() {
        return EMPTY;
    }

    /**
     * This constructor has been added to speed up object creation of LinearRings
     * @param base
     */
    protected LineString(LineString base){
        super(base.getCrsId(), base.getGeometryOperations());
        this.points = base.points;
    }

    protected LineString(PointSequence points, CrsId crsId, GeometryOperations geometryOperations) {
        super(crsId, geometryOperations);
        if (points == null){
            points = EmptyPointSequence.INSTANCE;
        }
        this.points = points;
    }


    private boolean determineSimple() {
        return super.isSimple();
    }

    @Override
    public PointSequence getPoints() {
        return points;
    }

    public double getLength() {
        double length = 0d;
        Point prev = null;
        for(Point pnt : getPoints()){
            if (prev == null){
                prev = pnt;
                continue;
            }
            length += Math.hypot(pnt.getX() - prev.getX(), pnt.getY() - prev.getY());
            prev = pnt;
        }
        return length;
    }

    public Point getStartPoint() {
        return isEmpty()?
                Point.createEmpty() :
                getPointN(0);
    }


    public Point getEndPoint() {
        return isEmpty()?
                Point.createEmpty() :
                getPointN(getNumPoints() - 1);
    }

    public boolean isClosed() {
        return !isEmpty() && getStartPoint().equals(getEndPoint());
    }


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
