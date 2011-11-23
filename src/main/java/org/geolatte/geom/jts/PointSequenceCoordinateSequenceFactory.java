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

package org.geolatte.geom.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import org.geolatte.geom.*;

/**
 * A <code>CoordinateSequenceFactory</code> that creates <code>PointSequences</code> (which extend
 * <code>CoordinateSequence</code>s).
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/22/11
 */
class PointSequenceCoordinateSequenceFactory implements CoordinateSequenceFactory {

    @Override
    public CoordinateSequence create(Coordinate[] coordinates) {
        DimensionalFlag flag = determineDimensionFlag(coordinates);
        return fromCoordinateArray(coordinates, flag);
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

    private CoordinateSequence fromCoordinateArray(Coordinate[] coordinates, DimensionalFlag dim) {
        PointSequenceBuilder builder = PointSequenceBuilderFactory.newFixedSizePointSequenceBuilder(coordinates.length, dim);
        double[] ordinates = new double[dim.getCoordinateDimension()];
        for (Coordinate co : coordinates) {
            copy(co,ordinates, dim);
            builder.add(ordinates);
        }
        return (CoordinateSequence)builder.toPointSequence();
    }

    private void copy(Coordinate co, double[] ordinates, DimensionalFlag flag) {
        ordinates[flag.getIndex(CoordinateComponent.X)] = co.x;
        ordinates[flag.getIndex(CoordinateComponent.Y)] = co.y;
        if (flag.is3D()) ordinates[flag.getIndex(CoordinateComponent.Z)] = co.z;
        if (flag.isMeasured()) {
            ordinates[flag.getIndex(CoordinateComponent.M)] = (co instanceof DimensionalCoordinate) ?
                   ((DimensionalCoordinate)co).m :
                    Double.NaN;
        }
    }

}
