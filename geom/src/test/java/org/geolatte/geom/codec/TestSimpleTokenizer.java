package org.geolatte.geom.codec;

import org.junit.Test;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class TestSimpleTokenizer {

    @Test
    public void testskipWhitespace(){
        SimpleTokenizer t = new SimpleTokenizer("  abcdef");
        t.skipWhitespace();
        assertEquals(2, t.currentPos());
        t.skipWhitespace();
        assertEquals(2, t.currentPos());
    }

    @Test
    public void testRestTo(){
        SimpleTokenizer t = new SimpleTokenizer("  abcdef");
        t.skipWhitespace();
        t.resetTo(1);
        assertEquals(1, t.currentPos());
    }

    @Test(expected = IllegalStateException.class)
    public void testRestWillFailWhenGreaterThanCurrent(){
        SimpleTokenizer t = new SimpleTokenizer("  abcdef");
        t.skipWhitespace();
        t.resetTo(4);
    }

    @Test
    public void testHasMoreInput() {
        SimpleTokenizer t = new SimpleTokenizer("  abcdef ");
        assertTrue(t.hasMoreInput());
        t.readText();
        assertFalse(t.hasMoreInput());
    }

    @Test
    public void testHasMoreInputFalseOnEmpty() {
        SimpleTokenizer t = new SimpleTokenizer("");
        assertFalse(t.hasMoreInput());
    }


    @Test
    public void testReadText(){
        SimpleTokenizer t = new SimpleTokenizer("  abc  def");
        assertEquals("abc", t.readText());
        assertEquals("def", t.readText());
        assertFalse(t.hasMoreInput());
    }

    @Test
    public void testReadTextLeavesPointerBeyondText(){
        SimpleTokenizer t = new SimpleTokenizer("  abc  def  ");
        assertEquals("abc", t.readText());
        assertEquals(5, t.currentPos());
    }


    @Test(expected = WktDecodeException.class)
    public void testReadTextFailsWhenNotAtLetterOrDigit(){
        SimpleTokenizer t = new SimpleTokenizer("$abc");
        t.readText();
    }

    @Test
    public void testReadNumber(){
        SimpleTokenizer t = new SimpleTokenizer("   12.34  ");
        assertEquals(12.34, t.readNumber(), 0.00001);
        assertFalse(t.hasMoreInput());

        t = new SimpleTokenizer("   -12.34  ");
        assertEquals(-12.34, t.readNumber(), 0.00001);
        assertFalse(t.hasMoreInput());

        t = new SimpleTokenizer("-1234");
        assertEquals(-1234, t.readNumber(), 0.00001);
        assertFalse(t.hasMoreInput());

        t = new SimpleTokenizer("1234ab");
        assertEquals(1234, t.readNumber(), 0.00001);
        assertTrue(t.hasMoreInput());
        assertEquals(4, t.currentPos());
        assertEquals("ab", t.readText());

        t = new SimpleTokenizer("1234.");
        assertEquals(1234, t.readNumber(), 0.00001);
        assertFalse(t.hasMoreInput());
    }

    @Test(expected = WktDecodeException.class)
    public void testReadNumberFailsOnAlpha(){
        SimpleTokenizer t = new SimpleTokenizer("  abc124e ");
        t.readNumber();
    }

    @Test
    public void testLiteralText(){
        SimpleTokenizer t = new SimpleTokenizer(" \"this is text\" ");
        assertEquals("this is text", t.readLiteralText());
        assertFalse(t.hasMoreInput());
    }


    @Test(expected= WktDecodeException.class)
    public void testLiteralTextFailsWhenNotTerminated(){
        SimpleTokenizer t = new SimpleTokenizer(" \"this is text ");
        t.readLiteralText();
    }

    @Test(expected= WktDecodeException.class)
    public void testLiteralTextFailsOnNotQuoteAtStart(){
        SimpleTokenizer t = new SimpleTokenizer(" This is text ");
        t.readLiteralText();
    }

    @Test
    public void testMatchPattern(){
        Pattern pattern = Pattern.compile("SRID=(.+);");
        String inputText = "prefix  SRID=4326;abcdef";
        SimpleTokenizer t = new SimpleTokenizer(inputText);
        //first skip the prefix text
        t.readText();
        assertTrue(t.matchPattern(pattern));
        assertEquals('a', t.currentChar());
    }

    @Test
    public void testMatchPatternFails(){
        Pattern pattern = Pattern.compile("SRID=(.+);");
        String inputText = "prefix  abcdef";
        SimpleTokenizer t = new SimpleTokenizer(inputText);
        //first skip the prefix text
        t.readText();
        t.skipWhitespace();
        int pos = t.currentPos();
        assertFalse(t.matchPattern(pattern));
        assertEquals(pos, t.currentPos());
    }

    @Test
    public void testExtractPattern(){
        Pattern pattern = Pattern.compile("^SRID=(.+);");
        String inputText = "prefix  SRID=4326;abcdef";
        SimpleTokenizer t = new SimpleTokenizer(inputText);
        //first skip the prefix text
        t.readText();
        assertEquals(Optional.of("4326"), t.extractGroupFromPattern(pattern, 1));
        assertEquals('a', t.currentChar());
    }


    @Test
    public void testFastReadNumber(){
        SimpleTokenizer t = new SimpleTokenizer("   12.34  ");
        assertEquals(12.34, t.fastReadNumber(), 0.00001);
        assertFalse(t.hasMoreInput());

        t = new SimpleTokenizer("   -12.34  ");
        assertEquals(-12.34, t.fastReadNumber(), 0.00001);
        assertFalse(t.hasMoreInput());

        t = new SimpleTokenizer("-1234");
        assertEquals(-1234, t.fastReadNumber(), 0.00001);
        assertFalse(t.hasMoreInput());

        t = new SimpleTokenizer("1234ab");
        assertEquals(1234, t.fastReadNumber(), 0.00001);
        assertTrue(t.hasMoreInput());
        assertEquals(4, t.currentPos());
        assertEquals("ab", t.readText());

        t = new SimpleTokenizer("1234.");
        assertEquals(1234, t.fastReadNumber(), 0.00001);
        assertFalse(t.hasMoreInput());

        t = new SimpleTokenizer("1234.0E003");
        assertEquals(1234000, t.fastReadNumber(), 0.00001);
        assertFalse(t.hasMoreInput());
    }

    @Test(expected= WktDecodeException.class)
    public void testFailfastreader(){
        SimpleTokenizer t = new SimpleTokenizer(" (1.2");
        t.fastReadNumber();
    }

    @Test
    public void testFastReadNumbersNoDecimals(){
        SimpleTokenizer t = new SimpleTokenizer(" 1.245 33");
        assertEquals(1.245, t.fastReadNumber(), 0.0001);
        assertEquals(33, t.fastReadNumber(), 0.0001);
    }

    @Test
    public void testMatchOneOf(){
        SimpleTokenizer t = new SimpleTokenizer("(12.3,45) )");
        assertEquals(Optional.of('('), t.matchesOneOf('(', ',', ')'));
        assertEquals(12.3, t.fastReadNumber(), 0.00001);
        assertEquals(Optional.of(','), t.matchesOneOf('(', ',', ')'));
        assertEquals(Optional.empty(), t.matchesOneOf('(', ',', ')'));
        assertEquals(45, t.fastReadNumber(), 0.00001);
        assertEquals(Optional.of(')'), t.matchesOneOf('(', ',', ')'));
        assertEquals(Optional.of(')'), t.matchesOneOf('(', ',', ')'));
        assertFalse(t.hasMoreInput());
    }

}
