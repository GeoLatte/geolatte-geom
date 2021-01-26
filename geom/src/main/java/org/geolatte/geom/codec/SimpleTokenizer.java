package org.geolatte.geom.codec;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTokenizer {

    final static char openListChar ='(';
    final static char closeListChar =')';
    final static char elementSeparator = ',';

    final private CharSequence input;

    private int currentPos = 0;


    public SimpleTokenizer(String input) {
        this.input = input;
    }

    public void resetTo(int newPos) {
        if (currentPos < newPos) {
            throw new IllegalStateException("Trying to reset to a position beyond current position");
        }
        currentPos = newPos;
    }

    public int currentPos(){
        return currentPos;
    }

    public void skipWhitespace() {
        while (!endOfInput() && Character.isWhitespace(currentChar())) {
            currentPos++;
        }
    }

    private boolean endOfInput(){
        return currentPos >= input.length();
    }

    public boolean hasMoreInput() {
        skipWhitespace();
        return !endOfInput();
    }

    public double readNumber() {
        skipWhitespace();

        StringBuilder stb = new StringBuilder();
        char c = currentChar();
        if (c == '-') {
            stb.append(c);
            nextChar();
        }
        readDigits(stb);
        if (!endOfInput() && currentChar() == '.') {
            stb.append(currentChar());
            nextChar();
            readDigits(stb);
        }
        try {
            return Double.parseDouble(stb.toString());
        } catch(NumberFormatException ex) {
            throw new WktParseException("Expected double", ex);
        }
    }

    private void readDigits(StringBuilder stb) {
        if (!endOfInput() && !Character.isDigit(currentChar())) {
            nextChar();
        }
        while(!endOfInput() && Character.isDigit(currentChar())) {
            stb.append(currentChar());
            nextChar();
        }
    }


    public String readLiteralText() {
        skipWhitespace();
        if( currentChar() != '"') throw new WktParseException("Expected quote character");
        StringBuilder builder = new StringBuilder();
        nextChar();
        while (currentChar() != '"') {
            builder.append(currentChar());
            nextChar();
            if (endOfInput()) {
                throw new WktParseException("Literal text is not terminated");
            }
        }
        nextChar();
        return builder.toString();
    }

    public String readText(){
        if( hasMoreInput() && !Character.isLetterOrDigit(currentChar())) throw new WktParseException("Expected a letter or digit");
        StringBuilder builder = new StringBuilder();
        while (!endOfInput() && Character.isLetterOrDigit(currentChar())) {
            builder.append(currentChar());
            nextChar();
        }

        return builder.toString();
    }

    public char currentChar() {
        return input.charAt(currentPos);
    }

    public boolean matchPattern(Pattern pattern){
        skipWhitespace();
        Matcher m = pattern.matcher(input);
        m.region(currentPos, input.length());
        if (m.lookingAt()) {
            currentPos = m.end();
            return true;
        }
        return false;
    }

    public Optional<String> extractGroupFromPattern(Pattern pattern, int group){
        skipWhitespace();
        Matcher m = pattern.matcher(input);
        m.region(currentPos, input.length());
        if(m.lookingAt()) {
            try {
                currentPos = m.end();
                return Optional.of(m.group(group));
            } catch(IndexOutOfBoundsException t) {
                throw new WktParseException("Attempted to extract non-existent capture group");
            }
        } else {
            return Optional.empty();
        }
    }

    private boolean matchesChar(char expected){
        skipWhitespace();
        if(currentChar() != expected) {
            return false;
        }
        nextChar();
        return true;
    }

    public boolean matchesOpenList(){
        return matchesChar(openListChar);
    }

    public boolean matchesCloseList(){
        return matchesChar(closeListChar);
    }

    public boolean matchElementSeparator(){
        return matchesChar(elementSeparator);
    }

    private void nextChar(){
        currentPos++;
    }

}
