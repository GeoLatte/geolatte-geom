package org.geolatte.geom.json;

import org.geolatte.geom.Position;
import org.geolatte.geom.Positions;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Arrays;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
class PointHolder extends Holder {

    final private double[] coordinates;

    public PointHolder(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    @Override
    boolean isEmpty() {
        return this.coordinates.length == 0;
    }

    @Override
    int getCoordinateDimension() {
        return this.coordinates.length;
    }

    public <P extends Position> P toPosition(CoordinateReferenceSystem<P> crs) {
        double[] co = (this.getCoordinateDimension() != crs.getCoordinateDimension()) ?
                toDimension(this.getCoordinates(), crs.getCoordinateDimension()) :
                this.getCoordinates();
        return Positions.mkPosition(crs.getPositionClass(), co);

    }

    private double[] toDimension(double[] orig, int targetDim) {
        return Arrays.copyOf(orig, targetDim);
    }

}
