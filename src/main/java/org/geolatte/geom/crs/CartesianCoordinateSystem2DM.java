package org.geolatte.geom.crs;

import org.geolatte.geom.P2DM;
import org.geolatte.geom.P3DM;
import org.geolatte.geom.Position;

import java.util.Arrays;
import java.util.List;

/**
 * A Two-dimensional Cartesian Coordinate system extended with a {@code LinearCoordinateSystemAxis}
 * <p/>
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class CartesianCoordinateSystem2DM extends CoordinateSystem<P2DM> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1, 3);

    public CartesianCoordinateSystem2DM(StraightLineAxis firstAxis, StraightLineAxis secondAxis,
                                        MeasureStraightLineAxis thirdAxis) {
        super(firstAxis, secondAxis, thirdAxis);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (!order.containsAll(REQUIRED_AXIS_NORMAL_ORDER)) {
            throw new IllegalArgumentException("Require order 0, 1 and 3 axes");
        }
    }

    @Override
    public Class<P2DM> getPositionClass() {
        return P2DM.class;
    }

    @Override
    public CoordinateSystem<P3DM> merge(OneDimensionCoordinateSystem<?> coordinateSystem) {
        CoordinateSystemAxis axis = coordinateSystem.getAxis();
        return extend(axis);
    }

    @Override
    public CoordinateSystem<P3DM> extend(CoordinateSystemAxis axis) {
        if (axis instanceof VerticalStraightLineAxis) {
            return new CartesianCoordinateSystem3DM((StraightLineAxis) getAxis(0), (StraightLineAxis) getAxis(1),
                    (VerticalStraightLineAxis) axis, (MeasureStraightLineAxis) getAxis(1));
        }
        throw new UnsupportedOperationException();
    }

}