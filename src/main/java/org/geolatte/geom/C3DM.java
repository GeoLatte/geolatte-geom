package org.geolatte.geom;

/**
 * A cartesian {@code Position} with both an z and a measure value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class C3DM extends C3D implements Measured {

    public C3DM() {
        super();
    }

    public C3DM(double x, double y, double z, double m) {
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
