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

import org.geolatte.geom.GeometryType;

import java.util.Locale;

/**
 * The <code>WktKeywordToken</code> for the type of geometry.
 *
 * @author Karel Maesen, Geovise BVBA
 */
class WktGeometryToken extends WktKeywordToken {

    static WktGeometryToken forType(GeometryType type) {
        return new WktGeometryToken(type.name().toUpperCase(Locale.US), type);
    }

    static WktGeometryToken forType(GeometryType type, String wktTag) {
        return new WktGeometryToken(wktTag, type);
    }

    private final GeometryType geometryType;
    @Deprecated
    private final boolean isMeasured;


    @Deprecated
    WktGeometryToken(String word, GeometryType tag, boolean measured) {
        super(word);
        this.geometryType = tag;
        this.isMeasured = measured;
    }

    WktGeometryToken(String word, GeometryType tag) {
        super(word);
        this.geometryType = tag;
        this.isMeasured = false;
    }

    GeometryType getType() {
        return geometryType;
    }

    @Deprecated
    boolean isMeasured() {
        return isMeasured;
    }

    public String toString(){
        return getType() + (isMeasured() ? " M" : "");
    }
}
