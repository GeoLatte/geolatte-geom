package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Constructions;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Voronoi diagram of a set of sites (positions), in the plane of the first two coordinates, clipped to an
 * envelope.
 *
 * <p>The diagram is derived from the exact {@link DelaunayTriangulation}, so its topology is exact: the cells of
 * two sites are adjacent if and only if the sites are connected by a Delaunay edge. The vertices of the cells are
 * the circumcenters of the Delaunay triangles, which in general are not representable as doubles. Each
 * circumcenter is computed <em>correctly rounded</em> ({@link Constructions#circumcenter(double, double, double,
 * double, double, double)}), so</p>
 * <ul>
 *     <li>each vertex is the best possible double approximation of the exact Voronoi vertex;</li>
 *     <li>adjacent cells share exactly the same vertices; this also holds for the points where cells are cut
 *     by the clip envelope (they are computed from the shared edge in a canonical order), so the cells fit together
 *     without gaps or overlaps;</li>
 *     <li>when four or more sites are cocircular, the triangles with the same circumcircle yield exactly the same
 *     vertex, which appears only once in a cell.</li>
 * </ul>
 * <p>Because of the rounding of the vertices, a cell can deviate from the exact cell by up to half a unit in the
 * last place of its vertex coordinates. In nearly degenerate configurations (vertices closer together than that),
 * this can make a cell slightly non-convex or self-intersecting.</p>
 *
 * <p>Cells are clipped to the clip envelope. If none is given, the envelope of the sites is used, expanded on
 * all sides by its diameter (by 1 for a single site), as in the JTS {@code VoronoiDiagramBuilder}. Coordinates
 * other than X and Y of the cell vertices are NaN.</p>
 *
 * @param <P> the type of the positions
 */
public final class VoronoiDiagram<P extends Position> {

    private final CoordinateReferenceSystem<P> crs;
    private final List<P> sites;
    private final List<Polygon<P>> cells;

    private VoronoiDiagram(CoordinateReferenceSystem<P> crs, List<P> sites, List<Polygon<P>> cells) {
        this.crs = crs;
        this.sites = sites;
        this.cells = Collections.unmodifiableList(cells);
    }

    /**
     * Computes the Voronoi diagram of the positions of a geometry, clipped to the default envelope.
     *
     * @param sites the sites
     * @param <P>   the type of the positions
     * @return the Voronoi diagram
     * @throws IllegalArgumentException if an X or Y coordinate is NaN or infinite
     */
    public static <P extends Position> VoronoiDiagram<P> of(Geometry<P> sites) {
        return of(DelaunayTriangulation.of(sites), null);
    }

    /**
     * Computes the Voronoi diagram of the positions of a geometry, clipped to the specified envelope.
     *
     * @param sites the sites
     * @param clip  the clip envelope, or null for the default envelope
     * @param <P>   the type of the positions
     * @return the Voronoi diagram
     * @throws IllegalArgumentException if an X or Y coordinate is NaN or infinite, or the envelope is empty
     */
    public static <P extends Position> VoronoiDiagram<P> of(Geometry<P> sites, Envelope<P> clip) {
        return of(DelaunayTriangulation.of(sites), clip);
    }

    /**
     * Computes the Voronoi diagram of the vertices of a Delaunay triangulation.
     *
     * @param triangulation the Delaunay triangulation
     * @param clip          the clip envelope, or null for the default envelope
     * @param <P>           the type of the positions
     * @return the Voronoi diagram, with the vertices of the triangulation as sites
     * @throws IllegalArgumentException if the envelope is empty
     */
    public static <P extends Position> VoronoiDiagram<P> of(DelaunayTriangulation<P> triangulation, Envelope<P> clip) {
        double[] box = clip == null ? defaultClip(triangulation) : box(clip);
        Builder<P> builder = new Builder<>(triangulation, box);
        return new VoronoiDiagram<>(triangulation.getCoordinateReferenceSystem(), triangulation.getVertices(),
                builder.cells());
    }

