package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

public class CrsSupportTest extends GeoJsonTest {
    @Test
    public void testSerializeCrsInUrnFormat() throws IOException {

        ObjectMapper mapper = createMapper(Setting.SERIALIZE_CRS_AS_URN, true);
        assertEquals(crswgs84TextWithUrnCrs, mapper.writeValueAsString(WGS84));
        assertEquals(crslambert72TextWithUrnCrs, mapper.writeValueAsString(lambert72));
    }

    @Test
    public void testSerializeCrsDefault() throws IOException {

        assertEquals(crswgs84, mapper.writeValueAsString(WGS84));
        assertEquals(crslambert72, mapper.writeValueAsString(lambert72));
    }


    @Test
    public void testDeserializeCrs() throws IOException {
        // wgs84
        CoordinateReferenceSystem<?> wgs84rec = mapper.readValue(crswgs84, CoordinateReferenceSystem.class);
        CoordinateReferenceSystem<?> wgs84expected = WGS84;
        assertEquals(wgs84expected, wgs84rec);

        // lambert72
        CoordinateReferenceSystem<?> lambert72rec = mapper.readValue(crslambert72, CoordinateReferenceSystem.class);
        CoordinateReferenceSystem<?> lambert72expected = lambert72;
        assertEquals(lambert72expected, lambert72rec);
    }
}
