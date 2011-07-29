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

import org.geolatte.geom.crs.CartesianCoordinateSystem;
import org.geolatte.geom.PointSequenceBuilder;
import org.geolatte.geom.FixedSizePointSequenceBuilder;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class WKTTokenizer {

    private final CharSequence wkt;
    private final WKTWordMatcher wordMatcher;
    private int currentPos = 0;
    private boolean isMeasured = false;

    WKTTokenizer(CharSequence wkt, WKTWordMatcher wordMatcher) {
        this.wkt = wkt;
        this.wordMatcher = wordMatcher;
    }

    public boolean moreTokens() {
        skipWhitespace();
        return this.currentPos < wkt.length();
    }

    public WKTToken nextToken() {
        if (!moreTokens()) {
            return WKTToken.end();
        }
        if (wkt.charAt(currentPos) == '(') {
            currentPos++;
            return WKTToken.startList();
        } else if (wkt.charAt(currentPos) == ')') {
            currentPos++;
            return WKTToken.endList();
        } else if (Character.isLetter(wkt.charAt(currentPos))) {
            return readWord();
        } else if (Character.isDigit(wkt.charAt(currentPos)) || wkt.charAt(currentPos) == '.' || wkt.charAt(currentPos) == '-') {
            return readPointList();
        } else if (wkt.charAt(currentPos) == ',') {
            currentPos++;
            return WKTToken.elementSeparator();
        } else {
            throw new WKTParseException(String.format("Illegal Character at pos %d in WKT text: %s", currentPos, wkt));
        }

    }

    //TODO -- handle irregular whitespace in reading the points.
    private WKTToken readPointList() {
        CartesianCoordinateSystem coordinateSystem = countDimension();
        int numPoints = countPoints();
        double[] coords = new double[coordinateSystem.getCoordinateDimension()];
        PointSequenceBuilder psBuilder = new FixedSizePointSequenceBuilder(numPoints, coordinateSystem);
        for (int i = 0; i < numPoints; i++) {
            readPoint(coords);
            psBuilder.add(coords);
            skipPointDelimiter();
        }
        return WKTToken.pointSquence(psBuilder.toPointSequence());
    }

    private void readPoint(double[] coords) {
        for (int i = 0; i < coords.length; i++) {
            coords[i] = readNumber();
        }
    }

    /**
     * Reads a number at the current position.
     *
     * TODO -- this does not handle approximate numbers (see the WKT spec). Approximate numbers have format: \d+E\d)
     *
     *
     * @return
     */
    protected double readNumber() {
        skipWhitespace();
        double d = 0.0d;
        char c = wkt.charAt(currentPos);
        double sign = 1.0d;
        if (c == '-') {
            sign = -1.0d;
            c = wkt.charAt(++currentPos);
        }
        while (Character.isDigit(c)) {
            d = 10 * d + (c - '0');
            c = wkt.charAt(++currentPos);
        }
        if (c == '.') {
            c = wkt.charAt(++currentPos);
            double divisor = 10d;
            while (Character.isDigit(c)) {
                double f = (c - '0');
                d += f / divisor;
                divisor *= 10d;
                c = wkt.charAt(++currentPos);
            }
            return sign*d;
        } else {
            return sign*d;
        }
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
     * and taking into account if the tokenizer has already determined that the current WKT geometery
     * is measured or not.
     *
     * @return
     */
    private CartesianCoordinateSystem countDimension() {
        int pos = currentPos;
        int num = 1;
        boolean inNumber = true;
        //move to the end of this point (ends with a ',' or a ')'
        char c = wkt.charAt(pos);
        while (!(c == ',' || c == ')')) {
            if (!(Character.isDigit(c) || c == '.' || c == '-')) {
                inNumber = false;
            } else if (!inNumber) {
                num++;
                inNumber = true;
            }
            if (pos == wkt.length() - 1) throw new WKTParseException("");
            c = wkt.charAt(++pos);
        }
        if (num == 4) return CartesianCoordinateSystem.XYZM;
        if (num == 3 && isMeasured) return CartesianCoordinateSystem.XYM;
        if (num == 3 && !isMeasured) return CartesianCoordinateSystem.XYZ;
        if (num == 2) return CartesianCoordinateSystem.XY;
        throw new WKTParseException("Point with less than 2 coordinates at position " + currentPos);
    }

    private void skipWhitespace() {
        while (currentPos < wkt.length() && Character.isWhitespace(wkt.charAt(currentPos))) {
            currentPos++;
        }
    }

    private void skipPointDelimiter() {
        skipWhitespace();
        if (wkt.charAt(currentPos) == ',') currentPos++;
    }

    private WKTToken readWord() {
        int endPos = this.currentPos;
        while (endPos < wkt.length() && Character.isLetter(wkt.charAt(endPos))) {
            endPos++;
        }
        WKTToken nextToken = matchWord(currentPos, endPos);
        currentPos = endPos;
        return nextToken;
    }

    /**
     * Determines if the current word matches a WKT symbol.
     * <p/>
     * <p>As a side-effect it informs this WKTTokenizer whether to expect coordinates with an M-coordinate.</p>
     *
     * @param currentPos
     * @param endPos
     * @return
     */
    private WKTToken matchWord(int currentPos, int endPos) {
        WKTToken token = wordMatcher.match(wkt, currentPos, endPos);
        if (token instanceof WKTToken.Geometry) {
            this.isMeasured = ((WKTToken.Geometry) token).isMeasured();
        }
        if (token instanceof WKTToken.DimensionMarker) {
            this.isMeasured = ((WKTToken.DimensionMarker) token).isMeasured();
        }
        return token;
    }

    public int position() {
        return this.currentPos;
    }
}
