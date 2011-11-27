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
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class GeometryCollection extends Geometry implements Iterable<Geometry>{

    protected final Geometry[] geometries;
    protected final PointSequence points;

    final static GeometryCollection EMPTY =  new GeometryCollection(new Geometry[0],CrsId.UNDEFINED, null);

    public static GeometryCollection create(Geometry[] geometries, CrsId crsId) {
        return new GeometryCollection(geometries, crsId, null);
    }

    public static GeometryCollection createEmpty() {
        return EMPTY;
    }

    protected GeometryCollection(Geometry[] geometries, CrsId crsId, GeometryOperations geometryOperations) {
        super(crsId, geometryOperations);
        points = createAndCheckPointSequence(geometries);
        if (points.isEmpty()) {
            this.geometries = new Geometry[0];
        } else {
            this.geometries = geometries;
        }
    }


    public int getNumGeometries() {
        return geometries.length;
    }

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
    public PointSequence getPoints() {
        return points;
    }

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

    @Override
    public void accept(GeometryVisitor visitor) {
        visitor.visit(this);
    }
}
