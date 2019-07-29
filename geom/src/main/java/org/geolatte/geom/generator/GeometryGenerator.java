package org.geolatte.geom.generator;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
public interface GeometryGenerator<P extends Position, G extends Geometry<P>> {

    G generate();

    @SuppressWarnings("unchecked")
    default G[] generateArray(int size) {
        List<G> geoms = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            geoms.add(generate());
        }
        return (G[])geoms.toArray();
    }

}
