package org.geolatte.geom.json.test;

import org.geolatte.geom.MultiLineString;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.multilinestring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.emptyMultiLineString;
import static org.geolatte.geom.json.GeoJsonStrings.multiLineString;
import static org.geolatte.geom.json.GeoJsonStrings.multiLineStringWithCrs;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link MultiLineString} encode/decode behaviour.
 */
public abstract class MultiLineStringSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeEmptyMultiLineString() {
        MultiLineString<?> mls = new MultiLineString(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mls);
        assertEquals(emptyMultiLineString, rec);
    }

    @Test
    public void serializeSimpleMultiLineString() {
        MultiLineString<?> mls = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mls);
        assertEquals(multiLineString, rec);
    }

    @Test
    public void serializeMultiLineStringWithCrs() {
        MultiLineString<?> mls = multilinestring(
                linestring(lambert72, c(1, 1), c(1, 2)),
                linestring(lambert72, c(3, 4), c(5, 6))
        );
        String rec = newMapper().writeAsString(mls);
        assertEquals(multiLineStringWithCrs, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeEmptyMultiLineString() {
        MultiLineString<?> rec = newMapper().readValue(emptyMultiLineString, MultiLineString.class);
        MultiLineString<?> expected = new MultiLineString<>(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeSimpleMultiLineString() {
        MultiLineString<?> rec = newMapper().readValue(multiLineString, MultiLineString.class);
        MultiLineString<?> expected = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeMultiLineStringWithCrs() {
        MultiLineString<?> rec = newMapper().readValue(multiLineStringWithCrs, MultiLineString.class);
        MultiLineString<?> expected = multilinestring(
                linestring(lambert72, c(1, 1), c(1, 2)),
                linestring(lambert72, c(3, 4), c(5, 6))
        );
        assertEquals(expected, rec);
    }
}
