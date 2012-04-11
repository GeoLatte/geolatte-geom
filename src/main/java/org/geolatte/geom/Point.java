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
public class Point extends Geometry {

    static final Point EMPTY = new Point(EmptyPointSequence.INSTANCE, CrsId.UNDEFINED, null);

    private final PointSequence points;

    public static Point createEmpty() {
        return EMPTY;
    }

    public Point(PointSequence sequence, CrsId crsId, GeometryOperations geometryOperations) {
        super(crsId, geometryOperations);
        this.points = sequence;
    }

    public Point(PointSequence sequence, CrsId crsId) {
        this(sequence, crsId, null);
    }

    @Override
    public PointSequence getPoints() {
        return points;
    }

    public double getX() {
        return isEmpty() ? Double.NaN : getPoints().getX(0);
    }

    public double getY() {
        return isEmpty() ? Double.NaN : getPoints().getY(0);
    }

    public double getZ() {
        return isEmpty() ? Double.NaN : getPoints().getZ(0);
    }

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

    //TODO -- remove this (obsolete)
    @Override
    public String toString(){
        StringBuilder stbuf = new StringBuilder("SRID=").append(getSRID()).append(",");
        stbuf.append(getX()).append(",").append(getY());
        if (is3D()) {
            stbuf.append(", Z=").append(getZ());
        }
        if (isMeasured()){
            stbuf.append(", M=").append(getM());
        }
        return stbuf.toString();

    }

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
