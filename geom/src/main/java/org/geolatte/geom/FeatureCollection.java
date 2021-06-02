package org.geolatte.geom;

import java.util.List;

public interface FeatureCollection<P extends Position, ID> {

    String TYPE = "FeatureCollection";

    List<Feature<P, ID>> getFeatures();

    default String getType() {
        return TYPE;
    }
}
