package org.geolatte.geom.generator;

import org.geolatte.geom.Box;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.builder.DSL;

import java.util.Random;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultMultiPointGenerator<P extends Position>  extends AbstractGeometryGenerator<P, MultiPoint<P>> {


    final private Generator<Point<P>> pntGen;
    final private int numPnts;
    DefaultMultiPointGenerator(int numPnts, Box<P> bbox, Random rnd) {
        super(bbox, rnd);
        this.numPnts = numPnts;
        pntGen = new DefaultPointGenerator<>(bbox, rnd);
    }

    @Override
    public MultiPoint<P> generate() {
        Point<P>[] points = (Point<P>[]) new Point[numPnts - 1];
        pntGen.generateArray(points);
        return DSL.multipoint(pntGen.generate(), points);
    }
}
