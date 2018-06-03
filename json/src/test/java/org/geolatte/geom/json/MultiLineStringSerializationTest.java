package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.MultiLineString;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiLineStringSerializationTest extends GeoJsonTest {


    @Test
    public void testSerializeEmpty() throws JsonProcessingException {

        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        MultiLineString<?> mls = new MultiLineString(WGS84);
        String rec = mapper.writeValueAsString(mls);
        assertEquals(emptyMultiLineString,rec) ;
    }

    @Test
    public void testSerializeSimple() throws JsonProcessingException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        MultiLineString<?> mls = multilinestring(
                linestring(WGS84, g(1, 1), g(1, 2)),
                linestring(WGS84, g(3, 4), g(5, 6))
        );
        String rec = mapper.writeValueAsString(mls);
        assertEquals(multiLineString,rec) ;
    }

    @Test
    public void testSerializeWithCRS() throws JsonProcessingException {
        MultiLineString<?> mls = multilinestring(
                linestring(Crss.lambert72, c(1, 1), c(1, 2)),
                linestring(Crss.lambert72, c(3, 4), c(5, 6))
        );
        String rec = mapper.writeValueAsString(mls);
        assertEquals(multiLineStringWithCrs,rec) ;
    }

}