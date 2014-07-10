package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G2D extends Position {

    public G2D() {
        super();
    }

    public G2D(double lon, double lat) {
        super(lon, lat);
    }

    protected G2D(double... coords) {
        super(coords);
    }

    @Override
    public int getCoordinateDimension() {
        return 2;
    }

    public double getLon() {
        return getCoordinate(0);
    }

    public double getLat() {
        return getCoordinate(1);
    }
}
