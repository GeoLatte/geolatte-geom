package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.jts.JTS;
import org.junit.Test;
import org.locationtech.jts.operation.buffer.BufferOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.geolatte.geom.cga.BufferParameters.EndCap;
import static org.geolatte.geom.cga.BufferParameters.Join;
import static org.junit.Assert.*;

public class BufferPropertyTest {

    private static final CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;

    // -------------------------------------------------------------------------------------------------------
    // random geometries
    // -------------------------------------------------------------------------------------------------------

    private static LineString<C2D> randomLine(Random random, double ox, double oy, double size, int n, boolean closed) {
        PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.variableSized(C2D.class);
        double x0 = ox + size * random.nextDouble(), y0 = oy + size * random.nextDouble();
        b.add(x0, y0);
        for (int i = 1; i < n; i++) b.add(ox + size * random.nextDouble(), oy + size * random.nextDouble());
        if (closed) b.add(x0, y0);
        return new LineString<>(b.toPositionSequence(), crs);
    }

    // a line along a circle of the given radius, with vertices spaced closely, and some noise
    private static LineString<C2D> denseLine(Random random, double cx, double cy, double radius, int n) {
        PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.fixedSized(n, C2D.class);
        for (int i = 0; i < n; i++) {
            double angle = 1.5 * Math.PI * i / n;
            double r = radius * (1 + 0.01 * random.nextGaussian());
            b.add(cx + r * Math.cos(angle), cy + r * Math.sin(angle));
        }
        return new LineString<>(b.toPositionSequence(), crs);
    }

