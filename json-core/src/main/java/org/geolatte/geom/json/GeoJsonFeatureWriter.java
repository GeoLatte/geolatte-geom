package org.geolatte.geom.json;

import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.spi.GeoJsonWriter;

/**
 * Jackson-free writer for GeoJSON {@code Feature} objects.
 *
 * <p>The {@code geometry}, {@code properties} and {@code id} values are emitted via
 * {@link GeoJsonWriter#writePojoProperty}, which delegates back to the host
 * {@code ObjectMapper}. That delegation is what lets nested geometries flow through
 * the registered Geometry serializer and arbitrary user types in the property map flow
 * through their respective serializers.</p>
 */
public final class GeoJsonFeatureWriter {

    private final Settings settings;

    public GeoJsonFeatureWriter(Settings settings) {
        this.settings = settings;
    }

    public <P extends Position, ID> void write(GeoJsonWriter out, Feature<P, ID> feature) {
        out.writeStartObject();
        out.writeStringProperty("type", Feature.TYPE);
        if (feature.getId() != null) {
            out.writePojoProperty("id", feature.getId());
        }
        Box<?> box = feature.getBbox();
        if (box != null && !box.isEmpty() && settings.isSet(Setting.SERIALIZE_FEATURE_BBOX)) {
            out.writePojoProperty("bbox", box);
        }
        out.writePojoProperty("geometry", feature.getGeometry());
        out.writePojoProperty("properties", feature.getProperties());
        out.writeEndObject();
    }
}
