package org.geolatte.geom.json;

import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.GeometryCollection;
import org.junit.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Setting.SUPPRESS_CRS_SERIALIZATION;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/09/17.
 */
public class GeometryCollectionSerializationTest extends GeoJsonTest {

    @Test
    public void testSerializeEmpty() throws JacksonException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        GeometryCollection<?> geom = new GeometryCollection<>(WGS84);
        String rec = mapper.writeValueAsString(geom);
        assertEquals(emptyGeometryCollection, rec);
    }


    @Test
    public void testSerializeSimple() throws JacksonException {
        ObjectMapper mapper = createMapper(SUPPRESS_CRS_SERIALIZATION, true);
        AbstractGeometryCollection<?, ?> geom = geometrycollection(
                linestring(WGS84, g(1, 1), g(1, 2)),
                point(WGS84, g(5, 6))
        );
        String rec = mapper.writeValueAsString(geom);
        assertEquals(GeoJsonStrings.geometryCollection, rec);
    }

    @Test
    public void testSerializeWithCrs() throws JacksonException {
        AbstractGeometryCollection<?, ?> geom = geometrycollection(
                linestring(Crss.lambert72, c(1, 1), c(1, 2)),
                point(Crss.lambert72, c(5, 6))
        );
        String rec = mapper.writeValueAsString(geom);
        assertEquals(geometryCollectionWithCrs, rec);
    }

    @Test
    public void testSerializeWithCrs3D() throws JacksonException {
        AbstractGeometryCollection<?, ?> geom = geometrycollection(Crss.lambert72Z,
                linestring(c(1, 1,1), c(1, 2,3)),
                point(c(5, 6,7))
        );
        String rec = mapper.writeValueAsString(geom);
        assertEquals(geometryCollectionWithCrs3D, rec);
    }

}
