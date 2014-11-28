package org.geolatte.geom;

/**
 * A geographic {@code Position} with both an altitude and a measure value.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G3DM extends G3D implements Measured {

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
    public int getCoordinateDimension() {
        return 4;
    }

}
