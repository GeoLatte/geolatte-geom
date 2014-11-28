package org.geolatte.geom.crs;

import org.geolatte.geom.P3D;
import org.geolatte.geom.P3DM;
import org.geolatte.geom.Position;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class CartesianCoordinateSystem3D extends CoordinateSystem<P3D> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1, 2);

    public CartesianCoordinateSystem3D(StraightLineAxis firstAxis, StraightLineAxis secondAxis,
                                       VerticalStraightLineAxis thirdAxis) {
        super(firstAxis, secondAxis, thirdAxis);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (!order.containsAll(REQUIRED_AXIS_NORMAL_ORDER)) {
            throw new IllegalArgumentException("Require order 0, 1 and 2 axes");
        }
    }


    @Override
    public Class<P3D> getPositionClass() {
        return P3D.class;
    }

    @Override
    public CoordinateSystem<?> merge(OneDimensionCoordinateSystem<?> coordinateSystem) {
        CoordinateSystemAxis axis = coordinateSystem.getAxis();
        return extend(axis);
    }

    @Override
    public CoordinateSystem<P3DM> extend(CoordinateSystemAxis axis) {
        if (axis instanceof MeasureStraightLineAxis) {
            return new CartesianCoordinateSystem3DM((StraightLineAxis) getAxis(0), (StraightLineAxis) getAxis(1),
                    (VerticalStraightLineAxis) getAxis(2), (MeasureStraightLineAxis) axis);
        }
        throw new UnsupportedOperationException();
    }


}
