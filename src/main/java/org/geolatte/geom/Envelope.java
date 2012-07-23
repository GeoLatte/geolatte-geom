package org.geolatte.geom;

import org.geolatte.geom.crs.CrsId;

/**
 * An envelope or bounding box implementation.
 *
 * <p>An <code>Envelope</code> is characterised by the minimum and maximum x and y coordinates. An <code>Envelope</code>
 * is empty if the set of enclosed points is empty. </p>
 * @author Karel Maesen, Geovise BVBA, 2011
 *
 * <p>An empty Envelope has Double.NaN for min. and max. X and Y coordinates.</p>
 */
public class Envelope {

    /**
     * An empty <code>Envelope</code>
     */
    public static final Envelope EMPTY = new Envelope(Double.NaN, Double.NaN, Double.NaN, Double.NaN, CrsId.UNDEFINED);

    private final CrsId crsId;
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    /**
     * Creates an instance from specified lower-left and upper-right <code>Point</code>s.
     * @param lowerLeft the <code>Point</code> designating the lower-left coordinates
     * @param upperRight the <code>Point</code> designating the upper-right coordinates
     * @param crsId the <code>CrsId</code> that identifies the <code>CoordinateReferenceSystem</code> for the coordinates
     * of the envelope.
     */
    public Envelope(Point lowerLeft, Point upperRight, CrsId crsId) {
        //TODO -- what if crsId and lowerleft/upperright.getCrsId() do not correspond?
        this(lowerLeft.getX(), lowerLeft.getY(), upperRight.getX(), upperRight.getY(), crsId);
    }

    /**
     * Create an instance using the specified coordinates.
     *
     * <p>The CrsId will be UNDEFINED.</p>
     *
     * @param minX minimum X-coordinate
     * @param minY minimum Y-coordinate
     * @param maxX minimum Y-coordinate
     * @param maxY maximum Y-coordinate
     */
    public Envelope(double minX, double minY, double maxX, double maxY) {
        this(minX, minY, maxX, maxY, CrsId.UNDEFINED);
    }

    /**
     * Create an instance using the specified coordinates and <code>CrsId</code>.
     *
     * @param minX minimum X-coordinate
     * @param minY minimum Y-coordinate
     * @param maxX minimum Y-coordinate
     * @param maxY maximum Y-coordinate
     * @param crsId the <code>CrsId</code> that identifies the <code>CoordinateReferenceSystem</code> for the coordinates
      * of the envelope.
      */
    public Envelope(double minX, double minY, double maxX, double maxY, CrsId crsId) {
        if (minX > maxX || minY > maxY) {
            this.minX = Double.NaN;
            this.minY = Double.NaN;
            this.maxX = Double.NaN;
            this.maxY = Double.NaN;
            this.crsId = CrsId.UNDEFINED;
        } else {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.crsId = crsId != null ? crsId : CrsId.UNDEFINED;
        }
    }

    /**
     * Returns the <code>CrsId</code> for this <code>Envelope</code>
     * @return
     */
    public CrsId getCrsId(){
        return this.crsId;
    }

    /**
     * Return the minimum X-coordinate.
     *
     * @return minimum X-coordinate
     */
    public double getMinX() {
        return minX;
    }

    /**
     * Return the minimum Y-coordinate.
     *
     * @return minimum Y-coordinate
     */
    public double getMinY() {
        return minY;
    }

    /**
     * Return the maximum X-coordinate.
     *
     * @return maximum X-coordinate
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * Return the maximum Y-coordinate.
     *
     * @return maximum Y-coordinate
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * returns the width of this <code>Envelope</code>.
     *
     * @return this <code>Envelope</code>'s width
     */
    public double getWidth() {
        return maxX - minX;
    }

    /**
     * returns the height of this <code>Envelope</code>.
     *
     * @return this <code>Envelope</code>'s height
     */
    public double getHeight() {
        return maxY - minY;
    }

    /**
     * Returns the lower-left point of this <code>Envelope</code>.
     *
     * @return the lower-left point
     */
    public Point lowerLeft() {
        return Points.create(minX, minY, crsId);
    }

    /**
     * Returns the upper-right point of this <code>Envelope</code>.
     *
     * @return the upper-right point
     */
    public Point upperRight() {
        return Points.create(maxX, maxY, crsId);
    }

    /**
     * Returns the upper-left point of this <code>Envelope</code>.
     *
     * @return the upper-left point
     */
    public Point upperLeft() {
        return Points.create(minX, maxY, crsId);
    }

