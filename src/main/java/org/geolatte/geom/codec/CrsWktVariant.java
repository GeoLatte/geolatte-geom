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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The <code>WktVariant</code> used in the WKT representations of Coordinate Reference Systems.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
class CrsWktVariant extends WktVariant {

    public static final WktKeywordToken PROJCS = new WktKeywordToken("PROJCS");
    public static final WktKeywordToken PROJECTION = new WktKeywordToken("PROJECTION");
    public static final WktKeywordToken GEOGCS = new WktKeywordToken("GEOGCS");
    public static final WktKeywordToken GEOCCS = new WktKeywordToken("GEOCCS");
    public static final WktKeywordToken VERT_CS = new WktKeywordToken("VERT_CS");
    public static final WktKeywordToken VERT_DATUM = new WktKeywordToken("VERT_DATUM");
    public static final WktKeywordToken COMPD_CS = new WktKeywordToken("COMPD_CS");
    public static final WktKeywordToken DATUM = new WktKeywordToken("DATUM");
    public static final WktKeywordToken SPHEROID = new WktKeywordToken("SPHEROID");
    public static final WktKeywordToken PRIMEM = new WktKeywordToken("PRIMEM");
    public static final WktKeywordToken AUTHORITY = new WktKeywordToken("AUTHORITY");
    public static final WktKeywordToken AXIS = new WktKeywordToken("AXIS");
    public static final WktKeywordToken NORTH = new WktKeywordToken("NORTH");
    public static final WktKeywordToken SOUTH = new WktKeywordToken("SOUTH");
    public static final WktKeywordToken EAST = new WktKeywordToken("EAST");
    public static final WktKeywordToken WEST = new WktKeywordToken("WEST");
    public static final WktKeywordToken UP = new WktKeywordToken("UP");
    public static final WktKeywordToken DOWN = new WktKeywordToken("DOWN");
    public static final WktKeywordToken OTHER = new WktKeywordToken("OTHER");
    public static final WktKeywordToken UNKNOWN = new WktKeywordToken("UNKNOWN");
    public static final WktKeywordToken PARAMETER = new WktKeywordToken("PARAMETER");
    public static final WktKeywordToken UNIT = new WktKeywordToken("UNIT");
    public static final WktKeywordToken TOWGS84 = new WktKeywordToken("TOWGS84");
    public static final WktKeywordToken EXTENSION = new WktKeywordToken("EXTENSION");

    private final static Set<WktKeywordToken> KEYWORDS;

    static {
        Set<WktKeywordToken> set = new HashSet<WktKeywordToken>();
        set.add(PROJCS);
        set.add(PROJECTION);
        set.add(GEOGCS);
        set.add(GEOCCS);
        set.add(DATUM);
        set.add(SPHEROID);
        set.add(PRIMEM);
        set.add(AUTHORITY);
        set.add(AXIS);
        set.add(NORTH);
        set.add(SOUTH);
        set.add(EAST);
        set.add(WEST);
        set.add(UP);
        set.add(DOWN);
        set.add(OTHER);
        set.add(UNKNOWN);
        set.add(PARAMETER);
        set.add(UNIT);
        set.add(TOWGS84);
        set.add(COMPD_CS);
        set.add(VERT_CS);
        set.add(VERT_DATUM);
        set.add(EXTENSION);
        KEYWORDS = Collections.unmodifiableSet(set);
    }

    protected CrsWktVariant() {
        super('[', ']', ',');
    }


    @Override
    protected Set<WktKeywordToken> getWktKeywords() {
        return KEYWORDS;
    }

}
