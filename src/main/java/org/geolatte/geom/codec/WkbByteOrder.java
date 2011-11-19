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

import java.nio.ByteOrder;

/**
 * Indicates the byte-order of a WKB representation.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public enum WkbByteOrder {

    XDR(ByteOrder.BIG_ENDIAN, (byte) 0),
    NDR(ByteOrder.LITTLE_ENDIAN, (byte) 1);

    private final ByteOrder order;
    private final byte orderByte;

    private WkbByteOrder(ByteOrder order, byte orderByte) {
        this.order = order;
        this.orderByte = orderByte;
    }


    public static WkbByteOrder valueOf(byte orderByte) {
        for (WkbByteOrder wbo : values()) {
            if (orderByte == wbo.byteValue()) return wbo;
        }
        throw new IllegalArgumentException("Order byte must be 0 or 1");
    }

    ByteOrder getByteOrder() {
        return this.order;
    }

    byte byteValue() {
        return orderByte;
    }

    static WkbByteOrder valueOf(ByteOrder byteOrder) {
        for (WkbByteOrder wkbOrder : values()) {
            if (wkbOrder.getByteOrder() == byteOrder) return wkbOrder;
        }
        throw new IllegalArgumentException();
    }

}
