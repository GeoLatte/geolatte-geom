package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/09/17.
 */
public class GeometryParser<P extends Position> extends AbstractGeometryParser<P, Geometry<P>> implements ResolvableDeserializer{


    final static private Map<GeometryType, Class<?>> typeMap = new HashMap<>();

    static {
        typeMap.put(GeometryType.POINT, Point.class);
        typeMap.put(GeometryType.LINESTRING, LineString.class);
        typeMap.put(GeometryType.GEOMETRYCOLLECTION, GeometryCollection.class);
        typeMap.put(GeometryType.POLYGON, Polygon.class);
        typeMap.put(GeometryType.MULTIPOINT, MultiPoint.class);
        typeMap.put(GeometryType.MULTILINESTRING, MultiLineString.class);
        typeMap.put(GeometryType.MULTIPOLYGON, MultiPolygon.class);
    }

    private Map<GeometryType, AbstractGeometryParser<?,?>> parsers = new HashMap<>();

    public GeometryParser(CoordinateReferenceSystem<P> defaultCRS,Settings settings) {
        super(defaultCRS, settings);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Geometry<P> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs) throws GeoJsonProcessingException {
        return ((AbstractGeometryParser<P,?>)parsers.get(getType(root))).parse(root, defaultCrs);
    }

    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (parsers.get(getType(root)) == null) throw new GeoJsonProcessingException("Can't find Parser for tpe " + getType(root));
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        for ( Map.Entry<GeometryType, Class<?>> kv : typeMap.entrySet()) {
            JavaType jtpe = ctxt.constructType(kv.getValue());
            parsers.put(kv.getKey(), (AbstractGeometryParser)ctxt.findRootValueDeserializer(jtpe));
        }
    }
}
