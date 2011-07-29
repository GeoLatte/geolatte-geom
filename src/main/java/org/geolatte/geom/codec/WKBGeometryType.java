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

package org.geolatte.geom.codec;

import org.geolatte.geom.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/19/11
 */
public enum WKBGeometryType {


    POINT(1, Point.class),

//    CURVE(13, Curve.class),

//    SURFACE(14, Surface.class),

    GEOMETRY_COLLECTION(7, GeometryCollection.class),

    LINE_STRING(2, LineString.class),

    LINE(2, LineString.class),

    LINEAR_RING(2, LinearRing.class),

    POLYGON(3, Polygon.class),

    POLYHEDRAL_SURFACE(15, PolyHedralSurface.class),

//    MULTI_SURFACE(12, MultiSurface.class),
//
//    MULTI_CURVE(11, MultiCurve.class),

    MULTI_POINT(4, MultiPoint.class),

//    TIN(16, TIN.class),

    MULTI_POLYGON(6, MultiPolygon.class),

    MULTI_LINE_STRING(5, MultiLineString.class);

    private int wkbType;
    private Class<? extends Geometry> geometryClass;

    private WKBGeometryType(int type, Class<? extends Geometry> geometryClass) {
        this.wkbType = type;
        this.geometryClass = geometryClass;
    }

    public int getTypeCode() {
        return this.wkbType;
    }

    public static WKBGeometryType parse(int typeCode) {
        for (WKBGeometryType type : values()) {
            if (type.wkbType == typeCode) return type;
        }
        throw new IllegalArgumentException("Type code " + typeCode + " is not known.");
    }

    public static WKBGeometryType forClass(Class<? extends Geometry> geomClass) {
        WKBGeometryType candidate = null;
        for (WKBGeometryType type : values()) {
            if (type.geometryClass.isAssignableFrom(geomClass) &&
                    isMoreSpecificClassMatch(candidate, type)) {
                candidate = type;
            }
        }
        return candidate;
    }

    private static boolean isMoreSpecificClassMatch(WKBGeometryType candidate, WKBGeometryType type) {
        return (candidate == null || candidate.geometryClass.isAssignableFrom(type.geometryClass));
    }
}
