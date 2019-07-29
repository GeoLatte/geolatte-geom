package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.LinearRing;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;
import org.geolatte.geom.builder.DSL;

import java.util.Random;


import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.generator.PositionGenerator.nPositionsWithinAndClosed;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultPolygonGenerator<P extends Position> extends AbstractGeometryGenerator<P, Polygon<P>> {

    final private int numPoints;

    DefaultPolygonGenerator(int numPoints, Envelope<P> bbox, Random rnd) {
        super(bbox, rnd);
        this.numPoints = numPoints;
    }

    @Override
    public Polygon<P> generate() {
        P[] ps = nPositionsWithinAndClosed(numPoints, bbox, rnd);
        LinearRing<P> hull = ring(crs(), ps);
        return DSL.polygon(hull);
    }

}
