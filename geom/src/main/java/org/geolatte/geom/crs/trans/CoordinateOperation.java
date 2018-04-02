package org.geolatte.geom.crs.trans;

/**
 * Low-level transformation interface
 *
 * Created by Karel Maesen, Geovise BVBA on 20/07/17.
 */
public interface CoordinateOperation {

    boolean isReversible();

    int inCoordinateDimension();

    int outCoordinateDimension();

    void forward(double[] inCoordinate, double[] outCoordinate);

    void reverse(double[] inCoordinate, double[] outCoordinate);

    /**
     * Creates a new {@code CoordinateOperation} from this instance by appending and the specified instance in the forward direction;
     *
     * @param operation the instance to append
     * @return a new instance
     */
    default CoordinateOperation appendForward(CoordinateOperation operation) {
        return new ConcatenatedOperation.Builder().forward( this ).forward(operation).build();
    }

    /**
     * Creates a new {@code CoordinateOperation} from this instance by appending and the specified instance in the reverse direction;
     *
     * @param operation the instance to append
     * @return a new instance
     */
    default CoordinateOperation appendReverse(CoordinateOperation operation) {
        return new ConcatenatedOperation.Builder().forward( this ).reverse(operation).build();
    }


    /**
     * Creates a new {@code CoordinateOperation} by reversing this instance
     * @return a new {@code CoordinateOperation} by reversing this instance
     */
    default CoordinateOperation reverse() {
        return new ConcatenatedOperation.Builder().reverse(this).build();
    }
}
