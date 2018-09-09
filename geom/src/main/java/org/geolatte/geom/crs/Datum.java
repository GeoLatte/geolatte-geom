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

import java.util.Arrays;

/**
 * A horizontal datum.
 *
 * <p>A horizontal datum is the procedure used to measure positions on the surface of the earth (see [CTS-1.00], p. 65).</p>
 *
 * <p> A <code>Datum</code> has an optional toWGS84 property.  If present, this property is a list of up to 7 Bursa Wolf
 * transformation parameters. The meaning of these parameters is described as follows by [CTS-1.00].
 * "These parameters can be used to approximate a transformation from the horizontal datum
 * to the WGS84 datum. However, it must be remembered that this transformation is only an approximation.
 * For a given horizontal datum, different Bursa Wolf transformations can be used to minimize the errors over different
 * regions. If the DATUM clause contains a TOWGS84 clause, then this should be its "preferred"
 * transformation, which will often be the transformation which gives a broad approximation over the whole
 * area of interest (e.g. the area of interest in the containing geographic coordinate system).
 * Sometimes, only the first three or six parameters are defined. In this case the remaining parameters must be zero.
 * If only three parameters are defined, then they can still be plugged into the Bursa Wolf formulas,
 * or you can take a short cut. The Bursa Wolf transformation works on geocentric coordinates, so you cannot apply
 * it onto geographic coordinates directly. If there are only three parameters then you can use the
 * Molodenski or abridged Molodenski formulas."
 *
 * </p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class Datum extends CrsIdentifiable {

    private final Ellipsoid ellipsoid;
    private final double[] toWGS84;

    /**
     * Constructs a <code>Datum</code>.
     *
     * @param crsId an identifier for this <code>Datum</code>
     * @param ellipsoid the <code>Ellipsoid</code> for the new <code>Datum</code>
     * @param name the common name for this <code>Datum</code>
     * @param toWGS84 the parameters for the Bursa-Wolf transformation into WGS84.
     */
    public Datum(CrsId crsId, Ellipsoid ellipsoid, String name, double[] toWGS84){
        super(crsId, name);
        this.ellipsoid = ellipsoid;
        this.toWGS84 = toWGS84 == null ? new double[0] : toWGS84;
    }

    /**
     * Returns the <code>Ellipsoid</code> for this <code>Datum</code>
     *
     * @return
     */
    public Ellipsoid getEllipsoid() {
        return ellipsoid;
    }

    /**
     * Returns the parameters for the {@code PositionVectorTransformation} to the WGS84 datum.
     *
     * <p>If the parameters are not available for this <code>Datum</code>, than an array of size zero is returned.</p>
     *
     * @return the parameters, or an empty array if the parameters are not present.
     */
    public double[] getToWGS84(){
        return this.toWGS84;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Datum that = (Datum) o;

        if (ellipsoid != null ? !ellipsoid.equals(that.ellipsoid) : that.ellipsoid != null) return false;
        if (!Arrays.equals(toWGS84, that.toWGS84)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (ellipsoid != null ? ellipsoid.hashCode() : 0);
        result = 31 * result + (toWGS84 != null ? Arrays.hashCode(toWGS84) : 0);
        return result;
    }
}
