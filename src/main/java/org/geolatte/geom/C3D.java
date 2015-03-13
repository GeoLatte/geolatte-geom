package org.geolatte.geom;

/**
 * A cartesian {@code Position} having an z-value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class C3D extends C2D {

    public C3D() {
        super();
    }

    public C3D(double x, double y, double z) {
        super(x, y, z);
    }

    protected C3D(double... coords) {
        super(coords);
    }

    public double getZ() {
        return getCoordinate(2);
    }

    @Override
    public int getCoordinateDimension() {
        return 3;
    }
}
