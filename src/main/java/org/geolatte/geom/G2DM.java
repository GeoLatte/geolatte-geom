package org.geolatte.geom;

/**
 * A geographic {@code Position} with a measure value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G2DM extends G2D implements Measured {


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
    public int getCoordinateDimension() {
        return 3;
    }
}
