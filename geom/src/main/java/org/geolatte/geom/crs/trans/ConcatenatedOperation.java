package org.geolatte.geom.crs.trans;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 21/07/17.
 */
public class ConcatenatedOperation implements CoordinateOperation {

    final private CoordinateOperation[] coordinateOperations;
    private double[][] buffers;

    private ConcatenatedOperation(List<CoordinateOperation> coordinateOperations) {
        this.coordinateOperations = coordinateOperations.toArray( new CoordinateOperation[coordinateOperations.size()]);
        setupIntermediateBuffers();
    }

    private void setupIntermediateBuffers(){
        this.buffers = new double[this.coordinateOperations.length + 1][];
        for( int idx = 0; idx < coordinateOperations.length-1; idx++) {
            if (coordinateOperations[idx].outCoordinateDimension() != coordinateOperations[idx+1].inCoordinateDimension()) {
                throw new IllegalArgumentException("Coordinate dimensions don't match at step " + idx);
            }
            this.buffers[idx+1] =new double[coordinateOperations[idx].outCoordinateDimension()];
        }
    }

    @Override
    public boolean isReversible() {
        for(CoordinateOperation s : coordinateOperations ) {
            if (!s.isReversible()) return false;
        }
        return true;
    }

    @Override
    public int inCoordinateDimension() {
        return coordinateOperations[0].inCoordinateDimension();
    }

    @Override
    public int outCoordinateDimension() {
        return coordinateOperations[coordinateOperations.length -1].outCoordinateDimension();
    }

    @Override
    public void forward(double[] inCoordinate, double[] outCoordinate) {
        buffers[0] = inCoordinate;
        buffers[buffers.length - 1] = outCoordinate;
        for ( int idx = 0; idx < coordinateOperations.length; idx++ ) {
                CoordinateOperation t = coordinateOperations[idx];
                t.forward(buffers[idx], buffers[idx + 1]);
        }
    }

    @Override
    public void reverse(double[] inCoordinate, double[] outCoordinate) {
        buffers[0] = outCoordinate;
        buffers[buffers.length - 1] = inCoordinate;
        for ( int idx = coordinateOperations.length-1; idx >= 0; idx-- ) {
            CoordinateOperation t = coordinateOperations[idx];
            t.reverse(buffers[idx+1], buffers[idx]);
        }
    }


    public static class Builder {

        private List<CoordinateOperation> coordinateOperations = new LinkedList<>();

        public Builder forward(CoordinateOperation t) {
            coordinateOperations.add( new Step( t, Step.FORWARD));
            return this;
        }

        public Builder reverse(CoordinateOperation t) {
            coordinateOperations.add( new Step( t, Step.REVERSE));
            return this;
        }

        public ConcatenatedOperation build() {
            return new ConcatenatedOperation( this.coordinateOperations );
        }
    }

    private static class Step implements CoordinateOperation {

        static final public int FORWARD = 1;
        static final public int REVERSE = 2;

        final CoordinateOperation coordinateOperation;
        final int direction;
        Step(CoordinateOperation t, int direction){
            this.coordinateOperation = t;
            this.direction = direction;
        }


        @Override
        public boolean isReversible() {
            return this.coordinateOperation.isReversible();
        }

        @Override
        public int inCoordinateDimension() {
            if (direction == FORWARD) {
                return this.coordinateOperation.inCoordinateDimension();
            } else {
                return this.coordinateOperation.outCoordinateDimension();
            }
        }

        @Override
        public int outCoordinateDimension() {
            if (direction == FORWARD) {
                return this.coordinateOperation.outCoordinateDimension();
            } else {
                return this.coordinateOperation.inCoordinateDimension();
            }
        }

        @Override
        public void forward(double[] inCoordinate, double[] outCoordinate) {
            if(direction == FORWARD) {
                this.coordinateOperation.forward( inCoordinate, outCoordinate);
            } else {
                this.coordinateOperation.reverse( inCoordinate, outCoordinate);
            }
        }

        @Override
        public void reverse(double[] inCoordinate, double[] outCoordinate) {
            if(direction == FORWARD) {
                this.coordinateOperation.reverse( inCoordinate, outCoordinate);
            } else {
                this.coordinateOperation.forward( inCoordinate, outCoordinate);
            }
        }
    }

}
