package org.geolatte.geom.crs.trans;

import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CompoundCoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.SingleCoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-27.
 */
public class TransformOperations {


   public static <P extends Position, Q extends Position> TransformOperation<P,Q> from(CoordinateReferenceSystem<P> source, CoordinateReferenceSystem<Q> target){
       return new DefaultTransformOperation<>(source, target);
   }
}

class DefaultTransformOperation<P extends Position, Q extends Position> implements TransformOperation<P,Q> {

    final private CoordinateReferenceSystem<P> source;
    final private CoordinateReferenceSystem<Q> target;
    final private CoordinateOperation op;

    DefaultTransformOperation(CoordinateReferenceSystem<P> source, CoordinateReferenceSystem<Q> target) {
        this.source = source;
        this.target = target;

        SingleCoordinateReferenceSystem<?> sourceBase;
        if (source.isCompound()) {
             sourceBase = ((CompoundCoordinateReferenceSystem<P>) source).headCs();
        } else {
            sourceBase = (SingleCoordinateReferenceSystem<?>) source;
        }

        SingleCoordinateReferenceSystem<?> targetBase;
        if (target.isCompound()) {
            targetBase = ((CompoundCoordinateReferenceSystem<P>) target).headCs();
        } else {
            targetBase = (SingleCoordinateReferenceSystem<?>) target;
        }

        this.op = CoordinateOperations.transform(sourceBase, targetBase);
    }

    @Override
    public CoordinateReferenceSystem<P> getSource() {
        return this.source;
    }

    @Override
    public CoordinateReferenceSystem<Q> getTarget() {
        return this.target;
    }

    @Override
    public CoordinateOperation getOperation() {
        return this.op;
    }
}
