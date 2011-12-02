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

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class NestedPointSequence extends AbstractPointSequence {

    private final PointSequence[] children;
    private final int size;

    NestedPointSequence(PointSequence[] children, DimensionalFlag dimensionalFlag) {
        super(dimensionalFlag);
        if (children == null) throw new IllegalArgumentException("Require non-null argumnet");
        this.children = children;
        this.size = calculateSize();
    }

    private int calculateSize(){
        int size = 0;
        for (PointSequence child : children()){
            size += child.size();
        }
        return size;

    }

    private PointSequence[] children(){
        return this.children;
    }


    @Override
    public boolean isEmpty() {
        return this.children.length == 0;
    }

    //TODO implement a custom getCoordinates(int, double[]) method;

    @Override
    public double getCoordinate(int index, CoordinateComponent component) {
        int childOffset = index ;
        for (PointSequence child : children()){
            if (childOffset < child.size()){
                return child.getCoordinate(childOffset, component);
            } else {
                childOffset -= child.size();
            }
        }
        throw new ArrayIndexOutOfBoundsException(String.format("Index %d not found in collection of size %d", index, size()));
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public PointSequence clone() {
        PointSequence[] clonedChildren = new PointSequence[this.children.length];
        for (int i = 0; i < clonedChildren.length; i++) {
            clonedChildren[i] = children[i].clone();
        }
        return new NestedPointSequence(clonedChildren, getDimensionalFlag());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NestedPointSequence)) return false;

        NestedPointSequence that = (NestedPointSequence) o;

        return Arrays.equals(children, that.children);

        }

    @Override
    public int hashCode() {
        return Arrays.hashCode(children);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < children.length; i++){
            if (i > 0) builder.append(",");
            builder.append(children()[i].toString());
        }
        builder.append("]");
        return builder.toString();
    }

}
