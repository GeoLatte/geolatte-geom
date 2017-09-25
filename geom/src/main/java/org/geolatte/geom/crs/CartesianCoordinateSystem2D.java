package org.geolatte.geom.crs;

import org.geolatte.geom.C2D;

import java.util.Arrays;
import java.util.List;

/**
 * A Cartesian 2-Dimensional coordinate system.
 *
 * <p>A Cartesian coordinate system is a
 * coordinate system which gives the position of points relative to orthogonal straight axes. All axes shall have the same unit of measure.
 * </p>
 * <p/>
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class CartesianCoordinateSystem2D extends CoordinateSystem<C2D> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1);

    /**
     *
     * Constructs an instance
     *
     * @param first  the first axis
     * @param second the second axis
     */
    public CartesianCoordinateSystem2D(StraightLineAxis first, StraightLineAxis second) {
        super(first, second);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (! order.containsAll(REQUIRED_AXIS_NORMAL_ORDER) ) {
            throw new IllegalArgumentException("Require order 0 and 1 axes");
        }
    }


    @Override
    public Class<C2D> getPositionClass() {
        return C2D.class;
    }

    @Override
    public CoordinateSystem<? extends C2D> merge(OneDimensionCoordinateSystem<?> coordinateSystem) {
        CoordinateSystemAxis axis = coordinateSystem.getAxis();
        return extend(axis);
    }

    @Override
    public CoordinateSystem<? extends C2D> extend(CoordinateSystemAxis axis) {
        if (axis instanceof VerticalStraightLineAxis) {
            return new CartesianCoordinateSystem3D((StraightLineAxis)getAxis(0), (StraightLineAxis)getAxis(1), (VerticalStraightLineAxis)axis);
        }
        if (axis instanceof MeasureStraightLineAxis){
            return new CartesianCoordinateSystem2DM((StraightLineAxis)getAxis(0), (StraightLineAxis)getAxis(1), (MeasureStraightLineAxis)axis);
        }
        throw new UnsupportedOperationException();
    }
}
