package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 05/06/17.
 */
public class DB2WktEncoder extends PostgisWktEncoder {


    protected boolean skipSrid(){
        return false;
    }


}
