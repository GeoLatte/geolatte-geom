package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.db.Decoder;

import java.sql.Struct;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class Decoders {

	public static Decoder decoderFor(SDOGeometry object) {
		return object.getGType().getTypeGeometry().createDecoder();
	}

    /**
     * Decodes the SQL Server Geometry object to its JTS Geometry instance
     *
     * @param raw
     *
     * @return
     */
    public static Geometry decode(Struct raw) {
        SDOGeometry sdo = SDOGeometry.load( raw );
        return decode(sdo);
    }

    public static Geometry decode(SDOGeometry sdo) {
        Decoder decoder = decoderFor(sdo);
        return decoder.decode( sdo );
    }

}
