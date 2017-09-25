package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 12/09/17.
 */
public class GeometryCollectionParser<P extends Position> extends AbstractGeometryParser<P, GeometryCollection<P, Geometry<P>>>
        implements ResolvableDeserializer {


    private GeometryParser<P> geomParser;

    public GeometryCollectionParser(Context<P> ctxt) {
        super(ctxt);
    }


    @Override
    public GeometryCollection<P, Geometry<P>> parse(JsonNode root, CoordinateReferenceSystem<P> defaultCrs)
            throws GeoJsonProcessingException {
        JsonNode geometries = root.get("geometries");
        if (geometries.size() == 0) return Geometries.mkEmptyGeometryCollection(resolveCrs(root, 2, defaultCrs));

        List<Geometry<P>> components = new ArrayList<>();
        CoordinateReferenceSystem<P> crs = resolveCrs(root, 2, defaultCrs);

        for (int i = 0; i < geometries.size(); i++) {
            components.add(geomParser.parse(geometries.get(i), crs));
        }
        return Geometries.mkGeometryCollection(components);
    }


    @Override
    protected void canHandle(JsonNode root) throws GeoJsonProcessingException {
        if (!getType(root).equals(GeometryType.GEOMETRYCOLLECTION)) {
            throw new GeoJsonProcessingException(
                    String.format("Can't parse %s with %s", getType(root).getCamelCased(), getClass().getCanonicalName())
            );
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        JavaType javaType = ctxt.constructType(Geometry.class);
        this.geomParser = (GeometryParser) ctxt.findRootValueDeserializer(javaType);
    }
}
