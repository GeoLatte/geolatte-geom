package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class SdoMultiLineStringEncoder extends AbstractSDOEncoder {
    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return GeometryType.MULTILINESTRING.equals(geom.getGeometryType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Position, G extends Geometry<P>> SDOGeometry encode(G geom) {
        MultiLineString<P> multiLineString = (MultiLineString<P>)geom;
        final int dim = multiLineString.getCoordinateDimension();
        final int lrsDim = getLRSDim(multiLineString);
        final boolean isLrs = (lrsDim != 0);

        SDOGType sdoGtype = new SDOGType(dim, lrsDim, TypeGeometry.MULTILINE);

        final ElemInfo info = new ElemInfo(multiLineString.getNumGeometries());
        int oordinatesOffset = 1;
        Double[] ordinates = new Double[]{};
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            info.setElement(i, oordinatesOffset, ElementType.LINE_STRAITH_SEGMENTS, 0);
            ordinates = addOrdinates(ordinates, convertPositionSequence(multiLineString.getGeometryN(i).getPositions()));
            oordinatesOffset = ordinates.length + 1;
        }
        return new SDOGeometry(sdoGtype, geom.getSRID(), null, info, new Ordinates(ordinates));
    }

}
