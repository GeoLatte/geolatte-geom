package org.geolatte.geom.crs.trans;

/**
 * Low-level transformation interface
 *
 * Created by Karel Maesen, Geovise BVBA on 20/07/17.
 */
public interface CoordinateOperation {

    boolean isReversible();

    int inCoordinateDimension();

    int outCoordinateDimension();

    void forward(double[] inCoordinate, double[] outCoordinate);

    void reverse(double[] inCoordinate, double[] outCoordinate);

}
