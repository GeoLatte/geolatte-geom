package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.*;

/**
 * The overlay operations: intersection, union, difference and symmetric difference of two geometries, and the
 * union of the elements of one geometry.
 *
 * <p>The operations have point set semantics: a geometry is the union of its elements (points, lines and polygons,
 * also as members of a {@code GeometryCollection}, which may overlap), and the result is the set of points for which
 * the operation holds. So a result can mix dimensions, and is then a {@code GeometryCollection}: the intersection
 * of two squares that share a side is a {@code LineString}. Lower dimensional parts that lie on higher dimensional
 * parts of the result are absorbed. Each polygon of an operand must be valid; the polygons may overlap each other.</p>
 *
 * <p>The result is computed exactly, then snap rounded to a {@link Grid}: by default the double grid, or a fixed
 * grid with a decimal cell size, such as 0.001. The lines and rings of both operands are snap rounded together
 * ({@link SnapRounder}): each segment is routed through the grid points of the cells that contain the vertices and
 * the crossing points of segments, where it passes through those cells. All further decisions are exact.
 * Consequently:</p>
 * <ul>
 *     <li>the result is valid;</li>
 *     <li>each of its vertices is a grid point: on the double grid a vertex of an operand, or the double point
 *     nearest to a crossing of two segments; on a fixed grid the grid point nearest to a vertex or a crossing
 *     (represented by the double nearest to it);</li>
 *     <li>each of its edges passes through the cells that the exact result passes through, so it lies within one
 *     cell of it (on the double grid: one unit in the last place);</li>
 *     <li>parts that are thinner than a cell can collapse; they are dropped. On a fixed grid this includes lines
 *     and polygons that collapse to a point.</li>
 * </ul>
 * <p>Points of an operand that are not on its lines or polygons are located exactly in the other operand. On a
 * fixed grid they are then rounded to their grid point, and dropped if that lies on a line or polygon of the
 * result.</p>
 *
 * <p>A fixed grid needs cells larger than the spacing of the doubles at the coordinates of the operands: a cell
 * size of 0.001 works up to coordinates of about 10<sup>12</sup>. Otherwise the operations throw an
 * {@code IllegalArgumentException}.</p>
 *
 * <p>The result has the coordinate reference system of the first operand. Its vertices keep the Z and M
 * coordinates of the operands (of the first operand where both have a vertex); a new vertex gets the values
 * interpolated along the segment it was created on (of the first operand, if both have one). Polygon shells are
 * counterclockwise, holes clockwise. An empty result is an empty geometry of the dimension of the result: for an
 * intersection the lower of the dimensions of the operands, for a difference that of the first operand, otherwise
 * the higher one.</p>
 *
 * <p>{@link #reducePrecision(Geometry, Grid)} snap rounds a single geometry to a grid: it is the union of its
 * elements on that grid.</p>
 */
public final class Overlay {

    /**
     * An overlay operation
     */
    public enum Operation {
        INTERSECTION, UNION, DIFFERENCE, SYM_DIFFERENCE;

        /**
         * @return whether a point that is (or is not) in the first and the second operand is in the result
         */
        boolean test(boolean a, boolean b) {
            switch (this) {
                case INTERSECTION:
                    return a && b;
                case UNION:
                    return a || b;
                case DIFFERENCE:
                    return a && !b;
                default:
                    return a != b;
            }
        }
    }

    private Overlay() {
    }

    /**
     * @return the points common to both geometries, snap rounded to the double grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> intersection(Geometry<P> a, Geometry<P> b) {
        return overlay(a, b, Operation.INTERSECTION, Grid.DOUBLE);
    }

    /**
     * @return the points common to both geometries, snap rounded to the grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or too large for the grid
     */
    public static <P extends Position> Geometry<P> intersection(Geometry<P> a, Geometry<P> b, Grid grid) {
        return overlay(a, b, Operation.INTERSECTION, grid);
    }

    /**
     * @return the points of either geometry, snap rounded to the double grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> union(Geometry<P> a, Geometry<P> b) {
        return overlay(a, b, Operation.UNION, Grid.DOUBLE);
    }

    /**
     * @return the points of either geometry, snap rounded to the grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or too large for the grid
     */
    public static <P extends Position> Geometry<P> union(Geometry<P> a, Geometry<P> b, Grid grid) {
        return overlay(a, b, Operation.UNION, grid);
    }

