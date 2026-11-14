package org.geolatte.geom.json.jackson3;

import tools.jackson.core.JacksonException;

/**
 * Created by Karel Maesen, Geovise BVBA on 08/09/17.
 *
 * @deprecated This exception is no longer thrown. GeoJSON encoding and decoding errors are reported as
 * {@link org.geolatte.geom.json.GeoJsonException}, possibly wrapped in a Jackson exception by the
 * {@code ObjectMapper}. Catch {@code GeoJsonException} instead. This class will be removed in the next
 * major release.
 */
@Deprecated(since = "2.0.2", forRemoval = true)
public class GeoJsonProcessingException extends JacksonException {

    GeoJsonProcessingException(String msg) {
        super(msg);
    }

    GeoJsonProcessingException(Throwable cause) {
        super(cause);
    }
}
