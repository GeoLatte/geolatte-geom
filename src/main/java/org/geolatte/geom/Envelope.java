package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * An envelope or bounding box implementation.
 *
 * <p>An <code>Envelope</code> is characterised by a lower-left and an upper-right coordinate.</p>
 * <p>An <code>Envelope</code> is empty if the set of enclosed points is empty. </p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 *         <p/>
 *         <p>An empty Envelope has Double.NaN for min. and max. X and Y coordinates.</p>
 */
public class Envelope<P extends Position> {

    private final CoordinateReferenceSystem<P> crs;
    private final P lowerLeft;
    private final P upperRight;


    /**
     * Creates an empty Envelop
     */
    public Envelope(CoordinateReferenceSystem<P> crs) {
        this.crs = crs;
        lowerLeft = Positions.mkPosition(crs);
        upperRight = lowerLeft;
    }
    /**
     * Creates an instance from specified lower-left and upper-right <code>Point</code>s.
     *
     * @param lowerLeft  the <code>Point</code> designating the lower-left coordinates
     * @param upperRight the <code>Point</code> designating the upper-right coordinates
     *                   of the envelope.
     */
    public Envelope(P lowerLeft, P upperRight, CoordinateReferenceSystem<P> crs) {
        if (lowerLeft == null || upperRight == null)
            throw new IllegalArgumentException("Envelope requires non-null Positions");
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
        this.crs = crs;
    }


    /**
     * Create an instance using the specified coordinates and <code>CoordinateReferenceSystem</code>.
     *
     * @param minC1 minimum first coordinate
     * @param minC2 minimum second coordinate
     * @param maxC1 maximum first coordinate
     * @param maxC2 maximum second coordinate
     * @param crs   the <code>CoordinateReferenceSystem</code> for the coordinates
     *              of the envelope.
     */
    public Envelope(double minC1, double minC2, double maxC1, double maxC2, CoordinateReferenceSystem<P> crs) {
        if (crs == null) {
            throw new IllegalArgumentException("Null CRS argument not allowed");
        }

        if (minC1 > maxC1) {
            double h = maxC1;
            maxC1 = minC1;
            minC1 = h;
        }
        if (minC2 > maxC2) {
            double h = maxC2;
            maxC2 = minC2;
            minC2 = h;
        }

        this.crs =  crs;
        this.lowerLeft = Positions.mkPosition(crs, new double[]{minC1, minC2});
        this.upperRight = Positions.mkPosition(crs, new double[]{maxC1, maxC2});


    }

    /**
     * Returns the <code>CoordinateReferenceSystem</code> for this <code>Envelope</code>
     *
     * @return
     */
    public CoordinateReferenceSystem<P> getCoordinateReferenceSystem() {
        return this.crs;
    }


    /**
     * Returns the lower-left point of this <code>Envelope</code>.
     *
     * @return the lower-left point
     */
    public P lowerLeft() {
        return this.lowerLeft;
    }

    /**
     * Returns the upper-right point of this <code>Envelope</code>.
     *
     * @return the upper-right point
     */
    public P upperRight() {
        return this.upperRight;
    }

    /**
     * Returns the upper-left point of this <code>Envelope</code>.
     *
     * @return the upper-left point
     */
    public P upperLeft() {
        return Positions.mkPosition(getCoordinateReferenceSystem(), lowerLeft.getCoordinate(0), upperRight.getCoordinate(1));
    }

    /**
     * Returns the lower-right point of this <code>Envelope</code>.
     *
     * @return the lower-right point
     */
    public P lowerRight() {
        return Positions.mkPosition(getCoordinateReferenceSystem(), upperRight.getCoordinate(0), lowerLeft.getCoordinate(1));
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(getCoordinateReferenceSystem().getCrsId().toString());
        builder.append("LL: ")
                .append(this.lowerLeft.toString())
                .append(" - UR: ")
                .append(this.upperRight.toString());
        return builder.toString();
    }

    //TODO get rid of minCx/maxCX public methods
    public double getMinC0() {
        return lowerLeft.getCoordinate(0);
    }

    public double getMinC1() {
        return lowerLeft.getCoordinate(1);
    }

    public double getMaxC0() {
        return upperRight.getCoordinate(0);
    }

    public double getMaxC1() {
        return upperRight.getCoordinate(1);
    }