    /**
     * @return the points of the first geometry that are not points of the second, snap rounded to the double grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> difference(Geometry<P> a, Geometry<P> b) {
        return overlay(a, b, Operation.DIFFERENCE, Grid.DOUBLE);
    }

    /**
     * @return the points of the first geometry that are not points of the second, snap rounded to the grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or too large for the grid
     */
    public static <P extends Position> Geometry<P> difference(Geometry<P> a, Geometry<P> b, Grid grid) {
        return overlay(a, b, Operation.DIFFERENCE, grid);
    }

    /**
     * @return the points of exactly one of the geometries, snap rounded to the double grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> symDifference(Geometry<P> a, Geometry<P> b) {
        return overlay(a, b, Operation.SYM_DIFFERENCE, Grid.DOUBLE);
    }

    /**
     * @return the points of exactly one of the geometries, snap rounded to the grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or too large for the grid
     */
    public static <P extends Position> Geometry<P> symDifference(Geometry<P> a, Geometry<P> b, Grid grid) {
        return overlay(a, b, Operation.SYM_DIFFERENCE, grid);
    }

    /**
     * @return the union of the elements of the geometry: its point set, as a valid geometry, snap rounded to the
     * double grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> unaryUnion(Geometry<P> geometry) {
        return unaryUnion(geometry, Grid.DOUBLE);
    }

    /**
     * @return the union of the elements of the geometry: its point set, as a valid geometry, snap rounded to the
     * grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or too large for the grid
     */
    public static <P extends Position> Geometry<P> unaryUnion(Geometry<P> geometry, Grid grid) {
        return overlay(geometry, Geometries.mkEmptyGeometryCollection(geometry.getCoordinateReferenceSystem()),
                Operation.UNION, grid);
    }

    /**
     * Reduces the precision of a geometry to a grid: snap rounds it to the grid, and makes it valid. This is the
     * same as {@link #unaryUnion(Geometry, Grid)}: overlapping elements are merged, crossing lines are noded, and
     * parts that collapse to lower dimensions are dropped.
     *
     * @return the geometry, snap rounded to the grid, as a valid geometry
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or too large for the grid
     */
    public static <P extends Position> Geometry<P> reducePrecision(Geometry<P> geometry, Grid grid) {
        return unaryUnion(geometry, grid);
    }

    /**
     * @return the result of the operation on the geometries, snap rounded to the double grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Geometry<P> overlay(Geometry<P> a, Geometry<P> b, Operation operation) {
        return overlay(a, b, operation, Grid.DOUBLE);
    }

    /**
     * @return the result of the operation on the geometries, snap rounded to the grid
     * @throws IllegalArgumentException if a coordinate is NaN or infinite, or too large for the grid
     */
    public static <P extends Position> Geometry<P> overlay(Geometry<P> a, Geometry<P> b, Operation operation,
                                                           Grid grid) {
        Objects.requireNonNull(operation);
        Objects.requireNonNull(grid);
        GeometryElements ea = GeometryElements.withMeasures(a), eb = GeometryElements.withMeasures(b);
        CoordinateReferenceSystem<P> crs = a.getCoordinateReferenceSystem();
        int dimension = resultDimension(operation, typeDimension(a), typeDimension(b));
        if (grid.isFixed()) grid.checkRange(Math.max(maxAbs(ea), maxAbs(eb)));
        if (operation == Operation.INTERSECTION && !envelopesMeet(ea, eb, grid)) return empty(dimension, crs);
        List<OverlayGraph.Input> inputs = new ArrayList<>();
        addInputs(OverlayGraph.A, ea, inputs);
        addInputs(OverlayGraph.B, eb, inputs);
        OverlayGraph graph = new OverlayGraph(inputs, grid);
        graph.label();
        graph.select(operation);
        OverlayResult result = OverlayResult.of(graph, operation);
        List<double[]> points = new ArrayList<>();
        for (OverlayGraph.Node n : result.points) points.add(new double[]{n.x, n.y, n.z, n.m});
        int resultPoints = points.size();
        addIsolatedPoints(ea, eb, operation, grid, points);
        if (grid.isFixed() && points.size() > resultPoints) {
            // rounded points on the lines or polygons of the result are absorbed
            PointLocator locator = PointLocator.of(build(result, new ArrayList<>(), dimension, crs));
            for (int i = points.size() - 1; i >= resultPoints; i--) {
                double[] p = points.get(i);
                if (locator.locate(p[0], p[1]) != Location.EXTERIOR) points.remove(i);
            }
        }
        return build(result, points, dimension, crs);
    }

