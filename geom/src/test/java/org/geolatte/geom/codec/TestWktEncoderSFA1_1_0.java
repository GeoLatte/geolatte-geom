package org.geolatte.geom.codec;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.LinearUnit;
import org.junit.Test;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addLinearSystem;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.addVerticalSystem;
import static org.junit.Assert.assertEquals;


public class TestWktEncoderSFA1_1_0 {

    public static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    WktEncoder encoder() {
        return Wkt.newEncoder(Wkt.Dialect.SFA_1_1_0);
    }

    @Test
    public void test_point() {
        Point<C2D> pnt = point(crs, c(1.52,2.43));
        assertEquals("POINT(1.52 2.43)", encoder().encode(pnt));
    }

    @Test
    public void test_empty_point() {
        Point<C2D> pnt = new Point<>(crs);
        assertEquals("POINT EMPTY", encoder().encode(pnt));
    }

    @Test
    public void test_linestring() {
        LineString<C2D> geom = linestring(crs, c(1.52,2.43), c(4.23, 5.32));
        assertEquals("LINESTRING(1.52 2.43,4.23 5.32)", encoder().encode(geom));
    }

    @Test
    public void test_empty_linestring() {
        LineString<C2D> geom = new LineString<>(crs);
        assertEquals("LINESTRING EMPTY", encoder().encode(geom));
    }

    @Test
    public void test_polygon() {
        Polygon<C2D> geom = polygon(crs, ring(c(1.52,2.43), c(4.23, 5.32), c( 4.23, 6.32), c(1.52, 3.43), c(1.52, 2.43)));
        assertEquals("POLYGON((1.52 2.43,4.23 5.32,4.23 6.32,1.52 3.43,1.52 2.43))", encoder().encode(geom));
    }

    @Test
    public void test_empty_polygon() {
        Polygon<C2D> geom = new Polygon<>(crs);
        assertEquals("POLYGON EMPTY", encoder().encode(geom));
    }

    @Test
    public void test_multipoint() {
        MultiPoint<C2D> geom = multipoint(crs, c(1.52,2.43), c(3.42, 5.34));
        assertEquals("MULTIPOINT(1.52 2.43,3.42 5.34)", encoder().encode(geom));
    }

    @Test
    public void test_empty_multipoint() {
        MultiPoint<C2D> geom = new MultiPoint<>(crs);
        assertEquals("MULTIPOINT EMPTY", encoder().encode(geom));
    }

    @Test
    public void test_multilinestring() {
        MultiLineString<C2D> geom = multilinestring(crs, linestring(c(1.52,2.43), c(3.42, 5.34)),
                linestring(c(4.5, 3.2), c(6.7,9.8)));
        assertEquals("MULTILINESTRING((1.52 2.43,3.42 5.34),(4.5 3.2,6.7 9.8))", encoder().encode(geom));
    }

    @Test
    public void test_empty_multilinestring() {
        MultiLineString<C2D> geom = new MultiLineString<>(crs);
        assertEquals("MULTILINESTRING EMPTY", encoder().encode(geom));
    }

    @Test
    public void test_multipolygon() {
        MultiPolygon<C2D> geom = multipolygon(crs,
                polygon(ring(c(1.52,2.43), c(4.23, 5.32), c( 4.23, 6.32), c(1.52, 3.43), c(1.52, 2.43))),
                polygon(ring(c(3.52,4.43), c(6.23, 7.32), c( 6.23, 8.32), c(3.52, 5.43), c(3.52, 4.43))));
        assertEquals("MULTIPOLYGON(((1.52 2.43,4.23 5.32,4.23 6.32,1.52 3.43,1.52 2.43)),((3.52 4.43,6.23 7.32,6.23 8.32,3.52 5.43,3.52 4.43)))", encoder().encode(geom));
    }

    @Test
    public void test_empty_multipolygon() {
        MultiPolygon<C2D> geom = new MultiPolygon<>(crs);
        assertEquals("MULTIPOLYGON EMPTY", encoder().encode(geom));
    }

    @Test
    public void test_geometry_collection() {
        GeometryCollection<C2D> geom= geometrycollection(crs, point(c(1.52,2.43)), linestring(c(1.52,2.43), c(4.23, 5.32)));
        assertEquals("GEOMETRYCOLLECTION(POINT(1.52 2.43),LINESTRING(1.52 2.43,4.23 5.32))", encoder().encode(geom));
    }

    @Test
    public void test_empty_geometry_collection() {
        GeometryCollection<C2D> geom= geometrycollection(crs);
        assertEquals("GEOMETRYCOLLECTION EMPTY", encoder().encode(geom));
    }

    @Test
    public void test_serializes_only_first_two_coordinates(){
        CoordinateReferenceSystem<C3D> crsZ= addVerticalSystem(crs, C3D.class, LinearUnit.METER);
        CoordinateReferenceSystem<C3DM> crsZM= addLinearSystem(crsZ, C3DM.class, LinearUnit.METER);
        Point<C3DM> pnt = point(crsZM, c(1.1, 2.4, 3.0, 4.0));
        assertEquals("POINT(1.1 2.4)", encoder().encode(pnt));
    }


}
