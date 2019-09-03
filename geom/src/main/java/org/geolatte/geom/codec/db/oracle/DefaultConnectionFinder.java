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

import java.sql.SQLException;
import java.sql.Connection;
import oracle.jdbc.OracleConnection;

/**
 * Default <code>ConnectionFinder</code> implementation.
 * <p>
 * This implementation attempts to retrieve the <code>OracleConnection</code>
 * by calling the java.sql.Wrapper.unwrap(Class<T>) method.
 *
 * </p>
 *
 * @author Karel Maesen
 */
public class DefaultConnectionFinder implements ConnectionFinder {

	@Override
	public Connection find(Connection con) {
		if ( con == null ) {
			return null;
		}

		// try to find the OracleConnection
		try{
			return conn.unwrap(OracleConnection.class);
		}
		catch (SQLException e) {
			throw new RuntimeException(
				"Couldn't get at the OracleSpatial Connection object from the PreparedStatement."
		};
	}

}
