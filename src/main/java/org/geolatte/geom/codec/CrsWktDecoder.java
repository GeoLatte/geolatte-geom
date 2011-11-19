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
 * A decoder for Coordinate Reference System definitions in WKT.
 *
 * <p> The current implementation ensures that the postgis CRS WKT's are correctly interpreted. There are
 * some minor differences with the OGC specification: "Coordinate Transformation Services (rev. 1.00)". </p>
 *
 * <p>This decoder uses a recursive-decent parsing approach.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CrsWktDecoder extends AbstractWktDecoder<CoordinateReferenceSystem> {

    private final static CrsWktVariant CRS_TOKENS =  new CrsWktVariant();

    public CrsWktDecoder() {
        super(CRS_TOKENS);
    }

    /**
     * Decodes a WKT representation of a <code>CoordinateReferenceSystem</code>.
     * @param wkt the WKT string to decode
     * @return
     */
    public CoordinateReferenceSystem decode(String wkt){
        setTokenizer(new WktTokenizer(wkt, getWktVariant(), false));
        nextToken();
        return matchesCRS();
    }

    private CoordinateReferenceSystem matchesCRS() {
        if (currentToken == CrsWktVariant.PROJCS) {
            return matchesProjectedCRS();
        } else if (currentToken == CrsWktVariant.GEOGCS) {
            return matchesGeographicCRS();
        } else if (currentToken == CrsWktVariant.GEOCCS) {
            return matchesGeocentricCRS();
        }
        throw new WktParseException("Expected Wkt Token PROJCS, GEOGCS or GEOCCS");
    }

    //currently not used in Postgis
    private CoordinateReferenceSystem matchesGeocentricCRS() {
        throw new UnsupportedConversionException("Currently not implemented");
    }

    private GeographicCoordinateReferenceSystem matchesGeographicCRS() {
        String crsName = matchesName();
        matchesElemSeparator();
        GeodeticDatum datum = matchesDatum();
        matchesElemSeparator();
        PrimeMeridian primem = matchesPrimem();
        matchesElemSeparator();
        Unit unit = matchesUnit(Unit.Type.ANGULAR);
        CoordinateSystemAxis[] twinAxes = optionalMatchesTwinAxis(unit, GeographicCoordinateReferenceSystem.class);
        int srid = optionalMatchesAuthority();
        matchesCloseList();
        GeographicCoordinateReferenceSystem system = new GeographicCoordinateReferenceSystem(srid, crsName, twinAxes);
        system.setDatum(datum);
        system.setPrimeMeridian(primem);
        return system;
    }

     private CoordinateReferenceSystem matchesProjectedCRS() {
        String crsName = matchesName();
        matchesElemSeparator();
        GeographicCoordinateReferenceSystem geogcs = matchesGeographicCRS();
        matchesElemSeparator();
        Unit unit;
        Projection projection;
        List<CrsParameter> parameters;
        // spatial_reference.sql contains both variants of ProjCRS Wkt
        if (currentToken == CrsWktVariant.UNIT) {
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
        return new ProjectedCoordinateReferenceSystem(srid, crsName, geogcs, projection, parameters, twinAxes);
    }

    private List<CrsParameter> optionalMatchesParameters() {
        List<CrsParameter> parameters = new ArrayList<CrsParameter>();
        CrsParameter parameter = optionalMatchParameter();
        while (parameter != null) {
            parameters.add( parameter );
            parameter = optionalMatchParameter();
        }
        return parameters;
    }

    private CrsParameter optionalMatchParameter(){
        matchesElemSeparator();
        if (currentToken != CrsWktVariant.PARAMETER) {
            return null;
        }
        nextToken();
        String name = matchesName();
        matchesElemSeparator();
        double value = matchesNumber();
        matchesCloseList();
        return new CrsParameter(name, value);
    }

    private Projection matchesProjection(){
        matchesElemSeparator();
        if (currentToken != CrsWktVariant.PROJECTION) {
            throw new WktParseException("Expected PROJECTION keyword, found " + currentToken.toString());
        }
        String name = matchesName();
        int srid = optionalMatchesAuthority();
        matchesCloseList();
        return new Projection(srid, name);
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] optionalMatchesTwinAxis(Unit unit, Class<T> crsClass) {
        matchesElemSeparator();
        if (currentToken != CrsWktVariant.AXIS) {
            return defaultCRS(unit, crsClass);
        }
        CoordinateSystemAxis[] twinAxes = new CoordinateSystemAxis[2];
        twinAxes[0] = matchesAxis(unit);
        matchesElemSeparator();
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
        if (currentToken != CrsWktVariant.AXIS) {
            throw new WktParseException("Expected AXIS keyword, found " + currentToken.toString());
        }
        String name = matchesName();
        matchesElemSeparator();
        CoordinateSystemAxisDirection direction = CoordinateSystemAxisDirection.valueOf(currentToken.toString());
        nextToken();
        matchesCloseList();
        return new CoordinateSystemAxis(name, direction, unit);
    }

    private Unit matchesUnit(Unit.Type type) {
        if (currentToken != CrsWktVariant.UNIT) {
            throw new WktParseException("Expected UNIT keyword, found " + currentToken.toString());
        }
        String name = matchesName();
        matchesElemSeparator();
        double cf = matchesNumber();
        matchesElemSeparator();
        int srid = optionalMatchesAuthority();
        matchesCloseList();
        return new Unit(srid, name, type, cf);
    }

    private PrimeMeridian matchesPrimem() {
        if (currentToken != CrsWktVariant.PRIMEM) {
            throw new WktParseException("Expected PRIMEM keyword, received " + currentToken.toString());
        }
        String name = matchesName();
        matchesElemSeparator();
        double longitude = matchesNumber();
        int srid = optionalMatchesAuthority();
        matchesCloseList();
        return new PrimeMeridian(srid, name, longitude);
    }

    private GeodeticDatum matchesDatum() {
        if (currentToken != CrsWktVariant.DATUM) {
            throw new WktParseException("Expected DATUM token.");
        }
        String datumName = matchesName();
        matchesElemSeparator();
        Ellipsoid ellipsoid = matchesSpheroid();
        double[] toWGS84 = optionalMatchesToWGS84();
        int srid = optionalMatchesAuthority();
        matchesCloseList();
        return new GeodeticDatum(srid, ellipsoid, datumName, toWGS84);
    }

    private double[] optionalMatchesToWGS84() {
        matchesElemSeparator();
        if (currentToken != CrsWktVariant.TOWGS84)
            return new double[0];
        nextToken();
        double[] toWGS = new double[7];
        matchesOpenList();
        for (int i = 0; i < 7; i++) {
            toWGS[i] = matchesNumber();
            matchesElemSeparator();
        }
        matchesCloseList();
        return toWGS;
    }

    private Ellipsoid matchesSpheroid() {
        if (currentToken != CrsWktVariant.SPHEROID){
            throw new WktParseException("Expected SPHEROID keyword, but received " + currentToken.toString());
        }
        String ellipsoidName = matchesName();
        matchesElemSeparator();
        double semiMajorAxis = matchesNumber();
        matchesElemSeparator();
        double inverseFlattening = matchesNumber();
        int srid = optionalMatchesAuthority();
        matchesCloseList();
        return new Ellipsoid(srid, ellipsoidName,semiMajorAxis, inverseFlattening);
    }

    private int optionalMatchesAuthority() {
        matchesElemSeparator();
        if (currentToken != CrsWktVariant.AUTHORITY)
            return -1;

        nextToken();
        matchesOpenList();
        String authority = matchesText();
        matchesElemSeparator();
        String value = matchesText();
        matchesCloseList();
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
        matchesOpenList();
        return matchesText();
    }



}