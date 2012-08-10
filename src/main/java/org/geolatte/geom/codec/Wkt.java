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

/**
 *  A utility class for encoding/decoding WKT geometry representations.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Wkt {

    /**
     * Decodes the specified WKT String to a <code>Geometry</code>.
     *
     * @param wkt the WKT string to decode
     * @return The decoded Geometry
     */
    public static Geometry fromWkt(String wkt) {
        PostgisWktDecoder decoder = new PostgisWktDecoder();
        return decoder.decode(wkt);
    }

    /**
     * Encodes a <code>Geometry</code> to a WKT representation.
     *
     * @param geometry the <code>Geometry</code> to encode
     * @return the WKT representation of the given geometry
     */
    public static String toWkt(Geometry geometry) {
        PostgisWktEncoder encoder = new PostgisWktEncoder();
        return encoder.encode(geometry);
    }
}
