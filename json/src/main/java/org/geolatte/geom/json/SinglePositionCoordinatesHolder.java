package org.geolatte.geom.json;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
class SinglePositionCoordinatesHolder extends CoordinatesHolder {

    final private double[] coordinates;

    public SinglePositionCoordinatesHolder(double[] coordinates) {
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
}
