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

package org.geolatte.geom.codec;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/9/12
 */
abstract class AbstractWktTokenizer {
    protected final CharSequence wkt;
    protected final WktVariant variant;
    protected int currentPos = 0;

    AbstractWktTokenizer(CharSequence wkt, WktVariant variant) {
        this.wkt = wkt;
        this.variant = variant;
    }

    boolean moreTokens() {
        skipWhitespace();
        return this.currentPos < wkt.length();
    }

    // this is just temporarily for testing
    WktToken nextToken() {
        WktToken token = inner();
//        System.out.println("NEXT TOKEN: " + token);
        return token;
    }

    private WktToken inner() {
        if (!moreTokens()) {
            return variant.end();
        }
        if (wkt.charAt(currentPos) == variant.getOpenListChar()) {
            currentPos++;
            return variant.getOpenList();
        } else if (wkt.charAt(currentPos) == variant.getCloseListChar()) {
            currentPos++;
            return variant.getCloseList();
        } else if (wkt.charAt(currentPos) == '"') {
            return readText();
        } else if (Character.isLetter(wkt.charAt(currentPos))) {
            return readToken();
        } else if (Character.isDigit(wkt.charAt(currentPos)) || wkt.charAt(currentPos) == '.' || wkt.charAt(currentPos) == '-') {
            return numericToken();
        } else if (wkt.charAt(currentPos) == variant.getElemSepChar()) {
            currentPos++;
            return variant.getElementSeparator();
        } else {
            throw new WktDecodeException(String.format("Illegal Character at pos %d in Wkt text: %s", currentPos, wkt));
        }

    }

    abstract WktToken numericToken();

    protected double readNumber() {
        skipWhitespace();
        StringBuilder stb = new StringBuilder();
        char c = wkt.charAt(currentPos);
        if (c == '-') {
            stb.append(c);
            c = wkt.charAt(++currentPos);
        }
        c = readDigits(stb, c);
        if (c == '.') {
            stb.append(c);
            c = wkt.charAt(++currentPos);
            readDigits(stb, c);
        }
        return Double.parseDouble(stb.toString());
    }

    private char readDigits(StringBuilder stb, char c) {
        while (Character.isDigit(c)) {
            stb.append(c);
            c = wkt.charAt(++currentPos);
        }
        return c;
    }

    protected WktToken readNumberToken() {
        double d = readNumber();
        return new WktNumberToken(d);
    }

    protected WktToken readText() {
        StringBuilder builder = new StringBuilder();
        char c = wkt.charAt(++currentPos);
        while (c != '"') {
            builder.append(c);
            c = wkt.charAt(++currentPos);
        }
        currentPos++;
        return new WktTextToken(builder.toString());
    }

    protected void skipWhitespace() {
        while (currentPos < wkt.length() && Character.isWhitespace(wkt.charAt(currentPos))) {
            currentPos++;
        }
    }

    protected WktToken readToken() {
        int endPos = this.currentPos;
        while (endPos < wkt.length() && isWordChar(wkt.charAt(endPos))) {
            endPos++;
        }
        WktToken nextToken = matchKeyword(currentPos, endPos);
        currentPos = endPos;
        return nextToken;
    }

    protected boolean isWordChar(char c) {
        return (Character.isLetter(c) || Character.isDigit(c) || c == '_');
    }

    /**
     * Matches the specifed subsequence of the WKT to a <code>WktToken</code>.
     * <p/>
     * @param currentPos the start of the subsequence to match
     * @param endPos     the end of the subsequence to match
     * @return the token that matches the specified subsequence
     * @throws org.geolatte.geom.codec.WktDecodeException if the specified subsequence does not match a token.
     */
    protected WktToken matchKeyword(int currentPos, int endPos) {
        return variant.matchKeyword(wkt, currentPos, endPos);
    }

    public int position() {
        return this.currentPos;
    }
}
