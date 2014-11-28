package org.geolatte.geom;

/**
 * A projected {@code Position} with both an altitude and a measure value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P3DM extends P3D implements Measured {

    public P3DM() {
        super();
    }

    public P3DM(double x, double y, double z, double m) {
        super(x, y, z, m);
    }

    @Override
    public double getM() {
        return getCoordinate(3);
    }

    @Override
    public int getCoordinateDimension() {
        return 4;
    }
}
