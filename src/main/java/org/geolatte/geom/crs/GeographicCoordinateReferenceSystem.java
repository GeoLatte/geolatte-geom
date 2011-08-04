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
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class GeographicCoordinateReferenceSystem extends CoordinateReferenceSystem {

    private final Unit unit;
    private GeodeticDatum datum;
    private PrimeMeridian primem;

    public GeographicCoordinateReferenceSystem(int SRID, String name, Unit unit) {
        super(SRID, name);
        this.unit = unit;
    }

    public void setDatum(GeodeticDatum datum) {
        this.datum = datum;
    }

    public GeodeticDatum getDatum() {
        return datum;
    }

    public void setPrimeMeridian(PrimeMeridian primeMeridian) {
        this.primem  = primeMeridian;
    }

    public PrimeMeridian getPrimeMeridian(){
        return primem;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeographicCoordinateReferenceSystem)) return false;

        GeographicCoordinateReferenceSystem that = (GeographicCoordinateReferenceSystem) o;

        if (datum != null ? !datum.equals(that.datum) : that.datum != null) return false;
        if (primem != null ? !primem.equals(that.primem) : that.primem != null) return false;
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = unit != null ? unit.hashCode() : 0;
        result = 31 * result + (datum != null ? datum.hashCode() : 0);
        result = 31 * result + (primem != null ? primem.hashCode() : 0);
        return result;
    }

    public Unit getUnit() {
        return unit;
    }
}
