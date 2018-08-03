package org.geolatte.geom.generator;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
public class Generators {


    public static <P extends Position> GeometryGenerator<P, Point<P>> point(Envelope<P> bbox) {
        return new DefaultPointGenerator<>(bbox);
    }
}
