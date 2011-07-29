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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 29, 2010
 */
public class TestBytes {

    @Test
    public void testBytesFromString() {

        String byteTxt = "0120E6FF";
        Bytes bStream = Bytes.from(byteTxt);

        assertEquals(4, bStream.limit());

        assertEquals(1, bStream.get());
        assertEquals(32, bStream.get());
        assertEquals((byte) 230, bStream.get());
        assertEquals((byte) 255, bStream.get());

    }

    @Test
    public void test_bytes_from_odd_length_string() {
        String byteTxt = "0120E6FFE";
        Bytes bStream = Bytes.from(byteTxt);
        assertEquals(4, bStream.limit());

    }

    @Test
    public void test_throws_NumberFormatException_if_string_not_hexadecimal() {
        try {
            Bytes.from("02FFKK");
            fail();
        } catch (NumberFormatException e) {
            //OK
        }
    }

    @Test
    public void test_return_empty_Bytes_on_empty_string() {
        Bytes bytes = Bytes.from("");
        assertTrue(bytes.isEmpty());
        assertEquals(0, bytes.limit());
    }

    @Test
    public void test_throw_IllegalArgumentException_on_null_string() {
        try {
            Bytes bytes = Bytes.from((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            //OK
        }
    }


    @Test
    public void test_toString() {
        String hxStr = "037FB4C7";
        Bytes bytes = Bytes.from(hxStr);
        assertEquals(hxStr, bytes.toString());

    }

    @Test
    public void test_big_endian_int() {
        String hxStr = "037FB4C7";
        int expectedValue = 58700999;

        Bytes bytes = Bytes.from(hxStr);
        bytes.setWKBByteOrder(WKBByteOrder.XDR);
        assertEquals(expectedValue, bytes.getInt());
    }

    @Test
    public void test_string_or_bytes_gives_same_result() {
        String hxStr = "037FB4C7";
        byte[] byteArray = new byte[]{0x03, 0x7F, (byte) 0xB4, (byte) 0xC7};
        int expectedValue = 58700999;

        Bytes bytes = Bytes.from(hxStr);
        bytes.setWKBByteOrder(WKBByteOrder.XDR);
        assertEquals(expectedValue, bytes.getInt());

        Bytes bytesFromArray = Bytes.from(byteArray);
        bytesFromArray.setWKBByteOrder(WKBByteOrder.XDR);
        assertEquals(expectedValue, bytesFromArray.getInt());
    }

    @Test
    public void test_little_endian_int() {
        String hxStr = "C7B47F03";
        int expectedValue = 58700999;

        Bytes bytes = Bytes.from(hxStr);
        bytes.setWKBByteOrder(WKBByteOrder.NDR);
        assertEquals(expectedValue, bytes.getInt());
    }

    @Test
    public void test_insufficient_bytes_for_retrieving_long_throws_exception() {
        String hxStr = "C7B47F03";
        Bytes bytes = Bytes.from(hxStr);
        try {
            Long lng = bytes.getLong();
        } catch (RuntimeException e) {
            //OK
        }

    }

    @Test
    public void test_get_unsigned_as_long() {
        String hxStr = "FFFFFFFF";
        Bytes bytes = Bytes.from(hxStr);
        long expected = 4294967295L;
        assertEquals(expected, bytes.getUInt());

        bytes = Bytes.from("80000000");
        assertEquals(2147483648L, bytes.getUInt());

        bytes = Bytes.from("00000001");
        assertEquals(1L, bytes.getUInt());

        bytes = Bytes.from("7FFFFFFF");
        assertEquals(2147483647L, bytes.getUInt());

    }

    @Test
    public void test_retrieve_byteorder() {
        String hxStr = "FFFFFFFF";
        Bytes bytes = Bytes.from(hxStr);
        bytes.setWKBByteOrder(WKBByteOrder.XDR);
        assertEquals(WKBByteOrder.XDR, bytes.getWKBByteOrder());
        bytes.setWKBByteOrder(WKBByteOrder.NDR);
        assertEquals(WKBByteOrder.NDR, bytes.getWKBByteOrder());
    }

    @Test
    public void test_create_bytes_having_specified_size() {
        Bytes bytes = Bytes.allocate(100);
        assertEquals(100, bytes.capacity());
    }

    @Test
    public void test_put_xdr_double() {
        double expected = 1234.56789;
        Bytes bytes = Bytes.allocate(8);
        bytes.setWKBByteOrder(WKBByteOrder.XDR);
        bytes.putDouble(expected);
        bytes.rewind();
        double d = bytes.getDouble();
        assertEquals(expected, d);
    }

    @Test
    public void test_put_ndr_double() {
        double expected = 1234.56789;
        Bytes bytes = Bytes.allocate(8);
        bytes.setWKBByteOrder(WKBByteOrder.NDR);
        bytes.putDouble(expected);
        bytes.rewind();
        double d = bytes.getDouble();
        assertEquals(expected, d);
    }

    @Test
    public void test_put_byte() {
        byte expected = (byte) 0xFF;
        Bytes bytes = Bytes.allocate(1);
        bytes.put(expected);
        bytes.rewind();
        assertEquals(expected, bytes.get());
    }

    @Test
    public void test_put_int() {
        int expected = 0xFF;
        Bytes bytes = Bytes.allocate(4);
        bytes.putInt(expected);
        bytes.rewind();
        assertEquals(expected, bytes.getInt());
    }

    @Test
    public void test_put_long() {
        long expected = Long.MAX_VALUE;
        Bytes bytes = Bytes.allocate(8);
        bytes.putLong(expected);
        bytes.rewind();
        assertEquals(expected, bytes.getLong());
    }


    @Test
    public void test_put_float() {
        float expected = 1234.567f;
        Bytes bytes = Bytes.allocate(4);
        bytes.putFloat(expected);
        bytes.rewind();
        assertEquals(expected, bytes.getFloat());
    }

    @Test
    public void test_put_uint() {
        long expected = 4294967295L;
        Bytes bytes = Bytes.allocate(4);
        bytes.putUInt(expected);
        bytes.rewind();
        assertEquals(expected, bytes.getUInt());
    }

    @Test
    public void test_put_too_large_value_in_uint_throws_exception() {
        long value = Bytes.UINT_MAX_VALUE;
        Bytes bytes = Bytes.allocate(4);
        bytes.putUInt(value);
        bytes.rewind();
        assertEquals(value, bytes.getUInt());
        value++;
        Bytes bytes2 = Bytes.allocate(4);
        try {
            bytes2.putUInt(value);
            fail();
        } catch (RuntimeException e) {
            //OK
        }
    }


}

