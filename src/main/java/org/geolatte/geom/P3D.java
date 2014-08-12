package org.geolatte.geom;

/**
 * A projected {@code Position} having an altitude.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P3D extends P2D implements Vertical {

    public final static PositionTypeDescriptor<P3D> descriptor = new PositionTypeDescriptor<>(P3D.class,
            3, 2, -1);

    public P3D() {
        super();
    }

    public P3D(double x, double y, double z) {
        super(x, y, z);
    }

    protected P3D(double... coords) {
        super(coords);
    }

    @Override
    public double getAltitude() {
        return getCoordinate(2);
    }

    @Override
    public PositionTypeDescriptor<? extends P3D> getDescriptor() {
        return descriptor;
    }
}
