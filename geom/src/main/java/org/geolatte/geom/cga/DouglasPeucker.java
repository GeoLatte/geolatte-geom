package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Interval;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.ArrayList;
import java.util.List;

/**
 * Exact Ramer-Douglas-Peucker line simplification, in the plane of the first two coordinates.
 *
 * <p>This is the prototype algorithm of Ratschek &amp; Rokne, <em>Geometric Computations with Interval and New
 * Robust Methods</em> (Sec. 8.4, Algorithm 24), which fixes the details that are left open in other descriptions,
 * so that the result is uniquely defined:</p>
 * <ol>
 *     <li>the first and last positions are kept. The positions in between are replaced by the segment between
 *     them (the base line) if their distances to the base line are all less than or equal to the tolerance;</li>
 *     <li>otherwise the positions are divided at the position with the largest distance to the base line, and
 *     both parts are simplified in the same way. If several positions have the largest distance, the first one
 *     is chosen.</li>
 * </ol>
 * <p>Distances are measured to the base line <em>segment</em> (not to the infinite line through its endpoints).</p>
 *
 * <p>The decisions of the algorithm only depend on comparisons of distances. These comparisons are made exactly
 * (see {@link Predicates#compareDistanceToSegment(double, double, double, double, double, double, double, double)}
 * and {@link Predicates#compareDistanceToSegment(double, double, double, double, double, double, double)}), so
 * the result is the exact result of the algorithm for the given coordinates: it is reproducible, and not
 * affected by rounding errors. As proposed in the book (Sec. 8.4.2), distances are first enclosed in intervals
 * (with a floating point filter, see {@link Predicates#squaredDistancesToSegment(double, double, double, double,
 * double[], double[], int, int, double[], double[])}), and the exact comparisons are only needed when intervals
 * overlap.</p>
 *
 * <p>The simplified result consists of original positions (including their Z and M values); no coordinates are
 * computed. Note that simplification may make a line self-intersecting, and a polygon invalid; see
 * {@link #simplify(Geometry, double, boolean)} for simplification that preserves the topology.</p>
 */
public final class DouglasPeucker {

    private DouglasPeucker() {
    }

    /**
     * Determines which positions are kept when the positions are simplified with the specified tolerance.
     *
     * @param positions the positions of a line
     * @param tolerance the distance tolerance (finite, non-negative)
     * @return for each position whether it is kept
     * @throws IllegalArgumentException if the tolerance is negative, NaN or infinite, or an X or Y coordinate is
     *                                  NaN or infinite
     */
    public static boolean[] keptPositions(PositionSequence<?> positions, double tolerance) {
        checkTolerance(tolerance);
        int n = positions.size();
        double[] xs = new double[n], ys = new double[n];
        coordinates(positions, xs, ys);
        boolean[] keep = new boolean[n];
        if (n == 0) return keep;
        keep[0] = keep[n - 1] = true;

        Interval tolerance2 = Interval.product(tolerance, tolerance);
        double[] lo = new double[n], hi = new double[n];
        // stack of the index ranges [first, last] that remain to be simplified; recursion would overflow the call
        // stack for long lines. The parts are independent, so the order in which they are processed is irrelevant.
        int[] stack = new int[2 * n];
        int top = 0;
        stack[top++] = 0;
        stack[top++] = n - 1;
        while (top > 0) {
            int last = stack[--top];
            int first = stack[--top];
            if (last - first < 2) continue;
            int farthest = farthestPosition(xs, ys, first, last, lo, hi);
            if (!isWithinTolerance(xs, ys, first, last, farthest, lo, hi, tolerance, tolerance2)) {
                keep[farthest] = true;
                stack[top++] = first;
                stack[top++] = farthest;
                stack[top++] = farthest;
                stack[top++] = last;
            }
        }
        return keep;
    }

