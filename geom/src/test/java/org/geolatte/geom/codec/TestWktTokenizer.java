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

import org.geolatte.geom.GeometryType;
import org.geolatte.geom.C2D;
import org.geolatte.geom.C2DM;
import org.geolatte.geom.C3D;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class TestWktTokenizer {

    private static CoordinateReferenceSystem<C2D> crs = CoordinateReferenceSystems.PROJECTED_2D_METER;


    private WktVariant words = new PostgisWktVariant();


    @Test
    public void test_only_whitespace() {
        String wkt = "    ";
        WktTokenizer tokenizer = new WktTokenizer(wkt, words, crs);
        assertFalse(tokenizer.moreTokens());
    }


    @Test
    public void test_tokenize_empty_point() {
        String wkt = "POINT EMPTY";
        WktTokenizer tokens = new WktTokenizer(wkt, words, crs);
        assertTrue(tokens.moreTokens());
        WktGeometryToken token = (WktGeometryToken) (tokens.nextToken());
        assertEquals(GeometryType.POINT, token.getType());
        assertFalse(token.isMeasured());
        assertTrue(tokens.moreTokens());
        assertTrue(tokens.nextToken() instanceof WktEmptyGeometryToken);
    }


    @Test
    public void test_tokenize_point() {
        String wkt = "POINT (20 33.3)";
        WktTokenizer tokens = new WktTokenizer(wkt, words, crs);
        assertTrue(tokens.moreTokens());
        WktGeometryToken token = (WktGeometryToken) (tokens.nextToken());
        assertEquals(GeometryType.POINT, token.getType());
        assertFalse(token.isMeasured());
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken(), words.getOpenList());
        assertTrue(tokens.moreTokens());
        WktPointSequenceToken pstoken = (WktPointSequenceToken) (tokens.nextToken());
        assertEquals(1, pstoken.getPositions().size());
        assertEquals(20, pstoken.getPositions().getPositionN(0).getCoordinate(0), Math.ulp(20d));
        assertEquals(33.3, pstoken.getPositions().getPositionN(0).getCoordinate(1), Math.ulp(20d));
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken() ,words.getCloseList());
        assertFalse(tokens.moreTokens());
    }

    @Test
    public void test_tokenize_point_3D() {
        String wkt = "POINT (20 33.3 .24)";
        WktTokenizer tokens = new WktTokenizer(wkt, words, crs);
        assertTrue(tokens.moreTokens());
        WktGeometryToken token = (WktGeometryToken) (tokens.nextToken());
        assertEquals(GeometryType.POINT, token.getType());
        assertFalse(token.isMeasured());
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken(), words.getOpenList());
        assertTrue(tokens.moreTokens());
        WktPointSequenceToken pstoken = (WktPointSequenceToken) (tokens.nextToken());
        assertEquals(1, pstoken.getPositions().size());
        assertEquals(20, pstoken.getPositions().getPositionN(0).getCoordinate(0), Math.ulp(20d));
        assertEquals(33.3, pstoken.getPositions().getPositionN(0).getCoordinate(1), Math.ulp(20d));
        assertEquals(0.24, pstoken.getPositions().getPositionN(0).getCoordinate(2), Math.ulp(2d));
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken(), words.getCloseList());
        assertFalse(tokens.moreTokens());
    }

    @Test
    public void test_tokenize_point_M() {
        String wkt = "POINTM (20 33.3 .24)";
        WktTokenizer tokens = new WktTokenizer(wkt, words, crs);
        assertTrue(tokens.moreTokens());
        WktGeometryToken token = (WktGeometryToken) (tokens.nextToken());
        assertEquals(GeometryType.POINT, token.getType());
        assertTrue(token.isMeasured());
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken(), words.getOpenList());
        assertTrue(tokens.moreTokens());
        WktPointSequenceToken pstoken = (WktPointSequenceToken) (tokens.nextToken());
        assertEquals(1, pstoken.getPositions().size());
        C2DM pos = (C2DM) pstoken.getPositions().getPositionN(0);
        assertEquals(20, pos.getX(), Math.ulp(20d));
        assertEquals(33.3, pos.getY(), Math.ulp(20d));
        assertEquals(0.24, pos.getM(), Math.ulp(2d));
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken(), words.getCloseList());
        assertFalse(tokens.moreTokens());
    }

    @Test
    public void test_tokenize_linestring() {
        String wkt = "LINESTRING(20 33.3 .24 , .1 2 3)";
        WktTokenizer tokens = new WktTokenizer(wkt, words, crs);
        assertTrue(tokens.moreTokens());
        WktGeometryToken token = (WktGeometryToken) (tokens.nextToken());
        assertEquals(GeometryType.LINESTRING, token.getType());
        assertFalse(token.isMeasured());
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken(), words.getOpenList());
        assertTrue(tokens.moreTokens());
        WktPointSequenceToken pstoken = (WktPointSequenceToken) (tokens.nextToken());
        assertEquals(2, pstoken.getPositions().size());
        C3D pos = (C3D) pstoken.getPositions().getPositionN(0);
        assertEquals(20, pos.getX(), Math.ulp(20d));
        assertEquals(33.3, pos.getY(), Math.ulp(20d));
        assertEquals(0.24, pos.getZ(), Math.ulp(2d));
        pos = (C3D) pstoken.getPositions().getPositionN(1);
        assertEquals(.1d, pos.getX(), Math.ulp(20d));
        assertEquals(2d, pos.getY(), Math.ulp(20d));
        assertEquals(3d, pos.getZ(), Math.ulp(2d));
        assertTrue(tokens.moreTokens());
        assertEquals(tokens.nextToken() ,words.getCloseList());
        assertFalse(tokens.moreTokens());
    }

    @Test
    public void test_tokenize_polygon() {
        String wkt = "POLYGON((5 5, 1 0, 1 1 ,0 1, 3 3),(0.25 0.25, 0.25 0.5, 0.5 0.5, 0.5 0.25, 0.25 0.25))";
        WktTokenizer tokens = new WktTokenizer(wkt, words, crs);
        assertTrue(tokens.moreTokens());
        WktGeometryToken token = (WktGeometryToken) (tokens.nextToken());
        assertEquals(GeometryType.POLYGON, token.getType());
        assertFalse(token.isMeasured());
        assertEquals(tokens.nextToken(), words.getOpenList());
        assertEquals(tokens.nextToken(), words.getOpenList());
        assertTrue(tokens.nextToken() instanceof WktPointSequenceToken);
        assertEquals(tokens.nextToken(), words.getCloseList());
        assertEquals(tokens.nextToken(), words.getElementSeparator());
        assertEquals(tokens.nextToken(), words.getOpenList());
        assertTrue(tokens.nextToken() instanceof WktPointSequenceToken);
        assertEquals(tokens.nextToken(), words.getCloseList());
        assertEquals(tokens.nextToken(), words.getCloseList());
        assertFalse(tokens.moreTokens());
    }


    @Test
    public void testWktTokenizerToDoubleSampleValues(){
        WktTokenizer tokens = null;
        String[] strings = {
                "12.345 ",
                "123456.1234 ",
                "123456789.12345 ",
                "1.23456 ",
                "0.123456 ",
                "0.00123456 "

            };
        for (String s : strings) {
            tokens = new WktTokenizer(s, words, crs);
            assertTrue(Double.valueOf(s) == tokens.fastReadNumber());
        }

    }


    @Test
    public void testWktTokenizerToDoubleRandomValues(){
        WktTokenizer tokens = null;
        for (int i = 0; i < 10000; i++) {
            double m = Math.random()*1000;
            int e = (int)(-20 + Math.random()*20);



            String testValue = Double.valueOf(m) + "E" + e;
            String wkt = testValue + " ";
            tokens = new WktTokenizer(wkt, words, crs);

            double rec = tokens.fastReadNumber();
            assertTrue("Expected == " + testValue  + ", received == " + Double.toString(rec), Double.valueOf(testValue) == rec);

        }

    }

    @Test
    public void testWktTokenizerToDoubleOnValueLargerThanMaxValue(){
        WktTokenizer tokens = null;
        String wkt = "1.7976931348623157E309 ";
        tokens = new WktTokenizer(wkt, words, crs);

        double v = tokens.fastReadNumber();
        assertTrue(Double.isInfinite(v));


    }

    @Test
    public void testWktTokenizerToDoubleOnValueLargerThanMinValue(){
        WktTokenizer tokens = null;
        String wkt = "4.8E-326 ";
        tokens = new WktTokenizer(wkt, words, crs);

        double v = tokens.fastReadNumber();
        assertTrue(v == Double.valueOf(wkt));


    }


    @Test
    public void testWktTokenizerOnVeryLongDigitalExpansion(){
        WktTokenizer tokens = null;
        String wkt = "489823441.12345678912334432212345543211345654332211133455068345928456727 ";
        tokens = new WktTokenizer(wkt, words, crs);

        double v = tokens.fastReadNumber();
        assertTrue(v == Double.valueOf(wkt));


    }


}


