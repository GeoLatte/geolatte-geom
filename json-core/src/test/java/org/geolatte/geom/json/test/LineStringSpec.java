package org.geolatte.geom.json.test;

import org.geolatte.geom.LineString;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.gM;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.Crss.wgs2DM;
import static org.geolatte.geom.json.GeoJsonStrings.emptyLineString;
import static org.geolatte.geom.json.GeoJsonStrings.lineString2DM;
import static org.geolatte.geom.json.GeoJsonStrings.lineStringWithCrs;
import static org.geolatte.geom.json.GeoJsonStrings.simpleLineString;
import static org.geolatte.geom.json.Setting.FORCE_DEFAULT_CRS_DIMENSION;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link LineString} encode/decode behaviour.
 */
public abstract class LineStringSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeEmptyLineString() {
        LineString<?> ln = new LineString(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(ln);
        assertEquals(emptyLineString, rec);
    }

    @Test
    public void serializeSimpleLineString() {
        LineString<?> ln = linestring(WGS84, g(1, 2), g(3, 4));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(ln);
        assertEquals(simpleLineString, rec);
    }

    @Test
    public void serializeLineStringWithCrs() {
        LineString<?> ln = linestring(lambert72, c(1, 2), c(3, 4));
        String rec = newMapper().writeAsString(ln);
        assertEquals(lineStringWithCrs, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeEmptyLineString() {
        LineString<?> rec = newMapper().readValue(emptyLineString, LineString.class);
        LineString<?> expected = new LineString(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeSimpleLineString() {
        LineString<?> rec = newMapper().readValue(simpleLineString, LineString.class);
        LineString<?> expected = linestring(WGS84, g(1, 2), g(3, 4));
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeLineStringWithCrs() {
        LineString<?> rec = newMapper().readValue(lineStringWithCrs, LineString.class);
        LineString<?> expected = linestring(lambert72, c(1, 2), c(3, 4));
        assertEquals(expected, rec);
    }

    @Test
    public void force3DMTo2DMLineString() {
        LineString<?> rec = newMapper(wgs2DM, FORCE_DEFAULT_CRS_DIMENSION, true)
                .readValue(lineString2DM, LineString.class);
        LineString<?> expected = linestring(wgs2DM, gM(1, 2, 3), gM(10, 20, 30));
        assertEquals(expected, rec);
    }
}
