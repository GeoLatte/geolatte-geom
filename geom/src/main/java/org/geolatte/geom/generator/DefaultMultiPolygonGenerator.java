package org.geolatte.geom.generator;

import org.geolatte.geom.*;
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
        Polygon<P>[] polys = (Polygon<P>[]) new Polygon[numPolys - 1];
        polyGen.generateArray(polys);
        return DSL.multipolygon(polyGen.generate(), polys);
    }
}
