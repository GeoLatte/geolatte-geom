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
 * A factory for <code>PointSequenceBuilder</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/22/11
 */
public class PointSequenceBuilders {

    /**
     * Creates a <code>PointSequenceBuilder</code> of fixed size.
     *
     * <p>The returned <code>PointSequenceBuilder</code> expects that exactly <code>numPoints</code> will be
     * added to it.</p>
     *
     * @param numPoints the number of <code>Point</code>s that will be added to the returned builder.
     * @param flag the <code>DimensionalFlag</code> for the <code>PointSequence</code> being built by the returned builder.
     * @return a <code>PointSequenceBuilder</code> that builds a <code>PointSequence</code> containing <code>numPoints</code> <code>Point</code>s.
     */
    public static PointSequenceBuilder fixedSized(int numPoints, DimensionalFlag flag){
        return new FixedSizePointSequenceBuilder(numPoints, flag);
    }

    /**
     * Creates a <code>PointSequenceBuilder</code> of variable size.
     *
     * @param flag the <code>DimensionalFlag</code> for the <code>PointSequence</code> being built by the returned builder.
     * @return a <code>PointSequenceBuilder</code> that builds a <code>PointSequence</code> for an arbitrary number of <code>Point</code>s.
     */
    public static PointSequenceBuilder variableSized(DimensionalFlag flag) {
        return new VariableSizePointSequenceBuilder(flag);
    }

}
