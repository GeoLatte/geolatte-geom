package org.geolatte.geom.json;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 12/09/17.
 */
public class GeometryColllectionDeserializationTest extends GeoJsonTest {

    @Test
    public void testEmtpy() throws IOException {
        GeometryCollection<?, Geometry<?>> rec = mapper.readValue(emptyGeometryCollection, GeometryCollection.class);
        GeometryCollection<?, ?> exp = new GeometryCollection<>(WGS84);
        assertEquals(exp, rec);
    }


    @Test
    public void testDeserializeSimple() throws IOException {
        GeometryCollection<?, ?> rec = mapper.readValue(geometryCollection, GeometryCollection.class);
        GeometryCollection<?, ?> expected = geometrycollection(
                linestring(WGS84, g(1, 1), g(1, 2)),
                point(WGS84, g(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeWithCRS() throws IOException {
        GeometryCollection<?, ?> rec = mapper.readValue(geometryCollectionWithCrs, GeometryCollection.class);
        GeometryCollection<?, ?> expected = geometrycollection(
                linestring(Crss.lambert72, c(1, 1), c(1, 2)),
                point(Crss.lambert72, c(5, 6))
        );
        assertEquals(expected, rec);
    }

    @Test
    public void testDeserializeWithCRS3D() throws IOException {
        GeometryCollection<?, ?> rec = mapper.readValue(geometryCollectionWithCrs3D, GeometryCollection.class);
        GeometryCollection<?, ?> expected = geometrycollection(Crss.lambert72Z,
                linestring(c(1, 1,1), c(1, 2,3)),
                point(c(5, 6,7))
        );
        assertEquals(expected, rec);
    }

}
