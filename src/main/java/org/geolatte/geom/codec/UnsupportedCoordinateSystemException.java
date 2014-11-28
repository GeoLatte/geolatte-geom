package org.geolatte.geom.codec;

/**
 * An exception for coordinate systems (as defined by their axes) that are not compatible with a given {@code
 * CoordinateReferenceSystem}.
 * <p/>
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class UnsupportedCoordinateSystemException extends RuntimeException {

    public UnsupportedCoordinateSystemException() {
        super();
    }

    public UnsupportedCoordinateSystemException(String message) {
        super(message);
    }

}
