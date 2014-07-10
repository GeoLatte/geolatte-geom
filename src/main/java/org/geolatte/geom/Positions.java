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
 * Copyright (C) 2010 - 2014 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Arrays;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/3/14
 */
public class Positions {

    /**
     * Factory method for {@code Position}s in the reference system.
     * <p/>
     * The coordinates array should be in normalized order. See
     * {@link Position}
     *
     * @param coordinates the coordinates of the position
     * @return the {@code Position} with the specified coordinates.
     */
    @SuppressWarnings("unchecked")
    public static <P extends Position> P mkPosition(Class<P> pClass, double... coordinates) {
        if (pClass == P2D.class) {
            return (P) (coordinates.length == 0 ?
                    new P2D() :
                    new P2D(coordinates[0], coordinates[1]));
        } else if (pClass == P3D.class) {
            return (P)(coordinates.length == 0 ?
                    new P3D() :
                    new P3D(coordinates[0], coordinates[1], coordinates[2]));
        } else if (pClass == P2DM.class) {
            return (P)(coordinates.length == 0 ?
                    new P2DM() :
                    new P2DM(coordinates[0], coordinates[1], coordinates[2]));
        } else if (pClass == P3DM.class) {
            return (P)(coordinates.length == 0 ?
                    new P3DM() :
                    new P3DM(coordinates[0], coordinates[1], coordinates[2], coordinates[3]));
        } else if (pClass == G2D.class) {
            return (P)( coordinates.length == 0 ?
                    new G2D() :
                    new G2D(coordinates[0], coordinates[1]));
        } else if (pClass == G3D.class) {
            return (P)(coordinates.length == 0 ?
                    new G3D() :
                    new G3D(coordinates[0], coordinates[1], coordinates[2]));
        } else if (pClass == G2DM.class) {
            return (P)(coordinates.length == 0 ?
                    new G2DM() :
                    new G2DM(coordinates[0], coordinates[1], coordinates[2]));
        } else if (pClass == G3DM.class) {
            return (P)(coordinates.length == 0 ?
                    new G3DM() :
                    new G3DM(coordinates[0], coordinates[1], coordinates[2], coordinates[3]));
        }
        throw new UnsupportedOperationException(String.format("Position type %s unsupported",
                pClass.getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public static <P extends Position> P mkPosition(CoordinateReferenceSystem<P> crs, double... coordinates) {
        return mkPosition(crs.getPositionClass(), coordinates);
    }


    /**
     * Copies the source positions to a new PointSequence.
     * <p/>
     * <P>The coordinates are taken as-is. If the target coordinate reference systems has a larger coordinate
     * dimensions then the source, NaN coordinate values are created.</p>
     *
     * @param source
     * @param targetCrs
     * @param <P>
     * @return
     */
    public static <Q extends Position, P extends Position> PositionSequence<P> copy(final PositionSequence<Q> source, final CoordinateReferenceSystem<P> targetCrs) {
        final PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(source.size(), targetCrs);
        if (source.isEmpty()) return builder.toPositionSequence();
        final double[] coords = new double[Math.max(source.getCoordinateDimension(), targetCrs.getCoordinateDimension())];
        Arrays.fill(coords, Double.NaN);
        PositionVisitor<Q> visitor = new PositionVisitor<Q>() {
            public void visit(Q position) {
                position.toArray(coords);
                builder.add(coords);
            }
        };
        source.accept(visitor);
        return builder.toPositionSequence();
    }

    @SuppressWarnings("unchecked")
    private static <P extends Position> CoordinateReferenceSystem<P> cast(CoordinateReferenceSystem crs, Class<P> tp) {
        return (CoordinateReferenceSystem<P>) crs;
    }
}
