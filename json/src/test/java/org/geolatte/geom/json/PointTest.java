package org.geolatte.geom.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.LinearUnit;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class PointTest {

    private String emptyPointText = " {\"type\":\"Point\",\"coordinates\":[]}";
    private String pointText = "{\"type\":\"Point\",\"coordinates\":[1,2]}";
    private String pointText3D = "{\"type\":\"Point\",\"coordinates\":[1,2,3]}";
    private String pointTextWithCrs = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1,2]}";
    private String pointTextWithCrs3D = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1,2,3]}";
    private String pointTextWithCrs34D = "{\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"coordinates\":[1,2,3,4]}";
    private String pointTextWithCrsAndBbox = " {\"type\":\"Point\",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:31370\"}},\"bbox\":[1.000000000000000,2.000000000000000,1.000000000000000,2.000000000000000],\"coordinates\":[1,2]}";

    private static CoordinateReferenceSystem<G3D> wgs3D = addVerticalSystem(
            WGS84, G3D.class, LinearUnit.METER
    );

    private static CoordinateReferenceSystem<C2D> lambert72 = CrsRegistry.getProjectedCoordinateReferenceSystemForEPSG(31370);
    private static CoordinateReferenceSystem<C3D> lambert72Z = addVerticalSystem(lambert72, C3D.class, LinearUnit.METER);
    private static CoordinateReferenceSystem<C3DM> lambert72ZM = addLinearSystem(lambert72, C3DM.class, LinearUnit.METER);


    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new GeolatteGeomModule());
    }

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
        module.setFeature(Feature.FORCE_DEFAULT_CRS_DIMENSION, true);
        mapper.registerModule(module);
        Point<?> pnt = mapper.readValue(pointText, Point.class);
        Point<G3D> expected = point(wgs3D, g(1, 2, 0));
        assertEquals(expected, pnt);
    }

    @Test
    public void testDeserializePointTextWithLimitToCrsReduce() throws IOException {
        mapper = new ObjectMapper();
        GeolatteGeomModule module = new GeolatteGeomModule(WGS84);
        module.setFeature(Feature.FORCE_DEFAULT_CRS_DIMENSION, true);
        mapper.registerModule(module);
        Point<?> pnt = mapper.readValue(pointText3D, Point.class);
        Point<G2D> expected = point(WGS84, g(1, 2));
        assertEquals(expected, pnt);
    }


    @Test
    public void testDeserializeEmpptyPoint() throws IOException {
        Point<?> pnt = mapper.readValue(emptyPointText, Point.class);
        Point<?> expected = new Point(WGS84);
        assertEquals(expected, pnt);
    }



}


