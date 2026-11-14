package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.cga.exact.PolygonMoments;

/**
 * The correctly rounded area of a geometry, in the plane of the first two coordinates.
 *
 * <p>The area of a polygon is the area enclosed by its shell, minus the areas enclosed by its holes, whatever the
 * orientation of the rings. The area of a multipolygon or geometry collection is the sum of the areas of its
 * polygons; points and line strings have no area. For valid polygons and multipolygons, this is the area of the
 * point set. Overlapping polygons in a geometry collection (or in an invalid multipolygon) are counted once for
 * each polygon, as in JTS.</p>
 *
 * <p>The area is a rational function of the coordinates. The result is the double nearest to the exact area (round
 * to nearest, ties to even), computed with {@link PolygonMoments}. It does not depend on the order of the polygons,
 * of the rings or of the positions, nor on the orientation of the rings.</p>
 */
public final class Area {

    private Area() {
    }

    /**
     * @return the area of the geometry, correctly rounded
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static double of(Geometry<?> geometry) {
        PolygonMoments moments = new PolygonMoments(false);
        addPolygons(geometry, moments);
        return moments.area();
    }

    /**
     * Computes the area in plain floating point arithmetic: for each ring, the shoelace formula
     * {@code sum (x_i - x_0) (y_(i-1) - y_(i+1)) / 2}, with the X coordinates relative to the first position.
     *
     * <p>This is about ten times faster than {@link #of(Geometry)}, but not exact: the error is not bounded, and it
     * can be large for thin or nearly degenerate polygons. The definition of the area is the same as for
     * {@link #of(Geometry)}. A NaN coordinate gives a NaN area.</p>
     *
     * @return the area of the geometry, approximately
     */
    public static double fast(Geometry<?> geometry) {
        if (geometry.isEmpty()) return 0;
        if (geometry instanceof Polygon) {
            Polygon<?> polygon = (Polygon<?>) geometry;
            double area = Math.abs(fastSignedArea(polygon.getExteriorRing().getPositions()));
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                area -= Math.abs(fastSignedArea(polygon.getInteriorRingN(i).getPositions()));
            }
            return area;
        }
        double area = 0;
        if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) area += fast(collection.getGeometryN(i));
        }
        return area;
    }

    // the signed area of a closed ring in floating point
    private static double fastSignedArea(PositionSequence<?> ring) {
        int n = ring.size();
        if (n < 3) return 0;
        double[] c = new double[Math.max(2, ring.getCoordinateDimension())];
        ring.getCoordinates(0, c);
        double x0 = c[0];
        double yPrev = c[1];
        ring.getCoordinates(1, c);
        double x = c[0] - x0, y = c[1];
        double sum = 0;
        for (int i = 1; i < n - 1; i++) {
            ring.getCoordinates(i + 1, c);
            double yNext = c[1];
            sum += x * (yPrev - yNext);
            yPrev = y;
            x = c[0] - x0;
            y = yNext;
        }
        return sum / 2;
    }

    // adds the rings of all polygons in the geometry
    static void addPolygons(Geometry<?> geometry, PolygonMoments moments) {
        if (geometry.isEmpty()) return;
        if (geometry instanceof Polygon) {
            Polygon<?> polygon = (Polygon<?>) geometry;
            addRing(polygon.getExteriorRing(), false, moments);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                addRing(polygon.getInteriorRingN(i), true, moments);
            }
        } else if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                addPolygons(collection.getGeometryN(i), moments);
            }
        }
    }

    private static void addRing(LineString<?> ring, boolean hole, PolygonMoments moments) {
        if (ring.isEmpty()) return;
        PositionSequence<?> positions = ring.getPositions();
        int n = positions.size();
        double[] xs = new double[n], ys = new double[n];
        DouglasPeucker.coordinates(positions, xs, ys);
        moments.addRing(xs, ys, n, hole);
    }
}
