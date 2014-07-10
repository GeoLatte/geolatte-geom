package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P2D extends Position {


    public P2D() {
        super();
    }

    public P2D(double x, double y) {
        super(x, y);
    }

    protected P2D(double... coords) {
        super(coords);
    }

    @Override
    public int getCoordinateDimension() {
        return 2;
    }

    public double getX() {
        return getCoordinate(0);
    }

    public double getY() {
        return getCoordinate(1);
    }

}
