package org.geolatte.geom.json;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.jackson3.GeolatteGeomModule;
import org.geolatte.geom.json.test.AbstractGeoJsonContract;
import org.geolatte.geom.json.test.MapperLike;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Runs the shared {@link AbstractGeoJsonContract} against the Jackson 3 adapter.
 */
public class Jackson3ContractTest extends AbstractGeoJsonContract {

    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        GeolatteGeomModule module = new GeolatteGeomModule(defaultCrs);
        settings.forEach(module::set);

        ObjectMapper mapper = JsonMapper.builder().addModule(module).build();
        return new MapperLike() {
            @Override
            public String writeAsString(Object value) {
                return mapper.writeValueAsString(value);
            }

            @Override
            public <T> T readValue(String json, Class<T> type) {
                return mapper.readValue(json, type);
            }
        };
    }
}
