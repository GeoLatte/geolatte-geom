package org.geolatte.geom.json;

import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.json.spi.JsonTreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Jackson-free reader for GeoJSON {@code FeatureCollection} objects.
 */
public final class GeoJsonFeatureCollectionReader {

    private final GeoJsonFeatureReader featureReader;

    public GeoJsonFeatureCollectionReader(CoordinateReferenceSystem<?> defaultCrs, Settings settings) {
        this.featureReader = new GeoJsonFeatureReader(defaultCrs, settings);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public FeatureCollection<?, ?> read(JsonTreeNode root) {
        JsonTreeNode featuresNode = root.get("features");
        List<Feature<?, ?>> features = new ArrayList<>();
        if (featuresNode != null) {
            for (int i = 0; i < featuresNode.size(); i++) {
                features.add(featureReader.read(featuresNode.get(i)));
            }
        }
        return new GeoJsonFeatureCollection(features);
    }
}
