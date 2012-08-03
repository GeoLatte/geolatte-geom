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

import java.util.Iterator;

/**
 * A <code>Geometry</code> that is an ordered collection of some number of <code>Geometry</code>s.
 *
 * <p>All elements in a <code>GeometryCollection</code> must be in the same <code>CoordinateReferenceSystem</code>,
 * which is also the <code>CoordinateReferenceSystem</code> for the <code>GeometryCollection</code>.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class GeometryCollection extends Geometry implements Iterable<Geometry>{

    protected final Geometry[] geometries;
    protected final PointCollection points;

    final static GeometryCollection EMPTY =  new GeometryCollection(new Geometry[0]);

    /**
     * Constructs an empty <code>GeometryCollection</code>.
     *
     * @return an <code>GeometryCollection</code> that is empty.
     */
    public static GeometryCollection createEmpty() {
        return EMPTY;
    }

    /**
     * Constructs a <code>GeometryCollection</code> from the specified <code>Geometry</code>s.
     *
     * @param geometries the <code>Geometry</code>s that are the elements of the constructed <code>GeometryCollection</code>.
     */
    public GeometryCollection(Geometry[] geometries) {
        super(getCrsId(geometries), getGeometryOperations(geometries));
        check(geometries);
        points = collectPointSets(geometries);
        if (points.isEmpty()) {
            this.geometries = new Geometry[0];
        } else {
            this.geometries = geometries;
        }
    }


    /**
     * Returns the number of elements in this <code>GeometryCollection</code>.
     *
     * @return the number of elements of this instance.
     */
    public int getNumGeometries() {
        return geometries.length;
    }

    /**
     * Returns the <code>Geometry</code> element at the specified (zero-based) position in this <code>GeometryCollection</code>.
     *
     * @param num the position in the collection of the requested <code>Geometry</code>
     * @return the element <code>Geometry</code> at the position specified by the num parameter.
     */
    public Geometry getGeometryN(int num) {
        return geometries[num];
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
        return GeometryType.GEOMETRY_COLLECTION;
    }

    @Override
    public PointCollection getPoints() {
        return points;
    }

    /**
     * Creates an <code>Iterator</code> over the elements of this <code>GeometryCollection</code>.
     *
     * @return an <code>Iterator</code> over the elements of this <code>GeometryCollection</code>.
     */
    public Iterator<Geometry> iterator() {
        return new Iterator<Geometry>(){
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < geometries.length;
            }

            @Override
            public Geometry next() {
                return geometries[index++];
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
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
        for (Geometry part : this) {
            part.accept(visitor);
        }
    }

    /**
     * Verifies that the <code>Geometry</code> array can be used to construct a Geometry collection.
     *
     * <p>Conditions:</p>
     * <il>
     *     <li>Array contains no NULL values</li>
     *     <li>All elements have the same coordinate reference system</li>
     * </il>
     * @param geometries
     */
    private void check(Geometry[] geometries){
        if (geometries == null || geometries.length == 0) return;
        String msg = "NULL element not allowd in Geometry array";
        if (geometries[0] == null) throw new IllegalStateException(msg);
        CrsId crsId = geometries[0].getCrsId();
        for (int i = 1; i < geometries.length; i++) {
            if (geometries[i] == null) throw new IllegalStateException(msg);
            if ( ! crsId.equals(geometries[i].getCrsId()) )
                throw new IllegalStateException("Geometries in the array do no share the same coordinate reference systems.");
        }
    }

}
