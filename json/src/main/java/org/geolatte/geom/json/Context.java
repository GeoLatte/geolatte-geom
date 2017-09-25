package org.geolatte.geom.json;

import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class Context<P extends Position> {

    private final CoordinateReferenceSystem<P> defaultCRS;
    private final Features features;

    Context(CoordinateReferenceSystem<P> defaultCRS,Features features) {
        this.features = features;
        this.defaultCRS =defaultCRS;
    }

    CoordinateReferenceSystem<P> getDefaultCrs() {
        return this.defaultCRS;
    }

    public boolean isFeatureSet(Feature feature) {
        return features.isFeatureSet(feature);
    }
}
