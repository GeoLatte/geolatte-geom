package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G3D extends Geographic<G3D> implements Vertical {

    public G3D(CoordinateReferenceSystem<G3D> crs) {
        super(crs);
    }

    public G3D(CoordinateReferenceSystem<G3D> crs, double lon, double lat, double alt) {
        super(crs, lon, lat, alt);
    }

    @Override
    public double getAltitude() {
        return getCoordinate(2);
    }

}
