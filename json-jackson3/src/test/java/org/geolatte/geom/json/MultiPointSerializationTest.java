package org.geolatte.geom.json;

import org.geolatte.geom.MultiPoint;
import org.junit.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 11/09/17.
 */
public class MultiPointSerializationTest extends GeoJsonTest {

    @Test
    public void testSerializeEmpty() throws JacksonException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        MultiPoint<?> mp = multipoint(WGS84);
        String rec = mapper.writeValueAsString(mp);
        assertEquals(emptyMultiPoint, rec);
    }

    @Test
    public void testSerializeSimple() throws JacksonException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        MultiPoint<?> mp = multipoint(WGS84, g(1, 2), g(3, 4));
        String rec = mapper.writeValueAsString(mp);
        assertEquals(multiPoint, rec);
    }

    @Test
    public void testSerializeWithCRS() throws JacksonException {
        MultiPoint<?> mp = multipoint(Crss.lambert72, c(1, 2), c(3, 4));
        String rec = mapper.writeValueAsString(mp);
        assertEquals(multiPointWithCrs, rec);
    }

}
