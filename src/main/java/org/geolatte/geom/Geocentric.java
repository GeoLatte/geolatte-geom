package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A marker interface for Geocentric {@code Position}s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/6/14
 */
public abstract class Geocentric<G extends Geocentric<G>> extends Position<G> {

    public Geocentric(CoordinateReferenceSystem<G> crs, double... coords) {
        super(crs, coords);
    }

    public double getX() {
        return getCoordinate(0);
    }

    public double getY() {
        return getCoordinate(1);
    }

    public double getZ() {
        return getCoordinate(2);
    }
}
