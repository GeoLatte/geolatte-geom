package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.Predicates;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A point that is guaranteed to lie on a geometry ({@code ST_PointOnSurface}), as JTS {@code InteriorPoint}
 * defines it:
 * <ul>
 *     <li>for a geometry with polygons: a point in the <b>interior</b> of one of its polygons, near the middle of
 *     its envelope (see below);</li>
 *     <li>otherwise, for a geometry with lines: the vertex nearest to the centroid that is not an endpoint of a line,
 *     or, if there is none, the endpoint nearest to the centroid;</li>
 *     <li>otherwise: the point nearest to the centroid.</li>
 * </ul>
 *
 * <p>The point in a polygon is found on a horizontal scan line through the middle of the polygon's envelope, moved
 * to the nearest y between two vertex y coordinates, so that it passes through no vertex. The crossings of the scan
 * line with the rings are computed approximately, and the result is the midpoint of the widest interval between
 * crossings that lies inside the polygon. Whether the result is inside is then <b>decided exactly</b>, by the parity
 * of the edges that pass to its left, with exact orientation tests; a point on an edge is rejected. So the result is
 * in the interior of a valid polygon, whatever rounding errors the approximation made; if no candidate passes the
 * test (for instance if the polygon is extremely thin everywhere along the scan line), other intervals and scan
 * lines are tried. For a multipolygon or a collection, the polygon with the widest interval is chosen; the point is
 * then in the interior of the union of the polygons as well.</p>
 *
 * <p>A polygon for which no interior point is found counts as its rings: one with zero area, or an extremely thin
 * one (often no point with double coordinates lies in its interior). A vertex is a point of the geometry, so the
 * result always lies on it, at worst on its boundary.</p>
 *
 * <p>The result of an empty geometry is an empty point. The result has the coordinate reference system of the
 * geometry; coordinates other than X and Y (Z, M) are NaN.</p>
 */
public final class PointOnSurface {

    // the number of scan lines that are tried, if the first does not give a point
    private static final int MAX_SCAN_LINES = 16;
    // the number of intervals on a scan line that are tested, widest first
    private static final int MAX_INTERVALS = 8;

    private PointOnSurface() {
    }

