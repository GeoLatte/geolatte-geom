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

/**
 * An abstract WKT decoder.
 *
 * <p>This class does not implement interface WktDecoder because it contains shared code to be used
 * both in CrsWktDecoder and the Geometry WktDecoders.</p>
 *
 * <p><code>AbstractWktDecoder</code>s are not thread-safe.</p>
 *
 * @param <T> the type of the decoded WKT.
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/19/11
 */
abstract class AbstractWktDecoder<T> {

    private final WktVariant wktVariant;

    protected WktToken currentToken;
    private AbstractWktTokenizer tokenizer;

    /**
     * The constructor of this AbstractWktDecoder. It sets the variant.
     *
     * @param wktVariant The <code>WktVariant</code> to be used by this decoder.
     */
    public AbstractWktDecoder(WktVariant wktVariant) {
        this.wktVariant = wktVariant;
    }


    protected void setTokenizer(AbstractWktTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.currentToken = null;
    }

    /**
     * Returns the text and moves to the next token if the current token matches text, otherwise throws an exception.
     *
     * @return the matched text
     * @throws WktDecodeException when the current token does not match text.
     */
    protected String decodeText() {
        if (currentToken instanceof WktTextToken) {
            String text = ((WktTextToken) currentToken).getText();
            nextToken();
            return text;
        }
        throw new WktDecodeException("Expected text token, received " + currentToken.toString());
    }

    protected int decodeInt() {
        if (currentToken instanceof WktNumberToken) {
            double num = ((WktNumberToken) currentToken).getNumber();
            nextToken();
            try {
                return (int)num;
            } catch (Exception e) {
                throw new WktDecodeException("Expected Integer, received " + currentToken.toString());
            }
        } else if (currentToken instanceof WktTextToken) {
            String text = ((WktTextToken) currentToken).getText();
            nextToken();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new WktDecodeException("Expected Integer, received " + currentToken.toString());
            }
        }
        throw new WktDecodeException("Expected text token, received " + currentToken.toString());
    }


    /**
     * Advances the decoding to the next token.
     */
    protected void nextToken() {
        currentToken = tokenizer.nextToken();
    }

    /**
     * Returns true and moves to the next token if the current token matches the open list token.
     *
     * @return True if the current token matches the open list token, false otherwise.
     */
    protected boolean matchesOpenList() {
        if (currentToken == getWktVariant().getOpenList()) {
            nextToken();
            return true;
        }
        return false;
    }

    /**
     * Returns true and moves to the next token if the current token matches the close list token.
     *
     * @return True if the current token matches the close list token, false otherwise.
     */
    protected boolean matchesCloseList() {
        if (currentToken == getWktVariant().getCloseList()) {
            nextToken();
            return true;
        }
        return false;
    }

    /**
     * Returns true and moves to the next token if the current token matches the element separator token.
     *
     * @return True if the current token matches the element separator token, false otherwise.
     */
    protected boolean matchesElementSeparator() {
        if (currentToken == getWktVariant().getElementSeparator()) {
            nextToken();
            return true;
        }
        return false;
    }

    /**
     * Returns the value of the current token and moves to the next token if the current token matches a number.
     *
     * @return The value of the current token if the current token matches a number.
     * @throws WktDecodeException if the current token does not match a number.
     */
    protected double decodeNumber() {
        if (currentToken instanceof WktNumberToken) {
            double value = ((WktNumberToken) currentToken).getNumber();
            nextToken();
            return value;
        }
        throw new WktDecodeException("Expected a number ; received " + currentToken.toString());
    }

    /**
     * Returns the <code>WktVariant</code> for this decoder.
     *
     * @return the <code>WktVariant</code> for this decoder.
     */
    protected WktVariant getWktVariant() {
        return this.wktVariant;
    }

    /**
     * Reports the current position of the tokenizer.
     *
     * @return the current position of the tokenizer.
     */
    protected int getTokenizerPosition() {
        return this.tokenizer.position();
    }
}
