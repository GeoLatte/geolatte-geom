package org.geolatte.geom.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.point;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.pointTextWithUrnCrs;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 14/02/2020.
 */
public class ForceCrsToDefaultTest extends GeoJsonTest {

    @Test
    public void test() throws IOException {

        ObjectMapper mapper = createMapper(Setting.FORCE_DEFAULT_CRS_DIMENSION, true);
        Point<?> pnt = point(WGS84, g(1, 2));
        assertEquals(pnt, mapper.readValue( pointTextWithUrnCrs, Geometry.class ));
    }

}

