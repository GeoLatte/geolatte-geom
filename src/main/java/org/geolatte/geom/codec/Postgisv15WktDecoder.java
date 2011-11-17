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
public class Postgisv15WktDecoder {


    private final static WktWordMatcher WORD_MATCHER = new Postgisv15WktWordMatcher();
    private final static Pattern SRID_RE = Pattern.compile("^SRID=(\\d+);", Pattern.CASE_INSENSITIVE);

    private String wktString;
    private WktTokenizer tokenizer;
    private WktToken currentToken;
    private PointSequence currentPointSequence = null;
    private int SRID = -1;

    public Geometry decode(String wkt) {
        if (wkt == null || wkt.isEmpty()) throw new WktParseException("Null or empty string passed");
        splitSRIDAndWKT(wkt);
        initializeTokenizer();
        return matchesGeometry();
    }

    private void initializeTokenizer() {
        this.tokenizer = new WktTokenizer(this.wktString, WORD_MATCHER);
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
        if (!(currentToken instanceof WktToken.Geometry)) {
            throw new WktParseException(buildWrongSymbolAtPositionMsg());
        }
        GeometryType type = ((WktToken.Geometry) currentToken).getType();
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
        throw new WktParseException("Unsupported geometry type in Wkt: " + type);
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
        throw new WktParseException(buildWrongSymbolAtPositionMsg());
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
        throw new WktParseException(buildWrongSymbolAtPositionMsg());
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
        throw new WktParseException(buildWrongSymbolAtPositionMsg());
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
        throw new WktParseException(buildWrongSymbolAtPositionMsg());
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
        throw new WktParseException(buildWrongSymbolAtPositionMsg());
    }

    private void matchesListDelimiter() {
        if (currentToken instanceof WktToken.ElementSeparator) {
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
        throw new WktParseException(buildWrongSymbolAtPositionMsg());
    }

    private Point matchesPointText() {
        if (matchesPoints()) {
            return Point.create(currentPointSequence, SRID);
        }
        if (matchesEmpty()) {
            return Point.createEmpty();
        }
        throw new WktParseException(buildWrongSymbolAtPositionMsg());

    }

    private boolean matchesPoints() {
        if (matchesStartList()) {
            this.currentPointSequence = matchesPointSequence();
            if (!matchesEndList()) throw new WktParseException(buildWrongSymbolAtPositionMsg());
            return true;
        }
        this.currentPointSequence = null;
        return false;
    }

    private boolean matchesEmpty() {
        if (currentToken instanceof WktToken.Empty) {
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

    private PointSequence matchesPointSequence() {
        if (currentToken instanceof WktToken.PointSequence) {
            PointSequence points = ((WktToken.PointSequence) currentToken).getPoints();
            nextToken();
            return points;
        }
        throw new WktParseException(buildWrongSymbolAtPositionMsg());
    }

    private boolean matchesStartList() {
        if (currentToken instanceof WktToken.StartList) {
            nextToken();
            return true;
        }
        return false;
    }

    private void nextToken() {
        currentToken = tokenizer.nextToken();
    }


    private String buildWrongSymbolAtPositionMsg() {
        return "Wrong symbol at position: " + tokenizer.position() + " in Wkt: " + wktString;
    }

}