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
import org.geolatte.geom.FixedSizePointSequenceBuilder;
import org.geolatte.geom.PointSequenceBuilder;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class WktTokenizer {

    private final CharSequence wkt;
    private final WktWordMatcher wordMatcher;
    private int currentPos = 0;
    private boolean isMeasured = false;
    private char openListChar = '(';
    private char closeListChar = ')';
    private boolean pointListAsSingleToken = true;


    WktTokenizer(CharSequence wkt, WktWordMatcher wordMatcher) {
        this.wkt = wkt;
        this.wordMatcher = wordMatcher;
    }

    /**
     * A Tokenizer for Wkt strings
     * @param wkt the string to tokenize
     * @param wordMatcher the list of words to recognize as separate tokens
     * @param openListChar the "open list" character
     * @param closeListChar the "close list" character
     * @param pointListAsSingelNumber this treats any substring consisting of list delimiters and numeric characters as a single pointlist
     */
    WktTokenizer(CharSequence wkt, WktWordMatcher wordMatcher, char openListChar, char closeListChar, boolean pointListAsSingelNumber) {
        this(wkt, wordMatcher);
        this.openListChar = openListChar;
        this.closeListChar = closeListChar;
        this.pointListAsSingleToken = pointListAsSingelNumber;
    }

    public boolean moreTokens() {
        skipWhitespace();
        return this.currentPos < wkt.length();
    }

    public WktToken nextToken() {
        if (!moreTokens()) {
            return WktToken.end();
        }
        if (wkt.charAt(currentPos) == openListChar) {
            currentPos++;
            return WktToken.startList();
        } else if (wkt.charAt(currentPos) == closeListChar) {
            currentPos++;
            return WktToken.endList();
        } else if (wkt.charAt(currentPos) == '"') {
            return readText();
        } else if (Character.isLetter(wkt.charAt(currentPos))) {
            return readWord();
        } else if (Character.isDigit(wkt.charAt(currentPos)) || wkt.charAt(currentPos) == '.' || wkt.charAt(currentPos) == '-') {
            if (pointListAsSingleToken){
                return readPointList();
            } else {
                return readNumberToken();
            }
        } else if (wkt.charAt(currentPos) == ',') {
            currentPos++;
            return WktToken.elementSeparator();
        } else {
            throw new WktParseException(String.format("Illegal Character at pos %d in Wkt text: %s", currentPos, wkt));
        }

    }

    //TODO -- handle irregular whitespace in reading the points.
    private WktToken readPointList() {
        DimensionalFlag dimensionalFlag = countDimension();
        int numPoints = countPoints();
        double[] coords = new double[dimensionalFlag.getCoordinateDimension()];
        PointSequenceBuilder psBuilder = new FixedSizePointSequenceBuilder(numPoints, dimensionalFlag);
        for (int i = 0; i < numPoints; i++) {
            readPoint(coords);
            psBuilder.add(coords);
            skipPointDelimiter();
        }
        return WktToken.pointSquence(psBuilder.toPointSequence());
    }

    private void readPoint(double[] coords) {
        for (int i = 0; i < coords.length; i++) {
            coords[i] = fastReadNumber();
        }
    }

    /**
     * Reads a number at the current position.
     *
     * TODO -- this does not handle approximate numbers (see the Wkt spec). Approximate numbers have format: \d+E\d)
     * TODO -- this has also problems in that it loses precision (cfr. 51.16666723333333 becomes 51.16666723333332)
     *
     *
     * @return
     */
    protected double fastReadNumber() {
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

    protected double readNumber() {
        skipWhitespace();
        StringBuilder stb = new StringBuilder();
        char c = wkt.charAt(currentPos);
        if (c == '-') {
            stb.append(c);
            c = wkt.charAt(++currentPos);
        }
        while (Character.isDigit(c)) {
            stb.append(c);
            c = wkt.charAt(++currentPos);
        }
        if (c == '.') {
            stb.append(c);
            c = wkt.charAt(++currentPos);
            while (Character.isDigit(c)) {
                stb.append(c);
                c = wkt.charAt(++currentPos);
            }
        }
        return Double.parseDouble(stb.toString());
    }


    protected WktToken readNumberToken(){
        double d = readNumber();
        return new WktToken.NumberToken(d);
    }

    protected WktToken readText(){
        StringBuilder builder = new StringBuilder();
        char c = wkt.charAt(++currentPos);
        while ( c != '"') {
            builder.append(c);
            c = wkt.charAt(++currentPos);
        }
        currentPos++;
        return new WktToken.TextToken(builder.toString());
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
        while (!(c == ',' || c == closeListChar)) {
            if (!(Character.isDigit(c) || c == '.' || c == '-')) {
                inNumber = false;
            } else if (!inNumber) {
                num++;
                inNumber = true;
            }
            if (pos == wkt.length() - 1) throw new WktParseException("");
            c = wkt.charAt(++pos);
        }
        if (num == 4) return DimensionalFlag.XYZM;
        if (num == 3 && isMeasured) return DimensionalFlag.XYM;
        if (num == 3 && !isMeasured) return DimensionalFlag.XYZ;
        if (num == 2) return DimensionalFlag.XY;
        throw new WktParseException("Point with less than 2 coordinates at position " + currentPos);
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

    private WktToken readWord() {
        int endPos = this.currentPos;
        while (endPos < wkt.length() && isWordChar(wkt.charAt(endPos))) {
            endPos++;
        }
        WktToken nextToken = matchWord(currentPos, endPos);
        currentPos = endPos;
        return nextToken;
    }

    private boolean isWordChar(char c) {
        return (Character.isLetter(c) || Character.isDigit(c) || c == '_');
    }

    /**
     * Determines if the current word matches a Wkt symbol.
     * <p/>
     * <p>As a side-effect it informs this WktTokenizer whether to expect coordinates with an M-coordinate.</p>
     *
     * @param currentPos
     * @param endPos
     * @return
     */
    private WktToken matchWord(int currentPos, int endPos) {
        WktToken token = wordMatcher.match(wkt, currentPos, endPos);
        if (token instanceof WktToken.Geometry) {
            this.isMeasured = ((WktToken.Geometry) token).isMeasured();
        }
        if (token instanceof WktToken.DimensionMarker) {
            this.isMeasured = ((WktToken.DimensionMarker) token).isMeasured();
        }
        return token;
    }

    public int position() {
        return this.currentPos;
    }
}
