package org.geolatte.geom.codec;

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

}
