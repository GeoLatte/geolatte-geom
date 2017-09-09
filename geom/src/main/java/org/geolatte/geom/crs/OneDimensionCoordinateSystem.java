package org.geolatte.geom.crs;

import org.geolatte.geom.Position;

/**
 *
 * Created by Karel Maesen, Geovise BVBA on 29/11/14.
 */
public class OneDimensionCoordinateSystem<P extends Position> extends CoordinateSystem<P>{

    private final Class<P> posType;

    public OneDimensionCoordinateSystem(StraightLineAxis axis, Class<P> posType){
        super(axis);
        this.posType = posType;
    }


    /**
     * Returns the single axis in the coordinate system.
     * @return
     */
    public CoordinateSystemAxis getAxis() {
        return getAxis(0);
    }

    @Override
    public Class<P> getPositionClass() {
        return posType;
    }

    @Override
    public CoordinateSystem<?> merge(OneDimensionCoordinateSystem<?> coordinateSystem) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CoordinateSystem<?> extend(CoordinateSystemAxis axis) {
        throw new UnsupportedOperationException();
    }
}
