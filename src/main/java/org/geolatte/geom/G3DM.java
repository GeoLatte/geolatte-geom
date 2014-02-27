package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G3DM extends Geographic<G3DM> implements Vertical, Measured {

    public G3DM(CoordinateReferenceSystem<G3DM> crs) {
        super(crs);
    }

    public G3DM(CoordinateReferenceSystem<G3DM> crs, double lon, double lat, double alt, double m) {
        super(crs, lon, lat, alt, m);
    }

    @Override
    public double getLon() {
        return getCoordinate(0);
    }

    @Override
    public double getLat() {
        return getCoordinate(1);
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
