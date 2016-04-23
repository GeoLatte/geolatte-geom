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

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class TestCrsWktWordMatcher {

    private CrsWktVariant matcher = new CrsWktVariant();

    @Test
    public void testMatchingProjectedCRS(){
        String test = "PROJCS";
        WktToken token = matcher.matchKeyword(test, 0, test.length());
        assertNotNull(token);
        assertTrue(token instanceof WktKeywordToken);
        assertEquals(CrsWktVariant.PROJCS, token);
    }

    @Test
    public void testMatchingGeoGCSCRS(){
        String test = " GEOGCS  ";
        WktToken token = matcher.matchKeyword(test, 1, 7);
        assertNotNull(token);
        assertEquals(CrsWktVariant.GEOGCS, token);
    }

    @Test
    public void testMatchingGeoAXIS(){
        String test = " AXIS  ";
        WktToken token = matcher.matchKeyword(test, 1, 5);
        assertNotNull(token);
        assertEquals(CrsWktVariant.AXIS, token);
    }

    @Test
    public void testMatchingCompd_cs(){
        String test = " COMPD_CS  ";
        WktToken token = matcher.matchKeyword(test, 1, 9);
        assertNotNull(token);
        assertEquals(CrsWktVariant.COMPD_CS, token);
    }


    @Test
    public void testNoMatchingWordThrowsException() {
        String test = "BLABLA";

        try {
            matcher.matchKeyword(test, 0, test.length());
            fail();
        }catch(WktDecodeException e){
            //OK
        }
    }

}
