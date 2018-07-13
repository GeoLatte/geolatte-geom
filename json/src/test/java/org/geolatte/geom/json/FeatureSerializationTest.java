package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.feature;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 13/07/2018.
 */
public class FeatureSerializationTest extends GeoJsonTest {

    @Test
    public void testDeSerialize() throws IOException {

        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Feature<?, ?> f = new GeoJsonFeature<>(point(WGS84, g(1, 2)), "1", map);
        String rec = mapper.writeValueAsString(f);
        assertEquals(feature, rec) ;
    }

}
