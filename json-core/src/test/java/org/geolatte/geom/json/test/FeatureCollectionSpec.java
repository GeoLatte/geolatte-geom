package org.geolatte.geom.json.test;

import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.G2D;
import org.geolatte.geom.json.GeoJsonFeature;
import org.geolatte.geom.json.GeoJsonFeatureCollection;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.emptyFeatureCollection;
import static org.geolatte.geom.json.GeoJsonStrings.featureCollection;
import static org.geolatte.geom.json.GeoJsonStrings.featureCollectionNoBbox;
import static org.geolatte.geom.json.Setting.SERIALIZE_FEATURE_COLLECTION_BBOX;
import static org.junit.Assert.assertEquals;

/**
 * Contract for the GeoJSON {@link FeatureCollection} encode/decode behaviour.
 */
public abstract class FeatureCollectionSpec extends AbstractGeoJsonContract {

    // ─── Serialization ──────────────────────────────────────────────────────

    @Test
    public void serializeFeatureCollection() {
        FeatureCollection<?, ?> fc = mkExampleFeatureCollection();
        String rec = newMapper().writeAsString(fc);
        assertEquals(featureCollectionNoBbox, rec);
    }

    @Test
    public void serializeEmptyFeatureCollection() {
        FeatureCollection<?, ?> fc = new GeoJsonFeatureCollection<>(new ArrayList<>());
        String rec = newMapper().writeAsString(fc);
        assertEquals(emptyFeatureCollection, rec);
    }

    @Test
    public void serializeFeatureCollectionWithBbox() {
        String rec = newMapper(SERIALIZE_FEATURE_COLLECTION_BBOX, true)
                .writeAsString(mkExampleFeatureCollection());
        assertEquals(featureCollection, rec);
    }

    // ─── Deserialization ────────────────────────────────────────────────────

    @Test
    public void deserializeFeatureCollection() {
        FeatureCollection<?, ?> rec = newMapper().readValue(featureCollection, FeatureCollection.class);
        FeatureCollection<?, ?> expected = mkExampleFeatureCollection();
        assertEquals(expected, rec);
    }

    @Test
    public void deserializeEmptyFeatureCollection() {
        FeatureCollection<?, ?> rec = newMapper().readValue(emptyFeatureCollection, FeatureCollection.class);
        FeatureCollection<?, ?> expected = new GeoJsonFeatureCollection<>(new ArrayList<>());
        assertEquals(expected, rec);
    }

    private static FeatureCollection<?, ?> mkExampleFeatureCollection() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("prop0", "value0");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("prop1", 0.0d);
        map2.put("prop0", "value0");
        List<Feature<G2D, String>> features = new ArrayList<>();
        features.add(new GeoJsonFeature<>(point(WGS84, g(102, 0.5)), "1", map1));
        features.add(new GeoJsonFeature<>(linestring(WGS84, g(102, 0), g(103, 1),
                g(104, 0), g(105, 1)), "2", map2));
        return new GeoJsonFeatureCollection<>(features);
    }
}
