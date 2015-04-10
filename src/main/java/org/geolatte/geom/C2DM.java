package org.geolatte.geom;

/**
 * A cartesian {@code Position} with a measure value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class C2DM extends C2D implements Measured {


    public C2DM() {
        super();
    }

    public C2DM(double x, double y, double m) {
        super(x, y, m);
    }

    @Override
    public int getCoordinateDimension() {
        return 3;
    }

    @Override
    public double getM() {
        return getCoordinate(2);
    }


}
