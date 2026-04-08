package org.geolatte.geom.json;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.jackson3.GeolatteGeomModule;
import org.geolatte.geom.json.test.MapperLike;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Builds a {@link MapperLike} backed by a Jackson 3 {@code JsonMapper} with the
 * Geolatte module installed. Used by every {@code Jackson3*Test} concrete spec
 * subclass so that the actual mapper construction code lives in exactly one
 * place per adapter.
 */
final class Jackson3MapperFactory {

    static MapperLike create(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
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

    private Jackson3MapperFactory() {
    }
}
