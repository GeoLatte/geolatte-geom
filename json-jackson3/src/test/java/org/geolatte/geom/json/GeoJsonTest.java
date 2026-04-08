package org.geolatte.geom.json;

import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.junit.Before;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
abstract public class GeoJsonTest {

    ObjectMapper mapper;

    @Before
    public void setUp() {
        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(new GeolatteGeomModule());
        mapper = builder.build();
    }

    public ObjectMapper createMapper(Map<Setting, Boolean> features) {
        GeolatteGeomModule module = new GeolatteGeomModule();
        features.forEach((f, v) -> module.set(f, v));

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(module);
        mapper = builder.build();
        return mapper;
    }

    public ObjectMapper createMapper(Setting setting, boolean value) {
        GeolatteGeomModule module = new GeolatteGeomModule();
        module.set(setting, value);

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(module);
        mapper = builder.build();
        return mapper;
    }

    public <P extends Position> ObjectMapper createMapper(CoordinateReferenceSystem<P> crs, Setting f, boolean isSet) {
        GeolatteGeomModule module = new GeolatteGeomModule(crs);
        module.set(f, isSet);

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(module);
        mapper = builder.build();
        return mapper;
    }

}
