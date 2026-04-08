package org.geolatte.geom.json.test;

import org.geolatte.geom.Geometries;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.json.GeoJsonStrings;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.emptyPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.polygonWithCrs;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link Polygon} encode/decode behaviour.
 */
public abstract class PolygonSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeEmptyPolygon() {
        Polygon<?> p = Geometries.mkEmptyPolygon(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(p);
        assertEquals(emptyPolygon, rec);
    }

    @Test
    public void serializeSimplePolygon() {
        Polygon<?> p = polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1)));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(p);
        assertEquals(GeoJsonStrings.polygon, rec);
    }

    @Test
    public void serializePolygonWithCrs() {
        Polygon<?> p = polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1)));
        String rec = newMapper().writeAsString(p);
        assertEquals(polygonWithCrs, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeEmptyPolygon() {
        Polygon<?> rec = newMapper().readValue(emptyPolygon, Polygon.class);
        Polygon<?> exp = new Polygon<>(WGS84);
        assertEquals(exp, rec);
    }

    @Test
    public void deserializeSimplePolygon() {
        Polygon<?> rec = newMapper().readValue(GeoJsonStrings.polygon, Polygon.class);
        Polygon<?> expected = polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1)));
        assertEquals(expected, rec);
    }

    @Test
    public void deserializePolygonWithCrs() {
        Polygon<?> rec = newMapper().readValue(polygonWithCrs, Polygon.class);
        Polygon<?> expected = polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1)));
        assertEquals(expected, rec);
    }
}
