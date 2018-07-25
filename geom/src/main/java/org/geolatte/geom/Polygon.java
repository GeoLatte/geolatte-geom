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

import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Arrays;
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
public class Polygon<P extends Position> extends Geometry<P> implements Polygonal<P>, Complex<P, LinearRing<P>> {


    private final LinearRing<P>[] rings;
    

    @SuppressWarnings("unchecked")
    public Polygon(CoordinateReferenceSystem<P> crs) {
        super(crs);
        rings = (LinearRing<P>[])new LinearRing[0];
    }

    /**
     * Creates a <code>Polygon</code> with no holes, and having the specified <code>PositionSequence</code> as exterior boundary
     *
     * @param positionSequence the <code>PositionSequence</code> representing the exterior boundary
     * @throws IllegalArgumentException when the specified <code>PositionSequence</code> does not form a
     *                                  <code>LinearRing</code> (i.e., is empty or not closed).
     */
    @SuppressWarnings("unchecked")
    public Polygon(PositionSequence<P> positionSequence, CoordinateReferenceSystem<P> crs) {
        this(new LinearRing<P>(positionSequence, crs));
    }

    /**
     * Creates a <code>Polygon</code> with the specified array of exterior and interior boundaries.
     *
     * @param rings the array of the <code>Polygon</code>'s boundaries: the first element is the exterior boundary, all subsequent rings represent
     *              interior boundaries.
     * @throws IllegalArgumentException when the specified <code>PositionSequence</code> does not form a
     *                                  <code>LinearRing</code> (i.e., is empty or not closed).
     */
    public Polygon(LinearRing<P>... rings) {
        super(nestPositionSequences(rings), getCrs(rings));
        checkRings(rings);
        this.rings = Arrays.copyOf(rings, rings.length);
    }


    private void checkRings(LinearRing<P>[] rings) {
        CoordinateReferenceSystem<P> crs = getCrs(rings);
        for (LinearRing<P> ring : rings) {
            checkLinearRing(ring, crs);
        }
    }

    private void checkLinearRing(LinearRing<P> ring, CoordinateReferenceSystem<P> crs) {
        if (ring == null) throw new IllegalArgumentException("NULL linear ring is not valid.");
        if (ring.isEmpty()) throw new IllegalArgumentException("Empty linear ring is not valid.");
        if (!ring.getCoordinateReferenceSystem().equals(crs)) throw new IllegalArgumentException("Linear ring with different CRS than exterior boundary.");
    }

    /**
     * Returns the exterior boundary of this <code>Polygon</code>.
     *
     * @return a <code>LinearRing</code> representing the exterior boundary of this <code>Polygon</code>.
     */
    public LinearRing<P> getExteriorRing() {
        return this.isEmpty() ?
                new LinearRing<P>(getPositions(), getCoordinateReferenceSystem()) :
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
    public LinearRing<P> getInteriorRingN(int index) {
        return this.rings[index + 1];
    }


    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POLYGON;
    }

    /**
     * Returns an <code>Iterator</code> over the boundaries of this <code>Polygon</code>.
     *
     * <p>The boundaries are returned in order, with the first element being the exterior boundary.</p>
     *
     * @return an <code>Iterator</code> over the boundaries of this <code>Polygon</code>.
     */
    public Iterator<LinearRing<P>> iterator() {
        return new Iterator<LinearRing<P>>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < rings.length;
            }

            @Override
            public LinearRing<P> next() {
                return rings[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void accept(GeometryVisitor<P> visitor) {
        visitor.visit(this);
    }

    @Override
    public int getNumGeometries() {
        return rings.length;
    }

    @Override
    public Class<? extends Geometry> getComponentType() {
        return LinearRing.class;
    }

    /**
     * Returns the components
     *
     * @return an array containing all component objects
     */
    @Override
    public LinearRing<P>[] components() {
        return Arrays.copyOf(this.rings, this.rings.length);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q extends Position> Polygon<Q> as(Class<Q> castToType){
        return (Polygon<Q>)this;
    }
}
