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

import java.util.Collections;
import java.util.List;

/**
 * A projected <code>CoordinateReferenceSystem</code>.
 *
 * <P>A projected coordinate reference system is a coordinate reference system derived
 * from a two-dimensional geographic coordinate reference system by applying a map projection and
 * using a Cartesian coordinate system. (see <a href="http://portal.opengeospatial.org/files/?artifact_id=39049">
 OGC Abstract Specifications, Topic 2: Spatial Referencing By coordinates</a>, section 4 )
</P>
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 8/2/11
 */
public class ProjectedCoordinateReferenceSystem extends CoordinateReferenceSystem {

    private final Projection projection;
    private final GeographicCoordinateReferenceSystem geoCRS;
    private final List<CrsParameter> parameters;

    /**
     * Constructs a <code>ProjectedCoordinateReferenceSystem</code>.
     *
     *
     * @param crsId      the <code>CrsId</code> that identifies this <code>CoordinateReferenceSystem</code> uniquely
     * @param name       the commonly used name for this <code>CoordinateReferenceSystem</code>
     * @param geoCRS     the <code>GeographicCoordinateReferenceSystem</code> for this projection
     * @param projection the map projection method
     * @param parameters the projection parameters for the projection method
     * @param axes       the <code>CoordinateSystemAxis</code>es of the cartesian coordinate system for
     *                   this <code>ProjectedCoordinateReferenceSystem</code>,
     */
    public ProjectedCoordinateReferenceSystem(CrsId crsId, String name, GeographicCoordinateReferenceSystem geoCRS,
                                              Projection projection, List<CrsParameter> parameters,
                                              CoordinateSystemAxis... axes) {
        super(crsId, name, axes);
        this.geoCRS = geoCRS;
        this.projection = projection;
        this.parameters = parameters;
    }

    /**
     * Returns the projection parameters
     *
     * @return a list of <code>CrsParameter</code>s.
     */
    public List<CrsParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Returns the base <code>GeographicCoordinateReferenceSystem</code> of this
     * projected reference system.
     *
     * @return
     */
    public GeographicCoordinateReferenceSystem getGeographicCoordinateSystem() {
        return geoCRS;
    }

    /**
     * Returns the map projection for this projected cooordinate system.
     * @return
     */
    public Projection getProjection() {
        return projection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectedCoordinateReferenceSystem)) return false;

        ProjectedCoordinateReferenceSystem that = (ProjectedCoordinateReferenceSystem) o;

        if (geoCRS != null ? !geoCRS.equals(that.geoCRS) : that.geoCRS != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (projection != null ? !projection.equals(that.projection) : that.projection != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = projection != null ? projection.hashCode() : 0;
        result = 31 * result + (geoCRS != null ? geoCRS.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}
