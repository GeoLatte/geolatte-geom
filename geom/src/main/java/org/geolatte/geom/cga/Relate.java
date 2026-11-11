package org.geolatte.geom.cga;

import org.geolatte.geom.Geometry;

/**
 * Exact computation of the spatial relationship (the DE-9IM {@link IntersectionMatrix}) between two geometries,
 * and of the named spatial predicates of the OGC Simple Feature Access specification.
 *
 * <p>The result is exact for the given double coordinates: no intersection point is ever rounded to decide the
 * topology (see {@link RelateComputer} for the algorithm).</p>
 *
 * <p>Geometries have union semantics: a geometry is the union of its elements (points, lines and polygons), also
 * in a {@code GeometryCollection} with members of different dimensions, and with members that overlap or touch.
 * Adjacent or overlapping polygons merge into one area, lines and points inside an area are part of its interior,
 * and points on a line are part of the line. The location of a point is determined by the elements of the
 * highest dimension that contain it, as described for {@link PointLocator}. For geometries that are valid
 * according to the Simple Feature Access specification, this coincides with the usual definitions. Line
 * boundaries follow the Mod-2 rule. The individual polygons must be valid.</p>
 *
 * <p>Only the X and Y coordinates are used; the coordinate reference systems are not checked.</p>
 */
public final class Relate {

    private Relate() {
    }

    /**
     * @return the DE-9IM matrix of the geometries
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static IntersectionMatrix relate(Geometry<?> a, Geometry<?> b) {
        return relate(GeometryElements.of(a), GeometryElements.of(b));
    }

    /**
     * @param pattern a DE-9IM pattern, see {@link IntersectionMatrix#matches(String)}
     * @return true if the DE-9IM matrix of the geometries matches the pattern
     */
    public static boolean relate(Geometry<?> a, Geometry<?> b, String pattern) {
        return relate(a, b).matches(pattern);
    }

    private static IntersectionMatrix relate(GeometryElements a, GeometryElements b) {
        return new RelateComputer(a, b).compute();
    }

    // the predicate, decided early if possible, otherwise from the matrix
    private static boolean evaluate(GeometryElements a, GeometryElements b, RelateComputer.Predicate predicate,
                                    java.util.function.Predicate<IntersectionMatrix> test) {
        RelateComputer computer = new RelateComputer(a, b, predicate);
        IntersectionMatrix matrix = computer.compute();
        return matrix == null ? computer.decided : test.test(matrix);
    }

    // the location (EXTERIOR, ON or INSIDE, see GeometryElements.roughLocation) in g of a position of the other
    // geometry. Every point of a geometry is a limit of points of its interior, so INSIDE means that the
    // interiors intersect.
    private static int roughLocation(GeometryElements other, GeometryElements g) {
        double[] p = other.anyPosition();
        return g.roughLocation(p[0], p[1]);
    }

    /**
     * @return true if the geometries are topologically equal (they are the same point set); two empty geometries
     * are equal
     */
    public static boolean equalsTopologically(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (ea.isEmpty() || eb.isEmpty()) return ea.isEmpty() && eb.isEmpty();
        if (ea.minX != eb.minX || ea.minY != eb.minY || ea.maxX != eb.maxX || ea.maxY != eb.maxY) return false;
        if (roughLocation(ea, eb) == GeometryElements.EXTERIOR) return false;
        if (roughLocation(eb, ea) == GeometryElements.EXTERIOR) return false;
        return evaluate(ea, eb, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isEquals);
    }

    /**
     * @return true if the geometries have no point in common
     */
    public static boolean disjoint(Geometry<?> a, Geometry<?> b) {
        return !intersects(a, b);
    }

    /**
     * @return true if the geometries have at least one point in common
     */
    public static boolean intersects(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (!ea.envelopeIntersects(eb)) return false;
        if (roughLocation(ea, eb) != GeometryElements.EXTERIOR) return true;
        if (roughLocation(eb, ea) != GeometryElements.EXTERIOR) return true;
        return evaluate(ea, eb, RelateComputer.Predicate.INTERSECTS, IntersectionMatrix::isIntersects);
    }

    /**
     * @return true if the geometries have a point in common, but their interiors do not intersect
     * (see {@link IntersectionMatrix#isTouches()})
     */
    public static boolean touches(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (ea.dimension == 0 && eb.dimension == 0) return false;
        if (!ea.envelopeIntersects(eb)) return false;
        if (roughLocation(ea, eb) == GeometryElements.INSIDE) return false;
        if (roughLocation(eb, ea) == GeometryElements.INSIDE) return false;
        return evaluate(ea, eb, RelateComputer.Predicate.TOUCHES, IntersectionMatrix::isTouches);
    }

    /**
     * @return true if a crosses b (see {@link IntersectionMatrix#isCrosses()})
     */
    public static boolean crosses(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (ea.dimension == eb.dimension && ea.dimension != 1) return false;
        return ea.envelopeIntersects(eb)
                && evaluate(ea, eb, RelateComputer.Predicate.CROSSES, IntersectionMatrix::isCrosses);
    }

    /**
     * @return true if a lies within b (see {@link IntersectionMatrix#isWithin()})
     */
    public static boolean within(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (!eb.envelopeContains(ea) || roughLocation(ea, eb) == GeometryElements.EXTERIOR) return false;
        return evaluate(ea, eb, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isWithin);
    }

    /**
     * @return true if a contains b (see {@link IntersectionMatrix#isContains()})
     */
    public static boolean contains(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (!ea.envelopeContains(eb) || roughLocation(eb, ea) == GeometryElements.EXTERIOR) return false;
        return evaluate(ea, eb, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isContains);
    }

    /**
     * @return true if the geometries overlap (see {@link IntersectionMatrix#isOverlaps()})
     */
    public static boolean overlaps(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (ea.dimension != eb.dimension) return false;
        return ea.envelopeIntersects(eb)
                && evaluate(ea, eb, RelateComputer.Predicate.OVERLAPS, IntersectionMatrix::isOverlaps);
    }

    /**
     * @return true if every point of b is a point of a, and b is not empty
     */
    public static boolean covers(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (!ea.envelopeContains(eb) || roughLocation(eb, ea) == GeometryElements.EXTERIOR) return false;
        return evaluate(ea, eb, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isCovers);
    }

    /**
     * @return true if every point of a is a point of b, and a is not empty
     */
    public static boolean coveredBy(Geometry<?> a, Geometry<?> b) {
        GeometryElements ea = GeometryElements.of(a), eb = GeometryElements.of(b);
        if (!eb.envelopeContains(ea) || roughLocation(ea, eb) == GeometryElements.EXTERIOR) return false;
        return evaluate(ea, eb, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isCoveredBy);
    }
}
