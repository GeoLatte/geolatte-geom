package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Marker interface for geographic (geodetic) positions.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/6/14
 */
public abstract class Geographic<G extends Geographic<G>> extends Position<G> {

    public Geographic(CoordinateReferenceSystem<G> crs, double... coords) {
        super(crs, coords);
    }

    public double getLon() {
        return getCoordinate(0);
    }

    public double getLat() {
        return getCoordinate(1);
    }

}
