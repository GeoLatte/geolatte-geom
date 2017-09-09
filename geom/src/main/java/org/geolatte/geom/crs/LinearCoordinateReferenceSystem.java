package org.geolatte.geom.crs;

import org.geolatte.geom.M;

/**
 * A 1-Dimensional Linear Coordinate Ssytem that consists of the points that lie on the axis of a linear
 * feature, for example a pipeline or a road.
 *
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class LinearCoordinateReferenceSystem extends SingleCoordinateReferenceSystem<M>{

    public LinearCoordinateReferenceSystem(String name, MeasureStraightLineAxis axis) {
        super(CrsId.UNDEFINED, name, new OneDimensionCoordinateSystem<M>(axis, M.class));
    }

}
