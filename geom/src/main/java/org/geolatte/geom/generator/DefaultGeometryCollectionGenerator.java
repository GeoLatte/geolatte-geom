package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.Position;
import org.geolatte.geom.builder.DSL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultGeometryCollectionGenerator<P extends Position> extends AbstractGeometryGenerator<P, GeometryCollection<P, Geometry<P>>> {


    private final int numGeoms;
    private final GeometryGenerator<P, Geometry<P>> combined;

    DefaultGeometryCollectionGenerator(int numGeoms, int numPos, Envelope<P> bbox, Random rnd) {
        super(bbox, rnd);
        this.numGeoms = numGeoms;

        this.combined = Generators.combine(asList(
                Generators.point(bbox, rnd).asGeometry(),
                Generators.lineString(numPos, bbox, rnd).asGeometry(),
                Generators.polygon(numPos, bbox, rnd).asGeometry()
        ));
    };

    @Override
    public GeometryCollection<P, Geometry<P>> generate() {
        return DSL.geometrycollection(combined.generate(), combined.generateArray(numGeoms - 1));
    }
}
