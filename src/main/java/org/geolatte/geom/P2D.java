package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
public class P2D extends Position {

    public final static PositionTypeDescriptor<P2D> descriptor = new PositionTypeDescriptor<>(P2D.class,
            2, -1, -1);

    public P2D() {
        super();
    }

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

    public double getX() {
        return getCoordinate(0);
    }

    public double getY() {
        return getCoordinate(1);
    }

}
