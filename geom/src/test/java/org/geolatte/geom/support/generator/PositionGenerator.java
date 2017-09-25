package org.geolatte.geom.support.generator;

import org.geolatte.geom.Position;
import org.geolatte.geom.PositionFactory;
import org.geolatte.geom.Positions;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/03/15.
 */
public class PositionGenerator<P extends Position> implements Generator<P> {

    final private PositionFactory<P> factory;
    final private double[] coordinates;
    final private Generator<Double> cGen;

    public PositionGenerator(Class<P> positionClass, Generator<Double> coordinateGenerator) {
        this.factory = Positions.getFactoryFor(positionClass);
        this.coordinates = new double[factory.getCoordinateDimension()];
        this.cGen = coordinateGenerator;
    }

    @Override
    public P sample() {
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = cGen.sample();
        }
        return factory.mkPosition(coordinates);
    }
}
