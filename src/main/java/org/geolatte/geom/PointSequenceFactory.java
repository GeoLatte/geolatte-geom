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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

import java.util.Arrays;


/**
 * A factory for <code>PointSequence</code>s.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PointSequenceFactory  implements CoordinateSequenceFactory {

    public static PointSequence createEmpty(){
        return EmptyPointSequence.INSTANCE;
    }

    public static PointSequence create(double[] coordinates, DimensionalFlag dimensionalFlag){
        return  new PackedPointSequence(Arrays.copyOf(coordinates, coordinates.length), dimensionalFlag);
    }

    @Override
    public CoordinateSequence create(Coordinate[] coordinates) {
        DimensionalFlag flag = determineDimensionFlag(coordinates);
        return AbstractPointSequence.fromCoordinateArray(coordinates, flag);
    }

    @Override
    public CoordinateSequence create(CoordinateSequence coordSeq) {
        return coordSeq;
    }

    @Override
    public CoordinateSequence create(int size, int dimension) {
        throw new UnsupportedOperationException();
    }

    private DimensionalFlag determineDimensionFlag(Coordinate[] coordinates) {
        if (coordinates == null || coordinates.length == 0) return DimensionalFlag.XY;
        if (coordinates[0] instanceof DimensionalCoordinate) return ((DimensionalCoordinate)coordinates[0]).getDimensionalFlag();
        return DimensionalFlag.XYZ;
    }
}


