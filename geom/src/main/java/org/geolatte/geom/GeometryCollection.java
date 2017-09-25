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
import org.geolatte.geom.crs.CrsId;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A <code>Geometry</code> that is an ordered collection of some number of <code>Geometry</code>s.
 * <p/>
 * <p>All elements in a <code>GeometryCollection</code> must be in the same <code>CoordinateReferenceSystem</code>,
 * which is also the <code>CoordinateReferenceSystem</code> for the <code>GeometryCollection</code>.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class GeometryCollection<P extends Position, G extends Geometry<P>> extends Geometry<P> implements Complex<P,G> {

    protected final Geometry<P>[] geometries;

    /**
     * Constructs a <code>GeometryCollection</code> from the specified <code>Geometry</code>s.
     *
     * @param geometries the <code>Geometry</code>s that are the elements of the constructed <code>GeometryCollection</code>.
     */
    @SafeVarargs
    public GeometryCollection(G... geometries) {
        super(nestPositionSequences(geometries), getCrs(geometries));
        check(geometries);
        this.geometries = Arrays.copyOf(geometries, geometries.length);
    }

    /**
     * Constructs an empty <code>GeometryCollection</code>
     *
     */
    @SuppressWarnings("unchecked")
    public GeometryCollection(CoordinateReferenceSystem<P> crs) {
        super(crs);
        this.geometries = (Geometry<P>[])new Geometry[0];
    }


    /**
     * Returns the number of elements in this <code>GeometryCollection</code>.
     *
     * @return the number of elements of this instance.
     */
    public int getNumGeometries() {
        return geometries.length;
    }

    @Override
    public Class<? extends Geometry> getComponentType() {
        return Geometry.class;
    }

    /**
     * Returns the components
     *
     * @return an array containing all component objects
     */
    @Override
    @SuppressWarnings("unchecked")
    public G[] components() {
        return (G[]) Arrays.copyOf(this.geometries, this.geometries.length);
    }

    /**
     * Returns the <code>Geometry</code> element at the specified (zero-based) position in this <code>GeometryCollection</code>.
     *
     * @param num the position in the collection of the requested <code>Geometry</code>
     * @return the element <code>Geometry</code> at the position specified by the num parameter.
     */
    @SuppressWarnings("unchecked")
    public G getGeometryN(int num) {
        return (G) geometries[num];
    }

    @Override
    public int getDimension() {
        int maxDim = 0;
        for (Geometry part : this) {
            maxDim = Math.max(maxDim, part.getDimension());
        }
        return maxDim;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.GEOMETRYCOLLECTION;
    }

    /**
     * Creates an <code>Iterator</code> over the elements of this <code>GeometryCollection</code>.
     *
     * @return an <code>Iterator</code> over the elements of this <code>GeometryCollection</code>.
     */
    public Iterator<G> iterator() {
        return new Iterator<G>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < geometries.length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public G next() {
                return (G) geometries[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Accepts the <code>GeometryVisitor</code>, and
     * will pass it to it's constituent <code>Geometries</code>.
     *
     * @param visitor
     */
    @Override
    public void accept(GeometryVisitor<P> visitor) {
        visitor.visit(this);
        for (G part : this) {
            part.accept(visitor);
        }
    }

    /**
     * Verifies that the <code>Geometry</code> array can be used to construct a Geometry collection.
     * <p/>
     * <p>Conditions:</p>
     * <il>
     * <li>Array contains no NULL values</li>
     * <li>All non-empty elements have the same coordinate reference system/li>
     * </il>
     *
     * @param geometries
     * @throws IllegalStateException When geometries contains a null value or when the given geometries do not share the
     *                               same {@link CrsId}
     */
    private void check(G[] geometries) {
        if (geometries == null || geometries.length == 0) return;
        String msg = "NULL element not allowd in Geometry array";
        if (geometries[0] == null) throw new IllegalStateException(msg);
        CoordinateReferenceSystem<P> crs = geometries[0].getCoordinateReferenceSystem();
        for (int i = 1; i < geometries.length; i++) {
            if (geometries[i] == null) throw new IllegalStateException(msg);
            if (!(geometries[i].isEmpty() || crs.equals(geometries[i].getCoordinateReferenceSystem()))) {
                throw new IllegalStateException("Geometries in the array do no share the same coordinate reference systems.");
            }
        }
    }

}
