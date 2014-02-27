package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G2D extends Geographic<G2D> {

    public G2D(CoordinateReferenceSystem<G2D> crs) {
        super(crs);
    }

    public G2D(CoordinateReferenceSystem<G2D> crs, double lon, double lat) {
        super(crs, lon, lat);
    }

}
