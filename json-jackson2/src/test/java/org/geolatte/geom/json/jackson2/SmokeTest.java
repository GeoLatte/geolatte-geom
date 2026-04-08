package org.geolatte.geom.json.jackson2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.junit.Before;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

/**
 * Minimal end-to-end check that the Jackson 2 adapter wires the core readers and writers
 * correctly. The full behavioral test suite lives in the shared contract (PR 11).
 */
public class SmokeTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new GeolatteGeomModule());
    }

    @Test
    public void roundTripsPoint() throws Exception {
        Point<G2D> input = point(WGS84, g(1, 2));
        String json = mapper.writeValueAsString(input);
        Point<?> decoded = mapper.readValue(json, Point.class);
        assertEquals(input, decoded);
    }

    @Test
    public void serializesPointWithoutCrsWhenSuppressed() throws Exception {
        GeolatteGeomModule module = new GeolatteGeomModule();
        module.set(org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION, true);
        ObjectMapper m = new ObjectMapper();
        m.registerModule(module);

        Point<G2D> input = point(WGS84, g(1, 2));
        String json = m.writeValueAsString(input);
        assertEquals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}", json);
    }
}
