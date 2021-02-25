package org.geolatte.geom.codec;

import org.geolatte.geom.GeometryType;

import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.GeometryType.*;

class WkbDialect {

    private final Map<Long, GeometryType> typemap= new HashMap<>();

    protected WkbDialect() {
        typemap.put(1L, POINT);
        typemap.put(2L, LINESTRING);
        typemap.put(3L, POLYGON);
        typemap.put(4L, MULTIPOINT);
        typemap.put(5L, MULTILINESTRING);
        typemap.put(6L, MULTIPOLYGON);
        typemap.put(7L, GEOMETRYCOLLECTION);
    }

    GeometryType parseType(long tpe) {
        GeometryType gt = typemap.get(tpe);
        if (gt == null) throw new WkbDecodeException("Unsupported WKB type code: " + tpe);
        return gt;
    }

    boolean hasZ(long tpe) {
        return false;
    }

    boolean hasM(long tpe) {
        return false;
    }


}
