package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.Point;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class MultiPointSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == SdoGeometryType.MULTIPOINT;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode() {
        return decode(nativeGeom.getGType(), nativeGeom.getElements()
                , nativeGeom.getCoordinateReferenceSystem());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs) {
        Point[] pnts = elements.stream()
                .map(el -> el.linearizedPositions(gtype, crs))
                .flatMap(pos -> pos.stream().map(p -> new Point(p, crs)))
                .toArray(Point[]::new);
        return pnts.length > 0 ? new MultiPoint(pnts) : new MultiPoint(crs);
    }
}
