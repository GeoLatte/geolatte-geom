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

import java.util.ArrayList;
import java.util.List;

/**
 * Decodes the WKT CRS definitions according to the Postgis
 *
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

    private GeographicCoordinateReferenceSystem matchesGeographicCRS() {
        String crsName = matchesName();
        matchesListDelimiter();
        GeodeticDatum datum = matchesDatum();
        matchesListDelimiter();
        PrimeMeridian primem = matchesPrimem();
        matchesListDelimiter();
        Unit unit = matchesUnit(Unit.Type.ANGULAR);
        CoordinateSystemAxis[] twinAxes = optionalMatchesTwinAxis(unit, GeographicCoordinateReferenceSystem.class);
        int srid = optionalMatchesAuthority();
        matchesEndList();
        GeographicCoordinateReferenceSystem system = new GeographicCoordinateReferenceSystem(srid, crsName, twinAxes);
        system.setDatum(datum);
        system.setPrimeMeridian(primem);
        return system;
    }

     private CoordinateReferenceSystem matchesProjectedCRS() {
        String crsName = matchesName();
        matchesListDelimiter();
        GeographicCoordinateReferenceSystem geogcs = matchesGeographicCRS();
        matchesListDelimiter();
        Unit unit = matchesUnit(Unit.Type.LINEAR);
        Projection projection = matchesProjection();
        List<CRSParameter> parameters = optionalMatchesParameters();
        int srid = optionalMatchesAuthority();
        CoordinateSystemAxis[] twinAxes = optionalMatchesTwinAxis(unit, ProjectedCoordinateReferenceSystem.class);
        ProjectedCoordinateReferenceSystem result = new ProjectedCoordinateReferenceSystem(srid, crsName, geogcs, projection, parameters, twinAxes);
        return result;
    }

    private List<CRSParameter> optionalMatchesParameters() {
        List<CRSParameter> parameters = new ArrayList<CRSParameter>();
        CRSParameter parameter = optionalMatchParameter();
        while (parameter != null) {
            parameters.add( parameter );
            parameter = optionalMatchParameter();
        }
        return parameters;
    }

    private CRSParameter optionalMatchParameter(){
        matchesListDelimiter();
        if (currentToken != CRSWKTToken.PARAMETER) {
            return null;
        }
        nextToken();
        String name = matchesName();
        matchesListDelimiter();
        double value = matchesNumber();
        matchesEndList();
        return new CRSParameter(name, value);
    }

    private Projection matchesProjection(){
        matchesListDelimiter();
        if (currentToken != CRSWKTToken.PROJECTION) {
            throw new WKTParseException("Expected PROJECTION keyword, found " + toString(currentToken));
        }
        String name = matchesName();
        int srid = optionalMatchesAuthority();
        matchesEndList();
        return new Projection(srid, name);
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] optionalMatchesTwinAxis(Unit unit, Class<T> crsClass) {
        matchesListDelimiter();
        if (currentToken != CRSWKTToken.AXIS) {
            return defaultCRS(unit, crsClass);
        }
        CoordinateSystemAxis[] twinAxes = new CoordinateSystemAxis[2];
        twinAxes[0] = matchesAxis(unit);
        matchesListDelimiter();
        twinAxes[1] = matchesAxis(unit);
        return twinAxes;
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] defaultCRS(Unit unit, Class<T> crsClass) {

        if (GeographicCoordinateReferenceSystem.class.isAssignableFrom(crsClass)){
            return new CoordinateSystemAxis[]{
                    new CoordinateSystemAxis("Lon", CoordinateSystemAxisDirection.EAST,unit),
                    new CoordinateSystemAxis("Lat", CoordinateSystemAxisDirection.NORTH, unit)
            };
        }

        if (ProjectedCoordinateReferenceSystem.class.isAssignableFrom(crsClass)){
            return new CoordinateSystemAxis[]{
                    new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.EAST, unit),
                    new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.NORTH, unit)
            };
        }

        if (GeocentricCoordinateReferenceSystem.class.isAssignableFrom(crsClass)){
            return new CoordinateSystemAxis[]{
                    new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.GeocentricX,unit),
                    new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.GeocentricY, unit),
                    new CoordinateSystemAxis("Z", CoordinateSystemAxisDirection.GeocentricZ, unit)
            };
        }

        throw new IllegalStateException("Can't create default for CRS of type " + crsClass.getCanonicalName());

    }

    private CoordinateSystemAxis matchesAxis(Unit unit) {
        if (currentToken != CRSWKTToken.AXIS) {
            throw new WKTParseException("Expected AXIS keyword, found " + toString(currentToken));
        }
        String name = matchesName();
        matchesListDelimiter();
        CoordinateSystemAxisDirection direction = CoordinateSystemAxisDirection.valueOf(currentToken.toString());
        nextToken();
        matchesEndList();
        return new CoordinateSystemAxis(name, direction, unit);
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
        matchesEndList();
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

    private GeodeticDatum matchesDatum() {
        if (currentToken != CRSWKTToken.DATUM) {
            throw new WKTParseException("Expected DATUM token.");
        }
        String datumName = matchesName();
        matchesListDelimiter();
        Ellipsoid ellipsoid = matchesSpheroid();
        double[] toWGS84 = optionalMatchesToWGS84();
        int srid = optionalMatchesAuthority();
        matchesEndList();
        return new GeodeticDatum(srid, ellipsoid, datumName, toWGS84);
    }

    private double[] optionalMatchesToWGS84() {
        matchesListDelimiter();
        if (currentToken != CRSWKTToken.TOWGS84)
            return new double[0];
        nextToken();
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
            return token.toString();
        }
        return token.getClass().getSimpleName();
    }

}