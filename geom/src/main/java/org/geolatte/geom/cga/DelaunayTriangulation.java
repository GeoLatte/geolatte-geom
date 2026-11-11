package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.*;

/**
 * The exact Delaunay triangulation of a set of positions, in the plane of the first two coordinates.
 *
 * <p>The triangulation is computed with the incremental algorithm of Ratschek &amp; Rokne, <em>Geometric
 * Computations with Interval and New Robust Methods</em> (Sec. 8.3), whose only geometric tests are the
 * orientation test and the in-circle test. Both are evaluated exactly (see {@link
 * org.geolatte.geom.cga.exact.Predicates}), so the result is the exact Delaunay triangulation of the positions as
 * given: no position lies strictly inside the circumcircle of a triangle, and no triangle is missing or
 * inverted. The book shows that a floating point implementation of the same algorithm produces up to 50% wrong
 * edges for nearly degenerate inputs.</p>
 *
 * <p>When four or more positions lie exactly on a common circle (e.g. on a regular grid), the Delaunay
 * triangulation is not unique. Then a canonical triangulation is chosen by a symbolic perturbation of the
 * positions (Devillers and Teillaud, as used in CGAL), which only depends on the set of positions, not on their
 * order.</p>
 *
 * <p>Further properties:</p>
 * <ul>
 *     <li>the vertices are the input positions (the same instances, so Z and M values are preserved). Of
 *     positions with equal X and Y coordinates, the first one is used. The vertices are in the order of the
 *     input;</li>
 *     <li>the triangles are counterclockwise, and together cover the convex hull of the positions. Positions on
 *     the boundary of the convex hull are vertices;</li>
 *     <li>the result is deterministic: apart from the numbering of the vertices, it does not depend on the order
 *     of the input positions;</li>
 *     <li>if all positions are collinear, there are no triangles, and the edges connect consecutive positions
 *     along the line.</li>
 * </ul>
 *
 * @param <P> the type of the positions
 */
public final class DelaunayTriangulation<P extends Position> {

    private final CoordinateReferenceSystem<P> crs;
    private final List<P> vertices;
    final double[] xs, ys;
    // 3 vertex indices per triangle; neighbors[3t + k] is the triangle across the edge opposite vertex k (-1 on
    // the convex hull)
    final int[] triangles;
    final int[] neighbors;
    private final int[] edges;

    private DelaunayTriangulation(CoordinateReferenceSystem<P> crs, List<P> vertices, double[] xs, double[] ys,
                                  int[] triangles, int[] neighbors, int[] edges) {
        this.crs = crs;
        this.vertices = Collections.unmodifiableList(vertices);
        this.xs = xs;
        this.ys = ys;
        this.triangles = triangles;
        this.neighbors = neighbors;
        this.edges = edges;
    }

    /**
     * Computes the Delaunay triangulation of the positions of a geometry.
     *
     * @param geometry the geometry
     * @param <P>      the type of the positions
     * @return the Delaunay triangulation, in the coordinate reference system of the geometry
     * @throws IllegalArgumentException if an X or Y coordinate is NaN or infinite
     */
    public static <P extends Position> DelaunayTriangulation<P> of(Geometry<P> geometry) {
        return of(geometry.getPositions(), geometry.getCoordinateReferenceSystem());
    }

