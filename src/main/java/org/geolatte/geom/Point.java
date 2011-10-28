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

import java.util.Arrays;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Point extends Geometry {

    static final Point EMPTY = new Point(EmptyPointSequence.INSTANCE,0);

    private final PointSequence points;

    public static Point create(PointSequence sequence, int SRID) {
        return new Point(sequence, SRID);
    }

    public static Point create2D(double x, double y, int SRID) {
        return new Point(new PackedPointSequence(new double[]{x, y}, DimensionalFlag.XY), SRID);
    }

    public static Point create3D(double x, double y, double z, int SRID) {
        return new Point(new PackedPointSequence(new double[]{x, y, z}, DimensionalFlag.XYZ), SRID);
    }

    public static Point create2DM(double x, double y, double m, int SRID) {
        return new Point(new PackedPointSequence(new double[]{x, y, m}, DimensionalFlag.XYM), SRID);
    }

    public static Point create3DM(double x, double y, double z, double m, int SRID) {
        return new Point(new PackedPointSequence(new double[]{x, y, z, m}, DimensionalFlag.XYZM), SRID);
    }

    public static Point create2D(double x, double y) {
        return new Point(new PackedPointSequence(new double[]{x, y}, DimensionalFlag.XY), CrsId.UNDEFINED.getCode());
    }

    public static Point create3D(double x, double y, double z) {
        return new Point(new PackedPointSequence(new double[]{x, y, z}, DimensionalFlag.XYZ), CrsId.UNDEFINED.getCode());
    }

    public static Point create2DM(double x, double y, double m) {
        return new Point(new PackedPointSequence(new double[]{x, y, m}, DimensionalFlag.XYM), CrsId.UNDEFINED.getCode());
    }

    public static Point create3DM(double x, double y, double z, double m) {
        return new Point(new PackedPointSequence(new double[]{x, y, z, m}, DimensionalFlag.XYZM), CrsId.UNDEFINED.getCode());
    }


    //TODO -- limit visibility to package
    public static Point create(double[] coordinates, DimensionalFlag dimensionalFlag, int SRID) {
        if (coordinates == null || coordinates.length == 0) {
            return EMPTY;
        }
        return new Point(new PackedPointSequence(Arrays.copyOf(coordinates, coordinates.length), dimensionalFlag), SRID);
    }

    Point(PointSequence sequence,int SRID){
        super(SRID);
        this.points = sequence;
    }

    public static Point createEmpty(){
        return EMPTY;
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

    //TODO -- is this still necessary?? If Geometry's equals not enough?
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (! (o instanceof Point)){
            return false;
        }
        Point otherPoint = (Point)o;
        if (this.isEmpty() != otherPoint.isEmpty() ) return false;
        if (this.isEmpty()) return true;
        if (this.isMeasured() != otherPoint.isMeasured()) return false;
        if (this.is3D() != otherPoint.is3D()) return false;
        return this.getSRID() == otherPoint.getSRID() &&
                this.getX() == otherPoint.getX() &&
                this.getY() == otherPoint.getY() &&
                (!is3D() || this.getZ() == otherPoint.getZ()) &&
                (!isMeasured() || this.getM() == otherPoint.getM());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = getX() != +0.0d ? Double.doubleToLongBits(getX()) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = getY() != +0.0d ? Double.doubleToLongBits(getY()) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        if (is3D()) {
            temp = getZ() != +0.0d ? Double.doubleToLongBits(getZ()) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
        }
        if (isMeasured()) {
            temp = getM() != +0.0d ? Double.doubleToLongBits(getM()) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
        }
        result = 31 * result + getSRID();
        return result;
    }

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