    /**
     * Returns the first of the positions first + 1, ..., last - 1 with the largest distance to the segment from
     * position first to position last (last - first must be at least 2). On return, lo and hi contain enclosures
     * of the squared distances of these positions.
     */
    static int farthestPosition(double[] xs, double[] ys, int first, int last, double[] lo, double[] hi) {
        double ax = xs[first], ay = ys[first], bx = xs[last], by = ys[last];
        // enclose the squared distances, and find the largest lower bound
        Predicates.squaredDistancesToSegment(ax, ay, bx, by, xs, ys, first + 1, last, lo, hi);
        double maxLo = Double.NEGATIVE_INFINITY;
        for (int m = first + 1; m < last; m++) {
            if (lo[m] > maxLo) maxLo = lo[m];
        }
        // Positions whose upper bound is below maxLo are strictly closer than the farthest position. Among the
        // others (usually just one), find the first farthest position with exact comparisons.
        int farthest = -1;
        for (int m = first + 1; m < last; m++) {
            if (hi[m] < maxLo) continue;
            if (farthest < 0 || Predicates.compareDistanceToSegment(ax, ay, bx, by,
                    xs[m], ys[m], xs[farthest], ys[farthest]) > 0) {
                farthest = m;
            }
        }
        return farthest;
    }

    // is the distance of position p to the segment (first, last), whose square lies in [lo[p], hi[p]], at most the
    // tolerance?
    static boolean isWithinTolerance(double[] xs, double[] ys, int first, int last, int p, double[] lo, double[] hi,
                                     double tolerance, Interval tolerance2) {
        if (hi[p] <= tolerance2.lo()) return true;
        if (lo[p] > tolerance2.hi()) return false;
        return Predicates.compareDistanceToSegment(xs[first], ys[first], xs[last], ys[last], xs[p], ys[p], tolerance) <= 0;
    }

    static void checkTolerance(double tolerance) {
        if (!(tolerance >= 0) || tolerance == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Tolerance must be finite and non-negative: " + tolerance);
        }
    }

    // copies the X and Y coordinates, which must be finite
    static void coordinates(PositionSequence<?> positions, double[] xs, double[] ys) {
        for (int i = 0; i < positions.size(); i++) {
            Position p = positions.getPositionN(i);
            xs[i] = p.getCoordinate(0);
            ys[i] = p.getCoordinate(1);
            if (!Double.isFinite(xs[i]) || !Double.isFinite(ys[i])) {
                throw new IllegalArgumentException("Coordinates must be finite");
            }
        }
    }

    // the positions for which keep is true
    static <P extends Position> PositionSequence<P> kept(PositionSequence<P> positions, boolean[] keep) {
        int count = 0;
        for (boolean k : keep) {
            if (k) count++;
        }
        if (count == positions.size()) return positions;
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(count, positions.getPositionClass());
        for (int i = 0; i < keep.length; i++) {
            if (keep[i]) builder.add(positions.getPositionN(i));
        }
        return builder.toPositionSequence();
    }

    /**
     * Simplifies the positions of a line.
     *
     * @param positions the positions of a line
     * @param tolerance the distance tolerance (finite, non-negative)
     * @param <P>       the type of the positions
     * @return the kept positions, in their original order
     * @throws IllegalArgumentException if the tolerance is negative, NaN or infinite, or an X or Y coordinate is
     *                                  NaN or infinite
     * @see #keptPositions(PositionSequence, double)
     */
    public static <P extends Position> PositionSequence<P> simplify(PositionSequence<P> positions, double tolerance) {
        return kept(positions, keptPositions(positions, tolerance));
    }

    /**
     * Simplifies a {@code LineString}.
     *
     * @param line      the line
     * @param tolerance the distance tolerance (finite, non-negative)
     * @param <P>       the type of the positions
     * @return the simplified line, in the coordinate reference system of the line
     * @see #keptPositions(PositionSequence, double)
     */
    public static <P extends Position> LineString<P> simplify(LineString<P> line, double tolerance) {
        PositionSequence<P> simplified = simplify(line.getPositions(), tolerance);
        return simplified == line.getPositions() ? line : new LineString<>(simplified, line.getCoordinateReferenceSystem());
    }

