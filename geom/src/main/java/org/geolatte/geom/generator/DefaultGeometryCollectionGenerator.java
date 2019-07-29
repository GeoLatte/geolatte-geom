package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.AbstractGeometryCollection;
import org.geolatte.geom.Position;
import org.geolatte.geom.builder.DSL;

import java.util.Random;

import static java.util.Arrays.asList;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultGeometryCollectionGenerator<P extends Position> extends AbstractGeometryGenerator<P, AbstractGeometryCollection<P, Geometry<P>>> {


    private final int numGeoms;
    private final GeometryGenerator<P, Geometry<P>> combined;

    DefaultGeometryCollectionGenerator(int numGeoms, int numPos, Envelope<P> bbox, Random rnd) {
        super(bbox, rnd);
        this.numGeoms = numGeoms;


        this.combined = GeometryGenerators.combine(asList(
                GeometryGenerators.point(bbox, rnd).asGeometry(),
                GeometryGenerators.lineString(numPos, bbox, rnd).asGeometry(),
                GeometryGenerators.polygon(numPos, bbox, rnd).asGeometry()
        ));
    };

    @Override
    public AbstractGeometryCollection<P, Geometry<P>> generate() {
        return DSL.geometrycollection(combined.generate(), combined.generateArray(numGeoms - 1));
    }
}
