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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Encodes geometries to Postgis WKT/EWKT representations.
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PostgisWktEncoder {

    private final static PostgisWktVariant WKT_WORDS = new PostgisWktVariant();

    //StringBuffer used so we can use DecimalFormat.format(double, StringBuffer, FieldPosition);
    private final FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD);
    private final StringBuffer builder = new StringBuffer();
    private final NumberFormat formatter;

    private static final int MAX_FRACTIONAL_DIGITS = 24;
    private static final DecimalFormatSymbols US_DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);

    /**
     * Constructs an instance.
     *
     */
    public PostgisWktEncoder() {
        formatter = new DecimalFormat("0.#", US_DECIMAL_FORMAT_SYMBOLS);
        formatter.setMaximumFractionDigits(MAX_FRACTIONAL_DIGITS);
    }

    /**
     * Encodes the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> to encode
     * @return the WKT representation of the geometry parameter value.
     */
    public String encode(Geometry geometry) {
        addSridIfValid(geometry);
        addGeometry(geometry);
        return result();
    }

    private void addSridIfValid(Geometry geometry) {
        if (geometry.getSRID() < 1) return;
        this.builder.append("SRID=")
                .append(geometry.getSRID())
                .append(";");
    }

    private void addGeometry(Geometry geometry) {
        addGeometryTag(geometry);
        addGeometryText(geometry);
    }

    private void addGeometryText(Geometry geometry) {
        if (geometry.isEmpty()) {
            addEmptyKeyword();
            return;
        }
        GeometryType type = geometry.getGeometryType();
        switch (type) {
            case POINT:
            case LINE_STRING:
                addPointList(geometry.getPoints());
                break;
            case POLYGON:
                addStartList();
                addLinearRings((Polygon) geometry);
                addEndList();
                break;
            case GEOMETRY_COLLECTION:
                addStartList();
                addGeometries((GeometryCollection) geometry, true);
                addEndList();
                break;
            case MULTI_POINT:
            case MULTI_LINE_STRING:
            case MULTI_POLYGON:
                addStartList();
                addGeometries((GeometryCollection) geometry, false);
                addEndList();
                break;
            default:
                throw new UnsupportedConversionException(String.format("Geometry type %s not supported.", type));
        }
    }

    private void addGeometries(GeometryCollection collection, boolean withTag) {
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            if (i > 0) addDelimiter();
            Geometry geom = collection.getGeometryN(i);
            if (withTag) {
                addGeometry(geom);
            } else {
                addGeometryText(geom);
            }
        }
    }

    private void addLinearRings(Polygon geometry) {
        addRing(geometry.getExteriorRing());
        for (int i = 0; i < geometry.getNumInteriorRing(); i++) {
            addDelimiter();
            addRing(geometry.getInteriorRingN(i));
        }
    }

    private void addRing(LinearRing ring) {
        addPointList(ring.getPoints());
    }

    private void addPointList(PointCollection points) {
        addStartList();
        double[] coords = new double[points.getCoordinateDimension()];
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) addDelimiter();
            points.getCoordinates(coords, i);
            addPoint(coords);
        }
        addEndList();

    }

    private void addEndList() {
        builder.append(')');
    }

    private void addPoint(double[] coords) {
        for (int i = 0; i < coords.length; i++) {
            if (i > 0) addWhitespace();
            addNumber(coords[i]);
        }
    }

    private void addNumber(double value) {
        formatter.format(value, builder, fp);
    }

    private void addWhitespace() {
        this.builder.append(" ");
    }

    private void addDelimiter() {
        this.builder.append(",");
    }

    private void addStartList() {
        this.builder.append("(");
    }

    private void addEmptyKeyword() {
        builder.append(" EMPTY");
    }

    private void addGeometryTag(Geometry geometry) {
        this.builder.append(WKT_WORDS.wordFor(geometry));
    }

    private String result() {
        return builder.toString();
    }

}
