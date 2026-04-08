package org.geolatte.geom.json.test;

import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.json.GeoJsonFeature;
import org.geolatte.geom.json.Setting;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.feature;
import static org.geolatte.geom.json.GeoJsonStrings.featureEmptyPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.featureIntId;
import static org.geolatte.geom.json.GeoJsonStrings.featureNullGeometry;
import static org.geolatte.geom.json.GeoJsonStrings.featureWithBBox;
import static org.geolatte.geom.json.GeoJsonStrings.featureWithLineString;
import static org.geolatte.geom.json.Setting.SERIALIZE_FEATURE_BBOX;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link Feature} encode/decode behaviour.
 */
public abstract class FeatureSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeFeature() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        String rec = newMapper().writeAsString(f);
        assertEquals(feature, rec);
    }

    @Test
    public void serializeFeatureWithBBox() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        String rec = newMapper(SERIALIZE_FEATURE_BBOX, true).writeAsString(f);
        assertEquals(featureWithBBox, rec);
    }

    @Test
    public void serializeLineStringFeature() {
        Map<Setting, Boolean> settingsMap = new HashMap<>();
        settingsMap.put(SUPPRESS_CRS_SERIALIZATION, true);
        settingsMap.put(SERIALIZE_FEATURE_BBOX, true);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(linestring(WGS84, g(1, 2), g(3, 4)), "1", map);
        String rec = newMapper(settingsMap).writeAsString(f);
        assertEquals(featureWithLineString, rec);
    }

    @Test
    public void serializeFeatureWithNullGeometry() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(null, "1", map);
        String rec = newMapper().writeAsString(f);
        assertEquals(featureNullGeometry, rec);
    }

    @Test
    public void serializeFeatureWithEmptyPolygonGeometry() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(Geometries.mkEmptyPolygon(WGS84), "1", map);
        String rec = newMapper(SUPPRESS_CRS_SERIALIZATION, true).writeAsString(f);
        assertEquals(featureEmptyPolygon, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeFeature() {
        Feature<?, ?> rec = newMapper().readValue(feature, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> expected = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        assertEquals(expected, rec);
        assertEquals(expected.getId(), rec.getId());
        assertEquals(expected.getType(), rec.getType());
        assertEquals(expected.getGeometry(), rec.getGeometry());
        assertEquals(expected.getProperties(), rec.getProperties());
    }

    @Test
    public void deserializeFeatureWithIntId() {
        Feature<?, ?> rec = newMapper().readValue(featureIntId, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> expected = new GeoJsonFeature<>(point(WGS84, g(1, 2)), 1L, map);
        assertEquals(expected, rec);
        assertEquals(expected.getId(), rec.getId());
        assertEquals(expected.getType(), rec.getType());
        assertEquals(expected.getGeometry(), rec.getGeometry());
        assertEquals(expected.getProperties(), rec.getProperties());
    }

    @Test
    public void deserializeFeatureWithNullGeometry() {
        Feature<?, ?> rec = newMapper().readValue(featureNullGeometry, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> expected = new GeoJsonFeature<>(null, "1", map);
        assertEquals(expected, rec);
        assertEquals(expected.getId(), rec.getId());
        assertEquals(expected.getType(), rec.getType());
        assertEquals(expected.getGeometry(), rec.getGeometry());
        assertEquals(expected.getProperties(), rec.getProperties());
    }
}
