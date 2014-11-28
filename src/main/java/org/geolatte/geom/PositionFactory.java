package org.geolatte.geom;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public interface PositionFactory<P extends Position> {

    public Class<P> forClass();

    public int getCoordinateDimension();

    public P mkPosition(double... coordinates);

    //the next three methods are required for interoperability with JTS coordinates

    public boolean hasZComponent();

    public boolean hasMComponent();

    public int getMComponentIndex();


}
