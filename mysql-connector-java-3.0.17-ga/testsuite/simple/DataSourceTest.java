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
package testsuite.simple;

import testsuite.BaseTestCase;

import java.io.File;

import java.sql.Connection;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;


/**
 * DOCUMENT ME!
 *
 * @author Mark Matthews
 * @version $Id: DataSourceTest.java,v 1.10.2.9 2004/11/30 04:11:05 mmatthew Exp $
 */
public class DataSourceTest extends BaseTestCase {
    private Context ctx;
    private File tempDir;

    /**
     * Creates a new DataSourceTest object.
     *
     * @param name DOCUMENT ME!
     */
    public DataSourceTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(DataSourceTest.class);
    }

    /**
     * Sets up this test, calling registerDataSource() to bind a  DataSource
     * into JNDI, using the FSContext JNDI provider from Sun
     *
     * @throws Exception if an error occurs.
     */
    public void setUp() throws Exception {
        super.setUp();
        registerDataSource();
    }

    /**
     * Un-binds the DataSource, and cleans up the filesystem
     *
     * @throws Exception if an error occurs
     */
    public void tearDown() throws Exception {
        ctx.unbind(tempDir.getAbsolutePath() + "/test");
        ctx.close();
        tempDir.delete();
        super.tearDown();
    }

    /**
     * Tests that we can get a connection from the DataSource bound in JNDI
     * during test setup
     *
     * @throws Exception if an error occurs
     */
    public void testDataSource() throws Exception {
        NameParser nameParser = ctx.getNameParser("");
        Name datasourceName = nameParser.parse(tempDir.getAbsolutePath()
                + "/test");
        Object obj = ctx.lookup(datasourceName);
        ConnectionPoolDataSource boundDs = null;

        if (obj instanceof DataSource) {
            boundDs = (ConnectionPoolDataSource) obj;
        } else if (obj instanceof Reference) {
            //
            // For some reason, this comes back as a Reference
            // instance under CruiseControl !?
            //
            Reference objAsRef = (Reference) obj;
            ObjectFactory factory = (ObjectFactory) Class.forName(objAsRef
                    .getFactoryClassName()).newInstance();
            boundDs = (ConnectionPoolDataSource) factory.getObjectInstance(objAsRef,
                    datasourceName, ctx, new Hashtable());
        }

        assertTrue("Datasource not bound", boundDs != null);

        Connection con = boundDs.getPooledConnection().getConnection();
        con.close();
        assertTrue("Connection can not be obtained from data source",
            con != null);
    }
    
    /**
     * Tests whether Connection.changeUser() (and thus pooled connections) restore
     * character set information correctly.
     * 
     * @throws Exception if the test fails.
     */
    public void testChangeUserAndCharsets() throws Exception {
    	if (versionMeetsMinimum(4, 1)) {
	    	MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
	    	StringBuffer urlBuf = new StringBuffer(BaseTestCase.dbUrl);
	    	if (dbUrl.indexOf("?") == -1) {
	    		urlBuf.append("?");
	    	} else {
	    		urlBuf.append("&");
	    	}
	    	
	    	urlBuf.append("characterEncoding=utf-8");
	    	
	    	ds.setURL(urlBuf.toString());
	    	
	    	PooledConnection pooledConnection = ds.getPooledConnection();
	
	    	Connection connToMySQL = pooledConnection.getConnection();
	    	this.rs = connToMySQL.createStatement().executeQuery("SHOW VARIABLES LIKE 'character_set_results'");
	    	assertTrue(this.rs.next());
	    	assertTrue("NULL".equalsIgnoreCase(this.rs.getString(2)));
	    	
	    	this.rs = connToMySQL.createStatement().executeQuery("SHOW VARIABLES LIKE 'character_set_client'");
	    	assertTrue(this.rs.next());
	    	assertTrue("utf8".equalsIgnoreCase(this.rs.getString(2)));
	    	
	    	connToMySQL.close();
	
	    	connToMySQL = pooledConnection.getConnection();
	     	this.rs = connToMySQL.createStatement().executeQuery("SHOW VARIABLES LIKE 'character_set_results'");
	     	assertTrue(this.rs.next());
	     	assertTrue("NULL".equalsIgnoreCase(this.rs.getString(2)));
	     	
	     	this.rs = connToMySQL.createStatement().executeQuery("SHOW VARIABLES LIKE 'character_set_client'");
	    	assertTrue(this.rs.next());
	    	assertTrue("utf8".equalsIgnoreCase(this.rs.getString(2)));
	    	
	    	pooledConnection.getConnection().close();
    	}
    }
    
    /**
     * This method is separated from the rest of the example since you normally
     * would NOT register a JDBC driver in your code.  It would likely be
     * configered into your naming and directory service using some GUI.
     *
     * @throws Exception if an error occurs
     */
    private void registerDataSource() throws Exception {
        tempDir = File.createTempFile("jnditest", null);
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();

        MysqlConnectionPoolDataSource ds;
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.fscontext.RefFSContextFactory");
        ctx = new InitialContext(env);
        assertTrue("Naming Context not created", ctx != null);
        ds = new MysqlConnectionPoolDataSource();
        ds.setUrl(dbUrl); // from BaseTestCase
        ds.setDatabaseName("test");
        ctx.bind(tempDir.getAbsolutePath() + "/test", ds);
        
        MysqlConnectionPoolDataSource noUrlDs = new MysqlConnectionPoolDataSource();
        noUrlDs.setDatabaseName("test");
        noUrlDs.setServerName("localhost");
        ctx.bind(tempDir.getAbsolutePath() + "/testNoUrl", noUrlDs);
    }
}
