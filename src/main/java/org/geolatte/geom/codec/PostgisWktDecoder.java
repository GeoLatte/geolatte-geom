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
import org.geolatte.geom.crs.CrsId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A decoder for the Postgis WKT/EWKT representations as used in Postgis (at least 1.0 to 1.5+).
 *
 * <p>This class is not thread-safe</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PostgisWktDecoder extends AbstractWktDecoder<Geometry> {

    private final static PostgisWktVariant WKT_GEOM_TOKENS = new PostgisWktVariant();
    private final static Pattern SRID_RE = Pattern.compile("^SRID=(\\d+);", Pattern.CASE_INSENSITIVE);

    private String wktString;
    private CrsId crsId;

    public PostgisWktDecoder() {
        super(WKT_GEOM_TOKENS);
    }

    public Geometry decode(String wkt) {
        if (wkt == null || wkt.isEmpty()) {
            throw new WktDecodeException("Null or empty string cannot be decoded into a geometry");
        }
        prepare(wkt);
        initializeTokenizer();
        return decodeGeometry();
    }

    /**
     * The instance fields wktString and crsId are initialized prior to decoding. For postgis EWKT that entails
     * extracting the SRID prefix (if any) from the WKT string.
     *
     * @param wkt the WKT representation
     */
    private void prepare(String wkt) {
        Matcher matcher = SRID_RE.matcher(wkt);
        if (matcher.find()) {
            crsId = CrsId.valueOf(Integer.parseInt(matcher.group(1)));
            wktString = wkt.substring(matcher.end());
        } else {
            crsId = CrsId.UNDEFINED;
            wktString = wkt;
        }
    }

    private void initializeTokenizer() {
        setTokenizer(new WktTokenizer(wktString, getWktVariant()));
        nextToken();
    }

    private Geometry decodeGeometry() {
        if (!(currentToken instanceof WktGeometryToken)) {
            throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
        }
        GeometryType type = ((WktGeometryToken) currentToken).getType();
        nextToken();
        switch (type) {
            case POINT:
                return decodePointText();
            case LINE_STRING:
                return decodeLineStringText();
            case POLYGON:
                return decodePolygonText();
            case GEOMETRY_COLLECTION:
                return decodeGeometryCollection();
            case MULTI_POINT:
                return decodeMultiPoint();
            case MULTI_LINE_STRING:
                return decodeMultiLineString();
            case MULTI_POLYGON:
                return decodeMultiPolygon();
        }
        throw new WktDecodeException("Unsupported geometry type in Wkt: " + type);
    }

    private Geometry decodeMultiPolygon() {
        if (matchesOpenList()) {
            List<Polygon> polygons = new ArrayList<Polygon>();
            while (!matchesCloseList()) {
                polygons.add(decodePolygonText());
                matchesElementSeparator();
            }
            return new MultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        }
        if (matchesEmptyToken()) {
            return MultiPolygon.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private Geometry decodeMultiLineString() {
        if (matchesOpenList()) {
            List<LineString> lineStrings = new ArrayList<LineString>();
            while (!matchesCloseList()) {
                lineStrings.add(decodeLineStringText());
                matchesElementSeparator();
            }
            return new MultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
        }
        if (matchesEmptyToken()) {
            return MultiLineString.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private Geometry decodeMultiPoint() {
        if (matchesOpenList()) {
            List<Point> points = new ArrayList<Point>();
            //this handles the case of the non-compliant MultiPoints in Postgis (e.g.
            // MULTIPOINT(10 10, 12 13) rather than MULTIPOINT((10 20), (30 40))
            if (currentToken instanceof WktPointSequenceToken) {
                PointSequence pointSequence = ((WktPointSequenceToken) currentToken).getPoints();
                for (Point pnt : pointSequence) {
                    points.add(pnt);
                }
                nextToken();
            }
            while (!matchesCloseList()) {
                points.add(decodePointText());
                matchesElementSeparator();
            }
            return new MultiPoint(points.toArray(new Point[points.size()]));
        }
        if (matchesEmptyToken()) {
            return MultiPoint.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private GeometryCollection decodeGeometryCollection() {
        if (matchesOpenList()) {
            List<Geometry> geometries = new ArrayList<Geometry>();
            while (!matchesCloseList()) {
                geometries.add(decodeGeometry());
                matchesElementSeparator();
            }
            return new GeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
        }
        if (matchesEmptyToken()) {
            return GeometryCollection.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private Polygon decodePolygonText() {
        if (matchesOpenList()) {
            List<LinearRing> rings = new ArrayList<LinearRing>();
            while (!matchesCloseList()) {
                LinearRing ring = decodeLinearRingText();
                rings.add(ring);
                matchesElementSeparator();
            }
            return new Polygon(rings.toArray(new LinearRing[rings.size()]));
        }
        if (matchesEmptyToken()) {
            return Polygon.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private LinearRing decodeLinearRingText() {
        try {
            return new LinearRing(decodePointSequence(), crsId);
        } catch (IllegalArgumentException ex) {
            throw new WktDecodeException(ex.getMessage());
        }
    }

    private LineString decodeLineStringText() {
        PointSequence pointSequence = decodePointSequence();
        if (pointSequence != null) {
            return new LineString(pointSequence, crsId);
        }
        if (matchesEmptyToken()) {
            return LineString.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private Point decodePointText() {
        PointSequence pointSequence = decodePointSequence();
        if (pointSequence != null) {
            return new Point(pointSequence, crsId);
        }
        if (matchesEmptyToken()) {
            return Points.createEmpty();
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

    private PointSequence decodePointSequence() {
        PointSequence pointSequence = null;
        if (matchesOpenList()) {
            if (currentToken instanceof WktPointSequenceToken) {
                pointSequence = ((WktPointSequenceToken) currentToken).getPoints();
                nextToken();
            } else {
                throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
            }
            if (!matchesCloseList()) {
                throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
            }
        }
        return pointSequence;
    }

    private String buildWrongSymbolAtPositionMsg() {
        return "Wrong symbol at position: " + getTokenizerPosition() + " in Wkt: " + wktString;
    }

}