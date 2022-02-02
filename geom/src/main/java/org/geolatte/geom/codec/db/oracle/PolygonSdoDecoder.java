package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class PolygonSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == SdoGeometryType.POLYGON;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode() {
        return decode(nativeGeom.getGType(), nativeGeom.getElements(), nativeGeom.getCoordinateReferenceSystem());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs) {
        LinearRing[] rings = new LinearRing[elements.size()];

        int idx = 1;
        for(Element element: elements ) {
            if (element.getElementType().isInteriorRing()) {
                rings[idx++] = new LinearRing(element.linearizedPositions(gtype, crs), crs);
            }
            else {
                rings[0] = new LinearRing(element.linearizedPositions(gtype, crs), crs);
            }
        }

        if( !Arrays.stream(rings).allMatch(Objects::nonNull)){
            //only possible when number of exterior rings != 1.
            throw new IllegalStateException("Invalid null ring found when attempting to construct polygon");
        }
        return new Polygon(rings);
    }
}
