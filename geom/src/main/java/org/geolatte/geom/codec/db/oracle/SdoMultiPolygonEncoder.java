package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.*;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class SdoMultiPolygonEncoder extends AbstractSDOEncoder {
    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return GeometryType.MULTIPOLYGON.equals(geom.getGeometryType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Position, G extends Geometry<P>> SDOGeometry encode(G geom) {
        MultiPolygon<P> multiPolygon = (MultiPolygon<P>)geom;
        final int dim = multiPolygon.getCoordinateDimension();
        final int lrsPos = getLRSDim(multiPolygon);

        SDOGType gType = new SDOGType(dim, lrsPos, TypeGeometry.MULTIPOLYGON);
        SDOGeometry sdoGeom = new SDOGeometry(gType, geom.getSRID(), null, null, null);
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            try {
                final Polygon<P> pg = multiPolygon.getGeometryN(i);
                sdoGeom = addPolygon(sdoGeom, pg);
            } catch (Exception e) {
                throw new RuntimeException("Found geometry that was not a geometry in MultiPolygon");
            }
        }
        return sdoGeom;
    }
}
