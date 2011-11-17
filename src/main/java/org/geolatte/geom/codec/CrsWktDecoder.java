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

//TODO -- add OGC specification reference.
/**
 * A decoder for Coordinate Reference System definitions in WKT.
 *
 * <p> The current implementation ensures that the postgis CRS WKT's are correctly interpreted. There are therefore
 * some minor differences with the OGC specification. </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CrsWktDecoder {

    private WktToken currentToken;
    private WktTokenizer tokenizer;


    /**
     * Decodes a WKT representation of a coordinate Reference system.
     *
     * @param wkt
     * @return
     */
    public CoordinateReferenceSystem decode(String wkt) {
        tokenizer = new WktTokenizer(wkt, new CrsWktWordMatcher(), '[', ']', false);
        nextToken();
        return matchesCRS();
    }

    private CoordinateReferenceSystem matchesCRS() {
        if (currentToken == CrsWktToken.PROJCS) {
            return matchesProjectedCRS();
        } else if (currentToken == CrsWktToken.GEOGCS) {
            return matchesGeographicCRS();
        } else if (currentToken == CrsWktToken.GEOCCS) {
            return matchesGeocentricCRS();
        }
        throw new WktParseException("Expected Wkt Token PROJCS, GEOGCS or GEOCCS");
    }

    private CoordinateReferenceSystem matchesGeocentricCRS() {
        return null;   //TODO -- add implementation?
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
        Unit unit;
        Projection projection;
        List<CRSParameter> parameters;
        // spatial_reference.sql contains both variants of ProjCRS Wkt
        if (currentToken == CrsWktToken.UNIT) {
            unit = matchesUnit(Unit.Type.LINEAR);
            projection = matchesProjection();
            parameters = optionalMatchesParameters();
        }  else {
            projection = matchesProjection();
            parameters = optionalMatchesParameters();
            unit = matchesUnit(Unit.Type.LINEAR);
        }
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
        if (currentToken != CrsWktToken.PARAMETER) {
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
        if (currentToken != CrsWktToken.PROJECTION) {
            throw new WktParseException("Expected PROJECTION keyword, found " + toString(currentToken));
        }
        String name = matchesName();
        int srid = optionalMatchesAuthority();
        matchesEndList();
        return new Projection(srid, name);
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] optionalMatchesTwinAxis(Unit unit, Class<T> crsClass) {
        matchesListDelimiter();
        if (currentToken != CrsWktToken.AXIS) {
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

        throw new IllegalStateException("Can't create default for CrsRegistry of type " + crsClass.getCanonicalName());

    }

    private CoordinateSystemAxis matchesAxis(Unit unit) {
        if (currentToken != CrsWktToken.AXIS) {
            throw new WktParseException("Expected AXIS keyword, found " + toString(currentToken));
        }
        String name = matchesName();
        matchesListDelimiter();
        CoordinateSystemAxisDirection direction = CoordinateSystemAxisDirection.valueOf(currentToken.toString());
        nextToken();
        matchesEndList();
        return new CoordinateSystemAxis(name, direction, unit);
    }

    private Unit matchesUnit(Unit.Type type) {
        if (currentToken != CrsWktToken.UNIT) {
            throw new WktParseException("Expected UNIT keyword, found " + toString(currentToken));
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
        if (currentToken != CrsWktToken.PRIMEM) {
            throw new WktParseException("Expected PRIMEM keyword, received " + toString(currentToken));
        }
        String name = matchesName();
        matchesListDelimiter();
        double longitude = matchesNumber();
        int srid = optionalMatchesAuthority();
        matchesEndList();
        return new PrimeMeridian(srid, name, longitude);
    }

    private GeodeticDatum matchesDatum() {
        if (currentToken != CrsWktToken.DATUM) {
            throw new WktParseException("Expected DATUM token.");
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
        if (currentToken != CrsWktToken.TOWGS84)
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
        if (currentToken != CrsWktToken.SPHEROID){
            throw new WktParseException("Expected SPHEROID keyword, but received " + toString(currentToken));
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
        if (currentToken != CrsWktToken.AUTHORITY)
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
                throw new WktParseException("Expected EPSG integer code, received " + value);
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
        if (currentToken instanceof WktToken.TextToken) {
            String text = ((WktToken.TextToken) currentToken).getText();
            nextToken();
            return text;
        }
        throw new WktParseException("Expected text token, received " + toString(currentToken));
    }


    //TODO -- these methods are shared with other decoders, move these abstract WKTDecoder class
    private void nextToken() {
        currentToken = tokenizer.nextToken();
    }

    private boolean matchesStartList() {
        if (currentToken instanceof WktToken.StartList) {
            nextToken();
            return true;
        }
        return false;
    }

    private boolean matchesEndList() {
        if (currentToken instanceof WktToken.EndList) {
            nextToken();
            return true;
        }
        return false;
    }

    private boolean matchesListDelimiter() {
        if (currentToken instanceof WktToken.ElementSeparator) {
            nextToken();
            return true;
        }
        return false;
    }

    private double matchesNumber(){
        if (currentToken instanceof WktToken.NumberToken) {
            double value = ((WktToken.NumberToken)currentToken).getNumber();
            nextToken();
            return value;
        }
        throw new WktParseException("Expected a number ; received " + toString(currentToken));
    }


    private String toString(WktToken token){
        if (token instanceof CrsWktToken) {
            return token.toString();
        }
        return token.getClass().getSimpleName();
    }

}