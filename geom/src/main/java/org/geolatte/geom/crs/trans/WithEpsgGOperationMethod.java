package org.geolatte.geom.crs.trans;

/**
 * Created by Karel Maesen, Geovise BVBA on 02/04/2018.
 */
public interface WithEpsgGOperationMethod {

	/**
	 * Returns the EPSG code for the operation method
	 * @return The EPSG Code for the operation method
	 */
	String getMethodId();

}
