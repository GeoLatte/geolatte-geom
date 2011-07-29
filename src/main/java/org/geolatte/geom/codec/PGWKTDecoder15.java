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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class PGWKTDecoder15 {


    private final static WKTWordMatcher wordMatcher = new PGWKTWordMatcher15();
    private final static Pattern SRID_RE = Pattern.compile("^SRID=(\\d+);", Pattern.CASE_INSENSITIVE);

    private String wktString;
    private WKTTokenizer tokenizer;
    private WKTToken currentToken;
    private PointSequence currentPointSequence = null;
    private int SRID = -1;

    public Geometry decode(String wkt) {
        if (wkt == null || wkt.isEmpty()) throw new WKTParseException("Null or empty string passed");
        splitSRIDAndWKT(wkt);
        initializeTokenizer();
        return matchesGeometry();
    }

    private void initializeTokenizer() {
        this.tokenizer = new WKTTokenizer(this.wktString, wordMatcher);
        nextToken();
    }

    private void splitSRIDAndWKT(String wkt) {
        Matcher matcher = SRID_RE.matcher(wkt);
        if (matcher.find()) {
            SRID = Integer.parseInt(matcher.group(1));
            this.wktString = wkt.substring(matcher.end());
        } else {
            this.wktString = wkt;
        }
    }

    private Geometry matchesGeometry() {
        if (!(currentToken instanceof WKTToken.Geometry)) {
            throw new WKTParseException(buildWrongSymbolAtPositionMsg());
        }
        GeometryType type = ((WKTToken.Geometry) currentToken).getType();
        nextToken();
        switch (type) {
            case POINT:
                return matchesPointText();
            case LINE_STRING:
                return matchesLineStringText();
            case POLYGON:
                return matchesPolygonText();
            case GEOMETRY_COLLECTION:
                return matchesGeometryCollection();
            case MULTI_POINT:
                return matchesMultiPoint();
            case MULTI_LINE_STRING:
                return matchesMultiLineString();
            case MULTI_POLYGON:
                return matchesMultiPolygon();
        }
        throw new WKTParseException("Unsupported geometry type in WKT: " + type);
    }

    private Geometry matchesMultiPolygon() {
        if (matchesStartList()) {
            List<Polygon> polygons = new ArrayList<Polygon>();
            while (!matchesEndList()) {
                polygons.add(matchesPolygonText());
                matchesListDelimiter();
            }
            return MultiPolygon.create(polygons.toArray(new Polygon[polygons.size()]), SRID);
        }
        if (matchesEmpty()) {
            return MultiPolygon.createEmpty();
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());
    }

    private Geometry matchesMultiLineString() {
        if (matchesStartList()) {
            List<LineString> lineStrings = new ArrayList<LineString>();
            while (!matchesEndList()) {
                lineStrings.add(matchesLineStringText());
                matchesListDelimiter();
            }
            return MultiLineString.create(lineStrings.toArray(new LineString[lineStrings.size()]), SRID);
        }
        if (matchesEmpty()) {
            return MultiLineString.createEmpty();
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());
    }

    private Geometry matchesMultiPoint() {
        if (matchesStartList()) {
            List<Point> points = new ArrayList<Point>();
            while (!matchesEndList()) {
                points.add(matchesPointText());
                matchesListDelimiter();
            }
            return MultiPoint.create(points.toArray(new Point[points.size()]), SRID);
        }
        if (matchesEmpty()) {
            return MultiPoint.createEmpty();
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());
    }

    private GeometryCollection matchesGeometryCollection() {
        if (matchesStartList()) {
            List<Geometry> geometries = new ArrayList<Geometry>();
            while (!matchesEndList()) {
                geometries.add(matchesGeometry());
                matchesListDelimiter();
            }
            return GeometryCollection.create(geometries.toArray(new Geometry[geometries.size()]), SRID);
        }
        if (matchesEmpty()) {
            return GeometryCollection.createEmpty();
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());
    }

    private Polygon matchesPolygonText() {
        if (matchesStartList()) {
            List<LinearRing> rings = new ArrayList<LinearRing>();
            while (!matchesEndList()) {
                matchesPoints();
                matchesListDelimiter();
                rings.add(LinearRing.create(currentPointSequence, SRID));
            }
            return Polygon.create(rings.toArray(new LinearRing[rings.size()]), SRID);
        }
        if (matchesEmpty()) {
            return Polygon.createEmpty();
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());
    }

    private void matchesListDelimiter() {
        if (currentToken instanceof WKTToken.ElementSeparator) {
            nextToken();
        }
    }

    private LineString matchesLineStringText() {
        if (matchesPoints()) {
            return LineString.create(currentPointSequence, SRID);
        }
        if (matchesEmpty()) {
            return LineString.createEmpty();
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());
    }

    private Point matchesPointText() {
        if (matchesPoints()) {
            return Point.create(currentPointSequence, SRID);
        }
        if (matchesEmpty()) {
            return Point.createEmpty();
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());

    }

    private boolean matchesPoints() {
        if (matchesStartList()) {
            this.currentPointSequence = matchesPointSequence();
            if (!matchesEndList()) throw new WKTParseException(buildWrongSymbolAtPositionMsg());
            return true;
        }
        this.currentPointSequence = null;
        return false;
    }

    private boolean matchesEmpty() {
        if (currentToken instanceof WKTToken.Empty) {
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

    private PointSequence matchesPointSequence() {
        if (currentToken instanceof WKTToken.PointSequence) {
            PointSequence points = ((WKTToken.PointSequence) currentToken).getPoints();
            nextToken();
            return points;
        }
        throw new WKTParseException(buildWrongSymbolAtPositionMsg());
    }

    private boolean matchesStartList() {
        if (currentToken instanceof WKTToken.StartList) {
            nextToken();
            return true;
        }
        return false;
    }

    private void nextToken() {
        currentToken = tokenizer.nextToken();
    }


    private String buildWrongSymbolAtPositionMsg() {
        return "Wrong symbol at position: " + tokenizer.position() + " in WKT: " + wktString;
    }

}