    /**
     * @return a point on the geometry, in its interior if it has polygons; an empty point if it is empty
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Point<P> of(Geometry<P> geometry) {
        CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
        double[] xy = of2D(geometry);
        if (xy == null) return new Point<>(crs);
        double[] coordinates = new double[crs.getCoordinateDimension()];
        Arrays.fill(coordinates, Double.NaN);
        coordinates[0] = xy[0];
        coordinates[1] = xy[1];
        return new Point<>(Positions.mkPosition(crs, coordinates), crs);
    }

    /**
     * @return the X and Y coordinate of a point on the geometry, or null if it is empty
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static double[] of2D(Geometry<?> geometry) {
        List<Polygon<?>> polygons = new ArrayList<>();
        List<PositionSequence<?>> lines = new ArrayList<>();
        List<PositionSequence<?>> points = new ArrayList<>();
        collect(geometry, polygons, lines, points);
        double[] best = null;
        List<PositionSequence<?>> rings = new ArrayList<>();
        for (Polygon<?> polygon : polygons) {
            double[][][] c = rings(polygon);
            double[] candidate = inPolygon(c[0], c[1]);
            if (candidate == null) {
                // no interior: the rings count as lines
                rings.add(polygon.getExteriorRing().getPositions());
                for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                    rings.add(polygon.getInteriorRingN(i).getPositions());
                }
            } else if (best == null || candidate[2] > best[2]) {
                best = candidate;
            }
        }
        if (best != null) return new double[]{best[0] + 0.0, best[1] + 0.0};
        lines.addAll(rings);
        if (lines.isEmpty() && points.isEmpty()) return null;
        double[] centroid = Centroid.of2D(geometry);
        double[] result = !lines.isEmpty() ? onLines(lines, centroid) : nearest(points, centroid);
        return new double[]{result[0] + 0.0, result[1] + 0.0};
    }

    private static void collect(Geometry<?> geometry, List<Polygon<?>> polygons, List<PositionSequence<?>> lines,
                                List<PositionSequence<?>> points) {
        if (geometry.isEmpty()) return;
        if (geometry instanceof Point) {
            points.add(geometry.getPositions());
        } else if (geometry instanceof LineString) {
            lines.add(geometry.getPositions());
        } else if (geometry instanceof Polygon) {
            polygons.add((Polygon<?>) geometry);
        } else if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                collect(collection.getGeometryN(i), polygons, lines, points);
            }
        }
    }

    // the X and Y coordinates of the rings of a polygon, as {xs, ys}, one array per ring
    private static double[][][] rings(Polygon<?> polygon) {
        int n = polygon.getNumInteriorRing() + 1;
        double[][] xs = new double[n][], ys = new double[n][];
        for (int r = 0; r < n; r++) {
            PositionSequence<?> positions = (r == 0 ? polygon.getExteriorRing() : polygon.getInteriorRingN(r - 1))
                    .getPositions();
            xs[r] = new double[positions.size()];
            ys[r] = new double[positions.size()];
            DouglasPeucker.coordinates(positions, xs[r], ys[r]);
        }
        return new double[][][]{xs, ys};
    }

    // ---------------------------------------------------------------------------------------------------------
    // a point in the interior of a polygon
    // ---------------------------------------------------------------------------------------------------------

    /**
     * @return {x, y, width of the interval} of an interior point of the polygon with the given rings, or null if
     * none is found
     */
    static double[] inPolygon(double[][] xs, double[][] ys) {
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (double[] ring : ys) {
            for (double y : ring) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        if (!(minY < maxY)) return null;
        // the first scan line: between the vertex y coordinates nearest to the middle, below and above
        double centre = 0.5 * minY + 0.5 * maxY;
        double below = Double.NEGATIVE_INFINITY, above = Double.POSITIVE_INFINITY;
        for (double[] ring : ys) {
            for (double y : ring) {
                if (y <= centre) {
                    below = Math.max(below, y);
                } else {
                    above = Math.min(above, y);
                }
            }
        }
        double[] result = above < Double.POSITIVE_INFINITY ? onScanLine(xs, ys, below, above) : null;
        if (result != null) return result;
        // the other gaps between the vertex y coordinates, from the middle outwards
        double[] all = distinctYs(ys);
        int middle = Math.max(0, Arrays.binarySearch(all, below));
        for (int step = 1; step < 2 * MAX_SCAN_LINES; step++) {
            int k = (step & 1) != 0 ? middle + (step + 1) / 2 : middle - step / 2;
            if (k < 0 || k + 1 >= all.length) continue;
            result = onScanLine(xs, ys, all[k], all[k + 1]);
            if (result != null) return result;
        }
        return null;
    }

    private static double[] distinctYs(double[][] ys) {
        int n = 0;
        for (double[] ring : ys) n += ring.length;
        double[] all = new double[n];
        int i = 0;
        for (double[] ring : ys) {
            System.arraycopy(ring, 0, all, i, ring.length);
            i += ring.length;
        }
        Arrays.sort(all);
        int m = 0;
        for (int j = 0; j < n; j++) {
            if (m == 0 || all[j] != all[m - 1]) all[m++] = all[j];
        }
        return Arrays.copyOf(all, m);
    }

    // a point on the scan line midway between two consecutive vertex y coordinates lo < hi, or null
    private static double[] onScanLine(double[][] xs, double[][] ys, double lo, double hi) {
        double y = 0.5 * lo + 0.5 * hi;
        if (!(lo < y && y < hi)) return null;     // lo and hi are adjacent doubles
        // the approximate crossings: no vertex lies on the scan line, so an edge crosses it iff its ends are on
        // either side
        double[] crossings = new double[16];
        int count = 0;
        for (int r = 0; r < xs.length; r++) {
            double[] rx = xs[r], ry = ys[r];
            for (int i = 0; i + 1 < rx.length; i++) {
                double ay = ry[i], by = ry[i + 1];
                if ((ay < y) == (by < y)) continue;
                double ax = rx[i], bx = rx[i + 1];
                double x = ax + (y - ay) / (by - ay) * (bx - ax);
                if (count == crossings.length) crossings = Arrays.copyOf(crossings, 2 * count);
                crossings[count++] = Math.min(Math.max(x, Math.min(ax, bx)), Math.max(ax, bx));
            }
        }
        Arrays.sort(crossings, 0, count);
        // the intervals between crossings 2k and 2k + 1, widest first
        int intervals = count / 2;
        Integer[] order = new Integer[intervals];
        for (int k = 0; k < intervals; k++) order[k] = k;
        final double[] c = crossings;
        Arrays.sort(order, (p, q) -> Double.compare(c[2 * q + 1] - c[2 * q], c[2 * p + 1] - c[2 * p]));
        for (int k = 0; k < Math.min(intervals, MAX_INTERVALS); k++) {
            double left = crossings[2 * order[k]], right = crossings[2 * order[k] + 1];
            double x = 0.5 * left + 0.5 * right;
            if (Double.isFinite(x) && isInterior(xs, ys, x, y)) return new double[]{x, y, right - left};
        }
        return null;
    }

    /**
     * Whether (x, y) lies in the interior of the polygon, by the parity of the edges that cross the horizontal line
     * through it to its left; exact, for a y that is not the y coordinate of a vertex.
     */
    private static boolean isInterior(double[][] xs, double[][] ys, double x, double y) {
        boolean inside = false;
        for (int r = 0; r < xs.length; r++) {
            double[] rx = xs[r], ry = ys[r];
            for (int i = 0; i + 1 < rx.length; i++) {
                double ay = ry[i], by = ry[i + 1];
                if ((ay < y) == (by < y)) continue;
                // the edge, directed upwards, passes to the left of (x, y) iff (x, y) lies to its right
                int o = ay < by
                        ? Predicates.orientation(rx[i], ay, rx[i + 1], by, x, y)
                        : Predicates.orientation(rx[i + 1], by, rx[i], ay, x, y);
                if (o == 0) return false;   // on the edge
                if (o < 0) inside = !inside;
            }
        }
        return inside;
    }

    // ---------------------------------------------------------------------------------------------------------
    // lines and points
    // ---------------------------------------------------------------------------------------------------------

    // the vertex nearest to the centroid that is not an endpoint of an open line; else the nearest endpoint
    private static double[] onLines(List<PositionSequence<?>> lines, double[] centroid) {
        double[] interior = null, end = null;
        double interiorDistance = Double.POSITIVE_INFINITY, endDistance = Double.POSITIVE_INFINITY;
        for (PositionSequence<?> line : lines) {
            int n = line.size();
            double[] xs = new double[n], ys = new double[n];
            DouglasPeucker.coordinates(line, xs, ys);
            boolean closed = n > 1 && xs[0] == xs[n - 1] && ys[0] == ys[n - 1];
            for (int i = 0; i < n; i++) {
                double d = squaredDistance(xs[i], ys[i], centroid);
                if (closed || (i > 0 && i < n - 1)) {
                    if (d < interiorDistance) {
                        interiorDistance = d;
                        interior = new double[]{xs[i], ys[i]};
                    }
                } else if (d < endDistance) {
                    endDistance = d;
                    end = new double[]{xs[i], ys[i]};
                }
            }
        }
        return interior != null ? interior : end;
    }

    private static double[] nearest(List<PositionSequence<?>> points, double[] centroid) {
        double[] best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (PositionSequence<?> point : points) {
            double[] xs = new double[1], ys = new double[1];
            DouglasPeucker.coordinates(point, xs, ys);
            double d = squaredDistance(xs[0], ys[0], centroid);
            if (best == null || d < bestDistance) {
                bestDistance = d;
                best = new double[]{xs[0], ys[0]};
            }
        }
        return best;
    }

    private static double squaredDistance(double x, double y, double[] c) {
        double dx = x - c[0], dy = y - c[1];
        return dx * dx + dy * dy;
    }
}
