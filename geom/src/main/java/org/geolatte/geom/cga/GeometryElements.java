package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Predicates;

import java.util.ArrayList;
import java.util.List;

/**
 * A geometry flattened into its elements: points, lines and the rings of polygons. Used by {@link PointLocator} and
 * {@link Relate}, which give the geometry union semantics: the geometry is the union of its elements.
 *
 * <p>Consecutive repeated positions are removed. A line with fewer than 2 distinct positions is a point. A ring
 * with zero area (all its positions are collinear) is not the boundary of an area; it is treated as a closed
 * line.</p>
 */
final class GeometryElements {

    /**
     * A ring of a polygon: closed ({@code xs[0] == xs[n - 1]}), without repeated consecutive positions. The
     * interior of its polygon lies to the left of each segment if {@code delta == 1}, to the right if
     * {@code delta == -1}.
     */
    static final class Ring {
        final double[] xs, ys;
        final int polygon;
        final int delta;

        Ring(double[] xs, double[] ys, int polygon, int delta) {
            this.xs = xs;
            this.ys = ys;
            this.polygon = polygon;
            this.delta = delta;
        }
    }

    final List<double[]> points = new ArrayList<>();      // {x, y}
    final List<double[][]> lines = new ArrayList<>();     // {xs, ys}, at least 2 distinct positions
    final List<Ring> rings = new ArrayList<>();
    int polygonCount = 0;
    int dimension = -1;    // the highest dimension of an element; -1 if there are none
    double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

    static GeometryElements of(Geometry<?> geometry) {
        GeometryElements elements = new GeometryElements();
        elements.collect(geometry);
        return elements;
    }

    boolean isEmpty() {
        return dimension < 0;
    }

    private void collect(Geometry<?> geometry) {
        if (geometry.isEmpty()) return;
        if (geometry instanceof Point) {
            PositionSequence<?> positions = geometry.getPositions();
            double[] xs = new double[1], ys = new double[1];
            DouglasPeucker.coordinates(positions, xs, ys);
            addPoint(xs[0], ys[0]);
        } else if (geometry instanceof LineString) {
            addLine(geometry.getPositions());
        } else if (geometry instanceof Polygon) {
            Polygon<?> polygon = (Polygon<?>) geometry;
            int id = polygonCount++;
            addRing(polygon.getExteriorRing().getPositions(), id, false);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                addRing(polygon.getInteriorRingN(i).getPositions(), id, true);
            }
        } else if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                collect(collection.getGeometryN(i));
            }
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }
    }

    private void addPoint(double x, double y) {
        points.add(new double[]{x + 0.0, y + 0.0});   // + 0.0 turns -0.0 into 0.0
        include(x, y, 0);
    }

    private void addLine(PositionSequence<?> positions) {
        double[][] c = distinct(positions);
        if (c[0].length == 1) {
            addPoint(c[0][0], c[1][0]);
            return;
        }
        lines.add(c);
        for (int i = 0; i < c[0].length; i++) include(c[0][i], c[1][i], 1);
    }

    private void addRing(PositionSequence<?> positions, int polygon, boolean hole) {
        if (positions.isEmpty()) return;
        double[][] c = distinct(positions);
        int n = c[0].length;
        if (n == 1) {
            addPoint(c[0][0], c[1][0]);
            return;
        }
        int orientation = Predicates.ringOrientation(c[0], c[1], n);
        if (orientation == 0) {
            lines.add(c);
            for (int i = 0; i < n; i++) include(c[0][i], c[1][i], 1);
            return;
        }
        int delta = hole ? -orientation : orientation;
        rings.add(new Ring(c[0], c[1], polygon, delta));
        for (int i = 0; i < n; i++) include(c[0][i], c[1][i], 2);
    }

    // the coordinates, without consecutive repeated positions
    private static double[][] distinct(PositionSequence<?> positions) {
        int n = positions.size();
        double[] xs = new double[n], ys = new double[n];
        DouglasPeucker.coordinates(positions, xs, ys);
        int m = 0;
        for (int i = 0; i < n; i++) {
            double x = xs[i] + 0.0, y = ys[i] + 0.0;
            if (m > 0 && xs[m - 1] == x && ys[m - 1] == y) continue;
            xs[m] = x;
            ys[m] = y;
            m++;
        }
        if (m < n) {
            xs = java.util.Arrays.copyOf(xs, m);
            ys = java.util.Arrays.copyOf(ys, m);
        }
        return new double[][]{xs, ys};
    }

    private void include(double x, double y, int dim) {
        dimension = Math.max(dimension, dim);
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
    }

    /**
     * @return true if the bounding boxes of this and the other geometry have a point in common
     */
    boolean envelopeIntersects(GeometryElements other) {
        return !isEmpty() && !other.isEmpty()
                && minX <= other.maxX && other.minX <= maxX && minY <= other.maxY && other.minY <= maxY;
    }

    /**
     * @return true if the bounding box of this geometry contains that of the other geometry
     */
    boolean envelopeContains(GeometryElements other) {
        return !isEmpty() && !other.isEmpty()
                && minX <= other.minX && other.maxX <= maxX && minY <= other.minY && other.maxY <= maxY;
    }
}
