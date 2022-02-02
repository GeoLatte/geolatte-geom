package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

import java.util.List;

/**
 * Created by Karel Maesen, Geovise BVBA on 18/02/15.
 */
public class PointSdoDecoder extends AbstractSDODecoder {


    @Override
    public boolean accepts(SDOGeometry nativeGeom) {
        return nativeGeom.getGType().getTypeGeometry() == SdoGeometryType.POINT;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Geometry<?> internalDecode() {
        CoordinateReferenceSystem<? extends Position> crs = nativeGeom.getCoordinateReferenceSystem();

        Double[] ordinates;
        if (nativeGeom.getPoint() != null) {
            if (nativeGeom.getDimension() == 2) {
                ordinates = new Double[]{nativeGeom.getPoint().x, nativeGeom.getPoint().y};
            } else {
                ordinates = new Double[]{nativeGeom.getPoint().x, nativeGeom.getPoint().y, nativeGeom.getPoint().z};
            }
            return new Point(buildSeq(ordinates, crs.getPositionClass()), crs);
        } else {
            return decode(nativeGeom.getGType(), nativeGeom.getElements(), crs);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected <P extends Position> Geometry<P> decode(SDOGType gtype, List<Element> elements, CoordinateReferenceSystem<P> crs) {
        assert (elements.size() <= 1);
        return new Point(elements.get(0).linearizedPositions(gtype, crs), crs);
    }

    private <P extends Position> PositionSequence<P> buildSeq(Double[] ordinates, Class<P> posClass) {
        PositionSequenceBuilder<P> builder = PositionSequenceBuilders.fixedSized(1, posClass);
        double[] coords = new double[ordinates.length];
        for (int i = 0; i < ordinates.length; i++) {
            coords[i] = ordinates[i] == null ? Double.NaN : ordinates[i];
        }
        builder.add(coords);
        return builder.toPositionSequence();
    }

}
