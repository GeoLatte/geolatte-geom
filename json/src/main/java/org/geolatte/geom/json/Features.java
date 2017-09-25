package org.geolatte.geom.json;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
class Features {

    private final Map<Feature, Boolean> overrides = new HashMap<Feature, Boolean>();

    public boolean isFeatureSet(Feature feature) {
        Boolean override = overrides.get(feature);
        return  override == null ? feature.isSetByDefault : override;
    }


    public void override(Feature feature, boolean value) {
        if(feature.isSetByDefault != value) {
            overrides.put(feature, value);
        }
    }

}
