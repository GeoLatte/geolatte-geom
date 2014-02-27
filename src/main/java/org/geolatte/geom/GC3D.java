package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A Geocentric coordinate
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/5/14
 */
public class GC3D extends Geocentric<GC3D> {


    public GC3D(CoordinateReferenceSystem<GC3D> crs) {
        super(crs);
    }

    public GC3D(CoordinateReferenceSystem<GC3D> crs, double x, double y, double z) {
        super(crs, x, y, z);
    }

}
