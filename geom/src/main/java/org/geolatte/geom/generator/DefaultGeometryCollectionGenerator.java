package org.geolatte.geom.generator;

import org.geolatte.geom.*;
import org.geolatte.geom.builder.DSL;

import java.util.Random;

import static java.util.Arrays.asList;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultGeometryCollectionGenerator<P extends Position> implements GeometryGenerator<P, GeometryCollection<P>> {


    private final int numGeoms;
    private final GeometryGenerator<P, Geometry<P>> baseGenerator;

    DefaultGeometryCollectionGenerator(int numGeoms, GeometryGenerator<P, Geometry<P>> baseGenerator, Random rnd) {
        this.numGeoms = numGeoms;
        this.baseGenerator = baseGenerator;
    };

    @Override
    @SuppressWarnings("unchecked")
    public GeometryCollection<P> generate() {
        Geometry<P>[] geoms = (Geometry<P>[]) new Geometry[numGeoms-1];
        baseGenerator.generateArray(geoms);
        return DSL.geometrycollection(baseGenerator.generate(), geoms);
    }
}
