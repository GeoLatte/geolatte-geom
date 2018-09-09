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
        } else if (currentToken == CrsWktVariant.COMPD_CS) {
            return decodeCompoundCrs();
        } else if (currentToken == CrsWktVariant.VERT_CS) {
            return decodeVertCS();
        }
        throw new WktDecodeException("Expected Wkt Token PROJCS, GEOGCS, GEOCCS or COMPD_CS. Received  " + currentToken);
    }

    /**
e     * Currently not used in Postgis and also not implemented here!
     *
     * @throws UnsupportedConversionException Geocentric CRS is currently not implemented
     */
    private GeocentricCartesianCoordinateReferenceSystem decodeGeocentricCrs() {
        String crsName = decodeName();
        matchesElementSeparator();
        Datum datum = decodeDatum();
        matchesElementSeparator();
        PrimeMeridian primem = decodePrimem();
        matchesElementSeparator();
        Unit unit = decodeUnit(true);
        CoordinateSystemAxis[] axes = decodeOptionalAxes(3, unit, GeocentricCartesianCoordinateReferenceSystem.class);
        CrsId cr = decodeOptionalAuthority(srid);
        matchesCloseList();
        GeocentricCartesianCoordinateReferenceSystem system = new GeocentricCartesianCoordinateReferenceSystem(cr, crsName,
                datum, primem,
                new CartesianCoordinateSystem3D((StraightLineAxis)axes[0], (StraightLineAxis)axes[1], (VerticalStraightLineAxis) axes[2])
        );
        return system;
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
        CoordinateSystemAxis[] twinAxes = decodeOptionalAxes(2, unit, Geographic2DCoordinateReferenceSystem.class);
        CrsId cr = decodeOptionalAuthority(srid);
        matchesCloseList();
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
        CoordinateSystemAxis[] twinAxes = decodeOptionalAxes(2, unit, ProjectedCoordinateReferenceSystem.class);
        Extension extension = decodeOptionalExtension();
        CrsId crsId = decodeOptionalAuthority(srid);
        matchesCloseList();
        return new ProjectedCoordinateReferenceSystem(crsId, crsName, geogcs, projection, parameters,
                new CartesianCoordinateSystem2D((StraightLineAxis)twinAxes[0], (StraightLineAxis)twinAxes[1]), extension);
    }

    private <P extends Position> CompoundCoordinateReferenceSystem<P> decodeCompoundCrs() {
        String crsName = decodeName();
        matchesElementSeparator();
        SingleCoordinateReferenceSystem<?> head = (SingleCoordinateReferenceSystem<?>)decode();
        matchesElementSeparator();
        SingleCoordinateReferenceSystem<?> tail = (SingleCoordinateReferenceSystem<?>)decode();
        CrsId cr = decodeOptionalAuthority(srid);
        return new CompoundCoordinateReferenceSystem<P>(cr, crsName, head, tail);
    }

    private VerticalCoordinateReferenceSystem decodeVertCS() {
        String crsName = decodeName();
        matchesElementSeparator();
        VerticalDatum vdatum = decodeVertDatum();
        matchesElementSeparator();
        LinearUnit unit =(LinearUnit) decodeUnit(true);
        matchesElementSeparator();
        VerticalStraightLineAxis axis = (VerticalStraightLineAxis)decodeAxis(unit, VerticalCoordinateReferenceSystem.class);
        CrsId id = decodeOptionalAuthority();
        return new VerticalCoordinateReferenceSystem(id, crsName, vdatum, axis);
    }

    private VerticalDatum decodeVertDatum() {
        if (currentToken != CrsWktVariant.VERT_DATUM) {
            throw new WktDecodeException("Expected VERT_DATUM keyword, found " + currentToken.toString());
        }
        String name = decodeName();
        matchesElementSeparator();
        int type = decodeInt();
        Extension extension = decodeOptionalExtension();
        CrsId authority = decodeOptionalAuthority(srid);
        matchesCloseList();
        return new VerticalDatum(authority, name, type, extension);
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

    private <T extends CoordinateReferenceSystem> CoordinateSystemAxis[] decodeOptionalAxes(int num, Unit unit, Class<T> crsClass) {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.AXIS) {
            return defaultCRS(unit, crsClass);
        }
        CoordinateSystemAxis[] axes = new CoordinateSystemAxis[num];

        for (int i = 0; i < num; i++) {
            if (i > 0) matchesElementSeparator();
            axes[i] = decodeAxis(unit, crsClass);
        }
        return axes;
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
                    return new StraightLineAxis(name, direction, 0, unit);
                } else {
                    return new StraightLineAxis(name, direction, 1, unit);
                }

            }
            return new StraightLineAxis(name, direction, (LinearUnit) unit);
        }

        //here we normalize based on the name, because usage of direction in Postgis can't be relied on
        if (GeocentricCartesianCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            String normalizedName = name.toUpperCase();
            if (normalizedName.equalsIgnoreCase("GEOCENTRIC X")) {
                return new StraightLineAxis(name, CoordinateSystemAxisDirection.GeocentricX, (LinearUnit) unit);
            } else if (normalizedName.equalsIgnoreCase("GEOCENTRIC Y")) {
                return new StraightLineAxis(name, CoordinateSystemAxisDirection.GeocentricY, (LinearUnit) unit);
            } else {
                return new VerticalStraightLineAxis(name, CoordinateSystemAxisDirection.GeocentricZ, (LinearUnit) unit);
            }
        }

        if (VerticalCoordinateReferenceSystem.class.isAssignableFrom(crsClass)) {
            return new VerticalStraightLineAxis(name, direction, (LinearUnit) unit);
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
        for (int i = 0; i < 7; i++) { //TODO -- what with 3-parameter variants?
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

    private CrsId decodeOptionalAuthority() {
        return decodeOptionalAuthority(CrsId.UNDEFINED.getCode());
    }

    private Extension decodeOptionalExtension(){
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.EXTENSION) {
            return null;
        }
        nextToken();
        matchesOpenList();
        String prop = decodeText();
        matchesElementSeparator();
        String val = decodeText();
        matchesCloseList();
        return new Extension(prop, val);
    }

    private CrsId decodeOptionalAuthority(int srid) {
        matchesElementSeparator();
        if (currentToken != CrsWktVariant.AUTHORITY) {
            return new CrsId("EPSG", srid);
        }
        nextToken();
        matchesOpenList();
        String authority = decodeText();
        matchesElementSeparator();
        int value = decodeInt();
        matchesCloseList();
        return new CrsId(authority, value);
    }

    private String decodeName() {
        nextToken();
        matchesOpenList();
        return decodeText();
    }


}