package org.geolatte.geom.codec.db.db2;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.codec.WktEncoder;
import org.geolatte.geom.codec.db.Encoder;

/**
 * Created by Karel Maesen, Geovise BVBA on 04/06/17.
 */
public class Db2ClobEncoder implements Encoder<String> {


    @Override
    public <P extends Position, G extends Geometry<P>> String encode(G geom) {
        WktEncoder encoder = Wkt.newEncoder(Wkt.Dialect.DB2_WKT);
        return encoder.encode(geom);
    }

    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return false;
    }
}
