package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class SdoLineStringEncoder extends AbstractSDOEncoder {
    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return GeometryType.LINESTRING.equals(geom.getGeometryType());
    }

    @Override
    public <P extends Position, G extends Geometry<P>> SDOGeometry encode(G geom) {
        final int dim = geom.getCoordinateDimension();
        final int lrsPos = getLRSDim(geom);
        final boolean isLrs = lrsPos > 0;
        final Double[] ordinates = convertPositionSequence(geom.getPositions());
        final ElemInfo info = new ElemInfo(1);
        info.setElement(0, 1, ElementType.LINE_STRAITH_SEGMENTS, 0);
        return new SDOGeometry(
                new SDOGType(dim, lrsPos, TypeGeometry.LINE),
                geom.getSRID(),
                null,
                info,
                new Ordinates(ordinates));

    }

}
