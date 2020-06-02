package org.geolatte.geom.generator;

import org.geolatte.geom.Box;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.builder.DSL;

import java.util.Random;


import static org.geolatte.geom.generator.PositionGenerator.positionWithin;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
class DefaultPointGenerator<P extends Position> extends AbstractGeometryGenerator<P, Point<P>>  {

    DefaultPointGenerator(Box<P> bbox, Random rnd) {
        super(bbox, rnd);
    }

    @Override
    public Point<P> generate()  {
        return DSL.point(crs(), positionWithin(bbox, rnd) );
    }

}
