package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.Random;

import static org.geolatte.geom.generator.PositionGenerator.positionWithin;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
abstract class AbstractGeometryGenerator<P extends Position, G extends Geometry<P>> implements GeometryGenerator<P, G> {
    protected final Envelope<P> bbox;
    protected final Random rnd;

    public AbstractGeometryGenerator(Envelope<P> bbox, Random rnd) {
        this.bbox = bbox;
        this.rnd = rnd;
    }

    @Override
    abstract public G generate();

    protected CoordinateReferenceSystem<P> crs() {
        return this.bbox.getCoordinateReferenceSystem();
    }

}
