package org.geolatte.geom.crs;

import org.geolatte.geom.G2D;

/**
 * Created by Karel Maesen, Geovise BVBA on 30/11/14.
 */
abstract public class GeographicCoordinateReferenceSystem<P extends G2D> extends SingleCoordinateReferenceSystem<P>{

    private Datum datum;
    private PrimeMeridian primem;

    /**
     * Constructs a <code>HorizontalCoordinateReferenceSystem</code>.
     *
     * @param crsId            the {@link org.geolatte.geom.crs.CrsId} that identifies this
     *                         <code>CoordinateReferenceSystem</code> uniquely
     * @param name             the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param coordinateSystem the coordinate system to use  @throws java.lang.IllegalArgumentException if less than
     *                         two {@link org.geolatte.geom.crs.CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public GeographicCoordinateReferenceSystem(CrsId crsId, String name, CoordinateSystem<P> coordinateSystem) {
        super(crsId, name, coordinateSystem);
    }

    /**
     * Returns the <code>Datum</code> for this <code>CoordinateReferenceSystem</code>
     *
     * @return
     */
    public Datum getDatum() {
        return datum;
    }

    /**
     * Sets the <code>Datum</code> for this <code>CoordinateReferenceSystem</code>
     *
     * @param datum the <code>Datum</code>
     */
    public void setDatum(Datum datum) {
        this.datum = datum;
    }

    /**
     * Returns the <code>PrimeMeridian</code> of this <code>CoordinateReferenceSystem</code>.
     *
     * @return the <code>PrimeMeridian</code>
     */
    public PrimeMeridian getPrimeMeridian() {
        return primem;
    }

    /**
     * Sets the <code>PrimeMeridian</code> for this <code>CoordinateReferenceSystem</code>.
     *
     * @param primeMeridian the <code>PrimeMeridian</code>
     */
    public void setPrimeMeridian(PrimeMeridian primeMeridian) {
        this.primem = primeMeridian;
    }

    /**
     * Returns the <code>Unit</code> for this <code>CoordinateReferenceSystem</code>.
     *
     * @return the <code>Unit</code>
     */
    public Unit getUnit() {
        return getAxis(0).getUnit();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GeographicCoordinateReferenceSystem that = (GeographicCoordinateReferenceSystem) o;

        if (datum != null ? !datum.equals(that.datum) : that.datum != null) return false;
        if (primem != null ? !primem.equals(that.primem) : that.primem != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (datum != null ? datum.hashCode() : 0);
        result = 31 * result + (primem != null ? primem.hashCode() : 0);
        return result;
    }
}
