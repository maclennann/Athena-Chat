/*
 Copyright (C) 2002-2004 MySQL AB

 This program is free software; you can redistribute it and/or modify
 it under the terms of version 2 of the GNU General Public License as
 published by the Free Software Foundation.
 

 There are special exceptions to the terms and conditions of the GPL 
 as it is applied to this software. View the full text of the 
 exception exception in file EXCEPTIONS-CONNECTOR-J in the directory of this 
 software distribution.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;


/**
 * Utility functions for admin functionality from Java.
 *
 * @author Mark Matthews
 */
public class MiniAdmin {
    private Connection conn;

    /**
     * Create a new MiniAdmin using the given connection
     *
     * @param conn the existing connection to use.
     *
     * @throws SQLException if an error occurs
     */
    public MiniAdmin(java.sql.Connection conn) throws SQLException {
        if (conn == null) {
            throw new SQLException("Conection can not be null.", SQLError.SQL_STATE_GENERAL_ERROR);
        }

        if (!(conn instanceof Connection)) {
            throw new SQLException("MiniAdmin can only be used with MySQL connections",
                SQLError.SQL_STATE_GENERAL_ERROR);
        }

        this.conn = (Connection) conn;
    }

    /**
     * Create a new MiniAdmin, connecting using the given JDBC
     * URL.
     *
     * @param jdbcUrl the JDBC URL to use
     *
     * @throws SQLException if an error occurs
     */
    public MiniAdmin(String jdbcUrl) throws SQLException {
        this(jdbcUrl, new Properties());
    }

    /**
     * Create a new MiniAdmin, connecting using the given JDBC
     * URL and properties
     *
     * @param jdbcUrl the JDBC URL to use
     * @param props the properties to use when connecting
     *
     * @throws SQLException if an error occurs
     */
    public MiniAdmin(String jdbcUrl, Properties props)
        throws SQLException {
        this.conn = (Connection) (new Driver().connect(jdbcUrl, props));
    }

    /**
     * Shuts down the MySQL server at the other end of the connection
     * that this MiniAdmin was created from/for.
     *
     * @throws SQLException if an error occurs
     */
    public void shutdown() throws SQLException {
        conn.shutdownServer();
    }
}
