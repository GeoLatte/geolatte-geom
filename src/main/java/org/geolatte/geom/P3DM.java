package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P3DM extends P3D implements Measured {

    public final static PositionTypeDescriptor<P3DM> descriptor = new PositionTypeDescriptor<>(P3DM.class,
            4, 2, 3);

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
    public PositionTypeDescriptor<? extends P3DM> getDescriptor() {
        return descriptor;
    }

}
