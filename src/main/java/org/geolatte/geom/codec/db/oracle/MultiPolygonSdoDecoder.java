package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class MultiPolygonSdoDecoder extends AbstractSDODecoder{

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == TypeGeometry.MULTIPOLYGON;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode(SDOGeometry sdoGeom) {
        CoordinateReferenceSystem<?> crs = getCoordinateReferenceSystem(sdoGeom);
        Deque<LinearRing> holes = new LinkedList<LinearRing>();
        final List<Polygon> polygons = new ArrayList<Polygon>();
        final ElemInfo info = sdoGeom.getInfo();
        LinearRing shell = null;
        int i = 0;
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
                final LinearRing lr = new LinearRing(cs, crs);
                holes.addLast(lr);
            } else {
                if (shell != null) {
                    holes.addFirst(shell);
                    final Polygon polygon = new Polygon(holes.toArray(new LinearRing[holes.size()]));
                    polygons.add(polygon);
                    shell = null;
                }
                shell = new LinearRing(cs, crs);
                holes = new LinkedList<LinearRing>();
            }
            i += 1 + numCompounds;
        }
        if (shell != null) {
            holes.addFirst(shell);
            final Polygon polygon = new Polygon(holes.toArray(new LinearRing[holes.size()]));
            polygons.add(polygon);
        }
        return new MultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
    }
}
