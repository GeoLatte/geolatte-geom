/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.cga.exact.Constructions;
import org.geolatte.geom.cga.exact.Predicates;

/**
 * A portion of a line delimited (inclusively) by two <code>Position</code>s.
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/25/11
 */
public class LineSegment<P extends Position> {


    private final P p0;
    private final P p1;

    /**
     * Constructs a <code>LineSegment</code> from the specified <code>Position</code>s.
     *
     * @param p0 the start <code>Position</code>
     * @param p1 the end <code>Position</code>
     */
    public LineSegment(P p0, P p1) {
        this.p0 = p0;
        this.p1 = p1;
    }

    /**
     * Returns the first, or start <code>Position</code> of this <code>LineSegment</code>
     * @return the first, or start <code>Position</code> of this <code>LineSegment</code>
     */
    public P getStartPosition() {
        return p0;
    }

    /**
     * Returns the second, or end <code>Position</code> of this <code>LineSegment</code>
     * @return the second, or end <code>Position</code> of this <code>LineSegment</code>
     */
    public P getEndPosition() {
        return p1;
    }

    /**
     * Determines how this segment and the specified segment intersect in the plane of the first two coordinates.
     *
     * <p>The test is exact for the coordinates as given: it is never falsified by rounding errors (see
     * {@link Predicates#segmentIntersection(Position, Position, Position, Position)}). Note that for geographic
     * coordinates, the segments are treated as straight lines in the longitude/latitude plane, not as geodesics.</p>
     *
     * @param other the other segment
     * @return the kind of intersection
     * @throws IllegalArgumentException if a coordinate is NaN or infinite (e.g. for an empty {@code Position})
     */
    public SegmentIntersection classifyIntersection(LineSegment<?> other) {
        return Predicates.segmentIntersection(p0, p1, other.p0, other.p1);
    }

    /**
     * Determines whether this segment and the specified segment have at least one point in common, in the plane of
     * the first two coordinates. The result is exact.
     *
     * @param other the other segment
     * @return true iff the segments intersect
     * @throws IllegalArgumentException if a coordinate is NaN or infinite (e.g. for an empty {@code Position})
     * @see #classifyIntersection(LineSegment)
     */
    public boolean intersects(LineSegment<?> other) {
        return classifyIntersection(other).intersects();
    }

    /**
     * Computes the intersection of this segment and the specified segment, in the plane of the first two
     * coordinates.
     *
     * <p>The kind of intersection is determined exactly (see {@link #classifyIntersection(LineSegment)}). The
     * common point of touching segments, and the endpoints of the common segment of overlapping segments, are
     * endpoints of the segments, and are returned exactly. The X and Y coordinates of the crossing point of
     * crossing segments are correctly rounded: they are the doubles nearest to the exact coordinates. The result
     * therefore does not depend on the order or orientation of the segments, and lies in the bounding boxes of
     * both segments. It is, however, generally not exactly on either of the segments.</p>
     *
     * <p>Other coordinates (Z, M) are interpolated linearly along this segment. The positions in the result
     * have the type of the positions of this segment.</p>
     *
     * @param other the other segment
     * @return the intersection
     * @throws IllegalArgumentException      if an X or Y coordinate is NaN or infinite (e.g. for an empty
     *                                       {@code Position})
     * @throws UnsupportedOperationException if new positions of the type of this segment's positions can not be
     *                                       created
     * @see Constructions#intersection(LineSegment, LineSegment)
     */
    public SegmentIntersectionResult<P> intersection(LineSegment<?> other) {
        return Constructions.intersection(this, other);
    }

}
