package org.geolatte.geom.jts;

import org.junit.Test;
import org.locationtech.jts.geom.*;

import static org.geolatte.geom.CrsMock.*;
import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.jts.JTSUtils.equalsExact3D;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JTSUtilsTest {

    @Test
    public void testNullsAreEqual() {
        assertTrue(equalsExact3D(null, null));
    }

    @Test
    public void testNullAndGeomAreNotEqual() {
        Point pnt1 = (Point) JTS.to(point(WGS84, g(3, 50)));
        assertFalse(equalsExact3D(null, pnt1));
        assertFalse(equalsExact3D(pnt1, null));
    }

    @Test
    public void identicalObjectsAreEqual() {
        Point pnt1 = (Point) JTS.to(point(WGS84, g(3, 50)));
        assertTrue(equalsExact3D(pnt1, pnt1));
    }

    @Test
    public void testDifferentTypesAreNotEqual() {
        Point pnt1 = (Point) JTS.to(point(WGS84, g(3, 50)));
        LineString ln1 = (LineString) JTS.to(linestring(WGS84, g(5, 50), g(5.1, 50.2)));
        assertFalse(equalsExact3D(ln1, pnt1));
    }

    @Test
    public void testEmptyGeometriesAreEqual() {
        Geometry pn1 = JTS.to(point(WGS84));
        Geometry pn2 = JTS.to(point(WGS84));
        assertTrue(equalsExact3D(pn1, pn2));

        pn1.setSRID(43426);
        assertFalse(equalsExact3D(pn1, pn2));

        Geometry ln2 = JTS.to(linestring(WGS84));
        assertFalse(equalsExact3D(pn2, ln2));
    }

    @Test
    public void testEqualsPointsAreEqual() {
        Point pnt1 = (Point) JTS.to(point(WGS84, g(3, 50)));
        Point pnt2 = (Point) JTS.to(point(WGS84, g(3, 50)));
        assertTrue(equalsExact3D(pnt1, pnt2));

        pnt1 = (Point) JTS.to(point(WGS84_Z, g(3, 50, 4)));
        pnt2 = (Point) JTS.to(point(WGS84_Z, g(3, 50, 4)));
        assertTrue(equalsExact3D(pnt1, pnt2));

        pnt1 = (Point) JTS.to(point(WGS84_M, gM(3, 50, 4)));
        pnt2 = (Point) JTS.to(point(WGS84_M, gM(3, 50, 4)));
        assertTrue(equalsExact3D(pnt1, pnt2));

        pnt1 = (Point) JTS.to(point(WGS84_ZM, g(3, 50, 4, 3.43)));
        pnt2 = (Point) JTS.to(point(WGS84_ZM, g(3, 50, 4, 3.43)));
        assertTrue(equalsExact3D(pnt1, pnt2));
    }

    @Test
    public void testPointsWithDifferingSRIDsAreUnEqual() {
        Point pnt1 = (Point) JTS.to(point(WGS84_Z, g(3, 50, 3.2)));
        Point pnt2 = (Point) JTS.to(point(WGS84_Z, g(3, 50, 4.3)));
        pnt1.setSRID(4326);
        pnt2.setSRID(4327);
        assertFalse(equalsExact3D(pnt1, pnt2));
    }

    @Test
    public void testUnEqualsPointsAreUnEqual() {
        Point pnt1 = (Point) JTS.to(point(WGS84_Z, g(3, 50, 3.2)));
        Point pnt2 = (Point) JTS.to(point(WGS84_Z, g(3, 50, 4.3)));
        assertFalse(equalsExact3D(pnt1, pnt2));
    }

    @Test
    public void testEqualLineStringsAreEqual() {
        LineString ln1 = (LineString) JTS.to(linestring(WGS84_Z, g(3, 40, 100), g(3.2, 40.1, 120)));
        LineString ln2 = (LineString) JTS.to(linestring(WGS84_Z, g(3, 40, 100), g(3.2, 40.1, 120)));
        assertTrue(equalsExact3D(ln1, ln2));
    }

    @Test
    public void testUnEqualLineStringsAreUnEqual() {
        LineString ln1 = (LineString) JTS.to(linestring(WGS84_Z, g(3, 40, 100), g(3.2, 40.1, 120)));
        LineString ln2 = (LineString) JTS.to(linestring(WGS84_Z, g(3, 40, 100), g(3.3, 40.1, 120)));
        assertFalse(equalsExact3D(ln1, ln2));
    }

    @Test
    public void testEqualPolygonsAreEqual() {
        Polygon pg1 = (Polygon) JTS.to(polygon(WGS84,
                ring(g(3, 40), g(3.2, 40.1), g(3, 40)),
                ring(g(3.01, 40.01), g(3.02, 40.09), g(3.01, 40.01))
        ));
        Polygon pg2 = (Polygon) JTS.to(polygon(WGS84,
                ring(g(3, 40), g(3.2, 40.1), g(3, 40)),
                ring(g(3.01, 40.01), g(3.02, 40.09), g(3.01, 40.01))
        ));
        assertTrue(equalsExact3D(pg1, pg2));
    }

    @Test
    public void testUnEqualPolygonsAreUnEqual() {
        Polygon pg1 = (Polygon) JTS.to(polygon(WGS84,
                ring(g(3, 40), g(3.2, 40.1), g(3, 40)))
        );
        Polygon pg2 = (Polygon) JTS.to(polygon(WGS84,
                ring(g(3, 40), g(3.3, 40.2), g(3, 40)))
        );
        assertFalse(equalsExact3D(pg1, pg2));

        pg1 = (Polygon) JTS.to(polygon(WGS84,
                ring(g(3, 40), g(3.2, 40.1), g(3, 40)),
                ring(g(3.01, 40.01), g(3.02, 40.09), g(3.01, 40.01))
        ));
        pg2 = (Polygon) JTS.to(polygon(WGS84,
                ring(g(3, 40), g(3.2, 40.1), g(3, 40)),
                ring(g(3.01, 40.01), g(3.03, 40.09), g(3.01, 40.01))
        ));
        assertFalse(equalsExact3D(pg1, pg2));
    }

    @Test
    public void testMultiPoints() {
        MultiPoint mp1 = (MultiPoint) JTS.to(multipoint(WGS84, g(3, 50), g(3.1, 50.2)));
        MultiPoint mp2 = (MultiPoint) JTS.to(multipoint(WGS84, g(3, 50), g(3.1, 50.2)));
        MultiPoint mp3 = (MultiPoint) JTS.to(multipoint(WGS84, g(3, 50), g(3.1, 51.2)));
        assertTrue(equalsExact3D(mp1, mp2));
        assertFalse(equalsExact3D(mp1, mp3));
    }

    @Test
    public void testMultiLineString() {
        MultiLineString ml1 = (MultiLineString) JTS.to(multilinestring(WGS84,
                linestring(g(3, 50), g(3.1, 50.2)),
                linestring(g(4, 52), g(4.1, 52.2))
        ));
        MultiLineString ml2 = (MultiLineString) JTS.to(multilinestring(WGS84,
                linestring(g(3, 50), g(3.1, 50.2)),
                linestring(g(4, 52), g(4.1, 52.2))
        ));
        MultiLineString ml3 = (MultiLineString) JTS.to(multilinestring(WGS84,
                linestring(g(3, 50), g(3.2, 50.2)),
                linestring(g(4, 51), g(4.1, 52.2))
        ));
        assertTrue(equalsExact3D(ml1, ml2));
        assertFalse(equalsExact3D(ml1, ml3));
    }

    @Test
    public void testGeometryCollection() {
        Geometry ml1 = JTS.to(geometrycollection(WGS84,
                linestring(g(3, 50), g(3.1, 50.2)),
                point(g(4, 52)),
                polygon(
                        ring(g(3, 40), g(3.2, 40.1), g(3, 40)),
                        ring(g(3.01, 40.01), g(3.02, 40.09), g(3.01, 40.01))
                )
        ));
        Geometry ml2 = JTS.to(geometrycollection(WGS84,
                linestring(g(3, 50), g(3.1, 50.2)),
                point(g(4, 52)),
                polygon(
                        ring(g(3, 40), g(3.2, 40.1), g(3, 40)),
                        ring(g(3.01, 40.01), g(3.02, 40.09), g(3.01, 40.01))
                )
        ));
        Geometry ml3 = JTS.to(geometrycollection(WGS84,
                linestring(g(3, 50), g(3.1, 50.2)),
                point(g(4, 52)),
                polygon(
                        ring(g(3, 40), g(3.1, 40.1), g(3, 40)),
                        ring(g(3.01, 40.01), g(3.02, 40.09), g(3.01, 40.01))
                )
        ));
        assertTrue(equalsExact3D(ml1, ml2));
        assertFalse(equalsExact3D(ml1, ml3));
    }

}
