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

import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import testsuite.BaseTestCase;

import testsuite.simple.DataSourceTest;

import java.io.File;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;


/**
 * Tests fixes for bugs related to datasources.
 *
 * @author Mark Matthews
 * @version $Id: DataSourceRegressionTest.java,v 1.1.2.7 2005/02/01 20:36:41 mmatthew Exp $
 */
public class DataSourceRegressionTest extends BaseTestCase {

    public final static String DS_DATABASE_PROP_NAME = "com.mysql.jdbc.test.ds.db";

    public final static String DS_HOST_PROP_NAME = "com.mysql.jdbc.test.ds.host";

    public final static String DS_PASSWORD_PROP_NAME = "com.mysql.jdbc.test.ds.password";

    public final static String DS_PORT_PROP_NAME = "com.mysql.jdbc.test.ds.port";

    public final static String DS_USER_PROP_NAME = "com.mysql.jdbc.test.ds.user";
    
    private Context ctx;
    private File tempDir;
    private String databaseName = null;
    private String hostName = null;
    private String password = null;
    private String user = null;
    private int portNumber = 3306;

    /**
     * Creates a new DataSourceRegressionTest suite for the given test name
     *
     * @param name the name of the testcase to run.
     */
    public DataSourceRegressionTest(String name) {
        super(name);

        // TODO Auto-generated constructor stub
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
        createJNDIContext();
        populateDataSourceProps();
    }

    /**
     * Un-binds the DataSource, and cleans up the filesystem
     *
     * @throws Exception if an error occurs
     */
    public void tearDown() throws Exception {
        this.ctx.unbind(tempDir.getAbsolutePath() + "/test");
        this.ctx.unbind(tempDir.getAbsolutePath() + "/testNoUrl");
        this.ctx.close();
        this.tempDir.delete();

        super.tearDown();
    }

    /**
     * Tests fix for Bug#3848, port # alone parsed incorrectly
     *
     * @throws Exception ...
     */
    public void testBug3848() throws Exception {
        String jndiName = "/testBug3848";

        MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
        
        if (this.databaseName != null) {
            ds.setDatabaseName(this.databaseName);
        }

        if (this.user != null) {
            ds.setUser(this.user);
        }

        if (this.password != null) {
            ds.setPassword(this.password);
        }

        ds.setPortNumber(this.portNumber);

        if (this.hostName != null && !this.hostName.startsWith(":")) {
            ds.setServerName(this.hostName);
        }

        bindDataSource(jndiName, ds);

        ConnectionPoolDataSource boundDs = null;

        try {
            boundDs = (ConnectionPoolDataSource) lookupDatasourceInJNDI(jndiName);

            assertTrue("Datasource not bound", boundDs != null);

            Connection dsConn = null;

            try {
                dsConn = boundDs.getPooledConnection().getConnection();
            } finally {
                if (dsConn != null) {
                    dsConn.close();
                }
            }
        } finally {
            if (boundDs != null) {
                this.ctx.unbind(jndiName);
            }
        }
    }

    /**
     * Tests that we can get a connection from the DataSource bound in JNDI
     * during test setup
     *
     * @throws Exception if an error occurs
     */
    public void testBug3920() throws Exception {
        String jndiName = "/testBug3920";

        MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();

        if (this.databaseName != null) {
            ds.setDatabaseName(this.databaseName);
        }

        if (this.user != null) {
            ds.setUser(this.user);
        }

        if (this.password != null) {
            ds.setPassword(this.password);
        }

        if (this.hostName != null && this.hostName.indexOf(":") == -1) {
        	ds.setPortNumber(this.portNumber);
        }

        if (this.hostName != null) {
            ds.setServerName(this.hostName);
        }

        bindDataSource(jndiName, ds);

        ConnectionPoolDataSource boundDs = null;

        try {
            boundDs = (ConnectionPoolDataSource) lookupDatasourceInJNDI(jndiName);

            assertTrue("Datasource not bound", boundDs != null);

            Connection dsCon = null;
            Statement dsStmt = null;

            try {
                dsCon = boundDs.getPooledConnection().getConnection();
                dsStmt = dsCon.createStatement();
                dsStmt.executeUpdate("DROP TABLE IF EXISTS testBug3920");
                dsStmt.executeUpdate(
                    "CREATE TABLE testBug3920 (field1 varchar(32))");

                assertTrue("Connection can not be obtained from data source",
                    dsCon != null);
            } finally {
                this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3920");

                if (dsStmt != null) {
                	dsStmt.close();
                }
                
                if (dsCon != null) {
                	dsCon.close();
                }
            }
        } finally {
            if (boundDs != null) {
                this.ctx.unbind(jndiName);
            }
        }
    }

