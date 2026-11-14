package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class PointOnSurfaceTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    private static Geometry<C2D> g(String wkt) {
        return Wkt.fromWkt(wkt, crs);
    }

    private static Location locate(Geometry<?> geometry) {
        double[] p = PointOnSurface.of2D(geometry);
        return PointLocator.of(geometry).locate(p[0], p[1]);
    }

    @Test
    public void polygons() {
        assertArrayEquals(new double[]{5, 5}, PointOnSurface.of2D(g("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))")), 0);
        // the middle lies in the hole
        Geometry<C2D> withHole = g("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 9 1, 9 9, 1 9, 1 1))");
        assertEquals(Location.INTERIOR, locate(withHole));
        // the middle lies outside
        Geometry<C2D> u = g("POLYGON ((0 0, 10 0, 10 10, 9 10, 9 1, 1 1, 1 10, 0 10, 0 0))");
        assertEquals(Location.INTERIOR, locate(u));
        // the widest interval: in the big square
        double[] p = PointOnSurface.of2D(g("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((5 0, 15 0, 15 10, 5 10, 5 0)))"));
        assertArrayEquals(new double[]{10, 5}, p, 0);
        // a triangle: the scan line passes between the vertices
        assertEquals(Location.INTERIOR, locate(g("POLYGON ((0 0, 10 0, 5 10, 0 0))")));
    }

    @Test
    public void collectionsAndLowerDimensions() {
        Geometry<C2D> collection = g("GEOMETRYCOLLECTION (POINT (100 100), LINESTRING (50 50, 60 60), "
                + "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)))");
        assertArrayEquals(new double[]{1, 1}, PointOnSurface.of2D(collection), 0);
        // an interior vertex of a line, nearest to the centroid
        assertArrayEquals(new double[]{5, 0}, PointOnSurface.of2D(g("LINESTRING (0 0, 5 0, 9 0, 10 0)")), 0);
        // no interior vertex: the endpoint nearest to the centroid
        // (the centroid is at x = 150.5 / 11)
        assertArrayEquals(new double[]{10, 0}, PointOnSurface.of2D(g("MULTILINESTRING ((0 0, 1 0), (10 0, 20 0))")), 0);
        // the point nearest to the centroid
        assertArrayEquals(new double[]{1, 1}, PointOnSurface.of2D(g("MULTIPOINT ((0 0), (1 1), (3 3))")), 0);
        // a polygon of zero area counts as its ring: a vertex
        double[] p = PointOnSurface.of2D(g("POLYGON ((0 0, 10 0, 5 0, 0 0))"));
        assertEquals(0, p[1], 0);
        assertNull(PointOnSurface.of2D(g("GEOMETRYCOLLECTION EMPTY")));
        assertTrue(PointOnSurface.of(g("POLYGON EMPTY")).isEmpty());
    }

    @Test
    public void resultHasTheCrsOfTheGeometry() {
        CoordinateReferenceSystem<C3DM> crs3dm = CoordinateReferenceSystems.PROJECTED_3DM_METER;
        @SuppressWarnings("unchecked")
        Geometry<C3DM> polygon = (Geometry<C3DM>) Wkt.fromWkt("POLYGON ZM ((0 0 1 2, 4 0 1 2, 4 4 1 2, 0 4 1 2, 0 0 1 2))",
                crs3dm);
        Point<C3DM> point = PointOnSurface.of(polygon);
        assertEquals(crs3dm, point.getCoordinateReferenceSystem());
        assertEquals(2.0, point.getPosition().getX(), 0);
        assertTrue(Double.isNaN(point.getPosition().getZ()));
        assertTrue(Double.isNaN(point.getPosition().getM()));
    }

    @Test
    public void adjacentVertexOrdinatesAtTheMiddle() {
        // vertex y coordinates 0.5 and the next double straddle the middle: no scan line fits between them
        double up = Math.nextUp(0.5);
        PositionSequence<C2D> ring = PositionSequenceBuilders.variableSized(C2D.class)
                .add(0, 0).add(2, 0).add(2, 0.5).add(1.5, up).add(2, 1).add(0, 1).add(0, 0).toPositionSequence();
        Polygon<C2D> polygon = new Polygon<>(ring, crs);
        double[] p = PointOnSurface.of2D(polygon);
        assertTrue(p[1] != 0.5 && p[1] != up);
        assertEquals(Location.INTERIOR, locate(polygon));
    }

    @Test
    public void slivers() {
        // nearly collinear triangles at projected coordinates: an interior point is found and verified exactly
        Random random = new Random(73);
        int interior = 0;
        for (int k = 0; k < 2000; k++) {
            double x0 = 152000 + random.nextDouble(), y0 = 212000 + random.nextDouble();
            double dx = 100 * random.nextDouble(), dy = 100 * random.nextDouble();
            double t = random.nextDouble();
            double ex = Math.ulp(x0) * (random.nextInt(9) - 4), ey = Math.ulp(y0) * (random.nextInt(9) - 4);
            PositionSequence<C2D> ring = PositionSequenceBuilders.variableSized(C2D.class)
                    .add(x0, y0).add(x0 + dx, y0 + dy).add(x0 + t * dx + ex, y0 + t * dy + ey).add(x0, y0)
                    .toPositionSequence();
            Polygon<C2D> polygon = new Polygon<>(ring, crs);
            Location location = locate(polygon);
            // a vertex at worst; in the interior whenever the area is not zero and a double lies inside
            assertNotEquals(polygon.toString(), Location.EXTERIOR, location);
            if (location == Location.INTERIOR) interior++;
            if (Area.of(polygon) == 0) assertEquals(Location.BOUNDARY, location);
        }
        assertTrue("" + interior, interior > 1000);
    }

    @Test
    public void randomPolygonsWithHoles() {
        Random random = new Random(79);
        for (int k = 0; k < 2000; k++) {
            double cx = 152000 + 1000 * random.nextDouble(), cy = 212000 + 1000 * random.nextDouble();
            double r = 0.1 + 100 * random.nextDouble();
            Polygon<C2D> polygon = starWithHoles(random, cx, cy, r);
            Location location = locate(polygon);
            assertEquals(polygon.toString(), Location.INTERIOR, location);
            assertTrue(Validity.isValid(polygon));
            // JTS agrees that the point is inside
            double[] p = PointOnSurface.of2D(polygon);
            assertTrue(JTS.to(polygon).intersects(JTS.to(new Point<>(new C2D(p[0], p[1]), crs))));
        }
    }

    // a star-shaped ring around (cx, cy), with up to three small star-shaped holes around the centre
    private static Polygon<C2D> starWithHoles(Random random, double cx, double cy, double r) {
        int n = 8 + random.nextInt(20);
        LinearRing<C2D> shell = new LinearRing<>(star(random, cx, cy, 0.5 * r, r, n), crs);
        int holes = random.nextInt(4);
        @SuppressWarnings("unchecked")
        LinearRing<C2D>[] rings = new LinearRing[holes + 1];
        rings[0] = shell;
        for (int h = 0; h < holes; h++) {
            double angle = 2 * Math.PI * (h + 0.5) / 3;
            double hx = cx + 0.25 * r * Math.cos(angle), hy = cy + 0.25 * r * Math.sin(angle);
            rings[h + 1] = new LinearRing<>(star(random, hx, hy, 0.02 * r, 0.1 * r, 3 + random.nextInt(6)), crs);
        }
        // the centre itself in a hole sometimes
        if (holes == 0 && random.nextBoolean()) {
            rings = java.util.Arrays.copyOf(rings, 2);
            rings[1] = new LinearRing<>(star(random, cx, cy, 0.1 * r, 0.3 * r, 3 + random.nextInt(6)), crs);
        }
        return new Polygon<>(rings);
    }

    private static PositionSequence<C2D> star(Random random, double cx, double cy, double rMin, double rMax, int n) {
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.variableSized(C2D.class);
        double x0 = 0, y0 = 0;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * (i + 0.8 * random.nextDouble()) / n;
            double radius = rMin + (rMax - rMin) * random.nextDouble();
            double x = Math.round((cx + radius * Math.cos(angle)) * 1e6) / 1e6;
            double y = Math.round((cy + radius * Math.sin(angle)) * 1e6) / 1e6;
            if (i == 0) {
                x0 = x;
                y0 = y;
            }
            builder.add(x, y);
        }
        builder.add(x0, y0);
        return builder.toPositionSequence();
    }
}
