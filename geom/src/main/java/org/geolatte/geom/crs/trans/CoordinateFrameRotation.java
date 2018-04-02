package org.geolatte.geom.crs.trans;

import static org.geolatte.geom.DecimalDegree.secondsToRadians;

/**
 * The Coordinate Frame Rotation transformation ( a 7-parameter Helmert Transformation in the geocentric domain).
 *
 * This implements the EPSG 1032 method It is used to
 * perform the TOWGS84 transforms.
 *
 *
 * Created by Karel Maesen, Geovise BVBA on 02/04/2018.
 */
public class CoordinateFrameRotation implements CoordinateOperation, WithEpsgGOperationMethod{

	final private double tx;
	final private double ty;
	final private double tz;
	final private double rx;
	final private double ry;
	final private double rz;
	final private double m;


	public static PositionVectorTransformation fromTOWGS84(double[] params) {
		return new PositionVectorTransformation( params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
	}


	/**
	 *
	 * @param tx X-axis translation in meters
	 * @param ty Y-axis translation in meters
	 * @param tz Z-axis translation in meters*
	 * @param rx X-axis rotation in arc-seconds
	 * @param ry Y-azis rotation in arc-seconds
	 * @param rz Z-azis rotation in arc-seconds
	 * @param ds scale correction in parts per million (ppm)
	 */
	public CoordinateFrameRotation(double tx, double ty, double tz, double rx, double ry, double rz, double ds) {
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
		this.rx = secondsToRadians(rx);
		this.ry = secondsToRadians(ry);
		this.rz = secondsToRadians(rz);
		this.m = ppmToScaleFactor(ds);
	}


	private double ppmToScaleFactor(double ds) {
		return 1 + ds * 1E-6;
	}

	@Override
	public boolean isReversible() {
		return true;
	}

	@Override
	public int inCoordinateDimension() {
		return 3;
	}

	@Override
	public int outCoordinateDimension() {
		return 3;
	}

	@Override
	public void forward(double[] inCoordinate, double[] outCoordinate) {

		double sx = inCoordinate[0];
		double sy = inCoordinate[1];
		double sz = inCoordinate[2];

		outCoordinate[0] = m * (sx + rz*sy - ry*sz) + tx;
		outCoordinate[1] = m * (-rz * sx + sy + rx*sz) + ty;
		outCoordinate[2] = m * (+ry*sx - rx*sy + sz) + tz;
	}

	@Override
	public void reverse(double[] inCoordinate, double[] outCoordinate) {
		// the rotation matrix is orthogonal, so the inverse matrix equals the transpose
		double sx = inCoordinate[0];
		double sy = inCoordinate[1];
		double sz = inCoordinate[2];

		outCoordinate[0] = (1/m) * (-sx - rz*sy + ry*sz) - tx;
		outCoordinate[1] = (1/m) * (rz * sx - sy - rx*sz) - ty;
		outCoordinate[2] = (1/m) * (-ry*sx + rx*sy - sz) - tz;

	}

	@Override
	public String getMethodId() {
		return "1032";
	}

}
