package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

import static org.geolatte.geom.GeometryType.*;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class GeometrySerializer<P extends Position> extends JsonSerializer<Geometry<P>> {

    final private CoordinateReferenceSystem<P> defaultCRS;
    final private Settings settings;

    public GeometrySerializer(CoordinateReferenceSystem<P> defaultCRS, Settings settings) {
        this.defaultCRS = defaultCRS;
        this.settings = settings;
    }


    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param geometry    Geometry value to serialize; can <b>not</b> be null.
     * @param gen         Generator used to output resulting Json content
     * @param serializers Provider that can be used to get serializers for
     */
    @Override
    public void serialize(Geometry<P> geometry, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        writeGeometry(gen, geometry, !settings.isSet(Setting.SUPPRESS_CRS_SERIALIZATION));
    }


    private void writeGeometry(JsonGenerator gen, Geometry<P> geometry, boolean includeCrs) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", geometry.getGeometryType().getCamelCased());
        if(includeCrs) {
            writeCrs(gen, geometry.getCoordinateReferenceSystem());
        }
        if (geometry.getGeometryType() != GEOMETRYCOLLECTION) {
            writeCoords(gen, geometry.getGeometryType(), geometry);
        } else {
            GeometryCollection<P, Geometry<P>> gc = (GeometryCollection<P, Geometry<P>>) geometry;
            writeGeometries(gen, gc.components());
        }
        gen.writeEndObject();
    }

    private void writeGeometries(JsonGenerator gen, Geometry<P>[] geometries) throws IOException {
        gen.writeFieldName("geometries");
        gen.writeStartArray();
        for(Geometry<P> g : geometries) {
            writeGeometry(gen, g, false);
        }
        gen.writeEndArray();
    }

    private void writeCoords(JsonGenerator gen, GeometryType type, Geometry<P> geom) throws IOException {
        gen.writeFieldName("coordinates");
        double[] buf = new double[geom.getCoordinateDimension()];
        if (geom.isEmpty()) {
            gen.writeStartArray();
            gen.writeEndArray();
            return;
        }
        if (type == POINT) {
            writePosition(gen, geom.getPositionN(0), buf);
        }
        if (type == LINESTRING || type == MULTIPOINT) {
            writeLinear(gen, geom, buf);
        }
        if (type == POLYGON || type == MULTILINESTRING) {
            writeListOfLinear(gen, (Complex) geom, buf);
        }
        if(type == MULTIPOLYGON){
            writeListOfPolygon(gen, (MultiPolygon) geom, buf);
        }
    }

    private void writeListOfPolygon(JsonGenerator gen, MultiPolygon<P> geom, double[] buf) throws IOException {
        gen.writeStartArray();
        for(Polygon<P> c : geom.components()) {
            writeListOfLinear(gen, c, buf);
        }
        gen.writeEndArray();
    }

    private void writeListOfLinear(JsonGenerator gen, Complex geom, double[] buf) throws IOException {
        gen.writeStartArray();
        for(Geometry<P> c : geom.components()) {
            writeLinear(gen, c, buf);
        }
        gen.writeEndArray();
    }

    private void writeLinear(JsonGenerator gen, Geometry<P> geom, double[] buf) throws IOException {
        gen.writeStartArray();

        for (P pos : geom.getPositions()) {
            writePosition(gen, pos, buf);
        }
        gen.writeEndArray();
    }


    private void writePosition(JsonGenerator gen, P position, double[] buf) throws IOException {
        gen.writeArray(position.toArray(buf), 0, buf.length);
    }

    private void writeCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) throws IOException {
        gen.writeFieldName("crs");
        writeNamedCrs(gen, crs);
    }

    private void writeNamedCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", "name");
        gen.writeFieldName("properties");
        if (settings.isSet(Setting.SERIALIZE_CRS_AS_URN)) {
            writeCrsName(gen, crs.getCrsId().toUrn());
        } else {
            writeCrsName(gen, crs.getCrsId().toString());
        }
        gen.writeEndObject();
    }

    private void writeCrsName(JsonGenerator gen, String epsgString) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", epsgString);
        gen.writeEndObject();

    }

}
