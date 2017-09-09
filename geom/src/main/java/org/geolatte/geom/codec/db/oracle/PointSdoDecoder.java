package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.codec.db.Decoder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
* Created by Karel Maesen, Geovise BVBA on 18/02/15.
*/
public class PointSdoDecoder extends AbstractSDODecoder {


    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == TypeGeometry.POINT;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Geometry<?> internalDecode(SDOGeometry nativeGeom) {
        CoordinateReferenceSystem<? extends Position> crs = getCoordinateReferenceSystem(nativeGeom);

        Double[] ordinates;
        if (nativeGeom.getPoint() != null) {
            if (nativeGeom.getDimension() == 2) {
                ordinates = new Double[]{nativeGeom.getPoint().x, nativeGeom.getPoint().y};
            } else {
                ordinates = new Double[]{nativeGeom.getPoint().x, nativeGeom.getPoint().y, nativeGeom.getPoint().z};
            }
        } else {
            ordinates = nativeGeom.getOrdinates().getOrdinateArray();
        }
        return new Point(convertOrdinateArray(ordinates, nativeGeom, crs), crs);
    }

}