    /**
     * @return the sites: the distinct positions, in input order (the vertices of the Delaunay triangulation)
     */
    public List<P> getSites() {
        return sites;
    }

    /**
     * @return the cells, in the order of {@link #getSites()}. A cell that lies outside the clip envelope is an
     * empty polygon. Exterior rings are counterclockwise.
     */
    public List<Polygon<P>> getCells() {
        return cells;
    }

    /**
     * @return the non-empty cells as a {@code GeometryCollection}, in the order of {@link #getSites()}
     */
    public GeometryCollection<P> getDiagram() {
        List<Geometry<P>> nonEmpty = new ArrayList<>();
        for (Polygon<P> cell : cells) {
            if (!cell.isEmpty()) nonEmpty.add(cell);
        }
        return nonEmpty.isEmpty() ? new GeometryCollection<>(crs) : Geometries.mkGeometryCollection(nonEmpty);
    }

    // ---------------------------------------------------------------------------------------------------------

    // {minX, minY, maxX, maxY}
    private static double[] box(Envelope<?> envelope) {
        if (envelope.isEmpty()) throw new IllegalArgumentException("Empty clip envelope");
        double[] box = {envelope.lowerLeft().getCoordinate(0), envelope.lowerLeft().getCoordinate(1),
                envelope.upperRight().getCoordinate(0), envelope.upperRight().getCoordinate(1)};
        for (double v : box) {
            if (!Double.isFinite(v)) throw new IllegalArgumentException("Clip envelope must be finite");
        }
        return box;
    }

