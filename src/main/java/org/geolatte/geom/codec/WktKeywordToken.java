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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * A <code>WktToken</code> that represents a keyword
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/19/11
 */
class WktKeywordToken implements WktToken {

    private final Pattern pattern;

    /**
     * Constructs the token.
     *
     * @param word the regex pattern to identify the keyword in the input text.
     */
    public WktKeywordToken(String word) {
        this.pattern = Pattern.compile(word, CASE_INSENSITIVE);
    }

    /**
     * Attempts to match the specified subsequence of a <code>CharSequence</code> to this <code>WktToken</code>.
     *
     * @param wkt        the input text
     * @param currentPos the start position for the match
     * @param endPos     the end position for the match
     * @throws WktDecodeException if nothing matches at the specified substring.
     */
    public boolean matches(CharSequence wkt, int currentPos, int endPos) {
        Matcher m = getPattern().matcher(wkt);
        m.region(currentPos, endPos);
        return m.matches();
    }

    /**
     * Returns the Regex pattern for this token.
     *
     * @return
     */
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }

}
