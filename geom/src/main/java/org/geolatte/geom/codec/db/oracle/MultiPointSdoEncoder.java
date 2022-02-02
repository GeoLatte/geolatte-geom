package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class MultiPointSdoEncoder extends AbstractSDOEncoder {
    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return GeometryType.MULTIPOINT.equals(geom.getGeometryType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Position, G extends Geometry<P>> SDOGeometry encode(G geom) {
        final int dim = geom.getCoordinateDimension();
        final int lrsDim = getLRSDim(geom);
        final boolean isLrs = (lrsDim != 0);
        MultiPoint<P> multiPoint = (MultiPoint<P>) geom;

        final ElemInfo info = new ElemInfo(1);
        info.setElement(0, 1, ElementType.POINT, multiPoint.getNumGeometries());
        Double[] ordinates = new Double[]{};
        for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
            ordinates = addOrdinates(ordinates, pointToOrdinates(multiPoint, i));
        }

        return new SDOGeometry(new SDOGType(dim, lrsDim, SdoGeometryType.MULTIPOINT), geom.getSRID(), null, info, new
                Ordinates(ordinates));
    }

    private Double[] pointToOrdinates(MultiPoint<?> multiPoint, int i) {
        if (multiPoint.getGeometryN(i).isEmpty()) {
            return new Double[]{};
        }
        Double[] pointOrdinates = new Double[multiPoint.getCoordinateDimension()];
        int idx = 0;
        for (double d : multiPoint.getGeometryN(i).getPosition().toArray(null)) {
            pointOrdinates[idx++] = d;
        }
        return pointOrdinates;
    }

}
