package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.codec.db.Decoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class GeometryCollectionSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == TypeGeometry.COLLECTION;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode(SDOGeometry sdoGeom) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        for (SDOGeometry elemGeom : sdoGeom.getElementGeometries()) {
            Decoder decoder = Decoders.decoderFor(elemGeom);
            geometries.add(decoder.decode(elemGeom));
        }
        final Geometry[] geomArray = new Geometry[geometries.size()];
        return new GeometryCollection(geometries.toArray(geomArray));
    }
}
