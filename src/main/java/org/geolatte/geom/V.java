package org.geolatte.geom;

/**
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class V extends Position{

    /**
     * Constructs an empty instance
     */
    public V() {
        super();
    }

    /**
     * Constructs an instance with the specified vertical value
     *
     * @param value the vertical value
     */
    public V(double value) {
        super(value);
    }

    /**
     * Constructs an instance from the specified coordinate.
     *
     * @param coords
     */
    protected V(double... coords) {
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
