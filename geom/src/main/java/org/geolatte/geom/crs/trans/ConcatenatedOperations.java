package org.geolatte.geom.crs.trans;

import org.geolatte.geom.crs.GeographicCoordinateReferenceSystem;

import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

/**
 * Factory for common ConcatenatedOperations
 * Created by Karel Maesen, Geovise BVBA on 02/04/2018.
 */
public class ConcatenatedOperations {

	/**
	 * Returns the Position Vector transformation for the geographic 3D domain.
	 *
	 * This corresponds to EPSG method 1037
	 * @return
	 */
	public ConcatenatedOperation PositionVectorTransformation3D(GeographicCoordinateReferenceSystem source, GeographicCoordinateReferenceSystem target) {
		if ( target.equals( WGS84 ) ) {
			return new ConcatenatedOperation.Builder()
					.reverse( new Geographic3DTo2DConversion() )
					.forward( new GeographicToGeocentricConversion( source ) )
					.forward( PositionVectorTransformation.fromTOWGS84( source.getDatum().getToWGS84() ) )
					.reverse( new GeographicToGeocentricConversion( target ) )
					.build();
		}
		else {
			return new ConcatenatedOperation.Builder()
					.reverse( new Geographic3DTo2DConversion() )
					.forward( new GeographicToGeocentricConversion( source ) )
					.forward( PositionVectorTransformation.fromTOWGS84( source.getDatum().getToWGS84() ) )
					.reverse( PositionVectorTransformation.fromTOWGS84( target.getDatum().getToWGS84() ) )
					.reverse( new GeographicToGeocentricConversion( target ) )
					.build();
		}
	}

}
