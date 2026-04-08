package org.geolatte.geom.json;

import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.Position;
import org.geolatte.geom.json.spi.GeoJsonWriter;

/**
 * Jackson-free writer for GeoJSON {@code FeatureCollection} objects.
 */
public final class GeoJsonFeatureCollectionWriter {

    private final Settings settings;

    public GeoJsonFeatureCollectionWriter(Settings settings) {
        this.settings = settings;
    }

    public <P extends Position, ID> void write(GeoJsonWriter out, FeatureCollection<P, ID> featureColl) {
        out.writeStartObject();
        out.writeStringProperty("type", FeatureCollection.TYPE);
        Box<?> box = featureColl.getBbox();
        if (box != null && !box.isEmpty() && settings.isSet(Setting.SERIALIZE_FEATURE_COLLECTION_BBOX)) {
            out.writePojoProperty("bbox", box);
        }
        out.writeArrayPropertyStart("features");
        for (Feature<?, ?> f : featureColl.getFeatures()) {
            out.writePojo(f);
        }
        out.writeEndArray();
        out.writeEndObject();
    }
}
