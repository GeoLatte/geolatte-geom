package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests {@link Area} and {@link Centroid} against an independent exact oracle (BigDecimal arithmetic and a midpoint
 * test), and against JTS.
 */
public class AreaCentroidTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;
    private static final CoordinateReferenceSystem<C3DM> crs3dm = CoordinateReferenceSystems.PROJECTED_3DM_METER;

    @SuppressWarnings("unchecked")
    private static Geometry<C2D> geometry(String wkt) {
        return (Geometry<C2D>) Wkt.fromWkt(wkt, crs);
    }

    // ---------------------------------------------------------------------------------------------------------
    // hand-made cases
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void simplePolygons() {
        Geometry<C2D> square = geometry("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))");
        assertEquals(100.0, Area.of(square), 0);
        assertCentroid(5, 5, square);

        Geometry<C2D> clockwise = geometry("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
        assertEquals(100.0, Area.of(clockwise), 0);
        assertCentroid(5, 5, clockwise);

        Geometry<C2D> withHole = geometry("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 1 5, 5 5, 5 1, 1 1))");
        assertEquals(84.0, Area.of(withHole), 0);
        // (100 * (5, 5) - 16 * (3, 3)) / 84
        assertCentroidExact(new BigDecimal(452), new BigDecimal(84), new BigDecimal(452), new BigDecimal(84),
                withHole);

        Geometry<C2D> triangle = geometry("POLYGON ((0 0, 3 0, 0 3, 0 0))");
        assertEquals(4.5, Area.of(triangle), 0);
        assertCentroid(1, 1, triangle);
    }

    @Test
    public void multiPolygonsAndCollections() {
        Geometry<C2D> multi = geometry("MULTIPOLYGON (((0 0, 2 0, 2 2, 0 2, 0 0)), ((10 0, 12 0, 12 2, 10 2, 10 0)))");
        assertEquals(8.0, Area.of(multi), 0);
        assertCentroid(6, 1, multi);

        Geometry<C2D> collection = geometry("GEOMETRYCOLLECTION (POINT (100 100), LINESTRING (0 0, 50 50), "
                + "POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0)))");
        assertEquals(4.0, Area.of(collection), 0);
        assertCentroid(1, 1, collection);   // only the area counts
    }

    @Test
    public void lowerDimensions() {
        assertEquals(0.0, Area.of(geometry("LINESTRING (0 0, 10 10)")), 0);
        assertEquals(0.0, Area.of(geometry("POINT (1 1)")), 0);
        assertEquals(0.0, Area.of(geometry("POLYGON EMPTY")), 0);

        assertCentroid(5, 0, geometry("LINESTRING (0 0, 10 0)"));
        // length-weighted: (10 * (5, 0) + 10 * (10, 5)) / 20
        assertCentroid(7.5, 2.5, geometry("LINESTRING (0 0, 10 0, 10 10)"));
        assertCentroid(2, 3, geometry("MULTIPOINT ((1 1), (3 5))"));
        // a zero-area polygon: the centroid of its boundary
        assertCentroid(1, 1, geometry("POLYGON ((0 0, 2 2, 2 2, 0 0))"));
        // a zero-length line counts as a point
        assertCentroid(2, 2, geometry("GEOMETRYCOLLECTION (LINESTRING (1 1, 1 1), POINT (3 3))"));
        // the exact mean of three points
        assertCentroidExact(new BigDecimal(1), new BigDecimal(3), new BigDecimal(2), new BigDecimal(3),
                geometry("MULTIPOINT ((0 0), (1 0), (0 2))"));

        assertTrue(Centroid.of(geometry("POLYGON EMPTY")).isEmpty());
        assertTrue(Centroid.of(geometry("GEOMETRYCOLLECTION EMPTY")).isEmpty());
        assertNull(Centroid.of2D(geometry("MULTIPOLYGON EMPTY")));
    }

    @Test
    public void resultHasTheCrsOfTheGeometry() {
        @SuppressWarnings("unchecked")
        Geometry<C3DM> polygon = (Geometry<C3DM>) Wkt.fromWkt(
                "POLYGON ZM ((0 0 1 2, 4 0 1 2, 4 4 1 2, 0 4 1 2, 0 0 1 2))", crs3dm);
        Point<C3DM> centroid = Centroid.of(polygon);
        assertEquals(crs3dm, centroid.getCoordinateReferenceSystem());
        assertEquals(2.0, centroid.getPosition().getX(), 0);
        assertEquals(2.0, centroid.getPosition().getY(), 0);
        assertTrue(Double.isNaN(centroid.getPosition().getZ()));
        assertTrue(Double.isNaN(centroid.getPosition().getM()));
    }

    @Test
    public void rejectsNonFiniteCoordinates() {
        Polygon<C2D> polygon = polygon(new double[]{0, 1, Double.NaN, 0}, new double[]{0, 0, 1, 0});
        assertThrows(IllegalArgumentException.class, () -> Area.of(polygon));
        assertThrows(IllegalArgumentException.class, () -> Centroid.of(polygon));
    }

    @Test
    public void tiesAreRoundedToEven() {
        // the unit square, with a triangle of area h / 2 added at the bottom edge
        double u = 0x1p-52;
        Polygon<C2D> tieDown = polygon(new double[]{0, 0.5, 1, 1, 0, 0}, new double[]{0, -u, 0, 1, 1, 0});
        assertEquals(1.0, Area.of(tieDown), 0);                       // 1 + 2^-53: a tie, rounded to 1
        Polygon<C2D> tieUp = polygon(new double[]{0, 0.5, 1, 1, 0, 0}, new double[]{0, -3 * u, 0, 1, 1, 0});
        assertEquals(1 + 2 * u, Area.of(tieUp), 0);                  // 1 + 3 2^-53: a tie, rounded to 1 + 2^-51
        Polygon<C2D> above = polygon(new double[]{0, 0.5, 1, 1, 0, 0}, new double[]{0, -1.5 * u, 0, 1, 1, 0});
        assertEquals(1 + u, Area.of(above), 0);                      // 1 + 1.5 2^-53
        checkAgainstOracle(tieDown);
        checkAgainstOracle(tieUp);
        checkAgainstOracle(above);
    }

    @Test
    public void largeCoordinatesSmallPolygons() {
        // a 1 mm square at Lambert-like coordinates, and a sliver: the naive shoelace formula fails here
        double x = 152345.678, y = 212345.789, d = 0.001;
        Polygon<C2D> square = polygon(new double[]{x, x + d, x + d, x, x}, new double[]{y, y, y + d, y + d, y});
        checkAgainstOracle(square);
        Polygon<C2D> sliver = polygon(new double[]{x, x + 1000, x + 2000, x},
                new double[]{y, y + 1e-9, y, y});
        checkAgainstOracle(sliver);
        assertTrue(Area.of(sliver) > 0);
    }

    // ---------------------------------------------------------------------------------------------------------
    // randomized tests against the oracle
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void randomPolygonsMatchTheOracle() {
        Random random = new Random(7);
        double[] offsets = {0, 1, 1e3, 1.5e5, 2e6, 1e9, -3.7e5};
        double[] sizes = {1e-9, 1e-3, 1, 100, 1e5};
        for (int k = 0; k < 3000; k++) {
            double ox = offsets[random.nextInt(offsets.length)] * (1 + random.nextDouble());
            double oy = offsets[random.nextInt(offsets.length)] * (1 + random.nextDouble());
            double size = sizes[random.nextInt(sizes.length)];
            Geometry<C2D> g = randomPolygonal(random, ox, oy, size);
            checkAgainstOracle(g);
        }
    }

    @Test
    public void nearlyDegeneratePolygonsMatchTheOracle() {
        Random random = new Random(11);
        for (int k = 0; k < 2000; k++) {
            // nearly collinear positions: tiny areas with massive cancellation
            double x0 = 1e5 * random.nextDouble(), y0 = 1e5 * random.nextDouble();
            double dx = random.nextDouble(), dy = random.nextDouble();
            int n = 3 + random.nextInt(6);
            double[] xs = new double[n + 1], ys = new double[n + 1];
            for (int i = 0; i < n; i++) {
                double t = random.nextDouble() * 1000;
                xs[i] = x0 + t * dx;
                ys[i] = y0 + t * dy;
                if (random.nextBoolean()) ys[i] = Math.nextUp(ys[i]);
            }
            xs[n] = xs[0];
            ys[n] = ys[0];
            checkAgainstOracle(polygon(xs, ys));
        }
    }

    @Test
    public void extremeMagnitudesMatchTheOracle() {
        Random random = new Random(13);
        double[] scales = {0x1p-1000, 0x1p-600, 0x1p-250, 0x1p250, 0x1p450, 0x1p500};
        for (int k = 0; k < 300; k++) {
            double scale = scales[random.nextInt(scales.length)];
            Geometry<C2D> g = randomPolygonal(random, scale * random.nextDouble(), scale * random.nextDouble(), scale);
            checkAgainstOracle(g);
        }
        // mixed magnitudes in one polygon
        Polygon<C2D> mixed = polygon(new double[]{0x1p-700, 1, 1, 0x1p-700, 0x1p-700},
                new double[]{0, 0, 0x1p300, 0x1p300, 0});
        checkAgainstOracle(mixed);
    }

    @Test
    public void largePolygonMatchesTheOracle() {
        Random random = new Random(23);
        int n = 20000;
        double[] xs = new double[n + 1], ys = new double[n + 1];
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n, r = 5000 * (1 + 0.1 * Math.sin(7 * angle) + 0.01 * random.nextDouble());
            xs[i] = Math.round((152000 + r * Math.cos(angle)) * 1000) / 1000.0;
            ys[i] = Math.round((212000 + r * Math.sin(angle)) * 1000) / 1000.0;
        }
        xs[n] = xs[0];
        ys[n] = ys[0];
        checkAgainstOracle(polygon(xs, ys));
    }

    @Test
    public void invariance() {
        Random random = new Random(17);
        for (int k = 0; k < 500; k++) {
            List<double[][]> rings = randomRings(random, 1e5 * random.nextDouble(), 1e5 * random.nextDouble(),
                    100 * random.nextDouble());
            Polygon<C2D> p = polygon(rings);
            double area = Area.of(p);
            double[] c = Centroid.of2D(p);

            // reversed rings and other start positions
            List<double[][]> changed = new ArrayList<>();
            for (double[][] r : rings) changed.add(rotate(reverse(r), random.nextInt(r[0].length - 1)));
            assertEquals(area, Area.of(polygon(changed)), 0);
            assertArrayEquals(c, Centroid.of2D(polygon(changed)), 0);

            // permuted holes
            List<double[][]> permuted = new ArrayList<>(rings.subList(1, rings.size()));
            Collections.shuffle(permuted, random);
            permuted.add(0, rings.get(0));
            assertEquals(area, Area.of(polygon(permuted)), 0);
            assertArrayEquals(c, Centroid.of2D(polygon(permuted)), 0);

            // scaling by a power of 2 is exact; swapping X and Y swaps the centroid
            List<double[][]> scaled = new ArrayList<>(), swapped = new ArrayList<>();
            for (double[][] r : rings) {
                double[] xs = r[0].clone(), ys = r[1].clone();
                for (int i = 0; i < xs.length; i++) {
                    xs[i] *= 0x1p-20;
                    ys[i] *= 0x1p-20;
                }
                scaled.add(new double[][]{xs, ys});
                swapped.add(new double[][]{r[1], r[0]});
            }
            assertEquals(area * 0x1p-40, Area.of(polygon(scaled)), 0);
            assertArrayEquals(new double[]{c[0] * 0x1p-20, c[1] * 0x1p-20}, Centroid.of2D(polygon(scaled)), 0);
            assertEquals(area, Area.of(polygon(swapped)), 0);
            assertArrayEquals(new double[]{c[1], c[0]}, Centroid.of2D(polygon(swapped)), 0);
        }
    }

    @Test
    public void agreesWithJts() {
        Random random = new Random(19);
        for (int k = 0; k < 1000; k++) {
            Geometry<C2D> g = randomPolygonal(random, 1e5 * random.nextDouble(), 1e5 * random.nextDouble(),
                    1 + 1000 * random.nextDouble());
            org.locationtech.jts.geom.Geometry jts = JTS.to(g);
            double area = Area.of(g);
            assertEquals(jts.getArea(), area, 1e-9 * area);
            double[] c = Centroid.of2D(g);
            assertEquals(jts.getCentroid().getX(), c[0], 1e-6);
            assertEquals(jts.getCentroid().getY(), c[1], 1e-6);
        }
        Geometry<C2D> line = geometry("MULTILINESTRING ((0 0, 3 4, 3 10), (5 5, 6 7))");
        double[] c = Centroid.of2D(line);
        assertEquals(JTS.to(line).getCentroid().getX(), c[0], 1e-12);
        assertEquals(JTS.to(line).getCentroid().getY(), c[1], 1e-12);
    }

    @Test
    public void fastArea() {
        Random random = new Random(29);
        for (int k = 0; k < 2000; k++) {
            Geometry<C2D> g = randomPolygonal(random, 1e5 * random.nextDouble(), 1e5 * random.nextDouble(),
                    1e-3 + 1000 * random.nextDouble());
            assertEquals(Area.of(g), Area.fast(g), 1e-9 * Area.of(g));
        }
        assertEquals(0.0, Area.fast(geometry("POLYGON EMPTY")), 0);
        assertEquals(0.0, Area.fast(geometry("LINESTRING (0 0, 1 1)")), 0);
        assertEquals(84.0, Area.fast(geometry("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 1 5, 5 5, 5 1, 1 1))")), 0);
        assertEquals(5.0, Area.fast(geometry("GEOMETRYCOLLECTION (POINT (0 0), POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0)), "
                + "MULTIPOLYGON (((5 5, 7 5, 7 7, 5 7, 5 5))))")), 0);
        // inexact for slivers: nearly collinear positions at large coordinates
        double worst = 0;
        for (int k = 0; k < 100; k++) {
            double x0 = 1e5 * random.nextDouble(), y0 = 1e5 * random.nextDouble();
            double dx = random.nextDouble(), dy = random.nextDouble();
            double[] xs = new double[5], ys = new double[5];
            for (int i = 0; i < 4; i++) {
                double t = random.nextDouble() * 1000;
                xs[i] = x0 + t * dx;
                ys[i] = Math.nextUp(y0 + t * dy);
            }
            xs[4] = xs[0];
            ys[4] = ys[0];
            Polygon<C2D> sliver = polygon(xs, ys);
            double exact = Area.of(sliver);
            if (exact == 0) continue;
            worst = Math.max(worst, Math.abs(Area.fast(sliver) - exact) / exact);
        }
        assertTrue("worst relative error " + worst, worst > 0.01);
    }

    // ---------------------------------------------------------------------------------------------------------
    // the oracle
    // ---------------------------------------------------------------------------------------------------------

    private static void checkAgainstOracle(Geometry<C2D> g) {
        List<double[][]> rings = new ArrayList<>();
        List<Boolean> holes = new ArrayList<>();
        collectRings(g, rings, holes);
        // the doubled area S, and the centroid numerators (sum of (x_i + x_j) c_ij), with the ring signs
        BigDecimal s = BigDecimal.ZERO, nx = BigDecimal.ZERO, ny = BigDecimal.ZERO;
        for (int r = 0; r < rings.size(); r++) {
            double[] xs = rings.get(r)[0], ys = rings.get(r)[1];
            int n = xs.length;
            BigDecimal rs = BigDecimal.ZERO, rx = BigDecimal.ZERO, ry = BigDecimal.ZERO;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                BigDecimal xi = new BigDecimal(xs[i]), yi = new BigDecimal(ys[i]);
                BigDecimal xj = new BigDecimal(xs[j]), yj = new BigDecimal(ys[j]);
                BigDecimal c = xi.multiply(yj).subtract(xj.multiply(yi));
                rs = rs.add(c);
                rx = rx.add(xi.add(xj).multiply(c));
                ry = ry.add(yi.add(yj).multiply(c));
            }
            int f = holes.get(r) ? -rs.signum() : rs.signum();
            if (f == 0) continue;
            s = s.add(rs.multiply(BigDecimal.valueOf(f)));
            nx = nx.add(rx.multiply(BigDecimal.valueOf(f)));
            ny = ny.add(ry.multiply(BigDecimal.valueOf(f)));
        }
        String message = "for " + g;
        assertEquals(message, roundExactly(s, BigDecimal.valueOf(2)), Area.of(g), 0);
        if (s.signum() != 0) {
            BigDecimal den = s.multiply(BigDecimal.valueOf(3));
            double[] c = Centroid.of2D(g);
            assertEquals(message, roundExactly(nx, den), c[0], 0);
            assertEquals(message, roundExactly(ny, den), c[1], 0);
        }
    }

    private static void collectRings(Geometry<?> g, List<double[][]> rings, List<Boolean> holes) {
        if (g.isEmpty()) return;
        if (g instanceof Polygon) {
            Polygon<?> p = (Polygon<?>) g;
            rings.add(coordinates(p.getExteriorRing()));
            holes.add(false);
            for (int i = 0; i < p.getNumInteriorRing(); i++) {
                rings.add(coordinates(p.getInteriorRingN(i)));
                holes.add(true);
            }
        } else if (g instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> c = (AbstractGeometryCollection<?, ?>) g;
            for (int i = 0; i < c.getNumGeometries(); i++) collectRings(c.getGeometryN(i), rings, holes);
        }
    }

    private static double[][] coordinates(LineString<?> line) {
        int n = line.getNumPositions();
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = line.getPositionN(i).getCoordinate(0);
            ys[i] = line.getPositionN(i).getCoordinate(1);
        }
        return new double[][]{xs, ys};
    }

    // num / den, rounded to the nearest double (ties to even), independently of the implementation under test
    static double roundExactly(BigDecimal num, BigDecimal den) {
        if (num.signum() == 0) return 0;
        double c = num.divide(den, MathContext.DECIMAL128).doubleValue();
        for (int iteration = 0; iteration < 10; iteration++) {
            double down = Math.nextDown(c), up = Math.nextUp(c);
            int lo = compare(num, den, midpoint(c, down));
            int hi = compare(num, den, midpoint(c, up));
            if (lo < 0) {
                c = down;
            } else if (hi > 0) {
                c = up;
            } else if (lo == 0) {
                return even(c) ? c : down;
            } else if (hi == 0) {
                return even(c) ? c : up;
            } else {
                return c;
            }
        }
        throw new AssertionError("no convergence");
    }

    private static BigDecimal midpoint(double a, double b) {
        return new BigDecimal(a).add(new BigDecimal(b)).divide(BigDecimal.valueOf(2));
    }

    // the sign of num / den - m
    private static int compare(BigDecimal num, BigDecimal den, BigDecimal m) {
        return num.subtract(m.multiply(den)).signum() * den.signum();
    }

    private static boolean even(double v) {
        return (Double.doubleToRawLongBits(v) & 1) == 0;
    }

    private static void assertCentroid(double x, double y, Geometry<C2D> g) {
        Point<C2D> c = Centroid.of(g);
        assertEquals(x, c.getPosition().getX(), 0);
        assertEquals(y, c.getPosition().getY(), 0);
    }

    private static void assertCentroidExact(BigDecimal xn, BigDecimal xd, BigDecimal yn, BigDecimal yd,
                                            Geometry<C2D> g) {
        double[] c = Centroid.of2D(g);
        assertEquals(roundExactly(xn, xd), c[0], 0);
        assertEquals(roundExactly(yn, yd), c[1], 0);
    }

    // ---------------------------------------------------------------------------------------------------------
    // generators
    // ---------------------------------------------------------------------------------------------------------

    // a polygon or multipolygon of star-shaped rings, with star-shaped holes
    private static Geometry<C2D> randomPolygonal(Random random, double ox, double oy, double size) {
        if (random.nextInt(4) > 0) return polygon(randomRings(random, ox, oy, size));
        int count = 2 + random.nextInt(3);
        List<Polygon<C2D>> polygons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            polygons.add(polygon(randomRings(random, ox + 3 * i * size, oy, size)));
        }
        @SuppressWarnings("unchecked")
        Polygon<C2D>[] array = polygons.toArray(new Polygon[0]);
        return new MultiPolygon<>(array);
    }

    // a star-shaped shell around (ox, oy), with radius about size, and up to 2 holes
    private static List<double[][]> randomRings(Random random, double ox, double oy, double size) {
        List<double[][]> rings = new ArrayList<>();
        rings.add(star(random, ox, oy, size * 0.5, size, 3 + random.nextInt(20), random.nextBoolean()));
        int holes = random.nextInt(3);
        for (int h = 0; h < holes; h++) {
            double angle = 2 * Math.PI * h / Math.max(1, holes);
            double hx = ox + 0.2 * size * Math.cos(angle), hy = oy + 0.2 * size * Math.sin(angle);
            rings.add(star(random, hx, hy, 0.02 * size, 0.08 * size, 3 + random.nextInt(8), random.nextBoolean()));
        }
        return rings;
    }

    private static double[][] star(Random random, double cx, double cy, double rMin, double rMax, int n,
                                   boolean clockwise) {
        double[] xs = new double[n + 1], ys = new double[n + 1];
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * (i + random.nextDouble() * 0.9) / n * (clockwise ? -1 : 1);
            double r = rMin + random.nextDouble() * (rMax - rMin);
            xs[i] = cx + r * Math.cos(angle);
            ys[i] = cy + r * Math.sin(angle);
        }
        xs[n] = xs[0];
        ys[n] = ys[0];
        return new double[][]{xs, ys};
    }

    private static double[][] reverse(double[][] ring) {
        int n = ring[0].length;
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = ring[0][n - 1 - i];
            ys[i] = ring[1][n - 1 - i];
        }
        return new double[][]{xs, ys};
    }

    // the closed ring starting at position k
    private static double[][] rotate(double[][] ring, int k) {
        int n = ring[0].length - 1;
        double[] xs = new double[n + 1], ys = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            xs[i] = ring[0][(i + k) % n];
            ys[i] = ring[1][(i + k) % n];
        }
        return new double[][]{xs, ys};
    }

    private static Polygon<C2D> polygon(double[] xs, double[] ys) {
        List<double[][]> rings = new ArrayList<>();
        rings.add(new double[][]{xs, ys});
        return polygon(rings);
    }

    @SuppressWarnings("unchecked")
    private static Polygon<C2D> polygon(List<double[][]> rings) {
        LinearRing<C2D>[] linearRings = new LinearRing[rings.size()];
        for (int r = 0; r < rings.size(); r++) {
            PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.variableSized(C2D.class);
            double[] xs = rings.get(r)[0], ys = rings.get(r)[1];
            for (int i = 0; i < xs.length; i++) builder.add(xs[i], ys[i]);
            linearRings[r] = new LinearRing<>(builder.toPositionSequence(), crs);
        }
        return new Polygon<>(linearRings);
    }
}
