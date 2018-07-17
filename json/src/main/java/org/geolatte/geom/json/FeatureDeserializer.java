package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public class FeatureDeserializer extends JsonDeserializer<Feature> {

    final private CoordinateReferenceSystem<?> defaultCrs;
    final private Settings settings;
    final private GeometryDeserializer geomParser;

    public FeatureDeserializer(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.defaultCrs = defaultCrs;
        this.settings = settings;
        geomParser = new GeometryDeserializer(defaultCrs, settings);
    }

    @Override
    public Feature<?,?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectCodec oc = p.getCodec();
        JsonNode root = oc.readTree(p);


        JsonNode geomNode = root.get("geometry");
        Geometry<?> geom = geomParser.parseGeometry(geomNode);

        Object id = null;
        JsonNode idNode = root.get("id");
        if (idNode.canConvertToLong()) {
            id = idNode.asLong();
        } else {
            id = idNode.asText();
        }

        HashMap<String, Object> properties =  (HashMap<String, Object>)oc.treeToValue(root.get("properties"), HashMap.class);

        return new GeoJsonFeature<>(geom, id, properties);
    }


}
