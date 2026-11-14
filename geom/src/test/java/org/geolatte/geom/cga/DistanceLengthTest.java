package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.MinimumDistanceTest;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.operation.distance.DistanceOp;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Tests {@link Distance} and {@link Length} against independent oracles (exact BigDecimal arithmetic, and high
 * precision square roots), and against JTS.
 */
public class DistanceLengthTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    @SuppressWarnings("unchecked")
    private static Geometry<C2D> geometry(String wkt) {
        return (Geometry<C2D>) Wkt.fromWkt(wkt, crs);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Distance: hand-made cases
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void simpleDistances() {
        assertEquals(5.0, Distance.of(geometry("POINT (0 0)"), geometry("POINT (3 4)")), 0);
        assertEquals(2.0, Distance.of(geometry("POINT (1 2)"), geometry("LINESTRING (0 0, 5 0)")), 0);
        assertEquals(Math.sqrt(2), Distance.of(geometry("POINT (6 1)"), geometry("LINESTRING (0 0, 5 0)")), 0);
        assertEquals(1.0, Distance.of(geometry("LINESTRING (0 1, 5 1)"), geometry("LINESTRING (0 0, 5 0)")), 0);
        assertEquals(Math.sqrt(2), Distance.of(geometry("POINT (1 0)"), geometry("LINESTRING (0 1, 1 2)")), 0);
        assertEquals(Math.sqrt(4.5), Distance.of(geometry("POINT (2 0)"), geometry("LINESTRING (0 1, 2 3)")), 0);
        assertEquals(1.0, Distance.of(geometry("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))"),
                geometry("POLYGON ((2 0, 3 0, 3 1, 2 1, 2 0))")), 0);
        // a perpendicular distance that is not representable: 1 / sqrt(5), correctly rounded
        assertEquals(MinimumDistanceTest.roundSqrt(BigDecimal.ONE, BigDecimal.valueOf(5)),
                Distance.of(geometry("POINT (0 1)"), geometry("LINESTRING (-1 0, 1 1)")), 0);
    }

    @Test
    public void intersectingGeometriesHaveDistanceZero() {
        Geometry<C2D> square = geometry("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 8 2, 8 8, 2 8, 2 2))");
        assertEquals(0.0, Distance.of(square, geometry("POINT (1 1)")), 0);
        assertEquals(0.0, Distance.of(square, geometry("POINT (10 5)")), 0);
        assertEquals(0.0, Distance.of(square, geometry("LINESTRING (-5 5, 20 5)")), 0);
        assertEquals(0.0, Distance.of(geometry("POLYGON ((-1 -1, 20 -1, 20 20, -1 20, -1 -1))"), square), 0);
        // in the hole: the distance to the hole
        assertEquals(1.0, Distance.of(square, geometry("POINT (5 3)")), 0);
        assertEquals(2.0, Distance.of(square, geometry("POLYGON ((4 4, 6 4, 6 6, 4 6, 4 4))")), 0);
    }

    @Test
    public void emptyGeometriesHaveNoDistance() {
        assertTrue(Double.isNaN(Distance.of(geometry("POINT EMPTY"), geometry("POINT (1 1)"))));
        assertTrue(Double.isNaN(Distance.of(geometry("POINT (1 1)"), geometry("GEOMETRYCOLLECTION EMPTY"))));
    }

    @Test
    public void nearlyTouching() {
        // a point one ulp off a segment: the distance is tiny, but not zero
        double y = Math.nextUp(0.5);
        Geometry<C2D> line = geometry("LINESTRING (0 0, 2 1)");
        Geometry<C2D> point = point(1, y);
        double d = Distance.of(point, line);
        assertTrue(d > 0 && d < 1e-16);
        checkDistanceOracle(point, line);
        checkDistanceOracle(point(1, Math.nextDown(0.5)), line);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Distance: randomized
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void randomDistancesMatchTheOracle() {
        Random random = new Random(31);
        double[] offsets = {0, 1, 1.5e5, 2e6, -3.7e5};
        double[] sizes = {1e-6, 1, 100, 1e4};
        for (int k = 0; k < 1500; k++) {
            double ox = offsets[random.nextInt(offsets.length)] * (1 + random.nextDouble());
            double oy = offsets[random.nextInt(offsets.length)] * (1 + random.nextDouble());
            double size = sizes[random.nextInt(sizes.length)];
            Geometry<C2D> g1 = randomGeometry(random, ox, oy, size);
            Geometry<C2D> g2 = randomGeometry(random, ox + size * 3 * random.nextDouble(),
                    oy + size * 3 * random.nextDouble(), size);
            checkDistanceOracle(g1, g2);
        }
    }

    @Test
    public void gridDistancesMatchTheOracle() {
        // many exact ties and degenerate positions on a small grid
        Random random = new Random(37);
        for (int k = 0; k < 1500; k++) {
            Geometry<C2D> g1 = gridGeometry(random), g2 = gridGeometry(random);
            checkDistanceOracle(g1, g2);
        }
    }

    @Test
    public void extremeMagnitudesMatchTheOracle() {
        Random random = new Random(41);
        double[] scales = {0x1p-700, 0x1p-450, 0x1p300, 0x1p450};
        for (int k = 0; k < 200; k++) {
            double scale = scales[random.nextInt(scales.length)];
            Geometry<C2D> g1 = randomGeometry(random, 0, 0, scale);
            Geometry<C2D> g2 = randomGeometry(random, 2 * scale, scale, scale);
            checkDistanceOracle(g1, g2);
        }
    }

    @Test
    public void agreesWithJts() {
        Random random = new Random(43);
        for (int k = 0; k < 1000; k++) {
            double size = 1 + 100 * random.nextDouble();
            Geometry<C2D> g1 = randomGeometry(random, 1e5, 2e5, size);
            Geometry<C2D> g2 = randomGeometry(random, 1e5 + 2 * size * random.nextDouble(), 2e5, size);
            double expected = DistanceOp.distance(JTS.to(g1), JTS.to(g2));
            assertEquals(g1 + " / " + g2, expected, Distance.of(g1, g2), 1e-9 * Math.max(1, expected));
        }
    }

    @Test
    public void largeGeometries() {
        Random random = new Random(47);
        Polygon<C2D> p1 = star(random, 152000, 212000, 5000, 20000);
        Polygon<C2D> p2 = star(random, 163000, 212000, 5000, 20000);
        LineString<C2D> line = new LineString<>(star(random, 152000, 225000, 3000, 5000).getExteriorRing()
                .getPositions(), crs);
        for (Geometry<C2D>[] pair : pairs(p1, p2, line)) {
            double expected = DistanceOp.distance(JTS.to(pair[0]), JTS.to(pair[1]));
            assertEquals(expected, Distance.of(pair[0], pair[1]), 1e-9);
            assertEquals(Distance.of(pair[0], pair[1]), Distance.of(pair[1], pair[0]), 0);
        }
    }

    @Test
    public void invariance() {
        Random random = new Random(53);
        for (int k = 0; k < 500; k++) {
            double size = 1 + 100 * random.nextDouble();
            Geometry<C2D> g1 = randomGeometry(random, 1e5, 2e5, size);
            Geometry<C2D> g2 = randomGeometry(random, 1e5 + 3 * size * random.nextDouble(), 2e5, size);
            double d = Distance.of(g1, g2);
            assertEquals(d, Distance.of(g2, g1), 0);
            assertEquals(d * 0x1p-30, Distance.of(scale(g1, 0x1p-30), scale(g2, 0x1p-30)), 0);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Length
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void simpleLengths() {
        assertEquals(5.0, Length.of(geometry("LINESTRING (0 0, 3 4)")), 0);
        assertEquals(10.0, Length.of(geometry("LINESTRING (0 0, 3 4, 0 8)")), 0);
        assertEquals(4.0 + 16, Length.of(geometry("POLYGON ((0 0, 4 0, 4 4, 0 4, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1))")), 0);
        assertEquals(10.0, Length.of(geometry("GEOMETRYCOLLECTION (POINT (5 5), LINESTRING (0 0, 0 10))")), 0);
        assertEquals(0.0, Length.of(geometry("LINESTRING EMPTY")), 0);
        assertEquals(0.0, Length.of(geometry("MULTIPOINT ((0 0), (1 1))")), 0);
        assertEquals(Math.sqrt(2), Length.of(geometry("LINESTRING (0 0, 1 1)")), 0);
        assertThrows(IllegalArgumentException.class, () -> Length.of(lineString(new double[]{0, Double.NaN},
                new double[]{0, 1})));
    }

    @Test
    public void tiesAreRoundedToEven() {
        double u = 0x1p-53;
        // 1 + 2^-53 is the midpoint between 1 and 1 + 2^-52: to the even 1
        assertEquals(1.0, Length.of(lineString(new double[]{0, 1, 1}, new double[]{0, 0, u})), 0);
        // 1 + 3 2^-53 is the midpoint between 1 + 2^-52 and 1 + 2^-51: to the even 1 + 2^-51
        assertEquals(1 + 4 * u, Length.of(lineString(new double[]{0, 1, 1}, new double[]{0, 0, 3 * u})), 0);
        // with lengths that are rational, but not axis-parallel: 5 + 5 2^-53 + 5 2^-53 + 2 2^-53 is 5 + 1.5 ulp
        @SuppressWarnings("unchecked")
        MultiLineString<C2D> lines = new MultiLineString<>(lineString(new double[]{0, 3}, new double[]{0, 4}),
                lineString(new double[]{0, 3 * u, 6 * u, 6 * u}, new double[]{0, 4 * u, 8 * u, 10 * u}));
        assertEquals(5 + 16 * u, Length.of(lines), 0);
        assertEquals(5 + 16 * u, roundLength(lines), 0);
        // the double-word stage cannot decide a tie, the exact stage can
        org.geolatte.geom.cga.exact.LengthSum sum = new org.geolatte.geom.cga.exact.LengthSum()
                .add(0, 0, 1, 0).add(1, 0, 1, u);
        assertTrue(Double.isNaN(sum.correctlyRounded()));
        assertEquals(1.0, org.geolatte.geom.cga.exact.LengthSum.exact().add(0, 0, 1, 0).add(1, 0, 1, u).value(), 0);
        // just above the tie
        assertEquals(1 + 2 * u, Length.of(lineString(new double[]{0, 1, 1, 1}, new double[]{0, 0, u, u + 0x1p-80})), 0);
    }

    @Test
    public void randomLengthsMatchTheOracle() {
        Random random = new Random(59);
        double[] offsets = {0, 1.5e5, 2e6, -3.7e5, 1e12};
        double[] sizes = {1e-6, 1, 100, 1e4};
        for (int k = 0; k < 2000; k++) {
            double ox = offsets[random.nextInt(offsets.length)], oy = offsets[random.nextInt(offsets.length)];
            double size = sizes[random.nextInt(sizes.length)];
            int n = 2 + random.nextInt(50);
            double[] xs = new double[n], ys = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = ox + size * random.nextDouble();
                ys[i] = oy + size * random.nextDouble();
            }
            LineString<C2D> line = lineString(xs, ys);
            assertEquals(line.toString(), roundLength(line), Length.of(line), 0);
            assertEquals(Length.of(line), Length.fast(line), 1e-12 * Length.of(line));
            assertEquals(JTS.to(line).getLength(), Length.fast(line), 0);   // the same sum, in the same order
        }
    }

    @Test
    public void extremeLengthsAreFaithful() {
        Random random = new Random(61);
        for (double scale : new double[]{0x1p-700, 0x1p-460, 0x1p460, 0x1p600}) {
            for (int k = 0; k < 50; k++) {
                int n = 2 + random.nextInt(10);
                double[] xs = new double[n], ys = new double[n];
                for (int i = 0; i < n; i++) {
                    xs[i] = scale * random.nextDouble();
                    ys[i] = scale * random.nextDouble();
                }
                LineString<C2D> line = lineString(xs, ys);
                double expected = roundLength(line), actual = Length.of(line);
                assertTrue(expected + " / " + actual,
                        actual == expected || actual == Math.nextUp(expected) || actual == Math.nextDown(expected));
            }
        }
    }

    @Test
    public void longLines() {
        Random random = new Random(67);
        int n = 100_000;
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = Math.round((150000 + i + random.nextDouble()) * 1000) / 1000.0;
            ys[i] = Math.round((200000 + 50 * Math.sin(i / 100.0)) * 1000) / 1000.0;
        }
        LineString<C2D> line = lineString(xs, ys);
        assertEquals(roundLength(line), Length.of(line), 0);
    }

    // ---------------------------------------------------------------------------------------------------------
    // oracles
    // ---------------------------------------------------------------------------------------------------------

    // the length, as the sum of square roots with 60 digits (exact for axis-parallel segments), rounded to the
    // nearest double
    private static double roundLength(Geometry<C2D> line) {
        MathContext context = new MathContext(60);
        BigDecimal sum = BigDecimal.ZERO;
        for (double[] s : segments(line)) {
            BigDecimal dx = new BigDecimal(s[2]).subtract(new BigDecimal(s[0]));
            BigDecimal dy = new BigDecimal(s[3]).subtract(new BigDecimal(s[1]));
            // axis-parallel lengths exactly, so that exact ties stay ties
            sum = sum.add(dx.signum() == 0 || dy.signum() == 0 ? dx.abs().add(dy.abs())
                    : dx.multiply(dx).add(dy.multiply(dy)).sqrt(context));
        }
        return sum.doubleValue();
    }

    // the brute force minimum over vertex-element pairs of the exact squared distances, then rounded
    private static void checkDistanceOracle(Geometry<C2D> g1, Geometry<C2D> g2) {
        double actual = Distance.of(g1, g2);
        if (Relate.intersects(g1, g2)) {
            assertEquals(0.0, actual, 0);
            return;
        }
        List<double[]> e1 = elements(g1), e2 = elements(g2);
        BigDecimal[] min = null;
        for (double[] a : e1) {
            for (double[] b : e2) {
                for (BigDecimal[] d : new BigDecimal[][]{
                        squared(a[0], a[1], b), squared(a[2], a[3], b), squared(b[0], b[1], a), squared(b[2], b[3], a)}) {
                    if (min == null || d[0].multiply(min[1]).compareTo(min[0].multiply(d[1])) < 0) min = d;
                }
            }
        }
        assertEquals(g1 + " / " + g2, MinimumDistanceTest.roundSqrt(min[0], min[1]), actual, 0);
    }

    // the exact squared distance of p to the segment (or point) e, as {numerator, denominator}
    private static BigDecimal[] squared(double px, double py, double[] e) {
        BigDecimal x = new BigDecimal(px), y = new BigDecimal(py);
        BigDecimal ax = new BigDecimal(e[0]), ay = new BigDecimal(e[1]), bx = new BigDecimal(e[2]), by = new BigDecimal(e[3]);
        BigDecimal ex = bx.subtract(ax), ey = by.subtract(ay), fx = x.subtract(ax), fy = y.subtract(ay);
        BigDecimal len2 = ex.multiply(ex).add(ey.multiply(ey));
        BigDecimal t = fx.multiply(ex).add(fy.multiply(ey));
        if (len2.signum() == 0 || t.signum() <= 0) return new BigDecimal[]{fx.multiply(fx).add(fy.multiply(fy)), BigDecimal.ONE};
        if (t.compareTo(len2) >= 0) {
            BigDecimal gx = x.subtract(bx), gy = y.subtract(by);
            return new BigDecimal[]{gx.multiply(gx).add(gy.multiply(gy)), BigDecimal.ONE};
        }
        BigDecimal c = ex.multiply(fy).subtract(ey.multiply(fx));
        return new BigDecimal[]{c.multiply(c), len2};
    }

    // the points and segments, as {x0, y0, x1, y1}
    private static List<double[]> elements(Geometry<?> g) {
        List<double[]> result = new ArrayList<>();
        if (g instanceof Point) {
            if (!g.isEmpty()) {
                double x = g.getPositionN(0).getCoordinate(0), y = g.getPositionN(0).getCoordinate(1);
                result.add(new double[]{x, y, x, y});
            }
        } else if (g instanceof LineString) {
            result.addAll(segments(g));
            if (g.getNumPositions() == 1) {
                double x = g.getPositionN(0).getCoordinate(0), y = g.getPositionN(0).getCoordinate(1);
                result.add(new double[]{x, y, x, y});
            }
        } else if (g instanceof Polygon) {
            Polygon<?> p = (Polygon<?>) g;
            if (!p.isEmpty()) {
                result.addAll(segments(p.getExteriorRing()));
                for (int i = 0; i < p.getNumInteriorRing(); i++) result.addAll(segments(p.getInteriorRingN(i)));
            }
        } else if (g instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> c = (AbstractGeometryCollection<?, ?>) g;
            for (int i = 0; i < c.getNumGeometries(); i++) result.addAll(elements(c.getGeometryN(i)));
        }
        return result;
    }

    private static List<double[]> segments(Geometry<?> line) {
        List<double[]> result = new ArrayList<>();
        if (line instanceof Polygon) return elements(line);
        if (line instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> c = (AbstractGeometryCollection<?, ?>) line;
            for (int i = 0; i < c.getNumGeometries(); i++) result.addAll(segments(c.getGeometryN(i)));
            return result;
        }
        if (!(line instanceof LineString)) return result;
        for (int i = 0; i + 1 < line.getNumPositions(); i++) {
            Position a = line.getPositionN(i), b = line.getPositionN(i + 1);
            result.add(new double[]{a.getCoordinate(0), a.getCoordinate(1), b.getCoordinate(0), b.getCoordinate(1)});
        }
        return result;
    }

    // ---------------------------------------------------------------------------------------------------------
    // generators
    // ---------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Geometry<C2D>[][] pairs(Geometry<C2D> a, Geometry<C2D> b, Geometry<C2D> c) {
        return new Geometry[][]{{a, b}, {a, c}, {b, c}};
    }

    private static Point<C2D> point(double x, double y) {
        return new Point<>(new C2D(x, y), crs);
    }

    private static LineString<C2D> lineString(double[] xs, double[] ys) {
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.variableSized(C2D.class);
        for (int i = 0; i < xs.length; i++) builder.add(xs[i], ys[i]);
        return new LineString<>(builder.toPositionSequence(), crs);
    }

    private static Geometry<C2D> randomGeometry(Random random, double ox, double oy, double size) {
        switch (random.nextInt(4)) {
            case 0:
                return point(ox + size * random.nextDouble(), oy + size * random.nextDouble());
            case 1: {
                int n = 2 + random.nextInt(6);
                double[] xs = new double[n], ys = new double[n];
                for (int i = 0; i < n; i++) {
                    xs[i] = ox + size * random.nextDouble();
                    ys[i] = oy + size * random.nextDouble();
                }
                return lineString(xs, ys);
            }
            case 2:
                return star(random, ox + size / 2, oy + size / 2, size / 2, 3 + random.nextInt(8));
            default: {
                List<Point<C2D>> points = new ArrayList<>();
                for (int i = 0; i < 1 + random.nextInt(4); i++) {
                    points.add(point(ox + size * random.nextDouble(), oy + size * random.nextDouble()));
                }
                @SuppressWarnings("unchecked")
                Point<C2D>[] array = points.toArray(new Point[0]);
                return new MultiPoint<>(array);
            }
        }
    }

    private static Geometry<C2D> gridGeometry(Random random) {
        int n = 1 + random.nextInt(4);
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = random.nextInt(9) * 0.5;
            ys[i] = random.nextInt(9) * 0.5;
        }
        if (n == 1) return point(xs[0], ys[0]);
        return lineString(xs, ys);
    }

    private static Polygon<C2D> star(Random random, double cx, double cy, double radius, int n) {
        PositionSequenceBuilder<C2D> builder = PositionSequenceBuilders.variableSized(C2D.class);
        double x0 = 0, y0 = 0;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * (i + 0.8 * random.nextDouble()) / n;
            double r = radius * (0.5 + 0.5 * random.nextDouble());
            double x = cx + r * Math.cos(angle), y = cy + r * Math.sin(angle);
            if (i == 0) {
                x0 = x;
                y0 = y;
            }
            builder.add(x, y);
        }
        builder.add(x0, y0);
        return new Polygon<>(builder.toPositionSequence(), crs);
    }

    @SuppressWarnings("unchecked")
    private static Geometry<C2D> scale(Geometry<C2D> g, double factor) {
        org.locationtech.jts.geom.Geometry jts = JTS.to(g).copy();
        jts.apply(new org.locationtech.jts.geom.CoordinateSequenceFilter() {
            @Override
            public void filter(org.locationtech.jts.geom.CoordinateSequence seq, int i) {
                seq.setOrdinate(i, 0, seq.getOrdinate(i, 0) * factor);
                seq.setOrdinate(i, 1, seq.getOrdinate(i, 1) * factor);
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public boolean isGeometryChanged() {
                return true;
            }
        });
        jts.geometryChanged();
        return (Geometry<C2D>) JTS.from(jts, crs);
    }
}
