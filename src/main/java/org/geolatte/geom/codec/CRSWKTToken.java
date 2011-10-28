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

import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Models the tokens in CrsRegistry WKT strings.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
class CRSWKTToken extends WKTToken {


    static final CRSWKTToken PROJCS = new CRSWKTToken("PROJCS");
    static final CRSWKTToken PROJECTION = new CRSWKTToken("PROJECTION");
    static final CRSWKTToken GEOGCS = new CRSWKTToken("GEOGCS");
    static final CRSWKTToken GEOCCS = new CRSWKTToken("GEOCCS");
    static final CRSWKTToken DATUM = new CRSWKTToken("DATUM");
    static final CRSWKTToken SPHEROID = new CRSWKTToken("SPHEROID");
    static final CRSWKTToken PRIMEM = new CRSWKTToken("PRIMEM");
    static final CRSWKTToken AUTHORITY = new CRSWKTToken("AUTHORITY");
    static final CRSWKTToken AXIS = new CRSWKTToken("AXIS");
    static final CRSWKTToken NORTH = new CRSWKTToken("NORTH");
    static final CRSWKTToken SOUTH = new CRSWKTToken("SOUTH");
    static final CRSWKTToken EAST = new CRSWKTToken("EAST");
    static final CRSWKTToken WEST = new CRSWKTToken("WEST");
    static final CRSWKTToken UP = new CRSWKTToken("UP");
    static final CRSWKTToken DOWN = new CRSWKTToken("DOWN");
    static final CRSWKTToken OTHER = new CRSWKTToken("OTHER");
    static final CRSWKTToken UNKNOWN = new CRSWKTToken("UNKNOWN");
    static final CRSWKTToken PARAMETER = new CRSWKTToken("PARAMETER");
    static final CRSWKTToken UNIT = new CRSWKTToken("UNIT");
    static final CRSWKTToken TOWGS84 = new CRSWKTToken("TOWGS84");

    private final Pattern pattern;

    private CRSWKTToken(String word) {
        this.pattern =  Pattern.compile(word, CASE_INSENSITIVE);
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }
}