    private static double maxAbs(GeometryElements e) {
        if (e.isEmpty()) return 0;
        return Math.max(Math.max(Math.abs(e.minX), Math.abs(e.maxX)), Math.max(Math.abs(e.minY), Math.abs(e.maxY)));
    }

    // whether the bounding boxes meet; on a fixed grid, after enlarging them by a cell, as snapping can make the
    // geometries meet
    private static boolean envelopesMeet(GeometryElements a, GeometryElements b, Grid grid) {
        if (!grid.isFixed()) return a.envelopeIntersects(b);
        if (a.isEmpty() || b.isEmpty()) return false;
        double g = grid.cellSize().doubleValue();
        return a.minX - g <= b.maxX + g && b.minX - g <= a.maxX + g
                && a.minY - g <= b.maxY + g && b.minY - g <= a.maxY + g;
    }

    private static void addInputs(int g, GeometryElements e, List<OverlayGraph.Input> inputs) {
        for (double[][] line : e.lines) inputs.add(new OverlayGraph.Input(g, false, 0, line));
        for (GeometryElements.Ring r : e.rings) {
            inputs.add(new OverlayGraph.Input(g, true, r.delta, new double[][]{r.xs, r.ys, r.zs, r.ms}));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // points
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Adds the points of each operand that are not on its own lines or polygons, and are in the result: located
     * exactly in the other operand. Such a point is absorbed by a union if it lies on a line or polygon of the
     * other operand.
     */
    private static void addIsolatedPoints(GeometryElements ea, GeometryElements eb, Operation op, Grid grid,
                                          List<double[]> points) {
        if (ea.points.isEmpty() && eb.points.isEmpty()) return;
        GeometryElements[] elements = {ea, eb};
        PointLocator[] covering = new PointLocator[2];
        List<Set<PointLocator.XY>> pointSets = new ArrayList<>();
        for (int g = 0; g < 2; g++) {
            covering[g] = new PointLocator(elements[g].withoutPoints());
            Set<PointLocator.XY> set = new HashSet<>();
            for (double[] p : elements[g].points) set.add(new PointLocator.XY(p[0], p[1]));
            pointSets.add(set);
        }
        Set<PointLocator.XY> emitted = new HashSet<>();
        for (double[] p : points) emitted.add(new PointLocator.XY(p[0], p[1]));
        for (int g = 0; g < 2; g++) {
            int o = 1 - g;
            for (double[] p : elements[g].points) {
                PointLocator.XY xy = new PointLocator.XY(p[0], p[1]);
                if (covering[g].locate(p[0], p[1]) != Location.EXTERIOR) continue;
                boolean onOther = covering[o].locate(p[0], p[1]) != Location.EXTERIOR;
                boolean inOther = onOther || pointSets.get(o).contains(xy);
                boolean in = g == OverlayGraph.A ? op.test(true, inOther) : op.test(inOther, true);
                if (!in || (op == Operation.UNION && onOther)) continue;
                // rounded to its grid point (on the double grid: the point itself)
                double[] r = grid.isFixed() ? new double[]{grid.round(p[0]), grid.round(p[1]), p[2], p[3]} : p;
                if (emitted.add(new PointLocator.XY(r[0], r[1]))) points.add(r);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // the result geometry
    // ---------------------------------------------------------------------------------------------------------

    // the dimension of the geometry's type: of its members for a collection; -1 for an empty collection
    static int typeDimension(Geometry<?> geometry) {
        if (geometry instanceof Point || geometry instanceof MultiPoint) return 0;
        if (geometry instanceof LineString || geometry instanceof MultiLineString) return 1;
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) return 2;
        int dimension = -1;
        if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                dimension = Math.max(dimension, typeDimension(collection.getGeometryN(i)));
            }
        }
        return dimension;
    }

    private static int resultDimension(Operation op, int a, int b) {
        switch (op) {
            case INTERSECTION:
                return Math.min(a, b);
            case DIFFERENCE:
                return a;
            default:
                return Math.max(a, b);
        }
    }

    private static <P extends Position> Geometry<P> empty(int dimension, CoordinateReferenceSystem<P> crs) {
        switch (dimension) {
            case 0:
                return Geometries.mkEmptyPoint(crs);
            case 1:
                return Geometries.mkEmptyLineString(crs);
            case 2:
                return Geometries.mkEmptyPolygon(crs);
            default:
                return Geometries.mkEmptyGeometryCollection(crs);
        }
    }

    private static <P extends Position> Geometry<P> build(OverlayResult result, List<double[]> points, int dimension,
                                                          CoordinateReferenceSystem<P> crs) {
        PositionFactory<P> factory = new PositionFactory<>(crs);
        List<Polygon<P>> polygons = new ArrayList<>(result.polygons.size());
        for (List<List<OverlayGraph.Node>> rings : result.polygons) {
            List<LinearRing<P>> linearRings = new ArrayList<>(rings.size());
            for (List<OverlayGraph.Node> ring : rings) {
                linearRings.add(new LinearRing<>(factory.sequence(ring, true), crs));
            }
            polygons.add(Geometries.mkPolygon(linearRings));
        }
        List<LineString<P>> lines = new ArrayList<>(result.lines.size());
        for (List<OverlayGraph.Node> line : result.lines) {
            lines.add(new LineString<>(factory.sequence(line, false), crs));
        }
        points.sort((p, q) -> p[0] != q[0] ? Double.compare(p[0], q[0]) : Double.compare(p[1], q[1]));
        List<Point<P>> pointList = new ArrayList<>(points.size());
        for (double[] p : points) pointList.add(new Point<>(factory.position(p[0], p[1], p[2], p[3]), crs));
        int kinds = (polygons.isEmpty() ? 0 : 1) + (lines.isEmpty() ? 0 : 1) + (pointList.isEmpty() ? 0 : 1);
        if (kinds == 0) return empty(dimension, crs);
        if (kinds > 1) {
            List<Geometry<P>> parts = new ArrayList<>(polygons.size() + lines.size() + pointList.size());
            parts.addAll(polygons);
            parts.addAll(lines);
            parts.addAll(pointList);
            return Geometries.mkGeometryCollection(parts);
        }
        if (!polygons.isEmpty()) return polygons.size() == 1 ? polygons.get(0) : Geometries.mkMultiPolygon(polygons);
        if (!lines.isEmpty()) return lines.size() == 1 ? lines.get(0) : Geometries.mkMultiLineString(lines);
        return pointList.size() == 1 ? pointList.get(0) : Geometries.mkMultiPoint(pointList);
    }

    private static final class PositionFactory<P extends Position> {
        private final CoordinateReferenceSystem<P> crs;
        private final int dimension, zIndex, mIndex;

        PositionFactory(CoordinateReferenceSystem<P> crs) {
            this.crs = crs;
            this.dimension = crs.getCoordinateDimension();
            this.zIndex = crs.hasZ() ? 2 : -1;
            this.mIndex = crs.hasM() ? dimension - 1 : -1;
        }

        double[] coordinates(double x, double y, double z, double m) {
            double[] c = new double[dimension];
            c[0] = x;
            c[1] = y;
            if (zIndex >= 0) c[zIndex] = z;
            if (mIndex >= 0) c[mIndex] = m;
            return c;
        }

        P position(double x, double y, double z, double m) {
            return Positions.mkPosition(crs, coordinates(x, y, z, m));
        }

        PositionSequence<P> sequence(List<OverlayGraph.Node> nodes, boolean close) {
            int n = nodes.size();
            PositionSequenceBuilder<P> builder =
                    PositionSequenceBuilders.fixedSized(n + (close ? 1 : 0), crs.getPositionClass());
            for (OverlayGraph.Node node : nodes) builder.add(coordinates(node.x, node.y, node.z, node.m));
            if (close) {
                OverlayGraph.Node node = nodes.get(0);
                builder.add(coordinates(node.x, node.y, node.z, node.m));
            }
            return builder.toPositionSequence();
        }
    }
}
