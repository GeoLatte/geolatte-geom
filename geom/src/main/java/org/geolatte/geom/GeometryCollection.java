package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-29.
 */
public class GeometryCollection<P extends Position>  extends AbstractGeometryCollection<P, Geometry<P>> {


    @SafeVarargs
    public GeometryCollection(Geometry<P>... geoms) {
        super(geoms);
    }

    public GeometryCollection(CoordinateReferenceSystem<P> crs) {
        super(crs);
    }

    @SuppressWarnings("unchecked")
    public <Q extends Position> GeometryCollection<Q> as(Class<Q> castToType){
        checkCast(castToType);
        return (GeometryCollection<Q>)this;
    }
}
