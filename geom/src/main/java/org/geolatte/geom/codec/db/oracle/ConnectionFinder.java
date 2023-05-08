/*
 * This file is part of Hibernate Spatial, an extension to the
 *  hibernate ORM solution for spatial (geographic) data.
 *
 *  Copyright © 2007-2012 Geovise BVBA
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geolatte.geom.codec.db.oracle;

import java.io.Serializable;
import java.sql.Connection;

/**
 * The <code>ConnectionFinder</code> returns an OracleConnection when given a
 * <code>Connection</code> object.
 * <p>
 * The SDOGeometryType requires access to an <code>OracleConnection</code>
 * object when converting a geometry to <code>SDOGeometry</code>, prior to
 * setting the geometry attribute in prepared statements. In some environments
 * the prepared statements do not return an <code>OracleConnection</code> but
 * a wrapper. Implementations of this interface attempt to retrieve the
 * <code>OracleConnection</code> from the wrapper in such cases.
 * </p>
 * <p>Implementations should be thread-safe, and have a default (no-args) constructor.</p>
 *
 * @author Karel Maesen
 */
public interface ConnectionFinder extends Serializable {

    /**
     * Find an instance of Connection that can be cast to an {@code OracleConnection} instance.
     *
     * @param conn the object that is being searched for an OracleConnection
     * @return the object sought
     * @throws RuntimeException thrown when the feature cannot be found;
     */
    Connection find(Connection conn);

}
