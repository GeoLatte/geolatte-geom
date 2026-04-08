package org.geolatte.geom.json.test;

import org.geolatte.geom.MultiPolygon;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.multipolygon;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.emptyMultiPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.multiPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.multiPolygonWithCrs;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link MultiPolygon} encode/decode behaviour.
 */
public abstract class MultiPolygonSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeEmptyMultiPolygon() {
        MultiPolygon<?> mp = multipolygon(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(emptyMultiPolygon, rec);
    }

    @Test
    public void serializeSimpleMultiPolygon() {
        MultiPolygon<?> mp = multipolygon(
                polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1))),
                polygon(WGS84, ring(g(3, 3), g(3, 5), g(5, 5), g(5, 3), g(3, 3)))
        );
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(multiPolygon, rec);
    }

    @Test
    public void serializeMultiPolygonWithCrs() {
        MultiPolygon<?> mp = multipolygon(
                polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1))),
                polygon(lambert72, ring(c(3, 3), c(3, 5), c(5, 5), c(5, 3), c(3, 3)))
        );
        String rec = newMapper().writeAsString(mp);
        assertEquals(multiPolygonWithCrs, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeEmptyMultiPolygon() {
        MultiPolygon<?> rec = newMapper().readValue(emptyMultiPolygon, MultiPolygon.class);
        MultiPolygon<?> exp = new MultiPolygon(WGS84);
        assertEquals(exp, rec);
    }

    @Test
    public void deserializeSimpleMultiPolygon() {
        MultiPolygon<?> rec = newMapper().readValue(multiPolygon, MultiPolygon.class);
        MultiPolygon<?> expected = multipolygon(
                polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1))),
                polygon(WGS84, ring(g(3, 3), g(3, 5), g(5, 5), g(5, 3), g(3, 3)))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeMultiPolygonWithCrs() {
        MultiPolygon<?> rec = newMapper().readValue(multiPolygonWithCrs, MultiPolygon.class);
        MultiPolygon<?> expected = multipolygon(
                polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1))),
                polygon(lambert72, ring(c(3, 3), c(3, 5), c(5, 5), c(5, 3), c(3, 3)))
        );
        assertEquals(expected, rec);
    }
}
