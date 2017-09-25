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

package org.geolatte.geom;

/**
 * Indicates the byte-order for a <code>ByteBuffer</code>.
 *
 * @author Karel Maesen, Geovise BVBA
 *
 */
public enum ByteOrder {
    /**
     * XDR or big endian byte order
     */
    XDR(java.nio.ByteOrder.BIG_ENDIAN, (byte) 0),

    /**
     * NDR or little endian byte order.
     */
    NDR(java.nio.ByteOrder.LITTLE_ENDIAN, (byte) 1);

    private final java.nio.ByteOrder order;
    private final byte orderByte;

    private ByteOrder(java.nio.ByteOrder order, byte orderByte) {
        this.order = order;
        this.orderByte = orderByte;
    }

    /**
     * Returns the <code>ByteOrder corresponding to the specified orderByte. </code>
     *
     * @param orderByte (0 or 1)
     * @return corresponding <code>ByteOrder</code>, resp. <code>XDR</code> or <code>NDR</code>.
     */
    public static ByteOrder valueOf(byte orderByte) {
        for (ByteOrder wbo : values()) {
            if (orderByte == wbo.byteValue()) return wbo;
        }
        throw new IllegalArgumentException("Order byte must be 0 or 1");
    }

    /**
     * Returns the corresponding {@link java.nio.ByteOrder java.nio.ByteOrder}
     * @return the corresponding {@link java.nio.ByteOrder java.nio.ByteOrder}
     */
    public java.nio.ByteOrder getByteOrder() {
        return this.order;
    }

    /**
     * Returns the byte order as a byte: 0 for XDR (big endian), 1 for NDR (little endian).
     *
     * @return byte 0 or 1
     */
    public byte byteValue() {
        return orderByte;
    }

    /**
      * Returns the <code>ByteOrder corresponding to the specified {@link java.nio.ByteOrder}.
      * @param byteOrder the byteOrder
      * @return corresponding <code>ByteOrder</code>, resp. <code>XDR</code> or <code>NDR</code>.
      */
    public static ByteOrder valueOf(java.nio.ByteOrder byteOrder) {
        for (ByteOrder order : values()) {
            if (order.getByteOrder() == byteOrder) return order;
        }
        throw new IllegalArgumentException();
    }

}