    /**
     * Tests fix for BUG#4808- Calling .close() twice on a
     * PooledConnection causes NPE.
     * 
     * @throws Exception if an error occurs.
     */
    public void testBug4808() throws Exception {
    	MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
    	ds.setURL(BaseTestCase.dbUrl);
    	PooledConnection closeMeTwice = ds.getPooledConnection();
    	closeMeTwice.close();
    	closeMeTwice.close();
    	
    }
    
    private void bindDataSource(String name, DataSource ds)
        throws Exception {
        this.ctx.bind(tempDir.getAbsolutePath() + name, ds);
    }

    /**
     * This method is separated from the rest of the example since you normally
     * would NOT register a JDBC driver in your code.  It would likely be
     * configered into your naming and directory service using some GUI.
     *
     * @throws Exception if an error occurs
     */
    private void createJNDIContext() throws Exception {
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
    }

    private DataSource lookupDatasourceInJNDI(String jndiName)
        throws Exception {
        NameParser nameParser = ctx.getNameParser("");
        Name datasourceName = nameParser.parse(tempDir.getAbsolutePath()
                + jndiName);
        Object obj = ctx.lookup(datasourceName);
        DataSource boundDs = null;

        if (obj instanceof DataSource) {
            boundDs = (DataSource) obj;
        } else if (obj instanceof Reference) {
            //
            // For some reason, this comes back as a Reference
            // instance under CruiseControl !?
            //
            Reference objAsRef = (Reference) obj;
            ObjectFactory factory = (ObjectFactory) Class.forName(objAsRef
                    .getFactoryClassName()).newInstance();
            boundDs = (DataSource) factory.getObjectInstance(objAsRef,
                    datasourceName, ctx, new Hashtable());
        }

        return boundDs;
    }

    private void populateDataSourceProps() throws SQLException {
        NonRegisteringDriver driver = new NonRegisteringDriver();

        Properties propsFromUrl = driver.parseURL(BaseTestCase.dbUrl, null);

        this.databaseName = System.getProperty(DS_DATABASE_PROP_NAME);

        if (this.databaseName == null) {
            this.databaseName = driver.database(propsFromUrl);
        }

        this.hostName = System.getProperty(DS_HOST_PROP_NAME);

        if (this.hostName == null) {
        	String possibleHost = driver.host(propsFromUrl);
        	
        	if (possibleHost.startsWith(":")) {
        		this.hostName = "localhost";
        	} else {
        		this.hostName = possibleHost;
        	}
        	
        	if (this.hostName.indexOf(":") != -1) {
        		this.hostName = this.hostName.substring(0, this.hostName.indexOf(":"));
        	}
        }

        this.user = System.getProperty(DS_USER_PROP_NAME);

        if (this.user == null) {
            this.user = propsFromUrl.getProperty("user");
        }

        this.password = System.getProperty(DS_PASSWORD_PROP_NAME);

        if (this.password == null) {
            this.password = propsFromUrl.getProperty("password");
        }

        String portAsString = System.getProperty(DS_PORT_PROP_NAME);

        if (portAsString == null) {
        	String hostAndPort = driver.host(propsFromUrl);
        	
        	if (hostAndPort.startsWith(":")) {
        		this.portNumber = Integer.parseInt(hostAndPort.substring(1));
        	} else {
        		this.portNumber = driver.port(propsFromUrl);
        	}
        } else {
            this.portNumber = Integer.parseInt(portAsString);
        }
    }
}
