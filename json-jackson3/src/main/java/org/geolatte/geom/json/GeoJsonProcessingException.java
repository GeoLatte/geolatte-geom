package org.geolatte.geom.json;

import tools.jackson.core.JacksonException;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class GeoJsonProcessingException extends JacksonException {

    GeoJsonProcessingException(String msg) {
        super(msg);
    }

    GeoJsonProcessingException(Throwable cause) {
        super(cause);
    }
}
