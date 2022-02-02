package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class GeometryCollectionSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == SdoGeometryType.COLLECTION;
    }

    @Override
    Geometry<?> internalDecode() {
        return decode(nativeGeom.getGType(), nativeGeom.getElements(), nativeGeom.getCoordinateReferenceSystem());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    <P extends Position> Geometry<P> decode(SDOGType parentType, List<Element> elements, CoordinateReferenceSystem<P> crs) {
        final List<Geometry> geometries = new ArrayList<>();
        int i = 0;
        while (i < elements.size()) {
            Element element = elements.get(i);
            SDOGType gtype = SDOGType.derive(element.getElementType(), parentType);
            assert gtype != null;
            AbstractSDODecoder decoder = gtype.getTypeGeometry().createDecoder();
            if (element.isExteriorRing()) {
                List<Element> rings = new ArrayList<>();
                rings.add(element);
                for (int k = i + 1; k < elements.size(); k++) {
                    element = elements.get(k);
                    if (!element.isInteriorRing()) break;
                    rings.add(element);
                }
                geometries.add(decoder.decode(gtype, rings, crs));
                i += rings.size();
            } else {
                List<Element> ell = new ArrayList<>();
                ell.add(element);
                geometries.add(decoder.decode(gtype, ell, crs));
                i++;
            }
        }
        final Geometry[] geomArray = new Geometry[geometries.size()];
        return new GeometryCollection(geometries.toArray(geomArray));
    }
}
