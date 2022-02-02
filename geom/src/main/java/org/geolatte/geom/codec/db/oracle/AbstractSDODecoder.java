package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Position;
import org.geolatte.geom.codec.db.Decoder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 17/02/15.
 */
abstract public class AbstractSDODecoder implements Decoder<SDOGeometry> {

    protected SDOGeometry nativeGeom;

    @Override
    public Geometry<?> decode(SDOGeometry nativeGeom) {
        if (!accepts(nativeGeom)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " received object of type " + nativeGeom.getGType());
        }
        this.nativeGeom = nativeGeom;
        return internalDecode();
    }

    abstract Geometry<?> internalDecode();

    abstract <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs);

}
