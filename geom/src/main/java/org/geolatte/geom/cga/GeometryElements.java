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
    static double[][] distinct(PositionSequence<?> positions) {
        int n = positions.size();
        double[] xs = new double[n], ys = new double[n];
        DouglasPeucker.coordinates(positions, xs, ys);
        return distinct(xs, ys);
    }

    // the coordinates without consecutive repeated positions, and with -0.0 replaced by 0.0; may reuse the arrays
    static double[][] distinct(double[] xs, double[] ys) {
        int n = xs.length;
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

    static final int EXTERIOR = 0, ON = 1, INSIDE = 2;

    /**
     * A quick, exact location of the point by a linear scan: {@code INSIDE} if it lies in the interior of a polygon
     * (and not on one of its rings), so in the interior of the geometry; {@code EXTERIOR} if it is not a point of
     * the geometry; {@code ON} otherwise (it lies on an element, in the interior or on the boundary).
     */
    int roughLocation(double x, double y) {
        x += 0.0;
        y += 0.0;
        boolean on = false;
        boolean[] odd = new boolean[polygonCount], touched = new boolean[polygonCount];
        for (Ring r : rings) {
            double[] xs = r.xs, ys = r.ys;
            for (int i = 0; i + 1 < xs.length; i++) {
                double y0 = ys[i], y1 = ys[i + 1];
                if ((y0 > y && y1 > y) || (y0 < y && y1 < y)) continue;
                double x0 = xs[i], x1 = xs[i + 1];
                if (x0 < x && x1 < x) continue;
                int o = Predicates.orientation(x0, y0, x1, y1, x, y);
                if (o == 0 && Math.min(x0, x1) <= x) {
                    touched[r.polygon] = true;
                    on = true;
                } else if (y0 <= y ? y1 > y && o > 0 : y1 <= y && o < 0) {
                    odd[r.polygon] = !odd[r.polygon];
                }
            }
        }
        for (int p = 0; p < polygonCount; p++) {
            if (odd[p] && !touched[p]) return INSIDE;
            if (odd[p]) on = true;
        }
        if (on) return ON;
        for (double[][] line : lines) {
            double[] xs = line[0], ys = line[1];
            for (int i = 0; i + 1 < xs.length; i++) {
                if (x < Math.min(xs[i], xs[i + 1]) || x > Math.max(xs[i], xs[i + 1])) continue;
                if (y < Math.min(ys[i], ys[i + 1]) || y > Math.max(ys[i], ys[i + 1])) continue;
                if (Predicates.orientation(xs[i], ys[i], xs[i + 1], ys[i + 1], x, y) == 0) return ON;
            }
        }
        for (double[] p : points) {
            if (p[0] == x && p[1] == y) return ON;
        }
        return EXTERIOR;
    }

    /**
     * @return a position of the geometry (the first one of its first element), or null if it is empty
     */
    double[] anyPosition() {
        if (!points.isEmpty()) return points.get(0);
        if (!lines.isEmpty()) return new double[]{lines.get(0)[0][0], lines.get(0)[1][0]};
        if (!rings.isEmpty()) return new double[]{rings.get(0).xs[0], rings.get(0).ys[0]};
        return null;
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
