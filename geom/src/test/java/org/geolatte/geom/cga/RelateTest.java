package org.geolatte.geom.cga;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.relate.RelateOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests {@link Relate}.
 */
public class RelateTest {

    private final Random random = new Random(71L);
    private final GeometryFactory factory = new GeometryFactory();

    // ---------------------------------------------------------------------------------------------------------
    // basic cases
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void pointCases() {
        assertMatrix("0FFFFFFF2", "POINT (1 1)", "POINT (1 1)");
        assertMatrix("FF0FFF0F2", "POINT (1 1)", "POINT (2 1)");
        assertMatrix("0FFFFF102", "POINT (1 1)", "LINESTRING (0 0, 2 2)");
        assertMatrix("F0FFFF102", "POINT (0 0)", "LINESTRING (0 0, 2 2)");
        assertMatrix("FF0FFF102", "POINT (0 1)", "LINESTRING (0 0, 2 2)");
        assertMatrix("0FFFFF212", "POINT (1 1)", "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
        assertMatrix("F0FFFF212", "POINT (2 1)", "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
        assertMatrix("FF0FFF212", "POINT (3 1)", "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
        // a point in a hole
        assertMatrix("FF0FFF212", "POINT (2 2)",
                "POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 3 1, 3 3, 1 3, 1 1))");
        // a closed line has no boundary
        assertMatrix("0FFFFF1F2", "POINT (0 0)", "LINESTRING (0 0, 1 0, 1 1, 0 0)");
    }

    @Test
    public void lineCases() {
        assertMatrix("0F1FF0102", "LINESTRING (0 0, 2 2)", "LINESTRING (0 2, 2 0)");
        assertMatrix("FF1F00102", "LINESTRING (0 0, 1 1)", "LINESTRING (1 1, 2 0)");
        assertMatrix("1FFF0FFF2", "LINESTRING (0 0, 2 2)", "LINESTRING (2 2, 1 1, 0 0)");
        assertMatrix("1010F0102", "LINESTRING (0 0, 2 0)", "LINESTRING (1 0, 3 0)");
        assertMatrix("1FF0FF102", "LINESTRING (1 0, 2 0)", "LINESTRING (0 0, 3 0)");
        assertMatrix("F1FF0F212", "LINESTRING (0 0, 0 2)", "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
    }

    @Test
    public void areaCases() {
        String square = "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))";
        assertMatrix("2FFF1FFF2", square, "POLYGON ((0 0, 0 2, 2 2, 2 0, 0 0))");
        assertMatrix("212101212", square, "POLYGON ((1 1, 3 1, 3 3, 1 3, 1 1))");
        assertMatrix("FF2F11212", square, "POLYGON ((2 0, 4 0, 4 2, 2 2, 2 0))");
        assertMatrix("FF2F01212", square, "POLYGON ((2 2, 4 2, 4 4, 2 4, 2 2))");
        assertMatrix("212F11FF2", square, "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))");
        assertMatrix("2FF1FF212", "POLYGON ((0.5 0.5, 1 0.5, 1 1, 0.5 0.5))", square);
    }

    @Test
    public void nonSimpleLines() {
        // cases where JTS 1.19 RelateOp is wrong; the matrices were verified by hand
        // a line of A retraces (1 4, 5 5) of B: the interiors share a line segment
        assertMatrix("101F00102", "MULTILINESTRING ((1 4, 2 5, 0 1, 2 3), (3 5, 5 5, 1 4, 3 4))",
                "MULTILINESTRING ((6 2, 2 2, 1 1), (1 4, 5 5))");
        // (5 3)-(4 4) lies on (3 5)-(6 2)
        assertMatrix("101FF0102", "LINESTRING (3 5, 6 2, 6 1, 4 6, 1 3)",
                "MULTILINESTRING ((1 2, 3 1, 5 6, 5 3, 4 4), (2 0, 4 5))");
        // no segment of A is collinear with an edge of B
        assertMatrix("101000212", "MULTILINESTRING ((3 0, 1 1, 4 3), (1 4, 5 1, 0 3))",
                "POLYGON ((-1 4, -3 2, -1 0, 0 -2, 2 0, 3 4, -1 4))");
        // (2 1) is an endpoint of one line only (Mod-2: boundary), although it lies on the other line
        assertMatrix("1F100F212", "MULTILINESTRING ((2 3, 2 0), (2 1, 1 0, 1 1))",
                "MULTIPOLYGON (((4 -1, 6 -1, 6 1, 5 2, 2 0, 4 -1)), ((0 1, 3 1, 3 4, 1 5, -2 4, 0 1)))");
        // two lines sharing an endpoint: it is an endpoint of 2 lines, so it is in the interior
        assertMatrix("0F1FF0102", "MULTILINESTRING ((0 0, 1 1), (1 1, 2 0))", "LINESTRING (1 0, 1 2)");
    }

    // ---------------------------------------------------------------------------------------------------------
    // union semantics
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void unionSemanticsCases() {
        String adjacent = "GEOMETRYCOLLECTION (POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0)), POLYGON ((1 0, 2 0, 2 1, 1 1, 1 0)))";
        // a line on the shared edge lies in the interior of the union
        assertMatrix("1FF0FF212", "LINESTRING (1 0.2, 1 0.8)", adjacent);
        assertTrue(Relate.within(geometry("LINESTRING (1 0.2, 1 0.8)"), geometry(adjacent)));
        // the union is the rectangle
        assertMatrix("2FFF1FFF2", adjacent, "POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))");
        assertTrue(Relate.equalsTopologically(geometry(adjacent), geometry("POLYGON ((0 0, 2 0, 2 1, 0 1, 0 0))")));
        // overlapping polygons
        assertMatrix("2FFF1FFF2", "GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), POLYGON ((1 1, 3 1, 3 3, 1 3, 1 1)))",
                "POLYGON ((0 0, 2 0, 2 1, 3 1, 3 3, 1 3, 1 2, 0 2, 0 0))");
        // a hole filled by another polygon
        assertMatrix("2FFF1FFF2", "GEOMETRYCOLLECTION (POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 3 1, 3 3, 1 3, 1 1)), "
                + "POLYGON ((1 1, 3 1, 3 3, 1 3, 1 1)))", "POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0))");
        // a line partly inside a polygon: its part inside, and its endpoint inside, are absorbed
        String mixed = "GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), LINESTRING (1 1, 4 1))";
        assertMatrix("FF2F01212", mixed, "POLYGON ((4 0, 5 0, 5 2, 4 2, 4 0))");      // touches at the line end
        assertMatrix("0F2FF1FF2", mixed, "POINT (1 1)");
        assertMatrix("0F2FF1FF2", mixed, "POINT (3 1)");
        assertMatrix("FF20F1FF2", mixed, "POINT (2 1)");
        // points on lines or in areas are absorbed
        assertMatrix("2FFF1FFF2", "GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), POINT (1 1), POINT (2 1))",
                "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
        assertMatrix("1FFF0FFF2", "GEOMETRYCOLLECTION (LINESTRING (0 0, 2 0), POINT (1 0))", "LINESTRING (0 0, 2 0)");
        // an isolated point keeps dimension 0
        assertMatrix("2F0F1FFF2", "GEOMETRYCOLLECTION (POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)), POINT (5 5))",
                "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
    }

    @Test
    public void emptyGeometries() {
        assertMatrix("FFFFFF212", "POINT EMPTY", "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))");
        assertMatrix("FFFFFF102", "GEOMETRYCOLLECTION EMPTY", "LINESTRING (0 0, 1 1)");
        assertMatrix("FFFFFFFF2", "POLYGON EMPTY", "LINESTRING EMPTY");
        assertTrue(Relate.equalsTopologically(geometry("POINT EMPTY"), geometry("POLYGON EMPTY")));
        assertFalse(Relate.intersects(geometry("POINT EMPTY"), geometry("POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))")));
        assertTrue(Relate.disjoint(geometry("POINT EMPTY"), geometry("POINT EMPTY")));
    }

    /**
     * Splitting the areas of a geometry into adjacent pieces, or adding elements that are covered by it, does not
     * change the point set, so the matrix stays the same.
     */
    @Test
    public void splittingAndAbsorbedElementsDoNotChangeTheMatrix() {
        for (int trial = 0; trial < 1000; trial++) {
            int x0 = random.nextInt(5), y0 = random.nextInt(5);
            int x1 = x0 + 1 + random.nextInt(3), x2 = x1 + 1 + random.nextInt(3), y1 = y0 + 1 + random.nextInt(4);
            org.locationtech.jts.geom.Geometry whole = rectangle(x0, y0, x2, y1);
            org.locationtech.jts.geom.Geometry split = factory.createGeometryCollection(new org.locationtech.jts.geom.Geometry[]{
                    rectangle(x0, y0, x1, y1), rectangle(x1, y0, x2, y1),
                    factory.createLineString(new Coordinate[]{new Coordinate(x1, y0), new Coordinate(x1, y1)}),
                    factory.createPoint(new Coordinate(x1, (y0 + y1) / 2.0)),
                    factory.createPoint(new Coordinate(x0, y0))});
            org.locationtech.jts.geom.Geometry other = randomCollection();
            IntersectionMatrix expected = Relate.relate(JTS.from(whole), JTS.from(other));
            assertEquals(split + " / " + other, expected, Relate.relate(JTS.from(split), JTS.from(other)));
            assertEquals(expected.transpose(), Relate.relate(JTS.from(other), JTS.from(split)));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // exactness
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The matrix does not change under transformations that are exact in floating point: translations by integers
     * (of geometries with small integer coordinates), scaling by powers of 2, swapping X and Y, reversing.
     */
    @Test
    public void invariantUnderExactTransformations() {
        for (int trial = 0; trial < 1000; trial++) {
            org.locationtech.jts.geom.Geometry a = randomCollection(), b = randomCollection();
            IntersectionMatrix expected = Relate.relate(JTS.from(a), JTS.from(b));
            double offset = 150000 + random.nextInt(100000), scale = Math.scalb(1.0, random.nextInt(200) - 100);
            assertEquals(expected, relate(transform(a, offset, scale, false), transform(b, offset, scale, false)));
            assertEquals(expected, relate(transform(a, 0, scale, true), transform(b, 0, scale, true)));
            assertEquals(expected, relate(a.reverse(), b.reverse()));
        }
    }

    /**
     * Nearly coincident and exactly coincident crossing points that are not representable: the crossings of lines
     * through a common rational point, perturbed by an ulp, compared with the exact oracle.
     */
    @Test
    public void concurrentCrossings() {
        for (int trial = 0; trial < 300; trial++) {
            long d = 2 + random.nextInt(20);
            long n1 = random.nextInt(100), n2 = random.nextInt(100);
            List<org.locationtech.jts.geom.Geometry> members = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Coordinate[] c = lineThrough(n1, n2, d);
                if (random.nextInt(3) == 0) c[1].x = Math.nextUp(c[1].x);
                members.add(factory.createLineString(c));
            }
            org.locationtech.jts.geom.Geometry a = factory.createGeometryCollection(new org.locationtech.jts.geom.Geometry[]{
                    members.get(0), members.get(1)});
            Coordinate[] t = lineThrough(n1, n2, d), u = lineThrough(n1, n2, d);
            org.locationtech.jts.geom.Geometry b = random.nextBoolean()
                    ? factory.createMultiLineString(new org.locationtech.jts.geom.LineString[]{
                    (org.locationtech.jts.geom.LineString) members.get(2), (org.locationtech.jts.geom.LineString) members.get(3)})
                    : triangle(t[0], t[1], u[1]);
            if (!b.isValid()) continue;
            assertEquals(a + " / " + b, RelateOracle.relate(a, b), Relate.relate(JTS.from(a), JTS.from(b)).toString());
        }
    }

    @Test
    public void nearlyOnSegment() {
        // (1.5, 0.5) lies on (0 0)-(3 1); its neighbours do not
        assertMatrix("0FFFFF102", "POINT (1.5 0.5)", "LINESTRING (0 0, 3 1)");
        assertMatrix("FF0FFF102", "POINT (1.5 0.5000000000000001)", "LINESTRING (0 0, 3 1)");
        // a segment that ends 1 ulp beside another one does not touch it
        assertMatrix("FF1FF0102", "LINESTRING (1.5 0.5000000000000001, 1.5 2)", "LINESTRING (0 0, 3 1)");
        assertMatrix("FF10F0102", "LINESTRING (1.5 0.5, 1.5 2)", "LINESTRING (0 0, 3 1)");
    }

    /**
     * For valid input (valid polygons), the depths computed from both ends of each edge are consistent.
     */
    @Test
    public void labellingIsConsistent() {
        for (int trial = 0; trial < 1000; trial++) {
            org.locationtech.jts.geom.Geometry a = randomCollection(), b = randomCollection();
            RelateComputer computer = new RelateComputer(GeometryElements.of(JTS.from(a)), GeometryElements.of(JTS.from(b)));
            computer.compute();
            assertEquals(a + " / " + b, 0, computer.inconsistencies);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // differential test against JTS on small integer grids
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void sameAsJtsOnGrids() throws ParseException {
        int failures = 0;
        StringBuilder report = new StringBuilder();
        for (int trial = 0; trial < 3000; trial++) {
            org.locationtech.jts.geom.Geometry a = randomGeometry(), b = randomGeometry();
            String expected = RelateOp.relate(a, b).toString();
            String actual = Relate.relate(JTS.from(a), JTS.from(b)).toString();
            if (trial < 500) {
                // validates the oracle that is used for collections
                assertEquals(a + " / " + b, expected, RelateOracle.relate(a, b));
            }
            if (!expected.equals(actual)) {
                if (failures++ < 10) {
                    report.append(a).append('\n').append(b).append("\n jts ").append(expected)
                            .append(" exact ").append(actual).append('\n');
                }
            }
        }
        assertEquals(report.toString(), 0, failures);
    }

    // ---------------------------------------------------------------------------------------------------------
    // comparison with a brute force oracle, for collections with union semantics and non-simple geometries
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void sameAsOracleForCollections() {
        int failures = 0;
        StringBuilder report = new StringBuilder();
        for (int trial = 0; trial < 1500; trial++) {
            org.locationtech.jts.geom.Geometry a = randomCollection(), b = randomCollection();
            String expected = RelateOracle.relate(a, b);
            String actual = Relate.relate(JTS.from(a), JTS.from(b)).toString();
            if (!expected.equals(actual) && failures++ < 10) {
                report.append(a).append('\n').append(b).append("\n oracle ").append(expected)
                        .append(" exact ").append(actual).append('\n');
            }
        }
        assertEquals(report.toString(), 0, failures);
    }

    @Test
    public void sameAsOracleForNonSimpleGeometries() {
        int failures = 0;
        StringBuilder report = new StringBuilder();
        for (int trial = 0; trial < 1500; trial++) {
            org.locationtech.jts.geom.Geometry a = randomMember(), b = randomMember();
            String expected = RelateOracle.relate(a, b);
            String actual = Relate.relate(JTS.from(a), JTS.from(b)).toString();
            if (!expected.equals(actual) && failures++ < 10) {
                report.append(a).append('\n').append(b).append("\n oracle ").append(expected)
                        .append(" exact ").append(actual).append('\n');
            }
        }
        assertEquals(report.toString(), 0, failures);
    }

    @Test
    public void pointLocatorSameAsOracle() {
        for (int trial = 0; trial < 500; trial++) {
            org.locationtech.jts.geom.Geometry g = randomCollection();
            PointLocator locator = PointLocator.of(JTS.from(g));
            RelateOracle.Elements oracle = new RelateOracle.Elements(g);
            for (int i = 0; i < 40; i++) {
                double x = random.nextInt(24) * 0.5 - 2, y = random.nextInt(24) * 0.5 - 2;
                Location expected = Location.values()[oracle.locate(new RelateOracle.P(RelateOracle.Q.of(x), RelateOracle.Q.of(y)))];
                assertEquals(g + " at " + x + " " + y, expected, locator.locate(x, y));
            }
        }
    }

    // a collection of 1 to 3 members, which may overlap or touch
    private org.locationtech.jts.geom.Geometry randomCollection() {
        int n = 1 + random.nextInt(3);
        org.locationtech.jts.geom.Geometry[] members = new org.locationtech.jts.geom.Geometry[n];
        for (int i = 0; i < n; i++) members[i] = randomMember();
        return factory.createGeometryCollection(members);
    }

    // points, (non-simple) lines, rectangles (which are often adjacent), polygons
    private org.locationtech.jts.geom.Geometry randomMember() {
        int size = 6;
        switch (random.nextInt(6)) {
            case 0:
                return factory.createMultiPointFromCoords(coordinates(1 + random.nextInt(4), size));
            case 1:
            case 2: {
                int n = 1 + random.nextInt(2);
                org.locationtech.jts.geom.LineString[] lines = new org.locationtech.jts.geom.LineString[n];
                for (int i = 0; i < n; i++) lines[i] = factory.createLineString(walk(2 + random.nextInt(4), size));
                return n == 1 ? lines[0] : factory.createMultiLineString(lines);
            }
            case 3: {
                int x0 = random.nextInt(size), y0 = random.nextInt(size);
                int x1 = x0 + 1 + random.nextInt(3), y1 = y0 + 1 + random.nextInt(3);
                return factory.createPolygon(new Coordinate[]{new Coordinate(x0, y0), new Coordinate(x1, y0),
                        new Coordinate(x1, y1), new Coordinate(x0, y1), new Coordinate(x0, y0)});
            }
            default:
                return randomPolygon(size);
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    private org.locationtech.jts.geom.Geometry randomGeometry() {
        int size = 6;
        switch (random.nextInt(6)) {
            case 0:
                return factory.createMultiPointFromCoords(coordinates(1 + random.nextInt(4), size));
            case 1:
            case 2: {
                List<org.locationtech.jts.geom.LineString> lines = new ArrayList<>();
                int n = 1 + random.nextInt(2);
                // JTS RelateOp is not reliable for lines that intersect themselves
                while (lines.size() < n) {
                    org.locationtech.jts.geom.LineString line = factory.createLineString(walk(2 + random.nextInt(4), size));
                    if (line.isSimple()) lines.add(line);
                }
                org.locationtech.jts.geom.Geometry g = n == 1 ? lines.get(0)
                        : factory.createMultiLineString(lines.toArray(new org.locationtech.jts.geom.LineString[0]));
                if (g.isSimple()) return g;
                return lines.get(0);
            }
            default: {
                while (true) {
                    int n = 1 + random.nextInt(2);
                    org.locationtech.jts.geom.Polygon[] polygons = new org.locationtech.jts.geom.Polygon[n];
                    for (int i = 0; i < n; i++) polygons[i] = randomPolygon(size);
                    org.locationtech.jts.geom.Geometry g = n == 1 ? polygons[0] : factory.createMultiPolygon(polygons);
                    if (g.isValid()) return g;
                }
            }
        }
    }

    private org.locationtech.jts.geom.Polygon rectangle(double x0, double y0, double x1, double y1) {
        return factory.createPolygon(new Coordinate[]{new Coordinate(x0, y0), new Coordinate(x1, y0),
                new Coordinate(x1, y1), new Coordinate(x0, y1), new Coordinate(x0, y0)});
    }

    private org.locationtech.jts.geom.Geometry triangle(Coordinate a, Coordinate b, Coordinate c) {
        return factory.createPolygon(new Coordinate[]{a, b, c, new Coordinate(a)});
    }

    // a segment through (n1 / d, n2 / d), with integer endpoints
    private Coordinate[] lineThrough(long n1, long n2, long d) {
        long x0 = random.nextInt(21) - 10, y0 = random.nextInt(21) - 10;
        long k = 1 + random.nextInt(2);
        return new Coordinate[]{new Coordinate(x0, y0), new Coordinate(x0 + k * (n1 - d * x0), y0 + k * (n2 - d * y0))};
    }

    private static IntersectionMatrix relate(org.locationtech.jts.geom.Geometry a, org.locationtech.jts.geom.Geometry b) {
        return Relate.relate(JTS.from(a), JTS.from(b));
    }

    // translates and scales (exactly), and optionally swaps X and Y
    private static org.locationtech.jts.geom.Geometry transform(org.locationtech.jts.geom.Geometry g, double offset,
                                                                double scale, boolean swap) {
        org.locationtech.jts.geom.Geometry copy = g.copy();
        copy.apply(new org.locationtech.jts.geom.CoordinateFilter() {
            @Override
            public void filter(Coordinate c) {
                double x = (c.x + offset) * scale, y = (c.y + offset) * scale;
                c.x = swap ? y : x;
                c.y = swap ? x : y;
            }
        });
        copy.geometryChanged();
        return copy;
    }

    private org.locationtech.jts.geom.Polygon randomPolygon(int size) {
        while (true) {
            int n = 3 + random.nextInt(5);
            double cx = random.nextInt(size + 1), cy = random.nextInt(size + 1);
            Coordinate[] shell = star(cx, cy, n, 1 + random.nextInt(3));
            org.locationtech.jts.geom.Polygon p;
            if (random.nextInt(3) == 0) {
                Coordinate[] hole = star(cx, cy, 3 + random.nextInt(3), 1);
                p = factory.createPolygon(factory.createLinearRing(shell), new org.locationtech.jts.geom.LinearRing[]{factory.createLinearRing(hole)});
            } else {
                p = factory.createPolygon(shell);
            }
            if (p.isValid()) return p;
        }
    }

    private Coordinate[] star(double cx, double cy, int n, int radius) {
        Coordinate[] c = new Coordinate[n + 1];
        double offset = random.nextDouble() * 2 * Math.PI;
        for (int i = 0; i < n; i++) {
            double angle = offset + 2 * Math.PI * i / n;
            double r = radius * (0.5 + random.nextDouble());
            c[i] = new Coordinate(Math.round(cx + r * Math.cos(angle)), Math.round(cy + r * Math.sin(angle)));
        }
        c[n] = c[0];
        return c;
    }

    private Coordinate[] coordinates(int n, int size) {
        Coordinate[] c = new Coordinate[n];
        for (int i = 0; i < n; i++) c[i] = new Coordinate(random.nextInt(size + 1), random.nextInt(size + 1));
        return c;
    }

    private Coordinate[] walk(int n, int size) {
        Coordinate[] c = coordinates(n, size);
        for (int i = 1; i < n; i++) {
            if (c[i].equals2D(c[i - 1])) c[i] = new Coordinate(c[i].x + 1, c[i].y);
        }
        return c;
    }

    private void assertMatrix(String expected, String a, String b) {
        Geometry<?> ga = geometry(a), gb = geometry(b);
        assertEquals(a + " / " + b, expected, Relate.relate(ga, gb).toString());
        assertEquals(b + " / " + a, transpose(expected), Relate.relate(gb, ga).toString());
    }

    private static String transpose(String m) {
        char[] t = new char[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) t[3 * j + i] = m.charAt(3 * i + j);
        }
        return new String(t);
    }

    static Geometry<?> geometry(String wkt) {
        try {
            return JTS.from(new WKTReader().read(wkt));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
