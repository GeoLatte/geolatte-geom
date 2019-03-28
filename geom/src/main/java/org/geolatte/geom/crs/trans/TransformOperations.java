package org.geolatte.geom.crs.trans;

import org.geolatte.geom.Position;
import org.geolatte.geom.crs.*;

import java.util.Optional;

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
    private CoordinateOperation op;

    DefaultTransformOperation(CoordinateReferenceSystem<P> source, CoordinateReferenceSystem<Q> target) {
        this.source = source;
        this.target = target;

        SingleCoordinateReferenceSystem<?> sourceBase;
        VerticalCoordinateReferenceSystem sourceVertical = null;
        LinearCoordinateReferenceSystem sourceLinear = null;
        if (source.isCompound()) {
            CompoundCoordinateReferenceSystem<P> cSource = (CompoundCoordinateReferenceSystem<P>) source;
            sourceBase = cSource.getBase();
            sourceVertical = cSource.getVertical().orElse(null);
            sourceLinear = cSource.getLinear().orElse(null);
        } else {
            sourceBase = (SingleCoordinateReferenceSystem<?>) source;
        }

        SingleCoordinateReferenceSystem<?> targetBase;
        VerticalCoordinateReferenceSystem targetVertical = null;
        LinearCoordinateReferenceSystem targetLinear = null;
        if (target.isCompound()) {
            CompoundCoordinateReferenceSystem<P> cTarget = (CompoundCoordinateReferenceSystem<P>) target;
            targetBase = cTarget.getBase();
            targetVertical = cTarget.getVertical().orElse(null);
            targetLinear = cTarget.getLinear().orElse(null);
        } else {
            targetBase = (SingleCoordinateReferenceSystem<?>) target;
        }
        op = CoordinateOperations.transform(sourceBase, targetBase);

        if (targetVertical != null || sourceVertical != null) {
            op = extend(op, sourceVertical, targetVertical);
        }
        if (targetLinear != null || sourceLinear != null) {
            op = extend(op, sourceLinear, targetLinear);
        }

    }

    DefaultTransformOperation(CoordinateReferenceSystem<P> source, CoordinateReferenceSystem<Q> target, CoordinateOperation op){
        this.op = op;
        this.source = source;
        this.target = target;
    }


    private CoordinateOperation extend(final CoordinateOperation baseOp,
                                       final OneDimensionCoordinateReferenceSystem sourceVertical,
                                       final OneDimensionCoordinateReferenceSystem targetVertical ) {

        return new ExtendedCoordinateOperation(baseOp, sourceVertical, targetVertical);
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

    @Override
    public TransformOperation<Q, P> reversed() {
        return new DefaultTransformOperation<Q,P>(this.target, this.source, this.op.reversed());
    }
}


/**
 * Extends a CoordinateOperation by the stacking an operation between  1-dimensional reference systems on top of this.
 *
 * Operations need to added according to normal form (first for Z-coordinate, then M-coordinate).
 */
class ExtendedCoordinateOperation implements CoordinateOperation {
    private final CoordinateOperation baseOp;
    private OneDimensionCoordinateReferenceSystem source;
    private OneDimensionCoordinateReferenceSystem target;
    private final int inCoDim;
    private final int outCoDim;
    double convFactor = 1.0;

    ExtendedCoordinateOperation(CoordinateOperation baseOp, OneDimensionCoordinateReferenceSystem source, final OneDimensionCoordinateReferenceSystem target) {
        this.baseOp = baseOp;
        this.source = source;
        this.target = target;
        this.inCoDim =  (source != null) ? baseOp.inCoordinateDimension() +1 :  baseOp.inCoordinateDimension();
        this.outCoDim = target != null ? baseOp.outCoordinateDimension() +1 : baseOp.outCoordinateDimension();
        if(source != null && target != null) {
            convFactor = source.getUnit().getConversionFactor() / target.getUnit().getConversionFactor();
        }
    }

    @Override
    public boolean isReversible() {
        return true;
    }

    @Override
    public int inCoordinateDimension() {
        return inCoDim;
    }

    @Override
    public int outCoordinateDimension() {
        return outCoDim;
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {
        baseOp.forward(inCoordinate, outCoordinate);
        if (source == null && target != null) {
            outCoordinate[outCoDim - 1] = 0d;
        }

        if (source != null && target != null ) {
            outCoordinate[outCoDim - 1] = convFactor * inCoordinate[inCoDim -1];
        }
    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        if (target == null && source != null) {
            outCoordinate[inCoDim - 1] = 0d;
        }

        if (source != null && target != null ) {
            outCoordinate[inCoDim-1] = inCoordinate[outCoDim -1] / convFactor;
        }
    }
}