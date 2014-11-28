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

import org.geolatte.geom.P3D;
import org.geolatte.geom.Position;
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

    private int srid = 0;

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
    public CoordinateReferenceSystem<? extends Position> decode(String wkt, int srid) {
        this.srid = srid;
        setTokenizer(new CrsWktTokenizer(wkt, getWktVariant()));
        nextToken();
        return decode();
    }

    /**
     * Determines the WKT variant and calls the according method to decode the WKT.
     *
     * @return The decoded WKT as a <code>CoordinateReferenceSystem</code> object.
     */
    private CoordinateReferenceSystem<? extends Position> decode() {
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
    private CoordinateReferenceSystem<P3D> decodeGeocentricCrs() {
        throw new UnsupportedConversionException("Currently not implemented");
    }

    /**
     * Implementation to decode a Geographic CRS.
     *
     * @return The <code>GeographicCoordinateReferenceSystem</code> that is decoded from the WKT.
     */
    private Geographic2DCoordinateReferenceSystem decodeGeographicCrs() {
        String crsName = decodeName();
        matchesElementSeparator();
        Datum datum = decodeDatum();
        matchesElementSeparator();
        PrimeMeridian primem = decodePrimem();
        matchesElementSeparator();
        Unit unit = decodeUnit(false);
        CoordinateSystemAxis[] twinAxes = decodeOptionalTwinAxis(unit, Geographic2DCoordinateReferenceSystem.class);
        CrsId cr = decodeOptionalAuthority(srid);
        matchesCloseList();
        //TODO clean up casting.
        Geographic2DCoordinateReferenceSystem system = new Geographic2DCoordinateReferenceSystem(cr, crsName, new
                EllipsoidalCoordinateSystem2D((EllipsoidalAxis) twinAxes[0], (EllipsoidalAxis) twinAxes[1]));
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
        Geographic2DCoordinateReferenceSystem geogcs = decodeGeographicCrs();
        matchesElementSeparator();
        Unit unit;
        Projection projection;
        List<CrsParameter> parameters;
        // spatial_reference.sql contains both variants of ProjCRS Wkt
        if (currentToken == CrsWktVariant.UNIT) {
            unit = decodeUnit(true);
            projection = decodeProjection();
            parameters = decodeOptionalParameters();
        } else {
            projection = decodeProjection();
            parameters = decodeOptionalParameters();
            unit = decodeUnit(true);
        }
        CrsId crsId = decodeOptionalAuthority(srid);
        CoordinateSystemAxis[] twinAxes = decodeOptionalTwinAxis(unit, ProjectedCoordinateReferenceSystem.class);
        return new ProjectedCoordinateReferenceSystem(crsId, crsName, geogcs, projection, parameters,
                new CartesianCoordinateSystem2D((StraightLineAxis)twinAxes[0], (StraightLineAxis)twinAxes[1]));
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
        CrsId crsId = decodeOptionalAuthority(CrsId.UNDEFINED.getCode());
        matchesCloseList();
        return new Projection(crsId, name);
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] decodeOptionalTwinAxis(Unit unit, Class<T> crsClass) {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.AXIS) {
            return defaultCRS(unit, crsClass);
        }
        CoordinateSystemAxis[] twinAxes = new CoordinateSystemAxis[2];
        twinAxes[0] = decodeAxis(unit, crsClass);
        matchesElementSeparator();
        twinAxes[1] = decodeAxis(unit, crsClass);
        return twinAxes;
    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] defaultCRS(Unit unit, Class<T> crsClass) {

        if (Geographic2DCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new CoordinateSystemAxis[]{
                    new GeodeticLongitudeCSAxis("Lon", (AngularUnit)unit),
                    new GeodeticLatitudeCSAxis("Lat", (AngularUnit)unit)
            };
        }

        if (ProjectedCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new CoordinateSystemAxis[]{
                    new StraightLineAxis("X", CoordinateSystemAxisDirection.EAST, (LinearUnit)unit),
                    new StraightLineAxis("Y", CoordinateSystemAxisDirection.NORTH, (LinearUnit)unit)
            };
        }

        if (GeocentricCartesianCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new CoordinateSystemAxis[]{
                    new StraightLineAxis("X", CoordinateSystemAxisDirection.GeocentricX, (LinearUnit)unit),
                    new StraightLineAxis("Y", CoordinateSystemAxisDirection.GeocentricY, (LinearUnit)unit),
                    new StraightLineAxis("Z", CoordinateSystemAxisDirection.GeocentricZ, (LinearUnit)unit)
            };
        }

        throw new IllegalStateException("Can't create default for CrsRegistry of type " + crsClass.getCanonicalName());

    }

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis decodeAxis(Unit unit, Class<T> crsClass) {
        if (currentToken != CrsWktVariant.AXIS) {
            throw new WktDecodeException("Expected AXIS keyword, found " + currentToken.toString());
        }
        String name = decodeName();
        matchesElementSeparator();
        CoordinateSystemAxisDirection direction = CoordinateSystemAxisDirection.valueOf(currentToken.toString());
        nextToken();
        matchesCloseList();

        if (Geographic2DCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            if (direction.equals(CoordinateSystemAxisDirection.NORTH)) {
                return new GeodeticLatitudeCSAxis(name, (AngularUnit) unit);
            } else if (direction.equals(CoordinateSystemAxisDirection.EAST)) {
                return new GeodeticLongitudeCSAxis(name, (AngularUnit) unit);
            } else {
                throw new IllegalStateException("Axis in horizontal Geographic coordinate system is neither latitude," +
                        " nor longitude");
            }
        }

        if (ProjectedCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            //this fixes problems with some polar projection systems.
            if (direction == CoordinateSystemAxisDirection.UNKNOWN) {
                if (name.equalsIgnoreCase("X") || name.equalsIgnoreCase("Easting")) {
                    return new StraightLineAxis(name, direction, 0, (LinearUnit) unit);
                } else {
                    return new StraightLineAxis(name, direction, 1, (LinearUnit) unit);
                }

            }
            return new StraightLineAxis(name, direction, (LinearUnit) unit);
        }

        if (GeocentricCartesianCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new StraightLineAxis(name, direction, (LinearUnit)unit);
        }

        throw new IllegalStateException("Can't create default for CrsRegistry of type " + crsClass.getCanonicalName());

    }

    private Unit decodeUnit(boolean isLinear) {
        if (currentToken != CrsWktVariant.UNIT) {
            throw new WktDecodeException("Expected UNIT keyword, found " + currentToken.toString());
        }
        String name = decodeName();
        matchesElementSeparator();
        double cf = decodeNumber();
        matchesElementSeparator();
        CrsId crsId = decodeOptionalAuthority(CrsId.UNDEFINED.getCode());
        matchesCloseList();
        return isLinear ? new LinearUnit(crsId, name, cf) : new AngularUnit(crsId, name, cf);
    }

    private PrimeMeridian decodePrimem() {
        if (currentToken != CrsWktVariant.PRIMEM) {
            throw new WktDecodeException("Expected PRIMEM keyword, received " + currentToken.toString());
        }
        String name = decodeName();
        matchesElementSeparator();
        double longitude = decodeNumber();
        CrsId crsId = decodeOptionalAuthority(CrsId.UNDEFINED.getCode());
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
        CrsId crsId = decodeOptionalAuthority(CrsId.UNDEFINED.getCode());
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
        CrsId crsId = decodeOptionalAuthority(CrsId.UNDEFINED.getCode());
        matchesCloseList();
        return new Ellipsoid(crsId, ellipsoidName, semiMajorAxis, inverseFlattening);
    }

    private CrsId decodeOptionalAuthority(int srid) {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.AUTHORITY) {
            return CrsId.valueOf(srid);
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
        return CrsId.valueOf(srid);
    }

    private String decodeName() {
        nextToken();
        matchesOpenList();
        return decodeText();
    }


}