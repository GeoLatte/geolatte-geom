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
 *         creation-date: 8/4/11
 */
public class TestCRSWKTTokenizer {

    private String wkt = "   GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";

    @Test
    public void testGEOGCSWords() {
        WKTTokenizer tokenizer = new WKTTokenizer(wkt, new CRSWKTWordMatcher(), '[', ']', false);
        //read type coordinate system
        assertTrue(tokenizer.moreTokens());
        assertEquals(CRSWKTToken.GEOGCS, tokenizer.nextToken());

        //read coordinate system
        assertTrue(tokenizer.moreTokens());
        assertTrue(tokenizer.nextToken() instanceof WKTToken.StartList);

        //read coordinate system name
        assertTrue(tokenizer.moreTokens());
        WKTToken token = tokenizer.nextToken();
        assertNotNull(token);
        assertEquals(WKTToken.TextToken.class, token.getClass());
        WKTToken.TextToken textToken = ((WKTToken.TextToken)token);
        assertEquals("WGS 84", textToken.getText());

        //read seperator
        assertTrue(tokenizer.moreTokens());
        token = tokenizer.nextToken();
        assertEquals(WKTToken.ElementSeparator.class, token.getClass());

        //read Datum
        assertTrue(tokenizer.moreTokens());
        token = tokenizer.nextToken();
        assertEquals(CRSWKTToken.class, token.getClass());
        assertEquals(CRSWKTToken.DATUM, token);


    }

}
