package org.geolatte.geom.json;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public enum Setting {

    /**
     *  Force the JSON strings to the CRS set as default for the {@code GeometryModule}.
     *
     *  <p>This ignores the CRS identifier present in the JSON object.</p>
     *
     */
    FORCE_DEFAULT_CRS_DIMENSION(false),

    /**
     * Suppresses the serialization of CRS property
     */
    SUPPRESS_CRS_SERIALIZATION(false),

    /**
     * Serialize the CRS as a URN, rather than the traditional AUTH:CODE format.
     */
    SERIALIZE_CRS_AS_URN(false);

    private boolean setByDefault;

    Setting(boolean isSetByDefault) {
        this.setByDefault = isSetByDefault;
    }

    public boolean isSetByDefault() {
        return this.setByDefault;
    }


}
