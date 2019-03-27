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
    final private CoordinateOperation op;

    DefaultTransformOperation(CoordinateReferenceSystem<P> source, CoordinateReferenceSystem<Q> target) {
        this.source = source;
        this.target = target;

        SingleCoordinateReferenceSystem<?> sourceBase;
        Optional<VerticalCoordinateReferenceSystem> sourceVertical = Optional.empty();
        Optional<LinearCoordinateReferenceSystem> sourceLinear = Optional.empty();
        if (source.isCompound()) {
            CompoundCoordinateReferenceSystem<P> cSource = (CompoundCoordinateReferenceSystem<P>) source;
            sourceBase = cSource.getBase();
            sourceVertical = cSource.getVertical();
            sourceLinear = cSource.getLinear();
        } else {
            sourceBase = (SingleCoordinateReferenceSystem<?>) source;
        }

        SingleCoordinateReferenceSystem<?> targetBase;
        Optional<VerticalCoordinateReferenceSystem> targetVertical = Optional.empty();
        Optional<LinearCoordinateReferenceSystem> targetLinear = Optional.empty();
        if (target.isCompound()) {
            CompoundCoordinateReferenceSystem<P> cTarget = (CompoundCoordinateReferenceSystem<P>) target;
            targetBase = cTarget.getBase();
            targetVertical = cTarget.getVertical();
            targetLinear = cTarget.getLinear();
        } else {
            targetBase = (SingleCoordinateReferenceSystem<?>) target;
        }
        CoordinateOperation baseOp = CoordinateOperations.transform(sourceBase, targetBase);
        CoordinateOperation vBaseOp = extend(baseOp, sourceVertical.orElse(null), targetVertical.orElse(null));

        this.op = vBaseOp;

    }


    private CoordinateOperation extend(final CoordinateOperation baseOp,
                                       final VerticalCoordinateReferenceSystem sourceVertical,
                                       final VerticalCoordinateReferenceSystem targetVertical ) {


        final VerticalCoordinateOperation vOp = new VerticalCoordinateOperation(sourceVertical, targetVertical, baseOp.inCoordinateDimension(), baseOp.outCoordinateDimension());

        return new CoordinateOperation(){

            @Override
            public boolean isReversible() {
                return true;
            }

            @Override
            public int inCoordinateDimension() {
                return vOp.inCoordinateDimension();
            }

            @Override
            public int outCoordinateDimension() {
                return vOp.outCoordinateDimension();
            }

            @Override
            public void forward(double[] inCoordinate, double[] outCoordinate) {
                baseOp.forward(inCoordinate, outCoordinate);
                vOp.forward(inCoordinate, outCoordinate);
            }

            @Override
            public void reverse(double[] inCoordinate, double[] outCoordinate) {

            }
        };
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


class VerticalCoordinateOperation implements CoordinateOperation {
    private int sourceStart;
    private int targetStart;
    private VerticalCoordinateReferenceSystem sourceVertical;
    private VerticalCoordinateReferenceSystem targetVertical;
    double convFactor = 1.0;

    VerticalCoordinateOperation(VerticalCoordinateReferenceSystem sourceVertical, final VerticalCoordinateReferenceSystem targetVertical, int sourceStart, int targetStart) {
        this.sourceVertical = sourceVertical;
        this.targetVertical = targetVertical;
        this.sourceStart = sourceStart;
        this.targetStart = targetStart;
        if(sourceVertical != null && targetVertical != null) {
            convFactor = sourceVertical.getUnit().getConversionFactor() / targetVertical.getUnit().getConversionFactor();
        }
    }

    @Override
    public boolean isReversible() {
        return true;
    }

    @Override
    public int inCoordinateDimension() {
        return sourceVertical != null ? sourceStart+1 :  sourceStart;
    }

    @Override
    public int outCoordinateDimension() {
        return targetVertical != null ? targetStart+1 : targetStart;
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {

        if (sourceVertical == null && targetVertical != null) {
            outCoordinate[targetStart] = 0d;
        }

        if (sourceVertical != null && targetVertical != null ) {
            outCoordinate[targetStart] = convFactor * inCoordinate[sourceStart];
        }

    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        if (targetVertical == null && sourceVertical != null) {
            outCoordinate[sourceStart] = 0d;
        }

        if (sourceVertical != null && targetVertical != null ) {
            outCoordinate[sourceStart] = inCoordinate[targetStart] / convFactor;
        }
    }
}