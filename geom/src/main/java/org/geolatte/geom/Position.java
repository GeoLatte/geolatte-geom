package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateSystemAxis;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a position in a coordinate system.
 *
 * <p>A {@code Position} is represented by an array of coordinates. There must be at least two coordinates.</p>
 *
 * <p>The order of coordinates must follow the order: x, y, z, measure (easting,
 * northing, altitude, measure) for coordinates in a projected coordinate reference system, or
 * longitude, latitude, altitude, measure for coordinates in a geographic coordinate reference system).
 * </p>
 *
 * <p> Usually the first coordinate value (X or Lon) increases along an EAST axis direction, and the second (Y or Lat)
 * along an NORTH axis direction. In some projected coordinate systems, such as those used in South-Africa,
 * the orientation is WEST for the first and SOUTH for the second coordinate value. To be sure of the interpretation
 * of the first and second coordinate, you can inspect the {@code CoordinateReferenceSystem} used with the
 * {@code Position}.
 * </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
abstract public class Position{

    protected final double[] coords;

    /**
     * Constructs an instance with the specified coordinates
     * @param coords
     */
    protected Position(double... coords) {
        if (coords.length == 0) {
            this.coords = new double[0];
            //Arrays.fill(this.coords, Double.NaN);
        } else {
            double[] c = new double[getCoordinateDimension()];
            System.arraycopy(coords, 0, c, 0, coords.length);
            this.coords = c;
        }
    }

    /**
     * Copies the coordinates of this {@code Position} in the specified Array, in normal order.
     *
     * <p>If the array is null or smaller than the coordinate dimension, then a new Array instance will be created.</p>
     *
     * @param dest the recipient of the coordinates of this instance (if large enough)
     * @return an array (possibly the same instance as specified by dest) holding the coordinates of this {@code Position}
     */
    public double[] toArray(double[] dest) {
        if (isEmpty()) {
            return new double[0];
        }
        int dim = getCoordinateDimension();
        if (dest == null || dest.length < dim) {
            dest = new double[dim];
        }
        System.arraycopy(this.coords, 0, dest, 0, dim);
        return dest;
    }


    public abstract int getCoordinateDimension();

    public boolean isEmpty() {
        return this.coords.length == 0;
    }

    /**
     * Returns the coordinate at the specified index
     *
     * <p>Note that the index here refers to the coordinates ordered in a normalized order.</p>
     *
     * @param idx the index of the coordinate (0-based)
     * @return the coordinate value at the specified index.
     */
    public double getCoordinate(int idx) {
        return isEmpty() ? Double.NaN : this.coords[idx];
    }

    /**
     * Gets the coordinate value for the specified axis
     *
     *
     * @param axis the CoordinateSystemAxis for which to get the coordinate
     * @return the coordinate value for the specified axis
     * @throws IllegalArgumentException if the specified axis is not present in the {@code CoordinateReferenceSystem}
     *                                  of this instance
     *
     * @Deprecated Use {@code getCoordinate(CoordinateSystemAxis axis)}
     *
     */
    @Deprecated
    public double getCoordinate(CoordinateSystemAxis axis, CoordinateReferenceSystem<?> crs) {
        int idx = crs.getAxisIndex(axis);
        if (idx == -1) throw new IllegalArgumentException("Not an axis of this coordinate reference system.");
        return getCoordinate(axis.getNormalOrder());
    }

    public double getCoordinate(CoordinateSystemAxis axis) {
        return getCoordinate(axis.getNormalOrder());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;

        Position position = (Position) o;

        if (!Arrays.equals(coords, position.toArray(null))) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coords);
    }

    @Override
    public String toString() {
        return Arrays.toString(coords);
    }
}
