package org.geolatte.geom.json.jackson3;

import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.GeoJsonCrsWriter;
import org.geolatte.geom.json.Settings;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class CrsSerializer<P extends Position> extends ValueSerializer<CoordinateReferenceSystem<P>> {

    final private CoordinateReferenceSystem<P> defaultCRS;
    final private GeoJsonCrsWriter writer;

    public CrsSerializer(CoordinateReferenceSystem<P> defaultCRS, Settings settings) {
        this.defaultCRS = defaultCRS;
        this.writer = new GeoJsonCrsWriter(settings);
    }

    @Override
    public void serialize(CoordinateReferenceSystem<P> crs, JsonGenerator gen, SerializationContext serializers) {
        writer.writeNamedCrs(new Jackson3GeoJsonWriter(gen, serializers), crs);
    }
}