    private static double[] defaultClip(DelaunayTriangulation<?> t) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < t.xs.length; i++) {
            minX = Math.min(minX, t.xs[i]);
            maxX = Math.max(maxX, t.xs[i]);
            minY = Math.min(minY, t.ys[i]);
            maxY = Math.max(maxY, t.ys[i]);
        }
        if (t.xs.length == 0) return new double[]{0, 0, 0, 0};
        double expand = Math.hypot(maxX - minX, maxY - minY);
        if (expand == 0) expand = 1;
        return new double[]{minX - expand, minY - expand, maxX + expand, maxY + expand};
    }

    private static final class Builder<P extends Position> {
        private final DelaunayTriangulation<P> t;
        private final double x0, y0, x1, y1;
        private final double[] xs, ys;

        Builder(DelaunayTriangulation<P> t, double[] box) {
            this.t = t;
            this.x0 = box[0];
            this.y0 = box[1];
            this.x1 = box[2];
            this.y1 = box[3];
            this.xs = t.xs;
            this.ys = t.ys;
        }

        List<Polygon<P>> cells() {
            int n = xs.length;
            List<Polygon<P>> cells = new ArrayList<>(n);
            if (t.getNumTriangles() > 0) {
                buildFromTriangles(cells);
            } else if (n == 1) {
                cells.add(polygon(rectangle()));
            } else if (n > 1) {
                buildCollinear(cells);
            }
            return cells;
        }

        // ---------------------------------------------------------------------------------------------------
        // general case
        // ---------------------------------------------------------------------------------------------------

        private double[] cx, cy;   // circumcenters
        private double bigMinX, bigMinY, bigMaxX, bigMaxY;   // a box containing the clip box and the circumcenters

        private void buildFromTriangles(List<Polygon<P>> cells) {
            int[] tri = t.triangles, nb = t.neighbors;
            int m = tri.length / 3;
            cx = new double[m];
            cy = new double[m];
            double cxMin = x0, cxMax = x1, cyMin = y0, cyMax = y1;
            for (int i = 0; i < m; i++) {
                double[] c = Constructions.circumcenter(xs[tri[3 * i]], ys[tri[3 * i]], xs[tri[3 * i + 1]],
                        ys[tri[3 * i + 1]], xs[tri[3 * i + 2]], ys[tri[3 * i + 2]]);
                cx[i] = c[0];
                cy[i] = c[1];
                cxMin = Math.min(cxMin, c[0]);
                cxMax = Math.max(cxMax, c[0]);
                cyMin = Math.min(cyMin, c[1]);
                cyMax = Math.max(cyMax, c[1]);
            }
            // rays to infinity are cut off at this box; its sides are far enough away that the cut is outside the
            // clip box. Each cell vertex is inside it.
            double margin = Math.max(cxMax - cxMin, cyMax - cyMin) + 1;
            bigMinX = cxMin - margin;
            bigMinY = cyMin - margin;
            bigMaxX = cxMax + margin;
            bigMaxY = cyMax + margin;

            // a triangle incident to each vertex, and the index of the vertex in it
            int n = xs.length;
            int[] incident = new int[n];
            for (int i = 0; i < 3 * m; i++) incident[tri[i]] = i;
            for (int v = 0; v < n; v++) {
                int corner = incident[v];
                int start = corner / 3;
                // turn clockwise until the hull, or back at the start
                int first = start, k = corner % 3;
                boolean onHull = false;
                while (true) {
                    int previous = nb[3 * first + (k + 2) % 3];   // across edge (v, a), clockwise
                    if (previous < 0) {
                        onHull = true;
                        break;
                    }
                    k = indexOf(tri, previous, v);
                    first = previous;
                    if (first == start) break;
                }
                // walk counterclockwise, collecting circumcenters
                Ring ring = new Ring();
                if (onHull) {
                    int a = tri[3 * first + (k + 1) % 3];
                    double[] exit = rayExit(first, v, a);
                    ring.add(exit[0], exit[1]);
                }
                int current = first;
                while (true) {
                    ring.add(cx[current], cy[current]);
                    int next = nb[3 * current + (k + 1) % 3];   // across edge (v, b), counterclockwise
                    if (next < 0) {
                        int b = tri[3 * current + (k + 2) % 3];
                        double[] exit = rayExit(current, b, v);
                        ring.add(exit[0], exit[1]);
                        ring.closeAlongBigBox(this);
                        break;
                    }
                    k = indexOf(tri, next, v);
                    current = next;
                    if (current == first) break;
                }
                cells.add(polygon(clip(ring)));
            }
        }

        // where the ray from the circumcenter of triangle tr, perpendicular to the hull edge from -> to (outward,
        // i.e. to the right), leaves the big box. The edge is given in the direction in which it occurs in tr, so
        // both cells sharing the ray compute it identically.
        private double[] rayExit(int tr, int from, int to) {
            double dx = ys[to] - ys[from], dy = -(xs[to] - xs[from]);
            double sx = cx[tr], sy = cy[tr];
            double tx = dx > 0 ? (bigMaxX - sx) / dx : dx < 0 ? (bigMinX - sx) / dx : Double.POSITIVE_INFINITY;
            double ty = dy > 0 ? (bigMaxY - sy) / dy : dy < 0 ? (bigMinY - sy) / dy : Double.POSITIVE_INFINITY;
            if (tx <= ty) {
                return new double[]{dx > 0 ? bigMaxX : bigMinX, clamp(sy + tx * dy, bigMinY, bigMaxY)};
            }
            return new double[]{clamp(sx + ty * dx, bigMinX, bigMaxX), dy > 0 ? bigMaxY : bigMinY};
        }

        private static double clamp(double v, double min, double max) {
            return Math.max(min, Math.min(max, v));
        }

        private static int indexOf(int[] tri, int t, int v) {
            return tri[3 * t] == v ? 0 : tri[3 * t + 1] == v ? 1 : 2;
        }

        // ---------------------------------------------------------------------------------------------------
        // collinear sites: the cells are strips between parallel bisectors
        // ---------------------------------------------------------------------------------------------------

        private void buildCollinear(List<Polygon<P>> cells) {
            int n = xs.length;
            Integer[] order = new Integer[n];
            for (int i = 0; i < n; i++) order[i] = i;
            java.util.Arrays.sort(order, (a, b) -> {
                int c = Double.compare(xs[a] + 0.0, xs[b] + 0.0);
                return c != 0 ? c : Double.compare(ys[a] + 0.0, ys[b] + 0.0);
            });
            List<Polygon<P>> result = new ArrayList<>(Collections.nCopies(n, null));
            for (int i = 0; i < n; i++) {
                Ring ring = rectangle();
                if (i > 0) ring = clipByBisector(ring, order[i - 1], order[i]);
                if (i + 1 < n) ring = clipByBisector(ring, order[i + 1], order[i]);
                result.set(order[i], polygon(ring));
            }
            cells.addAll(result);
        }

        // the part of the ring on the side of site keep of the bisector of other and keep. The ring consists of
        // parts of the clip box sides (the bisectors are parallel, so they do not cross), and the crossing points
        // are computed from the bisector and the box side only, so adjacent cells get the same points.
        private Ring clipByBisector(Ring ring, int other, int keep) {
            double mx = 0.5 * xs[other] + 0.5 * xs[keep], my = 0.5 * ys[other] + 0.5 * ys[keep];
            double dx = xs[keep] - xs[other], dy = ys[keep] - ys[other];
            Ring out = new Ring();
            for (int i = 0; i < ring.size; i++) {
                double sx = ring.x[i], sy = ring.y[i];
                double ex = ring.x[(i + 1) % ring.size], ey = ring.y[(i + 1) % ring.size];
                boolean sIn = (sx - mx) * dx + (sy - my) * dy >= 0;
                boolean eIn = (ex - mx) * dx + (ey - my) * dy >= 0;
                if (sIn) out.add(sx, sy);
                if (sIn != eIn) {
                    if (sx == ex) {
                        // on a vertical side: the bisector at x = sx
                        out.add(sx, clamp(my - dx * (sx - mx) / dy, Math.min(sy, ey), Math.max(sy, ey)));
                    } else {
                        out.add(clamp(mx - dy * (sy - my) / dx, Math.min(sx, ex), Math.max(sx, ex)), sy);
                    }
                }
            }
            return out;
        }

        // ---------------------------------------------------------------------------------------------------
        // clipping
        // ---------------------------------------------------------------------------------------------------

        private Ring rectangle() {
            Ring r = new Ring();
            r.add(x0, y0);
            r.add(x1, y0);
            r.add(x1, y1);
            r.add(x0, y1);
            return r;
        }

        // Sutherland-Hodgman clipping by the four sides of the clip box
        private Ring clip(Ring ring) {
            ring = clipSide(ring, 0, x0, true);
            ring = clipSide(ring, 0, x1, false);
            ring = clipSide(ring, 1, y0, true);
            return clipSide(ring, 1, y1, false);
        }

        // keeps the part with coordinate[axis] >= bound (lower) or <= bound (upper)
        private static Ring clipSide(Ring ring, int axis, double bound, boolean lower) {
            Ring out = new Ring();
            for (int i = 0; i < ring.size; i++) {
                double sx = ring.x[i], sy = ring.y[i];
                double ex = ring.x[(i + 1) % ring.size], ey = ring.y[(i + 1) % ring.size];
                double s = axis == 0 ? sx : sy, e = axis == 0 ? ex : ey;
                boolean sIn = lower ? s >= bound : s <= bound;
                boolean eIn = lower ? e >= bound : e <= bound;
                if (sIn) out.add(sx, sy);
                if (sIn != eIn) {
                    // canonical order of the endpoints, so that adjacent cells compute the same point
                    boolean swap = sx > ex || (sx == ex && sy > ey);
                    double px = swap ? ex : sx, py = swap ? ey : sy, qx = swap ? sx : ex, qy = swap ? sy : ey;
                    if (axis == 0) {
                        double y = py + (bound - px) * ((qy - py) / (qx - px));
                        out.add(bound, clamp(y, Math.min(py, qy), Math.max(py, qy)));
                    } else {
                        double x = px + (bound - py) * ((qx - px) / (qy - py));
                        out.add(clamp(x, Math.min(px, qx), Math.max(px, qx)), bound);
                    }
                }
            }
            return out;
        }

        private Polygon<P> polygon(Ring ring) {
            ring.removeDuplicates();
            CoordinateReferenceSystem<P> crs = t.getCoordinateReferenceSystem();
            if (ring.size < 3) return new Polygon<>(crs);
            int dim = crs.getCoordinateDimension();
            PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(ring.size + 1, crs.getPositionClass());
            for (int i = 0; i <= ring.size; i++) {
                double[] c = new double[dim];
                java.util.Arrays.fill(c, Double.NaN);
                c[0] = ring.x[i % ring.size];
                c[1] = ring.y[i % ring.size];
                builder.add(Positions.mkPosition(crs, c));
            }
            return new Polygon<>(new LinearRing<>(builder.toPositionSequence(), crs));
        }
    }

    // a growable list of (x, y); the ring is implicitly closed
    private static final class Ring {
        double[] x = new double[8], y = new double[8];
        int size;

        void add(double px, double py) {
            if (size == x.length) {
                x = java.util.Arrays.copyOf(x, 2 * size);
                y = java.util.Arrays.copyOf(y, 2 * size);
            }
            x[size] = px;
            y[size] = py;
            size++;
        }

        // removes consecutive duplicates (also between the last and the first point)
        void removeDuplicates() {
            int m = 0;
            for (int i = 0; i < size; i++) {
                if (m > 0 && x[m - 1] == x[i] && y[m - 1] == y[i]) continue;
                x[m] = x[i];
                y[m] = y[i];
                m++;
            }
            while (m > 1 && x[m - 1] == x[0] && y[m - 1] == y[0]) m--;
            size = m;
        }

        // closes a ring that ends at an exit point on the big box and starts at another exit point, by adding the
        // corners of the big box counterclockwise from the last point to the first
        void closeAlongBigBox(Builder<?> b) {
            double sEnd = perimeter(b, x[size - 1], y[size - 1]), sStart = perimeter(b, x[0], y[0]);
            double w = b.bigMaxX - b.bigMinX, h = b.bigMaxY - b.bigMinY;
            double[][] corners = {{b.bigMinX, b.bigMinY, 0}, {b.bigMaxX, b.bigMinY, w}, {b.bigMaxX, b.bigMaxY, w + h},
                    {b.bigMinX, b.bigMaxY, 2 * w + h}};
            double total = 2 * (w + h);
            double distance = sStart - sEnd;
            if (distance <= 0) distance += total;
            for (int i = 0; i < 4; i++) {
                double[] c = corners[(firstCorner(sEnd, corners) + i) % 4];
                double d = c[2] - sEnd;
                if (d <= 0) d += total;
                if (d < distance) add(c[0], c[1]);
            }
        }

        private static int firstCorner(double s, double[][] corners) {
            for (int i = 0; i < 4; i++) {
                if (corners[i][2] > s) return i;
            }
            return 0;
        }

        // the position along the boundary of the big box, counterclockwise from its lower left corner
        private static double perimeter(Builder<?> b, double px, double py) {
            double w = b.bigMaxX - b.bigMinX, h = b.bigMaxY - b.bigMinY;
            if (py == b.bigMinY && px < b.bigMaxX) return px - b.bigMinX;
            if (px == b.bigMaxX && py < b.bigMaxY) return w + (py - b.bigMinY);
            if (py == b.bigMaxY && px > b.bigMinX) return w + h + (b.bigMaxX - px);
            return 2 * w + h + (b.bigMaxY - py);
        }
    }
}
