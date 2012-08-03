package org.geolatte.geom;

/**
 * A <code>PointCollection</code> that contains other <code>PointCollection</code>s.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 7/25/12
 */
public interface ComplexPointCollection extends PointCollection, Iterable<PointCollection> {

    /**
     * Returns the constituent <code>PointCollection</code>s of this instance.
     *
     * @return an array containing the constituent <code>PointCollection</code>s of this instance.
     */
    PointCollection[] getPointSets();

}
