package org.geolatte.geom.json.test;

import org.geolatte.geom.MultiPoint;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.multipoint;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.emptyMultiPoint;
import static org.geolatte.geom.json.GeoJsonStrings.multiPoint;
import static org.geolatte.geom.json.GeoJsonStrings.multiPointWithCrs;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link MultiPoint} encode/decode behaviour.
 */
public abstract class MultiPointSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeEmptyMultiPoint() {
        MultiPoint<?> mp = multipoint(WGS84);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(emptyMultiPoint, rec);
    }

    @Test
    public void serializeSimpleMultiPoint() {
        MultiPoint<?> mp = multipoint(WGS84, g(1, 2), g(3, 4));
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(mp);
        assertEquals(multiPoint, rec);
    }

    @Test
    public void serializeMultiPointWithCrs() {
        MultiPoint<?> mp = multipoint(lambert72, c(1, 2), c(3, 4));
        String rec = newMapper().writeAsString(mp);
        assertEquals(multiPointWithCrs, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeEmptyMultiPoint() {
        MultiPoint<?> rec = newMapper().readValue(emptyMultiPoint, MultiPoint.class);
        MultiPoint<?> expected = new MultiPoint(WGS84);
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeSimpleMultiPoint() {
        MultiPoint<?> rec = newMapper().readValue(multiPoint, MultiPoint.class);
        MultiPoint<?> expected = multipoint(WGS84, g(1, 2), g(3, 4));
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeMultiPointWithCrs() {
        MultiPoint<?> rec = newMapper().readValue(multiPointWithCrs, MultiPoint.class);
        MultiPoint<?> expected = multipoint(lambert72, c(1, 2), c(3, 4));
        assertEquals(expected, rec);
    }
}
