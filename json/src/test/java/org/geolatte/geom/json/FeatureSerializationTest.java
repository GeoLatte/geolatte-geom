package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Geometries;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.linestring;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.feature;
import static org.geolatte.geom.json.GeoJsonStrings.featureEmptyPolygon;
import static org.geolatte.geom.json.GeoJsonStrings.featureNullGeometry;
import static org.geolatte.geom.json.GeoJsonStrings.featureWithLineString;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public class FeatureSerializationTest extends GeoJsonTest {

    @Test
    public void testSerialize() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        String rec = mapper.writeValueAsString(f);
        assertEquals(feature, rec);
    }

    @Test
    public void testSerializeLineStringFeature() throws IOException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(linestring(WGS84, g(1, 2), g(3, 4)), "1", map);
        String rec = mapper.writeValueAsString(f);
        assertEquals(featureWithLineString, rec);
    }

    @Test
    public void testSerializeNullGeometry() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(null, "1", map);
        String rec = mapper.writeValueAsString(f);
        assertEquals(featureNullGeometry, rec);
    }

    @Test
    public void testSerializeEmptyPolygon() throws IOException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(Geometries.mkEmptyPolygon(WGS84), "1", map);
        String rec = mapper.writeValueAsString(f);
        assertEquals(featureEmptyPolygon, rec);
    }
}
