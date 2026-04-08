package org.geolatte.geom.json;

import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.Complex;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.spi.GeoJsonWriter;

import static org.geolatte.geom.GeometryType.GEOMETRYCOLLECTION;
import static org.geolatte.geom.GeometryType.LINESTRING;
import static org.geolatte.geom.GeometryType.MULTILINESTRING;
import static org.geolatte.geom.GeometryType.MULTIPOINT;
import static org.geolatte.geom.GeometryType.MULTIPOLYGON;
import static org.geolatte.geom.GeometryType.POINT;
import static org.geolatte.geom.GeometryType.POLYGON;

/**
 * Jackson-free writer for GeoJSON geometry objects.
 *
 * <p>This is the canonical implementation of GeoJSON geometry encoding. The Jackson
 * adapter modules call into this class via {@link GeoJsonWriter}; they do not duplicate
 * the encoding logic.</p>
 */
public final class GeoJsonGeometryWriter {

    private final Settings settings;
    private final GeoJsonCrsWriter crsWriter;

    public GeoJsonGeometryWriter(Settings settings) {
        this.settings = settings;
        this.crsWriter = new GeoJsonCrsWriter(settings);
    }

    public <P extends Position> void write(GeoJsonWriter out, Geometry<P> geometry) {
        writeGeometry(out, geometry, !settings.isSet(Setting.SUPPRESS_CRS_SERIALIZATION));
    }

    private <P extends Position> void writeGeometry(GeoJsonWriter out, Geometry<P> geometry, boolean includeCrs) {
        out.writeStartObject();
        out.writeStringProperty("type", geometry.getGeometryType().getCamelCased());
        if (includeCrs) {
            writeCrs(out, geometry.getCoordinateReferenceSystem());
        }
        if (geometry.getGeometryType() != GEOMETRYCOLLECTION) {
            writeCoords(out, geometry.getGeometryType(), geometry);
        } else {
            @SuppressWarnings("unchecked")
            AbstractGeometryCollection<P, Geometry<P>> gc = (AbstractGeometryCollection<P, Geometry<P>>) geometry;
            writeGeometries(out, gc.components());
        }
        out.writeEndObject();
    }

    private <P extends Position> void writeGeometries(GeoJsonWriter out, Geometry<P>[] geometries) {
        out.writeName("geometries");
        out.writeStartArray();
        for (Geometry<P> g : geometries) {
            writeGeometry(out, g, false);
        }
        out.writeEndArray();
    }

    private <P extends Position> void writeCoords(GeoJsonWriter out, GeometryType type, Geometry<P> geom) {
        out.writeName("coordinates");
        double[] buf = new double[geom.getCoordinateDimension()];
        if (geom.isEmpty()) {
            out.writeStartArray();
            out.writeEndArray();
            return;
        }
        if (type == POINT) {
            writePosition(out, geom.getPositionN(0), buf);
        }
        if (type == LINESTRING || type == MULTIPOINT) {
            writeLinear(out, geom, buf);
        }
        if (type == POLYGON || type == MULTILINESTRING) {
            writeListOfLinear(out, (Complex) geom, buf);
        }
        if (type == MULTIPOLYGON) {
            @SuppressWarnings("unchecked")
            MultiPolygon<P> mp = (MultiPolygon<P>) geom;
            writeListOfPolygon(out, mp, buf);
        }
    }

    private <P extends Position> void writeListOfPolygon(GeoJsonWriter out, MultiPolygon<P> geom, double[] buf) {
        out.writeStartArray();
        for (Polygon<P> c : geom.components()) {
            writeListOfLinear(out, c, buf);
        }
        out.writeEndArray();
    }

    private <P extends Position> void writeListOfLinear(GeoJsonWriter out, Complex geom, double[] buf) {
        out.writeStartArray();
        @SuppressWarnings("unchecked")
        Geometry<P>[] components = (Geometry<P>[]) geom.components();
        for (Geometry<P> c : components) {
            writeLinear(out, c, buf);
        }
        out.writeEndArray();
    }

    private <P extends Position> void writeLinear(GeoJsonWriter out, Geometry<P> geom, double[] buf) {
        out.writeStartArray();
        for (P pos : geom.getPositions()) {
            writePosition(out, pos, buf);
        }
        out.writeEndArray();
    }

    private <P extends Position> void writePosition(GeoJsonWriter out, P position, double[] buf) {
        out.writeDoubleArray(position.toArray(buf), 0, buf.length);
    }

    private <P extends Position> void writeCrs(GeoJsonWriter out, CoordinateReferenceSystem<P> crs) {
        out.writeName("crs");
        crsWriter.writeNamedCrs(out, crs);
    }
}
