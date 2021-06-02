package org.geolatte.geom.json;

import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.G2D;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeatureCollectionSerializationTest extends GeoJsonTest {

    @Test
    public void testSerialize() throws IOException {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("prop0", "value0");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("prop1", 0.0d);
        map2.put("prop0", "value0");
        FeatureCollection<?, ?> fc = new GeoJsonFeatureCollection<>(
                new GeoJsonFeature<G2D, String>(point(WGS84, g(102, 0.5)), "1", map1),
                new GeoJsonFeature<G2D, String>(linestring(WGS84, g(102, 0), g(103, 1),
                        g(104, 0), g(105, 1)), "2", map2));
        String rec = mapper.writeValueAsString(fc);
        assertEquals(featureCollection, rec) ;
    }

    @Test
    public void testEmptySerialize() throws IOException {
        FeatureCollection<?, ?> fc = new GeoJsonFeatureCollection<>(new ArrayList<>());
        String rec = mapper.writeValueAsString(fc);
        assertEquals(emptyFeatureCollection, rec) ;
    }
}
