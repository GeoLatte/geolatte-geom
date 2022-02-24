package org.geolatte.geom.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.geolatte.geom.Box;
import org.geolatte.geom.Feature;
import org.geolatte.geom.FeatureCollection;
import org.geolatte.geom.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class GeoJsonFeatureCollection<P extends Position, ID> implements FeatureCollection<P, ID> {
    private final List<Feature<P, ID>> features;

    public GeoJsonFeatureCollection(List<Feature<P, ID>> features) {
        this.features = new ArrayList<>();
        this.features.addAll(features);
    }

    //usefull for testing, but unsafe
    @SafeVarargs
    GeoJsonFeatureCollection(Feature<P, ID>... features) {
        this.features = new ArrayList<>();
        Collections.addAll(this.features, features);
    }

    @Override
    public List<Feature<P, ID>> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    @Override
    @JsonInclude(NON_EMPTY)
    public Box<P> getBbox() {
        Box<P> bbox = null;
        for (Feature<P, ID> feature : features) {
            if (bbox == null) {
                bbox = feature.getBbox();
            } else {
                bbox = bbox.union(feature.getBbox());
            }
        }

        return bbox;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoJsonFeatureCollection<?, ?> that = (GeoJsonFeatureCollection<?, ?>) o;
        return features.equals(that.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(features);
    }

    @Override
    public String toString() {
        return "GeoJsonFeatureCollection{" +
                "features=" + features +
                '}';
    }
}
