package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;

class Sfa110WktEncoder extends BaseWktEncoder {

    /**
     * Constructs an instance.
     *
     */
    public Sfa110WktEncoder() {
        super(new Sfa110WktVariant());
    }

    @Override
    protected <P extends Position> void addMultiPointText(Geometry<P> geometry) {
        addPointList(geometry.getPositions());
    }
}
