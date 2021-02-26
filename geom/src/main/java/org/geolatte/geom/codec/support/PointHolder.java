package org.geolatte.geom.codec.support;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Created by Karel Maesen, Geovise BVBA on 09/09/17.
 */
public class PointHolder extends Holder {

    final private List<Double> coordinates;

    public PointHolder() {
        this.coordinates = new ArrayList<>(4);
    }

    public PointHolder(double[] co) {
        coordinates = DoubleStream.of(co)
                .boxed()
                .collect(Collectors.toList());
    }

    public void push(double co) {
        this.coordinates.add(co);
    }

    @Override
    public boolean isEmpty() {
        return this.coordinates.size() == 0;
    }

    public double[] getCoordinates() {
        return coordinates.stream().mapToDouble(x -> x).toArray();
    }

    @Override
    public int getCoordinateDimension() {
        return this.coordinates.size();
    }

    public <P extends Position> P toPosition(CoordinateReferenceSystem<P> crs) {
        double[] co = (this.getCoordinateDimension() != crs.getCoordinateDimension()) ?
                toDimension(this.getCoordinates(), crs.getCoordinateDimension()) :
                this.getCoordinates();
        return Positions.mkPosition(crs.getPositionClass(), co);

    }

    public <P extends Position> Point<P> toGeometry(CoordinateReferenceSystem<P> crs, GeometryType geomType) {
        if (isEmpty() || Double.isNaN(coordinates.get(0))) return Geometries.mkEmptyPoint(crs);
        return Geometries.mkPoint(toPosition(crs), crs);
    }

    private double[] toDimension(double[] orig, int targetDim) {
        return Arrays.copyOf(orig, targetDim);
    }

}
