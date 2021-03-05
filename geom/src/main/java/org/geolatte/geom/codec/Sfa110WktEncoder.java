package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

/**
 * A WKT encoder for the format specified in Simple Feature Access, version 1.1.0
 */
public class Sfa110WktEncoder implements WktEncoder {

    @Override
    public <P extends Position> String encode(Geometry<P> geometry) {
        return new Sfa110WktWriter().writeGeometry(geometry);
    }
}

class Sfa110WktWriter extends BaseWktWriter {

    public Sfa110WktWriter() {
        super(Sfa110WktDialect.INSTANCE, new StringBuilder());
    }

    @Override
    protected <P extends Position> void addMultiPointText(Geometry<P> geometry) {
        addPointList(geometry.getPositions());
    }
}
