package org.geolatte.geom.json;

import org.geolatte.geom.FeatureCollection;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeatureCollectionSerializationTest extends GeoJsonTest {

    @Test
    public void testSerialize() throws IOException {
        FeatureCollection<?, ?> fc = mkExample();
        String rec = mapper.writeValueAsString(fc);
        assertEquals(featureCollectionNoBbox, rec) ;
    }

    @Test
    public void testEmptySerialize() throws IOException {
        FeatureCollection<?, ?> fc = new GeoJsonFeatureCollection<>(new ArrayList<>());
        String rec = mapper.writeValueAsString(fc);
        assertEquals(emptyFeatureCollection, rec) ;
    }

    @Test
    public void testWithBbox() throws IOException {
        ObjectMapper mapper = createMapper(Setting.SERIALIZE_FEATURE_COLLECTION_BBOX, true);
        String rec = mapper.writeValueAsString(mkExample());
        assertEquals(featureCollection, rec);
    }

    private static FeatureCollection<?, ?> mkExample() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("prop0", "value0");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("prop1", 0.0d);
        map2.put("prop0", "value0");
        return new GeoJsonFeatureCollection<>(
                new GeoJsonFeature<>(point(WGS84, g(102, 0.5)), "1", map1),
                new GeoJsonFeature<>(linestring(WGS84, g(102, 0), g(103, 1),
                        g(104, 0), g(105, 1)), "2", map2));
    }
}