    /**
     * Simplifies each line of a {@code MultiLineString}.
     *
     * @param lines     the lines
     * @param tolerance the distance tolerance (finite, non-negative)
     * @param <P>       the type of the positions
     * @return the simplified lines
     * @see #keptPositions(PositionSequence, double)
     */
    public static <P extends Position> MultiLineString<P> simplify(MultiLineString<P> lines, double tolerance) {
        if (lines.isEmpty()) return lines;
        List<LineString<P>> simplified = new ArrayList<>();
        for (int i = 0; i < lines.getNumGeometries(); i++) {
            simplified.add(simplify(lines.getGeometryN(i), tolerance));
        }
        return Geometries.mkMultiLineString(simplified);
    }

    /**
     * Simplifies the rings of a {@code Polygon}.
     *
     * <p>Each ring is simplified as a closed line, so its start position is always kept. A ring that is left with
     * fewer than 4 positions has collapsed: a collapsed interior ring is removed, and if the exterior ring
     * collapses, the result is an empty polygon. The result may be invalid (e.g. rings may intersect); use
     * {@link #simplify(Geometry, double, boolean)} to preserve the topology.</p>
     *
     * @param polygon   the polygon
     * @param tolerance the distance tolerance (finite, non-negative)
     * @param <P>       the type of the positions
     * @return the simplified polygon
     */
    public static <P extends Position> Polygon<P> simplify(Polygon<P> polygon, double tolerance) {
        checkTolerance(tolerance);
        if (polygon.isEmpty()) return polygon;
        LinearRing<P> shell = simplifyRing(polygon.getExteriorRing(), tolerance);
        if (shell == null) return new Polygon<>(polygon.getCoordinateReferenceSystem());
        List<LinearRing<P>> rings = new ArrayList<>();
        rings.add(shell);
        boolean changed = shell != polygon.getExteriorRing();
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            LinearRing<P> hole = simplifyRing(polygon.getInteriorRingN(i), tolerance);
            if (hole != null) rings.add(hole);
            changed |= hole != polygon.getInteriorRingN(i);
        }
        return changed ? Geometries.mkPolygon(rings) : polygon;
    }

    /**
     * Simplifies the polygons of a {@code MultiPolygon}. Polygons whose exterior ring collapses are removed.
     *
     * @param polygons  the polygons
     * @param tolerance the distance tolerance (finite, non-negative)
     * @param <P>       the type of the positions
     * @return the simplified polygons
     * @see #simplify(Polygon, double)
     */
    public static <P extends Position> MultiPolygon<P> simplify(MultiPolygon<P> polygons, double tolerance) {
        checkTolerance(tolerance);
        if (polygons.isEmpty()) return polygons;
        List<Polygon<P>> simplified = new ArrayList<>();
        for (int i = 0; i < polygons.getNumGeometries(); i++) {
            Polygon<P> p = simplify(polygons.getGeometryN(i), tolerance);
            if (!p.isEmpty()) simplified.add(p);
        }
        return simplified.isEmpty()
                ? new MultiPolygon<>(polygons.getCoordinateReferenceSystem())
                : Geometries.mkMultiPolygon(simplified);
    }

    /**
     * Simplifies a geometry, optionally preserving its topology.
     *
     * <p>Lines ({@code LineString}, {@code MultiLineString}) and rings ({@code LinearRing}, {@code Polygon},
     * {@code MultiPolygon}) are simplified; points are returned unchanged; the members of a
     * {@code GeometryCollection} are simplified (empty results are removed). The result has the type of the
     * geometry.</p>
     *
     * <p>Without topology preservation, each line and ring is simplified independently, as by
     * {@link #simplify(LineString, double)} and {@link #simplify(Polygon, double)}: collapsed rings are removed, and
     * the result may intersect itself.</p>
     *
     * <p>With topology preservation, the lines and rings of the geometry are simplified in order, and a run of
     * positions that is within the tolerance is only replaced by the segment between its endpoints if
     * <ul>
     *     <li>a ring keeps at least 4 positions, and a closed line does not collapse to a point;</li>
     *     <li>no other part of the geometry has a point in common with the new segment or with the removed part of
     *     the line, except at the endpoints of the new segment. So no intersections are created, and no existing
     *     intersections or contacts are removed;</li>
     *     <li>no other part of the geometry lies in the area between the removed part of the line and the new
     *     segment (e.g. a hole can not move out of its exterior ring).</li>
     * </ul>
     * Otherwise, the run is divided at its farthest position, as if it were not within the tolerance. The "other
     * parts" include the remaining parts of the same line, and the points of a {@code GeometryCollection}. All
     * tests are exact, so the topology of the geometry (as given by its double coordinates) is preserved exactly:
     * a valid geometry remains valid, and the intersection of any two lines or rings is unchanged.</p>
     *
     * <p>Since existing intersections are preserved exactly, a run that crosses or touches another part of the
     * geometry (other than at its endpoints) is never replaced; e.g. for crossing lines, the positions around each
     * crossing are kept. Rings are never removed, so the result has the same number of lines, rings and
     * polygons as the geometry.</p>
     *
     * @param geometry         the geometry
     * @param tolerance        the distance tolerance (finite, non-negative)
     * @param preserveTopology whether to preserve the topology
     * @param <P>              the type of the positions
     * @param <G>              the type of the geometry
     * @return the simplified geometry
     * @throws IllegalArgumentException if the tolerance is negative, NaN or infinite, if an X or Y coordinate is NaN
     *                                  or infinite, or the geometry type is not supported
     */
    @SuppressWarnings("unchecked")
    public static <P extends Position, G extends Geometry<P>> G simplify(G geometry, double tolerance,
                                                                         boolean preserveTopology) {
        checkTolerance(tolerance);
        if (geometry.isEmpty()) return geometry;
        return (G) (preserveTopology
                ? TopologyPreservingSimplification.simplify(geometry, tolerance)
                : simplifyIndependently((Geometry<P>) geometry, tolerance));
    }

    @SuppressWarnings("unchecked")
    private static <P extends Position> Geometry<P> simplifyIndependently(Geometry<P> geometry, double tolerance) {
        if (geometry.isEmpty() || geometry instanceof Point || geometry instanceof MultiPoint) return geometry;
        if (geometry instanceof LinearRing) {
            LinearRing<P> ring = simplifyRing((LinearRing<P>) geometry, tolerance);
            return ring == null ? new LinearRing<>(geometry.getCoordinateReferenceSystem()) : ring;
        }
        if (geometry instanceof LineString) return simplify((LineString<P>) geometry, tolerance);
        if (geometry instanceof Polygon) return simplify((Polygon<P>) geometry, tolerance);
        if (geometry instanceof MultiLineString) return simplify((MultiLineString<P>) geometry, tolerance);
        if (geometry instanceof MultiPolygon) return simplify((MultiPolygon<P>) geometry, tolerance);
        if (geometry instanceof GeometryCollection) {
            GeometryCollection<P> collection = (GeometryCollection<P>) geometry;
            List<Geometry<P>> simplified = new ArrayList<>();
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                Geometry<P> g = simplifyIndependently(collection.getGeometryN(i), tolerance);
                if (!g.isEmpty()) simplified.add(g);
            }
            return simplified.isEmpty()
                    ? new GeometryCollection<>(geometry.getCoordinateReferenceSystem())
                    : Geometries.mkGeometryCollection(simplified);
        }
        throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
    }

    // the simplified ring, or null if it collapses
    private static <P extends Position> LinearRing<P> simplifyRing(LinearRing<P> ring, double tolerance) {
        PositionSequence<P> simplified = simplify(ring.getPositions(), tolerance);
        if (simplified.size() < 4) return null;
        return simplified == ring.getPositions() ? ring : new LinearRing<>(simplified, ring.getCoordinateReferenceSystem());
    }
}
