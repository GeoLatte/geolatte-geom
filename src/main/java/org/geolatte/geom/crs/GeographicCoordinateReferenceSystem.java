/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2011 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.crs;

import org.geolatte.geom.*;

/**
 * A geographic <code>CoordinateReferenceSystem</code>.
 * <p/>
 * <p>A <code>GeographicCoordinateReferenceSystem</code> is defined as a coordinate system based on
 * latitude and longitude. Some geographic coordinate systems are Lat/Lon, and some are Lon/Lat.
 * You can find out which this is by examining the {@link CoordinateSystemAxis CoordinateSystemAxes}.
 * You should also check the angular units, since not all geographic coordinate systems use degrees
 * (see [CTS-1.00], p. 63).</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class GeographicCoordinateReferenceSystem extends CoordinateReferenceSystem<G2D> {

    //TODO -- make immutable

    private Datum datum;
    private PrimeMeridian primem;

    /**
     * Constructs a <code>CoordinateReferenceSystem</code>.
     *
     * @param crsId the <code>CrsId</code> that identifies this <code>CoordinateReferenceSystem</code> uniquely
     * @param name  the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param axes  the {@link CoordinateSystemAxis CoordinateSystemAxes} for this <code>CoordinateReferenceSystem</code>
     * @throws IllegalArgumentException if less than two {@link CoordinateSystemAxis CoordinateSystemAxes} are passed.
     */
    public GeographicCoordinateReferenceSystem(CrsId crsId, String name, CoordinateSystemAxis... axes) {
        super(crsId, name, G2D.class, axes);
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
