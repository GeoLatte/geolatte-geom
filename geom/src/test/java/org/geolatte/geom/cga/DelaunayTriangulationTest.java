package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Predicates;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.*;

import static org.geolatte.geom.builder.DSL.*;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_2D_METER;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.PROJECTED_3D_METER;
import static org.junit.Assert.*;

/**
 * Tests {@link DelaunayTriangulation} by checking the defining properties of the result with exact predicates.
 */
public class DelaunayTriangulationTest {

    private final Random random = new Random(41L);

    @Test
    public void smallInputs() {
        assertEquals(0, triangulate().getNumTriangles());
        assertEquals(0, triangulate().getEdgeIndices().length);
        assertEquals(0, triangulate(1, 1).getEdgeIndices().length);
        assertArrayEquals(new int[]{0, 1}, triangulate(1, 1, 2, 2).getEdgeIndices());
        DelaunayTriangulation<C2D> triangle = triangulate(0, 0, 0, 1, 1, 0);
        assertArrayEquals(new int[]{0, 2, 1}, triangle.getTriangleIndices());   // counterclockwise
        assertArrayEquals(new int[]{0, 1, 0, 2, 1, 2}, triangle.getEdgeIndices());
    }

    @Test
    public void collinearPositions() {
        DelaunayTriangulation<C2D> line = triangulate(2, 2, 0, 0, 3, 3, 1, 1);
        assertEquals(0, line.getNumTriangles());
        assertTrue(line.getTriangles().isEmpty());
        // consecutive along the line: (0,0)-(1,1)-(2,2)-(3,3) = 1-3, 3-0, 0-2
        assertArrayEquals(new int[]{0, 2, 0, 3, 1, 3}, line.getEdgeIndices());
    }

    @Test
    public void duplicatesAreRemoved() {
        DelaunayTriangulation<C2D> t = triangulate(0, 0, 1, 0, 0, 0, 0, 1, 1, 0, -0.0, 1);
        assertEquals(3, t.getVertices().size());
        assertEquals(new C2D(0, 1), t.getVertices().get(2));
        assertEquals(1, t.getNumTriangles());
    }

    @Test
    public void squareIsCanonical() {
        // the 4 corners are cocircular: both diagonals are Delaunay. The perturbation picks one, independent of
        // the order of the input
        double[][] corners = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        Set<Set<Set<List<Double>>>> results = new HashSet<>();
        List<double[]> list = new ArrayList<>(Arrays.asList(corners));
        for (int i = 0; i < 24; i++) {
            Collections.shuffle(list, random);
            results.add(triangleSet(triangulate(list)));
        }
        assertEquals(1, results.size());
        assertEquals(2, results.iterator().next().size());
    }

    @Test
    public void verticesAndGeometries() {
        List<C3D> positions = Arrays.asList(new C3D(0, 0, 1), new C3D(4, 0, 2), new C3D(0, 4, 3), new C3D(4, 4, 4),
                new C3D(2, 1, 5));
        DelaunayTriangulation<C3D> t = DelaunayTriangulation.of(positions, PROJECTED_3D_METER);
        assertEquals(positions, t.getVertices());
        assertSame(positions.get(4), t.getVertices().get(4));
        assertArrayEquals(new int[]{0, 1, 4, 0, 4, 2, 1, 3, 4, 2, 4, 3}, t.getTriangleIndices());
        GeometryCollection<C3D> triangles = t.getTriangles();
        assertEquals(4, triangles.getNumGeometries());
        assertEquals(polygon(PROJECTED_3D_METER, ring(c(0, 0, 1), c(4, 0, 2), c(2, 1, 5), c(0, 0, 1))),
                triangles.getGeometryN(0));
        MultiLineString<C3D> edges = t.getEdges();
        assertEquals(8, edges.getNumGeometries());
        assertEquals(linestring(PROJECTED_3D_METER, c(0, 0, 1), c(4, 0, 2)), edges.getGeometryN(0));

        DelaunayTriangulation<C2D> fromGeometry = DelaunayTriangulation.of(
                multipoint(PROJECTED_2D_METER, new C2D(0, 0), new C2D(1, 0), new C2D(0, 1)));
        assertEquals(PROJECTED_2D_METER, fromGeometry.getTriangles().getCoordinateReferenceSystem());
        assertTrue(DelaunayTriangulation.of(new Point<>(PROJECTED_2D_METER)).getEdges().isEmpty());
    }

