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

import java.util.Arrays;


/**
 * A factory for <code>PointSequence</code>s.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PointCollectionFactory {

    //TODO remove this class

    /**
     * Creates an empty <code>PointSequence</code>.
     *
     * @return an empty <code>PointSequence</code>
     */
    public static PointSequence createEmpty(){
        return EmptyPointSequence.INSTANCE;
    }

    /**
     * Creates a <code>PointSequence</code> from a coordinate array.
     *
     * <p>The coordinate array is copied to the new <code>PointSequence</code>.</p>
     * @param coordinates the coordinates of the new <code>PointSequence</code>.
     * @param dimensionalFlag the dimensions for the new <code>PointSequence</code>
     * @return a <code>PointSequence</code> with the specified coordinates and having the specified <code>DimensionalFlag</code>.
     */
    public static PointSequence create(double[] coordinates, DimensionalFlag dimensionalFlag, CrsId crsId){
        return  new PackedPointSequence(Arrays.copyOf(coordinates, coordinates.length), dimensionalFlag, crsId);
    }

    /**
     * Creates a <code>PointCollection</code> from the specified array of <code>PointCollection</code>s.
     *
     * <p>If the specified <code>PointCollection</code> array is null or empty, an empty <code>PointSequence</code>
     * will be created; otherwise a <code>ComplexPointCollection</code>.</p>
     *
     * @param pointCollections the <code>PointCollection</code> to create the <code>PointCollection</code> from
     * @return the constructed <code>PointCollection</code>
     */
    public static PointCollection create(PointCollection[] pointCollections) {
        if (pointCollections == null || pointCollections.length == 0 ) {
            return EmptyPointSequence.INSTANCE;
        }
        return new NestedPointCollection(pointCollections);
    }


}


