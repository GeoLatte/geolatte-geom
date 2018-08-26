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

import org.locationtech.jts.geom.CoordinateSequence;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class NestedPositionSequence<P extends Position> extends AbstractPositionSequence<P> {

    protected final PositionSequence<P>[] children;
    private final int size;


    NestedPositionSequence(PositionSequence<P>[] children) {
        super(extractFactory(children));
        this.children = children;
        this.size = calculateSize();
    }

    private static <C extends Position> PositionFactory<C> extractFactory(PositionSequence<C>[] children) {

        if (children == null) {
            throw new IllegalArgumentException("Null or empty children array not allowed.");
        } else {
            PositionFactory<C> factory = null;
            for (PositionSequence<C> seq : children) {
                if (seq.isEmpty()) continue;

                if (seq == null) {
                    throw new IllegalArgumentException("No null entries allowed in children array.");
                } else {
                    if (factory != null && !factory.equals(seq.getPositionFactory())) {
                        throw new IllegalArgumentException("All child sequences must have the same Coordinate Reference System");
                    }
                    factory = seq.getPositionFactory();
                }
            }
            return factory;
        }
    }

    private int calculateSize() {
        int size = 0;
        for (PositionSequence<P> child : getChildren()) {
            size += child.size();
        }
        return size;

    }

    public PositionSequence<P>[] getChildren() {
        return Arrays.copyOf(this.children, this.children.length);
    }

    @Override
    public boolean isEmpty() {
        return this.children.length == 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    /** @deprecated in {@link org.locationtech.jts.geom.CoordinateSequence }.*/
    @Deprecated
    @Override
    public NestedPositionSequence<P> clone() {
        return this; //this is correct since this object is immutable.
    }

    @Override
    public NestedPositionSequence<P> copy() {
        return this; //this is correct since this object is immutable.
    }

    @Override
    public void setOrdinate(int position, int ordinateIndex, double value) {
        int childOffset = position;
        for (PositionSequence<P> pChild : getChildren()) {
            CoordinateSequence child = (CoordinateSequence)pChild;
            if (childOffset < child.size()) {
                child.setOrdinate(childOffset, ordinateIndex, value);
                return;
            } else {
                childOffset -= child.size();
            }
        }
        throw new ArrayIndexOutOfBoundsException(String.format("Index %d not found in collection of size %d", position, size()));
    }

    @Override
    public void getCoordinates(int position, double[] coordinates) {
        int childOffset = position;
        for (PositionSequence<P> child : getChildren()) {
            if (childOffset < child.size()) {
                child.getCoordinates(childOffset, coordinates);
                return;
            } else {
                childOffset -= child.size();
            }
        }
        throw new ArrayIndexOutOfBoundsException(String.format("Index %d not found in collection of size %d", position, size()));

    }



    @Override
    public void accept(PositionVisitor<P> visitor) {
        for (PositionSequence<P> child : getChildren()) {
            child.accept(visitor);
        }
    }

    @Override
    public void accept(LLAPositionVisitor visitor) {
        for (PositionSequence<P> child : getChildren()) {
            child.accept(visitor);
        }
    }

    /**
     * Creates a new <code>PositionSequence</code> with positions in reverse order.
     *
     * @return
     */
    @Override
    public PositionSequence<P> reverse() {
        PositionSequence<P>[] childrenCopy =  this.getChildren();
        for (int i = 0; i < childrenCopy.length; i++){
            childrenCopy[i] = childrenCopy[i].reverse();
        }
        reverseInPlace(childrenCopy);
        return new NestedPositionSequence<P>(childrenCopy);
    }

    private void reverseInPlace(PositionSequence<P>[] arr ) {
        for (int i = 0; i < arr.length/2; i++) {
            PositionSequence<P> h = arr[i];
            arr[i] = arr[arr.length - 1 -i];
            arr[arr.length - 1 -i] = h;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NestedPositionSequence that = (NestedPositionSequence) o;

        if (size != that.size) return false;
        if (!Arrays.equals(children, that.children)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = children != null ? Arrays.hashCode(children) : 0;
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < children.length; i++) {
            if (i > 0) builder.append(",");
            builder.append(getChildren()[i].toString());
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Iterator<P> iterator() {

        return new Iterator<P>() {
            private int childIndex = 0;
            private Iterator<P> currentChildIterator;

            @Override
            public boolean hasNext() {
                advanceChildIterator();
                return currentChildIterator.hasNext();
            }

            /**
             * set currentChildIterator to the iterator of the next child, if this child has no more elements and a next child exists.
             */
            private void advanceChildIterator() {
                while (childIndex < children.length && (currentChildIterator == null || !currentChildIterator.hasNext())) {
                    currentChildIterator = children[childIndex++].iterator();
                }
            }

            @Override
            public P next() {
                return currentChildIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove not supported on PointSets.");
            }
        };
    }

}
