package org.geolatte.geom.crs;

import java.io.Serializable;

/**
 * Created by Karel Maesen, Geovise BVBA on 23/04/16.
 */
public class Extension implements Serializable {

    private static final long serialVersionUID = 6884205871950410216L;

    private final String name;
    private final String value;

    public Extension(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}

