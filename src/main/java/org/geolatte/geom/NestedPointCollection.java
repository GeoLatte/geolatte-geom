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

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class NestedPointCollection extends AbstractPointCollection implements ComplexPointCollection {

    private final int size;
    protected final PointCollection[] children;


    NestedPointCollection(PointCollection[] children) {
        super(extractDimensionalFlag(children));
        this.children = children;
        this.size = calculateSize();
    }

    private static DimensionalFlag extractDimensionalFlag(PointCollection[] children) {
        return (children == null || children.length == 0) ? DimensionalFlag.d2D : children[0].getDimensionalFlag();
    }

    private int calculateSize() {
        int size = 0;
        for (PointCollection child : getPointSets()) {
            size += child.size();
        }
        return size;

    }

    @Override
    public PointCollection[] getPointSets() {
        return this.children;
    }


    @Override
    public boolean isEmpty() {
        return this.children.length == 0;
    }

    //TODO implement a custom getCoordinates(int, double[]) method

    @Override
    public double getCoordinate(int position, CoordinateComponent component) {
        int childOffset = position;
        for (PointCollection child : getPointSets()) {
            if (childOffset < child.size()) {
                return child.getCoordinate(childOffset, component);
            } else {
                childOffset -= child.size();
            }
        }
        throw new ArrayIndexOutOfBoundsException(String.format("Index %d not found in collection of size %d", position, size()));
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public PointCollection clone() {
        return this; //this is correct since PointSets are immutable.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PointSequence)) return false;

        PointSequence that = (PointSequence) o;

        if (is3D() != that.is3D()) return false;
        if (isMeasured() != that.isMeasured()) return false;
        return new PointCollectionPointEquality().equals(this, that);
    }



    @Override
    public int hashCode() {
        return Arrays.hashCode(children);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < children.length; i++) {
            if (i > 0) builder.append(",");
            builder.append(getPointSets()[i].toString());
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Iterator<PointCollection> iterator() {
        return new Iterator<PointCollection>(){
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < children.length;
            }

            @Override
            public PointCollection next() {
                return children[index++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove not supported on PointSets.");
            }
        };
    }
}
