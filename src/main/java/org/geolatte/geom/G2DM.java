package org.geolatte.geom;

/**
 * A geographic {@code Position} with a measure value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G2DM extends G2D implements Measured {

    public final static PositionTypeDescriptor<G2DM> descriptor = new PositionTypeDescriptor<>(G2DM.class,
            3, -1, 2);

    public G2DM() {
        super();
    }

    public G2DM(double lon, double lat, double m) {
        super(lon, lat, m);
    }

    @Override
    public double getM() {
        return getCoordinate(2);
    }

    @Override
    public PositionTypeDescriptor<? extends G2DM> getDescriptor() {
        return descriptor;
    }
}
