package org.geolatte.geom;

/**
 * An Interface for {@code Position}s that have a vertical dimension (i.e. have an altitude).
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public interface Vertical {

    /**
     * returns the altitude of this instance.
     *
     * @return the altitude of this instance.
     */
    double getAltitude();

}
