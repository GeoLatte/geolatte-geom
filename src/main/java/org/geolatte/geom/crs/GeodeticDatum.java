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
public class GeodeticDatum {

    private final Ellipsoid ellipsoid;
    private final String name;
    private final int SRID;
    private final double[] toWGS84;

    public GeodeticDatum(int SRID, Ellipsoid ellipsoid, String name, double[] toWGS84){
        this.SRID = SRID;
        this.name = name;
        this.ellipsoid = ellipsoid;
        this.toWGS84 = toWGS84;
    }

    public Ellipsoid getEllipsoid() {
        return ellipsoid;
    }

    public String getName() {
        return name;
    }

    public int getSRID() {
        return SRID;
    }

    public double[] getToWGS84(){
        return this.toWGS84;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeodeticDatum)) return false;

        GeodeticDatum that = (GeodeticDatum) o;

        if (SRID != that.SRID) return false;
        if (ellipsoid != null ? !ellipsoid.equals(that.ellipsoid) : that.ellipsoid != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ellipsoid != null ? ellipsoid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + SRID;
        return result;
    }
}
