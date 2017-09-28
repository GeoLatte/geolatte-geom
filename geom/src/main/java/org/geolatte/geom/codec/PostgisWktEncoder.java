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
 * <p>This class is not thread-safe.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
class PostgisWktEncoder implements WktEncoder {

    private final static PostgisWktVariant WKT_WORDS = new PostgisWktVariant();
    private static final int MAX_FRACTIONAL_DIGITS = 24;
    private static final DecimalFormatSymbols US_DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);

    private final FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD);
    private final NumberFormat formatter;

    //StringBuffer used so we can use DecimalFormat.format(double, StringBuffer, FieldPosition);
    private StringBuffer builder;
    private boolean inGeometryCollection = false;

    /**
     * Constructs an instance.
     */
    public PostgisWktEncoder() {
        formatter = new DecimalFormat("0.#", US_DECIMAL_FORMAT_SYMBOLS);
        formatter.setMaximumFractionDigits(MAX_FRACTIONAL_DIGITS);
    }

    /**
     * Encodes the specified <code>Geometry</code>.
     *
     * @param geometry the <code>Geometry</code> to encode
     * @return the WKT representation of the given geometry
     */
    @Override
    public <P extends Position> String encode(Geometry<P> geometry) {
        prepare();
        addSridIfValid(geometry);
        addGeometry(geometry);
        return result();
    }

    protected boolean skipSrid(){
        return false;
    }

    private void prepare() {
        builder = new StringBuffer();
        inGeometryCollection = false;
    }

    private <P extends Position> void addSridIfValid(Geometry<P> geometry) {
        if (geometry.getSRID() < 1 || skipSrid()) {
            return;
        }
        builder.append("SRID=")
                .append(geometry.getSRID())
                .append(";");
    }

    private <P extends Position> void addGeometry(Geometry<P> geometry) {
        addGeometryTag(geometry);
        addGeometryText(geometry);
    }

    private <P extends Position> void addGeometryText(Geometry<P> geometry) {
        if (geometry.isEmpty()) {
            addEmptyKeyword();
            return;
        }
        GeometryType type = geometry.getGeometryType();
        switch (type) {
            case POINT:
            case LINESTRING:
            case LINEARRING:
                addPointList(geometry.getPositions());
                break;
            case POLYGON:
                addStartList();
                addLinearRings((Polygon<P>) geometry);
                addEndList();
                break;
            case GEOMETRYCOLLECTION:
                addStartList();
                addGeometries((GeometryCollection<P, ?>) geometry, true);
                addEndList();
                break;
            case MULTIPOINT:
            case MULTILINESTRING:
            case MULTIPOLYGON:
                addStartList();
                addGeometries((GeometryCollection<P, ?>) geometry, false);
                addEndList();
                break;
            default:
                throw new UnsupportedConversionException(String.format("Geometry type %s not supported.", type));
        }
    }

    private <P extends Position, G extends Geometry<P>> void addGeometries(GeometryCollection<P,G> collection, boolean withTag) {
        inGeometryCollection = true;
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            if (i > 0) {
                addDelimiter();
            }
            Geometry geom = collection.getGeometryN(i);
            if (withTag) {
                addGeometry(geom);
            } else {
                addGeometryText(geom);
            }
        }
    }

    private <P extends Position> void addLinearRings(Polygon<P> geometry) {
        addRing(geometry.getExteriorRing());
        for (int i = 0; i < geometry.getNumInteriorRing(); i++) {
            addDelimiter();
            addRing(geometry.getInteriorRingN(i));
        }
    }

    private <P extends Position> void addRing(LinearRing<P> ring) {
        addPointList(ring.getPositions());
    }

    private <P extends Position> void addPointList(PositionSequence<P> points) {
        addStartList();
        double[] coords = new double[points.getCoordinateDimension()];
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                addDelimiter();
            }
            points.getCoordinates(i, coords);
            addPoint(coords);
        }
        addEndList();

    }

    private void addEndList() {
        builder.append(')');
    }

    private void addPoint(double[] coords) {
        for (int i = 0; i < coords.length; i++) {
            if (i > 0) {
                addWhitespace();
            }
            addNumber(coords[i]);
        }
    }

    private void addNumber(double value) {
        formatter.format(value, builder, fp);
    }

    private void addWhitespace() {
        builder.append(" ");
    }

    private void addDelimiter() {
        builder.append(",");
    }

    private void addStartList() {
        builder.append("(");
    }

    private void addEmptyKeyword() {
        builder.append(" EMPTY");
    }

    private void addGeometryTag(Geometry geometry) {
        if (inGeometryCollection) {
            builder.append(getWktWords().wordFor(geometry, true));
        } else {
            builder.append(getWktWords().wordFor(geometry, false));
        }
    }

    private String result() {
        return builder.toString();
    }
    
    protected PostgisWktVariant getWktWords() {
    	return WKT_WORDS;
    }

}
