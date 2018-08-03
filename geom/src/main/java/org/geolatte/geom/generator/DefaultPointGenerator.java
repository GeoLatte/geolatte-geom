package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;

import static org.geolatte.geom.generator.PositionGenerator.positionWithin;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
class DefaultPointGenerator<P extends Position> implements GeometryGenerator<P, Point<P>> {

    final private Envelope<P> bbox;

    public DefaultPointGenerator(Envelope<P> bbox) {
        this.bbox = bbox;
    }

    @Override
    public Point<P> generate() {
        return new Point(positionWithin(bbox), bbox.getCoordinateReferenceSystem());
    }

}
