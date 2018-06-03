package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.MultiPolygon;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiPolygonSerializationTest extends GeoJsonTest {

    @Test
    public void testSerializeEmpty() throws JsonProcessingException {

        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        MultiPolygon<?> mp = multipolygon(WGS84);
        String rec = mapper.writeValueAsString(mp);
        assertEquals(emptyMultiPolygon, rec);
    }

    @Test
    public void testSerializeSimple() throws JsonProcessingException {

        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        MultiPolygon<?> mp = multipolygon(
                polygon(WGS84, ring(g(1, 1), g(1, 2), g(2, 2), g(2, 1), g(1, 1))),
                polygon(WGS84, ring(g(3, 3), g(3, 5), g(5, 5), g(5, 3), g(3, 3)))
        );
        String rec = mapper.writeValueAsString(mp);
        assertEquals(multiPolygon, rec);
    }

    @Test
    public void testSerializeWithCRS() throws JsonProcessingException {
        MultiPolygon<?> mp =multipolygon(
                polygon(lambert72, ring(c(1, 1), c(1, 2), c(2, 2), c(2, 1), c(1, 1))),
                polygon(lambert72, ring(c(3, 3), c(3, 5), c(5, 5), c(5, 3), c(3, 3)))
        );
        String rec = mapper.writeValueAsString(mp);
        assertEquals(multiPolygonWithCrs, rec);
    }

}
