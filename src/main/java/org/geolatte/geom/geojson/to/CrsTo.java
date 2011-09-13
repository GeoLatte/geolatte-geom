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
 * Qmino bvba - Esperantolaan 4 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.geojson.to;

/**
 * Represents a Crs specification. This is a simplified version from the actual GeoJSON specification, since the GeoJSON specification
 * allows for relative links to either URLS or local files in which the crs should be defined. This implementation
 * only supports named crs's: namely:
 * <pre>
 *  "crs": {
 *       "type": "name",
 *       "properties": {
 *             "name": "<yourcrsname>"
 *       }
 * }
 * </pre>
 * Besides the fact that only named crs is permitted for deserialization, the given name must either be of the form:
 * <pre>
 *  urn:ogc:def:EPSG:x.y:4326
 * </pre>
 * (with x.y the version of the EPSG) or of the form:
 * <pre>
 * EPSG:4326
 * </pre>
 *
 * @author Yves Vandewoude
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public class CrsTo {

    private NamedCrsPropertyTo properties;

    public String getType() {
        return "name";
    }

    public NamedCrsPropertyTo getProperties() {
        return properties;
    }

    public void setProperties(NamedCrsPropertyTo properties) {
        this.properties = properties;
    }
}
