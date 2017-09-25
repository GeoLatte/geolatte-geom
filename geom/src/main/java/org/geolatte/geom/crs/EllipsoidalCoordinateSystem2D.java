package org.geolatte.geom.crs;

import org.geolatte.geom.G2D;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by Karel Maesen, Geovise BVBA on 28/11/14.
 */
public class EllipsoidalCoordinateSystem2D extends CoordinateSystem<G2D> {

    private final static List<Integer> REQUIRED_AXIS_NORMAL_ORDER = Arrays.asList(0, 1);

    public EllipsoidalCoordinateSystem2D(EllipsoidalAxis first, EllipsoidalAxis second) {
        super(first, second);
        checkAxes();
    }

    private void checkAxes() {
        List<Integer> order = getAxisNormalOrder();
        if (!order.containsAll(REQUIRED_AXIS_NORMAL_ORDER)) {
            throw new IllegalArgumentException("Require order 0 and 1 axes");
        }
    }

    @Override
    public Class<G2D> getPositionClass() {
        return G2D.class;
    }

    @Override
    public CoordinateSystem<? extends G2D> merge(OneDimensionCoordinateSystem<?> coordinateSystem) {
        CoordinateSystemAxis axis = coordinateSystem.getAxis();
        return extend(axis);
    }

    @Override
    public CoordinateSystem<? extends G2D> extend(CoordinateSystemAxis axis) {
        if (axis instanceof EllipsoidalHeightCSAXis) {
            return new EllipsoidalCoordinateSystem3D((EllipsoidalAxis) getAxis(0), (EllipsoidalAxis) getAxis(1),
                    (EllipsoidalHeightCSAXis) axis);
        }
        if (axis instanceof MeasureStraightLineAxis) {
            return new EllipsoidalCoordinateSystem2DM((EllipsoidalAxis) getAxis(0), (EllipsoidalAxis) getAxis(1),
                    (MeasureStraightLineAxis) axis);
        }
        if (axis instanceof VerticalStraightLineAxis) {
            return new EllipsoidalCoordinateSystem3D((EllipsoidalAxis) getAxis(0), (EllipsoidalAxis) getAxis(1),
                    (VerticalStraightLineAxis) axis);
        }
        throw new UnsupportedOperationException();
    }

}
