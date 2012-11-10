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
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom;

import org.geolatte.geom.builder.internal.*;
import org.geolatte.geom.crs.CrsId;

import static org.geolatte.geom.DimensionalFlag.*;
import static org.geolatte.geom.PointSequenceBuilders.variableSized;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class LinearRings {

    static public GeometryBuilder2D<LinearRing> create2D(CrsId crsId) {
        return new LinearRingBuilder2D(crsId);
    }

    static public GeometryBuilder2DM<LinearRing> create2DM(CrsId crsId) {
        return new LinearRingBuilder2DM(crsId);
    }

    static public GeometryBuilder3D<LinearRing> create3D(CrsId crsId) {
        return new LinearRingBuilder3D(crsId);
    }

    static public GeometryBuilder3DM<LinearRing> create3DM(CrsId crsId) {
        return new LinearRingBuilder3DM(crsId);
    }


    static private abstract class Builder implements SimpleGeometryBuilder<LinearRing> {
        protected PointSequenceBuilder pointSequenceBuilder;

        Builder(DimensionalFlag dimFlag, CrsId crsId) {
            pointSequenceBuilder = variableSized(dimFlag, crsId);
        }

        public LinearRing build() {
            return new LinearRing(pointSequenceBuilder.toPointSequence());
        }

    }

    static private class LinearRingBuilder2D extends Builder implements GeometryBuilder2D<LinearRing> {

        LinearRingBuilder2D(CrsId crsId) {
            super(d2D, crsId);
        }

        public LinearRingBuilder2D add(double x, double y) {
            pointSequenceBuilder.add(x, y);
            return this;
        }
    }

    static private class LinearRingBuilder2DM extends Builder implements GeometryBuilder2DM<LinearRing> {

        LinearRingBuilder2DM(CrsId crsId) {
            super(d2DM, crsId);
        }

        public LinearRingBuilder2DM add(double x, double y, double m) {
            pointSequenceBuilder.add(x, y, m);
            return this;
        }
    }

    static private class LinearRingBuilder3D extends Builder implements GeometryBuilder3D<LinearRing> {

        LinearRingBuilder3D(CrsId crsId) {
            super(d3D, crsId);
        }

        public LinearRingBuilder3D add(double x, double y, double z) {
            pointSequenceBuilder.add(x, y, z);
            return this;
        }
    }

    static private class LinearRingBuilder3DM extends Builder implements GeometryBuilder3DM<LinearRing> {

        LinearRingBuilder3DM(CrsId crsId) {
            super(d3DM, crsId);
        }

        public LinearRingBuilder3DM add(double x, double y, double z, double m) {
            pointSequenceBuilder.add(x, y, z, m);
            return this;
        }
    }

}
