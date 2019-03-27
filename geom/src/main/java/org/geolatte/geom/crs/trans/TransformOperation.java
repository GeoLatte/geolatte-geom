package org.geolatte.geom.crs.trans;

import org.geolatte.geom.Position;
import org.geolatte.geom.Positions;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-27.
 */
public interface TransformOperation<P extends Position, Q extends Position> {

    CoordinateReferenceSystem<P> getSource();
    CoordinateReferenceSystem<Q> getTarget();

    CoordinateOperation getOperation();

    default Q forward(P pos) {
        double[] out = pos.toArray(null);
        getOperation().forward(pos.toArray(null), out);
        return Positions.mkPosition(getTarget().getPositionClass(), out);
    }

    default P reverse(Q pos) {
        double[] out = pos.toArray(null);
        getOperation().reverse(pos.toArray(null), out);
        return Positions.mkPosition(getSource().getPositionClass(), out);
    }

}
