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

package org.geolatte.geom.benchmark;

import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.*;

import java.util.Random;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 4/26/11
 */
public class RandomGeometryGenerator {

    private final static double MAX_COORDINATE_VALUE = 90;
    private final static double MIN_COORDINATE_VALUE = -90;
    private static final int MAX_LINESTRING_LENGTH = 100;
    private static final Random random = new Random();

    public static double[] randomCoordinate(DimensionalFlag dim) {
        double[] co = new double[dim.getCoordinateDimension()];
        for (int i = 0; i < dim.getCoordinateDimension(); i++) {
            co[i] = MIN_COORDINATE_VALUE + random.nextDouble() *(MAX_COORDINATE_VALUE - MIN_COORDINATE_VALUE);
        }
        return co;
    }

    public static Geometry[] createRandomGeometries(GeometryType type, int num, DimensionalFlag dim) {
        Geometry[] result = new Geometry[num];
        for (int i = 0; i < num; i++) {
            result[i] = createRandomGeometry(type, dim);
        }
        return result;
    }

    private static Geometry createRandomGeometry(GeometryType type, DimensionalFlag dim) {
        switch (type) {
            case POINT:
                return createRandomPoint(dim);
            case LINE_STRING:
                return createRandomLengthLineString(dim);
        }
        throw new IllegalStateException();
    }



    public static Point createRandomPoint(DimensionalFlag dim){
        return Point.create(randomCoordinate(dim), dim, -1);
    }

    public static LineString createRandomLengthLineString(DimensionalFlag dim){
        int length = randomLineStringLength();
        PointSequenceBuilder builder = new FixedSizePointSequenceBuilder(length, dim);
        for (int i = 0; i < length; i++) {
            builder.add(randomCoordinate(dim));
        }
        return LineString.create(builder.toPointSequence(), -1);
    }

    private static int randomLineStringLength() {
        int r = random.nextInt(MAX_LINESTRING_LENGTH);
        while (r < 2){
            r = random.nextInt(MAX_LINESTRING_LENGTH);
        }
        return r;
    }
}
