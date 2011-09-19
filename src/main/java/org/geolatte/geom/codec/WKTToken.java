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

package org.geolatte.geom.codec;

import org.geolatte.geom.DimensionalFlag;
import org.geolatte.geom.GeometryType;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
abstract class WKTToken {

    private static final WKTToken END = new End();
    private static final WKTToken START_LIST = new StartList();
    private static final WKTToken END_LIST = new EndList();
    private static final WKTToken EMPTY = new Empty();
    private static final WKTToken ELEMENT_SEP = new ElementSeparator();

    static WKTToken geometryTag(GeometryType type, boolean measured) {
        return new Geometry(type, measured);
    }

    static WKTToken startList() {
        return START_LIST;
    }

    static WKTToken endList() {
        return END_LIST;
    }

    static WKTToken pointSquence(org.geolatte.geom.PointSequence sequence) {
        return new PointSequence(sequence);
    }

    static WKTToken dimensionMarker(DimensionalFlag flag) {
        return new DimensionMarker(flag);
    }

    public static WKTToken end() {
        return END;
    }

    public static WKTToken elementSeparator() {
        return ELEMENT_SEP;
    }

    public static WKTToken empty() {
        return EMPTY;
    }

    static class Geometry extends WKTToken {
        private GeometryType geometryType;
        private boolean isMeasured;

        Geometry(GeometryType tag, boolean measured) {
            this.geometryType = tag;
            this.isMeasured = measured;
        }

        GeometryType getType() {
            return this.geometryType;
        }

        boolean isMeasured() {
            return this.isMeasured;
        }

    }

    static class End extends WKTToken {
    }

    static class StartList extends WKTToken {
    }

    static class EndList extends WKTToken {
    }

    static class Empty extends WKTToken {
    }

    static class PointSequence extends WKTToken {
        private org.geolatte.geom.PointSequence points;

        PointSequence(org.geolatte.geom.PointSequence points) {
            this.points = points;
        }

        org.geolatte.geom.PointSequence getPoints() {
            return this.points;
        }
    }

    static class DimensionMarker extends WKTToken {
        private DimensionalFlag dimensionalFlag;

        DimensionMarker(DimensionalFlag flag) {
            this.dimensionalFlag = flag;
        }

        public boolean isMeasured() {
            return this.dimensionalFlag.isMeasured();
        }

        public boolean is3D() {
            return this.dimensionalFlag.is3D();
        }
    }

    static class ElementSeparator extends WKTToken {
    }

    static class TextToken extends WKTToken {
        private String text;

        TextToken(String text){
            super();
            this.text = text;
        }

        public String getText(){
            return this.text;
        }
    }

    static class NumberToken extends WKTToken {
        private double number;

        public NumberToken(double number) {
            super();
            this.number = number;
        }

        public double getNumber(){
            return this.number;
        }
    }
}

