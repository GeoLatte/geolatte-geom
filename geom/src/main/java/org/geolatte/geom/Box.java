package org.geolatte.geom;

import org.geolatte.geom.crs.CoordinateReferenceSystem;

import static java.lang.String.format;

/**
 * An multi-dimensional axis-aligned bounding box. 
 *
 * <p>A <code>Box</code> determines a sub characterised by a lower-left and an upper-right coordinate.</p>
 * <p>An <code>Box</code> is empty if the set of enclosed points are emptym, and has Double.NaN for its lowerleft/upperright coordinates.</p>
 *
 * @author Karel Maesen, Geovise BVBA, 2011
 */
public class Box<P extends Position> {

    private final CoordinateReferenceSystem<P> crs;
    private final P lowerLeft;
    private final P upperRight;


    /**
     * Creates an empty Box
     */
    public Box(CoordinateReferenceSystem<P> crs) {
        this.crs = crs;
        lowerLeft = Positions.mkPosition(crs);
        upperRight = lowerLeft;
    }
    /**
     * Creates an instance from specified lower-left and upper-right <code>Point</code>s.
     *
     *
     * @param lowerLeft  the <code>Point</code> designating the lower-left coordinates
     * @param upperRight the <code>Point</code> designating the upper-right coordinates
     *                   of the envelope.
     */
    public Box(P lowerLeft, P upperRight, CoordinateReferenceSystem<P> crs) {
        if (crs == null) {
            throw new IllegalArgumentException("Null CRS argument not allowed");
        }
        for (int i = 0; i < lowerLeft.getCoordinateDimension(); i++) {
            if (lowerLeft.getCoordinate(i) > upperRight.getCoordinate(i)) {
                throw new IllegalArgumentException("Lowerleft needs to be smaller than upperRight");
            }
        }
        this.crs =  crs;
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    
    /**
     * Returns the <code>CoordinateReferenceSystem</code> for this <code>Box</code>
     *
     * @return
     */
    public CoordinateReferenceSystem<P> getCoordinateReferenceSystem() {
        return this.crs;
    }


    /**
     * Returns the lower-left point of this <code>Box</code>.
     *
     * @return the lower-left point
     */
    public P lowerLeft() {
        return this.lowerLeft;
    }

    /**
     * Returns the upper-right point of this <code>Box</code>.
     *
     * @return the upper-right point
     */
    public P upperRight() {
        return this.upperRight;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(getCoordinateReferenceSystem().getCrsId().toString());
        builder.append("Box LL: ")
                .append(this.lowerLeft.toString())
                .append(" - UR: ")
                .append(this.upperRight.toString());
        return builder.toString();
    }

    
    /**
     * Creates an <code>Box</code> that is the set-theoretic union of this {@code Box} with the specified {@code Box}
     *
     * @param other other Box
     * @return an <code>Box</code> that encompasses both operands.
     * @throws IllegalArgumentException when the operand <code>Box</code>s don't have the same coordinate reference system.
     */
    public Box<P> union(Box<P> other) {
        if (other == null || other.isEmpty()) return this;
        if (!this.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem()))
            throw new IllegalArgumentException("Boxs have different CRS.");
        double[] lowerLeft = new double[getCoordinateDimension()];
        double[] upperRight = new double[getCoordinateDimension()];
        for (int i = 0; i < getCoordinateDimension(); i++){
            lowerLeft[i] = Math.min(this.lowerLeft.getCoordinate(i), other.lowerLeft.getCoordinate(i));
            upperRight[i] = Math.max(this.upperRight.getCoordinate(i), other.upperRight.getCoordinate(i));
        }
        return new Box<P>(Positions.mkPosition(crs, lowerLeft), Positions.mkPosition(crs, upperRight) , crs);
    }

