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

package org.geolatte.geom.builder;

import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.LineString;
import org.geolatte.geom.PointSequenceBuilder;
import org.geolatte.geom.builder.internal.*;
import org.geolatte.geom.crs.CrsId;

import static org.geolatte.geom.DimensionalFlag.*;
import static org.geolatte.geom.PointSequenceBuilders.variableSized;

/**
 * A builder for <code>LineString</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/10/12
 */
public class LineStrings {

    static public GeometryBuilder2D<LineString> create2D(CrsId crsId) {
        return new LineStringBuilder2D(crsId);
    }

    static public GeometryBuilder2DM<LineString> create2DM(CrsId crsId) {
        return new LineStringBuilder2DM(crsId);
    }

    static public GeometryBuilder3D<LineString> create3D(CrsId crsId) {
        return new LineStringBuilder3D(crsId);
    }

    static public GeometryBuilder3DM<LineString> create3DM(CrsId crsId) {
        return new LineStringBuilder3DM(crsId);
    }


    static private abstract class Builder implements SimpleGeometryBuilder<LineString> {
        protected PointSequenceBuilder pointSequenceBuilder;

        Builder(DimensionalFlag dimFlag, CrsId crsId) {
            pointSequenceBuilder = variableSized(dimFlag, crsId);
        }

        public LineString build() {
            return new LineString(pointSequenceBuilder.toPointSequence());
        }

    }

    static private class LineStringBuilder2D extends Builder implements GeometryBuilder2D<LineString> {

        LineStringBuilder2D(CrsId crsId) {
            super(d2D, crsId);
        }

        public LineStringBuilder2D add(double x, double y) {
            pointSequenceBuilder.add(x, y);
            return this;
        }
    }

    static private class LineStringBuilder2DM extends Builder implements GeometryBuilder2DM<LineString> {

        LineStringBuilder2DM(CrsId crsId) {
            super(d2DM, crsId);
        }

        public LineStringBuilder2DM add(double x, double y, double m) {
            pointSequenceBuilder.add(x, y, m);
            return this;
        }
    }

    static private class LineStringBuilder3D extends Builder implements GeometryBuilder3D<LineString> {

        LineStringBuilder3D(CrsId crsId) {
            super(d3D, crsId);
        }

        public LineStringBuilder3D add(double x, double y, double z) {
            pointSequenceBuilder.add(x, y, z);
            return this;
        }
    }

    static private class LineStringBuilder3DM extends Builder implements GeometryBuilder3DM<LineString> {

        LineStringBuilder3DM(CrsId crsId) {
            super(d3DM, crsId);
        }

        public LineStringBuilder3DM add(double x, double y, double z, double m) {
            pointSequenceBuilder.add(x, y, z, m);
            return this;
        }
    }

}

