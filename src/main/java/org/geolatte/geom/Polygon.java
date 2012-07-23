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

import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.jts.JTS;

import java.util.Iterator;

/**
 * A planar surface defined by 1 exterior boundary and 0 or more interior boundaries. Each interior boundary defines a
 * hole in the <code>Polygon</code>.
 * <p/>
 * <p>The exterior boundary <code>LinearRing</code> defines the "top" of the surface which is the side of the surface
 * from which the exterior boundary appears to traverse the boundary in a counter clockwise direction. The interior
 * <code>LinearRing</code>s will have the opposite orientation and appear as clockwise when viewed from the "top".</p>
 * <p/>
 * <p>The rules that define valid <code>Polygon</code>s are as follows</p>
 * <ol>
 * <li><code>Polygon</code>s are topologically closed;</li>
 * <li>The boundary of a <code>Polygon</code> consists of a set of <code>LinearRing</code>s that make up its
 * exterior and interior boundaries;</li>
 * <li>No two rings in the boundary cross and the rings in the boundary of a <code>Polygon</code> may
 * intersect at a <code>Point</code> but only as a tangent</li>
 * <li>A <code>Polygon</code> may not have cut lines, spikes or punctures;</li>
 * <li>The interior of every <code>Polygon</code> is a connected point set; </li>
 * <li>The exterior of a <code>Polygon</code> with 1 or more holes is not connected. Each hole defines a
 * connected component of the exterior</li>
 * </ol>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/14/11
 */
public class Polygon extends Geometry implements Iterable<LinearRing> {


    private final PointSequence points;
    private final LinearRing[] rings;
    static final Polygon EMPTY = new Polygon(new LinearRing[0]);

    /**
     * Creates an empty <code>Polygon</code>.
     *
     * @return an empty <code>Polygon</code>.
     */
    public static Polygon createEmpty() {
        return EMPTY;
    }

    /**
     * Creates a <code>Polygon</code> with no holes, and having the specified <code>PointSequence</code> as exterior boundary
     *
     * @param pointSequence the <code>PointSequence</code> representing the exterior boundary
     * @param crsId         the <code>CrsId</code> for the constructed <code>Polygon</code>
     * @param ops           the <code>GeometryOperatoins</code> implementation for the constructed <code>Polygon</code>
     * @throws IllegalArgumentException when the specified <code>PointSequence</code> does not form a
     *                                  <code>LinearRing</code> (i.e., is empty or not closed).
     */
    public Polygon(PointSequence pointSequence, CrsId crsId, GeometryOperations ops) {
        this(new LinearRing[]{new LinearRing(pointSequence, crsId, ops)});
    }

    /**
     * Creates a <code>Polygon</code> with no holes, and having the specified <code>PointSequence</code> as exterior boundary
     *
     * @param pointSequence the <code>PointSequence</code> representing the exterior boundary
     * @param crsId         the <code>CrsId</code> for the constructed <code>Polygon</code>
     * @throws IllegalArgumentException when the specified <code>PointSequence</code> does not form a
     *                                  <code>LinearRing</code> (i.e., is empty or not closed).
     */
    public Polygon(PointSequence pointSequence, CrsId crsId) {
        this(new LinearRing[]{new LinearRing(pointSequence, crsId, null)});
    }

    /**
     * Creates a <code>Polygon</code> with the specified array of exterior and interior boundaries.
     *
     * @param rings the array of the <code>Polygon</code>'s boundaries: the first element is the exterior boundary, all subsequent rings represent
     *              interior boundaries.
     * @throws IllegalArgumentException when the specified <code>PointSequence</code> does not form a
     *                                  <code>LinearRing</code> (i.e., is empty or not closed).
     */
    public Polygon(LinearRing[] rings) {
        super(getCrsId(rings), getGeometryOperations(rings));
        checkRings(rings);
        points = createAndCheckPointSequence(rings);
        this.rings = rings;
    }


    private void checkRings(LinearRing[] rings) {
        for (LinearRing ring : rings) {
            checkLinearRing(ring);
        }
    }

    private void checkLinearRing(LinearRing ring) {
        if (ring == null) throw new IllegalArgumentException("NULL linear ring is not valid.");
        if (ring.isEmpty()) throw new IllegalArgumentException("Empty linear ring is not valid.");
    }

    @Override
    public PointSequence getPoints() {
        return this.points;
    }

    /**
     * Returns the exterior boundary of this <code>Polygon</code>.
     *
     * @return a <code>LinearRing</code> representing the exterior boundary of this <code>Polygon</code>.
     */
    public LinearRing getExteriorRing() {
        return this.isEmpty() ?
                LinearRing.EMPTY :
                this.rings[0];
    }

    /**
     * returns the number of interior boundaries.
     *
     * @return the number of interior boundaries
     */
    public int getNumInteriorRing() {
        return this.isEmpty() ?
                0 :
                this.rings.length - 1;
    }

    /**
     * Returns the specified interior ring.
     *
     * @param index the (zero-based) position of the interior boundary in the list of interior boundaries.
     * @return the <code>LinearRing</code> at the position specified by the parameter index.
     */
    public LinearRing getInteriorRingN(int index) {
        return this.rings[index + 1];
    }

    /**
     * Returns the area of this <code>Polygon</code> as measured in the <code>CoordinateReferenceSystem</code> of this
     * <code>Polygon</code>.
     *
     * @return the area of this <code>Polygon</code>.
     */
    public double getArea() {
        return JTS.to(this).getArea();
    }

    /**
     * Returns the mathematical centroid for this <code>Polygon</code>.
     * <p/>
     * <p>The result is not guaranteed to be on this surface.</p>
     *
     * @return the centroid <code>Point</code> for this <code>Polygon</code>
     */
    public Point getCentroid() {
        return (Point) JTS.from(JTS.to(this).getCentroid());
    }

    /**
     * Returns a <code>Point</code> that is guaranteed to lie on this <code>Polygon</code>.
     *
     * @return a <code>Point</code> on this <code>Polygon</code>
     */
    public Point getPointOnSurface() {
        return getPointN(0);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYGON;
    }

    @Override
    public MultiLineString getBoundary() {

        return isEmpty() ?
                MultiLineString.EMPTY :
                new MultiLineString(rings);
    }

    /**
     * Returns an <code>Iterator</code> over the boundaries of this <code>Polygon</code>.
     *
     * <p>The boundaries are returned in order, with the first element being the exterior boundary.</p>
     *
     * @return
     */
    public Iterator<LinearRing> iterator() {
        return new Iterator<LinearRing>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < rings.length;
            }

            @Override
            public LinearRing next() {
                return rings[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
