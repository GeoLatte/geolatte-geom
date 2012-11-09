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
import org.geolatte.geom.PointSequenceBuilder;
import org.geolatte.geom.PointSequenceBuilders;
import org.geolatte.geom.crs.CrsId;

/**
 * A tokenizer for WKT representations.
 * <p/>
 * <p>The variant of WKT that this tokenizer recognizes is determined by the {@link WktVariant}
 * that is passed upon construction. </p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class WktTokenizer extends AbstractWktTokenizer {

    private boolean isMeasured = false;
    private final CrsId crsId;

    /**
     * A Tokenizer for the specified WKT string
     *
     * @param wkt                     the string to tokenize
     * @param variant                 the list of words to recognize as separate variant
     * @param crsId   the <code>CrsId</code> for the points in the WKT representation.
     */
    protected WktTokenizer(CharSequence wkt, WktVariant variant, CrsId crsId) {
        super(wkt, variant);
        if (wkt == null || variant == null)
            throw new IllegalArgumentException("Input WKT and variant must not be null");
        if (crsId == null) {
            this.crsId = CrsId.UNDEFINED;
        } else {
            this.crsId = crsId;
        }
    }

    @Override
    WktToken numericToken() {
        DimensionalFlag dimensionalFlag = countDimension();
        int numPoints = countPoints();
        double[] coords = new double[dimensionalFlag.getCoordinateDimension()];
        PointSequenceBuilder psBuilder = PointSequenceBuilders.fixedSized(numPoints, dimensionalFlag, crsId);
        for (int i = 0; i < numPoints; i++) {
            readPoint(coords);
            psBuilder.add(coords);
            skipPointDelimiter();
        }
        return new WktPointSequenceToken(psBuilder.toPointSequence());
    }

    private void readPoint(double[] coords) {
        for (int i = 0; i < coords.length; i++) {
            coords[i] = fastReadNumber();
        }
    }

    /**
     * Reads a number at the current position.
     * <p>Note that this method loses precision, e.g. 51.16666723333333 becomes 51.16666723333332) <p/>
     *
     * @return
     */
    protected double fastReadNumber() {
        skipWhitespace();
        char c = wkt.charAt(currentPos);
        double sign = 1.0d;
        //read the sign
        if (c == '-') {
            sign = -1.0d;
            c = wkt.charAt(++currentPos);
        }
        //read the integer part
        double d = 0.0d;
        while (Character.isDigit(c)) {
            d = 10 * d + (c - '0');
            c = wkt.charAt(++currentPos);
        }
        //read the decimal part
        if (c == '.') {
            c = wkt.charAt(++currentPos);
            double divisor = 10d;
            while (Character.isDigit(c)) {
                double f = (c - '0');
                d += f / divisor;
                divisor *= 10d;
                c = wkt.charAt(++currentPos);
            }
        }
        //read the exponent (scientific notation)
        double exp = 0;
        double expSign = 1;
        if (c == 'e' || c == 'E') {
            c = wkt.charAt(++currentPos);
            if (c == '-')  {
                expSign = -1;
                c = wkt.charAt(++currentPos);
            }
            while (Character.isDigit(c)) {
                exp = 10 * exp + (c - '0');
                c = wkt.charAt(++currentPos);
            }
        }
        return sign * d * Math.pow(10, expSign * exp);
    }


    private int countPoints() {
        int pos = currentPos + 1;
        char c = wkt.charAt(pos);
        int num = 1;
        while (c != ')') {
            if (c == ',') num++;
            c = wkt.charAt(++pos);
        }
        return num;
    }

    /**
     * Determines the dimension by counting the number of coordinates in the current point,
     * and taking into account if the tokenizer has already determined that the current Wkt geometery
     * is measured or not.
     *
     * @return
     */
    private DimensionalFlag countDimension() {
        int pos = currentPos;
        int num = 1;
        boolean inNumber = true;
        //move to the end of this point (ends with a ',' or a ')'
        char c = wkt.charAt(pos);
        while (!(c == ',' || c == variant.getCloseListChar())) {
            if (!(Character.isDigit(c) || c == '.' || c == '-' || c == 'e' || c == 'E')) {
                inNumber = false;
            } else if (!inNumber) {
                num++;
                inNumber = true;
            }
            if (pos == wkt.length() - 1) throw new WktDecodeException("");
            c = wkt.charAt(++pos);
        }
        if (num == 4) return DimensionalFlag.d3DM;
        if (num == 3 && isMeasured) return DimensionalFlag.d2DM;
        if (num == 3 && !isMeasured) return DimensionalFlag.d3D;
        if (num == 2) return DimensionalFlag.d2D;
        throw new WktDecodeException("Point with less than 2 coordinates at position " + currentPos);
    }

    private void skipPointDelimiter() {
        skipWhitespace();
        if (wkt.charAt(currentPos) == ',') currentPos++;
    }

    protected WktToken matchKeyword(int currentPos, int endPos) {
        WktToken token = variant.matchKeyword(wkt, currentPos, endPos);
        if (token instanceof WktGeometryToken) {
            this.isMeasured = isMeasured || ((WktGeometryToken) token).isMeasured();
        }
        if (token instanceof WktDimensionMarkerToken) {
            this.isMeasured = isMeasured || ((WktDimensionMarkerToken) token).isMeasured();
        }
        return token;
    }

}
