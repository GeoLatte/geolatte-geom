package org.geolatte.geom.crs;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-24.
 */
public class UnsupportedCoordinateReferenceSystem extends RuntimeException {

    public UnsupportedCoordinateReferenceSystem(CrsId crsId) {
        super(String.format("CRS %s is not supported", crsId));
    }
}
