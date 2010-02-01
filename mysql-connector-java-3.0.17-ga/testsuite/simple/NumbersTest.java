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
 * @version $Id: NumbersTest.java,v 1.4.2.3 2004/08/09 22:15:13 mmatthew Exp $
 */
public class NumbersTest extends BaseTestCase {
    private static final long TEST_BIGINT_VALUE = 6147483647L;

    /**
     * Creates a new NumbersTest object.
     *
     * @param name DOCUMENT ME!
     */
    public NumbersTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(NumbersTest.class);
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
    public void testNumbers() throws SQLException {
        rs = stmt.executeQuery("SELECT * from number_test");

        while (rs.next()) {
            long minBigInt = rs.getLong(1);
            long maxBigInt = rs.getLong(2);
            long testBigInt = rs.getLong(3);
            assertTrue("Minimum bigint not stored correctly",
                (minBigInt == Long.MIN_VALUE));
            assertTrue("Maximum bigint not stored correctly",
                (maxBigInt == Long.MAX_VALUE));
            assertTrue("Test bigint not stored correctly",
                (TEST_BIGINT_VALUE == testBigInt));
        }
    }

    private void createTestTable() throws SQLException {
        try {
            stmt.executeUpdate("DROP TABLE number_test");
        } /* ignore */catch (SQLException sqlEx) {
            ;
        }

        stmt.executeUpdate(
            "CREATE TABLE number_test (minBigInt bigint, maxBigInt bigint, testBigInt bigint)");
        stmt.executeUpdate(
            "INSERT INTO number_test (minBigInt,maxBigInt,testBigInt) values ("
            + Long.MIN_VALUE + "," + Long.MAX_VALUE + "," + TEST_BIGINT_VALUE
            + ")");
    }
}
