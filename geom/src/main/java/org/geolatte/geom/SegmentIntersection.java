package org.geolatte.geom;

/**
 * The ways in which two line segments can intersect in the plane.
 *
 * <p>Corresponds to the cases in Ratschek &amp; Rokne, <em>Geometric Computations with Interval and New Robust
 * Methods</em>, Sec. 5.2 (Fig. 5.3), extended with the overlapping case for collinear segments.</p>
 *
 * @see LineSegment#classifyIntersection(LineSegment)
 */
public enum SegmentIntersection {

    /**
     * The segments have no point in common
     */
    DISJOINT,

    /**
     * The segments have exactly one point in common, which lies strictly inside both segments (a proper crossing)
     */
    CROSSING,

    /**
     * The segments have exactly one point in common, which is an endpoint of at least one of the segments
     */
    TOUCHING,

    /**
     * The segments are collinear and have a segment of positive length in common
     */
    OVERLAPPING;

    /**
     * @return true unless this is {@link #DISJOINT}
     */
    public boolean intersects() {
        return this != DISJOINT;
    }
}