    /**
     * Computes the Delaunay triangulation of a collection of positions.
     *
     * @param positions the positions
     * @param crs       the coordinate reference system of the positions (used for the geometries returned by
     *                  {@link #getTriangles()} and {@link #getEdges()})
     * @param <P>       the type of the positions
     * @return the Delaunay triangulation
     * @throws IllegalArgumentException if an X or Y coordinate is NaN or infinite
     */
    public static <P extends Position> DelaunayTriangulation<P> of(Iterable<? extends P> positions,
                                                                   CoordinateReferenceSystem<P> crs) {
        // distinct positions, in input order
        List<P> vertices = new ArrayList<>();
        Set<XY> seen = new HashSet<>();
        for (P p : positions) {
            double x = p.getCoordinate(0), y = p.getCoordinate(1);
            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                throw new IllegalArgumentException("Coordinates must be finite");
            }
            if (seen.add(new XY(x, y))) vertices.add(p);
        }
        int n = vertices.size();
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = vertices.get(i).getCoordinate(0);
            ys[i] = vertices.get(i).getCoordinate(1);
        }
        Triangulator triangulator = new Triangulator(xs, ys);
        int[] edges = triangulator.triangulate()
                ? edgesOf(triangulator.triangles, triangulator.neighbors, n)
                : collinearEdges(xs, ys);
        return new DelaunayTriangulation<>(crs, vertices, xs, ys, triangulator.triangles, triangulator.neighbors, edges);
    }

    /**
     * @return the vertices: the distinct input positions, in input order
     */
    public List<P> getVertices() {
        return vertices;
    }

    /**
     * @return the number of triangles
     */
    public int getNumTriangles() {
        return triangles.length / 3;
    }

    /**
     * Returns the triangles as indices into {@link #getVertices()}: triangle {@code t} has vertices
     * {@code [3t], [3t + 1], [3t + 2]}, in counterclockwise order, starting at the smallest index. The triangles
     * are sorted lexicographically on their vertex indices.
     *
     * @return a copy of the vertex indices of the triangles
     */
    public int[] getTriangleIndices() {
        return triangles.clone();
    }

    /**
     * Returns the edges as indices into {@link #getVertices()}: edge {@code e} connects vertices {@code [2e]} and
     * {@code [2e + 1]}, with {@code [2e] < [2e + 1]}. The edges are sorted lexicographically.
     *
     * @return a copy of the vertex indices of the edges
     */
    public int[] getEdgeIndices() {
        return edges.clone();
    }

    /**
     * Returns the triangles as a {@code GeometryCollection} of {@code Polygon}s, each with a counterclockwise
     * exterior ring that starts and ends at the vertex with the smallest index. The collection is empty if there
     * are no triangles (as for the JTS {@code DelaunayTriangulationBuilder}).
     *
     * @return the triangles
     */
    public GeometryCollection<P> getTriangles() {
        List<Geometry<P>> polygons = new ArrayList<>();
        for (int t = 0; t < getNumTriangles(); t++) {
            PositionSequenceBuilder<P> ring = PositionSequenceBuilders.fixedSized(4, crs.getPositionClass());
            ring.add(vertices.get(triangles[3 * t]));
            ring.add(vertices.get(triangles[3 * t + 1]));
            ring.add(vertices.get(triangles[3 * t + 2]));
            ring.add(vertices.get(triangles[3 * t]));
            polygons.add(new Polygon<>(new LinearRing<>(ring.toPositionSequence(), crs)));
        }
        return polygons.isEmpty() ? new GeometryCollection<>(crs) : Geometries.mkGeometryCollection(polygons);
    }

    /**
     * Returns the edges as a {@code MultiLineString}, in the order of {@link #getEdgeIndices()}. It is empty if
     * there are fewer than two vertices.
     *
     * @return the edges
     */
    public MultiLineString<P> getEdges() {
        List<LineString<P>> lines = new ArrayList<>();
        for (int e = 0; e < edges.length / 2; e++) {
            PositionSequenceBuilder<P> line = PositionSequenceBuilders.fixedSized(2, crs.getPositionClass());
            line.add(vertices.get(edges[2 * e]));
            line.add(vertices.get(edges[2 * e + 1]));
            lines.add(new LineString<>(line.toPositionSequence(), crs));
        }
        return lines.isEmpty() ? new MultiLineString<>(crs) : Geometries.mkMultiLineString(lines);
    }

    CoordinateReferenceSystem<P> getCoordinateReferenceSystem() {
        return crs;
    }

    // ---------------------------------------------------------------------------------------------------------

    // each undirected edge once: the directed edge (a, b) of a triangle is taken if a < b, or if it is on the
    // convex hull (then (b, a) is not an edge of a triangle)
    private static int[] edgesOf(int[] triangles, int[] neighbors, int n) {
        int[] pairs = new int[2 * triangles.length];
        int count = 0;
        for (int t = 0; t < triangles.length / 3; t++) {
            for (int k = 0; k < 3; k++) {
                int a = triangles[3 * t + (k + 1) % 3], b = triangles[3 * t + (k + 2) % 3];
                if (a < b || neighbors[3 * t + k] < 0) {
                    pairs[2 * count] = Math.min(a, b);
                    pairs[2 * count + 1] = Math.max(a, b);
                    count++;
                }
            }
        }
        return sortPairs(Arrays.copyOf(pairs, 2 * count), n);
    }

    // the edges between consecutive positions in lexicographic (x, y) order
    private static int[] collinearEdges(double[] xs, double[] ys) {
        int n = xs.length;
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> {
            int c = Double.compare(xs[a] + 0.0, xs[b] + 0.0);
            return c != 0 ? c : Double.compare(ys[a] + 0.0, ys[b] + 0.0);
        });
        int[] pairs = new int[2 * Math.max(n - 1, 0)];
        for (int i = 0; i + 1 < n; i++) {
            pairs[2 * i] = Math.min(order[i], order[i + 1]);
            pairs[2 * i + 1] = Math.max(order[i], order[i + 1]);
        }
        return sortPairs(pairs, n);
    }

    // sorts pairs (a, b) lexicographically: bucket sort on a, insertion sort on b within a bucket
    private static int[] sortPairs(int[] pairs, int n) {
        int m = pairs.length / 2;
        int[] start = new int[n + 1];
        for (int i = 0; i < m; i++) start[pairs[2 * i] + 1]++;
        for (int v = 0; v < n; v++) start[v + 1] += start[v];
        int[] fill = Arrays.copyOf(start, n);
        int[] second = new int[m];
        for (int i = 0; i < m; i++) second[fill[pairs[2 * i]]++] = pairs[2 * i + 1];
        int[] result = new int[2 * m];
        for (int v = 0; v < n; v++) {
            Arrays.sort(second, start[v], start[v + 1]);
            for (int i = start[v]; i < start[v + 1]; i++) {
                result[2 * i] = v;
                result[2 * i + 1] = second[i];
            }
        }
        return result;
    }

    // exact X and Y, with 0.0 and -0.0 equal
    private static final class XY {
        final double x, y;

        XY(double x, double y) {
            this.x = x + 0.0;
            this.y = y + 0.0;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof XY && ((XY) o).x == x && ((XY) o).y == y;
        }

        @Override
        public int hashCode() {
            return 31 * Double.hashCode(x) + Double.hashCode(y);
        }
    }
}
