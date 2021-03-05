package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;

class Sfa121WktDialect extends WktDialect{

    final static Sfa121WktDialect INSTANCE = new Sfa121WktDialect();

    @Override
    void addGeometryZMMarker(StringBuilder buffer, Geometry<?> geometry) {
        if (!(geometry.hasZ() || geometry.hasM())) return;

        if (geometry.hasZ() && geometry.hasM())  buffer.append(" ZM");
        else if (geometry.hasZ()) buffer.append(" Z");
        else if (geometry.hasM()) buffer.append(" M");

        if(!geometry.isEmpty())
            buffer.append(' ');
    }

}
