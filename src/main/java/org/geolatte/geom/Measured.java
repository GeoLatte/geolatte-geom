package org.geolatte.geom;

/**
 * Interface for {@code Position}s that have a "measure" value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public interface Measured {

    /**
     * Returns the measure value of this instance.
     *
     * @return the measure value of this instance.
     */
    double getM();

}
