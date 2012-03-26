package org.geolatte.geom;

import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 *
 * <p>An empty Envelope has Double.NaN for min. and max. X and Y coordinates.</p>
 */
public class Envelope {

    public static final Envelope EMPTY = new Envelope(Double.NaN, Double.NaN, Double.NaN, Double.NaN, CrsId.UNDEFINED);

    private final CrsId crsId;
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    public Envelope(Point lowerLeft, Point upperRight, CrsId crsId) {
        this(lowerLeft.getX(), lowerLeft.getY(), upperRight.getX(), upperRight.getY(), crsId);
    }

    public Envelope(double minX, double minY, double maxX, double maxY) {
        this(minX, minY, maxX, maxY, CrsId.UNDEFINED);
    }

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

    public CrsId getCrsId(){
        return this.crsId;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public Point lowerLeft() {
        return Points.create(minX, minY, crsId);
    }

    public Point upperRight() {
        return Points.create(maxX, maxY, crsId);
    }

    public Point upperLeft() {
        return Points.create(minX, maxY, crsId);
    }

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
     * Intersects the specified BoundingBox with this BoundingBox and returns the result.
     *
     * @param bbox the BoundingBox to intersect.
     * @return
     */
    public Envelope intersect(Envelope bbox) {
        if (this.isEmpty() || bbox.isEmpty()) return EMPTY;
        if (!this.getCrsId().equals(bbox.getCrsId())) throw new IllegalArgumentException("Envelopes have different CRS.");
        double minX = Math.max(bbox.getMinX(), getMinX());
        double minY = Math.max(bbox.getMinY(), getMinY());
        double maxX = Math.min(bbox.getMaxX(), getMaxX());
        double maxY = Math.min(bbox.getMaxY(), getMaxY());
        if (minX > maxX || minY > maxY)
            return EMPTY;
        return new Envelope(minX, minY, maxX, maxY, this.getCrsId());

    }

    public boolean isEmpty() {
        return Double.isNaN(this.minX)  || Double.isNaN(this.minY) ||
                Double.isNaN(this.maxX) || Double.isNaN(this.maxY);
    }

    /**
     * Checks whether this <code>Envelope</code> is contained within the specified BoundingBox
     *
     * @param bbox
     */
    public boolean within(Envelope bbox) {
        if (isEmpty()) return true;
        if (bbox.isEmpty()) return false;
        if (!this.getCrsId().equals(bbox.getCrsId()))
            throw new IllegalArgumentException("Envelopes have different CRS.");
        return bbox.getMinX() <= this.getMinX() &&
                bbox.getMaxX() >= this.getMaxX() &&
                bbox.getMinY() <= this.getMinY() &&
                bbox.getMaxY() >= this.getMaxY();
    }

    /**
     * Checks whether this <code>Envelope</code> contains the specifies <code>Envelope</code>.
     * @param other
     * @return
     */
    public boolean contains(Envelope other) {
        return other.within(this);
    }

    public boolean contains(Point p) {
        if (isEmpty()) return false;
        return getMinX() <= p.getX() && getMaxX() >= p.getX() && getMinY() <= p.getY() && getMaxY() >= p.getY();
    }

    /**
     * The intersection of this <code>Envelope</code> and the other <code>Envelope</code>
     * is not null
     *
     * @param other
     * @return
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
        if (this.getCrsId() != that.getCrsId()) return false;
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
