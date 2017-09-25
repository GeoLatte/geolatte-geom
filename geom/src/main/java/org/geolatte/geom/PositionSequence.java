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
 * A <code>PositionSequence</code> is an ordered sequence of <code>Position</code>s.
 * <p/>
 * <p>A <code>PositionSequence</code> is typically used to store the <code>Position</code>s (vertices) that define a
 * curve (a 1-dimensional geometric primitive), with the subtype of the curve specifying the form of interpolation between
 * consecutive <code>Position</code>s. (E.g.a <code>LineString</code> uses linear interpolation between <code>Position</code>s.)</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public interface PositionSequence<P extends Position> extends Iterable<P> {

    PositionSequence<P> clone();

    Class<P> getPositionClass();

    PositionFactory<P> getPositionFactory();

    /**
     * Returns true iff this <code>PositionSequence</code> contains no <code>Position</code>s
     *
     * @return true iff this <code>PositionSequence</code> contains no <code>Position</code>s
     */
    boolean isEmpty();

    /**
     * Returns the coordinate dimension of this <code>PositionSequence</code>.
     * <p/>
     * <p>The coordinate dimension is number of measurements or axes needed to describe <code>Position</code>
     * in the coordinate system associated with this <code>PositionSequence</code>.</p>
     *
     * @return the coordinate dimension of this <code>PositionSequence</code>
     */
    int getCoordinateDimension();

    /**
     * Returns the number of <code>Position</code>s contained in this <code>PositionSequence</code>.
     *
     * @return the number of <code>Position</code>s contained in this <code>PositionSequence</code>.
     */
    int size();

    /**
     * Copies the coordinates at position i into the specified array.
     * @param position the position index to copy
     * @param coordinates the destination array
     * @throws IllegalArgumentException if the destination array is smaller than the coordiante dimension.
     */
    void getCoordinates(int position, double[] coordinates);

    P getPositionN(int index);

    /**
     * Accepts a <code>PositionVisitor</code>.
     * <p/>
     * <p>This instance will pass the visitor to all of its <code>Position</code>s.</p>
     *
     * @param visitor the visitor for this instance's <code>Position</code>s
     */
    void accept(PositionVisitor<P> visitor);

    /**
     * Accepts a <code>LLAPositionVisitor</code>.
     *
     * @param visitor
     */
    void accept(LLAPositionVisitor visitor);

    /**
     * Creates a new <code>PositionSequence</code> with positions in reverse order.
     *
     * @return
     */
    PositionSequence<P> reverse();


}
