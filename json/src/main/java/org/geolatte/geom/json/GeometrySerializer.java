package org.geolatte.geom.json;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;

import static org.geolatte.geom.GeometryType.*;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class GeometrySerializer<P extends Position> extends ValueSerializer<Geometry<P>> {

    final private Settings settings;

    public GeometrySerializer(Settings settings) {
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
    public void serialize(Geometry<P> geometry, JsonGenerator gen, SerializationContext serializers) {
        writeGeometry(gen, geometry, !settings.isSet(Setting.SUPPRESS_CRS_SERIALIZATION));
    }

    @Override
    public void serializeWithType(Geometry<P> value, JsonGenerator gen, SerializationContext serializers, TypeSerializer typeSer) {
        // Better ensure we don't use specific sub-classes:
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, serializers,
                typeSer.typeId(value, value.getClass(), JsonToken.VALUE_STRING));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, serializers, typeIdDef);
    }

    private void writeGeometry(JsonGenerator gen, Geometry<P> geometry, boolean includeCrs) {
        gen.writeStartObject();
        gen.writeStringProperty("type", geometry.getGeometryType().getCamelCased());
        if(includeCrs) {
            writeCrs(gen, geometry.getCoordinateReferenceSystem());
        }
        if (geometry.getGeometryType() != GEOMETRYCOLLECTION) {
            writeCoords(gen, geometry.getGeometryType(), geometry);
        } else {
            AbstractGeometryCollection<P, Geometry<P>> gc = (AbstractGeometryCollection<P, Geometry<P>>) geometry;
            writeGeometries(gen, gc.components());
        }
        gen.writeEndObject();
    }

    private void writeGeometries(JsonGenerator gen, Geometry<P>[] geometries) {
        gen.writeName("geometries");
        gen.writeStartArray();
        for(Geometry<P> g : geometries) {
            writeGeometry(gen, g, false);
        }
        gen.writeEndArray();
    }

    private void writeCoords(JsonGenerator gen, GeometryType type, Geometry<P> geom) {
        gen.writeName("coordinates");
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

    private void writeListOfPolygon(JsonGenerator gen, MultiPolygon<P> geom, double[] buf) {
        gen.writeStartArray();
        for(Polygon<P> c : geom.components()) {
            writeListOfLinear(gen, c, buf);
        }
        gen.writeEndArray();
    }

    private void writeListOfLinear(JsonGenerator gen, Complex geom, double[] buf) {
        gen.writeStartArray();
        for(Geometry<P> c : geom.components()) {
            writeLinear(gen, c, buf);
        }
        gen.writeEndArray();
    }

    private void writeLinear(JsonGenerator gen, Geometry<P> geom, double[] buf) {
        gen.writeStartArray();

        for (P pos : geom.getPositions()) {
            writePosition(gen, pos, buf);
        }
        gen.writeEndArray();
    }

    private void writePosition(JsonGenerator gen, P position, double[] buf) {
        gen.writeArray(position.toArray(buf), 0, buf.length);
    }

    private void writeCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) {
        gen.writeName("crs");
        writeNamedCrs(gen, crs);
    }

    private void writeNamedCrs(JsonGenerator gen, CoordinateReferenceSystem<P> crs) {
        gen.writeStartObject();
        gen.writeStringProperty("type", "name");
        gen.writeName("properties");
        if (settings.isSet(Setting.SERIALIZE_CRS_AS_URN)) {
            writeCrsName(gen, crs.getCrsId().toUrn());
        } else {
            writeCrsName(gen, crs.getCrsId().toString());
        }
        gen.writeEndObject();
    }

    private void writeCrsName(JsonGenerator gen, String epsgString) {
        gen.writeStartObject();
        gen.writeStringProperty("name", epsgString);
        gen.writeEndObject();
    }
}
