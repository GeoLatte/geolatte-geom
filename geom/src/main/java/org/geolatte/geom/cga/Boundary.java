package org.geolatte.geom.cga;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The boundary of a geometry, as defined by the Simple Feature Access specification and SQL/MM
 * ({@code ST_Boundary}). Only the X and Y coordinates are used to decide which positions are on the boundary; the
 * returned positions are those of the geometry, with their other coordinates.
 *
 * <ul>
 *     <li>The boundary of a point or multipoint is empty: an empty {@code GeometryCollection}.</li>
 *     <li>The boundary of a line string is a {@code MultiPoint} with its start and end point, or an empty one if
 *     the line string is closed.</li>
 *     <li>The boundary of a multi line string follows the Mod-2 rule: it is a {@code MultiPoint} with the
 *     positions that are an endpoint of an odd number of its elements (a closed element has the same endpoint
 *     twice), in the order in which they first occur.</li>
 *     <li>The boundary of a polygon is a {@code LineString} with its exterior ring if it has no holes, and a
 *     {@code MultiLineString} with all its rings otherwise. The boundary of a multipolygon is a
 *     {@code MultiLineString} with the rings of all its polygons.</li>
 *     <li>The boundary of an empty geometry is an empty geometry of the type above.</li>
 * </ul>
 *
 * <p>No coordinates are computed: the result consists of positions of the geometry, compared exactly. The
 * boundary of a polygon or multipolygon is the set of its rings if it is valid (see {@link Validity}).</p>
 */
public final class Boundary {

    private Boundary() {
    }

    /**
     * @return the boundary of the geometry
     * @throws IllegalArgumentException for a {@code GeometryCollection} that is not a multipoint, multi line string
     *                                  or multipolygon: its boundary is not defined element by element
     */
    @SuppressWarnings("unchecked")
    public static <P extends Position> Geometry<P> of(Geometry<P> geometry) {
        CoordinateReferenceSystem<P> crs = geometry.getCoordinateReferenceSystem();
        if (geometry instanceof Point || geometry instanceof MultiPoint) {
            return Geometries.mkEmptyGeometryCollection(crs);
        }
        if (geometry instanceof LineString) {
            return endpoints(java.util.Collections.singletonList((LineString<P>) geometry), crs);
        }
        if (geometry instanceof MultiLineString) {
            MultiLineString<P> lines = (MultiLineString<P>) geometry;
            List<LineString<P>> list = new ArrayList<>();
            for (int i = 0; i < lines.getNumGeometries(); i++) list.add(lines.getGeometryN(i));
            return endpoints(list, crs);
        }
        if (geometry instanceof Polygon) {
            Polygon<P> polygon = (Polygon<P>) geometry;
            if (polygon.isEmpty()) return Geometries.mkEmptyMultiLineString(crs);
            if (polygon.getNumInteriorRing() == 0) return lineString(polygon.getExteriorRing());
            List<LineString<P>> rings = new ArrayList<>();
            addRings(polygon, rings);
            return Geometries.mkMultiLineString(rings);
        }
        if (geometry instanceof MultiPolygon) {
            List<LineString<P>> rings = new ArrayList<>();
            MultiPolygon<P> polygons = (MultiPolygon<P>) geometry;
            for (int i = 0; i < polygons.getNumGeometries(); i++) addRings(polygons.getGeometryN(i), rings);
            return rings.isEmpty() ? Geometries.mkEmptyMultiLineString(crs) : Geometries.mkMultiLineString(rings);
        }
        throw new IllegalArgumentException("The boundary of a " + geometry.getGeometryType() + " is not supported");
    }

    private static <P extends Position> LineString<P> lineString(LineString<P> ring) {
        return new LineString<>(ring.getPositions(), ring.getCoordinateReferenceSystem());
    }

    private static <P extends Position> void addRings(Polygon<P> polygon, List<LineString<P>> rings) {
        if (polygon.isEmpty()) return;
        rings.add(lineString(polygon.getExteriorRing()));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) rings.add(lineString(polygon.getInteriorRingN(i)));
    }

    // the Mod-2 boundary of the lines
    private static <P extends Position> MultiPoint<P> endpoints(List<LineString<P>> lines,
                                                                CoordinateReferenceSystem<P> crs) {
        Map<PointLocator.XY, P> first = new LinkedHashMap<>();
        Map<PointLocator.XY, Integer> count = new LinkedHashMap<>();
        for (LineString<P> line : lines) {
            if (line.isEmpty()) continue;
            for (P p : endpointsOf(line)) {
                PointLocator.XY key = new PointLocator.XY(p.getCoordinate(0) + 0.0, p.getCoordinate(1) + 0.0);
                first.putIfAbsent(key, p);
                count.merge(key, 1, Integer::sum);
            }
        }
        List<Point<P>> points = new ArrayList<>();
        for (Map.Entry<PointLocator.XY, P> e : first.entrySet()) {
            if (count.get(e.getKey()) % 2 == 1) points.add(new Point<>(e.getValue(), crs));
        }
        return points.isEmpty() ? Geometries.mkEmptyMultiPoint(crs) : Geometries.mkMultiPoint(points);
    }

    private static <P extends Position> List<P> endpointsOf(LineString<P> line) {
        List<P> result = new ArrayList<>(2);
        result.add(line.getStartPosition());
        result.add(line.getEndPosition());
        return result;
    }
}
