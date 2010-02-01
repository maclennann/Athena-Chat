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

import java.sql.SQLException;


/**
 * DOCUMENT ME!
 *
 * @author Mark Matthews
 * @version $Id: TransactionTest.java,v 1.3.2.4 2005/01/27 16:57:13 mmatthew Exp $
 */
public class TransactionTest extends BaseTestCase {
    private static final double DOUBLE_CONST = 25.4312;
    private static final double EPSILON = .0000001;

    /**
     * Creates a new TransactionTest object.
     *
     * @param name DOCUMENT ME!
     */
    public TransactionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TransactionTest.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setUp() throws Exception {
        super.setUp();
        createTestTable();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public void testTransaction() throws SQLException {
        try {
            conn.setAutoCommit(false);
            stmt.executeUpdate(
                "INSERT INTO trans_test (id, decdata) VALUES (1, 1.0)");
            conn.rollback();
            rs = stmt.executeQuery("SELECT * from trans_test");

            boolean hasResults = rs.next();
            assertTrue("Results returned, rollback to empty table failed",
                (hasResults != true));
            stmt.executeUpdate(
                "INSERT INTO trans_test (id, decdata) VALUES (2, "
                + DOUBLE_CONST + ")");
            conn.commit();
            rs = stmt.executeQuery("SELECT * from trans_test where id=2");
            hasResults = rs.next();
            assertTrue("No rows in table after INSERT", hasResults);

            double doubleVal = rs.getDouble(2);
            double delta = Math.abs(DOUBLE_CONST - doubleVal);
            assertTrue("Double value returned != " + DOUBLE_CONST,
                (delta < EPSILON));
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void createTestTable() throws SQLException {
        
        stmt.executeUpdate("DROP TABLE IF EXISTS trans_test");
        

        stmt.executeUpdate(
            "CREATE TABLE trans_test (id INT NOT NULL PRIMARY KEY, decdata DOUBLE) TYPE=InnoDB");
    }
}
