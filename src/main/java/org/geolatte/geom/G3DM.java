package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G3DM extends G3D implements Measured {

    public final static PositionTypeDescriptor<G3DM> descriptor = new PositionTypeDescriptor<>(G3DM.class,
            4, 2, 3);

    public G3DM() {
        super();
    }

    public G3DM(double lon, double lat, double alt, double m) {
        super(lon, lat, alt, m);
    }

    @Override
    public double getM() {
        return getCoordinate(3);
    }

    @Override
    public PositionTypeDescriptor<? extends G3DM> getDescriptor() {
        return descriptor;
    }

}
