package org.geolatte.geom.cga;

import org.junit.Test;

import static org.geolatte.geom.cga.Location.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link PointLocator}.
 */
public class PointLocatorTest {

    @Test
    public void points() {
        PointLocator locator = locator("MULTIPOINT ((1 1), (2 2))");
        assertEquals(INTERIOR, locator.locate(1, 1));
        assertEquals(INTERIOR, locator.locate(2, 2));
        assertEquals(EXTERIOR, locator.locate(1, 2));
        // -0.0 and 0.0 are the same coordinate
        assertEquals(INTERIOR, locator("POINT (0 0)").locate(-0.0, -0.0));
    }

    @Test
    public void lines() {
        PointLocator locator = locator("LINESTRING (0 0, 2 0, 2 2)");
        assertEquals(BOUNDARY, locator.locate(0, 0));
        assertEquals(BOUNDARY, locator.locate(2, 2));
        assertEquals(INTERIOR, locator.locate(2, 0));
        assertEquals(INTERIOR, locator.locate(1, 0));
        assertEquals(EXTERIOR, locator.locate(1, 1));
        // a closed line has no boundary
        assertEquals(INTERIOR, locator("LINESTRING (0 0, 2 0, 2 2, 0 0)").locate(0, 0));
        // Mod-2 rule: an endpoint of two lines is in the interior, of three lines on the boundary
        assertEquals(INTERIOR, locator("MULTILINESTRING ((0 0, 1 1), (1 1, 2 0))").locate(1, 1));
        assertEquals(BOUNDARY, locator("MULTILINESTRING ((0 0, 1 1), (1 1, 2 0), (1 1, 1 2))").locate(1, 1));
        // an endpoint of one line, in the interior of another line, is on the boundary
        assertEquals(BOUNDARY, locator("MULTILINESTRING ((0 0, 2 0), (1 0, 1 1))").locate(1, 0));
    }

    @Test
    public void polygons() {
        PointLocator locator = locator("POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 3 1, 3 3, 1 3, 1 1))");
        assertEquals(INTERIOR, locator.locate(0.5, 0.5));
        assertEquals(BOUNDARY, locator.locate(0, 0));
        assertEquals(BOUNDARY, locator.locate(2, 0));
        assertEquals(BOUNDARY, locator.locate(1, 1));
        assertEquals(BOUNDARY, locator.locate(2, 3));
        assertEquals(EXTERIOR, locator.locate(2, 2));
        assertEquals(EXTERIOR, locator.locate(5, 2));
        // a clockwise shell
        assertEquals(INTERIOR, locator("POLYGON ((0 0, 0 4, 4 4, 4 0, 0 0))").locate(1, 1));
    }

    @Test
    public void unionOfPolygons() {
        // adjacent squares: the shared edge is in the interior of the union, except its endpoints
        PointLocator two = locator("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((1 0, 2 0, 2 1, 1 1, 1 0)))");
        assertEquals(INTERIOR, two.locate(1, 0.5));
        assertEquals(BOUNDARY, two.locate(1, 0));
        assertEquals(BOUNDARY, two.locate(1, 1));
        // four squares around (1, 1): it is in the interior; with three squares on the boundary
        String squares = "((0 0, 1 0, 1 1, 0 1, 0 0)), ((1 0, 2 0, 2 1, 1 1, 1 0)), ((1 1, 2 1, 2 2, 1 2, 1 1))";
        assertEquals(INTERIOR, locator("MULTIPOLYGON (" + squares + ", ((0 1, 1 1, 1 2, 0 2, 0 1)))").locate(1, 1));
        assertEquals(BOUNDARY, locator("MULTIPOLYGON (" + squares + ")").locate(1, 1));
        // squares touching at a corner only
        assertEquals(BOUNDARY, locator("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((1 1, 2 1, 2 2, 1 2, 1 1)))").locate(1, 1));
        // a hole filled by another polygon
        PointLocator filled = locator("GEOMETRYCOLLECTION (POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 3 1, 3 3, 1 3, 1 1)), "
                + "POLYGON ((1 1, 3 1, 3 3, 1 3, 1 1)))");
        assertEquals(INTERIOR, filled.locate(2, 2));
        assertEquals(INTERIOR, filled.locate(1, 1));
        assertEquals(INTERIOR, filled.locate(2, 1));
        assertEquals(BOUNDARY, filled.locate(0, 1));
        // overlapping polygons
        PointLocator overlapping = locator("GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), POLYGON ((1 1, 3 1, 3 3, 1 3, 1 1)))");
        assertEquals(INTERIOR, overlapping.locate(1.5, 1));
        assertEquals(INTERIOR, overlapping.locate(2, 2));
        assertEquals(BOUNDARY, overlapping.locate(3, 2));
    }

    @Test
    public void mixedCollections() {
        PointLocator locator = locator("GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), "
                + "LINESTRING (1 1, 4 1), POINT (1 2), POINT (5 5), POINT (3 1))");
        assertEquals(INTERIOR, locator.locate(1, 1));     // a line endpoint inside the polygon
        assertEquals(BOUNDARY, locator.locate(2, 1));     // the line crosses the boundary of the polygon
        assertEquals(INTERIOR, locator.locate(3, 1));     // a point on the line
        assertEquals(BOUNDARY, locator.locate(4, 1));
        assertEquals(BOUNDARY, locator.locate(1, 2));     // a point on the boundary of the polygon
        assertEquals(INTERIOR, locator.locate(5, 5));
        assertEquals(EXTERIOR, locator.locate(5, 4));
    }

    @Test
    public void nearlyOnTheBoundary() {
        // (1/3, 1/3)-ish: the edge (0 0)-(3 1) contains (1.5, 0.5), but not its neighbours
        PointLocator locator = locator("POLYGON ((0 0, 3 1, 0 1, 0 0))");
        assertEquals(BOUNDARY, locator.locate(1.5, 0.5));
        assertEquals(INTERIOR, locator.locate(1.5, Math.nextUp(0.5)));
        assertEquals(EXTERIOR, locator.locate(1.5, Math.nextDown(0.5)));
        PointLocator far = locator("POLYGON ((150000 200000, 150003 200001, 150000 200001, 150000 200000))");
        assertEquals(BOUNDARY, far.locate(150001.5, 200000.5));
        assertEquals(INTERIOR, far.locate(150001.5, Math.nextUp(200000.5)));
        assertEquals(EXTERIOR, far.locate(Math.nextUp(150001.5), 200000.5));
    }

    private static PointLocator locator(String wkt) {
        return PointLocator.of(RelateTest.geometry(wkt));
    }
}
