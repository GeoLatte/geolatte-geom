package org.geolatte.geom.generator;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
public interface GeometryGenerator<P extends Position, G extends Geometry<P>> {

    G generate();

    @SuppressWarnings("unchecked")
    default void generateArray(G[] geoms) {
        for(int i = 0; i < geoms.length; i++){
            geoms[i]  = generate();
        }
    }

}
