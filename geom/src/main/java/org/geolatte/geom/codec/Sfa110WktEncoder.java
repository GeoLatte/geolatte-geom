package org.geolatte.geom.codec;

import org.geolatte.geom.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

class Sfa110WktEncoder implements WktEncoder {
    private final static Sfa110WktVariant WKT_WORDS = new Sfa110WktVariant();
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
    public Sfa110WktEncoder() {
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
        builder = new StringBuffer();
        inGeometryCollection = false;
        addGeometry(geometry);
        return result();
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
                addGeometries((AbstractGeometryCollection<P, ?>) geometry, true);
                addEndList();
                break;
            case MULTIPOINT:
            case MULTILINESTRING:
            case MULTIPOLYGON:
                addStartList();
                addGeometries((AbstractGeometryCollection<P, ?>) geometry, false);
                addEndList();
                break;
            default:
                throw new UnsupportedConversionException(String.format("Geometry type %s not supported.", type));
        }
    }

    private <P extends Position, G extends Geometry<P>> void addGeometries(AbstractGeometryCollection<P, G> collection, boolean withTag) {
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
        builder.append(getWktVariant().wordFor(geometry));
    }

    private String result() {
        return builder.toString();
    }

    protected Sfa110WktVariant getWktVariant() {
        return WKT_WORDS;
    }
}
