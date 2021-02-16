package org.geolatte.geom.codec;

import org.geolatte.geom.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

class BaseWktEncoder implements WktEncoder {

    //StringBuffer used so we can use DecimalFormat.format(double, StringBuffer, FieldPosition);
    private StringBuffer builder;
    private final BaseWktDialect dialect;
    private final WktPositionEncoder positionEncoder = new PositionEncoder();

    /**
     * Constructs an instance.
     */
    public BaseWktEncoder(BaseWktDialect variant) {
        this.dialect = variant;
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
        addSrid(geometry.getSRID());
        addGeometry(geometry, true);
        return result();
    }

    protected void addSrid(int srid) {
        dialect.addSrid(builder, srid);
    }

    protected <P extends Position> void addGeometry(Geometry<P> geometry, boolean topLevel) {
        addGeometryTag(geometry);
        if (topLevel) addGeometryZMMarker(geometry);
        addGeometryText(geometry);
    }

    protected void addGeometryTag(Geometry<?> geometry) {
        dialect.addGeometryTag(builder, geometry);
    }

    protected void addGeometryZMMarker(Geometry<?> geometry) {
        dialect.addGeometryZMMarker(builder, geometry);
    }

    protected <P extends Position> void addGeometryText(Geometry<P> geometry) {
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
                addMultiPointText(geometry);
                break;
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

    protected <P extends Position> void addMultiPointText(Geometry<P> geometry) {
        addStartList();
        addGeometries((AbstractGeometryCollection<P, ?>) geometry, false);
        addEndList();
    }

    protected <P extends Position, G extends Geometry<P>> void addGeometries(AbstractGeometryCollection<P, G> collection, boolean withTag) {
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            if (i > 0) {
                addDelimiter();
            }
            Geometry<?> geom = collection.getGeometryN(i);
            if (withTag) {
                addGeometry(geom, false);
            } else {
                addGeometryText(geom);
            }
        }
    }

    protected <P extends Position> void addLinearRings(Polygon<P> geometry) {
        addRing(geometry.getExteriorRing());
        for (int i = 0; i < geometry.getNumInteriorRing(); i++) {
            addDelimiter();
            addRing(geometry.getInteriorRingN(i));
        }
    }

    protected <P extends Position> void addRing(LinearRing<P> ring) {
        addPointList(ring.getPositions());
    }

    protected <P extends Position> void addPointList(PositionSequence<P> points) {
        addStartList();
        addPositions(points);
        addEndList();

    }

    protected <P extends Position> double[] createCoordinateBuffer(PositionSequence<P> positions) {
        return new double[2];
    }

    protected <P extends Position> void setCoordinatesToWrite(PositionSequence<P> positions, int pos, double[] coords) {
        coords[0] = positions.getPositionN(pos).getCoordinate(0);
        coords[1] = positions.getPositionN(pos).getCoordinate(1);
    }

    private <P extends Position> void addPositions(PositionSequence<P> positions) {
        double[] coords = createCoordinateBuffer(positions);
        for (int i = 0; i < positions.size(); i++) {
            if (i > 0) {
                addDelimiter();
            }
            setCoordinatesToWrite(positions, i, coords);
            positionEncoder.addPoint(builder, coords);
        }
    }

    private void addEndList() {
        builder.append(')');
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


    private String result() {
        return builder.toString();
    }

    static class PositionEncoder implements WktPositionEncoder {
        private static final int MAX_FRACTIONAL_DIGITS = 15;
        private static final DecimalFormatSymbols US_DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);

        private final FieldPosition fp = new FieldPosition(NumberFormat.INTEGER_FIELD);
        private final NumberFormat formatter;

        PositionEncoder() {
            formatter = new DecimalFormat("0.#", US_DECIMAL_FORMAT_SYMBOLS);
            formatter.setMaximumFractionDigits(MAX_FRACTIONAL_DIGITS);
        }

        public void addPoint(StringBuffer buffer, double[] coords) {
            for (int i = 0; i < coords.length; i++) {
                if (i > 0) {
                    buffer.append(' ');
                }
                formatter.format(coords[i], buffer, fp);
            }
        }
    }
}

