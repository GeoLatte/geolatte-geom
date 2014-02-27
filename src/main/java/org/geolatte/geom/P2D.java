package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P2D extends Projected<P2D> {


    public P2D(CoordinateReferenceSystem<P2D> crs) {
        super(crs);
    }

    public P2D(CoordinateReferenceSystem<P2D> crs, double x, double y) {
        super(crs, x, y);
    }


}
