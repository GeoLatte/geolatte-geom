package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 23/04/16.
 */
public class Extension {

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

