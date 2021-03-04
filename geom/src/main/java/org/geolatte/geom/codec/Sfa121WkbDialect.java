package org.geolatte.geom.codec;

import org.geolatte.geom.*;

public class Sfa121WkbDialect extends WkbDialect {

    final static Sfa121WkbDialect INSTANCE = new Sfa121WkbDialect();

    @Override
    protected <P extends Position> Long geometryTypeCode(Geometry<P> geometry) {
        Long base =  super.geometryTypeCode(geometry);
        if (geometry.hasZ()) base += 1000L;
        if (geometry.hasM()) base += 2000L;
        return base;
    }

    @Override
    GeometryType parseType(long tpe) {
        long t = tpe % 1000L;
        return super.parseType(t);
    }

    @Override
    boolean hasZ(long tpe) {
        return (tpe > 1000 && tpe < 2000) || tpe > 3000;
    }

    @Override
    boolean hasM(long tpe) {
        return tpe > 2000;
    }
}