    /**
     * Creates an <code>Envelope</code> that is the set-theoretic union of this {@code Envelope} with the specified {@code Envelope}
     *
     * @param other other Envelope
     * @return an <code>Envelope</code> that encompasses both operands.
     * @throws IllegalArgumentException when the operand <code>Envelope</code>s don't have the same coordinate reference system.
     */
    public Envelope<P> union(Envelope<P> other) {
        if (other == null || other.isEmpty()) return this;
        if (!this.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem()))
            throw new IllegalArgumentException("Envelopes have different CRS.");
        double minC0 = Math.min(getMinC0(), other.getMinC0());
        double minC1 = Math.min(getMinC1(), other.getMinC1());
        double maxC0 = Math.max(getMaxC0(), other.getMaxC0());
        double maxC1 = Math.max(getMaxC1(), other.getMaxC1());
        return new Envelope<P>(minC0, minC1, maxC0, maxC1, this.getCoordinateReferenceSystem());
    }

    /**
     * Intersects the specified <code>Envelope</code> with this <code>Envelope</code> and returns the result.
     *
     * @param other the Envelope to intersect with this instance
     * @return the set-theoretic intersection of this Envelope and the specified Envelope.
     * @throws IllegalArgumentException when the specified <code>Envelope</code> doesn't have the same coordinate reference system as this instance.
     */
    public Envelope intersect(Envelope other) {
        if (this.isEmpty() || other.isEmpty()) return mkEmpty();
        if (!this.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem()))
            throw new IllegalArgumentException("Envelopes have different CRS.");
        double minC0 = Math.max(getMinC0(), other.getMinC0());
        double minC1 = Math.max(getMinC1(), other.getMinC1());
        double maxC0 = Math.min(getMaxC0(), other.getMaxC0());
        double maxC1 = Math.min(getMaxC1(), other.getMaxC1());
        if (minC0 > maxC0 || minC1 > maxC1)
            return mkEmpty();
        return new Envelope<P>(minC0, minC1, maxC0, maxC1, this.getCoordinateReferenceSystem());

    }

    private Envelope<P> mkEmpty() {
        return new Envelope<P>(Double.NaN, Double.NaN, Double.NaN, Double.NaN, getCoordinateReferenceSystem());
    }

    /**
     * Checks whether this <code>Envelope</code> is empty.
     *
     * @return true iff this instance  is empty (the empty set).
     */
    public boolean isEmpty() {
        return Double.isNaN(getMinC0()) || Double.isNaN(getMinC1()) ||
                Double.isNaN(getMaxC0()) || Double.isNaN(getMaxC1());
    }

    /**
     * Checks whether this <code>Envelope</code> is contained within the specified <code>Envelope</code>
     *
     * @param other the other <code>Envelope</code>
     * @return true iff this instance is contained within the specified <code>Envelope</code>
     * @throws IllegalArgumentException when the specified <code>Envelope</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean within(Envelope other) {
        if (isEmpty()) return true;
        if (other.isEmpty()) return false;
        if (!this.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem()))
            throw new IllegalArgumentException("Envelopes have different CRS.");
        return other.getMinC0() <= this.getMinC0() &&
                other.getMaxC0() >= this.getMaxC0() &&
                other.getMinC1() <= this.getMinC1() &&
                other.getMaxC1() >= this.getMaxC1();
    }

    /**
     * Checks whether this <code>Envelope</code> contains the specifies <code>Envelope</code>.
     *
     * @param other the other <code>Envelope</code>
     * @return true iff this instance contains the specified <code>Envelope</code>
     * @throws IllegalArgumentException when the specified <code>Envelope</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean contains(Envelope other) {
        return other.within(this);
    }

    /**
     * Checks whether this <code>Envelope</code> contains the specifies <code>Envelope</code>.
     *
     * @param p the <code>Point</code>
     * @return true iff this instance contains the specified <code>Point</code>
     * @throws IllegalArgumentException when the specified <code>Point</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean contains(P p) {
        //TODO -- is this test required??
        if (!p.getClass().equals(this.getCoordinateReferenceSystem().getPositionClass()))
            throw new IllegalArgumentException("Position and envelope of different types");
        if (isEmpty()) return false;
        return getMinC0() <= p.getCoordinate(0) &&
                getMaxC0() >= p.getCoordinate(0) &&
                getMinC1() <= p.getCoordinate(1) &&
                getMaxC1() >= p.getCoordinate(1);
    }

    /**
     * Checks whether this <code>Envelope</code> intersects the specifies <code>Envelope</code>.
     * <p/>
     * <p>Two instances intersect when their set-theoretic intersection is non-empty.</p>
     *
     * @param other the other <code>Envelope</code>
     * @return true iff this instance intersects with the other <code>Envelope</code>
     * @throws IllegalArgumentException when the specified <code>Envelope</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean intersects(Envelope other) {
        return !(isEmpty() || other.isEmpty()) &&
                !(this.getMaxC0() < other.getMinC0() ||
                        this.getMinC0() > other.getMaxC0() ||
                        this.getMaxC1() < other.getMinC1() ||
                        this.getMinC1() > other.getMaxC1());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Envelope envelope = (Envelope) o;

        if (crs != null ? !crs.equals(envelope.crs) : envelope.crs != null) return false;
        if (this.isEmpty() && envelope.isEmpty()) return true;
        if (lowerLeft != null ? !lowerLeft.equals(envelope.lowerLeft) : envelope.lowerLeft != null) return false;
        if (upperRight != null ? !upperRight.equals(envelope.upperRight) : envelope.upperRight != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = crs != null ? crs.hashCode() : 0;
        result = 31 * result + (lowerLeft != null ? lowerLeft.hashCode() : 0);
        result = 31 * result + (upperRight != null ? upperRight.hashCode() : 0);
        return result;
    }
}
