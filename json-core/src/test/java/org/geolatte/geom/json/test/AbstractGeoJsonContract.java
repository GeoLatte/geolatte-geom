package org.geolatte.geom.json.test;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.json.Setting;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.junit.Assert.assertEquals;

/**
 * Behavioural contract for the GeoJSON encode/decode pipeline.
 *
 * <p>This abstract test class is shared (via the {@code geolatte-geojson-core} test-jar)
 * by both the Jackson 2 and Jackson 3 adapter modules. Each adapter provides a concrete
 * subclass that implements {@link #newMapper(Map)} by constructing a native
 * {@code ObjectMapper} with the corresponding {@code GeolatteGeomModule} installed.</p>
 *
 * <p>The contract intentionally covers the cross-cutting cases that exercise the SPI
 * surface (streaming writer, tree-model reader, POJO delegation hooks). Comprehensive
 * per-geometry-type coverage lives in the adapter modules' own test sources.</p>
 */
public abstract class AbstractGeoJsonContract {

    /**
     * Returns a freshly-constructed mapper with the given setting overrides applied.
     * Implementations create a new mapper per call to keep tests isolated.
     */
    protected abstract MapperLike newMapper(Map<Setting, Boolean> settings);

    protected MapperLike newMapper() {
        return newMapper(Collections.emptyMap());
    }

    // ─── Round-trip tests ────────────────────────────────────────────────────

    @Test
    public void roundTripPoint() {
        Point<G2D> input = point(WGS84, g(1, 2));
        MapperLike mapper = newMapper();
        String json = mapper.writeAsString(input);
        Point<?> decoded = mapper.readValue(json, Point.class);
        assertEquals(input, decoded);
    }

    @Test
    public void roundTripLineString() {
        LineString<G2D> input = linestring(WGS84, g(1, 2), g(3, 4), g(5, 6));
        MapperLike mapper = newMapper();
        String json = mapper.writeAsString(input);
        LineString<?> decoded = mapper.readValue(json, LineString.class);
        assertEquals(input, decoded);
    }

    @Test
    public void roundTripPolygon() {
        Polygon<G2D> input = polygon(ring(WGS84, g(0, 0), g(1, 0), g(1, 1), g(0, 1), g(0, 0)));
        MapperLike mapper = newMapper();
        String json = mapper.writeAsString(input);
        Polygon<?> decoded = mapper.readValue(json, Polygon.class);
        assertEquals(input, decoded);
    }

    @Test
    public void roundTripGeometryViaInterface() {
        Geometry<G2D> input = point(WGS84, g(1, 2));
        MapperLike mapper = newMapper();
        String json = mapper.writeAsString(input);
        Geometry<?> decoded = mapper.readValue(json, Geometry.class);
        assertEquals(input, decoded);
    }

    @Test
    public void serializesPointWithCrsByDefault() {
        Point<G2D> input = point(WGS84, g(1, 2));
        String json = newMapper().writeAsString(input);
        // The default behaviour includes a "crs" member; we just assert its presence.
        // The exact serialized form is asserted in the per-adapter tests.
        org.junit.Assert.assertTrue(
                "expected default serialization to include a crs member, was: " + json,
                json.contains("\"crs\""));
    }

    @Test
    public void suppressesCrsWhenSettingIsSet() {
        Point<G2D> input = point(WGS84, g(1, 2));
        String json = newMapper(Collections.singletonMap(Setting.SUPPRESS_CRS_SERIALIZATION, true))
                .writeAsString(input);
        org.junit.Assert.assertFalse(
                "expected suppressed serialization to omit the crs member, was: " + json,
                json.contains("\"crs\""));
        assertEquals("{\"type\":\"Point\",\"coordinates\":[1.0,2.0]}", json);
    }
}
