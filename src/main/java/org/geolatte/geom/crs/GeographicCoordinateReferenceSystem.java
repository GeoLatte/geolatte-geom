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

/**
 * A geographic <code>CoordinateReferenceSystem</code>.
 *
 * <p>A <code>GeographicCoordinateReferenceSystem</code> is defined as A coordinate system based on
 * latitude and longitude. Some geographic coordinate systems are Lat/Lon, and some are Lon/Lat.
 * You can find out which this is by examining the <code>CoordinateSystemAxis</code>es. You should also check the angular units,
 * since not all geographic coordinate systems use degrees (see [CTS-1.00], p. 63).</p>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class GeographicCoordinateReferenceSystem extends CoordinateReferenceSystem {

    private Datum datum;
    private PrimeMeridian primem;

    public GeographicCoordinateReferenceSystem(CrsId crsId, String name, CoordinateSystemAxis... axes) {
        super(crsId, name, axes);
    }

    public void setDatum(Datum datum) {
        this.datum = datum;
    }

    public Datum getDatum() {
        return datum;
    }

    public void setPrimeMeridian(PrimeMeridian primeMeridian) {
        this.primem  = primeMeridian;
    }

    public PrimeMeridian getPrimeMeridian(){
        return primem;
    }


    public Unit getUnit() {

        //TODO -- this should no longer be necessary. CrsRegistry always has at least two coordinate axes
        if (getAxes() == null || getAxes().length == 0) {
            return Unit.UNKNOWN;
        }
        return getAxes()[0].getUnit();

    }
}
