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
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public class Bytes {

    public static final int UINT_SIZE = 4;
    public static final int DOUBLE_SIZE = 8;

    static final long UINT_MAX_VALUE = 4294967295L;
    final private ByteBuffer buffer;

    private Bytes(ByteBuffer buffer) {
        this.buffer = buffer;
    }

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

    public String toString() {
        StringBuilder builder = new StringBuilder();
        rewind();
        for (int i = 0; i < limit(); i++) {
            int hexValue = readByte();
            appendByte(builder, hexValue);
        }
        return builder.toString();
    }

    private void appendByte(StringBuilder builder, int hexValue) {
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

    public static Bytes from(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new Bytes(buffer);
    }

    public static Bytes allocate(int capacity) {
        return new Bytes(ByteBuffer.allocate(capacity));
    }

    public byte get() {
        try {
            return this.buffer.get();
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(byte value) {
        try {
            this.buffer.put(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    public int capacity() {
        return this.buffer.capacity();
    }

    public int limit() {
        return this.buffer.limit();
    }

    public Bytes limit(int newLimit) {
        buffer.limit(newLimit);
        return this;
    }

    public void rewind() {
        this.buffer.rewind();
    }

    public boolean isEmpty() {
        return buffer.limit() == 0;
    }

    public void setWKBByteOrder(WKBByteOrder wbo) {
        this.buffer.order(wbo.getByteOrder());
    }

    public int getInt() {
        try {
            return this.buffer.getInt();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    public void putInt(int value) {
        try {
            this.buffer.putInt(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }


    public long getLong() {
        try {
            return this.buffer.getLong();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    public void putLong(long value) {
        try {
            this.buffer.putLong(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    public float getFloat() {
        try {
            return this.buffer.getFloat();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    public void putFloat(float value) {
        try {
            this.buffer.putFloat(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    public double getDouble() {
        try {
            return this.buffer.getDouble();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    public void putDouble(Double value) {
        try {
            this.buffer.putDouble(value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    public long getUInt() {
        try {
            long value = this.buffer.getInt();   // read integer in a long
            return (value << 32) >>> 32; //shift 32-bytes left (dropping all             
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(e);
        }
    }

    public void putUInt(long value) {
        if (value > UINT_MAX_VALUE) throw new RuntimeException("Value received doesn't fit in unsigned integer");
        try {
            this.buffer.putInt((int) value);
        } catch (BufferOverflowException e) {
            throw new RuntimeException(e);
        }
    }

    public WKBByteOrder getWKBByteOrder() {
        ByteOrder order = this.buffer.order();
        return WKBByteOrder.valueOf(order);
    }

    public byte[] toByteArray(){
        return this.buffer.array();
    }



    //This is used for geom purposes
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
