package org.geolatte.geom;

/**
 * A position in a projected coordinate reference system.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P2D extends Position {

    public final static PositionTypeDescriptor<P2D> descriptor = new PositionTypeDescriptor<>(P2D.class,
            2, -1, -1);

    /**
     * Constructs an empty instance
     */
    public P2D() {
        super();
    }

    /**
     * Constructs an instance with the specified x (EASTING or WESTING) or y (NORTHING or SOUTHING) coordinates
     *
     * {@link org.geolatte.geom.crs.CoordinateReferenceSystem}, the </p>
     * @param x coordinate for axis in EAST or WEST direction
     * @param y coordinate for axis in NORTH or SOUTH direction
     */
    public P2D(double x, double y) {
        super(x, y);
    }

    protected P2D(double... coords) {
        super(coords);
    }

    @Override
    public PositionTypeDescriptor<? extends P2D> getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the coordinate for EAST (WEST) axis direction
     *
     * @return the coordinate for EAST (WEST) axis direction
     */
    public double getX() {
        return getCoordinate(0);
    }

    /**
     * Returns the coordinate for NORTH (SOUTH) axis direction
     *
     * @return the coordinate for NORTH (SOUTH) axis direction
     */
    public double getY() {
        return getCoordinate(1);
    }

}
