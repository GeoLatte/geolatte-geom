package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.Setting;
import org.geolatte.geom.json.test.MapperLike;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Builds a {@link MapperLike} backed by a Jackson 2 {@code ObjectMapper} with
 * the Geolatte module installed. Used by every {@code Jackson2*Test} concrete
 * spec subclass so that the actual mapper construction code lives in exactly
 * one place per adapter. Wraps Jackson 2's checked {@code IOException} into
 * {@link UncheckedIOException} so the {@code MapperLike} contract stays
 * exception-free.
 */
final class Jackson2MapperFactory {

    static MapperLike create(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
        GeolatteGeomModule module = new GeolatteGeomModule(defaultCrs);
        settings.forEach(module::set);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        return new MapperLike() {
            @Override
            public String writeAsString(Object value) {
                try {
                    return mapper.writeValueAsString(value);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public <T> T readValue(String json, Class<T> type) {
                try {
                    return mapper.readValue(json, type);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    private Jackson2MapperFactory() {
    }
}
