package org.geolatte.geom.codec;

import org.geolatte.geom.*;

class BaseWktWriter {

    private final StringBuilder builder;
    private final WktDialect dialect;

    /**
     * Constructs an instance.
     */
    public BaseWktWriter(WktDialect variant, StringBuilder builder) {
        this.dialect = variant;
        this.builder = builder;
    }

    public <P extends Position> String writeGeometry(Geometry<P> geometry) {
        addSrid(geometry.getSRID());
        addGeometry(geometry, true);
        return result();
    }

    protected void addSrid(int srid) {
        dialect.addSrid(builder, srid);
    }

    protected <P extends Position> void addGeometry(Geometry<P> geometry, boolean topLevel) {
        addGeometryTag(geometry);
        addGeometryZMMarker(geometry);
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
        if(dialect.writeMultiPointAsListOfPositions()) {
            addPointList(geometry.getPositions());
        } else {
            addStartList();
            addGeometries((AbstractGeometryCollection<P, ?>) geometry, false);
            addEndList();
        }
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
        if(dialect.isLimitedTo2D()) {
            return new double[2];
        }
        return new double[positions.getCoordinateDimension()];
    }

    protected <P extends Position> void setCoordinatesToWrite(PositionSequence<P> positions, int pos, double[] coords) {
        if(dialect.isLimitedTo2D()) {
            coords[0] = positions.getPositionN(pos).getCoordinate(0);
            coords[1] = positions.getPositionN(pos).getCoordinate(1);
        } else {
            for (int i = 0; i < positions.getCoordinateDimension(); i++) {
                coords[i] = positions.getPositionN(pos).getCoordinate(i);
            }
        }
    }

    private <P extends Position> void addPositions(PositionSequence<P> positions) {
        double[] coords = createCoordinateBuffer(positions);
        for (int i = 0; i < positions.size(); i++) {
            if (i > 0) {
                addDelimiter();
            }
            setCoordinatesToWrite(positions, i, coords);
            for (int k = 0; k < coords.length; k++) {
                //this is locale independent as it should be
                if (k > 0) builder.append(' ');
                builder.append(formatCoordinate(coords[k]));
            }
        }
    }

    private String formatCoordinate(double coord) {
        if (coord == (long) coord) {
            return String.valueOf((long) coord);
        } else {
            return String.valueOf(coord);
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

}

