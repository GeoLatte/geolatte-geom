package org.geolatte.geom.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.*;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;
import static org.geolatte.geom.json.Crss.*;
import static org.geolatte.geom.json.GeoJsonStrings.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class PointDeserializationTest extends GeoJsonTest{




    @Test
    public void testDeserializePointText() throws IOException {
        TypeReference<Point<G2D>> typeRef = new TypeReference<Point<G2D>>() {
        };
        Point<G2D> pnt = mapper.readValue(pointText, typeRef);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointText3D() throws IOException {

        Point<?> pnt = mapper.readValue(pointText3D, Point.class);
        Point<G3D> expected = point(wgs3D, g(1, 2, 3));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithSpecificCRS() throws IOException {
        TypeReference<Point<G2D>> typeRef = new TypeReference<Point<G2D>>() {
        };
        mapper = new ObjectMapper();
        mapper.registerModule(new GeolatteGeomModule(WGS84));
        Point<G2D> pnt = mapper.readValue(pointText, typeRef);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithWildCard() throws IOException {
        Point<?> pnt = mapper.readValue(pointText, Point.class);
        Point<?> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithCrs() throws IOException {
        TypeReference<Point<C2D>> typeRef = new TypeReference<Point<C2D>>() {
        };
        Point<C2D> pnt = mapper.readValue(pointTextWithCrs, typeRef);
        Point<C2D> expected = point(lambert72, c(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithCrs3D() throws IOException {
        TypeReference<Point<C3D>> typeRef = new TypeReference<Point<C3D>>() {
        };
        Point<C3D> pnt = mapper.readValue(pointTextWithCrs3D, typeRef);
        Point<C3D> expected = point(lambert72Z, c(1, 2, 3));
        assertEquals(expected, pnt);
    }


    @Test
    public void testDeserializePointTextWithNoLimitToCrs() throws IOException {
        GeolatteGeomModule module = new GeolatteGeomModule(wgs3D);
        mapper.registerModule(module);
        Point<?> pnt = mapper.readValue(pointText, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithLimitToCrs() throws IOException {
        mapper = new ObjectMapper();
        GeolatteGeomModule module = new GeolatteGeomModule(wgs3D);
        module.set(Setting.FORCE_DEFAULT_CRS_DIMENSION, true);
        mapper.registerModule(module);
        Point<?> pnt = mapper.readValue(pointText, Point.class);
        Point<G3D> expected = point(wgs3D, g(1, 2, 0));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithLimitToCrsReduce() throws IOException {
        mapper = new ObjectMapper();
        GeolatteGeomModule module = new GeolatteGeomModule(WGS84);
        module.set(Setting.FORCE_DEFAULT_CRS_DIMENSION, true);
        mapper.registerModule(module);
        Point<?> pnt = mapper.readValue(pointText3D, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }


    @Test
    public void testDeserializeEmpptyPoint() throws IOException {
        Point<?> pnt = mapper.readValue(emptyPointText, Point.class);
        Point<?> expected = new Point<>(WGS84);
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithCrsURN() throws IOException {
        TypeReference<Point<C2D>> typeRef = new TypeReference<Point<C2D>>() {
        };
        Point<C2D> pnt = mapper.readValue(pointTextWithUrnCrs, typeRef);
        Point<C2D> expected = point(lambert72, c(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void testForce3DMTo2DMPoint() throws IOException {
        ObjectMapper mapper = createMapper(wgs2DM, Setting.FORCE_DEFAULT_CRS_DIMENSION, true);
        Point<?> pnt = mapper.readValue(pointTextWithCrs3D, Point.class);
        Point<?> expected = point(wgs2DM, gM(1, 2, 3.0));
        assertEquals(expected, pnt);
    }


}


