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
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Arrays;

import static org.geolatte.geom.crs.CommonCoordinateReferenceSystems.*;

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
        CoordinateReferenceSystem<?> crs = determineCRS(coordinates);
        return fromCoordinateArray(coordinates, crs.getPositionClass());
    }

    @Override
    public CoordinateSequence create(CoordinateSequence coordSeq) {
        return coordSeq;
    }

    @Override
    public CoordinateSequence create(int size, int dimension) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public <P extends Position> PositionSequence<P> toPositionSequence(CoordinateSequence cs, Class<P> posType) {
        if (cs instanceof PositionSequence &&
                ((PositionSequence) cs).getPositionClass().equals(posType)) {
            return (PositionSequence<P>) cs;
        }
        PositionTypeDescriptor<?> desc = Positions.getDescriptor(posType);

        Coordinate c = new Coordinate();
        double[] psc = new double[desc.getCoordinateDimension()];
        Arrays.fill(psc, Double.NaN);
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(cs.size(), posType);
        for (int i = 0; i < cs.size(); i++) {
            psc[0] = cs.getOrdinate(i, 0);
            psc[1] = cs.getOrdinate(i, 1);
            if(desc.hasVerticalComponent()) {
                psc[2] = cs.getOrdinate(i, 2);
            }
            builder.add(psc);
        }
        return builder.toPositionSequence();
    }

    private CoordinateReferenceSystem<?> determineCRS(Coordinate[] coordinates) {
        boolean hasZ, hasM = false;
        if (coordinates == null || coordinates.length == 0) return PROJECTED_2D_METER;
        if (coordinates[0] instanceof DimensionalCoordinate) {
            hasM = !Double.isNaN(((DimensionalCoordinate) coordinates[0]).getM());
        }
        hasZ = !Double.isNaN(coordinates[0].z);
        if (hasM && hasZ) {
            return PROJECTED_3DM_METER;
        } else if (hasM) {
            return PROJECTED_2DM_METER;
        } else if (hasZ) {
            return PROJECTED_3D_METER;
        } else {
            return PROJECTED_2D_METER;
        }
    }

    private <P extends Position> CoordinateSequence fromCoordinateArray(Coordinate[] coordinates, Class<P> posType) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(coordinates.length, posType);
        PositionTypeDescriptor<P> descriptor = Positions.getDescriptor(posType);
        double[] ordinates = new double[descriptor.getCoordinateDimension()];
        for (Coordinate co : coordinates) {
            copy(co, ordinates, descriptor);
            builder.add(ordinates);
        }
        return (CoordinateSequence) builder.toPositionSequence();
    }

    private <P extends Position> void copy(Coordinate co, double[] ordinates, PositionTypeDescriptor<P> desc) {
        ordinates[0] = co.x;
        ordinates[1] = co.y;
        if (desc.hasVerticalComponent()){
            ordinates[desc.getVerticalComponentIndex()] = co.z;
        }
        if (desc.hasMeasureComponent()) {
            ordinates[desc.getMeasureComponentIndex()] = (co instanceof DimensionalCoordinate) ?
                    ((DimensionalCoordinate) co).m : Double.NaN;
        }
    }

}
