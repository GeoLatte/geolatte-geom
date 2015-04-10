package org.geolatte.geom.codec.db.oracle;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.GeometryCollection;
import org.geolatte.geom.GeometryType;
import org.geolatte.geom.Position;

/**
 * Created by Karel Maesen, Geovise BVBA on 01/04/15.
 */
public class SdoGeometryCollectionEncoder extends AbstractSDOEncoder {

    @Override
    public <P extends Position> boolean accepts(Geometry<P> geom) {
        return GeometryType.GEOMETRYCOLLECTION.equals(geom.getGeometryType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Position, G extends Geometry<P>> SDOGeometry encode(G geom) {
        GeometryCollection<P, Geometry<P>> collection = (GeometryCollection<P, Geometry<P>>)geom;
        final SDOGeometry[] sdoElements = new SDOGeometry[collection.getNumGeometries()];
        for (int i = 0; i < collection.getNumGeometries(); i++) {
            final Geometry component = collection.getGeometryN(i);
            sdoElements[i] = Encoders.encode(component);
        }
        final SDOGeometry ccollect = join(sdoElements);
        return ccollect;
    }


	public SDOGeometry join(SDOGeometry[] sdoElements) {
		if ( sdoElements == null || sdoElements.length == 0 ) {
			return new SDOGeometry(new SDOGType( 2, 0, TypeGeometry.COLLECTION ), 0, null, null,null );
		}
		else {
			final SDOGeometry firstElement = sdoElements[0];
			final int dim = firstElement.getGType().getDimension();
			final int lrsDim = firstElement.getGType().getLRSDimension();
            final int srid = firstElement.getSRID();
            SDOGType gtype =  new SDOGType( dim, lrsDim, TypeGeometry.COLLECTION );
			int ordinatesOffset = 1;
            ElemInfo resultInfo = null;
            Ordinates resultOrdinates = null;
			for ( int i = 0; i < sdoElements.length; i++ ) {
				final ElemInfo element = sdoElements[i].getInfo();
				final Double[] ordinates = sdoElements[i].getOrdinates().getOrdinateArray();
				if ( element != null && element.getSize() > 0 ) {
					final int shift = ordinatesOffset - element.getOrdinatesOffset( 0 );
					SDOGeometry.shiftOrdinateOffset(element, shift);
					resultInfo = addElementInfo(resultInfo, element);
					resultOrdinates = addOrdinates( resultOrdinates, ordinates );
					ordinatesOffset += ordinates.length;
				}
			}
            return new SDOGeometry(gtype, srid, null, resultInfo, resultOrdinates);
		}
    }

}
