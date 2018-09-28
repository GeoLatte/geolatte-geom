package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.builder.DSL;

import java.util.Random;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultMultiPolygonGenerator<P extends Position> extends AbstractGeometryGenerator<P, MultiPolygon<P>>{


    private final int numPolys;
    private final DefaultPolygonGenerator<P> polyGen;

    DefaultMultiPolygonGenerator(int numPolys, int numPoints, Envelope<P> bbox, Random rnd) {
        super(bbox, rnd);
        this.numPolys = numPolys;
        this.polyGen = new DefaultPolygonGenerator<>(numPoints, bbox, rnd);
    }

    @Override
    public MultiPolygon<P> generate() {
        return DSL.multipolygon(polyGen.generate(), polyGen.generateArray(numPolys - 1));
    }
}
