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
import org.geolatte.geom.Position;

/**
 * @author Karel Maesen, Geovise BVBA
 * creation-date: Jun 30, 2010
 */
class SDOGType {

    private int dimension = 2;

    private int lrsDimension;

    private SdoGeometryType sdoGeometryType = SdoGeometryType.UNKNOWN_GEOMETRY;

    static <P extends Position> SDOGType gtypeFor(Geometry<P> geometry) {
        SdoGeometryType sdoGeometryType = SdoGeometryType.forGeometry(geometry);
        if (sdoGeometryType == null) {
            throw new IllegalStateException("Can't determine TypeGeometry for geometries of type " + geometry.getGeometryType());
        }
        int lrsDimension = geometry.hasM() ? geometry.getCoordinateDimension() : 0;
        return new SDOGType(geometry.getCoordinateDimension(), lrsDimension, sdoGeometryType);
    }


    @Deprecated
    static SDOGType derive(ElementType elementType, SDOGType gtype) {
        switch (elementType) {
            case POINT:
            case ORIENTATION:
                return new SDOGType(
                        gtype.getDimension(), gtype
                        .getLRSDimension(), SdoGeometryType.POINT
                );
            case POINT_CLUSTER:
                return new SDOGType(
                        gtype.getDimension(), gtype
                        .getLRSDimension(), SdoGeometryType.MULTIPOINT
                );
            case LINE_ARC_SEGMENTS:
            case LINE_STRAIGTH_SEGMENTS:
            case COMPOUND_LINESTRING:
                return new SDOGType(
                        gtype.getDimension(), gtype
                        .getLRSDimension(), SdoGeometryType.LINE
                );
            case COMPOUND_EXTERIOR_RING:
            case EXTERIOR_RING_ARC_SEGMENTS:
            case EXTERIOR_RING_CIRCLE:
            case EXTERIOR_RING_RECT:
            case EXTERIOR_RING_STRAIGHT_SEGMENTS:
                return new SDOGType(
                        gtype.getDimension(), gtype
                        .getLRSDimension(), SdoGeometryType.POLYGON
                );
            default:
                return null;
        }
    }

    public SDOGType(int dimension, int lrsDimension, SdoGeometryType sdoGeometryType) {
        setDimension(dimension);
        setLrsDimension(lrsDimension);
        setTypeGeometry(sdoGeometryType);
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        if (dimension < 2 || dimension > 4) {
            throw new IllegalArgumentException("Dimension can only be 2,3 or 4.");
        }
        this.dimension = dimension;
    }

    public SdoGeometryType getTypeGeometry() {
        return sdoGeometryType;
    }

    public void setTypeGeometry(SdoGeometryType sdoGeometryType) {

        this.sdoGeometryType = sdoGeometryType;
    }

    public int getLRSDimension() {
        if (this.lrsDimension > 0) {
            return this.lrsDimension;
        }
        //TODO -- why would this be necessary?
        else if (this.lrsDimension == 0 && this.dimension == 4) {
            return 4;
        }
        return 0;
    }

    public int getZDimension() {
        if (getLRSDimension() == 3 && this.dimension == 3) {
            return -1;
        } else if (getLRSDimension() == 3 && this.dimension == 4) {
            return 4;
        } else if (getLRSDimension() == 4 && this.dimension == 4) {
            return 3;
        } else {
            return this.dimension > 2 ? 3 : -1;
        }
    }

    public boolean isLRSGeometry() {
        return getLRSDimension() > 0;
    }

    public void setLrsDimension(int lrsDimension) {
        if (lrsDimension != 0 && lrsDimension > this.dimension) {
            throw new IllegalArgumentException("lrsDimension must be 0 or lower or equal to dimension.");
        }
        this.lrsDimension = lrsDimension;
    }

    public int intValue() {
        int v = this.dimension * 1000;
        v += lrsDimension * 100;
        v += sdoGeometryType.intValue();
        return v;
    }

    public static SDOGType parse(int v) {
        final int dim = v / 1000;
        v -= dim * 1000;
        final int lrsDim = v / 100;
        v -= lrsDim * 100;
        final SdoGeometryType sdoGeometryType = SdoGeometryType.parse(v);
        return new SDOGType(dim, lrsDim, sdoGeometryType);
    }

    public static SDOGType parse(Object datum) {

        try {
            final int v = ((Number) datum).intValue();
            return parse(v);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String toString() {
        return Integer.toString(this.intValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SDOGType sdogType = (SDOGType) o;

        if (dimension != sdogType.dimension) return false;
        if (lrsDimension != sdogType.lrsDimension) return false;
        return sdoGeometryType == sdogType.sdoGeometryType;
    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + lrsDimension;
        result = 31 * result + (sdoGeometryType != null ? sdoGeometryType.hashCode() : 0);
        return result;
    }
}
