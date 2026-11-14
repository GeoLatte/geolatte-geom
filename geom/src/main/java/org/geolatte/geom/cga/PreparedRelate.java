package org.geolatte.geom.cga;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.cga.RelateComputer.Operand;

/**
 * A geometry prepared for computing its spatial relationship with many other geometries, as {@link Relate} does:
 * exactly, and with union semantics.
 *
 * <p>Preparing computes, once, everything that depends only on this geometry: its elements, the monotone chains
 * of its lines and rings in an R-tree, the points where its own elements meet, and an index for point location.
 * Each test then costs roughly in proportion to the size of the other geometry and the number of points where the
 * two meet, plus a logarithmic factor for each lookup in the prepared geometry. The prepared geometry is the
 * first argument of each predicate: {@code PreparedRelate.of(a).within(b)} is {@code Relate.within(a, b)}.</p>
 *
 * <p>Instances are immutable and can be used by several threads at the same time.</p>
 */
public final class PreparedRelate {

    private final Geometry<?> geometry;
    private final Operand operand;

    private PreparedRelate(Geometry<?> geometry) {
        this.geometry = geometry;
        this.operand = new Operand(GeometryElements.of(geometry)).prepare();
    }

    /**
     * @return the geometry, prepared
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public static PreparedRelate of(Geometry<?> geometry) {
        return new PreparedRelate(geometry);
    }

    /**
     * @return the prepared geometry
     */
    public Geometry<?> getGeometry() {
        return geometry;
    }

    private static Operand operand(Geometry<?> other) {
        return new Operand(GeometryElements.of(other));
    }

    /**
     * @return the DE-9IM matrix of the prepared geometry and the other one
     * @throws IllegalArgumentException if a coordinate is NaN or infinite
     */
    public IntersectionMatrix relate(Geometry<?> other) {
        return Relate.relate(operand, operand(other));
    }

    /**
     * @param pattern a DE-9IM pattern, see {@link IntersectionMatrix#matches(String)}
     * @return true if the DE-9IM matrix of the prepared geometry and the other one matches the pattern
     */
    public boolean relate(Geometry<?> other, String pattern) {
        return relate(other).matches(pattern);
    }

    /**
     * @see Relate#equalsTopologically(Geometry, Geometry)
     */
    public boolean equalsTopologically(Geometry<?> other) {
        return Relate.equalsTopologically(operand, operand(other));
    }

    /**
     * @see Relate#disjoint(Geometry, Geometry)
     */
    public boolean disjoint(Geometry<?> other) {
        return !intersects(other);
    }

    /**
     * @see Relate#intersects(Geometry, Geometry)
     */
    public boolean intersects(Geometry<?> other) {
        return Relate.intersects(operand, operand(other));
    }

    /**
     * @see Relate#touches(Geometry, Geometry)
     */
    public boolean touches(Geometry<?> other) {
        return Relate.touches(operand, operand(other));
    }

    /**
     * @see Relate#crosses(Geometry, Geometry)
     */
    public boolean crosses(Geometry<?> other) {
        return Relate.crosses(operand, operand(other));
    }

    /**
     * @return true if the prepared geometry lies within the other one
     * @see Relate#within(Geometry, Geometry)
     */
    public boolean within(Geometry<?> other) {
        return Relate.within(operand, operand(other));
    }

    /**
     * @return true if the prepared geometry contains the other one
     * @see Relate#contains(Geometry, Geometry)
     */
    public boolean contains(Geometry<?> other) {
        return Relate.contains(operand, operand(other));
    }

    /**
     * @see Relate#overlaps(Geometry, Geometry)
     */
    public boolean overlaps(Geometry<?> other) {
        return Relate.overlaps(operand, operand(other));
    }

    /**
     * @return true if the prepared geometry covers the other one
     * @see Relate#covers(Geometry, Geometry)
     */
    public boolean covers(Geometry<?> other) {
        return Relate.covers(operand, operand(other));
    }

    /**
     * @return true if the prepared geometry is covered by the other one
     * @see Relate#coveredBy(Geometry, Geometry)
     */
    public boolean coveredBy(Geometry<?> other) {
        return Relate.coveredBy(operand, operand(other));
    }
}
