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
 * A factory for <code>PositionSequenceBuilder</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/22/11
 */
public class PositionSequenceBuilders {

    /**
     * Creates a <code>PositionSequenceBuilder</code> of fixed size.
     * <p/>
     * <p>The returned <code>PositionSequenceBuilder</code> expects that exactly <code>num</code> will be
     * added to it.</p>
     *
     * @param num     the number of <code>Position</code>s that will be added to the returned builder.
     * @param posType the class of {@code Position}
     * @return a <code>PositionSequenceBuilder</code> that builds a <code>PointSequence</code> containing <code>num</code> <code>Position</code>s.
     */
    public static <P extends Position> PositionSequenceBuilder<P> fixedSized(int num, Class<P> posType) {
        return new FixedSizePositionSequenceBuilder<P>(num, Positions.getFactoryFor(posType));
    }

    /**
     * Creates a <code>PositionSequenceBuilder</code> of variable size.
     *
     * @param posType the class of {@code Position}
     * @return a <code>PositionSequenceBuilder</code> that builds a <code>PositionSequence</code> for an arbitrary number of <code>Position</code>s.
     */
    public static <P extends Position> PositionSequenceBuilder<P> variableSized(Class<P> posType) {
        return new VariableSizePositionSequenceBuilder<P>(Positions.getFactoryFor(posType));

    }

}
