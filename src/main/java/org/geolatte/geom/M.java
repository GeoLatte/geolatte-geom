package org.geolatte.geom;

/**
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class M extends Position {

    /**
     * Constructs an empty instance
     */
    public M() {
        super();
    }

    /**
     * Constructs an instance with the specified longitude and latitude
     *
     * @param value the vertical value
     */
    public M(double value) {
        super(value);
    }

    /**
     * Constructs an instance from the specified coordinates.
     * <p/>
     * It expects first the longitude, then the latitude coordinate
     *
     * @param coords
     */
    protected M(double... coords) {
        super(coords);
    }

    public double getValue() {
        return getCoordinate(0);
    }

    @Override
    public int getCoordinateDimension() {
        return 1;
    }


}
