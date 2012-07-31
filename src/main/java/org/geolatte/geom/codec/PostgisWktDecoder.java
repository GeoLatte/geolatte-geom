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
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PostgisWktDecoder extends AbstractWktDecoder<Geometry>{

    private final static PostgisWktVariant WKT_GEOM_TOKENS = new PostgisWktVariant();
    private final static Pattern SRID_RE = Pattern.compile("^SRID=(\\d+);", Pattern.CASE_INSENSITIVE);

    private String wktString;
    private PointSequence currentPointSequence = null;
    private CrsId crsId = CrsId.UNDEFINED;

    public PostgisWktDecoder() {
        super(WKT_GEOM_TOKENS);
    }

    public Geometry decode(String wkt) {
        if (wkt == null || wkt.isEmpty()) throw new WktDecodeException("Null or empty string passed");
        splitSRIDAndWKT(wkt);
        initializeTokenizer();
        return decodeGeometry();
    }

    private void initializeTokenizer() {
        setTokenizer(new WktTokenizer(this.wktString, getWktVariant()));
        nextToken();
    }

    private void splitSRIDAndWKT(String wkt) {
        Matcher matcher = SRID_RE.matcher(wkt);
        if (matcher.find()) {
            crsId = CrsId.valueOf(Integer.parseInt(matcher.group(1)));
            this.wktString = wkt.substring(matcher.end());
        } else {
            this.wktString = wkt;
        }
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
                matchesPoints();
                matchesElementSeparator();
                rings.add(new LinearRing(currentPointSequence, crsId));
            }
            return new Polygon(rings.toArray(new LinearRing[rings.size()]));
        }
        if (matchesEmptyToken()) {
            return Polygon.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private LineString decodeLineStringText() {
        if (matchesPoints()) {
            return new LineString(currentPointSequence, crsId);
        }
        if (matchesEmptyToken()) {
            return LineString.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private Point decodePointText() {
        if (matchesPoints()) {
            return new Point(currentPointSequence, crsId);
        }
        if (matchesEmptyToken()) {
            return Points.createEmpty();
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());

    }

    private boolean matchesPoints() {
        if (matchesOpenList()) {
            this.currentPointSequence = decodePointSequence();
            if (!matchesCloseList()) throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
            return true;
        }
        this.currentPointSequence = null;
        return false;
    }

    private boolean matchesEmptyToken() {
        if (currentToken == WKT_GEOM_TOKENS.getEmpty()) {
            nextToken();
            return true;
        }
        return false;
    }

    private PointSequence decodePointSequence() {
        if (currentToken instanceof WktPointSequenceToken) {
            PointSequence points = ((WktPointSequenceToken) currentToken).getPoints();
            nextToken();
            return points;
        }
        throw new WktDecodeException(buildWrongSymbolAtPositionMsg());
    }

    private String buildWrongSymbolAtPositionMsg() {
        return "Wrong symbol at position: " + getTokenizerPosition() + " in Wkt: " + wktString;
    }

}