package org.geolatte.geom.codec;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTokenizer {

    final public static char openListChar = '(';
    final public static char closeListChar = ')';
    final public static char elementSeparator = ',';

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

    public void back(int numPos) {
        if (currentPos < numPos) {
            throw new IllegalStateException("Trying to return beyond start of input");
        }
        currentPos -= numPos;
    }

    public int currentPos() {
        return currentPos;
    }

    public String input() {
        return this.input.toString();
    }

    public void skipWhitespace() {
        while (!endOfInput() && Character.isWhitespace(currentChar())) {
            currentPos++;
        }
    }

    private boolean endOfInput() {
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
        } catch (NumberFormatException ex) {
            throw new WktDecodeException("Expected double", ex);
        }
    }

    private void readDigits(StringBuilder stb) {
        if (!endOfInput() && !Character.isDigit(currentChar())) {
            nextChar();
        }
        while (!endOfInput() && Character.isDigit(currentChar())) {
            stb.append(currentChar());
            nextChar();
        }
    }


    public String readLiteralText() {
        skipWhitespace();
        if (currentChar() != '"') throw new WktDecodeException("Expected quote character");
        StringBuilder builder = new StringBuilder();
        nextChar();
        while (currentChar() != '"') {
            builder.append(currentChar());
            nextChar();
            if (endOfInput()) {
                throw new WktDecodeException("Literal text is not terminated");
            }
        }
        nextChar();
        return builder.toString();
    }

    public String readText() {
        if (hasMoreInput() && !Character.isLetterOrDigit(currentChar()))
            throw new WktDecodeException("Expected a letter or digit");
        StringBuilder builder = new StringBuilder();
        while (!endOfInput() && Character.isLetterOrDigit(currentChar())) {
            builder.append(currentChar());
            nextChar();
        }

        return builder.toString();
    }

    public char currentChar() {
        try {
            return input.charAt(currentPos);
        } catch (StringIndexOutOfBoundsException e) {
            throw new WktDecodeException("Unexpected end of input at position " + currentPos);
        }
    }

    public boolean matchesChar(char c, boolean skipWhitespace) {
        if (skipWhitespace) {
            skipWhitespace();
        }
        if (currentChar() == c) {
            nextChar();
            return true;
        }
        return false;
    }

    public boolean matchPattern(Pattern pattern) {
        skipWhitespace();
        Matcher m = pattern.matcher(input);
        m.region(currentPos, input.length());
        if (m.lookingAt()) {
            currentPos = m.end();
            return true;
        }
        return false;
    }

    public Optional<String> extractGroupFromPattern(Pattern pattern, int group) {
        skipWhitespace();
        Matcher m = pattern.matcher(input);
        m.region(currentPos, input.length());
        if (m.lookingAt()) {
            try {
                currentPos = m.end();
                return Optional.of(m.group(group));
            } catch (IndexOutOfBoundsException t) {
                throw new WktDecodeException("Attempted to extract non-existent capture group");
            }
        } else {
            return Optional.empty();
        }
    }


    public Optional<Character> matchesOneOf(char... choices) {
        skipWhitespace();
        for (char c : choices) {
            if (currentChar() == c) {
                nextChar();
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }


    public boolean matchesOpenList() {
        return matchesChar(openListChar, true);
    }

    public boolean matchesCloseList() {
        return matchesChar(closeListChar, true);
    }

    public boolean matchElementSeparator() {
        return matchesChar(elementSeparator, true);
    }

    /**
     * Reads a number at the current position.
     * <p>Note that this method loses precision, e.g. 51.16666723333333 becomes 51.16666723333332) </p>
     *
     * @return a double
     */
    protected double fastReadNumber() {
        skipWhitespace();
        int startPos = currentPos;
        char c = input.charAt(currentPos);
        double sign = 1.0d;
        //read the sign
        if (c == '-') {
            sign = -1.0d;
            c = input.charAt(++currentPos);
        }

        //read the number and put it in form <long>E<long>
        long s = 0L;
        boolean digitsSeen = false;
        boolean decPntSeen = false;
        long decPos = -1;
        while (true) {

            if (Character.isDigit(c)) {
                s = 10 * s + (c - '0');
                digitsSeen = true;
            } else if (c == '.') {
                if (decPntSeen) {
                    throw new WktDecodeException("Invalid number format at position " + currentPos);
                }
                decPntSeen = true;
            } else {
                break;
            }
            if (decPntSeen) {
                decPos++;
            }

            nextChar();
            if (endOfInput()) break;
            c = currentChar();
        }

        //read the exponent (scientific notation)
        long exp = 0L;
        long expSign = 1L;
        if (c == 'e' || c == 'E') {
            c = input.charAt(++currentPos);
            if (c == '-') {
                expSign = -1L;
                c = input.charAt(++currentPos);
            }
            while (Character.isDigit(c)) {
                exp = 10 * exp + (c - '0');
                nextChar();
                if (endOfInput()) break;
                c = currentChar();
            }
        }
        if (!digitsSeen) {
            throw new WktDecodeException("Invalid number format at position " + currentPos);
        }
        long p = decPos >= 0 ? expSign * exp - decPos : expSign * exp;
        int endPos = currentPos;
        return toDouble(sign, s, p, startPos, endPos);
    }

    /**
     * Converts the decimal number representation into a double
     * <p>
     * This routine tries to apply the "Fast path" to get a really fast conversion, if applicable. If not, it
     * delegates to the more expensive Double.parseDouble() StdLib conversion.
     * <p>
     * See: http://www.exploringbinary.com/fast-path-decimal-to-floating-point-conversion/
     * and Handbook of Floating-Point Arithmetic, p. 47-8 (Muller e.a)
     *
     * @param sign     the sign of the number
     * @param s        the decimal mantissa or significand as a long
     * @param p        the exponent as a long
     * @param startPos the start position in the input for the parsed number
     * @param endPos   the end position in the input for the parsed number
     * @return the double at the specified positions
     */
    private double toDouble(double sign, long s, long p, int startPos, int endPos) {
        //check if the Fast-path is applicable.
        // we also test for negative s values to account for overrun/underrun conditions in the calling fastNumber procedure
        if (s == 0) {
            return 0.0d;
        } else if (s > 0 && s <= S_MAX && Math.abs(p) <= P_MAX) {
            if (p == 0l) {
                return sign * s;
            } else if (p < 0) {
                return sign * (s / Math.pow(10, -p));
            } else {
                return sign * (s * Math.pow(10, p));
            }
        } else {
            return Double.parseDouble(input.subSequence(startPos, endPos).toString());
        }
    }

    private static final long S_MAX = 9007199254740991L;
    private static final long P_MAX = 22L;

    private void nextChar() {
        currentPos++;
    }


}
