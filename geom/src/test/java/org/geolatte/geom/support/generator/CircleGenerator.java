package org.geolatte.geom.support.generator;

import org.geolatte.geom.cga.Circle;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/03/15.
 */
public class CircleGenerator implements Generator<Circle>{

    final Generator<Double> cGen;
    final Generator<Double> rGen;

    public CircleGenerator(double lowestCoordinate, double highestCoordinate, double maxRadius) {
        cGen = StdGenerators.between(lowestCoordinate, highestCoordinate);
        rGen = StdGenerators.between(0.0000000001, maxRadius);
    }

    @Override
    public Circle sample() {
        double x = cGen.sample();
        double y = cGen.sample();
        double radius = rGen.sample();
        return new Circle(x, y, radius);
    }

}
