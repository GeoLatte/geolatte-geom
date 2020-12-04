package org.geolatte.geom.codec;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;

abstract class WktGeomVariant extends WktVariant {

    /**
     * Constructs an instance.
     */
    protected WktGeomVariant() {
        super('(', ')', ',');
    }

    abstract void addGeometryTag(StringBuffer buffer, Geometry<?> geometry);


    abstract void addGeometryZMMarker(StringBuffer buffer, Geometry<?> geometry);

    public abstract void addSrid(StringBuffer builder, int srid);
}
