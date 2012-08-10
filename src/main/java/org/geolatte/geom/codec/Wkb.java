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

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.ByteOrder;
import org.geolatte.geom.Geometry;

/**
 * A Utility class for encoding/decoding WKB geometry representations for the PostGIS EWKB dialect (versions 1.0 to 1.5).
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public class Wkb {

    /**
     * Encodes a <code>Geometry</code> into a WKB representation using the NDR (little-endian)  byte-order.
     *
     * @param geometry The <code>Geometry</code> to be encoded as WKB.
     * @return A buffer of bytes that contains the WKB-encoded <code>Geometry</code>.
     */
    public static ByteBuffer toWkb(Geometry geometry) {
        return toWkb(geometry, ByteOrder.NDR);
    }

    /**
     * Encodes a <code>Geometry</code> into a WKB representation using the specified byte-order.
     *
     * @param geometry The <code>Geometry</code> to be encoded as WKB.
     * @param byteOrder The WKB byte order, either {@link ByteOrder#XDR XDR} or {@link ByteOrder#NDR NDR}
     * @return A buffer of bytes that contains the WKB-encoded <code>Geometry</code>.
     */
    public static ByteBuffer toWkb(Geometry geometry, ByteOrder byteOrder) {
        PostgisWkbEncoder encoder = new PostgisWkbEncoder();
        return encoder.encode(geometry, byteOrder);
    }

    /**
     * Decodes a Postgis WKB representation in a <code>ByteBuffer</code> to a <code>Geometry</code>.
     *
     * @param byteBuffer A buffer of bytes that contains a WKB-encoded <code>Geometry</code>.
     * @return The <code>Geometry</code> that is encoded in the WKB.
     */
    public static Geometry fromWkb(ByteBuffer byteBuffer) {
        PostgisWkbDecoder decoder = new PostgisWkbDecoder();
        return decoder.decode(byteBuffer);
    }

}
