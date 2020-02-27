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

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jun 30, 2010
 */

public class SDOGeometry {

    private static final String SQL_TYPE_NAME = "MDSYS.SDO_GEOMETRY";
    private static final String SQL_POINT_TYPE_NAME = "MDSYS.SDO_POINT_TYPE";
    final private SDOGType gtype;
    final private int srid;
    final private SDOPoint point;
    final private ElemInfo info;
    final private Ordinates ordinates;

    public SDOGeometry(SDOGType gtype, int srid, SDOPoint point, ElemInfo info, Ordinates ordinates) {
        this.gtype = gtype;
        this.srid = srid;
        this.point = point;
        this.info = info;
        this.ordinates = ordinates;
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
        for (int i = 0; i < elemInfo.getSize(); i++) {
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

        return new SDOGeometry(SDOGType.parse(data[0]), parseSRID(data[1]),
                data[2] == null ? null : new SDOPoint((Struct) data[2]), new ElemInfo((Array) data[3]),
                new Ordinates( (Array) data[4] )
        );

    }

    public ElemInfo getInfo() {
        return info;
    }

    public SDOGType getGType() {
        return gtype;
    }

    public Ordinates getOrdinates() {
        return ordinates;
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

    public boolean isLRSGeometry() {
        return gtype.isLRSGeometry();
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

    /**
     * Gets the number of elements or compound elements.
	 *
     * Subelements of a compound element are not counted.
     *
     * @return the number of elements
     */
    public int getNumElements() {
        int cnt = 0;
        int i = 0;
        while (i < info.getSize()) {
            if (info.getElementType(i).isCompound()) {
                final int numCompounds = info.getNumCompounds(i);
                i += 1 + numCompounds;
            } else {
                i++;
            }
            cnt++;
        }
        return cnt;
    }

    public String toString() {
        final StringBuilder stb = new StringBuilder();
        stb.append("(").append(gtype).append(",").append(srid).append(",").append(point).append(",").append(info)
                .append(",").append(ordinates).append(")");
        return stb.toString();
    }

    /**
	 * If this SDOGeometry is a COLLECTION, this method returns an array of
	 * the SDO_GEOMETRIES that make up the collection. If not a Collection,
	 * an array containing this SDOGeometry is returned.
     *
     * @return collection elements as individual SDO_GEOMETRIES
     */
    public SDOGeometry[] getElementGeometries() {
        if (getGType().getTypeGeometry() == TypeGeometry.COLLECTION) {
            final List<SDOGeometry> elements = new ArrayList<SDOGeometry>();
            int i = 0;
            ElemInfoTriplet[] elemInfos = this.info.interpret();
            while (i < this.getNumElements()) {
                ElemInfoTriplet elemInfo = elemInfos[i];
                ElementType et = elemInfo.getElementType();
                int next = findNextGeometryElemInfo(i, elemInfos, elemInfo, et);

                final SDOGType elemGtype = SDOGType.derive(et, this);
                final ElemInfo outElemInfo = buildElemInfo(elemInfos, i, next);
                Ordinates elemOrdinates = buildOrdinates(elemInfos, i, next);
                SDOGeometry elemGeom = new SDOGeometry(
                        elemGtype,
                        this.getSRID(),
                        null,
                        outElemInfo,
                        elemOrdinates
                );
                elements.add(elemGeom);
                i = next;
            }
            return elements.toArray(new SDOGeometry[elements.size()]);
        } else {
            return new SDOGeometry[]{this};
        }
    }

    private Ordinates buildOrdinates(ElemInfoTriplet[] elemInfos, int start, int next) {
        Double[] ordinateArray = this.ordinates.getOrdinateArray();
        int length;
        int srcPos = elemInfos[start].getStartingOffset();
        if(next < elemInfos.length){
            length = elemInfos[next].getStartingOffset() - srcPos;
        } else {
            length = (ordinateArray.length + 1) - srcPos;
        }
        Double[] out = new Double[length];
        System.arraycopy(ordinateArray, srcPos-1, out, 0, length);
        return new Ordinates(out);
    }


    //start is the next Geometry in the collection
    private ElemInfo buildElemInfo(ElemInfoTriplet[] elemInfos, int start, int next) {
        List<BigDecimal> out = new ArrayList<>(3*(next - start));
        int startingOffset = elemInfos[start].getStartingOffset();
        for (int idx = start; idx < next; idx++) {
            ElemInfoTriplet adjusted = elemInfos[idx].shiftStartingOffset(-startingOffset + 1);
            adjusted.addTo(out);
        }
        return new ElemInfo(out.toArray(new BigDecimal[0]));
    }

    private int findNextGeometryElemInfo(int i, ElemInfoTriplet[] elemInfos, ElemInfoTriplet elemInfo, ElementType et) {
        int next = i + 1;
        // if the element is an exterior ring, or a compound
        // element, then this geometry spans multiple elements.
        if (et.isExteriorRing()) {
            // then next element is the
            // first non-interior ring
            while (next < elemInfos.length) {
                if (!elemInfos[next].getElementType().isInteriorRing()) {
                    break;
                }
                next++;
            }
        } else if (elemInfo.isCompound()) {
            next = i + ((CompoundIElemInfoTriplet)elemInfo).numParts() + 1;
        }
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SDOGeometry that = (SDOGeometry) o;

        if (srid != that.srid)
            return false;
        if (gtype != null ? !gtype.equals(that.gtype) : that.gtype != null)
            return false;
        if (info != null ? !info.equals(that.info) : that.info != null)
            return false;
        if (ordinates != null ? !ordinates.equals(that.ordinates) : that.ordinates != null)
            return false;
        if (point != null ? !point.equals(that.point) : that.point != null)
            return false;

        return true;
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
