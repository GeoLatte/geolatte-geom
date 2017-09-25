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
 * Copyright (C) 2010 - 2012 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.crs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 9/1/12
 */
public class TestCrsId {

    @Test
    public void testFactorymethods() {
        assertEquals("EPSG code 0 and -1 should map to undefined", CrsId.UNDEFINED, CrsId.valueOf(0));
        assertEquals("EPSG code 0 and -1 should map to undefined", CrsId.UNDEFINED, CrsId.valueOf(-1));
        assertEquals("Incorrect code", 31370, CrsId.valueOf(31370).getCode());
        assertEquals("Incorrect authority", "EPSG", CrsId.valueOf(31370).getAuthority());
        assertEquals("Authorities should be returned in uppercase.", "AUTH", CrsId.valueOf("auth", 0).getAuthority());
        assertFalse("0 and -1 codes are only Undefined in EPSG", CrsId.UNDEFINED.equals(CrsId.valueOf("auth", 0)));
        assertEquals("Normalisation of code 0 to -1, should only execute for EPSG", 0, new CrsId("auth", 0).getCode());
    }

    @Test
    public void testParse(){
        CrsId c = CrsId.valueOf("EPSG", 31370);
        assertEquals("Parse of toString() on CrsId instance should return equal instance",c, CrsId.parse(c.toString()));
        assertEquals(c, CrsId.parse("epsg:31370"));
        assertEquals(c, CrsId.parse("31370"));
        assertEquals(CrsId.valueOf("AUTH", 3), CrsId.parse("auth:3"));
        assertEquals(CrsId.UNDEFINED, CrsId.parse("EPSG:0"));
        assertEquals(CrsId.UNDEFINED, CrsId.parse("0"));
        assertEquals(CrsId.UNDEFINED, CrsId.parse("-1"));
    }

    @Test
    public void testParseURNSyntax() {
        CrsId c = CrsId.valueOf("OGC", 26912);
        assertEquals(c, CrsId.parse("urn:ogc:def:crs:OGC::26912"));
    }


    @Test
    public void testtoUrn() {
        CrsId c = CrsId.valueOf("OGC", 26912);
        assertEquals("urn:ogc:def:crs:OGC::26912", c.toUrn());
    }


    @Test
    public void testValueOf(){
        assertEquals(CrsId.parse("epsg:4326"), CrsId.valueOf("EPSG", 4326));
        assertEquals(CrsId.parse("4326"), CrsId.valueOf(4326));
        assertEquals(CrsId.UNDEFINED, CrsId.valueOf(0));
        assertEquals(CrsId.UNDEFINED, CrsId.valueOf(-1));
        assertEquals(CrsId.parse("1"), CrsId.valueOf(1));
    }

    @Test
    public void testValueOfReturnsIdenticalObjectForIdentialEPSGCodes(){
        CrsId id1 = CrsId.valueOf(4326);
        CrsId id2 = CrsId.valueOf(4326);
        assertTrue(id1 == id2);
    }
    

    @Test
    public void testToString() {
        assertEquals("EPSG:31370", CrsId.valueOf("epsg", 31370).toString());
        assertEquals("EPSG:31370", CrsId.valueOf(31370).toString());
        assertEquals("EPSG:-1", CrsId.UNDEFINED.toString());
    }

    @Test
    public void testHashCode(){
        CrsId id1 = CrsId.valueOf(12);
        CrsId id2 = CrsId.valueOf(12);
        assertTrue(id1.equals(id2));
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyAuthority() {
        CrsId.valueOf("", 1234);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAuthority() {
        CrsId.valueOf(null, 1234);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseIllegalFormatNonNumericCode(){
        CrsId.parse("auth:oef");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIllegalFormatEmptyAuthority(){
        CrsId.parse(":1234");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullInput(){
        CrsId.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseEmptyInput(){
        CrsId.parse("");
    }


}
