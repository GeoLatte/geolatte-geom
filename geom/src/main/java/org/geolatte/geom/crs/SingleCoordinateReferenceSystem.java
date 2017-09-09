package org.geolatte.geom.crs;

import org.geolatte.geom.Position;

/**
 * A Coordinate reference system consisting of one Coordinate System and one Datum (as opposed to a Compound CRS)
 *
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
abstract public class SingleCoordinateReferenceSystem<P extends Position> extends CoordinateReferenceSystem<P>{

    /**
     * Constructs a <code>HorizontalCoordinateReferenceSystem</code>.
     *
     * @param crsId            the {@link org.geolatte.geom.crs.CrsId} that identifies this
     *                         <code>CoordinateReferenceSystem</code> uniquely
     * @param name             the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param coordinateSystem the coordinate system to use  @throws java.lang.IllegalArgumentException if less than
     *                         two {@link org.geolatte.geom.crs.CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public SingleCoordinateReferenceSystem(CrsId crsId, String name, CoordinateSystem<P>
            coordinateSystem) {
        super(crsId, name, coordinateSystem);
    }

    @Override
    public boolean isCompound() {
       return false;
    }


}