    // a closed ring of n vertices around a centre, with tight waves and some noise
    private static PositionSequence<C2D> wavyRing(Random random, double cx, double cy, double radius, int n) {
        PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.fixedSized(n + 1, C2D.class);
        int waves = 5 + random.nextInt(15);
        double x0 = 0, y0 = 0;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double r = radius * (1 + 0.12 * Math.sin(waves * angle) + 0.003 * random.nextGaussian());
            double x = cx + r * Math.cos(angle), y = cy + r * Math.sin(angle);
            if (i == 0) {
                x0 = x;
                y0 = y;
            }
            b.add(x, y);
        }
        b.add(x0, y0);
        return b.toPositionSequence();
    }

    // dense wavy polygons (possibly with a hole) and lines, whose offset curves have many spikes
    static Geometry<C2D> spikyGeometry(Random random, int kind, double ox, double oy) {
        int n = 100 + random.nextInt(500);
        PositionSequence<C2D> shell = wavyRing(random, ox + 10, oy + 10, 10, n);
        switch (kind) {
            case 0:
                return new Polygon<>(new LinearRing<>(shell, crs));
            case 1:
                return new Polygon<>(new LinearRing<>(shell, crs),
                        new LinearRing<>(wavyRing(random, ox + 10, oy + 10, 3, n / 3).reverse(), crs));
            case 2:
                return new LineString<>(shell, crs);
            default:
                return denseLine(random, ox + 10, oy + 10, 8, 100 + random.nextInt(500));
        }
    }

    private static Geometry<C2D> randomPoints(Random random, double ox, double oy, double size, int n) {
        List<Point<C2D>> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            points.add(new Point<>(new C2D(ox + size * random.nextDouble(), oy + size * random.nextDouble()), crs));
        }
        return Geometries.mkMultiPoint(points);
    }

    static Geometry<C2D> randomGeometry(Random random, int kind, double ox, double oy) {
        switch (kind) {
            case 0:
                return OverlayPropertyTest.star(random, ox + 10, oy + 10, 10, 5 + random.nextInt(40), false);
            case 1:
                return OverlayPropertyTest.star(random, ox + 10, oy + 10, 10, 5 + random.nextInt(40), true);
            case 2:
                return randomLine(random, ox, oy, 20, 2 + random.nextInt(12), false);
            case 3:
                return randomLine(random, ox, oy, 20, 3 + random.nextInt(10), true);
            case 4:
                return denseLine(random, ox + 10, oy + 10, 8, 50 + random.nextInt(200));
            case 5:
                return randomPoints(random, ox, oy, 20, 1 + random.nextInt(20));
            default:
                List<Geometry<C2D>> parts = new ArrayList<>();
                parts.add(OverlayPropertyTest.star(random, ox + 8, oy + 8, 8, 5 + random.nextInt(20), false));
                parts.add(OverlayPropertyTest.star(random, ox + 12, oy + 12, 8, 5 + random.nextInt(20), true));
                parts.add(randomLine(random, ox, oy, 20, 2 + random.nextInt(6), false));
                parts.add(randomPoints(random, ox, oy, 20, 3));
                return Geometries.mkGeometryCollection(parts);
        }
    }

    private static final double[] DISTANCES = {0.05, 0.5, 2, 6, 15};

    static double randomDistance(Random random, Geometry<C2D> g) {
        double d = DISTANCES[random.nextInt(DISTANCES.length)];
        boolean areal = Overlay.typeDimension(g) == 2 || g instanceof GeometryCollection;
        return areal && random.nextBoolean() ? -d : d;
    }

    // -------------------------------------------------------------------------------------------------------
    // the distance oracle
    // -------------------------------------------------------------------------------------------------------

    /**
     * The segments and points of a geometry, and whether a point lies in its polygons
     */
    static final class Oracle {
        final List<double[]> segments = new ArrayList<>();
        final PointLocator polygons;

        /**
         * @param erosion whether the oracle is for a negative distance: then only the boundary of the union of the
         *                polygons counts
         */
        Oracle(Geometry<C2D> g, boolean erosion) {
            if (erosion) {
                List<Geometry<C2D>> areal = new ArrayList<>();
                collectPolygons(g, areal);
                g = Overlay.unaryUnion(Geometries.mkGeometryCollection(areal));
            }
            GeometryElements e = GeometryElements.of(g);
            for (GeometryElements.Ring r : e.rings) addChain(r.xs, r.ys);
            for (double[][] line : e.lines) addChain(line[0], line[1]);
            for (double[] p : e.points) segments.add(new double[]{p[0], p[1], p[0], p[1]});
            List<Geometry<C2D>> areal = new ArrayList<>();
            collectPolygons(g, areal);
            polygons = areal.isEmpty() ? null : PointLocator.of(Geometries.mkGeometryCollection(areal));
        }

        private void addChain(double[] xs, double[] ys) {
            for (int i = 0; i + 1 < xs.length; i++) segments.add(new double[]{xs[i], ys[i], xs[i + 1], ys[i + 1]});
        }

        private static void collectPolygons(Geometry<C2D> g, List<Geometry<C2D>> areal) {
            if (g instanceof Polygon || g instanceof MultiPolygon) {
                areal.add(g);
            } else if (g instanceof AbstractGeometryCollection) {
                AbstractGeometryCollection<C2D, ?> c = (AbstractGeometryCollection<C2D, ?>) g;
                for (int i = 0; i < c.getNumGeometries(); i++) collectPolygons(c.getGeometryN(i), areal);
            }
        }

        boolean inPolygons(double x, double y) {
            return polygons != null && polygons.locate(x, y) != Location.EXTERIOR;
        }

        double distanceToElements(double x, double y) {
            double min = Double.POSITIVE_INFINITY;
            for (double[] s : segments) min = Math.min(min, distance(s, x, y));
            return min;
        }

        private static double distance(double[] s, double x, double y) {
            double dx = s[2] - s[0], dy = s[3] - s[1];
            double t = dx == 0 && dy == 0 ? 0 : ((x - s[0]) * dx + (y - s[1]) * dy) / (dx * dx + dy * dy);
            t = Math.max(0, Math.min(1, t));
            return Math.hypot(x - (s[0] + t * dx), y - (s[1] + t * dy));
        }
    }

    /**
     * For round joins and caps, checks sample points against the exact buffer: the result contains the points
     * within the inner radius of the arcs, and its interior contains no point beyond the distance. For a negative
     * distance, mirrored inside the polygons.
     */
    static void checkAgainstOracle(Geometry<C2D> g, double d, BufferParameters parameters,
                                           Geometry<C2D> result, Random random) {
        Oracle oracle = new Oracle(g, d < 0);
        PointLocator locator = result.isEmpty() ? null : PointLocator.of(result);
        double r = Math.abs(d);
        // the arcs are divided into parts of less than 1.5 quantum; with round joins, simplification with tolerance h
        // loses at most 2h more
        double h = parameters.getJoin() == Join.ROUND ? parameters.getSimplifyFactor() * r : 0;
        double inner = r * Math.cos(0.75 * Math.PI / 2 / parameters.getQuadrantSegments()) - 2 * h;
        double margin = 1e-9 * Math.max(1, r);
        Envelope<C2D> envelope = g.getEnvelope();
        double minX = envelope.lowerLeft().getX() - r - 1, maxX = envelope.upperRight().getX() + r + 1;
        double minY = envelope.lowerLeft().getY() - r - 1, maxY = envelope.upperRight().getY() + r + 1;
        List<double[]> samples = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            samples.add(new double[]{minX + (maxX - minX) * random.nextDouble(),
                    minY + (maxY - minY) * random.nextDouble()});
        }
        // near the arcs around the vertices
        for (int i = 0; i < 200 && !oracle.segments.isEmpty(); i++) {
            double[] s = oracle.segments.get(random.nextInt(oracle.segments.size()));
            double angle = 2 * Math.PI * random.nextDouble();
            double distance = r * (inner / r + (1 - inner / r) * 2 * random.nextDouble() - 0.5 * (1 - inner / r));
            samples.add(new double[]{s[0] + distance * Math.cos(angle), s[1] + distance * Math.sin(angle)});
        }
        for (double[] p : samples) {
            double x = p[0], y = p[1];
            double distance = oracle.distanceToElements(x, y);
            Location location = locator == null ? Location.EXTERIOR : locator.locate(x, y);
            boolean inPolygons = oracle.inPolygons(x, y);
            String message = "buffer by " + d + " of " + g + " with " + parameters + " at (" + x + ", " + y
                    + "), distance " + distance + ": " + location;
            if (d > 0) {
                if (inPolygons || distance <= inner - margin) assertNotEquals(message, Location.EXTERIOR, location);
                if (!inPolygons && distance >= r + margin) assertNotEquals(message, Location.INTERIOR, location);
            } else {
                if (inPolygons && distance >= r + margin) assertNotEquals(message, Location.EXTERIOR, location);
                if (!inPolygons || distance <= inner - margin) assertNotEquals(message, Location.INTERIOR, location);
            }
        }
    }

    static Geometry<C2D> checkedBuffer(Geometry<C2D> g, double d, BufferParameters parameters, Grid grid) {
        Geometry<C2D> result = Buffer.buffer(g, d, parameters, grid);
        String message = "buffer by " + d + " with " + parameters + " on " + grid + " of " + g + ": " + result;
        assertTrue(message + " is not valid: " + Validity.of(result), Validity.isValid(result));
        assertTrue(message, result.isEmpty() || result instanceof Polygon || result instanceof MultiPolygon);
        return result;
    }

    @Test
    public void roundBuffersAgainstTheExactBuffer() {
        Random random = new Random(1957);
        for (int k = 0; k < 280; k++) {
            int kind = k % 7;
            double ox = random.nextBoolean() ? 0 : 152000, oy = random.nextBoolean() ? 0 : 212000;
            Geometry<C2D> g = randomGeometry(random, kind, ox, oy);
            double d = randomDistance(random, g);
            BufferParameters parameters = BufferParameters.DEFAULT.withQuadrantSegments(1 + random.nextInt(10));
            Geometry<C2D> result = checkedBuffer(g, d, parameters, Grid.DOUBLE);
            checkAgainstOracle(g, d, parameters, result, random);
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // all styles, against JTS
    // -------------------------------------------------------------------------------------------------------

    @Test
    public void spikyPathsAgainstTheExactBuffer() {
        Random random = new Random(2026);
        for (int k = 0; k < 96; k++) {
            int kind = k % 4;
            double ox = random.nextBoolean() ? 0 : 152000, oy = random.nextBoolean() ? 0 : 212000;
            Geometry<C2D> g = spikyGeometry(random, kind, ox, oy);
            double d = new double[]{0.3, 1, 2, 4}[random.nextInt(4)];
            if (kind < 2 && random.nextBoolean()) d = -d;
            BufferParameters parameters = BufferParameters.DEFAULT.withQuadrantSegments(1 + random.nextInt(10))
                    .withSimplifyFactor(new double[]{0, 0.01, 0.05, BufferParameters.MAX_SIMPLIFY_FACTOR}[k / 4 % 4]);
            Geometry<C2D> result = checkedBuffer(g, d, parameters, Grid.DOUBLE);
            checkAgainstOracle(g, d, parameters, result, random);
        }
    }

    private static final EndCap[] CAPS = EndCap.values();
    private static final Join[] JOINS = Join.values();

    static org.locationtech.jts.operation.buffer.BufferParameters jts(BufferParameters p) {
        return JTS.toBufferParameters(p);
    }

    /**
     * A random case on which JTS agrees with the definition of the buffer. Not included: JTS does not unite
     * overlapping polygons before a negative buffer; it drops the offset curve of a hole that the buffer closes,
     * which removes the mitres of the hole; and it can leave holes in the buffer of a closed line that crosses
     * itself.
     */
    static Object[] jtsComparableCase(Random random) {
        int[] kinds = {0, 1, 2, 4, 5, 6};
        int kind = kinds[random.nextInt(kinds.length)];
        double ox = random.nextBoolean() ? 0 : 152000, oy = random.nextBoolean() ? 0 : 212000;
        Geometry<C2D> g = randomGeometry(random, kind, ox, oy);
        double d = randomDistance(random, g);
        if (kind == 6) d = Math.abs(d);
        Join[] joins = kind == 1 || kind == 6 ? new Join[]{Join.ROUND, Join.BEVEL} : JOINS;
        BufferParameters parameters = BufferParameters.DEFAULT
                .withQuadrantSegments(1 + random.nextInt(10))
                .withEndCap(CAPS[random.nextInt(CAPS.length)])
                .withJoin(joins[random.nextInt(joins.length)])
                .withMitreLimit(0.5 + 5 * random.nextDouble());
        return new Object[]{g, d, parameters};
    }

    /**
     * The area of the symmetric difference with the JTS buffer, relative to the distance times the length of the
     * geometry and the circumference of the buffers of its points
     */
    @SuppressWarnings("unchecked")
    static double relativeDifferenceWithJts(Geometry<C2D> g, double d, BufferParameters parameters,
                                            Geometry<C2D> result) {
        Geometry<C2D> expected = JTS.from(BufferOp.bufferOp(JTS.to(g), d, jts(parameters)), crs);
        double r = Math.abs(d);
        return Area.of(Overlay.symDifference(result, expected))
                / (r * (Length.of(g) + 2 * Math.PI * r * g.getNumPositions()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void allStylesAgreeWithJts() {
        Random random = new Random(4111);
        for (int k = 0; k < 420; k++) {
            Object[] c = jtsComparableCase(random);
            Geometry<C2D> g = (Geometry<C2D>) c[0];
            double d = (Double) c[1];
            BufferParameters parameters = (BufferParameters) c[2];
            Geometry<C2D> result = checkedBuffer(g, d, parameters, Grid.DOUBLE);
            // JTS simplifies the input by 1% of the distance, and skips offset points that lie close together
            double difference = relativeDifferenceWithJts(g, d, parameters, result);
            assertTrue("buffer by " + d + " with " + parameters + " of " + g + ": " + result + " differs from JTS by "
                    + difference, difference <= 0.01);
        }
    }

    @Test
    public void fixedGrids() {
        Random random = new Random(77);
        Grid[] grids = {Grid.fixed("0.01"), Grid.fixed("0.5"), Grid.fixed(3)};
        for (int k = 0; k < 140; k++) {
            Geometry<C2D> g = randomGeometry(random, k % 7, 0, 0);
            double d = randomDistance(random, g);
            Grid grid = grids[random.nextInt(grids.length)];
            Geometry<C2D> result = checkedBuffer(g, d, BufferParameters.DEFAULT, grid);
            OverlayTest.assertOnGrid(result, grid);
            // within a cell of the buffer on the double grid
            Geometry<C2D> exact = Buffer.buffer(g, d);
            double g2 = 2 * grid.cellSize().doubleValue();
            double difference = Area.of(Overlay.symDifference(result, exact));
            double perimeter = exact.isEmpty() ? 0 : Length.of(exact);
            assertTrue(grid + ": " + difference, difference <= g2 * (perimeter + Length.of(result)) + 1e-9);
        }
    }
}
