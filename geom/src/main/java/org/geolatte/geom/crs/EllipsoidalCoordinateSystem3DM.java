package org.geolatte.geom.crs;

import org.geolatte.geom.G3DM;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class EllipsoidalCoordinateSystem3DM extends CoordinateSystem<G3DM> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1, 2, 3);

    public EllipsoidalCoordinateSystem3DM(EllipsoidalAxis first, EllipsoidalAxis second, StraightLineAxis heightAxis,
                                          MeasureStraightLineAxis other) {
        super(first, second, heightAxis, other);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (!order.containsAll(REQUIRED_AXIS_NORMAL_ORDER)) {
            throw new IllegalArgumentException("Require order 0, 1, 2 and 3 axes");
        }
    }

    @Override
    public Class<G3DM> getPositionClass() {
        return G3DM.class;
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
