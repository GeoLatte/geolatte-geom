package org.geolatte.geom.generator;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class CombinedGeometryGenerator<P extends Position> implements GeometryGenerator<P, Geometry<P>> {

    private final Random  chooser;
    private final List<GeometryGenerator<P, ? extends Geometry<P>>> generators;

    CombinedGeometryGenerator(Random rnd, List<GeometryGenerator<P, ? extends Geometry<P>>> generators) {
        this.chooser = rnd;
        this.generators = generators;
    }

    @Override
    public Geometry<P> generate() {
        int idx = chooser.nextInt(generators.size());
        return generators.get(idx).generate();
    }

}
