package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class SdoPointEncoder extends AbstractSDOEncoder {

    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return GeometryType.POINT.equals(geom.getGeometryType());
    }

    @Override
    public <P extends Position, G extends Geometry<P>> SDOGeometry encode(G geom) {
        final int dim = geom.getCoordinateDimension();
        final int lrsDim = getLRSDim(geom);
        final boolean isLrs = (lrsDim != 0);

        final Double[] coord = convertPositionSequence(geom.getPositions());

        if (Settings.useSdoPointType() && !isLrs && !geom.isEmpty()) {
            return new SDOGeometry(
                    new SDOGType(dim, lrsDim, TypeGeometry.POINT),
                    geom.getSRID(),
                    new SDOPoint(coord), null, null
            );
        }

        final ElemInfo info = new ElemInfo(1);
        info.setElement(0, 1, ElementType.POINT, 1);
        return new SDOGeometry(
                new SDOGType(dim, lrsDim, TypeGeometry.POINT),
                geom.getSRID(),
                null,
                info,
                new Ordinates(coord)
        );

    }

}
