package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

public class Sfa121WktEncoder implements WktEncoder {

    @Override
    public <P extends Position> String encode(Geometry<P> geometry) {
        return new BaseWktWriter(Sfa121WktDialect.INSTANCE, new StringBuilder()).writeGeometry(geometry);
    }

}
