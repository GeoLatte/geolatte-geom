package org.geolatte.geom.crs.trans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class TransformationChain implements Transformation {

    final private Transformation[] transformations;
    private double[][] buffers;

    private TransformationChain(List<Transformation> transformations) {
        this.transformations = transformations.toArray(new Transformation[transformations.size()]);
        setupIntermediateBuffers();
    }

    private void setupIntermediateBuffers(){
        this.buffers = new double[this.transformations.length + 1][];
        for(int idx = 0; idx < transformations.length-1; idx++) {
            //is this safe?? If not, add explicit test when constructing the chain
            int dim = Math.max( transformations[idx].outCoordinateDimension(), transformations[idx+1].inCoordinateDimension() );
            this.buffers[idx+1] =new double[dim];
        }
    }

    @Override
    public boolean isReversible() {
        for(Transformation s : transformations) {
            if (!s.isReversible()) return false;
        }
        return true;
    }

    @Override
    public int inCoordinateDimension() {
        return transformations[0].inCoordinateDimension();
    }

    @Override
    public int outCoordinateDimension() {
        return transformations[transformations.length -1].outCoordinateDimension();
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {
        buffers[0] = inCoordinate;
        buffers[buffers.length - 1] = outCoordinate;
        for (int idx = 0; idx < transformations.length; idx++ ) {
                Transformation t = transformations[idx];
                t.forward(buffers[idx], buffers[idx + 1]);
        }
    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        buffers[0] = outCoordinate;
        buffers[buffers.length - 1] = inCoordinate;
        for (int idx = transformations.length-1; idx >= 0; idx-- ) {
            Transformation t = transformations[idx];
            t.reverse(buffers[idx+1], buffers[idx]);
        }
    }

    public static class Builder {

        private List<Transformation> transformations = new LinkedList<>();

        public Builder forward(Transformation t) {
            transformations.add(new Step(t, Step.FORWARD));
            return this;
        }

        public Builder reverse(Transformation t) {
            transformations.add(new Step(t, Step.REVERSE));
            return this;
        }

        public TransformationChain build() {
            return new TransformationChain(this.transformations);
        }
    }

    private static class Step implements Transformation {

        static final public int FORWARD = 1;
        static final public int REVERSE = 2;

        final Transformation transformation;
        final int direction;
        Step(Transformation t, int direction){
            this.transformation = t;
            this.direction = direction;
        }


        @Override
        public boolean isReversible() {
            return this.transformation.isReversible();
        }

        @Override
        public int inCoordinateDimension() {
            if (direction == FORWARD) {
                return this.transformation.inCoordinateDimension();
            } else {
                return this.transformation.outCoordinateDimension();
            }
        }

        @Override
        public int outCoordinateDimension() {
            if (direction == FORWARD) {
                return this.transformation.outCoordinateDimension();
            } else {
                return this.transformation.inCoordinateDimension();
            }
        }

        @Override
        public void forward(double[] inCoordinate, double[] outCoordinate) {
            if(direction == FORWARD) {
                this.transformation.forward(inCoordinate, outCoordinate);
            } else {
                this.transformation.reverse(inCoordinate, outCoordinate);
            }
        }

        @Override
        public void reverse(double[] inCoordinate, double[] outCoordinate) {
            if(direction == FORWARD) {
                this.transformation.reverse(inCoordinate, outCoordinate);
            } else {
                this.transformation.forward(inCoordinate, outCoordinate);
            }
        }
    }

}
