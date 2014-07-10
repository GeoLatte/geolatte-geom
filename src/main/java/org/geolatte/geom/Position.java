package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateSystemAxis;

import java.util.Arrays;

/**
 * Represents a position in the {@code CoordinateReferenceSystem}
 * <p/>
 * A {@code Position} is represented by an array of coordinates and a {@code CoordinateReferenceSystem}.
 * <p/>
 * There must be at least two coordinates. The order of coordinates must follow x, y, z, measure order (easting,
 * northing, altitude, measure) for coordinates in a projected coordinate reference system, or
 * longitude, latitude, altitude, measure for coordinates in a geographic coordinate reference system).
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/19/14
 */
abstract public class Position{

    protected final double[] coords;

    public Position(double... coords) {
        if (coords.length == 0) {
            this.coords = new double[0];
            //Arrays.fill(this.coords, Double.NaN);
        } else {
            double[] c = new double[getCoordinateDimension()];
            System.arraycopy(coords, 0, c, 0, coords.length);
            this.coords = c;
        }
    }

    public double[] toArray(double[] dest) {
        if (isEmpty()) {
            return new double[0];
        }
        if (dest == null) {
            dest = new double[getCoordinateDimension()];
        }
        System.arraycopy(this.coords, 0, dest, 0, this.coords.length);
        return dest;
    }

    public boolean isEmpty() {
        return this.coords.length == 0;
    }

    /**
     * Returns the coordinate at the specified index
     * <p/>
     * Note that the index here refers to the coordinates ordered in a normalized order (
     *
     * @param idx
     * @return
     */
    public double getCoordinate(int idx) {
        return isEmpty() ? Double.NaN : this.coords[idx];
    }

    /**
     * Gets the coordinate value for the specified axis
     *
     * @param axis the CoordinateSystemAxis for which to get the coordinate
     * @return the coordinate value for the specified axis
     * @throws IllegalArgumentException if the specified axis is not present in the {@code CoordinateReferenceSystem}
     *                                  of this instance
     */
    public double getCoordinate(CoordinateSystemAxis axis, CoordinateReferenceSystem<?> crs) {
        int idx = crs.getAxisIndex(axis);
        if (idx == -1) throw new IllegalArgumentException("Not an axis of this coordinate reference system.");
        return crs.getNormalizedOrder().normalizedToCrsDefined(coords)[idx];
    }

    abstract public int getCoordinateDimension();

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
