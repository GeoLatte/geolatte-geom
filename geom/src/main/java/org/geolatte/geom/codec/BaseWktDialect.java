package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.geolatte.geom.GeometryType.*;

public class BaseWktDialect {

    final static Map<GeometryType, Pattern> GEOMETRY_TYPE_PATTERN_MAP = new HashMap<>();

    static {
        addToMap(POINT, "point");
        addToMap(LINESTRING, "linestring");
        addToMap(POLYGON, "polygon");
        addToMap(GEOMETRYCOLLECTION, "geometrycollection");
        addToMap(MULTIPOINT, "multipoint");
        addToMap(MULTILINESTRING, "multilinestring");
        addToMap(MULTIPOLYGON, "multipolygon");
    }

    private static void addToMap(GeometryType tpe, String pattern) {
        GEOMETRY_TYPE_PATTERN_MAP.put(tpe, Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
    }

    public Map<GeometryType, Pattern> geometryTypePatternMap() {
        return Collections.unmodifiableMap(GEOMETRY_TYPE_PATTERN_MAP);
    }

    void addGeometryTag(StringBuffer buffer, Geometry<?> geometry) {
        switch (geometry.getGeometryType()) {
            case POINT:
                buffer.append("POINT");
                return;
            case LINEARRING:
            case LINESTRING:
                buffer.append("LINESTRING");
                return;
            case POLYGON:
                buffer.append("POLYGON");
                return;
            case MULTIPOINT:
                buffer.append("MULTIPOINT");
                return;
            case MULTILINESTRING:
                buffer.append("MULTILINESTRING");
                return;
            case MULTIPOLYGON:
                buffer.append("MULTIPOLYGON");
                return;
            case GEOMETRYCOLLECTION:
                buffer.append("GEOMETRYCOLLECTION");
                return;
            default:
                throw new IllegalStateException(
                        String.format(
                                "Geometry type %s cannot be encoded in WKT.",
                                geometry.getGeometryType().getCamelCased()
                        )
                );
        }
    }


    void addGeometryZMMarker(StringBuffer buffer, Geometry<?> geometry) {
        if (geometry.hasM() && geometry.getCoordinateDimension() < 4)
            buffer.append('M');
    }


    public void addSrid(StringBuffer builder, int srid) {

    }


}
