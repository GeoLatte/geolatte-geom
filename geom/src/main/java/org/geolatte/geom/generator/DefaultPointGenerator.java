package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;

import java.util.Random;

import static org.geolatte.geom.generator.PositionGenerator.positionWithin;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
class DefaultPointGenerator<P extends Position> implements GeometryGenerator<P, Point<P>> {

    final private Envelope<P> bbox;
    final private Random rnd;

    public DefaultPointGenerator(Envelope<P> bbox, Random rnd) {
        this.bbox = bbox;
        this.rnd = rnd;
    }

    @Override
    public Point<P> generate() {
        return new Point(positionWithin(bbox, rnd), bbox.getCoordinateReferenceSystem());
    }

}
