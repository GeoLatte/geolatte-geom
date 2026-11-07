/**
 * Exact geometric predicates for double precision coordinates.
 *
 * <p>The methods in this package are based on H. Ratschek and J. Rokne, <em>Geometric Computations with Interval
 * and New Robust Methods. Applications in Computer Graphics, GIS and Computational Geometry</em> (Horwood, 2003):</p>
 * <ul>
 *     <li>{@link org.geolatte.geom.cga.exact.Interval}: machine interval arithmetic, used as an arithmetic filter
 *     (Ch. 2);</li>
 *     <li>{@link org.geolatte.geom.cga.exact.Essa}: the Exact Sign of Sum Algorithm (Ch. 4);</li>
 *     <li>{@link org.geolatte.geom.cga.exact.ExactSum}: exact representation of sums of products of doubles as
 *     lists of doubles, so that ESSA can be applied (Sec. 4.6);</li>
 *     <li>{@link org.geolatte.geom.cga.exact.Predicates}: the geometric predicates (orientation, in-circle, ring
 *     orientation, distance comparison), each implemented as an interval filter followed by ESSA (Sec. 4.6, 8.3,
 *     8.4).</li>
 * </ul>
 *
 * <p>Predicates are exact: their result is the result for the given double coordinates as exact real numbers.
 * Constructions (e.g. computing an intersection point) are not exact in general, since their results usually
 * are not representable as doubles.</p>
 */
package org.geolatte.geom.cga.exact;
