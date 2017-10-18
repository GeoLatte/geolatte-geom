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

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.CrsRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.geolatte.geom.Geometries.*;

/**
 * A decoder for the Postgis WKT/EWKT representations as used in Postgis (at least 1.0 to 1.5+).
 * <p/>
 * <p>This class is not thread-safe</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PostgisWktDecoder extends AbstractWktDecoder<Geometry<?>> implements WktDecoder {

    private final static PostgisWktVariant WKT_GEOM_TOKENS = new PostgisWktVariant();
    private final static Pattern SRID_RE = Pattern.compile("^SRID=(.*);", Pattern.CASE_INSENSITIVE);

    private String wktString;
    private CoordinateReferenceSystem<?> crs;

    public PostgisWktDecoder() {
        super(WKT_GEOM_TOKENS);
    }
    
    protected PostgisWktDecoder(WktVariant wktVariant) {
        super(wktVariant);
    }


    public Geometry<?> decode(String wkt) {
        return decode(wkt, (CoordinateReferenceSystem<?>)null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Position> Geometry<P> decode(String wkt, CoordinateReferenceSystem<P> crs) {
        if (wkt == null || wkt.isEmpty()) {
            throw new WktDecodeException("Null or empty string cannot be decoded into a geometry");
        }
        prepare(wkt, crs);
        initializeTokenizer(crs != null);
        return (Geometry<P>)decodeGeometry(this.crs);
    }

    /**
     * The instance fields wktString and crsId are initialized prior to decoding. For postgis EWKT that entails
     * extracting the SRID prefix (if any) from the WKT string.
     *
     * @param wkt the WKT representation
     */
    private <P extends Position> void prepare(String wkt, CoordinateReferenceSystem<P> crs) {
        Matcher matcher = SRID_RE.matcher(wkt);
        if (matcher.find()) {
            int srid = Integer.parseInt(matcher.group(1));
            this.crs = crs != null? crs : CrsRegistry.getCoordinateReferenceSystemForEPSG(srid, CoordinateReferenceSystems.PROJECTED_2D_METER);
            wktString = wkt.substring(matcher.end());
        } else {
            this.crs = crs != null ? crs : CoordinateReferenceSystems.PROJECTED_2D_METER;
            wktString = wkt;
        }
    }

    private void initializeTokenizer(boolean forceToCRS) {
        setTokenizer(new WktTokenizer(wktString, getWktVariant(), crs, forceToCRS));
        nextToken();
    }

    private <P extends Position> Geometry<P> decodeGeometry(CoordinateReferenceSystem<P> crs) {
        if (!(currentToken instanceof WktGeometryToken)) {
            throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
        }
        GeometryType type = ((WktGeometryToken) currentToken).getType();
        nextToken();
        switch (type) {
            case POINT:
                return decodePointText(crs);
            case LINESTRING:
                return decodeLineStringText(crs);
            case POLYGON:
                return decodePolygonText(crs);
            case GEOMETRYCOLLECTION:
                return decodeGeometryCollection(crs);
            case MULTIPOINT:
                return decodeMultiPoint(crs);
            case MULTILINESTRING:
                return decodeMultiLineString(crs);
            case MULTIPOLYGON:
                return decodeMultiPolygon(crs);
        }
        throw new WktDecodeException("Unsupported geometry type in Wkt: " + type);
    }

    private <P extends Position> MultiPolygon<P> decodeMultiPolygon(CoordinateReferenceSystem<P> crs) {
        if (matchesOpenList()) {
            List<Polygon<P>> polygons = new ArrayList<Polygon<P>>();
            while (!matchesCloseList()) {
                polygons.add(decodePolygonText(crs));
                matchesElementSeparator();
            }
            return mkMultiPolygon(polygons);
        }
        if (matchesEmptyToken()) {
            return new MultiPolygon<P>(crs);
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private <P extends Position> Geometry<P> decodeMultiLineString(CoordinateReferenceSystem<P> crs) {
        if (matchesOpenList()) {
            List<LineString<P>> lineStrings = new ArrayList<LineString<P>>();
            while (!matchesCloseList()) {
                lineStrings.add(decodeLineStringText(crs));
                matchesElementSeparator();
            }
            return mkMultiLineString(lineStrings);
        }
        if (matchesEmptyToken()) {
            return new MultiLineString<P>(crs);
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private <P extends Position> Geometry<P> decodeMultiPoint(CoordinateReferenceSystem<P> crs) {
        if (matchesOpenList()) {
            List<Point<P>> points = new ArrayList<Point<P>>();
            //this handles the case of the non-compliant MultiPoints in Postgis (e.g.
            // MULTIPOINT(10 10, 12 13) rather than MULTIPOINT((10 20), (30 40))
            if (currentToken instanceof WktPointSequenceToken) {
                PositionSequence<P> positionSequence = ((WktPointSequenceToken<P>) currentToken).getPositions();
                CoordinateReferenceSystem<P> tcrs = ((WktPointSequenceToken<P>) currentToken).getCoordinateReferenceSystem();
                for (P p : positionSequence) {
                    points.add(new Point<P>(p, tcrs));
                }
                nextToken();
            }
            while (!matchesCloseList()) {
                points.add(decodePointText(crs));
                matchesElementSeparator();
            }
            return mkMultiPoint(points);
        }
        if (matchesEmptyToken()) {
            return new MultiPoint<P>(crs);
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private <P extends Position> GeometryCollection<P, Geometry<P>> decodeGeometryCollection(CoordinateReferenceSystem<P> crs) {
        if (matchesOpenList()) {
            List<Geometry<P>> geometries = new ArrayList<Geometry<P>>();
            while (!matchesCloseList()) {
                geometries.add(decodeGeometry(crs));
                matchesElementSeparator();
            }
            return mkGeometryCollection(geometries);
        }
        if (matchesEmptyToken()) {
            return new GeometryCollection<P, Geometry<P>>(crs);
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private <P extends Position> Polygon<P> decodePolygonText(CoordinateReferenceSystem<P> crs) {
        if (matchesOpenList()) {
            List<LinearRing<P>> rings = new ArrayList<LinearRing<P>>();
            while (!matchesCloseList()) {
                LinearRing ring = decodeLinearRingText(crs);
                if (ring.isEmpty()) {
                    throw new WktDecodeException("Empty ring found in polygon" +
                            "Wkt: " + wktString);
                }
                rings.add(ring);
                matchesElementSeparator();
            }
            return mkPolygon(rings);
        }
        if (matchesEmptyToken()) {
            return new Polygon<P>(crs);
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private <P extends Position> LinearRing<P> decodeLinearRingText(CoordinateReferenceSystem<P> crs) {
        try {
            WktPointSequenceToken<P> token = decodePointSequence(crs);
            if (token == null) throw new WktDecodeException("No Linear Ring when expected");
            return new LinearRing<P>(token.getPositions(), token.getCoordinateReferenceSystem());
        } catch (IllegalArgumentException ex) {
            throw new WktDecodeException(ex.getMessage());
        }
    }

    private <P extends Position> LineString<P> decodeLineStringText(CoordinateReferenceSystem<P> crs) {
        WktPointSequenceToken<P> token = decodePointSequence(crs);
        if (token != null) {
            PositionSequence<P> positionSequence = token.getPositions();
            CoordinateReferenceSystem<P> tcrs = token.getCoordinateReferenceSystem();
            return new LineString<P>(positionSequence, tcrs);
        }
        if (matchesEmptyToken()) {
            return new LineString<P>(crs);
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private <P extends Position> Point<P> decodePointText(CoordinateReferenceSystem<P> crs) {
        WktPointSequenceToken<P> token = decodePointSequence(crs);
        if (token != null) {
            PositionSequence<P> positionSequence = token.getPositions();
            CoordinateReferenceSystem<P> tcrs = token.getCoordinateReferenceSystem();
            return new Point<P>(positionSequence, tcrs);
        }
        if (matchesEmptyToken()) {
            return new Point<P>(crs);
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private boolean matchesEmptyToken() {
        if (currentToken == WKT_GEOM_TOKENS.getEmpty()) {
            nextToken();
            return true;
        }
        return false;
    }

    private <P extends Position> WktPointSequenceToken<P> decodePointSequence(CoordinateReferenceSystem<P> forceCrs) {
        WktPointSequenceToken<P> token = null;
        if (matchesOpenList()) {
            if (currentToken instanceof WktPointSequenceToken) {
                token = (WktPointSequenceToken<P>) currentToken;
                nextToken();
            } else {
                throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
            }
            if (!matchesCloseList()) {
                throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
            }
        }
        return token;
    }

    private String buildWrongSymbolAtPositionMsg() {
        return "Wrong symbol at position: " + getTokenizerPosition() + " in Wkt: " + wktString;
    }

}