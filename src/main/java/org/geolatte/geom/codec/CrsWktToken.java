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
 * A token in a WKT representation of a Coordinate Reference System.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
class CrsWktToken extends WktToken {


    static final CrsWktToken PROJCS = new CrsWktToken("PROJCS");
    static final CrsWktToken PROJECTION = new CrsWktToken("PROJECTION");
    static final CrsWktToken GEOGCS = new CrsWktToken("GEOGCS");
    static final CrsWktToken GEOCCS = new CrsWktToken("GEOCCS");
    static final CrsWktToken DATUM = new CrsWktToken("DATUM");
    static final CrsWktToken SPHEROID = new CrsWktToken("SPHEROID");
    static final CrsWktToken PRIMEM = new CrsWktToken("PRIMEM");
    static final CrsWktToken AUTHORITY = new CrsWktToken("AUTHORITY");
    static final CrsWktToken AXIS = new CrsWktToken("AXIS");
    static final CrsWktToken NORTH = new CrsWktToken("NORTH");
    static final CrsWktToken SOUTH = new CrsWktToken("SOUTH");
    static final CrsWktToken EAST = new CrsWktToken("EAST");
    static final CrsWktToken WEST = new CrsWktToken("WEST");
    static final CrsWktToken UP = new CrsWktToken("UP");
    static final CrsWktToken DOWN = new CrsWktToken("DOWN");
    static final CrsWktToken OTHER = new CrsWktToken("OTHER");
    static final CrsWktToken UNKNOWN = new CrsWktToken("UNKNOWN");
    static final CrsWktToken PARAMETER = new CrsWktToken("PARAMETER");
    static final CrsWktToken UNIT = new CrsWktToken("UNIT");
    static final CrsWktToken TOWGS84 = new CrsWktToken("TOWGS84");

    private final Pattern pattern;

    private CrsWktToken(String word) {
        this.pattern =  Pattern.compile(word, CASE_INSENSITIVE);
    }

    /**
     * Returns the Regex pattern for this token.
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
