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
abstract public class Position<P extends Position<P>> {

    protected final CoordinateReferenceSystem<P> crs;
    protected final double[] coords;

    public Position(CoordinateReferenceSystem<P> crs, double... coords) {
        if (coords.length != 0 && crs.getCoordinateDimension() != coords.length) {
            throw new IllegalArgumentException("Coordinate dimension doesn't correspond to array length.");
        }
        this.crs = crs;
        if (coords.length == 0) {
            this.coords = new double[0];
            //Arrays.fill(this.coords, Double.NaN);
        } else {
            double[] c = new double[crs.getCoordinateDimension()];
            System.arraycopy(coords, 0, c, 0, coords.length);
            this.coords = c;
        }
    }

    public CoordinateReferenceSystem<P> getCoordinateReferenceSystem() {
        return this.crs;
    }

    public double[] toArray(double[] dest) {
        if (isEmpty()) {
            return new double[0];
        }
        if (dest == null || dest.length < this.crs.getCoordinateDimension()) {
            dest = new double[crs.getCoordinateDimension()];
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
    public double getCoordinate(CoordinateSystemAxis axis) {
        int idx = this.crs.getAxisIndex(axis);
        if (idx == -1) throw new IllegalArgumentException("Not an axis of this coordinate reference system.");
        return crs.getNormalizedOrder().normalizedToCrsDefined(coords)[idx];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;

        Position position = (Position) o;

        if (!Arrays.equals(coords, position.toArray(null))) return false;
        if (!crs.equals(position.getCoordinateReferenceSystem())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = crs.hashCode();
        result = 31 * result + Arrays.hashCode(coords);
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(coords);
    }
}
