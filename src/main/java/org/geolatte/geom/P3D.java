package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P3D extends Projected<P3D> implements Vertical {

    public P3D(CoordinateReferenceSystem<P3D> crs) {
        super(crs);
    }

    public P3D(CoordinateReferenceSystem<P3D> crs, double x, double y, double z) {
        super(crs, x, y, z);
    }

    @Override
    public double getAltitude() {
        return getCoordinate(2);
    }
}
