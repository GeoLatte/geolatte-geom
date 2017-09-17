package org.geolatte.geom.json;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 */
public class GeoJsonProcessingException extends JsonProcessingException {

    GeoJsonProcessingException(String msg) {
        super(msg);
    }
}
