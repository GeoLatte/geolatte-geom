package org.geolatte.geom.json;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.junit.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithUrnCrs;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 14/02/2020.
 */
public class IgnoreCrsTest extends GeoJsonTest{

    @Test
    public void testSrializePointWithCrsInUrnFormat() throws IOException {

        ObjectMapper mapper = createMapper(Setting.IGNORE_CRS, true);
        Point<?> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt, mapper.readValue( pointTextWithUrnCrs, Geometry.class ));
    }

}

