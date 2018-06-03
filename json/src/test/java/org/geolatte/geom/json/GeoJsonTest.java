package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
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

    public ObjectMapper createMapper(Map<Setting, Boolean> features) {
        GeolatteGeomModule module = new GeolatteGeomModule();
        features.forEach((f, v) -> module.set(f, v));
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    public ObjectMapper createMapper(Setting setting, boolean value) {
        GeolatteGeomModule module = new GeolatteGeomModule();
        module.set(setting, value);
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    public <P extends Position> ObjectMapper createMapper(CoordinateReferenceSystem<P> crs, Setting f, boolean isSet) {
        mapper = new ObjectMapper();
        GeolatteGeomModule module = new GeolatteGeomModule(crs);
        module.set(f, isSet);
        mapper.registerModule(module);
        return mapper;
    }

}
