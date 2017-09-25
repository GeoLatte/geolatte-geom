package org.geolatte.geom.crs;

import org.geolatte.geom.G2DM;
import org.geolatte.geom.G3DM;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class EllipsoidalCoordinateSystem2DM extends CoordinateSystem<G2DM> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1, 3);

    public EllipsoidalCoordinateSystem2DM(EllipsoidalAxis first, EllipsoidalAxis second, MeasureStraightLineAxis other) {
        super(first, second, other);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (!order.containsAll(REQUIRED_AXIS_NORMAL_ORDER)) {
            throw new IllegalArgumentException("Require order 0, 1 and 2 axes");
        }
    }

    @Override
    public Class<G2DM> getPositionClass() {
        return G2DM.class;
    }

    @Override
    public CoordinateSystem<?> merge(OneDimensionCoordinateSystem<?> coordinateSystem) {
        CoordinateSystemAxis axis = coordinateSystem.getAxis();
        return extend(axis);
    }

    @Override
    public CoordinateSystem<G3DM> extend(CoordinateSystemAxis axis) {
        if (axis instanceof EllipsoidalHeightCSAXis) {
            return new EllipsoidalCoordinateSystem3DM((EllipsoidalAxis) getAxis(0), (EllipsoidalAxis) getAxis(1),
                    (EllipsoidalHeightCSAXis) axis, (MeasureStraightLineAxis)getAxis(2));
        }
        if (axis instanceof VerticalStraightLineAxis) {
            return new EllipsoidalCoordinateSystem3DM((EllipsoidalAxis) getAxis(0), (EllipsoidalAxis) getAxis(1),
                    (VerticalStraightLineAxis) axis, (MeasureStraightLineAxis)getAxis(2));
        }
        throw new UnsupportedOperationException();
    }

}
