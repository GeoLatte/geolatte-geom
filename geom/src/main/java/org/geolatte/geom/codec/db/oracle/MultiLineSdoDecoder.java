package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class MultiLineSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == TypeGeometry.MULTILINE;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode(SDOGeometry sdoGeom) {
        CoordinateReferenceSystem crs = getCoordinateReferenceSystem(sdoGeom);
        final ElemInfo info = sdoGeom.getInfo();
        final LineString[] lines = new LineString[sdoGeom.getInfo().getSize()];
        int i = 0;
        while (i < info.getSize()) {
            PositionSequence cs = null;
            if (info.getElementType(i).isCompound()) {
                final int numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, sdoGeom));
                final LineString line = new LineString(cs, crs);
                lines[i] = line;
                i += 1 + numCompounds;
            } else {
                cs = add(cs, getElementCSeq(i, sdoGeom, false, crs));
                final LineString line = new LineString(cs, crs);
                lines[i] = line;
                i++;
            }
        }
        return new MultiLineString(lines);
    }
}
