package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class MultiPointSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == TypeGeometry.MULTIPOINT;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode(SDOGeometry sdoGeom) {
        CoordinateReferenceSystem<?> crs = getCoordinateReferenceSystem(sdoGeom);
        final Double[] ordinates = sdoGeom.getOrdinates().getOrdinateArray();
        PositionSequence<? extends Position> positions = convertOrdinateArray(ordinates, sdoGeom, crs);
        Point[] pnts = new Point[positions.size()];
        int i = 0;
        for (Position p : positions) {
            pnts[i++] = new Point(p, crs);
        }
        return new MultiPoint(pnts);
    }
}
