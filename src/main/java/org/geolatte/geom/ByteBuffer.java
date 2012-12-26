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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

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
public class ByteBuffer {

    /**
     * byte size for unsigned int
     */
    public static final int UINT_SIZE = 4;

    /**
     * byte size for doubles
     */
    public static final int DOUBLE_SIZE = 8;

    /**
     * Max. permissible value for an unsigned int.
     */
    public static final long UINT_MAX_VALUE = 4294967295L;

    private final java.nio.ByteBuffer buffer;

    private ByteBuffer(java.nio.ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Creates a <code>ByteBuffer</code> from a hexadecimal string.
     *
     * <p>Every two chars in the string are interpreted as the hexadecimal representation of a byte.
     * If the string length is odd, the last character will be ignored.</p>
     *
     * @param hexString the bytes represented in hexadecimal form
     * @return A ByteBuffer based on the hexadecimal string
     */
    public static ByteBuffer from(String hexString) {
        if (hexString == null) throw new IllegalArgumentException("Cannot create ByteBuffer from null input String.");
        int size = hexString.length() / 2; // this will drop the last char, if hexString is not even.
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(size);
        for (int i = 0; i < size * 2; i += 2) {
            byte b = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
            buffer.put(b);
        }
        buffer.rewind();
        return new ByteBuffer(buffer);
    }

    /**
     * Returns this instance as a hexadecimal string.
     *
     * @return A string representation of this ByteBuffer in hexadecimal form
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int savedPosition = buffer.position();
        rewind();
        for (int i = 0; i < limit(); i++) {
            int hexValue = readByte();
            appendHexByteRepresentation(builder, hexValue);
        }
        buffer.position(savedPosition);
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
     * Wraps a byte array into a <code>ByteBuffer</code>.
     *
     * The new buffer will be backed by the given byte array; that is, modifications to the buffer will cause the array
     * to be modified and vice versa. The new buffer's capacity and limit will be bytes.length.
     *
     * @param bytes The array that will back this buffer
     * @return The new byte buffer.
     */
    public static ByteBuffer from(byte[] bytes) {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
        return new ByteBuffer(buffer);
    }

    /**
     * Allocates a new <code>ByteBuffer</code> of the specified capacity.
     *
     * The new buffer's position will be zero, its limit will be its capacity and each of its elements will be
     * initialized to zero.
     *
     * @param capacity The new buffer's capacity, in bytes
     * @return a new <code>ByteBuffer</code> instance of the specified capacity
     */
    public static ByteBuffer allocate(int capacity) {
        return new ByteBuffer(java.nio.ByteBuffer.allocate(capacity));
    }

    /**
     * Relative get method. Reads the byte at this buffer's current position, and then increments the position.
     *
     * @return The byte at the buffer's current position
     * @throws BufferAccessException If the buffer's current position is not smaller than its limit.
     */
    public byte get() {
        try {
            return buffer.get();
        } catch (BufferOverflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Writes the given byte into this buffer at the current position, and then increments the position.
     *
     * @param value The byte to be written
     * @throws BufferAccessException If this buffer's current position is not smaller than its limit.
     */
    public void put(byte value) {
        try {
            buffer.put(value);
        } catch (BufferOverflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Returns this buffer's capacity.
     *
     * @return The capacity of this buffer
     */
    public int capacity() {
        return buffer.capacity();
    }

    /**
     * Returns this buffer's limit.
     *
     * @return The limit of this buffer
     */
    public int limit() {
        return buffer.limit();
    }

    /**
     * Rewinds the buffer.
     *
     * <p>After rewind, the next get() or put() will take place on the first element of this instance. </p>
     */
    public void rewind() {
        buffer.rewind();
    }

    /**
     * Reports if this buffer is empty (holds no bytes).
     *
     * @return True if limit is 0, otherwise false
     */
    public boolean isEmpty() {
        return buffer.limit() == 0;
    }

    /**
     * Sets the byte order for this instance.
     *
     * @param wbo The new byte order, either {@link ByteOrder#XDR XDR} or {@link ByteOrder#NDR NDR}
     */
    public void setByteOrder(ByteOrder wbo) {
        buffer.order(wbo.getByteOrder());
    }

    /**
     * Reads the next 4 bytes as an int from this instance at the current position, taking into account the byte-order,
     * and then increments the position by four.
     *
     * @return The int value at the buffer's current position
     * @throws BufferAccessException If there are fewer than four bytes remaining in this buffer.
     */
    public int getInt() {
        try {
            return buffer.getInt();
        } catch (BufferUnderflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Writes the specified int-value as 4 bytes to this instance at the current position, respecting the byte-order,
     * and then increments the position by four.
     *
     * @param value The int value to be written
     * @throws BufferAccessException If there are fewer than four bytes remaining in this buffer.
     */
    public void putInt(int value) {
        try {
            buffer.putInt(value);
        } catch (BufferOverflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Reads the next 8 bytes as a long from this instance at the current position, taking into account the byte-order,
     * and then increments the position by eight.
     *
     * @return The long value at the buffer's current position
     * @throws BufferAccessException If there are fewer than eight bytes remaining in this buffer.
     */
    public long getLong() {
        try {
            return buffer.getLong();
        } catch (BufferUnderflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Writes the specified long value as 8 bytes to this instance at the current position, respecting the byte-order,
     * and then increments the position by eight.
     *
     * @param value The long value to be written
     * @throws BufferAccessException If there are fewer than eight bytes remaining in this buffer.
     */
    public void putLong(long value) {
        try {
            buffer.putLong(value);
        } catch (BufferOverflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Reads the next 4 bytes as a float from this instance at the current position, taking into account the byte-order,
     * and then increments the position by four.
     *
     * @return The float value at the buffer's current position
     * @throws BufferAccessException If there are fewer than four bytes remaining in this buffer.
     */
    public float getFloat() {
        try {
            return buffer.getFloat();
        } catch (BufferUnderflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Writes the specified float-value as 4 bytes to this instance at the current position, respecting the byte-order,
     * and then increments the position by four.
     *
     * @param value The float value to be written
     * @throws BufferAccessException If there are fewer than four bytes remaining in this buffer.
     */
    public void putFloat(float value) {
        try {
            buffer.putFloat(value);
        } catch (BufferOverflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Reads the next 8 bytes as a double from this instance at the current position, taking into account the byte-order,
     * and then increments the position by eight.
     *
     * @return The double value at the buffer's current position
     * @throws BufferAccessException If there are fewer than eight bytes remaining in this buffer.
     */
    public double getDouble() {
        try {
            return buffer.getDouble();
        } catch (BufferUnderflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Writes the specified double-value as 8 bytes to this instance at the current position, respecting the byte-order,
     * and then increments the position by eight.
     *
     * @param value The double value to be written
     * @throws BufferAccessException If there are fewer than eight bytes remaining in this buffer.
     */
    public void putDouble(Double value) {
        try {
            buffer.putDouble(value);
        } catch (BufferOverflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Reads the next 4 bytes as an unsigned integer from this instance at the current position,
     * taking into account the byte-order, and then increments the position by four.
     *
     * @return The value of the 4-byte unsigned integer at the current position as a long
     * @throws BufferAccessException If there are fewer than four bytes remaining in this buffer
     */
    public long getUInt() {
        try {
            int signedInt = buffer.getInt();
            return signedInt & 0xffffffffL; //cast the signed int to an unsigned value in the bottom 32 bits of a long
        } catch (BufferUnderflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     *  Interprets the specified long-value as and unsigned integer, and appends it as 4 bytes
     *  to this instance at the current position, respecting the byte-order.
     *
     * @param value The unsigned integer value to be written
     * @throws RuntimeException      If the specified value is larger than the largest unsigned integer (4294967295L)
     * @throws BufferAccessException If there are fewer than eight bytes remaining in this buffer.
     */
    public void putUInt(long value) {
        if (value > UINT_MAX_VALUE) throw new RuntimeException("Value received doesn't fit in unsigned integer");
        try {
            buffer.putInt((int) value);
        } catch (BufferOverflowException e) {
            throw new BufferAccessException(e.getMessage());
        }
    }

    /**
     * Gets the byte order of this instance.
     *
     * @return This buffer's byte order
     */
    public ByteOrder getByteOrder() {
        java.nio.ByteOrder order = buffer.order();
        return ByteOrder.valueOf(order);
    }

    /**
     * Returns the byte array that backs this buffer.
     *
     * @return The array that backs this buffer
     */
    public byte[] toByteArray(){
        return buffer.array();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteBuffer that = (ByteBuffer) o;

        if (buffer != null ? !buffer.equals(that.buffer) : that.buffer != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return buffer != null ? buffer.hashCode() : 0;
    }

    /**
     * Used for testing purposes.
     *
     * @param other another <code>ByteBuffer</code>
     * @return true if both buffers contain the same bytes, false otherwise
     */
    public boolean hasSameContent(ByteBuffer other) {
        if (other == null) return false;
        if (this.limit() != other.limit()) return false;
        int thisSavedPosition = this.buffer.position();
        int otherSavedPosition = other.buffer.position();
        this.rewind();
        other.rewind();
        for (int i = 0; i < this.limit(); i++) {
            if (this.get() != other.get()) return false;
        }
        this.buffer.position(thisSavedPosition);
        other.buffer.position(otherSavedPosition);
        return true;
    }

}
