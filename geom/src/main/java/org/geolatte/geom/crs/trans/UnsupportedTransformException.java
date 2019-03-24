package org.geolatte.geom.crs.trans;

import org.geolatte.geom.crs.CrsId;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-24.
 */
public class UnsupportedTransformException extends RuntimeException {

    public UnsupportedTransformException(CrsId source, CrsId target) {
        super(String.format("Can't find transform from %s to %s", source, target));
    }

    public UnsupportedTransformException(CrsId method) {
        super(String.format("Can't find operation method with id %s", method));
    }


}
