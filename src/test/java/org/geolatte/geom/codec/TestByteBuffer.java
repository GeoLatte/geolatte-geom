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
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public class TestByteBuffer {

    @Test
    public void testBytesFromString() {

        String byteTxt = "0120E6FF";
        ByteBuffer bStream = ByteBuffer.from(byteTxt);

        assertEquals(4, bStream.limit());

        assertEquals(1, bStream.get());
        assertEquals(32, bStream.get());
        assertEquals((byte) 230, bStream.get());
        assertEquals((byte) 255, bStream.get());

    }

    @Test
    public void test_bytes_from_odd_length_string() {
        String byteTxt = "0120E6FFE";
        ByteBuffer bStream = ByteBuffer.from(byteTxt);
        assertEquals(4, bStream.limit());

    }

    @Test
    public void test_throws_NumberFormatException_if_string_not_hexadecimal() {
        try {
            ByteBuffer.from("02FFKK");
            fail();
        } catch (NumberFormatException e) {
            //OK
        }
    }

    @Test
    public void test_return_empty_Bytes_on_empty_string() {
        ByteBuffer byteBuffer = ByteBuffer.from("");
        assertTrue(byteBuffer.isEmpty());
        assertEquals(0, byteBuffer.limit());
    }

    @Test
    public void test_throw_IllegalArgumentException_on_null_string() {
        try {
            ByteBuffer.from((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }


    @Test
    public void test_toString() {
        String hxStr = "037FB4C7";
        ByteBuffer byteBuffer = ByteBuffer.from(hxStr);
        assertEquals(hxStr, byteBuffer.toString());

    }

    @Test
    public void test_big_endian_int() {
        String hxStr = "037FB4C7";
        int expectedValue = 58700999;

        ByteBuffer byteBuffer = ByteBuffer.from(hxStr);
        byteBuffer.setByteOrder(ByteOrder.XDR);
        assertEquals(expectedValue, byteBuffer.getInt());
    }

    @Test
    public void test_string_or_bytes_gives_same_result() {
        String hxStr = "037FB4C7";
        byte[] byteArray = new byte[]{0x03, 0x7F, (byte) 0xB4, (byte) 0xC7};
        int expectedValue = 58700999;

        ByteBuffer byteBuffer = ByteBuffer.from(hxStr);
        byteBuffer.setByteOrder(ByteOrder.XDR);
        assertEquals(expectedValue, byteBuffer.getInt());

        ByteBuffer byteBufferFromArray = ByteBuffer.from(byteArray);
        byteBufferFromArray.setByteOrder(ByteOrder.XDR);
        assertEquals(expectedValue, byteBufferFromArray.getInt());
    }

    @Test
    public void test_little_endian_int() {
        String hxStr = "C7B47F03";
        int expectedValue = 58700999;

        ByteBuffer byteBuffer = ByteBuffer.from(hxStr);
        byteBuffer.setByteOrder(ByteOrder.NDR);
        assertEquals(expectedValue, byteBuffer.getInt());
    }

    @Test
    public void test_insufficient_bytes_for_retrieving_long_throws_exception() {
        String hxStr = "C7B47F03";
        ByteBuffer byteBuffer = ByteBuffer.from(hxStr);
        try {
            byteBuffer.getLong();
        } catch (RuntimeException e) {
            //OK
        }

    }

    @Test
    public void test_get_unsigned_as_long() {
        String hxStr = "FFFFFFFF";
        ByteBuffer byteBuffer = ByteBuffer.from(hxStr);
        long expected = 4294967295L;
        assertEquals(expected, byteBuffer.getUInt());

        byteBuffer = ByteBuffer.from("80000000");
        assertEquals(2147483648L, byteBuffer.getUInt());

        byteBuffer = ByteBuffer.from("00000001");
        assertEquals(1L, byteBuffer.getUInt());

        byteBuffer = ByteBuffer.from("7FFFFFFF");
        assertEquals(2147483647L, byteBuffer.getUInt());

    }

    @Test
    public void test_retrieve_byteorder() {
        String hxStr = "FFFFFFFF";
        ByteBuffer byteBuffer = ByteBuffer.from(hxStr);
        byteBuffer.setByteOrder(ByteOrder.XDR);
        assertEquals(ByteOrder.XDR, byteBuffer.getByteOrder());
        byteBuffer.setByteOrder(ByteOrder.NDR);
        assertEquals(ByteOrder.NDR, byteBuffer.getByteOrder());
    }

    @Test
    public void test_create_bytes_having_specified_size() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        assertEquals(100, byteBuffer.capacity());
    }

    @Test
    public void test_put_xdr_double() {
        double expected = 1234.56789;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.setByteOrder(ByteOrder.XDR);
        byteBuffer.putDouble(expected);
        byteBuffer.rewind();
        double d = byteBuffer.getDouble();
        assertEquals(expected, d);
    }

    @Test
    public void test_put_ndr_double() {
        double expected = 1234.56789;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.setByteOrder(ByteOrder.NDR);
        byteBuffer.putDouble(expected);
        byteBuffer.rewind();
        double d = byteBuffer.getDouble();
        assertEquals(expected, d);
    }

    @Test
    public void test_put_byte() {
        byte expected = (byte) 0xFF;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        byteBuffer.put(expected);
        byteBuffer.rewind();
        assertEquals(expected, byteBuffer.get());
    }

    @Test
    public void test_put_int() {
        int expected = 0xFF;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(expected);
        byteBuffer.rewind();
        assertEquals(expected, byteBuffer.getInt());
    }

    @Test
    public void test_put_long() {
        long expected = Long.MAX_VALUE;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putLong(expected);
        byteBuffer.rewind();
        assertEquals(expected, byteBuffer.getLong());
    }


    @Test
    public void test_put_float() {
        float expected = 1234.567f;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putFloat(expected);
        byteBuffer.rewind();
        assertEquals(expected, byteBuffer.getFloat());
    }

    @Test
    public void test_put_uint() {
        long expected = 4294967295L;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putUInt(expected);
        byteBuffer.rewind();
        assertEquals(expected, byteBuffer.getUInt());
    }

    @Test
    public void test_put_too_large_value_in_uint_throws_exception() {
        long value = ByteBuffer.UINT_MAX_VALUE;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putUInt(value);
        byteBuffer.rewind();
        assertEquals(value, byteBuffer.getUInt());
        value++;
        ByteBuffer byteBuffer2 = ByteBuffer.allocate(4);
        try {
            byteBuffer2.putUInt(value);
            fail();
        } catch (RuntimeException e) {
            //OK
        }
    }


}

