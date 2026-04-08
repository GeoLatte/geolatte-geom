package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.Setting;
import org.geolatte.geom.json.test.AbstractGeoJsonContract;
import org.geolatte.geom.json.test.MapperLike;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Runs the shared {@link AbstractGeoJsonContract} against the Jackson 2 adapter.
 */
public class Jackson2ContractTest extends AbstractGeoJsonContract {

    @Override
    protected MapperLike newMapper(CoordinateReferenceSystem<?> defaultCrs, Map<Setting, Boolean> settings) {
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
}
