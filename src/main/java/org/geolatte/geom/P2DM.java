package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P2DM extends P2D implements Measured {


    public P2DM() {
        super();
    }

    public P2DM(double x, double y, double m) {
        super(x, y, m);
    }

    @Override
    public double getM() {
        return getCoordinate(2);
    }

    @Override
    public int getCoordinateDimension() {
        return 3;
    }
}
