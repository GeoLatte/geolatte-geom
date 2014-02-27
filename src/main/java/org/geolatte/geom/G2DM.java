package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G2DM extends Geographic<G2DM> implements Measured {

    public G2DM(CoordinateReferenceSystem<G2DM> crs) {
        super(crs);
    }

    public G2DM(CoordinateReferenceSystem<G2DM> crs, double lon, double lat, double m) {
        super(crs, lon, lat, m);
    }

    @Override
    public double getM() {
        return getCoordinate(2);
    }
}
