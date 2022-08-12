package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;

class PostgisWkbV2Dialect extends PostgisWkbV1Dialect {

    final public static PostgisWkbV2Dialect INSTANCE = new PostgisWkbV2Dialect();

    @Override
    boolean emptyPointAsNaN() {
        return true;
    }

    @Override
    protected <P extends Position> Long geometryTypeCode(Geometry<P> geometry) {
        if (geometry.isEmpty() && geometry.getGeometryType() == GeometryType.POINT) {
            return (long) WKB_POINT;
        }
        return super.geometryTypeCode(geometry);
    }
}
