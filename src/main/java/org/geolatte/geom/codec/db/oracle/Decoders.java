package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.db.Decoder;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class Decoders {

    final private static List<AbstractSDODecoder> DECODERS = new ArrayList<AbstractSDODecoder>();

    static {
        //Decoders
        DECODERS.add( new PointSdoDecoder() );
        DECODERS.add( new LineStringSdoDecoder() );
        DECODERS.add( new PolygonSdoDecoder() );
        DECODERS.add( new MultiLineSdoDecoder(  ) );
        DECODERS.add( new MultiPolygonSdoDecoder(  ) );
        DECODERS.add( new MultiPointSdoDecoder( ) );
        DECODERS.add( new GeometryCollectionSdoDecoder(  ) );
    }


    public static Decoder decoderFor(SDOGeometry object) {
        for ( Decoder decoder : DECODERS ) {
            if ( decoder.accepts( object ) ) {
                return decoder;
            }
        }
        throw new IllegalArgumentException( "No decoder for type " + object.getGType().getTypeGeometry() );
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
