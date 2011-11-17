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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A byte buffer class.
 *
 * <p>This class is modeled on the <code>java.nio.Buffer</code> interface. Specifically, the properties capacity and limit are
 * defined as for <code>Buffer</code>.</p>
 *
 * <ul>
 *     <li>A buffer's capacity is the number of elements it contains. The capacity of a buffer is never negative and never changes.</li>
 *     <li>A buffer's limit is the index of the first element that should not be read or written. A buffer's limit is never negative and is never greater than its capacity.</li>
 * </ul>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public class Bytes {

    /**
     * byte size for unsigned int
     */
    public static final int UINT_SIZE = 4;

    /**
     * byte size for doubles
     */
    public static final int DOUBLE_SIZE = 8;

    static final long UINT_MAX_VALUE = 4294967295L;
    final private ByteBuffer buffer;

    private Bytes(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Creates a <code>Bytes</code> instance from a hexadecimal string.
     *
     * <p>Every two chars in the string are interpreted as the hexadecimal representation of a byte.
     * If the string length is odd, the last character will be ignored.</p>
     * @param text the bytes represented in hexadecimal
     * @return
     */
    public static Bytes from(String text) {
        if (text == null) throw new IllegalArgumentException("Null not allowed.");
        int size = text.length() / 2; // this will drop the last char, if text is not even.
        byte[] byteArray = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        for (int i = 0; i < text.length() - 1; i += 2) {
            byte b = (byte) Integer.parseInt(text.substring(i, i + 2), 16);
            buffer.put(b);
        }
        buffer.rewind();
        return new Bytes(buffer);
    }

    /**
     * Returns this instance as a hexadecimal string.
     *
     * @return
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        rewind();
        for (int i = 0; i < limit(); i++) {
            int hexValue = readByte();
            appendHexByteRepresentation(builder, hexValue);
        }
        return builder.toString();
    }

    private void appendHexByteRepresentation(StringBuilder builder, int hexValue) {
        String byteStr = Integer.toHexString(hexValue).toUpperCase();
        if (byteStr.length() == 1) {
            builder.append("0").append(byteStr);
        } else {
            builder.append(byteStr);
        }
    }

    private int readByte() {
        int hexValue = get();
        hexValue = (hexValue << 24) >>> 24;
        return hexValue;
    }

    /**
     * Creates a <code>Bytes</code> instance from byte array.
     *
     * @param bytes
     * @return
     */
    public static Bytes from(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new Bytes(buffer);
    }

    /**
     * Creates an empty <code>Bytes</code> instance of the specified capacity
     * @param capacity capacity of the returned instance
     * @return a new <code>Bytes</code> instance of the specified capacity
     */
    public static Bytes allocate(int capacity) {
        return new Bytes(ByteBuffer.allocate(capacity));
    }

    /**
     * Returns the next byte.
     *
     * @return
     */
    public byte get() {
        try {
            return this.buffer.get();
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Puts the specified byte in this <code>Bytes</code> instance
     *
     * @param value
     */
    public void put(byte value) {
        try {
            this.buffer.put(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the capacity.
     *
     * @return
     */
    public int capacity() {
        return this.buffer.capacity();
    }

    /**
     * Returns the limit of the <code>Bytes</code> instance.
     * @return
     */
    protected int limit() {
        return this.buffer.limit();
    }

    /**
     * Rewinds this instance.
     *
     * <p>After rewind, the next get() or put() will take place on the first element of this instance. </p>
     */
    public void rewind() {
        this.buffer.rewind();
    }

    /**
     * Reports if this instance is empty (holds no bytes).
     *
     * @return
     */
    public boolean isEmpty() {
        return buffer.limit() == 0;
    }

    /**
     * Sets the byte order for this instance.
     *
     * @param wbo
     */
    public void setWKBByteOrder(WkbByteOrder wbo) {
        this.buffer.order(wbo.getByteOrder());
    }

    /**
     * Returns the next 4 bytes as an int from this instance, taking into account the byte-order..
     *
     * @return
     */
    public int getInt() {
        try {
            return this.buffer.getInt();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends the specified int-value as 4 bytes to this instance, respecting the byte-order.
     *
     * @param value
     */
    public void putInt(int value) {
        try {
            this.buffer.putInt(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the next 8 bytes as a long from this instance, taking into account the byte-order.
     *
     * @return
     */
    public long getLong() {
        try {
            return this.buffer.getLong();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends the specified long value as 8 bytes to this instance, respecting the byte-order.
     *
     * @param value
     */
    public void putLong(long value) {
        try {
            this.buffer.putLong(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the next 4 bytes as a float, taking into account the byte-order.
     * @return
     */
    public float getFloat() {
        try {
            return this.buffer.getFloat();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends the specified float-value as 4 bytes to this instance, respecting the byte-order.
     * @param value
     */
    public void putFloat(float value) {
        try {
            this.buffer.putFloat(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

     /**
     * Returns the next 8 bytes as a double, taking into account the byte-order.
     * @return
     */
    public double getDouble() {
        try {
            return this.buffer.getDouble();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends the specified double-value as 8 bytes to this instance, respecting the byte-order.
     * @param value
     */
    public void putDouble(Double value) {
        try {
            this.buffer.putDouble(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the next 4 bytes as an unsigned integer, taking into account the byte-order.
     * @return
     */
    public long getUInt() {
        try {
            long value = this.buffer.getInt();   // read integer in a long
            return (value << 32) >>> 32; //shift 32-bytes left (dropping all             
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Interprets the specified long-value as and unsigned integer, and appends it as 4 bytes to this instance,
     *  respecting the byte-order.
     *
     * @param value
     * @throws RuntimeException if the specified value is larger than the largest unsigned integer (4294967295L).
     */
    public void putUInt(long value) {
        if (value > UINT_MAX_VALUE) throw new RuntimeException("Value received doesn't fit in unsigned integer");
        try {
            this.buffer.putInt((int) value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the byte order of this instance.
     *
     * @return
     */
    public WkbByteOrder getWKBByteOrder() {
        ByteOrder order = this.buffer.order();
        return WkbByteOrder.valueOf(order);
    }

    /**
     * Returns this instance as a byte array.
     * @return
     */
    public byte[] toByteArray(){
        return this.buffer.array();
    }



    //This is used for testing purposes
    protected boolean hasSameContent(Bytes other) {
        if (other == null) return false;
        if (other.isEmpty() != this.isEmpty()) return false;
        if (this.limit() != other.limit()) return false;
        this.rewind();
        other.rewind();
        for (int i = 0; i < this.limit(); i++) {
            if (this.get() != other.get()) return false;
        }
        this.rewind();
        other.rewind();
        return true;
    }

}
