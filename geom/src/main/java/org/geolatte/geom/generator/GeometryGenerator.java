package org.geolatte.geom.generator;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 03/08/2018.
 */
public interface GeometryGenerator<P extends Position, G extends Geometry<P>> {

    G generate();
}