    @Test
    public void invalidInput() {
        assertThrows(IllegalArgumentException.class, () -> triangulate(0, 0, 1, Double.NaN, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> triangulate(0, 0, 1, 1, Double.POSITIVE_INFINITY, 1));
    }

    @Test
    public void randomPositions() {
        for (int trial = 0; trial < 30; trial++) {
            int n = 3 + random.nextInt(300);
            double scale = Math.scalb(1.0, random.nextInt(400) - 200);
            double offset = random.nextBoolean() ? 0 : scale * 1e6;
            double[] xy = new double[2 * n];
            for (int i = 0; i < 2 * n; i++) xy[i] = offset + scale * random.nextDouble();
            assertDelaunay(triangulate(xy));
        }
    }

    @Test
    public void gridsAreCanonical() {
        // regular grids: many cocircular quadruples, and collinear positions on the hull
        for (int trial = 0; trial < 10; trial++) {
            int w = 2 + random.nextInt(12), h = 1 + random.nextInt(12);
            double step = trial % 2 == 0 ? 1 : 0.1;
            List<double[]> grid = new ArrayList<>();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) grid.add(new double[]{i * step, j * step});
            }
            DelaunayTriangulation<C2D> t = triangulate(grid);
            if (h > 1) {
                assertDelaunay(t);
                assertEquals(2 * (w - 1) * (h - 1), t.getNumTriangles());
            }
            Set<Set<Set<List<Double>>>> results = new HashSet<>();
            results.add(triangleSet(t));
            for (int s = 0; s < 5; s++) {
                Collections.shuffle(grid, random);
                results.add(triangleSet(triangulate(grid)));
            }
            assertEquals(1, results.size());
        }
    }

    @Test
    public void nearlyDegenerateConfigurations() {
        // positions on a circle (rounded, so nearly cocircular), on a perturbed grid, and on the boundary of a
        // square: the cases where the floating point algorithm fails (book, Sec. 8.3.8)
        for (double perturbation : new double[]{1e-1, 1e-5, 1e-9, 1e-13, 0}) {
            List<double[]> circle = new ArrayList<>(), grid = new ArrayList<>(), square = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                double a = 2 * Math.PI * i / 100;
                circle.add(new double[]{Math.cos(a) + perturbation * random.nextGaussian(),
                        Math.sin(a) + perturbation * random.nextGaussian()});
                grid.add(new double[]{i % 10 + perturbation * random.nextGaussian(),
                        i / 10 + perturbation * random.nextGaussian()});
                double s = 4.0 * i / 100;
                double[] onSquare = s < 1 ? new double[]{s, 0} : s < 2 ? new double[]{1, s - 1}
                        : s < 3 ? new double[]{3 - s, 1} : new double[]{0, 4 - s};
                square.add(new double[]{onSquare[0] + perturbation * random.nextGaussian(),
                        onSquare[1] + perturbation * random.nextGaussian()});
            }
            assertDelaunay(triangulate(circle));
            assertDelaunay(triangulate(grid));
            assertDelaunay(triangulate(square));
        }
    }

    @Test
    public void jtsMissesEdgesAlongTheHull() {
        for (int trial = 0; trial < 10; trial++) {
            int n = 1000;
            List<double[]> points = new ArrayList<>();
            List<Coordinate> coordinates = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                double x = 150000 + 1000 * random.nextDouble(), y = 200000 + 1000 * random.nextDouble();
                points.add(new double[]{x, y});
                coordinates.add(new Coordinate(x, y));
            }
            DelaunayTriangulation<C2D> t = triangulate(points);
            DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
            builder.setSites(coordinates);
            org.locationtech.jts.geom.Geometry jtsEdges = builder.getEdges(new GeometryFactory());
            Set<Set<List<Double>>> jts = new HashSet<>();
            for (int i = 0; i < jtsEdges.getNumGeometries(); i++) {
                Coordinate[] cs = jtsEdges.getGeometryN(i).getCoordinates();
                jts.add(new HashSet<>(Arrays.asList(Arrays.asList(cs[0].x, cs[0].y), Arrays.asList(cs[1].x, cs[1].y))));
            }
            Set<Set<List<Double>>> ours = new HashSet<>();
            int[] e = t.getEdgeIndices();
            for (int i = 0; i < e.length; i += 2) {
                ours.add(new HashSet<>(Arrays.asList(xy(t, e[i]), xy(t, e[i + 1]))));
            }
            // JTS uses a finite frame triangle, so it misses the (thin) triangles along the convex hull whose
            // circumcircle contains a frame vertex: its edges are a subset, and each missing edge belongs to a
            // triangle with a circumradius larger than the extent of the points (here 1000)
            assertDelaunay(t);
            assertTrue(ours.containsAll(jts));
            Set<Set<List<Double>>> largeCircle = new HashSet<>();
            int[] tri = t.getTriangleIndices();
            for (int i = 0; i < tri.length; i += 3) {
                double[] c = org.geolatte.geom.cga.exact.Constructions.circumcenter(t.xs[tri[i]], t.ys[tri[i]],
                        t.xs[tri[i + 1]], t.ys[tri[i + 1]], t.xs[tri[i + 2]], t.ys[tri[i + 2]]);
                if (Math.hypot(c[0] - t.xs[tri[i]], c[1] - t.ys[tri[i]]) < 1000) continue;
                for (int k = 0; k < 3; k++) {
                    largeCircle.add(new HashSet<>(Arrays.asList(xy(t, tri[i + k]), xy(t, tri[i + (k + 1) % 3]))));
                }
            }
            ours.removeAll(jts);
            assertTrue(largeCircle.containsAll(ours));
        }
    }

    // ---------------------------------------------------------------------------------------------------------

    /**
     * Checks that the triangulation is a Delaunay triangulation of its vertices: consistent neighbors, every
     * triangle counterclockwise with an empty circumcircle, the hull edges convex with all vertices on their inner
     * side, and the number of triangles and edges as given by Euler's formula.
     */
    private static void assertDelaunay(DelaunayTriangulation<C2D> t) {
        int n = t.getVertices().size();
        int[] tri = t.triangles, nb = t.neighbors;
        int m = tri.length / 3;
        int hullEdges = 0;
        for (int i = 0; i < m; i++) {
            assertEquals(1, orientation(t, tri[3 * i], tri[3 * i + 1], tri[3 * i + 2]));
            for (int k = 0; k < 3; k++) {
                int a = tri[3 * i + (k + 1) % 3], b = tri[3 * i + (k + 2) % 3];
                int u = nb[3 * i + k];
                if (u < 0) {
                    hullEdges++;
                    for (int v = 0; v < n; v++) assertTrue(orientation(t, a, b, v) >= 0);
                } else {
                    // the neighbor has the reversed edge, and points back
                    boolean found = false;
                    for (int j = 0; j < 3; j++) {
                        if (tri[3 * u + (j + 1) % 3] == b && tri[3 * u + (j + 2) % 3] == a) {
                            assertEquals(i, nb[3 * u + j]);
                            found = true;
                        }
                    }
                    assertTrue(found);
                }
            }
            for (int v = 0; v < n; v++) {
                assertTrue("vertex inside circumcircle", Predicates.inCircle(
                        t.xs[tri[3 * i]], t.ys[tri[3 * i]], t.xs[tri[3 * i + 1]], t.ys[tri[3 * i + 1]],
                        t.xs[tri[3 * i + 2]], t.ys[tri[3 * i + 2]], t.xs[v], t.ys[v]) <= 0);
            }
        }
        // the hull edges are the boundary positions (including collinear ones): T = 2n - h - 2, E = 3n - h - 3
        assertEquals(2 * n - hullEdges - 2, m);
        assertEquals(2 * (3 * n - hullEdges - 3), t.getEdgeIndices().length);
    }

    private static int orientation(DelaunayTriangulation<C2D> t, int a, int b, int c) {
        return Predicates.orientation(t.xs[a], t.ys[a], t.xs[b], t.ys[b], t.xs[c], t.ys[c]);
    }

    private static Set<Set<List<Double>>> triangleSet(DelaunayTriangulation<C2D> t) {
        Set<Set<List<Double>>> result = new HashSet<>();
        int[] tri = t.getTriangleIndices();
        for (int i = 0; i < tri.length; i += 3) {
            result.add(new HashSet<>(Arrays.asList(xy(t, tri[i]), xy(t, tri[i + 1]), xy(t, tri[i + 2]))));
        }
        return result;
    }

    private static List<Double> xy(DelaunayTriangulation<C2D> t, int v) {
        return Arrays.asList(t.xs[v], t.ys[v]);
    }

    private static DelaunayTriangulation<C2D> triangulate(double... xy) {
        List<C2D> positions = new ArrayList<>();
        for (int i = 0; i < xy.length; i += 2) positions.add(new C2D(xy[i], xy[i + 1]));
        return DelaunayTriangulation.of(positions, PROJECTED_2D_METER);
    }

    private static DelaunayTriangulation<C2D> triangulate(List<double[]> points) {
        List<C2D> positions = new ArrayList<>();
        for (double[] p : points) positions.add(new C2D(p[0], p[1]));
        return DelaunayTriangulation.of(positions, PROJECTED_2D_METER);
    }
}
