package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Predicates;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The buffer of a geometry: the points within a distance of it, approximated by a polygon.
 *
 * <p>For a positive distance d the buffer contains the points within distance d of the geometry; for a negative
 * distance it contains the points of the polygons of the geometry that are farther than |d| from their boundary
 * (lines and points have no interior, so they do not contribute). A distance of 0 gives the union of the polygons.
 * The style of the ends of lines and of the outer corners is set with {@link BufferParameters}.</p>
 *
 * <p>Circular arcs are approximated as in JTS: the arc around a vertex is divided into
 * {@code round(angle / quantum)} equal parts, with a quantum of a quarter circle divided by the number of segments
 * per quadrant, and its vertices lie on the circle. So with round joins and end caps the result lies inside the
 * exact buffer, by at most d (1 - cos(3&pi; / 8q)) for q segments per quadrant (0.011 d for the default of 8).</p>
 *
 * <p>With round joins, the input is first simplified with a tolerance h, the {@linkplain
 * BufferParameters#getSimplifyFactor() simplify factor} times |d| (by default 0.01 |d|, as in JTS). That adds at most
 * 2h to the error above, and the result still lies inside the exact buffer; see below. On input with many vertices
 * compared to the distance, it saves most of the work.</p>
 *
 * <p>The buffer is computed from raw offset curves ({@link OffsetCurve}): closed curves whose winding number is
 * positive exactly in the buffer. They are noded, snap rounded to a {@link Grid} and labelled with the winding
 * numbers by the overlay graph of {@link Overlay}, which takes the faces with a positive winding number. All
 * decisions are exact. Consequently the result is valid, and it differs from the region of positive winding number
 * only by snap rounding: on the double grid by a unit in the last place. On a fixed grid, parts thinner than a
 * cell collapse.</p>
 *
 * <h2>Why the winding number gives the buffer</h2>
 * <p>Take a closed path, and offset it by d &gt; 0 to its right. The points within distance d to the right of the
 * path are covered by these pieces, each a polygon with a winding number of 1 inside and 0 outside:</p>
 * <ul>
 *     <li>for each segment from a to b, the quadrilateral (a, a + d m, b + d m, b), with m the unit normal to its
 *     right;</li>
 *     <li>at each vertex where the path turns to the left (and at a reversal), a wedge between the two offset
 *     endpoints: the vertex plus the join (an arc, a mitre or a bevel), or at the end of a line the cap.</li>
 * </ul>
 * <p>The winding number of the sum of the boundaries of all pieces is the number of pieces that contain a point.
 * In that sum, the sides that a quadrilateral and a wedge share cancel, and the segments of the path add up to the
 * reversed path. What remains, added to the path, is the offset curve: the offset segments joined by the joins,
 * and where the path turns right, through the vertex (from the end of one offset segment to the vertex, and on to
 * the start of the next one). So the winding number of the offset curve is the winding number of the path plus the
 * number of pieces that contain the point:</p>
 * <ul>
 *     <li>a polygon ring, oriented with the interior to its left: winding number 1 in the polygon, so the
 *     positive winding number of the curves of the rings is the polygon plus the pieces, its buffer;</li>
 *     <li>for a negative distance, the reversed ring is offset to its right, into the polygon, and the curve is
 *     counted negatively: 1 in the polygon minus the number of pieces. It is positive in the polygon outside the
 *     pieces;</li>
 *     <li>an open line is the closed path that goes along the line and back, with caps at its two ends; a closed
 *     line gives the offset curves of the ring in both directions.</li>
 * </ul>
 * <p>Where the path turns right and the two offset segments cross, the curve takes a shortcut through the crossing
 * point instead of through the vertex, as in JTS. That removes a cycle around the region between the crossing
 * point, the offset endpoints and the vertex. The shortcut is taken only if each offset endpoint lies in the
 * quadrilateral of the other segment (decided exactly), so that region lies in both quadrilaterals, and not at every
 * vertex of a path: then a point lies in fewer of the removed regions than in quadrilaterals, and the number of
 * pieces that contain it stays positive. (Without these conditions, a negative buffer of a convex polygon that
 * removes it completely could leave an inverted copy.)</p>
 * <h2>Spikes</h2>
 * <p>Where the path turns right and the offset segments do not cross, the curve passes through the vertex: a spike
 * of length d. Where the path curves more tightly than d, for example along a dense, noisy or tightly curved line,
 * every vertex has one, and each spike crosses the spikes and offset segments of the vertices around it: noding the
 * curve as a whole would cost time quadratic in d over the vertex spacing. So the spikes are handled locally. The
 * winding number of the offset curve is a sum: of the path, and of closed curves around chunks of consecutive
 * vertices, each from the first vertex of the chunk along the offset curve to its last vertex and back along the
 * path. The winding number of a chunk curve is the number of pieces of the chunk that contain the point (the
 * shortcut argument above holds within the chunk, which takes no shortcut at its ends), so it is positive exactly
 * on the union of those pieces. Around each group of nearby spikes, the chunks (of two spikes each) are united
 * separately, the unions merged pairwise, and the union of the group replaces its chunks in the sum: the offset
 * curve follows the path through the group instead, and the union of the group counts once, with the sign of the
 * curve. Since all terms are non-negative, the sign of the total is unchanged. JTS avoids the spikes with short
 * closing segments near the vertex, which is fast but can leave out parts of the buffer.</p>
 * <p>The argument holds for the pieces formed by the rounded constructed vertices just as well, so rounding the
 * offset points only perturbs the pieces.</p>
 *
 * <h2>Simplification</h2>
 * <p>Each ring and line is simplified with the exact Douglas-Peucker algorithm ({@link DouglasPeucker}): every vertex
 * it drops lies within h of the chord between the kept vertices around it, so the part of the path that the chord
 * replaces lies within h of the chord, and each point of the chord lies within h of that part (the part runs from
 * one end of the chord to the other). The pieces are then built on the simplified path, with arcs of radius d
 * around its vertices, which are vertices of the input. The quadrilateral of a chord has depth d if no dropped vertex
 * lies on the side away from the buffer: a point of the quadrilateral is then within d of the input, because the
 * dropped part crosses the normal through it between the chord and the point, or within h of the point. Otherwise the
 * quadrilateral has depth d - h, and its points are within d of the input as well. So all pieces lie within the
 * exact buffer. Conversely, a point within d cos(3&pi; / 8q) - 2h of the input lies within d cos(3&pi; / 8q) - h
 * of the simplified path, and the pieces contain it. The simplified polygons must be valid, with their rings
 * oriented as before (checked exactly; otherwise they are used as they are), so the winding number argument holds
 * for them; and a point that simplification cuts off a polygon lies within 2h of the simplified boundary, which
 * the pieces cover as long as 2h &le; (d - h) cos(3&pi; / 8q): hence the limit on the simplify factor. With mitre or
 * bevel joins the corners would change shape, so the input is not simplified; with flat or square end caps, the
 * first and last segments of a line are kept.</p>
 *
 * <p>The result is a {@code Polygon} or a {@code MultiPolygon}, possibly empty, in the coordinate reference system
 * of the geometry. Z and M coordinates are not kept. The polygons of the geometry must be valid; for a negative
 * distance, the polygons of a {@code GeometryCollection} may overlap: they are united first.</p>
 */
public final class Buffer {

    private Buffer() {
    }

    /**
     * @return the buffer with the default parameters (round end caps and joins, 8 segments per quadrant), snap
     * rounded to the double grid
     * @throws IllegalArgumentException if the distance or a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> buffer(Geometry<P> geometry, double distance) {
        return buffer(geometry, distance, BufferParameters.DEFAULT, Grid.DOUBLE);
    }

    /**
     * @return the buffer, snap rounded to the double grid
     * @throws IllegalArgumentException if the distance or a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> buffer(Geometry<P> geometry, double distance,
                                                          BufferParameters parameters) {
        return buffer(geometry, distance, parameters, Grid.DOUBLE);
    }

    /**
     * @return the buffer, snap rounded to the grid
     * @throws IllegalArgumentException if the distance or a coordinate is NaN or infinite, or a coordinate is too
     *                                  large for the grid
     */
    public static <P extends Position> Geometry<P> buffer(Geometry<P> geometry, double distance,
                                                          BufferParameters parameters, Grid grid) {
        Objects.requireNonNull(geometry);
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(grid);
        if (!Double.isFinite(distance)) {
            throw new IllegalArgumentException("The buffer distance must be finite: " + distance);
        }
        CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
        GeometryElements elements = GeometryElements.of(geometry);
        if (distance < 0 && elements.polygonCount > 1
                && !(geometry instanceof Polygon || geometry instanceof MultiPolygon)) {
            // the polygons of a collection may overlap
            elements = GeometryElements.of(Overlay.unaryUnion(geometry));
        }
        List<OverlayGraph.Input> inputs = curves(elements, distance, parameters);
        if (inputs.isEmpty()) return Overlay.empty(2, crs);
        if (grid.isFixed()) {
            double maxAbs = 0;
            for (OverlayGraph.Input in : inputs) {
                for (int i = 0; i < in.xs.length; i++) {
                    maxAbs = Math.max(maxAbs, Math.max(Math.abs(in.xs[i]), Math.abs(in.ys[i])));
                }
            }
            grid.checkRange(maxAbs);
        }
        OverlayGraph graph = new OverlayGraph(inputs, grid);
        graph.label();
        graph.select(Overlay.Operation.UNION);
        OverlayResult result = OverlayResult.of(graph, Overlay.Operation.UNION);
        return Overlay.build(result, new ArrayList<>(), 2, crs);
    }

    // the closed curves, as rings of geometry A, whose winding number is positive in the buffer
    private static List<OverlayGraph.Input> curves(GeometryElements elements, double distance,
                                                   BufferParameters parameters) {
        List<OverlayGraph.Input> inputs = new ArrayList<>();
        if (distance == 0) {
            for (GeometryElements.Ring r : elements.rings) add(inputs, new double[][]{r.xs, r.ys}, r.delta);
            return inputs;
        }
        double d = Math.abs(distance);
        OffsetCurve offset = new OffsetCurve(d, parameters);
        int sign = distance > 0 ? 1 : -1;
        // the simplification tolerance, and the offset distance of a chord that cut off vertices on the side away from
        // the buffer (rounded down, so that it is at most d - h)
        double h = parameters.getJoin() == BufferParameters.Join.ROUND ? parameters.getSimplifyFactor() * d : 0;
        double reduced = Math.nextDown(d - h);
        Simplifier simplifier = h > 0 ? new Simplifier(h) : null;
        Simplified[] rings = simplifyRings(elements, simplifier, distance);
        for (int j = 0; j < rings.length; j++) {
            GeometryElements.Ring r = elements.rings.get(j);
            // the ring, without its closing vertex, with the interior to its left
            int k = r.xs.length - 1;
            boolean reverse = (r.delta < 0) == (distance > 0);
            closedPath(offset, r.xs, r.ys, k, reverse, rings[j], d, reduced);
            add(inputs, offset, sign);
        }
        if (distance < 0) return inputs;
        for (double[][] line : elements.lines) {
            double[] xs = line[0], ys = line[1];
            int k = xs.length - 1;
            if (k >= 3 && xs[0] == xs[k] && ys[0] == ys[k]) {
                // a closed line: both sides of the ring
                Simplified simplified = simplifier == null ? null : simplifier.simplify(xs, ys, true, false);
                for (boolean reverse : new boolean[]{false, true}) {
                    closedPath(offset, xs, ys, k, reverse, simplified, d, reduced);
                    add(inputs, offset, 1);
                }
            } else {
                boolean keepEnds = parameters.getEndCap() != BufferParameters.EndCap.ROUND;
                Simplified simplified = simplifier == null ? null : simplifier.simplify(xs, ys, false, keepEnds);
                if (simplified == null) {
                    offset.line(xs, ys);
                } else {
                    int[] kept = simplified.kept;
                    int m = kept.length - 1;
                    double[] depth = new double[2 * m];
                    for (int t = 0; t < m; t++) {
                        depth[t] = simplified.left[t] ? reduced : d;
                        // segment m + t runs back along chord m - 1 - t
                        depth[m + t] = simplified.right[m - 1 - t] ? reduced : d;
                    }
                    offset.line(select(xs, kept), select(ys, kept), depth);
                }
                add(inputs, offset, 1);
            }
        }
        for (double[] p : elements.points) add(inputs, offset.point(p[0], p[1]), 1);
        return inputs;
    }

    /**
     * Sets the path of a closed ring (k vertices, the closing one repeated at index k), in order or reversed, with the
     * buffer to its right; simplified if given
     */
    private static void closedPath(OffsetCurve offset, double[] xs, double[] ys, int k, boolean reverse,
                                   Simplified simplified, double d, double reduced) {
        if (simplified == null) {
            double[][] path = path(xs, ys, k, reverse);
            offset.path(path[0], path[1], k, -1, -1);
            return;
        }
        int[] kept = simplified.kept;
        int n = kept.length;
        double[] px = new double[n], py = new double[n], depth = new double[n];
        for (int i = 0; i < n; i++) {
            int v = kept[reverse ? n - 1 - i : i];
            px[i] = xs[v];
            py[i] = ys[v];
            // segment i is chord i, or chord n - 2 - i backwards; the buffer is on the right of the path, so the
            // offset distance is reduced if the chord cut off vertices on the left of the path
            depth[i] = reverse ? (simplified.right[Math.floorMod(n - 2 - i, n)] ? reduced : d)
                    : (simplified.left[i] ? reduced : d);
        }
        offset.path(px, py, n, -1, -1, depth);
    }

    /**
     * The simplified rings, or null for a ring that is used as it is. The simplified polygons must be valid, with
     * their rings oriented as before: per polygon for a positive distance, all together for a negative one (the
     * polygons must not overlap then). Where that fails, the polygons are used as they are.
     */
    private static Simplified[] simplifyRings(GeometryElements elements, Simplifier simplifier, double distance) {
        int n = elements.rings.size();
        Simplified[] simplified = new Simplified[n];
        if (simplifier == null || n == 0) return simplified;
        boolean any = false;
        for (int j = 0; j < n; j++) {
            GeometryElements.Ring r = elements.rings.get(j);
            simplified[j] = simplifier.simplify(r.xs, r.ys, true, false);
            if (simplified[j] != null) {
                int k = r.xs.length - 1;
                double[] sx = select(r.xs, simplified[j].kept), sy = select(r.ys, simplified[j].kept);
                if (Predicates.ringOrientation(sx, sy, sx.length) != Predicates.ringOrientation(r.xs, r.ys, k)) {
                    simplified[j] = null;
                }
            }
            any |= simplified[j] != null;
        }
        if (!any) return simplified;
        // the polygons, as lists of ring indices (shell first)
        List<List<Integer>> polygons = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            int id = elements.rings.get(j).polygon;
            while (polygons.size() <= id) polygons.add(new ArrayList<>());
            polygons.get(id).add(j);
        }
        List<Polygon<C2D>> all = new ArrayList<>();
        for (List<Integer> polygon : polygons) {
            if (polygon.isEmpty()) continue;
            boolean changed = false;
            for (int j : polygon) changed |= simplified[j] != null;
            Polygon<C2D> p = polygon(elements, polygon, simplified);
            if (distance > 0 && changed && !Validity.isValid(p)) {
                for (int j : polygon) simplified[j] = null;
            }
            all.add(p);
        }
        if (distance < 0 && !Validity.isValid(all.size() == 1 ? all.get(0) : Geometries.mkMultiPolygon(all))) {
            Arrays.fill(simplified, null);
        }
        return simplified;
    }

    private static Polygon<C2D> polygon(GeometryElements elements, List<Integer> rings, Simplified[] simplified) {
        List<LinearRing<C2D>> linearRings = new ArrayList<>(rings.size());
        for (int j : rings) {
            GeometryElements.Ring r = elements.rings.get(j);
            int[] kept = simplified[j] == null ? null : simplified[j].kept;
            int n = kept == null ? r.xs.length - 1 : kept.length;
            PositionSequenceBuilder<C2D> b = PositionSequenceBuilders.fixedSized(n + 1, C2D.class);
            for (int i = 0; i <= n; i++) {
                int v = kept == null ? i : kept[i % n];
                b.add(r.xs[v], r.ys[v]);
            }
            linearRings.add(new LinearRing<>(b.toPositionSequence(), SIMPLIFIED_CRS));
        }
        return Geometries.mkPolygon(linearRings);
    }

    private static final CoordinateReferenceSystem<C2D> SIMPLIFIED_CRS = CoordinateReferenceSystems.PROJECTED_2D_METER;

    private static double[] select(double[] values, int[] indices) {
        double[] result = new double[indices.length];
        for (int i = 0; i < indices.length; i++) result[i] = values[indices[i]];
        return result;
    }

    /**
     * The vertices of a ring or line that simplification keeps, and per chord (the segment from a kept vertex to the
     * next one, for a ring the last one back to the first) whether it cut off a vertex strictly to its left, and to
     * its right
     */
    private static final class Simplified {
        final int[] kept;
        final boolean[] left, right;

        Simplified(int[] kept, boolean[] left, boolean[] right) {
            this.kept = kept;
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Exact Douglas-Peucker simplification with tolerance h: every vertex that is dropped lies within distance h of
     * the chord that replaces it (decided exactly), so the segments it replaces do too
     */
    private static final class Simplifier {
        private final double h;
        private double[] lo = new double[0], hi = new double[0];

        Simplifier(double h) {
            this.h = h;
        }

        /**
         * @param xs       the X coordinates; consecutive positions differ; for a closed ring the first is repeated at
         *                 the end
         * @param keepEnds whether to keep the first and last segment of a line
         * @return the simplification, or null if it drops no vertex, or the ring or line collapses
         */
        Simplified simplify(double[] xs, double[] ys, boolean closed, boolean keepEnds) {
            int last = xs.length - 1;
            if (lo.length < xs.length) {
                lo = new double[xs.length];
                hi = new double[xs.length];
            }
            boolean[] keep = new boolean[xs.length];
            keep[0] = keep[last] = true;
            if (keepEnds && last >= 2) {
                keep[1] = keep[last - 1] = true;
                DouglasPeucker.simplify(xs, ys, 1, last - 1, h, keep, lo, hi);
            } else {
                DouglasPeucker.simplify(xs, ys, 0, last, h, keep, lo, hi);
            }
            // the kept vertices (for a ring without the closing one), without repeated positions: a vertex dropped
            // between two kept vertices at the same position lies within h of that position
            int end = closed ? last - 1 : last;
            int[] kept = new int[end + 1];
            int count = 0;
            for (int i = 0; i <= end; i++) {
                if (!keep[i]) continue;
                if (count > 0 && xs[i] == xs[kept[count - 1]] && ys[i] == ys[kept[count - 1]]) continue;
                kept[count++] = i;
            }
            if (closed) {
                while (count > 1 && xs[kept[count - 1]] == xs[0] && ys[kept[count - 1]] == ys[0]) count--;
                if (count < 3) return null;
            } else {
                if (kept[count - 1] != last) {
                    // the last vertex repeats the position of the kept one before it
                    kept[count - 1] = last;
                }
                if (count < 2) return null;
            }
            if (count == end + 1) return null;
            kept = Arrays.copyOf(kept, count);
            int chords = closed ? count : count - 1;
            boolean[] left = new boolean[chords], right = new boolean[chords];
            for (int t = 0; t < chords; t++) {
                int u = kept[t], w = t + 1 < count ? kept[t + 1] : last;
                for (int v = u + 1; v < w; v++) {
                    int o = Predicates.orientation(xs[u], ys[u], xs[w], ys[w], xs[v], ys[v]);
                    if (o > 0) left[t] = true;
                    if (o < 0) right[t] = true;
                }
            }
            return new Simplified(kept, left, right);
        }
    }

    // the number of spikes per chunk, and the largest number of vertices between two spikes of a group (at least 2)
    private static final int MAX_SPIKES = 2, GAP = 8;

    /**
     * Adds curves whose winding number, times the sign, is positive exactly where the winding number of the path
     * plus the number of pieces of its buffer that contain the point is: the offset curve, or for a path with
     * spikes, that curve with the groups of spikes cut out (see {@link OffsetCurve#local}) plus, per group, the union
     * of its chunks. The chunks of a group are united pairwise, level by level, so that each union involves few
     * spikes and overlapping chunks.
     */
    private static void add(List<OverlayGraph.Input> inputs, OffsetCurve offset, int sign) {
        if (offset.spikes() == 0) {
            add(inputs, offset.curve(), sign);
            return;
        }
        List<List<double[][]>> groups = new ArrayList<>();
        add(inputs, offset.local(MAX_SPIKES, GAP, groups), sign);
        for (List<double[][]> chunks : groups) {
            List<List<double[][]>> unions = new ArrayList<>(chunks.size());
            for (double[][] chunk : chunks) unions.add(union(Collections.singletonList(chunk)));
            while (unions.size() > 1) {
                List<List<double[][]>> merged = new ArrayList<>((unions.size() + 1) / 2);
                for (int i = 0; i < unions.size(); i += 2) {
                    if (i + 1 == unions.size()) {
                        merged.add(unions.get(i));
                    } else {
                        List<double[][]> both = new ArrayList<>(unions.get(i));
                        both.addAll(unions.get(i + 1));
                        merged.add(union(both));
                    }
                }
                unions = merged;
            }
            for (double[][] ring : unions.get(0)) add(inputs, ring, sign);
        }
    }

    /**
     * The union of the regions where the winding number of the curves is positive, snap rounded to the double grid,
     * as rings whose winding number is 1 in the union and 0 outside it: shells counterclockwise, holes clockwise
     */
    private static List<double[][]> union(List<double[][]> curves) {
        List<OverlayGraph.Input> inputs = new ArrayList<>(curves.size());
        for (double[][] c : curves) add(inputs, c, 1);
        OverlayGraph graph = new OverlayGraph(inputs, Grid.DOUBLE);
        graph.label();
        graph.select(Overlay.Operation.UNION);
        OverlayResult result = OverlayResult.of(graph, Overlay.Operation.UNION);
        List<double[][]> rings = new ArrayList<>();
        for (List<List<OverlayGraph.Node>> polygon : result.polygons) {
            for (List<OverlayGraph.Node> ring : polygon) {
                int n = ring.size();
                double[] xs = new double[n + 1], ys = new double[n + 1];
                for (int i = 0; i < n; i++) {
                    xs[i] = ring.get(i).x;
                    ys[i] = ring.get(i).y;
                }
                xs[n] = xs[0];
                ys[n] = ys[0];
                rings.add(new double[][]{xs, ys});
            }
        }
        return rings;
    }

    // the first k vertices, in order or reversed
    private static double[][] path(double[] xs, double[] ys, int k, boolean reverse) {
        double[] px = new double[k], py = new double[k];
        for (int i = 0; i < k; i++) {
            int j = reverse ? k - 1 - i : i;
            px[i] = xs[j];
            py[i] = ys[j];
        }
        return new double[][]{px, py};
    }

    private static void add(List<OverlayGraph.Input> inputs, double[][] curve, int delta) {
        if (curve != null) inputs.add(new OverlayGraph.Input(OverlayGraph.A, true, delta, curve));
    }
}
