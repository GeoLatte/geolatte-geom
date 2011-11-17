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
import org.geolatte.geom.GeometryType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Postgisv15WktWordMatcher extends WktWordMatcher {

    private static final String EMPTY = "EMPTY";
    private final static Pattern EMPTY_RE = Pattern.compile(EMPTY, Pattern.CASE_INSENSITIVE);

    private final static Map<Type, Pattern> GEOMETRY_REGEXES = new HashMap<Type, Pattern>();

    static {
        add(GeometryType.POINT, false, "POINT");
        add(GeometryType.POINT, true, "POINTM");
        add(GeometryType.LINE_STRING, true, "LINESTRINGM");
        add(GeometryType.LINE_STRING, false, "LINESTRING");
        add(GeometryType.POLYGON, false, "POLYGON");
        add(GeometryType.POLYGON, true, "POLYGONM");
        add(GeometryType.MULTI_POINT, true, "MULTIPOINTM");
        add(GeometryType.MULTI_POINT, false, "MULTIPOINT");
        add(GeometryType.MULTI_LINE_STRING, false, "MULTILINESTRING");
        add(GeometryType.MULTI_LINE_STRING, true, "MULTILINESTRINGM");
        add(GeometryType.MULTI_POLYGON, false, "MULTIPOLYGON");
        add(GeometryType.MULTI_POLYGON, true, "MULTIPOLYGONM");
        add(GeometryType.GEOMETRY_COLLECTION, false, "GEOMETRYCOLLECTION");
        add(GeometryType.GEOMETRY_COLLECTION, true, "GEOMETRYCOLLECTIONM");
    }

    private static void add(GeometryType type, boolean isMeasured, String word) {
        GEOMETRY_REGEXES.put(new Type(type, isMeasured), Pattern.compile(word, Pattern.CASE_INSENSITIVE));
    }

    @Override
    public WktToken match(CharSequence wkt, int currentPos, int endPos) {
        if (matchesEmpty(wkt, currentPos, endPos)) return WktToken.empty();
        for (Type type : GEOMETRY_REGEXES.keySet()) {
            if (matches(type, wkt, currentPos, endPos)) return toToken(type);
        }
        throw new WktParseException(String.format("Invalid word %s in wkt tekst %s", wkt.subSequence(currentPos, endPos), wkt));
    }

    @Override
    public String wordFor(Geometry geometry) {
        for (Type type : GEOMETRY_REGEXES.keySet()) {
            if (sameGeometryType(geometry, type) &&
                    sameDimensions(geometry, type)) {
                return GEOMETRY_REGEXES.get(type).pattern();
            }
        }
        throw new UnsupportedConversionException("Conversion of geometries of type " + geometry.getGeometryType() + " to Wkt is not supported.");
    }


    // type <GEOMETRYTYPE>M should match iff geometry is not 3D and measured
    private boolean sameDimensions(Geometry geometry, Type type) {
        if (type.isMeasured) {
            return geometry.isMeasured() && !geometry.is3D();
        }
        return !geometry.isMeasured() || geometry.is3D();
    }

    private boolean sameGeometryType(Geometry geometry, Type type) {
        return type.geometryType == geometry.getGeometryType();
    }

    @Override
    public String wordFor(DimensionalFlag flag) {
        throw new UnsupportedOperationException("Postgis EWKT has no symbol corresponding to a dimensional flag.");
    }

    private boolean matchesEmpty(CharSequence wkt, int currentPos, int endPos) {
        Matcher m = EMPTY_RE.matcher(wkt);
        m.region(currentPos, endPos);
        return m.matches();
    }

    private boolean matches(Type type, CharSequence wkt, int currentPos, int endPos) {
        Matcher m = GEOMETRY_REGEXES.get(type).matcher(wkt);
        m.region(currentPos, endPos);
        return m.matches();
    }

    private WktToken toToken(Type type) {
        return WktToken.geometryTag(type.geometryType, type.isMeasured);
    }
}

class Type {
    GeometryType geometryType;
    boolean isMeasured;

    Type(GeometryType gt, boolean isM) {
        this.geometryType = gt;
        this.isMeasured = isM;
    }
}