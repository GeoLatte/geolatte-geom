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

import org.geolatte.geom.Geometry;
import org.geolatte.geom.crs.CartesianCoordinateSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CRSWKTWordMatcher extends WKTWordMatcher{

    private static final Map<CRSWKTToken.CRSWords, Pattern> patterns = new HashMap<CRSWKTToken.CRSWords, Pattern>();

    static {
        for(CRSWKTToken.CRSWords word : CRSWKTToken.CRSWords.values()) {
            patterns.put(word, Pattern.compile(word.toString(), CASE_INSENSITIVE));
        }
    }

    @Override
    public WKTToken match(CharSequence wkt, int currentPos, int endPos) {

        for (CRSWKTToken.CRSWords word : patterns.keySet()){
             Matcher m = patterns.get(word).matcher(wkt);
             m.region(currentPos, endPos);
             if (m.matches()) {
                 return new CRSWKTToken(word);
             }
        }
        throw new WKTParseException(String.format("Can't interpret wordt %s in WKT.", wkt.subSequence(currentPos, endPos)));
    }

    @Override
    public String wordFor(Geometry geometry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String wordFor(CartesianCoordinateSystem flag) {
        throw new UnsupportedOperationException();
    }
}
