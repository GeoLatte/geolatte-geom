package org.geolatte.geom.codec.db.oracle;

import com.vividsolutions.jts.geom.Coordinate;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class SdoPolygonEncoder extends AbstractSDOEncoder {
    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return GeometryType.POLYGON.equals(geom.getGeometryType());
    }

    @Override
    public <P extends Position, G extends Geometry<P>> SDOGeometry encode(G geom) {
        final int dim = geom.getCoordinateDimension();
        final int lrsPos = getLRSDim(geom);

        SDOGType sdogType = new SDOGType(dim, lrsPos, TypeGeometry.POLYGON);
        SDOGeometry base = new SDOGeometry(sdogType, geom.getSRID(), null, null, null);
        Polygon<?> polygon = (Polygon<?>)geom;
        return addPolygon(base, polygon);
    }

}
