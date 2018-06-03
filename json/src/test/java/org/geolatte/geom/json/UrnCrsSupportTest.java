package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.Point;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.c;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.json.Crss.lambert72;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithUrnCrs;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/09/17.
 */
public class UrnCrsSupportTest extends GeoJsonTest {

    @Test
    public void testSrializePointWithCrsInUrnFormat() throws IOException {

        ObjectMapper mapper = createMapper(Setting.SERIALIZE_CRS_AS_URN, true);
        Point<?> pnt = point(lambert72, c(1, 2));
        assertEquals(pointTextWithUrnCrs, mapper.writeValueAsString(pnt));
    }

}

