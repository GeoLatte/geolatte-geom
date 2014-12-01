package org.geolatte.geom.crs;

import org.geolatte.geom.C3DM;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class CartesianCoordinateSystem3DM extends CoordinateSystem<C3DM> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1, 2, 3);

    public CartesianCoordinateSystem3DM(StraightLineAxis firstAxis, StraightLineAxis secondAxis, VerticalStraightLineAxis
            thirdAxis, MeasureStraightLineAxis fourthAxis) {
        super(firstAxis, secondAxis, thirdAxis, fourthAxis);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (!order.containsAll(REQUIRED_AXIS_NORMAL_ORDER)) {
            throw new IllegalArgumentException("Require order 0, 1, 2 and 3 axes");
        }
    }

    @Override
    public Class<C3DM> getPositionClass() {
        return C3DM.class;
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
