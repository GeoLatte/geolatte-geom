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
 * A Utility class for encoding/decoding WKB geometry representations.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public class Wkb {

    /**
     * Encodes a <code>Geometry</code> to a WKB representation using the NDR (little-endian)  byte-order.
     *
     * @param geometry
     * @return
     */
    public static ByteBuffer toWkb(Geometry geometry) {
        return toWkb(geometry, ByteOrder.NDR);
    }

    /**
     * Encodes a <code>Geometry</code> to a WKB representation using the specified byte-order.
     *
     * @param geometry
     * @param byteOrder
     * @return
     */
    public static ByteBuffer toWkb(Geometry geometry, ByteOrder byteOrder) {
        PostgisWkbEncoder encoder = new PostgisWkbEncoder();
        return encoder.encode(geometry, byteOrder);
    }

    /**
     * Decodes a <code>ByteBuffer</code> buffer to a Geometry.
     *
     * @param byteBuffer
     * @return
     */
    public static Geometry fromWkb(ByteBuffer byteBuffer) {
        PostgisWkbDecoder decoder = new PostgisWkbDecoder();
        return decoder.decode(byteBuffer);
    }

}
