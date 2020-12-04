package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Sfa110WktVariant extends WktGeomVariant{

    public static final HashSet<WktGeometryToken> GEOMETRY_WORDS = new HashSet<>();

    static {
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.POINT));
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.LINESTRING));
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.LINEARRING, "LINESTRING"));
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.POLYGON));
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.GEOMETRYCOLLECTION));
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.MULTIPOINT));
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.MULTILINESTRING));
        GEOMETRY_WORDS.add(WktGeometryToken.forType(GeometryType.MULTIPOLYGON));
    }

    @Override
    protected Set<WktKeywordToken> getWktKeywords() {
        return Collections.unmodifiableSet(GEOMETRY_WORDS);
    }

    @Override
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

    @Override
    void addGeometryZMMarker(StringBuffer buffer, Geometry<?> geometry) {
        //do nothing;
    }

    @Override
    public void addSrid(StringBuffer builder, int srid) {
        //donot write srid
    }

}
