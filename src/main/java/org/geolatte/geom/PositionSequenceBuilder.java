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
 * A builder for <code>PositionSequence</code>s.
 *
 * <p>{@code PositionSequences} are built by adding points in order.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public interface PositionSequenceBuilder<P extends Position> {

    /**
     * Adds a <code>Position</code> to the <code>PositionSequence</code> being built.
     *
     * @param coordinates the coordinates of the <code>Position</code> that is added
     * @return this instance
     */
    PositionSequenceBuilder<P> add(double... coordinates);

    /**
     * Adds a {@code} Position to the <code>PositionSequence</code> being built.
     * @param position the position that is added
     * @return this instance
     */
    PositionSequenceBuilder<P> add(P position);

    /**
     * Returns the result of this builder.
     *
     * @return the <code>PositionSequence</code> that has been built by this builder instance.
     * @throws IllegalStateException when the construction of the <code>PositionSequence</code> has not yet been completed.
     */
    PositionSequence<P> toPositionSequence();

    /**
     * Returns the number of positions that have already been added.
     * @return the number of positions that have already been added.
     */
    int getNumAdded();
}
