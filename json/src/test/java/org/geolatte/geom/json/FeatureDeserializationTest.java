package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public class FeatureDeserializationTest extends GeoJsonTest {

    @Test
    public void testDeSerialize() throws IOException {
        Feature<?,?> rec = mapper.readValue(feature, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> feature = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        assertEquals(feature,rec) ;
        assertEquals(feature.getId(),rec.getId()) ;
        assertEquals(feature.getType(),rec.getType()) ;
        assertEquals(feature.getGeometry(),rec.getGeometry()) ;
        assertEquals(feature.getProperties(),rec.getProperties()) ;
    }

    @Test
    public void testDeSerializeWithIntAsId() throws IOException {
        Feature<?,?> rec = mapper.readValue(featureIntId, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> feature = new GeoJsonFeature<>(point(WGS84, g(1, 2)), 1L, map);
        assertEquals(feature,rec) ;
        assertEquals(feature.getId(),rec.getId()) ;
        assertEquals(feature.getType(),rec.getType()) ;
        assertEquals(feature.getGeometry(),rec.getGeometry()) ;
        assertEquals(feature.getProperties(),rec.getProperties()) ;
    }

    @Test
    public void testDeSerializeWithNullGeometry() throws IOException {
        Feature<?,?> rec = mapper.readValue(featureNullGeometry, Feature.class);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> feature = new GeoJsonFeature<>(null, "1", map);
        assertEquals(feature,rec) ;
        assertEquals(feature.getId(),rec.getId()) ;
        assertEquals(feature.getType(),rec.getType()) ;
        assertEquals(feature.getGeometry(),rec.getGeometry()) ;
        assertEquals(feature.getProperties(),rec.getProperties()) ;
    }

}
