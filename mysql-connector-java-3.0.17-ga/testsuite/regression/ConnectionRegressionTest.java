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
package testsuite.regression;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.NonRegisteringDriver;

import testsuite.BaseTestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Regression tests for Connections
 *
 * @author Mark Matthews
 * @version $Id: ConnectionRegressionTest.java,v 1.1.2.17 2005/06/21 19:05:58 mmatthews Exp $
 */
public class ConnectionRegressionTest extends BaseTestCase {
    /**
     * DOCUMENT ME!
     *
     * @param name the name of the testcase
     */
    public ConnectionRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConnectionRegressionTest.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception ...
     */
    public void testBug1914() throws Exception {
    	if (versionMeetsMinimum(4, 1)) {
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), BIGINT)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), BINARY)}"));
	        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), BIT)}"));
	        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), CHAR)}"));
	        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), DATE)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), DECIMAL)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), DOUBLE)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), FLOAT)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), INTEGER)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), LONGVARBINARY)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), LONGVARCHAR)}"));
	        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), TIME)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), TIMESTAMP)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), TINYINT)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), VARBINARY)}"));
	        System.out.println(this.conn.nativeSQL(
	                "{fn convert(foo(a,b,c), VARCHAR)}"));
    	}
    }

    /**
     * Tests fix for BUG#3554 - Not specifying database in URL causes
     * MalformedURL exception.
     *
     * @throws Exception if an error ocurrs.
     */
    public void testBug3554() throws Exception {
        try {
            new NonRegisteringDriver().connect("jdbc:mysql://localhost:3306/?user=root&password=root",
                new Properties());
        } catch (SQLException sqlEx) {
            assertTrue(sqlEx.getMessage().indexOf("Malformed") == -1);
        }
    }

    /**
     *
     *
     * @throws Exception ...
     */
    public void testBug3790() throws Exception {
        String field2OldValue = "foo";
        String field2NewValue = "bar";
        int field1OldValue = 1;

        Connection conn1 = null;
        Connection conn2 = null;
        Statement stmt1 = null;
        Statement stmt2 = null;
        ResultSet rs2 = null;

        Properties props = new Properties();

        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3790");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug3790 (field1 INT NOT NULL PRIMARY KEY, field2 VARCHAR(32)) TYPE=InnoDB");
            this.stmt.executeUpdate("INSERT INTO testBug3790 VALUES ("
                + field1OldValue + ", '" + field2OldValue + "')");

            conn1 = getConnectionWithProps(props); // creates a new connection
            conn2 = getConnectionWithProps(props); // creates another new connection
            conn1.setAutoCommit(false);
            conn2.setAutoCommit(false);

            stmt1 = conn1.createStatement();
            stmt1.executeUpdate("UPDATE testBug3790 SET field2 = '"
                + field2NewValue + "' WHERE field1=" + field1OldValue);
            conn1.commit();

            stmt2 = conn2.createStatement();

            rs2 = stmt2.executeQuery("SELECT field1, field2 FROM testBug3790");

            assertTrue(rs2.next());
            assertTrue(rs2.getInt(1) == field1OldValue);
            assertTrue(rs2.getString(2).equals(field2NewValue));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3790");

            if (rs2 != null) {
                rs2.close();
            }

            if (stmt2 != null) {
                stmt2.close();
            }

            if (stmt1 != null) {
                stmt1.close();
            }

            if (conn1 != null) {
                conn1.close();
            }

            if (conn2 != null) {
                conn2.close();
            }
        }
    }

    /**
     * Tests fix for BUG#4334, port #'s not being picked up 
     * for failover/autoreconnect.
     * 
     * @throws Exception if an error occurs.
     */
    public void testBug4334() throws Exception {
        if (isAdminConnectionConfigured()) {
            Connection adminConnection = null;

            try {
                adminConnection = getAdminConnection();

                int bogusPortNumber = 65534;

                NonRegisteringDriver driver = new NonRegisteringDriver();

                Properties oldProps = driver.parseURL(BaseTestCase.dbUrl, null);

                String host = driver.host(oldProps);
                int port = driver.port(oldProps);
                String database = oldProps.getProperty("DBNAME");
                String user = oldProps.getProperty("user");
                String password = oldProps.getProperty("password");

                StringBuffer newUrlToTestPortNum = new StringBuffer(
                        "jdbc:mysql://");

                if (host != null) {
                    newUrlToTestPortNum.append(host);
                }

                newUrlToTestPortNum.append(":").append(port);
                newUrlToTestPortNum.append(",");

                if (host != null) {
                    newUrlToTestPortNum.append(host);
                }

                newUrlToTestPortNum.append(":").append(bogusPortNumber);
                newUrlToTestPortNum.append("/");

                if (database != null) {
                    newUrlToTestPortNum.append(database);
                }

                if ((user != null) || (password != null)) {
                    newUrlToTestPortNum.append("?");

                    if (user != null) {
                        newUrlToTestPortNum.append("user=").append(user);

                        if (password != null) {
                            newUrlToTestPortNum.append("&");
                        }
                    }

                    if (password != null) {
                        newUrlToTestPortNum.append("password=").append(password);
                    }
                }

                Properties autoReconnectProps = new Properties();
                autoReconnectProps.put("autoReconnect", "true");

                System.out.println(newUrlToTestPortNum);

                //
                // First test that port #'s are being correctly picked up
                //
                // We do this by looking at the error message that is returned
                //
                Connection portNumConn = DriverManager.getConnection(newUrlToTestPortNum
                        .toString(), autoReconnectProps);
                Statement portNumStmt = portNumConn.createStatement();
                this.rs = portNumStmt.executeQuery("SELECT connection_id()");
                this.rs.next();

                killConnection(adminConnection, this.rs.getString(1));

                try {
                    portNumStmt.executeQuery("SELECT connection_id()");
                } catch (SQLException sqlEx) {
                    // we expect this one
                }

                try {
                    portNumStmt.executeQuery("SELECT connection_id()");
                } catch (SQLException sqlEx) {
                    assertTrue(sqlEx.getMessage().toLowerCase().indexOf("connection refused") != -1);
                }

                //
                // Now make sure failover works
                //
                StringBuffer newUrlToTestFailover = new StringBuffer(
                        "jdbc:mysql://");

                if (host != null) {
                    newUrlToTestFailover.append(host);
                }

                newUrlToTestFailover.append(":").append(port);
                newUrlToTestFailover.append(",");

                if (host != null) {
                    newUrlToTestFailover.append(host);
                }

                newUrlToTestFailover.append(":").append(bogusPortNumber);
                newUrlToTestFailover.append("/");

                if (database != null) {
                    newUrlToTestFailover.append(database);
                }

                if ((user != null) || (password != null)) {
                    newUrlToTestFailover.append("?");

                    if (user != null) {
                        newUrlToTestFailover.append("user=").append(user);

                        if (password != null) {
                            newUrlToTestFailover.append("&");
                        }
                    }

                    if (password != null) {
                        newUrlToTestFailover.append("password=").append(password);
                    }
                }

                Connection failoverConn = DriverManager.getConnection(newUrlToTestFailover
                        .toString(), autoReconnectProps);
                Statement failoverStmt = portNumConn.createStatement();
                this.rs = failoverStmt.executeQuery("SELECT connection_id()");
                this.rs.next();

                killConnection(adminConnection, this.rs.getString(1));

                try {
                    failoverStmt.executeQuery("SELECT connection_id()");
                } catch (SQLException sqlEx) {
                    // we expect this one
                }

                failoverStmt.executeQuery("SELECT connection_id()");
            } finally {
                if (adminConnection != null) {
                    adminConnection.close();
                }
            }
        }
    }

    /**
     * Tests if the driver configures character sets correctly for 4.1.x
     * servers.  Requires that the 'admin connection' is configured, as this
     * test needs to create/drop databases.
     *
     * @throws Exception if an error occurs
     */
    public void testCollation41() throws Exception {
        if (versionMeetsMinimum(4, 1) && isAdminConnectionConfigured()) {
            Map charsetsAndCollations = getCharacterSetsAndCollations();
            charsetsAndCollations.remove("latin7"); // Maps to multiple Java charsets
            charsetsAndCollations.remove("ucs2"); // can't be used as a connection charset

            Iterator charsets = charsetsAndCollations.keySet().iterator();

            while (charsets.hasNext()) {
                Connection charsetConn = null;
                Statement charsetStmt = null;

                try {
                    String charsetName = charsets.next().toString();
                    String collationName = charsetsAndCollations.get(charsetName)
                                                                .toString();
                    Properties props = new Properties();
                    props.put("characterEncoding", charsetName);

                    System.out.println("Testing character set " + charsetName);

                    charsetConn = getAdminConnectionWithProps(props);

                    charsetStmt = charsetConn.createStatement();

                    charsetStmt.executeUpdate(
                        "DROP DATABASE IF EXISTS testCollation41");
                    charsetStmt.executeUpdate(
                        "DROP TABLE IF EXISTS testCollation41");

                    charsetStmt.executeUpdate(
                        "CREATE DATABASE testCollation41 DEFAULT CHARACTER SET "
                        + charsetName);
                    charsetConn.setCatalog("testCollation41");

                    // We've switched catalogs, so we need to recreate the statement to pick this up...
                    charsetStmt = charsetConn.createStatement();

                    StringBuffer createTableCommand = new StringBuffer(
                            "CREATE TABLE testCollation41"
                            + "(field1 VARCHAR(255), field2 INT)");

                    charsetStmt.executeUpdate(createTableCommand.toString());

                    charsetStmt.executeUpdate(
                        "INSERT INTO testCollation41 VALUES ('abc', 0)");

                    int updateCount = charsetStmt.executeUpdate(
                            "UPDATE testCollation41 SET field2=1 WHERE field1='abc'");
                    assertTrue(updateCount == 1);
                } finally {
                    if (charsetStmt != null) {
                        charsetStmt.executeUpdate(
                            "DROP TABLE IF EXISTS testCollation41");
                        charsetStmt.executeUpdate(
                            "DROP DATABASE IF EXISTS testCollation41");
                        charsetStmt.close();
                    }

                    if (charsetConn != null) {
                        charsetConn.close();
                    }
                }
            }
        }
    }

    /**
     * Tests setReadOnly() being reset during failover
     *
     * @throws Exception if an error occurs.
     */
    public void testSetReadOnly() throws Exception {
        Properties props = new Properties();
        props.put("autoReconnect", "true");

        String sepChar = "?";

        if (BaseTestCase.dbUrl.indexOf("?") != -1) {
            sepChar = "&";
        }

        Connection reconnectableConn = DriverManager.getConnection(BaseTestCase.dbUrl
                + sepChar + "autoReconnect=true", props);

        rs = reconnectableConn.createStatement().executeQuery("SELECT CONNECTION_ID()");
        rs.next();

        String connectionId = rs.getString(1);

        reconnectableConn.setReadOnly(true);

        boolean isReadOnly = reconnectableConn.isReadOnly();

        System.out.println("You have 30 seconds to kill connection id "
            + connectionId + "...");
        Thread.sleep(30000);
        System.out.println("Executing statement on reconnectable connection...");

        try {
            reconnectableConn.createStatement().executeQuery("SELECT 1");
        } catch (SQLException sqlEx) {
            ; // ignore
        }

        reconnectableConn.createStatement().executeQuery("SELECT 1");

        assertTrue(reconnectableConn.isReadOnly() == isReadOnly);
    }

    private Map getCharacterSetsAndCollations() throws Exception {
        Map charsetsToLoad = new HashMap();

        try {
            this.rs = this.stmt.executeQuery("SHOW character set");

            while (rs.next()) {
                charsetsToLoad.put(rs.getString("Charset"),
                    rs.getString("Default collation"));
            }

            //
            // These don't have mappings in Java...
            //
            charsetsToLoad.remove("swe7");
            charsetsToLoad.remove("hp8");
            charsetsToLoad.remove("dec8");
            charsetsToLoad.remove("koi8u");
            charsetsToLoad.remove("keybcs2");
            charsetsToLoad.remove("geostd8");
            charsetsToLoad.remove("armscii8");
        } finally {
            if (this.rs != null) {
                this.rs.close();
            }
        }

        return charsetsToLoad;
    }

    /**
     * Tests fix for BUG#6966, connections starting up failed-over (due to down master)
     * never retry master.
     * 
     * @throws Exception if the test fails...Note, test is timing-dependent, but should
     * work in most cases.
     */
    public void testBug6966() throws Exception {
    	Properties props = new Driver().parseURL(BaseTestCase.dbUrl, null);
		props.setProperty("autoReconnect", "true");
		
		//   Re-build the connection information
		int firstIndexOfHost = BaseTestCase.dbUrl.indexOf("//") + 2;
		int lastIndexOfHost = BaseTestCase.dbUrl.indexOf("/", firstIndexOfHost);
		
		String hostPortPair = BaseTestCase.dbUrl.substring(firstIndexOfHost, lastIndexOfHost);
		
		StringTokenizer st = new StringTokenizer(hostPortPair, ":");
		
		String host = null;
		String port = null;
		
		if (st.hasMoreTokens()) {
			String possibleHostOrPort = st.nextToken();
			
			if (Character.isDigit(possibleHostOrPort.charAt(0))) {
				port = possibleHostOrPort;
				host = "localhost";
			} else {
				host = possibleHostOrPort;
			}
		}
		
		if (st.hasMoreTokens()) {
			port = st.nextToken();
		}
		
		StringBuffer newHostBuf = new StringBuffer();
		newHostBuf.append(host);
		newHostBuf.append(":0"); // make sure the master fails
		newHostBuf.append(",");
		newHostBuf.append(host);
		if (port != null) {
			newHostBuf.append(":");
			newHostBuf.append(port);
		} 
		
		props.remove("PORT");
		
		props.setProperty("HOST", newHostBuf.toString());
		props.setProperty("queriesBeforeRetryMaster", "50");
		props.setProperty("maxReconnects", "1");

		Connection failoverConnection = null;
		
		try {
			failoverConnection = getConnectionWithProps("jdbc:mysql:///", props);
			failoverConnection.setAutoCommit(false);
			
			for (int i = 0; i < 49; i++) {
				failoverConnection.createStatement().executeQuery("SELECT 1");
			}
			
			long begin = System.currentTimeMillis();
			
			failoverConnection.setAutoCommit(true);
			
			long end = System.currentTimeMillis();
			
			assertTrue("Probably didn't try failing back to the master....check test", (end - begin) > 500);
			
			failoverConnection.createStatement().executeQuery("SELECT 1");
		} finally {
			if (failoverConnection != null) {
				failoverConnection.close();
			}
		}	
    }
    
    /**
     * Tests fix for BUG#7607 - MS932, SHIFT_JIS and Windows_31J not recog.
     * as aliases for sjis.
     * 
     * @throws Exception if the test fails.
     */
    public void testBug7607() throws Exception {
    	if (versionMeetsMinimum(4, 1)) {
			Connection ms932Conn = null, cp943Conn = null, shiftJisConn = null, windows31JConn = null;

			try {
				Properties props = new Properties();
				props.setProperty("characterEncoding", "MS932");

				ms932Conn = getConnectionWithProps(props);

				this.rs = ms932Conn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				String encoding = this.rs.getString(2);
				if (!versionMeetsMinimum(5, 0, 3) &&
					!versionMeetsMinimum(4, 1, 11)) {
					assertEquals("sjis", encoding.toLowerCase(Locale.ENGLISH));
				} else {
					assertEquals("cp932", encoding.toLowerCase(Locale.ENGLISH));
				}

				this.rs = ms932Conn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());

				String charsetToCheck = "ms932";

				if (versionMeetsMinimum(5, 0, 3) || 
					versionMeetsMinimum(4, 1, 11)) {
					charsetToCheck = "windows-31j";
				}

				assertEquals(charsetToCheck,
						((com.mysql.jdbc.ResultSetMetaData) this.rs
								.getMetaData()).getColumnCharacterSet(1)
								.toLowerCase(Locale.ENGLISH));

				try {
					ms932Conn.createStatement().executeUpdate(
							"drop table if exists testBug7607");
					ms932Conn
							.createStatement()
							.executeUpdate(
									"create table testBug7607 (sortCol int, col1 varchar(100) ) character set sjis");
					ms932Conn.createStatement().executeUpdate(
							"insert into testBug7607 values(1, 0x835C)"); // standard
																			// sjis
					ms932Conn.createStatement().executeUpdate(
							"insert into testBug7607 values(2, 0x878A)"); // NEC
																			// kanji

					this.rs = ms932Conn
							.createStatement()
							.executeQuery(
									"SELECT col1 FROM testBug7607 ORDER BY sortCol ASC");
					assertTrue(this.rs.next());
					String asString = this.rs.getString(1);
					assertTrue("\u30bd".equals(asString));

					// Can't be fixed unless server is fixed,
					// this is fixed in 4.1.7.

					assertTrue(this.rs.next());
					asString = this.rs.getString(1);
					assertEquals("\u3231", asString);
				} finally {
					ms932Conn.createStatement().executeUpdate(
							"drop table if exists testBug7607");
				}

				props = new Properties();
				props.setProperty("characterEncoding", "SHIFT_JIS");

				shiftJisConn = getConnectionWithProps(props);

				this.rs = shiftJisConn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				encoding = this.rs.getString(2);
				assertTrue("sjis".equalsIgnoreCase(encoding));

				this.rs = shiftJisConn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());
				assertTrue("SHIFT_JIS"
						.equalsIgnoreCase(((com.mysql.jdbc.ResultSetMetaData) this.rs
								.getMetaData()).getColumnCharacterSet(1)));

				props = new Properties();
				props.setProperty("characterEncoding", "WINDOWS-31J");

				windows31JConn = getConnectionWithProps(props);

				this.rs = windows31JConn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				encoding = this.rs.getString(2);

				if (!versionMeetsMinimum(5, 0, 3) &&
					 !versionMeetsMinimum(4, 1, 11)) {
					assertEquals("sjis", encoding.toLowerCase(Locale.ENGLISH));
				} else {
					assertEquals("cp932", encoding.toLowerCase(Locale.ENGLISH));
				}

				this.rs = windows31JConn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());

				if (!versionMeetsMinimum(4, 1, 11)) {
					assertEquals("sjis".toLowerCase(Locale.ENGLISH),
							((com.mysql.jdbc.ResultSetMetaData) this.rs
									.getMetaData()).getColumnCharacterSet(1)
									.toLowerCase(Locale.ENGLISH));
				} else {
					assertEquals("windows-31j".toLowerCase(Locale.ENGLISH),
							((com.mysql.jdbc.ResultSetMetaData) this.rs
									.getMetaData()).getColumnCharacterSet(1)
									.toLowerCase(Locale.ENGLISH));
				}

				props = new Properties();
				props.setProperty("characterEncoding", "CP943");

				cp943Conn = getConnectionWithProps(props);

				this.rs = cp943Conn.createStatement().executeQuery(
						"SHOW VARIABLES LIKE 'character_set_client'");
				assertTrue(this.rs.next());
				encoding = this.rs.getString(2);
				assertTrue("sjis".equalsIgnoreCase(encoding));

				this.rs = cp943Conn.createStatement().executeQuery(
						"SELECT 'abc'");
				assertTrue(this.rs.next());
				assertTrue("CP943"
						.equalsIgnoreCase(((com.mysql.jdbc.ResultSetMetaData) this.rs
								.getMetaData()).getColumnCharacterSet(1)));

			} finally {
				if (ms932Conn != null) {
					ms932Conn.close();
				}

				if (shiftJisConn != null) {
					shiftJisConn.close();
				}

				if (windows31JConn != null) {
					windows31JConn.close();
				}

				if (cp943Conn != null) {
					cp943Conn.close();
				}
			}
		}
	}
    
	public void testCP932() throws Exception {
		if (versionMeetsMinimum(4, 1, 12)) {
			Connection ms932Conn = null;
			Connection win31JConn = null;
			
			try {
				Properties props = new Properties();
				props.setProperty("useUnicode", "true");
				props.setProperty("characterEncoding", "MS932");
				
				ms932Conn = getConnectionWithProps(props);
				String characterEncoding = getSingleIndexedValueWithQuery(ms932Conn, 
						2, "SHOW VARIABLES LIKE 'character_set_client'").toString();
				assertEquals("cp932", characterEncoding);
				
				props.clear();
				
				props.setProperty("useUnicode", "true");
				props.setProperty("characterEncoding", "Windows-31J");
				
				win31JConn = getConnectionWithProps(props);
				characterEncoding = getSingleIndexedValueWithQuery(win31JConn, 
						2, "SHOW VARIABLES LIKE 'character_set_client'").toString();
				assertEquals("cp932", characterEncoding);
			} finally {
				
			}
		}
	}
    private static void killConnection(Connection adminConn, String threadId)
        throws SQLException {
        adminConn.createStatement().execute("KILL " + threadId);
    }
}
