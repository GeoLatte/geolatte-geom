package org.geolatte.geom.crs;

import org.geolatte.geom.G3D;
import org.geolatte.geom.G3DM;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class EllipsoidalCoordinateSystem3D extends CoordinateSystem<G3D> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1, 2);

    public EllipsoidalCoordinateSystem3D(EllipsoidalAxis first, EllipsoidalAxis second, StraightLineAxis heightAxis) {
        super(first, second, heightAxis);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (!order.containsAll(REQUIRED_AXIS_NORMAL_ORDER)) {
            throw new IllegalArgumentException("Require order 0, 1 and 2 axes");
        }
    }


    @Override
    public Class<G3D> getPositionClass() {
        return G3D.class;
    }

    @Override
    public CoordinateSystem<G3DM> merge(OneDimensionCoordinateSystem<?> coordinateSystem) {
        CoordinateSystemAxis axis = coordinateSystem.getAxis();
        return extend(axis);
    }

    @Override
    public CoordinateSystem<G3DM> extend(CoordinateSystemAxis axis) {
        if (axis instanceof MeasureStraightLineAxis) {
            return new EllipsoidalCoordinateSystem3DM((EllipsoidalAxis) getAxis(0), (EllipsoidalAxis) getAxis(1),
                    (StraightLineAxis) getAxis(2), (MeasureStraightLineAxis) axis);
        }
        throw new UnsupportedOperationException();
    }
}
