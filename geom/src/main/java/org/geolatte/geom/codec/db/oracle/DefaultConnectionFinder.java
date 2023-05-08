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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Default <code>ConnectionFinder</code> implementation.
 * <p>
 * This implementation attempts to retrieve the <code>OracleConnection</code>
 * by recursive reflection: it searches for methods that return
 * <code>Connection</code> objects, executes these methods and checks the
 * result. If the result is of type <code>OracleConnection</code> the object
 * is returned, otherwise it recurses on it.
 *
 * </p>
 *
 * @author Karel Maesen
 */
public class DefaultConnectionFinder implements ConnectionFinder {

    private static final Class<?> ORACLE_CONNECTION_CLASS;
    private static final Class<?> ORACLE_CONNECTION_INTERFACE;

    static {
        try {
            ORACLE_CONNECTION_CLASS = Class.forName("oracle.jdbc.driver.OracleConnection");
            ORACLE_CONNECTION_INTERFACE = Class.forName("oracle.jdbc.OracleConnection");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find Oracle JDBC Driver on classpath.");
        }
    }

    @Override
    public Connection find(Connection con) {
        if (con == null) {
            return null;
        }

        if (ORACLE_CONNECTION_CLASS.isInstance(con)) {
            return con;
        }

        try {
            if (con.isWrapperFor(ORACLE_CONNECTION_INTERFACE)) {
                return (Connection) con.unwrap(ORACLE_CONNECTION_INTERFACE);
            }
        } catch (SQLException e) {
            // Unwrapping failed, falling back on reflection
        }

        // try to find the Oracleconnection recursively
        for (Method method : con.getClass().getMethods()) {
            if (java.sql.Connection.class.isAssignableFrom(
                    method.getReturnType()
            )
                    && method.getParameterTypes().length == 0) {

                try {
                    method.setAccessible(true);
                    final Connection oc = find((Connection) (method.invoke(con, new Object[]{})));
                    if (oc == null) {
                        throw new RuntimeException(
                                String.format(
                                        "Tried retrieving OracleConnection from %s using method %s, but received null.",
                                        con.getClass().getCanonicalName(),
                                        method.getName()
                                )
                        );
                    }
                    return oc;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            String.format(
                                    "Illegal access on executing method %s when finding OracleConnection",
                                    method.getName()
                            )
                    );
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(
                            String.format(
                                    "Invocation exception on executing method %s when finding OracleConnection",
                                    method.getName()
                            )
                    );
                }


            }
        }
        throw new RuntimeException(
                "Couldn't get at the OracleSpatial Connection object from the PreparedStatement."
        );
    }

}
