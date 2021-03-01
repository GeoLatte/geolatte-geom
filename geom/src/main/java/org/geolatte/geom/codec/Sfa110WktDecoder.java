package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

public class Sfa110WktDecoder implements WktDecoder {

    @Override
    public Geometry<?> decode(String wkt) {
        return decode(wkt, null);
    }

    @Override
    public <P extends Position> Geometry<P> decode(String wkt, CoordinateReferenceSystem<P> crs) {
        return new Sfa110WktParser<>(wkt, crs).parse();
    }
}

class Sfa110WktParser<P extends Position> extends BaseWktParser<P> {

    private final static WktDialect dialect = new WktDialect();

    Sfa110WktParser(String wkt, CoordinateReferenceSystem<P> crs) {
        super(dialect, wkt, crs);
    }

    @Override
    protected CoordinateReferenceSystem<?> widenCrsToCoordinateDimension(CoordinateReferenceSystem<?> crs) {
        //don't do this for this dialect
        return crs;
    }
}
