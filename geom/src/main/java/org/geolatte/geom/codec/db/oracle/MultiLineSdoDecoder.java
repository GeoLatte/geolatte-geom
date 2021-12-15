package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Karel Maesen, Geovise BVBA on 19/03/15.
 */
public class MultiLineSdoDecoder extends AbstractSDODecoder {

    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == SdoGeometryType.MULTILINE;
    }

    @Override
    @SuppressWarnings("unchecked")
    Geometry<?> internalDecode() {
        return decode(nativeGeom.getGType(), nativeGeom.getElements(), nativeGeom.getCoordinateReferenceSystem());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs) {
        List<LineString<P>> strings = elements
                .stream()
                .map(el -> el.linearizedPositions(gtype, crs))
                .map(ps -> new LineString<>(ps, crs))
                .collect(Collectors.toList());
        return strings.isEmpty() ? new MultiLineString<>(crs)
                : new MultiLineString<>(strings.toArray(new LineString[]{}));
    }
}
