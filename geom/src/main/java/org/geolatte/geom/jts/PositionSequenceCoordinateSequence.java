package org.geolatte.geom.jts;

import org.geolatte.geom.PositionSequence;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Envelope;

import java.io.Serializable;

/**
 * A JTS {@code CoordinateSequence} view of a geolatte {@code PositionSequence}, with copy on write.
 *
 * <p>It reads the coordinates from the position sequence, so converting a geometry to JTS copies no coordinates. A
 * {@code PositionSequence} is immutable, so the first {@link #setOrdinate(int, int, double)} (for example by
 * {@code Geometry.apply(CoordinateSequenceFilter)}) first copies the coordinates into an array of this sequence, and
 * writes there: changing the JTS geometry never changes the geolatte geometry it was converted from.</p>
 *
 * <p>The ordinates are those of the positions, in their order: X, Y, then Z if the positions have a vertical
 * coordinate, then M if they have a measure.</p>
 */
final class PositionSequenceCoordinateSequence implements CoordinateSequence, Serializable {

    private static final long serialVersionUID = 1L;

    private final int dimension;
    private final int measures;
    // the source of the coordinates until the first write, then null
    private PositionSequence<?> source;
    // the coordinates after the first write (packed, dimension per position), else null
    private double[] ordinates;
    private final int size;

    PositionSequenceCoordinateSequence(PositionSequence<?> source) {
        this.source = source;
        this.dimension = source.getCoordinateDimension();
        this.measures = source.getPositionFactory().hasMComponent() ? 1 : 0;
        this.size = source.size();
    }

    private PositionSequenceCoordinateSequence(double[] ordinates, int dimension, int measures) {
        this.ordinates = ordinates;
        this.dimension = dimension;
        this.measures = measures;
        this.size = ordinates.length / dimension;
    }

    /**
     * @return the position sequence this sequence reads from, or null once it has been written to
     */
    PositionSequence<?> unmodifiedSource() {
        return source;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public int getMeasures() {
        return measures;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public double getOrdinate(int index, int ordinateIndex) {
        if (ordinateIndex < 0 || ordinateIndex >= dimension) {
            throw new IllegalArgumentException("Ordinate index " + ordinateIndex + " is not supported.");
        }
        PositionSequence<?> s = source;
        if (s != null) return s.getCoordinate(index, ordinateIndex);
        checkIndex(index);
        return ordinates[index * dimension + ordinateIndex];
    }

    @Override
    public double getX(int index) {
        return getOrdinate(index, 0);
    }

    @Override
    public double getY(int index) {
        return getOrdinate(index, 1);
    }

    @Override
    public Coordinate getCoordinate(int i) {
        Coordinate coordinate = createCoordinate();
        getCoordinate(i, coordinate);
        return coordinate;
    }

    @Override
    public Coordinate getCoordinateCopy(int i) {
        return getCoordinate(i);
    }

    @Override
    public void getCoordinate(int index, Coordinate coordinate) {
        coordinate.x = getX(index);
        coordinate.y = getY(index);
        // only the ordinates that both the sequence and the coordinate have
        if (hasZ() && !(coordinate instanceof CoordinateXY || coordinate instanceof CoordinateXYM)) {
            coordinate.setZ(getZ(index));
        }
        if (hasM() && (coordinate instanceof CoordinateXYM || coordinate instanceof CoordinateXYZM)) {
            coordinate.setM(getM(index));
        }
    }

    @Override
    public void setOrdinate(int index, int ordinateIndex, double value) {
        if (ordinateIndex < 0 || ordinateIndex >= dimension) {
            throw new IllegalArgumentException("Ordinate index " + ordinateIndex + " is not supported.");
        }
        checkIndex(index);
        if (ordinates == null) {
            double[] copy = new double[size * dimension];
            double[] buffer = new double[dimension];
            for (int i = 0; i < size; i++) {
                source.getCoordinates(i, buffer);
                System.arraycopy(buffer, 0, copy, i * dimension, dimension);
            }
            ordinates = copy;
            source = null;
        }
        ordinates[index * dimension + ordinateIndex] = value;
    }

    @Override
    public Coordinate[] toCoordinateArray() {
        Coordinate[] coordinates = new Coordinate[size];
        for (int i = 0; i < size; i++) coordinates[i] = getCoordinate(i);
        return coordinates;
    }

    @Override
    public Envelope expandEnvelope(Envelope envelope) {
        for (int i = 0; i < size; i++) envelope.expandToInclude(getX(i), getY(i));
        return envelope;
    }

    @Override
    public PositionSequenceCoordinateSequence copy() {
        PositionSequence<?> s = source;
        // a view of the same (immutable) source is an independent copy
        return s != null ? new PositionSequenceCoordinateSequence(s)
                : new PositionSequenceCoordinateSequence(ordinates.clone(), dimension, measures);
    }

    /**
     * @deprecated use {@link #copy()}
     */
    @Deprecated
    @Override
    public Object clone() {
        return copy();
    }

    @Override
    public String toString() {
        return CoordinateSequences.toString(this);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " in a sequence of size " + size);
        }
    }
}
