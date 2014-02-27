package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P2DM extends Projected<P2DM> implements Measured {


    public P2DM(CoordinateReferenceSystem<P2DM> crs) {
        super(crs);
    }

    public P2DM(CoordinateReferenceSystem<P2DM> crs, double x, double y, double m) {
        super(crs, x, y, m);
    }

    @Override
    public double getM() {
        return getCoordinate(2);
    }
}
