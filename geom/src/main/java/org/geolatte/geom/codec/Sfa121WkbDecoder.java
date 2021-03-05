package org.geolatte.geom.codec;

import org.geolatte.geom.ByteBuffer;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A WKB decoder for the format specified in Simple Feature Access, version 1.2.1
 */
public class Sfa121WkbDecoder implements WkbDecoder {
    @Override
    public <P extends Position> Geometry<P> decode(ByteBuffer byteBuffer, CoordinateReferenceSystem<P> crs) {
        BaseWkbParser<P> parser = new BaseWkbParser<>(Sfa121WkbDialect.INSTANCE, byteBuffer, crs);
        try {
            return parser.parse();
        } catch( WkbDecodeException e) {
            throw e;
        } catch (Throwable e) {
            throw new WkbDecodeException(e);
        }
    }
}
