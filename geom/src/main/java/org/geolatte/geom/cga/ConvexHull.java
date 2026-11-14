package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Predicates;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Computes the exact convex hull of a set of positions, in the plane of the first two coordinates.
 *
 * <p>The hull is computed with Andrew's monotone chain algorithm (a variant of the Graham scan), whose only
 * geometric test is the left-turn test (the orientation of three positions). That test is evaluated exactly with
 * {@link Predicates#orientation(Position, Position, Position)}, as proposed by Ratschek &amp; Rokne,
 * <em>Geometric Computations with Interval and New Robust Methods</em> (Ch. 4, Application 1, and Sec. 8.2).
 * Since the vertices of the hull are input positions, no new coordinates are computed, and the result is the
 * exact convex hull of the positions as given:</p>
 * <ul>
 *     <li>every position lies inside or on the boundary of the hull;</li>
 *     <li>the hull is strictly convex: positions on a hull edge, but not at a corner, are not vertices;</li>
 *     <li>the vertices are input positions (the same instances, so Z and M values are preserved). Of positions
 *     with equal X and Y coordinates, the first one encountered is used;</li>
 *     <li>the result is deterministic: it does not depend on the order of the input positions, apart from the
 *     choice between positions with equal X and Y coordinates.</li>
 * </ul>
 *
 * <p>The vertices are returned in counterclockwise order, starting at the vertex with the smallest X coordinate
 * (and of those, the smallest Y coordinate).</p>
 */
public final class ConvexHull {

    // lexicographic order on (x, y); numeric comparison, so that 0.0 and -0.0 are equal
    private static final Comparator<Position> XY_ORDER = (p, q) -> {
        int c = compare(p.getCoordinate(0), q.getCoordinate(0));
        return c != 0 ? c : compare(p.getCoordinate(1), q.getCoordinate(1));
    };

    // below this number of positions, the Akl-Toussaint heuristic does not pay off
    private static final int PREFILTER_THRESHOLD = 32;

    private ConvexHull() {
    }

    /**
     * Returns the vertices of the convex hull of the specified positions.
     *
     * @param positions the positions
     * @param <P>       the type of the positions
     * @return the vertices of the convex hull in counterclockwise order, starting at the vertex with the smallest X
     * (and then smallest Y) coordinate, without repeating the first vertex. The list is empty if there are no
     * positions, contains one position if all positions have the same X and Y coordinates, and contains the two
     * extreme positions if all positions are collinear.
     * @throws IllegalArgumentException if an X or Y coordinate is NaN or infinite (e.g. for an empty
     *                                  {@code Position})
     */
    public static <P extends Position> List<P> vertices(Iterable<? extends P> positions) {
        List<P> sorted = new ArrayList<>();
        for (P p : positions) {
            if (!Double.isFinite(p.getCoordinate(0)) || !Double.isFinite(p.getCoordinate(1))) {
                throw new IllegalArgumentException("Coordinates must be finite");
            }
            sorted.add(p);
        }
        if (sorted.size() > PREFILTER_THRESHOLD) {
            sorted = withoutInteriorPositions(sorted);
        }
        sorted.sort(XY_ORDER);  // stable: the first of the positions with equal X and Y comes first
        int n = removeDuplicates(sorted);
        if (n <= 2) {
            return new ArrayList<>(sorted.subList(0, n));
        }

        // lower chain from left to right, then upper chain from right to left. A position is removed from the chain
        // as long as it does not make a strict left turn.
        List<P> hull = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            P p = sorted.get(i);
            while (hull.size() >= 2 && !isLeftTurn(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p)) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        int lowerSize = hull.size();
        for (int i = n - 2; i >= 0; i--) {
            P p = sorted.get(i);
            while (hull.size() > lowerSize && !isLeftTurn(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p)) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        hull.remove(hull.size() - 1);  // the first vertex, which closes the upper chain
        return hull;
    }

    /**
     * Returns the convex hull of the specified geometry.
     *
     * <p>The result has the same coordinate reference system as the geometry, and follows the conventions of the
     * JTS {@code ConvexHull}:</p>
     * <ul>
     *     <li>an empty {@code GeometryCollection} for an empty geometry;</li>
     *     <li>a {@code Point} if all positions have the same X and Y coordinates;</li>
     *     <li>a {@code LineString} between the two extreme positions, if all positions are collinear;</li>
     *     <li>a {@code Polygon} otherwise, with the exterior ring in counterclockwise order.</li>
     * </ul>
     *
     * @param geometry the geometry
     * @param <P>      the type of the positions
     * @return the convex hull
     * @throws IllegalArgumentException if an X or Y coordinate is NaN or infinite
     * @see #vertices(Iterable)
     */
    public static <P extends Position> Geometry<P> of(Geometry<P> geometry) {
        CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
        List<P> vertices = vertices(geometry.getPositions());
        switch (vertices.size()) {
            case 0:
                return Geometries.mkEmptyGeometryCollection(crs);
            case 1:
                return Geometries.mkPoint(vertices.get(0), crs);
            case 2:
                return Geometries.mkLineString(sequence(vertices, false, crs), crs);
            default:
                return new Polygon<>(Geometries.mkLinearRing(sequence(vertices, true, crs), crs));
        }
    }

    private static <P extends Position> PositionSequence<P> sequence(List<P> vertices, boolean close,
                                                                     CoordinateReferenceSystem<P> crs) {
        PositionSequenceBuilder<P> builder =
                PositionSequenceBuilders.fixedSized(vertices.size() + (close ? 1 : 0), crs.getPositionClass());
        for (P p : vertices) {
            builder.add(p);
        }
        if (close) {
            builder.add(vertices.get(0));
        }
        return builder.toPositionSequence();
    }

    /**
     * The Akl-Toussaint heuristic: removes the positions that lie strictly inside the octagon of the positions with
     * extreme values of x, y, x + y and x - y. These can not be vertices of the hull. For uniformly distributed
     * positions, this removes almost all positions before sorting.
     *
     * <p>The extremes are selected in floating point; rounding errors in x + y and x - y only affect the choice
     * of the octagon's vertices, which are input positions in any case. A position is removed only if it lies
     * strictly to the left of every (non-degenerate) edge of the octagon, as decided by the exact orientation test.
     * Seen from such a position, the octagon turns counterclockwise along every edge, so the octagon winds around
     * it, and the position lies strictly inside the convex hull of the octagon's vertices, even if rounding errors
     * have made the octagon slightly non-convex.</p>
     */
    private static <P extends Position> List<P> withoutInteriorPositions(List<P> positions) {
        // indices of: min y, max x - y, max x, max x + y, max y, min x - y, min x, min x + y (counterclockwise)
        int[] extreme = new int[8];
        double[] best = new double[8];
        for (int i = 0; i < positions.size(); i++) {
            double x = positions.get(i).getCoordinate(0), y = positions.get(i).getCoordinate(1);
            double[] keys = {-y, x - y, x, x + y, y, y - x, -x, -x - y};
            for (int k = 0; k < 8; k++) {
                if (i == 0 || keys[k] > best[k]) {
                    best[k] = keys[k];
                    extreme[k] = i;
                }
            }
        }
        List<P> octagon = new ArrayList<>();
        for (int k = 0; k < 8; k++) {
            P v = positions.get(extreme[k]);
            if (octagon.isEmpty() || XY_ORDER.compare(octagon.get(octagon.size() - 1), v) != 0) octagon.add(v);
        }
        while (octagon.size() > 1 && XY_ORDER.compare(octagon.get(0), octagon.get(octagon.size() - 1)) == 0) {
            octagon.remove(octagon.size() - 1);
        }
        if (octagon.size() < 3) return positions;

        List<P> result = new ArrayList<>();
        for (P p : positions) {
            if (!isStrictlyInside(octagon, p)) result.add(p);
        }
        return result;
    }

    private static boolean isStrictlyInside(List<? extends Position> polygon, Position p) {
        for (int i = 0; i < polygon.size(); i++) {
            if (!isLeftTurn(polygon.get(i), polygon.get((i + 1) % polygon.size()), p)) return false;
        }
        return true;
    }

    // removes positions with the same X and Y as their predecessor from the sorted list, returns the new size
    private static <P extends Position> int removeDuplicates(List<P> sorted) {
        int n = 0;
        for (P p : sorted) {
            if (n == 0 || XY_ORDER.compare(sorted.get(n - 1), p) != 0) {
                sorted.set(n++, p);
            }
        }
        return n;
    }

    private static boolean isLeftTurn(Position a, Position b, Position c) {
        return Predicates.orientation(a, b, c) > 0;
    }

    private static int compare(double a, double b) {
        return a < b ? -1 : (a > b ? 1 : 0);
    }
}
