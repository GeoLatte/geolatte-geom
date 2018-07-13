package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public class FeatureDeserializer<P extends Position, ID> extends JsonDeserializer<Feature<P,ID>> {

    final private CoordinateReferenceSystem<P> defaultCrs;
    final private Settings settings;
    private GeometryParser geomParser;

    public FeatureDeserializer(CoordinateReferenceSystem<P> defaultCrs, Settings settings) {
        this.defaultCrs = defaultCrs;
        this.settings = settings;
    }

    @Override
    public Feature<P, ID> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectCodec oc = p.getCodec();
        JsonNode root = oc.readTree(p);
        initGeomParser(ctxt);

        JsonNode geomNode = root.get("geometry");
        Geometry<P> geom = geomParser.parse(geomNode, defaultCrs);

        Object id = null;
        JsonNode idNode = root.get("id");
        if (idNode.canConvertToLong()) {
            id = idNode.asLong();
        } else {
            id = idNode.asText();
        }

        HashMap<String, Object> properties =  (HashMap<String, Object>)oc.treeToValue(root.get("properties"), HashMap.class);

        return new GeoJsonFeature<>(geom, (ID)id, properties);
    }

    private void initGeomParser(DeserializationContext ctxt) throws JsonMappingException {
        JavaType javaType = ctxt.constructType(Geometry.class);
        this.geomParser = (GeometryParser) ctxt.findRootValueDeserializer(javaType);
    }
}
