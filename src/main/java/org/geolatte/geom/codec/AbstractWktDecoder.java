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
 * <p><code>AbstractWktDecoder</code>s are not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 11/19/11
 *
 * @param <T> the type of the decoded WKT.
 */
public abstract class AbstractWktDecoder<T> {

    protected WktToken currentToken;
    private WktTokenizer tokenizer;
    private final WktVariant wktVariant;

    public AbstractWktDecoder(WktVariant wktVariant) {
        this.wktVariant = wktVariant;
    }

    /**
     * Decodes a WKT representation.
     *
     * @param wkt the WKT string to decode
     * @return the decoded object
     */
    abstract public T decode(String wkt);


    //TODO -- make the decode method concrete, and remove the need for setTokenizer(), e.g. by
    //introducing a WktTokenizerFactory

    protected void setTokenizer(WktTokenizer tokenizer){
        this.tokenizer = tokenizer;
    }

    /**
     * Returns the text and moves to the next token if the current token matches text, otherwise throws an exception.
     *
     * @return the matched text
     * @throws WktParseException when the current token does not match text.
     */
    protected String matchesText() {
        if (currentToken instanceof WktTextToken) {
            String text = ((WktTextToken) currentToken).getText();
            nextToken();
            return text;
        }
        throw new WktParseException("Expected text token, received " + currentToken.toString());
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
     * @return
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
     * @return
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
     * @return
     */
    protected boolean matchesElemSeparator() {
        if (currentToken == getWktVariant().getElemSep()) {
            nextToken();
            return true;
        }
        return false;
    }

    /**
     * Returns the value of the current token and moves to the next token if the current token matches a number.
     *
     * @return
     * @throws WktParseException if the current token does not match a number.
     */
    protected double matchesNumber(){
        if (currentToken instanceof WktNumberToken) {
            double value = ((WktNumberToken)currentToken).getNumber();
            nextToken();
            return value;
        }
        throw new WktParseException("Expected a number ; received " + currentToken.toString());
    }

    /**
     * Returns the <code>WktVariant</code> for this decoder.
     * @return
     */
    protected WktVariant getWktVariant(){
        return this.wktVariant;
    }

    /**
     * Reports the current position of the tokenizer.
     *
     * @return
     */
    protected int getTokenizerPosition(){
        return this.tokenizer.position();
    }
}
