package org.geolatte.geom;

/**
 * A geographic {@code Position} with an altitude.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G3D extends G2D implements Vertical {

    public G3D() {
        super();
    }

    public G3D(double lon, double lat, double alt) {
        super(lon, lat, alt);
    }

    protected G3D(double... coords) {
        super(coords);
    }

    @Override
    public double getAltitude() {
        return getCoordinate(2);
    }

    @Override
    public int getCoordinateDimension() {
        return 3;
    }
}
