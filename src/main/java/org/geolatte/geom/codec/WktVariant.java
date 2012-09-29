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


import java.util.Set;

/**
 * The punctuation characters and keywords for a particular dialect of WKT.
 * <p/>
 * <p>There exist several dailects for WKT (e.g. postgis EWKT, SFS version 1.2.1 WKT). Instances of <code>WktVariant</code>
 * capture these differences. This allows easy customization of the <code>WktTokenizer</code> for particular WKT variants
 * or dialects. </p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
abstract class WktVariant {

    private final WktPunctuationToken openList;
    private final WktPunctuationToken closeList;
    private final WktPunctuationToken elementSeparator;
    private final WktPunctuationToken end = new WktPunctuationToken(' ');

    /**
     * Constructs an instance with the specified punctuation tokens.
     *
     * @param openList         the character that indicates the start of a list, e.g. '(' or '['
     * @param closeList        the character that indicates the end of a list, e.g. ')' or ']'
     * @param elementSeparator the character that separates individual elements in a structure (usually ',')
     */
    protected WktVariant(char openList, char closeList, char elementSeparator) {
        this.openList = new WktPunctuationToken(openList);
        this.closeList = new WktPunctuationToken(closeList);
        this.elementSeparator = new WktPunctuationToken(elementSeparator);
    }


    /**
     * Attempts to match the specified subsequence of a <code>CharSequence</code> to a keyword for the
     * this <code>WktVariant</code>.
     *
     * @param wkt        the input text
     * @param currentPos the start position for the match
     * @param endPos     the end position for the match
     * @return a matching <code>WktKeywordToken</code>
     * @throws WktDecodeException if nothing matches at the specified substring.
     */
    public WktKeywordToken matchKeyword(CharSequence wkt, int currentPos, int endPos) {
        for (WktKeywordToken token : getWktKeywords()) {
            if (token.matches(wkt, currentPos, endPos)) {
                return token;
            }
        }
        throw new WktDecodeException(
                String.format(
                        "Can't interpret word %s in Wkt.",
                        wkt.subSequence(currentPos, endPos)
                )
        );
    }

    /**
     * Returns all <code>WktPatternTokens</code> in this instance.
     *
     * @return
     */
    protected abstract Set<WktKeywordToken> getWktKeywords();

    public WktPunctuationToken getOpenList() {
        return this.openList;
    }

    public WktPunctuationToken getCloseList() {
        return closeList;
    }

    public WktPunctuationToken getElementSeparator() {
        return elementSeparator;
    }

    public WktPunctuationToken end() {
        return end;
    }

    public char getOpenListChar() {
        return getOpenList().getChar();
    }

    public char getCloseListChar() {
        return getCloseList().getChar();
    }

    public char getElemSepChar() {
        return getElementSeparator().getChar();
    }
}
