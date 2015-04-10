package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.LinearRing;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class PolygonSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == TypeGeometry.POLYGON;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode(SDOGeometry sdoGeom) {
        CoordinateReferenceSystem crs = getCoordinateReferenceSystem(sdoGeom);
        LinearRing shell = null;
        final LinearRing[] rings = new LinearRing[sdoGeom.getNumElements()];
        final ElemInfo info = sdoGeom.getInfo();
        int i = 0;
        int idxInteriorRings = 1;
        while (i < info.getSize()) {
            PositionSequence cs = null;
            int numCompounds = 0;
            if (info.getElementType(i).isCompound()) {
                numCompounds = info.getNumCompounds(i);
                cs = add(cs, getCompoundCSeq(i + 1, i + numCompounds, sdoGeom));
            } else {
                cs = add(cs, getElementCSeq(i, sdoGeom, false, crs));
            }
            if (info.getElementType(i).isInteriorRing()) {
                rings[idxInteriorRings] = new LinearRing(cs, crs);
                idxInteriorRings++;
            } else {
                rings[0] = new LinearRing(cs, crs);
            }
            i += 1 + numCompounds;
        }
        return new Polygon(rings);

    }
}
