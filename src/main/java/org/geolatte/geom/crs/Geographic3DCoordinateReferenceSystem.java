package org.geolatte.geom.crs;

import org.geolatte.geom.G3D;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 30/11/14.
 */
public class Geographic3DCoordinateReferenceSystem extends GeographicCoordinateReferenceSystem<G3D> {



    /**
     * Constructs a 3-Dimensional geographic coordinate reference system.
     *
     * @param crsId            the {@link org.geolatte.geom.crs.CrsId} that identifies this
     *                         <code>CoordinateReferenceSystem</code> uniquely
     * @param name             the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param coordinateSystem the coordinate system to use  @throws java.lang.IllegalArgumentException if less than
     *                         two {@link org.geolatte.geom.crs.CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public Geographic3DCoordinateReferenceSystem(CrsId crsId, String name, EllipsoidalCoordinateSystem3D coordinateSystem) {
        super(crsId, name, coordinateSystem);
    }

}
