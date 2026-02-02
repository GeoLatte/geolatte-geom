package org.geolatte.geom.json;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.Unit;
import org.junit.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(new GeolatteGeomModule(WGS84));
        mapper = builder.build();

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

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(module);
        mapper = builder.build();

        Point<?> pnt = mapper.readValue(pointText, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithLimitToCrs() throws IOException {
        GeolatteGeomModule module = new GeolatteGeomModule(wgs3D);
        module.set(Setting.IGNORE_CRS, true);

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(module);
        mapper = builder.build();

        Point<?> pnt = mapper.readValue(pointText, Point.class);
        Point<G3D> expected = point(wgs3D, g(1, 2, 0));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithLimitToCrsReduce() throws IOException {
        GeolatteGeomModule module = new GeolatteGeomModule(WGS84);
        module.set(Setting.IGNORE_CRS, true);

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(module);
        mapper = builder.build();

        Point<?> pnt = mapper.readValue(pointText3D, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }


    @Test
    public void testDeserializePointTextWithLimitToCrs2DM() throws IOException {
        CoordinateReferenceSystem<G2DM> crs = WGS84.addLinearSystem(Unit.METER, G2DM.class);
        GeolatteGeomModule module = new GeolatteGeomModule(crs);
        module.set(Setting.IGNORE_CRS, true);

        JsonMapper.Builder builder = JsonMapper.builder();
        builder.addModule(module);
        mapper = builder.build();

        Point<?> pnt = mapper.readValue(pointText2DM, Point.class);
        Point<G2DM> expected = point(crs, gM(1, 2, 4));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializeEmptyPoint() throws IOException {
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
        ObjectMapper mapper = createMapper(wgs2DM, Setting.IGNORE_CRS, true);
        Point<?> pnt = mapper.readValue(pointTextWithCrs3D, Point.class);
        Point<?> expected = point(wgs2DM, gM(1, 2, 3.0));
        assertEquals(expected, pnt);
    }

}


