package org.geolatte.geom.generator;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-08-16.
 */
public interface Generator<T> {

    T generate();

    @SuppressWarnings("unchecked")
    default void generateArray(T[] ts) {
        for(int i = 0; i < ts.length; i++){
            ts[i]  = generate();
        }
    }
}
