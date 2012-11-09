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
 * A decoder for <code>CoordinateReferenceSystem</code> definitions in WKT.
 * <p/>
 * <p> The current implementation ensures that the postgis CRS WKT's are correctly interpreted. There are
 * some minor differences with the OGC specification: "Coordinate Transformation Services (rev. 1.00)". </p>
 * <p/>
 * <p>The implementation uses a recursive-decent parsing approach.</p>
 * <p/>
 * <p>This class is not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class CrsWktDecoder extends AbstractWktDecoder<CoordinateReferenceSystem> {

    private final static CrsWktVariant CRS_TOKENS = new CrsWktVariant();

    /**
     * Initiates a new <code>CrsWktDecoder</code> that uses the <code>CrsWktVariant</code>.
     */
    public CrsWktDecoder() {
        super(CRS_TOKENS);
    }

    /**
     * Decodes a WKT representation of a <code>CoordinateReferenceSystem</code>.
     *
     * @param wkt the WKT string to decode
     * @return The <code>CoordinateReferenceSystem</code> that is encoded in the input WKT.
     */
    public CoordinateReferenceSystem decode(String wkt) {
        setTokenizer(new CrsWktTokenizer(wkt, getWktVariant()));
        nextToken();
        return decode();
    }

    /**
     * Determines the WKT variant and calls the according method to decode the WKT.
     *
     * @return The decoded WKT as a <code>CoordinateReferenceSystem</code> object.
     */
    private CoordinateReferenceSystem decode() {
        if (currentToken == CrsWktVariant.PROJCS) {
            return decodeProjectedCrs();
        } else if (currentToken == CrsWktVariant.GEOGCS) {
            return decodeGeographicCrs();
        } else if (currentToken == CrsWktVariant.GEOCCS) {
            return decodeGeocentricCrs();
        }
        throw new WktDecodeException("Expected Wkt Token PROJCS, GEOGCS or GEOCCS");
    }

    /**
     * Implementation to decode Geocentric CRS.
     * Currently not used in Postgis and also not implemented here!
     *
     * @throws UnsupportedConversionException Geocentric CRS is currently not implemented
     */
    private CoordinateReferenceSystem decodeGeocentricCrs() {
        throw new UnsupportedConversionException("Currently not implemented");
    }

    /**
     * Implementation to decode a Geographic CRS.
     *
     * @return The <code>GeographicCoordinateReferenceSystem</code> that is decoded from the WKT.
     */
    private GeographicCoordinateReferenceSystem decodeGeographicCrs() {
        String crsName = decodeName();
        matchesElementSeparator();
        Datum datum = decodeDatum();
        matchesElementSeparator();
        PrimeMeridian primem = decodePrimem();
        matchesElementSeparator();
        Unit unit = decodeUnit(Unit.Type.ANGULAR);
        CoordinateSystemAxis[] twinAxes = decodeOptionalTwinAxis(unit, GeographicCoordinateReferenceSystem.class);
        CrsId cr = decodeOptionalAuthority();
        matchesCloseList();
        GeographicCoordinateReferenceSystem system = new GeographicCoordinateReferenceSystem(cr, crsName, twinAxes);
        system.setDatum(datum);
        system.setPrimeMeridian(primem);
        return system;
    }

    /**
     * Implementation to decode a Projected CRS.
     *
     * @return The <code>ProjectedCoordinateReferenceSystem</code> that is decoded from the WKT.
     */
    private ProjectedCoordinateReferenceSystem decodeProjectedCrs() {
        String crsName = decodeName();
        matchesElementSeparator();
        GeographicCoordinateReferenceSystem geogcs = decodeGeographicCrs();
        matchesElementSeparator();
        Unit unit;
        Projection projection;
        List<CrsParameter> parameters;
        // spatial_reference.sql contains both variants of ProjCRS Wkt
        if (currentToken == CrsWktVariant.UNIT) {
            unit = decodeUnit(Unit.Type.LINEAR);
            projection = decodeProjection();
            parameters = decodeOptionalParameters();
        } else {
            projection = decodeProjection();
            parameters = decodeOptionalParameters();
            unit = decodeUnit(Unit.Type.LINEAR);
        }
        CrsId crsId = decodeOptionalAuthority();
        CoordinateSystemAxis[] twinAxes = decodeOptionalTwinAxis(unit, ProjectedCoordinateReferenceSystem.class);
        return new ProjectedCoordinateReferenceSystem(crsId, crsName, geogcs, projection, parameters, twinAxes);
    }

    private List<CrsParameter> decodeOptionalParameters() {
        List<CrsParameter> parameters = new ArrayList<CrsParameter>();
        CrsParameter parameter = decodeOptionalParameter();
        while (parameter != null) {
            parameters.add(parameter);
            parameter = decodeOptionalParameter();
        }
        return parameters;
    }

    private CrsParameter decodeOptionalParameter() {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.PARAMETER) {
            return null;
        }
        nextToken();
        String name = decodeName();
        matchesElementSeparator();
        double value = decodeNumber();
        matchesCloseList();
        return new CrsParameter(name, value);
    }

    private Projection decodeProjection() {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.PROJECTION) {
            throw new WktDecodeException("Expected PROJECTION keyword, found " + currentToken.toString());
        }
        String name = decodeName();
        CrsId crsId = decodeOptionalAuthority();
        matchesCloseList();
        return new Projection(crsId, name);
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] decodeOptionalTwinAxis(Unit unit, Class<T> crsClass) {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.AXIS) {
            return defaultCRS(unit, crsClass);
        }
        CoordinateSystemAxis[] twinAxes = new CoordinateSystemAxis[2];
        twinAxes[0] = decodeAxis(unit);
        matchesElementSeparator();
        twinAxes[1] = decodeAxis(unit);
        return twinAxes;
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] defaultCRS(Unit unit, Class<T> crsClass) {

        if (GeographicCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new CoordinateSystemAxis[]{
                    new CoordinateSystemAxis("Lon", CoordinateSystemAxisDirection.EAST, unit),
                    new CoordinateSystemAxis("Lat", CoordinateSystemAxisDirection.NORTH, unit)
            };
        }

        if (ProjectedCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new CoordinateSystemAxis[]{
                    new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.EAST, unit),
                    new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.NORTH, unit)
            };
        }

        if (GeocentricCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new CoordinateSystemAxis[]{
                    new CoordinateSystemAxis("X", CoordinateSystemAxisDirection.GeocentricX, unit),
                    new CoordinateSystemAxis("Y", CoordinateSystemAxisDirection.GeocentricY, unit),
                    new CoordinateSystemAxis("Z", CoordinateSystemAxisDirection.GeocentricZ, unit)
            };
        }

        throw new IllegalStateException("Can't create default for CrsRegistry of type " + crsClass.getCanonicalName());

    }

    private CoordinateSystemAxis decodeAxis(Unit unit) {
        if (currentToken != CrsWktVariant.AXIS) {
            throw new WktDecodeException("Expected AXIS keyword, found " + currentToken.toString());
        }
        String name = decodeName();
        matchesElementSeparator();
        CoordinateSystemAxisDirection direction = CoordinateSystemAxisDirection.valueOf(currentToken.toString());
        nextToken();
        matchesCloseList();
        return new CoordinateSystemAxis(name, direction, unit);
    }

    private Unit decodeUnit(Unit.Type type) {
        if (currentToken != CrsWktVariant.UNIT) {
            throw new WktDecodeException("Expected UNIT keyword, found " + currentToken.toString());
        }
        String name = decodeName();
        matchesElementSeparator();
        double cf = decodeNumber();
        matchesElementSeparator();
        CrsId crsId = decodeOptionalAuthority();
        matchesCloseList();
        return new Unit(crsId, name, type, cf);
    }

    private PrimeMeridian decodePrimem() {
        if (currentToken != CrsWktVariant.PRIMEM) {
            throw new WktDecodeException("Expected PRIMEM keyword, received " + currentToken.toString());
        }
        String name = decodeName();
        matchesElementSeparator();
        double longitude = decodeNumber();
        CrsId crsId = decodeOptionalAuthority();
        matchesCloseList();
        return new PrimeMeridian(crsId, name, longitude);
    }

    private Datum decodeDatum() {
        if (currentToken != CrsWktVariant.DATUM) {
            throw new WktDecodeException("Expected DATUM token.");
        }
        String datumName = decodeName();
        matchesElementSeparator();
        Ellipsoid ellipsoid = decodeSpheroid();
        double[] toWGS84 = decodeOptionalToWGS84();
        CrsId crsId = decodeOptionalAuthority();
        matchesCloseList();
        return new Datum(crsId, ellipsoid, datumName, toWGS84);
    }

    private double[] decodeOptionalToWGS84() {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.TOWGS84) {
            return new double[0];
        }
        nextToken();
        double[] toWGS = new double[7];
        matchesOpenList();
        for (int i = 0; i < 7; i++) {
            toWGS[i] = decodeNumber();
            matchesElementSeparator();
        }
        matchesCloseList();
        return toWGS;
    }

    private Ellipsoid decodeSpheroid() {
        if (currentToken != CrsWktVariant.SPHEROID) {
            throw new WktDecodeException("Expected SPHEROID keyword, but received " + currentToken.toString());
        }
        String ellipsoidName = decodeName();
        matchesElementSeparator();
        double semiMajorAxis = decodeNumber();
        matchesElementSeparator();
        double inverseFlattening = decodeNumber();
        CrsId crsId = decodeOptionalAuthority();
        matchesCloseList();
        return new Ellipsoid(crsId, ellipsoidName, semiMajorAxis, inverseFlattening);
    }

    private CrsId decodeOptionalAuthority() {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.AUTHORITY) {
            return CrsId.UNDEFINED;
        }

        nextToken();
        matchesOpenList();
        String authority = decodeText();
        matchesElementSeparator();
        String value = decodeText();
        matchesCloseList();
        if (authority.equals("EPSG")) {
            try {
                return new CrsId("EPSG", Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new WktDecodeException("Expected EPSG integer code, received " + value);
            }
        }
        return CrsId.UNDEFINED;
    }

    private String decodeName() {
        nextToken();
        matchesOpenList();
        return decodeText();
    }


}