package org.geolatte.geom.json;

import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.spi.GeoJsonWriter;

/**
 * Jackson-free writer for the GeoJSON {@code crs} member, encoded as a "named" CRS object.
 */
public final class GeoJsonCrsWriter {

    private final Settings settings;

    public GeoJsonCrsWriter(Settings settings) {
        this.settings = settings;
    }

    public <P extends Position> void writeNamedCrs(GeoJsonWriter out, CoordinateReferenceSystem<P> crs) {
        out.writeStartObject();
        out.writeStringProperty("type", "name");
        out.writeName("properties");
        if (settings.isSet(Setting.SERIALIZE_CRS_AS_URN)) {
            writeCrsName(out, crs.getCrsId().toUrn());
        } else {
            writeCrsName(out, crs.getCrsId().toString());
        }
        out.writeEndObject();
    }

    private void writeCrsName(GeoJsonWriter out, String epsgString) {
        out.writeStartObject();
        out.writeStringProperty("name", epsgString);
        out.writeEndObject();
    }
}