    /**
     * Returns the lower-right point of this <code>Envelope</code>.
     *
     * @return the lower-right point
     */
    public Point lowerRight() {
        return Points.create(maxX, minY, crsId);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(this.crsId.toString());
        builder.append("LL: ")
                .append(minX)
                .append(",")
                .append(minY)
                .append(" - UR: ")
                .append(maxX)
                .append(",")
                .append(maxY);
        return builder.toString();
    }

    /**
     * Creates an <code>Envelope</code> that is the set-theoretic union of the specified <code>Envelopes</code>.
     * @param b1 first operand
     * @param b2 second operand
     * @return an <code>Envelope</code> that encompasses both operands.
     * @throws IllegalArgumentException when the operand <code>Envelope</code>s don't have the same coordinate reference system.
     */
    public static Envelope union(Envelope b1, Envelope b2) {
        if (b1 == null || b1.isEmpty()) return b2;
        if (b2 == null || b2.isEmpty()) return b1;
        if (! b1.getCrsId().equals(b2.getCrsId())) throw new IllegalArgumentException("Envelopes have different CRS.");
        double minX = Math.min(b1.getMinX(), b2.getMinX());
        double minY = Math.min(b1.getMinY(), b2.getMinY());
        double maxX = Math.max(b1.getMaxX(), b2.getMaxX());
        double maxY = Math.max(b1.getMaxY(), b2.getMaxY());
        return new Envelope(minX, minY, maxX, maxY, b1.getCrsId());
    }

    /**
     * Intersects the specified <code>Envelope</code> with this <code>Envelope</code> and returns the result.
     *
     * @param other the Envelope to intersect with this instance
     * @return the set-theoretic intersection of this Envelope and the specified Envelope.
     * @throws IllegalArgumentException when the specified <code>Envelope</code> doesn't have the same coordinate reference system as this instance.
     */
    public Envelope intersect(Envelope other) {
        if (this.isEmpty() || other.isEmpty()) return EMPTY;
        if (!this.getCrsId().equals(other.getCrsId())) throw new IllegalArgumentException("Envelopes have different CRS.");
        double minX = Math.max(other.getMinX(), getMinX());
        double minY = Math.max(other.getMinY(), getMinY());
        double maxX = Math.min(other.getMaxX(), getMaxX());
        double maxY = Math.min(other.getMaxY(), getMaxY());
        if (minX > maxX || minY > maxY)
            return EMPTY;
        return new Envelope(minX, minY, maxX, maxY, this.getCrsId());

    }

    /**
     * Checks whether this <code>Envelope</code> is empty.
     *
     * @return true iff this instance  is empty (the empty set).
     */
    public boolean isEmpty() {
        return Double.isNaN(this.minX)  || Double.isNaN(this.minY) ||
                Double.isNaN(this.maxX) || Double.isNaN(this.maxY);
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
        if (!this.getCrsId().equals(other.getCrsId()))
            throw new IllegalArgumentException("Envelopes have different CRS.");
        return other.getMinX() <= this.getMinX() &&
                other.getMaxX() >= this.getMaxX() &&
                other.getMinY() <= this.getMinY() &&
                other.getMaxY() >= this.getMaxY();
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
    public boolean contains(Point p) {
        if (!this.getCrsId().equals(p.getCrsId()))
            throw new IllegalArgumentException("Envelopes have different CRS.");
        if (isEmpty()) return false;
        return getMinX() <= p.getX() && getMaxX() >= p.getX() && getMinY() <= p.getY() && getMaxY() >= p.getY();
    }

    /**
     * Checks whether this <code>Envelope</code> intersects the specifies <code>Envelope</code>.
     *
     * <p>Two instances intersect when their set-theoretic intersection is non-empty.</p>
     *
     * @param other the other <code>Envelope</code>
     * @return true iff this instance intersects with the other <code>Envelope</code>
     * @throws IllegalArgumentException when the specified <code>Envelope</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean intersects(Envelope other) {
        return !(isEmpty() || other.isEmpty()) &&
                !(this.maxX < other.minX || this.minX > other.maxX || this.maxY < other.minY || this.minY > other.maxY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Envelope)) return false;
        Envelope that = (Envelope) o;
        if (this.isEmpty() && that.isEmpty()) return true;
        if (!this.getCrsId().equals(that.getCrsId()) ) return false;
        if (Double.compare(that.maxX, maxX) != 0) return false;
        if (Double.compare(that.maxY, maxY) != 0) return false;
        if (Double.compare(that.minX, minX) != 0) return false;
        return Double.compare(that.minY, minY) == 0;

    }



    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = minX != +0.0d ? Double.doubleToLongBits(minX) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = maxX != +0.0d ? Double.doubleToLongBits(maxX) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = minY != +0.0d ? Double.doubleToLongBits(minY) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = maxY != +0.0d ? Double.doubleToLongBits(maxY) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
