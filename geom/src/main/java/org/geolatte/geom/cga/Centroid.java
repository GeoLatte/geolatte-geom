package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.PolygonMoments;
import org.geolatte.geom.cga.exact.Summation;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * The centroid of a geometry, in the plane of the first two coordinates.
 *
 * <p>As in JTS, the centroid is that of the parts of the highest dimension that have a non-zero measure:</p>
 * <ul>
 *     <li>if the polygons of the geometry have a non-zero area (see {@link Area}), the centroid of that area. Shells
 *     add area and holes subtract it, whatever the orientation of the rings. Each coordinate is <b>correctly
 *     rounded</b>: the double nearest to the exact (rational) coordinate, computed with {@link PolygonMoments};</li>
 *     <li>otherwise, if the line strings and polygon rings have a non-zero length, the centroid of the segments,
 *     weighted by their length. Lengths are square roots, which are rounded: the sums are exact for the rounded
 *     lengths, so that the result is very accurate, but not necessarily correctly rounded;</li>
 *     <li>otherwise, the average of the points, where a line string or ring of zero length counts as its first
 *     point. Each coordinate is correctly rounded.</li>
 * </ul>
 *
 * <p>The centroid of an empty geometry is an empty point. The result has the coordinate reference system of the
 * geometry; coordinates other than X and Y (Z, M) are NaN.</p>
 */
public final class Centroid {

    private Centroid() {
    }

    /**
     * @return the centroid of the geometry
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static <P extends Position> Point<P> of(Geometry<P> geometry) {
        CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
        double[] xy = of2D(geometry);
        if (xy == null) return new Point<>(crs);
        double[] coordinates = new double[crs.getCoordinateDimension()];
        java.util.Arrays.fill(coordinates, Double.NaN);
        coordinates[0] = xy[0];
        coordinates[1] = xy[1];
        return new Point<>(Positions.mkPosition(crs, coordinates), crs);
    }

    /**
     * @return the X and Y coordinate of the centroid of the geometry, or null if it is empty
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static double[] of2D(Geometry<?> geometry) {
        PolygonMoments moments = new PolygonMoments(true);
        Area.addPolygons(geometry, moments);
        if (moments.areaSignum() != 0) return moments.centroid();
        Weights weights = new Weights();
        weights.add(geometry);
        return weights.centroid();
    }

    // the length-weighted midpoints of the segments, and the points
    private static final class Weights {
        final Summation lineX = new Summation(), lineY = new Summation(), length = new Summation();
        final Summation pointX = new Summation(), pointY = new Summation();
        int points = 0;

        void add(Geometry<?> geometry) {
            if (geometry.isEmpty()) return;
            if (geometry instanceof Point) {
                double[][] c = coordinates(geometry.getPositions());
                addPoint(c[0][0], c[1][0]);
            } else if (geometry instanceof LineString) {
                addLine(geometry.getPositions());
            } else if (geometry instanceof Polygon) {
                Polygon<?> polygon = (Polygon<?>) geometry;
                addLine(polygon.getExteriorRing().getPositions());
                for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                    addLine(polygon.getInteriorRingN(i).getPositions());
                }
            } else if (geometry instanceof AbstractGeometryCollection) {
                AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
                for (int i = 0; i < collection.getNumGeometries(); i++) add(collection.getGeometryN(i));
            }
        }

        void addPoint(double x, double y) {
            pointX.add(x);
            pointY.add(y);
            points++;
        }

        void addLine(PositionSequence<?> positions) {
            if (positions.isEmpty()) return;
            double[][] c = coordinates(positions);
            double[] xs = c[0], ys = c[1];
            boolean zeroLength = true;
            for (int i = 0; i + 1 < xs.length; i++) {
                double len = Math.hypot(xs[i + 1] - xs[i], ys[i + 1] - ys[i]);
                if (len == 0) continue;
                zeroLength = false;
                // the midpoint (x_i + x_(i+1)) / 2, weighted by len; the factor 2 is in the denominator
                lineX.addProduct(len, xs[i]).addProduct(len, xs[i + 1]);
                lineY.addProduct(len, ys[i]).addProduct(len, ys[i + 1]);
                length.addProduct(len, 2);
            }
            if (zeroLength) addPoint(xs[0], ys[0]);
        }

        double[] centroid() {
            if (length.signum() > 0) {
                return new double[]{Summation.roundQuotient(lineX, length) + 0.0,
                        Summation.roundQuotient(lineY, length) + 0.0};
            }
            if (points > 0) {
                Summation count = new Summation().add(points);
                return new double[]{Summation.roundQuotient(pointX, count) + 0.0,
                        Summation.roundQuotient(pointY, count) + 0.0};
            }
            return null;
        }

        private static double[][] coordinates(PositionSequence<?> positions) {
            int n = positions.size();
            double[] xs = new double[n], ys = new double[n];
            DouglasPeucker.coordinates(positions, xs, ys);
            return new double[][]{xs, ys};
        }
    }
}
