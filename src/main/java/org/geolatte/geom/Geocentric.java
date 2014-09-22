package org.geolatte.geom;

/**
 * A {@code Position} in a Geocentric Coordinate Reference System.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 3/6/14
 */
public abstract class Geocentric extends Position {

    public final static PositionTypeDescriptor<Geocentric> descriptor = new PositionTypeDescriptor<>(Geocentric.class,
            3, 2, -1);

    public Geocentric() {
        super();
    }

    public Geocentric(double x, double y, double z) {
        super(x, y, z);
    }

    public double getX() {
        return getCoordinate(0);
    }

    public double getY() {
        return getCoordinate(1);
    }

    public double getZ() {
        return getCoordinate(2);
    }

    @Override
    public PositionTypeDescriptor<? extends Geocentric> getDescriptor() {
        return descriptor;
    }
}
