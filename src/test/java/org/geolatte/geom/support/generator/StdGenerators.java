package org.geolatte.geom.support.generator;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/03/15.
 */
public class StdGenerators {

    public static Generator<Double> between(final double min, final double max){
        return new Generator<Double>() {

            @Override
            public Double sample() {
                return min + Math.random() * (max - min);
            }
        };
    }

    public static Generator<Boolean> bernouilly(final double probabilityOfSuccess){
        return new Generator<Boolean>(){

            @Override
            public Boolean sample() {
                if ( probabilityOfSuccess < 0 || probabilityOfSuccess > 1) throw new IllegalArgumentException();
                double r = Math.random();
                return r <= probabilityOfSuccess;
            }
        };
    }


}
