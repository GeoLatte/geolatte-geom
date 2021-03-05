package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * A WKT decoder for the format specified in Simple Feature Access, version 1.2.1
 */
public class Sfa121WktDecoder implements WktDecoder {

    @Override
    public <P extends Position> Geometry<P> decode(String wkt, CoordinateReferenceSystem<P> crs) {
        return new Sfa121WktParser<>(wkt, crs).parse();
    }

}

class Sfa121WktParser<P extends Position> extends BaseWktParser<P> {

    Sfa121WktParser(String wkt, CoordinateReferenceSystem<P> crs) {
        super(Sfa110WktDialect.INSTANCE, wkt, crs);
    }

    @Override
    protected void matchesOptionalZMMarkers() {
        tokenizer.skipWhitespace();
        if (tokenizer.matchesOneOf('Z', 'z').isPresent()) {
            this.hasZMark = true;
        }
        if (tokenizer.matchesOneOf('M', 'm').isPresent()) {
            this.hasMMark = true;
        }
    }


}