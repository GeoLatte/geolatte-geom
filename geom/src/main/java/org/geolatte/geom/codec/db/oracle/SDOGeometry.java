/*
 * This file is part of Hibernate Spatial, an extension to the
 *  hibernate ORM solution for spatial (geographic) data.
 *
 *  Copyright © 2007-2012 Geovise BVBA
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Karel Maesen, Geovise BVBA
 * creation-date: Jun 30, 2010
 */

public class SDOGeometry {

    private static final String SQL_TYPE_NAME = "MDSYS.SDO_GEOMETRY";
    private static final String SQL_POINT_TYPE_NAME = "MDSYS.SDO_POINT_TYPE";
    final private SDOGType gtype;
    final private int srid;
    final private SDOPoint point;
    final private ElemInfo info;
    final private Ordinates ordinates;
    final private List<Element> elements;
    final private CoordinateReferenceSystem<?> crs;

    SDOGeometry(SDOGType gtype, int srid, SDOPoint point, ElemInfo info, Ordinates ordinates) {
        this.gtype = gtype;
        this.srid = srid;
        this.point = point;
        this.info = info;
        this.ordinates = ordinates;
        this.crs = determineCrs(srid, gtype);
        if (info != null) {
            this.elements = info.interpret(gtype, ordinates.getOrdinateArray());
        } else {
            // else this must be a point
            elements = new ArrayList<>();
        }
    }

    public static String getTypeName() {
        return SQL_TYPE_NAME;
    }

    public static String getPointTypeName() {
        return SQL_POINT_TYPE_NAME;
    }

    static String arrayToString(Object array) {
        if (array == null || java.lang.reflect.Array.getLength(array) == 0) {
            return "()";
        }
        final int length = java.lang.reflect.Array.getLength(array);
        final StringBuilder stb = new StringBuilder();
        stb.append("(").append(java.lang.reflect.Array.get(array, 0));
        for (int i = 1; i < length; i++) {
            stb.append(",").append(java.lang.reflect.Array.get(array, i));
        }
        stb.append(")");
        return stb.toString();
    }

    static void shiftOrdinateOffset(ElemInfo elemInfo, int offset) {
        for (int i = 0; i < elemInfo.getNumTriplets(); i++) {
            final int newOffset = elemInfo.getOrdinatesOffset(i) + offset;
            elemInfo.setOrdinatesOffset(i, newOffset);
        }
    }


    public static SDOGeometry load(Struct struct) {

        Object[] data;
        try {
            data = struct.getAttributes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new SDOGeometry(SDOGType.parse(data[0]), parseSRID(data[1]), data[2] == null ? null : new SDOPoint((Struct) data[2]), new ElemInfo((Array) data[3]), new Ordinates((Array) data[4]));

    }

    public ElemInfo getInfo() {
        return info;
    }

    public SDOGType getGType() {
        return gtype;
    }

    public SDOPoint getPoint() {
        return point;
    }

    public int getSRID() {
        return srid;
    }

    private static int parseSRID(Object datum) {
        if (datum == null) {
            return 0;
        }
        try {
            return ((Number) datum).intValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getDimension() {
        return gtype.getDimension();
    }

    public int getLRSDimension() {
        return gtype.getLRSDimension();
    }

    public int getZDimension() {
        return gtype.getZDimension();
    }

    public CoordinateReferenceSystem<?> getCoordinateReferenceSystem() {
        return this.crs;
    }

    public Ordinates getOrdinates() {
        return this.ordinates;
    }

    public int getNumElements() {
        return elements.size();
    }

    public Element getElement(int i) {
        return elements.get(i);
    }

    List<Element> getElements() {
        return elements;
    }

    public PositionSequence<?> getElementPositions(int i) {
        return elements.get(i).linearizedPositions(gtype, crs);
    }

    private static CoordinateReferenceSystem<?> determineCrs(int srid, SDOGType gtype) {
        CoordinateReferenceSystem<?> crs = CrsRegistry.ifAbsentReturnProjected2D(srid);

        return CoordinateReferenceSystems.adjustTo(crs, gtype.getZDimension() > 0, gtype.getLRSDimension() > 0);
    }

    public String toString() {
        return "(" + gtype + "," + srid + "," + point + "," + info + "," + ordinates + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SDOGeometry that = (SDOGeometry) o;

        if (srid != that.srid) return false;
        if (!Objects.equals(gtype, that.gtype)) return false;
        if (!Objects.equals(info, that.info)) return false;
        if (!Objects.equals(ordinates, that.ordinates)) return false;
        return Objects.equals(point, that.point);
    }

    @Override
    public int hashCode() {
        int result = gtype != null ? gtype.hashCode() : 0;
        result = 31 * result + srid;
        result = 31 * result + (point != null ? point.hashCode() : 0);
        result = 31 * result + (info != null ? info.hashCode() : 0);
        result = 31 * result + (ordinates != null ? ordinates.hashCode() : 0);
        return result;
    }

}
