package org.geolatte.geom.json;

import org.geolatte.geom.Point;
import org.junit.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.lambert72Z;
import static org.geolatte.geom.json.Crss.wgs3D;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.geolatte.geom.json.GeoJsonStrings.emptyPointText;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class PointSerializationTest extends GeoJsonTest {

    @Test
    public void testSerializeEmptyPoint() throws JacksonException {
        Point<?> pnt = new Point(WGS84);
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        String rec = mapper.writeValueAsString(pnt);
        assertEquals(emptyPointText, rec);
    }

    @Test
    public void testSerializeSimplePoint() throws JacksonException {
        Point<?> pnt = point(WGS84, g(1, 2));
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        String rec = mapper.writeValueAsString(pnt);
        assertEquals(GeoJsonStrings.pointText, rec);
    }

    @Test
    public void testSerializeSimplePoint3D() throws JacksonException {
        Point<?> pnt = point(wgs3D, g(1, 2, 3));
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        String rec = mapper.writeValueAsString(pnt);
        assertEquals(GeoJsonStrings.pointText3D, rec);
    }

    @Test
    public void testSerializePointWithCrs() throws JacksonException {
        Point<?> pnt = point(lambert72Z, c(1, 2, 3));
        String rec = mapper.writeValueAsString(pnt);
        assertEquals(GeoJsonStrings.pointTextWithCrs3D, rec);
    }

}
