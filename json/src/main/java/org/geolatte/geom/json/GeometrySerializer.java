package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.io.IOException;

import static org.geolatte.geom.GeometryType.LINESTRING;
import static org.geolatte.geom.GeometryType.POINT;
import static org.geolatte.geom.GeometryType.POLYGON;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class GeometrySerializer<P extends Position> extends JsonSerializer<Geometry<P>> {

    final private Context<P> context;

    public GeometrySerializer(Context<P> context) {
        this.context = context;
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
        gen.writeStartObject();
        gen.writeStringField("type", geometry.getGeometryType().getCamelCased());
        writeCrs(gen, geometry.getCoordinateReferenceSystem());
        writeCoords(gen, geometry.getGeometryType(), geometry);
        gen.writeEndObject();

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
        if (type == LINESTRING) {
            writeLinear(gen, geom, buf);
        }
        if (type == POLYGON) {
            gen.writeStartArray();
            for(Geometry<P> c : ((Complex)geom).components()) {
                writeLinear(gen, c, buf);
            }
            gen.writeEndArray();
        }
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
        if (context.isFeatureSet(Feature.SUPPRESS_CRS_SERIALIZATION)) {
            return;
        }
        gen.writeFieldName("crs");
        writeNamedCrs(gen, crs);
    }

    private void writeNamedCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", "name");
        gen.writeFieldName("properties");
        writeCrsName(gen, crs.getCrsId().toString());
        gen.writeEndObject();
    }

    private void writeCrsName(JsonGenerator gen, String epsgString) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", epsgString);
        gen.writeEndObject();

    }

}
