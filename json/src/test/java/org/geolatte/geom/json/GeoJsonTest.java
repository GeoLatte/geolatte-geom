package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;

import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
abstract public class GeoJsonTest {

    ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new GeolatteGeomModule());
    }

    public ObjectMapper createMapperWithFeatures(Map<Feature, Boolean> features) {
        GeolatteGeomModule module = new GeolatteGeomModule();
        features.forEach((f, v) -> module.setFeature(f, v));
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    public ObjectMapper createMapperWithFeature(Feature feature, boolean value) {
        GeolatteGeomModule module = new GeolatteGeomModule();
        module.setFeature(feature,value);
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }


}
