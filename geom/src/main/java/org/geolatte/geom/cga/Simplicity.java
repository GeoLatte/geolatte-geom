package org.geolatte.geom.cga;

import org.geolatte.geom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exact test of the simplicity of a geometry, as defined by the Simple Feature Access specification and SQL/MM
 * ({@code ST_IsSimple}). Only the X and Y coordinates are used.
 *
 * <ul>
 *     <li>A point is simple. A multipoint is simple if no two of its points are equal.</li>
 *     <li>A line string is simple if it does not pass through the same point twice, except that its start point
 *     may be its end point (a closed line string).</li>
 *     <li>A multi line string is simple if its elements are simple, and two elements only meet in points that are
 *     in the boundary of both: an endpoint of each, which are not closed.</li>
 *     <li>A polygon or multipolygon is simple if each of its rings is simple (for a valid polygon, this is always
 *     the case).</li>
 *     <li>A geometry collection is simple if each of its elements is simple.</li>
 *     <li>An empty geometry is simple.</li>
 * </ul>
 *
 * <p>Repeated consecutive positions are ignored; a line string whose positions are all equal is simple. The
 * result is exact for the given double coordinates: all segment intersections are classified with exact
 * predicates, without computing intersection points.</p>
 */
public final class Simplicity {

    private Simplicity() {
    }

    /**
     * @return true if the geometry is simple
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static boolean isSimple(Geometry<?> geometry) {
        if (geometry.isEmpty() || geometry instanceof Point) return true;
        if (geometry instanceof MultiPoint) {
            Set<PointLocator.XY> seen = new HashSet<>();
            MultiPoint<?> points = (MultiPoint<?>) geometry;
            for (int i = 0; i < points.getNumGeometries(); i++) {
                Point<?> p = points.getGeometryN(i);
                if (p.isEmpty()) continue;
                double[][] c = GeometryElements.distinct(p.getPositions());
                if (!seen.add(new PointLocator.XY(c[0][0], c[1][0]))) return false;
            }
            return true;
        }
        if (geometry instanceof LineString) {
            return linesSimple(chains(geometry));
        }
        if (geometry instanceof MultiLineString) {
            List<double[][]> lines = new ArrayList<>();
            MultiLineString<?> multi = (MultiLineString<?>) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) lines.addAll(chains(multi.getGeometryN(i)));
            return linesSimple(lines);
        }
        if (geometry instanceof Polygon) {
            return ringsSimple(rings((Polygon<?>) geometry, new ArrayList<>()));
        }
        if (geometry instanceof MultiPolygon) {
            List<double[][]> rings = new ArrayList<>();
            MultiPolygon<?> multi = (MultiPolygon<?>) geometry;
            for (int i = 0; i < multi.getNumGeometries(); i++) rings(multi.getGeometryN(i), rings);
            return ringsSimple(rings);
        }
        if (geometry instanceof AbstractGeometryCollection) {
            AbstractGeometryCollection<?, ?> collection = (AbstractGeometryCollection<?, ?>) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                if (!isSimple(collection.getGeometryN(i))) return false;
            }
            return true;
        }
        throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
    }

    // the coordinates of a line, if it has at least two distinct positions
    private static List<double[][]> chains(Geometry<?> line) {
        List<double[][]> result = new ArrayList<>(1);
        if (line.isEmpty()) return result;
        double[][] c = GeometryElements.distinct(line.getPositions());
        if (c[0].length >= 2) result.add(c);
        return result;
    }

    private static List<double[][]> rings(Polygon<?> polygon, List<double[][]> rings) {
        if (polygon.isEmpty()) return rings;
        rings.addAll(chains(polygon.getExteriorRing()));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) rings.addAll(chains(polygon.getInteriorRingN(i)));
        return rings;
    }

    private static boolean isClosed(double[][] c) {
        int n = c[0].length - 1;
        return c[0][0] == c[0][n] && c[1][0] == c[1][n];
    }

    // are the lines simple, and do they only meet in endpoints of both that are not closed?
    static boolean linesSimple(List<double[][]> lines) {
        int count = lines.size();
        boolean[] closed = new boolean[count];
        for (int k = 0; k < count; k++) closed[k] = isClosed(lines.get(k));
        boolean violated = SegmentContacts.find(lines, (a, i, b, j, kind) -> {
            if (a == b) return selfContact(lines.get(a), closed[a], i, j, kind);
            if (kind != SegmentIntersection.TOUCHING || closed[a] || closed[b]) return true;
            return !touchAtEndpoints(lines.get(a), i, lines.get(b), j);
        });
        return !violated;
    }

    // are the rings simple? Contacts between different rings are not considered
    private static boolean ringsSimple(List<double[][]> rings) {
        boolean violated = SegmentContacts.find(rings, (a, i, b, j, kind) ->
                a == b && selfContact(rings.get(a), true, i, j, kind));
        return !violated;
    }

    // is the contact of segments i and j of the same line or ring a violation of its simplicity?
    static boolean selfContact(double[][] c, boolean closed, int i, int j, SegmentIntersection kind) {
        if (SegmentContacts.consecutive(i, j, c[0].length - 1, closed)) return kind == SegmentIntersection.OVERLAPPING;
        return true;
    }

    // for touching segments i of line a and j of line b: is the common point an endpoint of both lines?
    private static boolean touchAtEndpoints(double[][] a, int i, double[][] b, int j) {
        int na = a[0].length - 1, nb = b[0].length - 1;
        for (int ea : endpointsAt(i, na)) {
            for (int eb : endpointsAt(j, nb)) {
                if (a[0][ea] == b[0][eb] && a[1][ea] == b[1][eb]) return true;
            }
        }
        return false;
    }

    // the endpoints of a line (vertex 0 or n) that are vertices of segment i
    private static int[] endpointsAt(int i, int n) {
        if (n == 1) return new int[]{0, 1};
        if (i == 0) return new int[]{0};
        if (i == n - 1) return new int[]{n};
        return new int[0];
    }
}
