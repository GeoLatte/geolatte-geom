package org.geolatte.geom.crs.trans;

import org.geolatte.geom.crs.CrsId;
import org.geolatte.geom.crs.Projection;

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

    public UnsupportedTransformException(CrsId crs, Projection projection) {
        super(String.format("Projection '%s' of %s is not supported", projection.getName(), crs));
    }


}
