package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.db.Encoder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class Encoders {

    final private static List<AbstractSDOEncoder> ENCODERS = new ArrayList<AbstractSDOEncoder>();


    static {
        ENCODERS.add(new SdoPointEncoder());
        ENCODERS.add(new SdoLineStringEncoder());
        ENCODERS.add(new SdoPolygonEncoder());
        ENCODERS.add(new SdoMultiPointEncoder());
        ENCODERS.add(new SdoMultiLineStringEncoder());
        ENCODERS.add(new SdoMultiPolygonEncoder());
        ENCODERS.add(new SdoGeometryCollectionEncoder());
    }

    public static Encoder<SDOGeometry> encoderFor(Geometry<?> geom) {
        for (Encoder<SDOGeometry> encoder : ENCODERS) {
            if (encoder.accepts(geom)) {
                return encoder;
            }
        }
        throw new IllegalArgumentException("No encoder for type " + (geom == null ? "NULL" : geom.getGeometryType()));
    }

    public static SDOGeometry encode(Geometry<?> geom) {
        Encoder<SDOGeometry> encoder = encoderFor(geom);
        return encoder.encode(geom);
    }

    public static Struct encode(Geometry<?> geom, Connection conn, SQLTypeFactory typeFactory) throws SQLException {
        return typeFactory.createStruct(encode(geom), conn);
    }

}
