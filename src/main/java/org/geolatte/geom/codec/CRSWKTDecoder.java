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

import org.geolatte.geom.crs.*;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CRSWKTDecoder {

    private WKTToken currentToken;
    private WKTTokenizer tokenizer;


    public CoordinateReferenceSystem decode(String wkt) {
        tokenizer = new WKTTokenizer(wkt, new CRSWKTWordMatcher(), '[', ']', false);
        nextToken();
        return matchesCRS();
    }

    private CoordinateReferenceSystem matchesCRS() {
        if (currentToken == CRSWKTToken.PROJCS) {
            return matchesProjectedCRS();
        } else if (currentToken == CRSWKTToken.GEOGCS) {
            return matchesGeographicCRS();
        } else if (currentToken == CRSWKTToken.GEOCCS) {
            return matchesGeocentricCRS();
        }
        throw new WKTParseException("Expected WKT Token PROJCS, GEOGCS or GEOCCS");
    }

    private CoordinateReferenceSystem matchesGeocentricCRS() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private CoordinateReferenceSystem matchesGeographicCRS() {
        String crsName = matchesName();
        matchesListDelimiter();
        GeodeticDatum datum = matchesDatum();
        matchesListDelimiter();
        PrimeMeridian primem = matchesPrimem();
        matchesListDelimiter();
        Unit unit = matchesUnit(Unit.Type.ANGULAR);
        int srid = 4326;
        GeographicCoordinateReferenceSystem system = new GeographicCoordinateReferenceSystem(srid, crsName, unit);
        system.setDatum(datum);
        system.setPrimeMeridian(primem);
        return system;
    }

    private Unit matchesUnit(Unit.Type type) {
        if (currentToken != CRSWKTToken.UNIT) {
            throw new WKTParseException("Expected UNIT keyword, found " + toString(currentToken));
        }
        String name = matchesName();
        matchesListDelimiter();
        double cf = matchesNumber();
        matchesListDelimiter();
        int srid = optionalMatchesAuthority();
        return new Unit(srid, name, type, cf);
    }

    private PrimeMeridian matchesPrimem() {
        if (currentToken != CRSWKTToken.PRIMEM) {
            throw new WKTParseException("Expected PRIMEM keyword, received " + toString(currentToken));
        }
        String name = matchesName();
        matchesListDelimiter();
        double longitude = matchesNumber();
        int srid = optionalMatchesAuthority();
        matchesEndList();
        return new PrimeMeridian(srid, name, longitude);
    }

    private CoordinateReferenceSystem matchesProjectedCRS() {
        String crsName = matchesName();
        CoordinateReferenceSystem geogcs = matchesGeographicCRS();
        int srid = 0;
        ProjectedCoordinateReferenceSystem result = new ProjectedCoordinateReferenceSystem(srid, crsName);
        return result;
    }

    private GeodeticDatum matchesDatum() {
        if (currentToken != CRSWKTToken.DATUM) {
            throw new WKTParseException("Expected DATUM token.");
        }
        String datumName = matchesName();
        matchesListDelimiter();
        Ellipsoid ellipsoid = matchesSpheroid();
        double[] toWGS84 = matchesToWGS84();
        int srid = optionalMatchesAuthority();
        matchesEndList();
        return new GeodeticDatum(srid, ellipsoid, datumName, toWGS84);
    }

    private double[] matchesToWGS84() {
        matchesListDelimiter();
        if (currentToken != CRSWKTToken.TOWGS84)
            return new double[0];
        double[] toWGS = new double[7];
        matchesStartList();
        for (int i = 0; i < 7; i++) {
            toWGS[i] = matchesNumber();
            matchesListDelimiter();
        }
        matchesEndList();
        return toWGS;
    }

    private Ellipsoid matchesSpheroid() {
        if (currentToken != CRSWKTToken.SPHEROID){
            throw new WKTParseException("Expected SPHEROID keyword, but received " + toString(currentToken));
        }
        String ellipsoidName = matchesName();
        matchesListDelimiter();
        double semiMajorAxis = matchesNumber();
        matchesListDelimiter();
        double inverseFlattening = matchesNumber();
        int srid = optionalMatchesAuthority();
        matchesEndList();
        return new Ellipsoid(srid, ellipsoidName,semiMajorAxis, inverseFlattening);
    }

    private int optionalMatchesAuthority() {
        matchesListDelimiter();
        if (currentToken != CRSWKTToken.AUTHORITY)
            return -1;

        nextToken();
        matchesStartList();
        String authority = matchesText();
        matchesListDelimiter();
        String value = matchesText();
        matchesEndList();
        if (authority.equals("EPSG")) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new WKTParseException("Expected EPSG integer code, received " + value);
            }
        }
        return -1;
    }

    private String matchesName() {
        nextToken();
        matchesStartList();
        return matchesText();
    }

    private String matchesText() {
        if (currentToken instanceof WKTToken.TextToken) {
            String text = ((WKTToken.TextToken) currentToken).getText();
            nextToken();
            return text;
        }
        throw new WKTParseException("Expected text token, received " + toString(currentToken));
    }


    //TODO -- these methods are shared with other decoders, move these abstract WKTDecoder class
    private void nextToken() {
        currentToken = tokenizer.nextToken();
    }

    private boolean matchesStartList() {
        if (currentToken instanceof WKTToken.StartList) {
            nextToken();
            return true;
        }
        return false;
    }

    private boolean matchesEndList() {
        if (currentToken instanceof WKTToken.EndList) {
            nextToken();
            return true;
        }
        return false;
    }

    private boolean matchesListDelimiter() {
        if (currentToken instanceof WKTToken.ElementSeparator) {
            nextToken();
            return true;
        }
        return false;
    }

    private double matchesNumber(){
        if (currentToken instanceof WKTToken.NumberToken) {
            double value = ((WKTToken.NumberToken)currentToken).getNumber();
            nextToken();
            return value;
        }
        throw new WKTParseException("Expected a number ; received " + toString(currentToken));
    }


    private String toString(WKTToken token){
        if (token instanceof CRSWKTToken) {
            return ((CRSWKTToken)token).getPattern().toString();
        }
        return token.getClass().getSimpleName();
    }

}