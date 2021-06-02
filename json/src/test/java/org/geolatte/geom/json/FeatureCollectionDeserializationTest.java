package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.G2D;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.emptyFeatureCollection;
import static org.geolatte.geom.json.GeoJsonStrings.featureCollection;
import static org.junit.Assert.assertEquals;

public class FeatureCollectionDeserializationTest extends GeoJsonTest {

    @Test
    public void testDeSerialize() throws IOException {
        FeatureCollection<?, ?> rec = mapper.readValue(featureCollection, FeatureCollection.class);
        Map<String, Object> map1 = new HashMap<>();
        map1.put("prop0", "value0");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("prop1", 0.0d);
        map2.put("prop0", "value0");
        FeatureCollection<?, ?> fc = new GeoJsonFeatureCollection<>(
                new GeoJsonFeature<G2D,String>(point(WGS84, g(102, 0.5)), "1", map1),
                new GeoJsonFeature<G2D, String>(linestring(WGS84, g(102, 0), g(103, 1),
                        g(104,0), g(105, 1)), "2", map2));
        assertEquals(fc, rec);
    }

    @Test
    public void testEmptyDeserialize() throws IOException {
        FeatureCollection<?, ?> rec = mapper.readValue(emptyFeatureCollection, FeatureCollection.class);
        FeatureCollection<?, ?> fc = new GeoJsonFeatureCollection<>(new ArrayList<>());
        assertEquals(fc, rec);
    }


}