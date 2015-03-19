package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Position;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
* Created by Karel Maesen, Geovise BVBA on 18/02/15.
*/
public class LineStringSdoDecoder extends AbstractSDODecoder {


    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == TypeGeometry.LINE;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Geometry<?> internalDecode(SDOGeometry nativeGeom) {
        final CoordinateReferenceSystem<?> crs = getCoordinateReferenceSystem(nativeGeom);
        final ElemInfo info = nativeGeom.getInfo();

        PositionSequence cs = null;

        int i = 0;
        while (i < info.getSize()) {
            if (info.getElementType(i).isCompound()) {
                final int numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, nativeGeom));
                i += 1 + numCompounds;
            } else {
                cs = add(cs, getElementCSeq(i, nativeGeom, false, crs));
                i++;
            }
        }
        return new LineString(cs, crs);

    }

}
