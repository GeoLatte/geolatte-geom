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

    public G2D() {
        super();
    }

    public G2D(double lon, double lat) {
        super(lon, lat);
    }

    protected G2D(double... coords) {
        super(coords);
    }

    public double getLon() {
        return getCoordinate(0);
    }

    public double getLat() {
        return getCoordinate(1);
    }

    @Override
    public PositionTypeDescriptor<? extends G2D> getDescriptor() {
        return descriptor;
    }
}
