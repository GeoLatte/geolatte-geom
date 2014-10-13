package org.geolatte.geom;

/**
 * A {@code Position} in a geographic coordinate reference system.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class G2D extends Position {

    public final static PositionTypeDescriptor<G2D> descriptor = new PositionTypeDescriptor<>(G2D.class,
            2, -1, -1);

    /**
     * Constructs an empty instance
     */
    public G2D() {
        super();
    }

    /**
     * Constructs an instance with the specified longitude and latitude
     *
     * @param lon the longitude
     * @param lat the latitude
     */
    public G2D(double lon, double lat) {
        super(lon, lat);
    }

    /**
     * Constructs an instance from the specified coordinates.
     *
     * It expects first the longitude, then the latitude coordinate
     * @param coords
     */
    protected G2D(double... coords) {
        super(coords);
    }

    /**
     * Returns the longitude of this position
     * @return the longitude of this position
     */
    public double getLon() {
        return getCoordinate(0);
    }

    /**
     * Returns the latitude of this position
     * @return the latitude of this position
     */
    public double getLat() {
        return getCoordinate(1);
    }

    @Override
    public PositionTypeDescriptor<? extends G2D> getDescriptor() {
        return descriptor;
    }
}
