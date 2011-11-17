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
import org.geolatte.geom.Geometry;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CrsWktWordMatcher extends WktWordMatcher {

    private final static Set<CrsWktToken> TOKENs;

    static {
        TOKENs = new HashSet<CrsWktToken>();
        for (Field field : CrsWktToken.class.getDeclaredFields()) {
            if ( isCRSWKTTokenField(field) ){
                try {
                    Object o = field.get(null);
                    TOKENs.add((CrsWktToken)o);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Programming error.", e);
                }
            }
        }
    }

    private static boolean isCRSWKTTokenField(Field field){
        return CrsWktToken.class.isAssignableFrom(field.getType());
    }

    @Override
    public WktToken match(CharSequence wkt, int currentPos, int endPos) {

        for (CrsWktToken token : TOKENs){
             Matcher m = token.getPattern().matcher(wkt);
             m.region(currentPos, endPos);
             if (m.matches()) {
                 return token;
             }
        }
        throw new WktParseException(String.format("Can't interpret word %s in Wkt.", wkt.subSequence(currentPos, endPos)));
    }

    @Override
    public String wordFor(Geometry geometry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String wordFor(DimensionalFlag flag) {
        throw new UnsupportedOperationException();
    }
}