    /**
     * Intersects the specified <code>Box</code> with this <code>Box</code> and returns the result.
     *
     * @param other the Box to intersect with this instance
     * @return the set-theoretic intersection of this Box and the specified Box.
     * @throws IllegalArgumentException when the specified <code>Box</code> doesn't have the same coordinate reference system as this instance.
     */
    public Box<P> intersect(Box<P> other) {
        if (other == null || other.isEmpty()) return this;
        if (!this.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem()))
            throw new IllegalArgumentException("Boxs have different CRS.");
        double[] lowerLeft = new double[getCoordinateDimension()];
        double[] upperRight = new double[getCoordinateDimension()];
        for (int i = 0; i < getCoordinateDimension(); i++){
            if (this.lowerLeft.getCoordinate(i) > other.upperRight.getCoordinate(i) || this.upperRight.getCoordinate(i) <
                other.lowerLeft.getCoordinate(i)) {
                return mkEmpty();
            }

            lowerLeft[i] = Math.max(this.lowerLeft.getCoordinate(i), other.lowerLeft.getCoordinate(i));
            upperRight[i] = Math.min(this.upperRight.getCoordinate(i), other.upperRight.getCoordinate(i));
        }
        return new Box<P>(Positions.mkPosition(crs, lowerLeft), Positions.mkPosition(crs, upperRight) , crs);

    }

    private Box<P> mkEmpty() {
        P pos = Positions.mkPosition(crs);
        return new Box<P>(pos, pos, crs);
    }

    static public <P extends Position> Box<P> mkEmpty(CoordinateReferenceSystem<P> crs) {
        P pos = Positions.mkPosition(crs);
        return new Box<P>(pos, pos, crs);
    }

    public int getCoordinateDimension(){
        return crs.getCoordinateDimension();
    }
    
    /**
     * Checks whether this <code>Box</code> is empty.
     *
     * @return true iff this instance  is empty (the empty set).
     */
    public boolean isEmpty() {
        return this.lowerLeft.isEmpty() && this.upperRight.isEmpty() || this.lowerLeft.equals(this.upperRight);
    }

    /**
     * Checks whether this <code>Box</code> is contained within the specified <code>Box</code>
     *
     * @param other the other <code>Box</code>
     * @return true iff this instance is contained within the specified <code>Box</code>
     * @throws IllegalArgumentException when the specified <code>Box</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean within(Box<P> other) {
        if (other == null || other.isEmpty()) return false;
        if (!this.getCoordinateReferenceSystem().equals(other.getCoordinateReferenceSystem()))
            throw new IllegalArgumentException("Boxs have different CRS.");

        for (int i = 0; i < getCoordinateDimension(); i++){
            if (this.lowerLeft.getCoordinate(i) < other.lowerLeft.getCoordinate(i)  || this.upperRight.getCoordinate(i) >
                    other.upperRight.getCoordinate(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether this <code>Box</code> contains the specifies <code>Box</code>.
     *
     * @param other the other <code>Box</code>
     * @return true iff this instance contains the specified <code>Box</code>
     * @throws IllegalArgumentException when the specified <code>Box</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean contains(Box other) {
        return other.within(this);
    }

    /**
     * Checks whether this <code>Box</code> contains the specifies <code>Position</code>.
     *
     * @param p the <code>Position</code>
     * @return true iff this instance contains the specified <code>Position</code>
     * @throws IllegalArgumentException when the specified <code>Point</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean contains(P p) {
        if (!p.getClass().equals(this.getCoordinateReferenceSystem().getPositionClass()))
            throw new IllegalArgumentException("Position and envelope of different types");
        if (isEmpty()) return false;
        for(int i = 0; i < getCoordinateDimension(); i++) {
            if (p.getCoordinate(i) < lowerLeft.getCoordinate(i) || p.getCoordinate(i) > upperRight.getCoordinate(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether this <code>Box</code> intersects the specifies <code>Box</code>.
     *
     * <p>Two instances intersect when their set-theoretic intersection is non-empty.</p>
     *
     * @param other the other <code>Box</code>
     * @return true iff this instance intersects with the other <code>Box</code>
     * @throws IllegalArgumentException when the specified <code>Box</code> doesn't have the same coordinate reference system as this instance.
     */
    public boolean intersects(Box<P> other) {
        if (isEmpty() || other.isEmpty()) return false;

        for(int i = 0; i < getCoordinateDimension(); i++){
            if (this.lowerLeft.getCoordinate(i) > other.upperRight.getCoordinate(i) || this.upperRight.getCoordinate(i) <
                    other.lowerLeft.getCoordinate(i)) {
                return false;
            }
        }
        return true;
    }


    @SuppressWarnings("unchecked")
    public <Q extends Position> Box<Q> as(Class<Q> castToType){
        if (! castToType.isAssignableFrom(this.crs.getPositionClass()) ) {
            throw new ClassCastException(format("Can't cast a %s to a %s", this.crs.getPositionClass().getName(),
                    castToType.getName()));
        }
        return (Box<Q>)this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box<?> envelope = (Box<?>) o;

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
