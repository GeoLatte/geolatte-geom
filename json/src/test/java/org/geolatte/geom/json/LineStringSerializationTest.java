package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.LineString;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class LineStringSerializationTest extends GeoJsonTest {


    @Test
    public void testSerializeEmpty() throws JsonProcessingException {

        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        LineString<?> ln = new LineString(WGS84);
        String rec = mapper.writeValueAsString(ln);
        assertEquals(emptyLineString,rec) ;
    }

    @Test
    public void testSerializeSimple() throws JsonProcessingException {

        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        LineString<?> ln = linestring(WGS84, g(1, 2), g(3, 4));
        String rec = mapper.writeValueAsString(ln);
        assertEquals(simpleLineString,rec) ;
    }

    @Test
    public void testSerializeWithCRS() throws JsonProcessingException {
        LineString<?> ln = linestring(Crss.lambert72, c(1, 2), c(3, 4));
        String rec = mapper.writeValueAsString(ln);
        assertEquals(lineStringWithCrs,rec) ;
    }

}
