package org.geolatte.geom.generator;

import org.geolatte.geom.Box;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Position;
import org.geolatte.geom.builder.DSL;

import java.util.Random;

import static org.geolatte.geom.generator.PositionGenerator.nPositionsWithin;

/**
 * Created by Karel Maesen, Geovise BVBA on 28/09/2018.
 */
class DefaultLineStringGenerator<P extends Position> extends AbstractGeometryGenerator<P, LineString<P>> {

    final private int size;
    final private boolean closed;

    DefaultLineStringGenerator(int size, Box<P> bbox, Random rnd) {
        super(bbox, rnd);
        this.size = size;
        this.closed = false;
    }

    @Override
    public LineString<P> generate() {
        P[] ps = nPositionsWithin(size, bbox, rnd);
        if (closed) {
            ps[ps.length - 1] = ps[0];
        }
        return DSL.linestring(crs(), ps);
    }


}
