package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P3DM extends Projected<P3DM> implements Vertical, Measured {

    public P3DM(CoordinateReferenceSystem<P3DM> crs) {
        super(crs);
    }

    public P3DM(CoordinateReferenceSystem<P3DM> crs, double x, double y, double z, double m) {
        super(crs, x, y, z, m);
    }

    @Override
    public double getAltitude() {
        return getCoordinate(2);
    }

    @Override
    public double getM() {
        return getCoordinate(3);
    }
}
