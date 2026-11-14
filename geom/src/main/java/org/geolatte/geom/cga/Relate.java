package org.geolatte.geom.cga;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.cga.RelateComputer.Operand;

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
 *
 * <p>To test one geometry against many others, use {@link PreparedRelate}.</p>
 */
public final class Relate {

    private Relate() {
    }

    private static Operand operand(Geometry<?> geometry) {
        return new Operand(GeometryElements.of(geometry));
    }

    /**
     * @return the DE-9IM matrix of the geometries
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static IntersectionMatrix relate(Geometry<?> a, Geometry<?> b) {
        return relate(operand(a), operand(b));
    }

    /**
     * @param pattern a DE-9IM pattern, see {@link IntersectionMatrix#matches(String)}
     * @return true if the DE-9IM matrix of the geometries matches the pattern
     */
    public static boolean relate(Geometry<?> a, Geometry<?> b, String pattern) {
        return relate(a, b).matches(pattern);
    }

    /**
     * @return true if the geometries are topologically equal (they are the same point set); two empty geometries
     * are equal
     */
    public static boolean equalsTopologically(Geometry<?> a, Geometry<?> b) {
        return equalsTopologically(operand(a), operand(b));
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
        return intersects(operand(a), operand(b));
    }

    /**
     * @return true if the geometries have a point in common, but their interiors do not intersect
     * (see {@link IntersectionMatrix#isTouches()})
     */
    public static boolean touches(Geometry<?> a, Geometry<?> b) {
        return touches(operand(a), operand(b));
    }

    /**
     * @return true if a crosses b (see {@link IntersectionMatrix#isCrosses()})
     */
    public static boolean crosses(Geometry<?> a, Geometry<?> b) {
        return crosses(operand(a), operand(b));
    }

    /**
     * @return true if a lies within b (see {@link IntersectionMatrix#isWithin()})
     */
    public static boolean within(Geometry<?> a, Geometry<?> b) {
        return within(operand(a), operand(b));
    }

    /**
     * @return true if a contains b (see {@link IntersectionMatrix#isContains()})
     */
    public static boolean contains(Geometry<?> a, Geometry<?> b) {
        return contains(operand(a), operand(b));
    }

    /**
     * @return true if the geometries overlap (see {@link IntersectionMatrix#isOverlaps()})
     */
    public static boolean overlaps(Geometry<?> a, Geometry<?> b) {
        return overlaps(operand(a), operand(b));
    }

    /**
     * @return true if every point of b is a point of a, and b is not empty
     */
    public static boolean covers(Geometry<?> a, Geometry<?> b) {
        return covers(operand(a), operand(b));
    }

    /**
     * @return true if every point of a is a point of b, and a is not empty
     */
    public static boolean coveredBy(Geometry<?> a, Geometry<?> b) {
        return coveredBy(operand(a), operand(b));
    }

    // ---------------------------------------------------------------------------------------------------------
    // on operands, shared with PreparedRelate
    // ---------------------------------------------------------------------------------------------------------

    static IntersectionMatrix relate(Operand a, Operand b) {
        return new RelateComputer(a, b, RelateComputer.Predicate.NONE).compute();
    }

    // the predicate, decided early if possible, otherwise from the matrix
    private static boolean evaluate(Operand a, Operand b, RelateComputer.Predicate predicate,
                                    java.util.function.Predicate<IntersectionMatrix> test) {
        RelateComputer computer = new RelateComputer(a, b, predicate);
        IntersectionMatrix matrix = computer.compute();
        return matrix == null ? computer.decided : test.test(matrix);
    }

    // the location (EXTERIOR, ON or INSIDE, see GeometryElements.roughLocation) in g of a position of the other
    // geometry. Every point of a geometry is a limit of points of its interior, so INSIDE means that the
    // interiors intersect.
    private static int roughLocation(Operand other, Operand g) {
        double[] p = other.elements.anyPosition();
        return g.roughLocation(p[0], p[1]);
    }

    static boolean equalsTopologically(Operand a, Operand b) {
        GeometryElements ea = a.elements, eb = b.elements;
        if (ea.isEmpty() || eb.isEmpty()) return ea.isEmpty() && eb.isEmpty();
        if (ea.minX != eb.minX || ea.minY != eb.minY || ea.maxX != eb.maxX || ea.maxY != eb.maxY) return false;
        if (roughLocation(a, b) == GeometryElements.EXTERIOR) return false;
        if (roughLocation(b, a) == GeometryElements.EXTERIOR) return false;
        return evaluate(a, b, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isEquals);
    }

    static boolean intersects(Operand a, Operand b) {
        if (!a.elements.envelopeIntersects(b.elements)) return false;
        if (roughLocation(a, b) != GeometryElements.EXTERIOR) return true;
        if (roughLocation(b, a) != GeometryElements.EXTERIOR) return true;
        return evaluate(a, b, RelateComputer.Predicate.INTERSECTS, IntersectionMatrix::isIntersects);
    }

    static boolean touches(Operand a, Operand b) {
        if (a.elements.dimension == 0 && b.elements.dimension == 0) return false;
        if (!a.elements.envelopeIntersects(b.elements)) return false;
        if (roughLocation(a, b) == GeometryElements.INSIDE) return false;
        if (roughLocation(b, a) == GeometryElements.INSIDE) return false;
        return evaluate(a, b, RelateComputer.Predicate.TOUCHES, IntersectionMatrix::isTouches);
    }

    static boolean crosses(Operand a, Operand b) {
        if (a.elements.dimension == b.elements.dimension && a.elements.dimension != 1) return false;
        return a.elements.envelopeIntersects(b.elements)
                && evaluate(a, b, RelateComputer.Predicate.CROSSES, IntersectionMatrix::isCrosses);
    }

    static boolean within(Operand a, Operand b) {
        if (!b.elements.envelopeContains(a.elements) || roughLocation(a, b) == GeometryElements.EXTERIOR) return false;
        return evaluate(a, b, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isWithin);
    }

    static boolean contains(Operand a, Operand b) {
        if (!a.elements.envelopeContains(b.elements) || roughLocation(b, a) == GeometryElements.EXTERIOR) return false;
        return evaluate(a, b, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isContains);
    }

    static boolean overlaps(Operand a, Operand b) {
        if (a.elements.dimension != b.elements.dimension) return false;
        return a.elements.envelopeIntersects(b.elements)
                && evaluate(a, b, RelateComputer.Predicate.OVERLAPS, IntersectionMatrix::isOverlaps);
    }

    static boolean covers(Operand a, Operand b) {
        if (!a.elements.envelopeContains(b.elements) || roughLocation(b, a) == GeometryElements.EXTERIOR) return false;
        return evaluate(a, b, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isCovers);
    }

    static boolean coveredBy(Operand a, Operand b) {
        if (!b.elements.envelopeContains(a.elements) || roughLocation(a, b) == GeometryElements.EXTERIOR) return false;
        return evaluate(a, b, RelateComputer.Predicate.CONTAINMENT, IntersectionMatrix::isCoveredBy);
    }
}
