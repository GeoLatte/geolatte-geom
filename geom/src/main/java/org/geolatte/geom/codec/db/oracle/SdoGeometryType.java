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

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;

/**
 * @author Karel Maesen, Geovise BVBA
 * creation-date: Jul 1, 2010
 */
enum SdoGeometryType {

    UNKNOWN_GEOMETRY(0, null) {
        @Override
        AbstractSDODecoder createDecoder() {
            throw new UnsupportedOperationException();
        }
    },
    POINT(1, GeometryType.POINT) {
        @Override
        AbstractSDODecoder createDecoder() {
            return new PointSdoDecoder();
        }
    },
    LINE(2, GeometryType.LINESTRING) {
        @Override
        AbstractSDODecoder createDecoder() {
            return new LineStringSdoDecoder();
        }
    },
    POLYGON(3, GeometryType.POLYGON) {
        @Override
        AbstractSDODecoder createDecoder() {
            return new PolygonSdoDecoder();
        }
    },
    COLLECTION(4, GeometryType.GEOMETRYCOLLECTION) {
        @Override
        AbstractSDODecoder createDecoder() {
            return new GeometryCollectionSdoDecoder();
        }
    },
    MULTIPOINT(5, GeometryType.MULTIPOINT) {
        @Override
        AbstractSDODecoder createDecoder() {
            return new MultiPointSdoDecoder();
        }
    },
    MULTILINE(6, GeometryType.MULTILINESTRING) {
        @Override
        AbstractSDODecoder createDecoder() {
            return new MultiLineSdoDecoder();
        }
    },
    MULTIPOLYGON(7, GeometryType.MULTIPOLYGON) {
        @Override
        AbstractSDODecoder createDecoder() {
            return new MultiPolygonSdoDecoder();
        }
    },
    SOLID(8, null) {
        @Override
        AbstractSDODecoder createDecoder() {
            throw new UnsupportedOperationException();
        }
    },
    MULTISOLID(9, null) {
        @Override
        AbstractSDODecoder createDecoder() {
            throw new UnsupportedOperationException();
        }
    };

    private final int gtype;
    private final GeometryType matchingGeometryType;

    SdoGeometryType(int gtype, GeometryType geometryType) {

        this.gtype = gtype;
        this.matchingGeometryType = geometryType;
    }

    int intValue() {
        return this.gtype;
    }

    static SdoGeometryType parse(int v) {
        for (SdoGeometryType gt : values()) {
            if (gt.intValue() == v) {
                return gt;
            }
        }
        throw new RuntimeException(
                "Value " + v
                        + " isn't a valid TypeGeometry value"
        );
    }

    static <P extends Position> SdoGeometryType forGeometry(Geometry<P> geom){
        if (geom == null) {
            return null;
        }
        for (SdoGeometryType gt: values()) {
            if (gt.matchingGeometryType != null && gt.matchingGeometryType.equals(geom.getGeometryType())) {
                return gt;
            }
        }
        return null;
    }

    abstract AbstractSDODecoder createDecoder();

    /**
     * Returns the corresponding {@code GeometryType}, or null when no such type exists
     *
     * @return the corresponding {@code GeometryType}, or null when no such type exists
     */
    public GeometryType getMatchingGeometryType() {
        return matchingGeometryType;
    }
}
