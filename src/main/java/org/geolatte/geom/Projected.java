package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A marker interface for Projected {@code Position}s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public abstract class Projected<P extends Projected<P>> extends Position<P> {

    public Projected(CoordinateReferenceSystem<P> crs, double... coords) {
        super(crs, coords);
    }

    public double getX() {
        return getCoordinate(0);
    }

    public double getY() {
        return getCoordinate(1);
    }

}
