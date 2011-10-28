package org.geolatte.geom;

import org.geolatte.geom.crs.CrsId;

/**
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Envelope {

    final CrsId crsId;
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    public Envelope(Point lowerLeft, Point upperRight, CrsId crsId) {
        this(lowerLeft.getX(), lowerLeft.getY(), upperRight.getX(), upperRight.getY(), crsId);
    }

    public Envelope(double minX, double minY, double maxX, double maxY, CrsId crsId) {
        if (minX > maxX || minY > maxY)
            throw new IllegalArgumentException("Valid Bounding boxes require minX <= maxX and minY <= maxY");
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.crsId = crsId != null ? crsId : CrsId.UNDEFINED;
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
        return createPoint(minX, minY);
    }

    public Point upperRight() {
        return createPoint(maxX, maxY);
    }

    public Point upperLeft() {
        return createPoint(minX, maxY);
    }

    public Point lowerRight() {
        return createPoint(maxX, minY);
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
        if (b1.getCrsId() != b2.getCrsId()) throw new IllegalArgumentException("Envelopes have different CRS.");
        if (b1 == null) return b2;
        if (b2 == null) return b1;
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
        if (this.getCrsId() != bbox.getCrsId()) throw new IllegalArgumentException("Envelopes have different CRS.");
        double minX = Math.max(bbox.getMinX(), getMinX());
        double minY = Math.max(bbox.getMinY(), getMinY());
        double maxX = Math.min(bbox.getMaxX(), getMaxX());
        double maxY = Math.min(bbox.getMaxY(), getMaxY());
        if (minX > maxX || minY > maxY)
            return new Envelope(0, 0, 0, 0, this.getCrsId());

        return new Envelope(minX, minY, maxX, maxY, this.getCrsId());

    }

    public boolean isEmpty() {
        return getWidth() == 0 || getHeight() == 0;
    }

    /**
     * Checks whether this BoundingBox falls within the specified BoundingBox
     *
     * @param bbox
     */
    public boolean isWithin(Envelope bbox) {
        return bbox.getMinX() <= this.getMinX() &&
                bbox.getMaxX() >= this.getMaxX() &&
                bbox.getMinY() <= this.getMinY() &&
                bbox.getMaxY() >= this.getMaxY();
    }

    public boolean contains(Point p) {
        return getMinX() <= p.getX() && getMaxX() >= p.getX() && getMinY() <= p.getY() && getMaxY() >= p.getY();
    }

    private Point createPoint(double x, double y){
        return Point.create(new double[]{x, y}, DimensionalFlag.XY, crsId.getCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Envelope that = (Envelope) o;

        if (Double.compare(that.maxX, maxX) != 0) return false;
        if (Double.compare(that.maxY, maxY) != 0) return false;
        if (Double.compare(that.minX, minX) != 0) return false;
        if (Double.compare(that.minY, minY) != 0) return false;

        return true;
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
