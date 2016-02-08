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

/**
* Defines standard operations on {@code Geometry}s with projected coordinate systems.
* <p/>
* <p>The semantics of the operations is as specified in
* <a href="http://portal.opengeospatial.org/files/?artifact_id=25355">Simple Feature Access - Part 1: common architecture</a>
* </p>
*
* @author Karel Maesen, Geovise BVBA
*/
public interface ProjectedGeometryOperations {

    /**
     * Default implementation of this interface
     */
    public final ProjectedGeometryOperations Default = new JTSGeometryOperations();

    /**
     * Tests the simplicity of the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> to test for simplicity.
     * @return True iff the specified <code>Geometry</code> is simple.
     */
    <P extends C2D> boolean isSimple(final Geometry<P> geometry);

    /**
     * Calculates the boundary of the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> for which to calculate the boundary.
     * @return a <code>Geometry</code> representing the boundary of the specified <code>Geometry</code>.
     */
    <P extends C2D> Geometry<P> boundary(final Geometry<P> geometry);


    /**
     * Creates a <code>Geometry</code> with the positions of the input <code>Geometry</code> in reverse order.
     *
     * @param geometry the <code>Geometry</code> to reverse
     * @return a <code>Geometry</code> with the same positions as the specified input <code>Geometry</code> but in reverse order
     */
    <P extends C2D, G extends Geometry<P>> G reverse (final G geometry);


    /**
     * Checks if the specified <code>Geometry</code>s intersect.
     *
     * @param geometry the first <code>Geometry</code> operand of the intersection test
     * @param other    the second <code>Geometry</code> operand of the intersection test
     * @return true iff the specified <code>Geometry</code>s spatially intersect
     */
    <P extends C2D> boolean intersects(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Checks if the specified <code>Geometry</code>s touch.
     *
     * @param geometry the first <code>Geometry</code> operand of the touch test
     * @param other    the second <code>Geometry</code> operand of the touch test
     * @return true iff the specified <code>Geometry</code>s spatially touch
     */
    <P extends C2D> boolean touches(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Checks if the specified <code>Geometry</code>s cross.
     *
     * @param geometry the first <code>Geometry</code> operand of the cross test
     * @param other    the second <code>Geometry</code> operand of the cross test
     * @return true iff the specified <code>Geometry</code>s spatially cross
     */
    <P extends C2D> boolean crosses(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Checks if the first specified <code>Geometry</code> spatially
     * contains the second.
     *
     * @param geometry the first <code>Geometry</code> operand of the containment test
     * @param other    the second <code>Geometry</code> operand of the containment test
     * @return true iff the first specified <code>Geometry</code> spatially contains the second
     */
    <P extends C2D> boolean contains(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Checks if the specified <code>Geometry</code>s overlap.
     *
     * @param geometry the first <code>Geometry</code> operand of the overlap test
     * @param other    the second <code>Geometry</code> operand of the overlap test
     * @return true iff the specified <code>Geometry</code>s spatially overlap
     */
    <P extends C2D> boolean overlaps(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Checks if the specified <code>Geometry</code>s are spatially related by testing
     * for intersections between the interior, boundary and exterior of the two geometric objects as specified by
     * the values in the intersection pattern matrix. This returns false if all the tested intersections are empty except
     * exterior (this) intersect exterior (another).
     *
     * @param geometry the first <code>Geometry</code> operand of the relates test
     * @param other    the second <code>Geometry</code> operand of the relates test
     * @param matrix   the intersection pattern matrix
     * @return true iff this instance intersects the specifed other <code>Geometry</code>
     */
    <P extends C2D> boolean relates(final Geometry<P> geometry, final Geometry<P> other, final String matrix);


    /**
     * Calculates the shortest distance between any two points in the two <code>Geometry</code>s in the
     * coordinate reference system of this <code>Geometry</code>. Only the X/Y-coordinates are used in the distance
     * calculation; M- and Z-coordinates are ignored.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return the shortest distance between the two specified <code>Geometries</code>
     */
    <P extends C2D> double distance(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Calculates a <code>Geometry</code> that represents all points whose distance from the specified
     * <code>Geometry</code> is less than or equal the specified distance.
     * <p/>
     * <p>Calculations are in the <code>CoordinateReferenceSystem</code> of this
     * <code>Geometry</code>.</p>
     * <p>Z- or M-coordinates are ignored in the buffering operation; and the result will always be a 2D geometry.</p>
     *
     * @param geometry the <code>Geometry</code> for which to calculate the buffer
     * @param distance the buffer distance
     * @return a 2D <code>Geometry</code> representing the buffer of the specified <code>Geometry</code> with the specified distance.
     */
    <P extends C2D> Geometry<P> buffer(final Geometry<P> geometry, final double distance);

    /**
     * Calculates the convex hull of the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> for which to calculate the convex hull.
     * @return the convex hull for the specified <code>Geometry</code>.
     */
    <P extends C2D> Geometry<P> convexHull(final Geometry<P> geometry);

    /**
     * Calculates the point set intersection of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>Geometry</code> representing the point set intersection between the two specified <code>Geometries</code>
     */
    <P extends C2D> Geometry<P> intersection(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Calculates the point set union of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>Geometry</code> representing the point set union between the two specified <code>Geometries</code>
     */
    <P extends C2D> Geometry<P> union(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Calculates the point set difference of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>Geometry</code> representing the point set difference between the two specified <code>Geometries</code>
     */
    <P extends C2D> Geometry<P> difference(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Calculates the point set symmetric difference of the specified <code>Geometry</code>s.
     *
     * @param geometry the first <code>Geometry</code>
     * @param other    the second <code>Geometry</code>
     * @return a <code>Geometry</code> representing the point set symmetric difference between the two specified <code>Geometries</code>
     */
    <P extends C2D> Geometry<P> symmetricDifference(final Geometry<P> geometry, final Geometry<P> other);

    /**
     * Calculates the length of the specified {@code Geometry}.
     *
     * @param geometry the Geometry
     * @return the length of the specified Geometry
     */
    <P extends C2D, G extends Geometry<P> & Linear<P>> double length(final G geometry);

    /**
     * Calculates the area of the specified {@code Geometry}.
     *
     * @param geometry the Geometry
     * @return the area of the specified Geometry
     */
    <P extends C2D, G extends Geometry<P> & Polygonal<P>> double area(final G geometry);

    /**
     * Calculates a centroid for the specified {@code Geometry}.
     *
     * @param geometry the Geometry
     * @param <G>      Polygonal  Geometry
     * @return the centroid of the specified Geometry as a {@code Point}
     */
    <P extends C2D, G extends Geometry<P> & Polygonal<P>> Point<P> centroid(final G geometry);


}
