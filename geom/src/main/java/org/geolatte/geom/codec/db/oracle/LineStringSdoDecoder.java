package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/02/15.
 */
public class LineStringSdoDecoder extends AbstractSDODecoder {


    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == SdoGeometryType.LINE;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Geometry<?> internalDecode() {
        return decode(nativeGeom.getGType(), nativeGeom.getElements(), nativeGeom.getCoordinateReferenceSystem());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs) {
        assert (elements.size() <= 1);
        return new LineString(elements.get(0).linearizedPositions(gtype, crs), crs);
    }

}
