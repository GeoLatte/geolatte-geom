package org.geolatte.geom.json;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public enum Feature {

    FORCE_DEFAULT_CRS_DIMENSION(false),
    SUPPRESS_CRS_SERIALIZATION(false);

    boolean isSetByDefault;

    Feature(boolean isSetByDefault) {
        this.isSetByDefault = isSetByDefault;
    }

    public boolean getValue() {
        return this.isSetByDefault;
    }


}
