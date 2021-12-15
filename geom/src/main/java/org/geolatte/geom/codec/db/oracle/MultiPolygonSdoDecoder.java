package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class MultiPolygonSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == SdoGeometryType.MULTIPOLYGON;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode() {
        return decode(nativeGeom.getGType(), nativeGeom.getElements(), nativeGeom.getCoordinateReferenceSystem());
    }

    @Override
    protected <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs) {
        List<Polygon<P>> polys = new ArrayList<>();
        List<Element> rings = new ArrayList<>();
        AbstractSDODecoder decoder = new PolygonSdoDecoder();
        for (Element element : elements) {
            if (element.isExteriorRing() && !rings.isEmpty()) {
                polys.add((Polygon<P>)decoder.decode(gtype, rings, crs));
                rings.clear();
            }
            rings.add(element);
        }
        if (!rings.isEmpty()) {
            polys.add((Polygon<P>)decoder.decode(gtype, rings, crs));
        }
        return polys.isEmpty() ?
                new MultiPolygon<>(crs) :
                new MultiPolygon<>(polys.toArray(new Polygon[0]));
    }
}
