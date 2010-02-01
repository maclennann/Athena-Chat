/*
    Copyright (C) 2005 MySQL AB

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

package testsuite.nodist;

import testsuite.BaseTestCase;

/**
 * Contains non-redistributable testcases.
 * 
 * @version $Id: StatementNodistTest.java,v 1.1.2.1 2005/02/01 21:44:14 mmatthew Exp $
 */
public class StatementNodistTest extends BaseTestCase {

	/**
	 * @param name
	 */
	public StatementNodistTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
	}
	
    //==========================================================
    // Tests for binary
    //==========================================================
    /**
     * Tests fix for BUG#8064 : 
     *   With sjis config, inserting some data into blob 
     *   column causes "Syntax error"
     * 
     * Requires server set to default character set of 'sjis' to run.
     *
     * @throws Exception if the test fails.
     */
    public void testBug8064_binary01() throws Exception {
        byte[] expected = { 
                (byte)0x80, // NOT one of the first bytes of SJIS double byte char 
                (byte)0x27  // single quote 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary02() throws Exception {
        byte[] expected = { 
                (byte)0x81, // one of the first bytes of SJIS double byte char 
                (byte)0x27  // single quote 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary03() throws Exception {
        byte[] expected = { 
                (byte)0x81, // one of the first bytes of SJIS double byte char 
                (byte)0x5c  // back slash
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary04() throws Exception {
        byte[] expected = { 
                (byte)0x81, // one of the first bytes of SJIS double byte char 
                (byte)0x00  // 0x00
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary05() throws Exception {
        byte[] expected = { 
                (byte)0x5c, // back slash 
                (byte)0x5c  // back slash
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary06() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x00  // 
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary07() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x01  // 
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary08() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x02  // 
        	};
        insertByteArray(expected);
    }
    public void testBug8064_binary09() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x03  // 
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary10() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x04  // 
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary11() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x05  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary12() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x06  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary13() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x07  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary14() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x08  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary15() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x09  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary16() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x0a  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary17() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x0b  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary18() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x0c  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary19() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x0d  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary20() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x0e  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary21() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x0f  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary22() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x11  // 
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary23() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x12  // 
        	};
        insertByteArray(expected);
    }
    public void testBug8064_binary24() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x13  // 
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary25() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x14  // 
        	};
        insertByteArray(expected);
    }
    
    public void testBug8064_binary26() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x15  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary27() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x16  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary28() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x17  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary29() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x18  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary30() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x19  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary31() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x1a  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary32() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x1b  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary33() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x1c  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary34() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x1d  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary35() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x1e  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary36() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x1f  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary40() throws Exception {
        byte[] expected = { 
                (byte)0x81, //  
                (byte)0x20  // 
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary41() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x27,
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary42() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x27,
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary43() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x27,
                (byte)0x27,                
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary44() throws Exception {
        byte[] expected = { 
                (byte)0x27,
                (byte)0x81, 
                (byte)0x27                
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary45() throws Exception {
        byte[] expected = { 
                (byte)0x27,
                (byte)0x27,
                (byte)0x81, 
                (byte)0x27                
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary46() throws Exception {
        byte[] expected = { 
                (byte)0x27,
                (byte)0x27,
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x27                
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary47() throws Exception {
        byte[] expected = { 
                (byte)0x27,
                (byte)0x27,
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x27,                
                (byte)0x27                                
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary48() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x5c,                
                (byte)0x27                                
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary49() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27
                
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary50() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x5c,                
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary51() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x81,                 
                (byte)0x5c,                
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary52() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x81,                 
                (byte)0x5c,                
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27,                
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary53() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x81,                 
                (byte)0x5c,                
                (byte)0x5c,
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27,                
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary54() throws Exception {
        byte[] expected = { 
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x81,                 
                (byte)0x5c,                
                (byte)0x5c,
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27,                
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary55() throws Exception {
        byte[] expected = { 
                (byte)0x5c,                
                (byte)0x5c,
                (byte)0x5c,                
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x81,                 
                (byte)0x5c,                
                (byte)0x5c,
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27,                
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    public void testBug8064_binary56() throws Exception {
        byte[] expected = { 
                (byte)0x27,
                (byte)0x27,                
                (byte)0x27,                
                (byte)0x5c,                
                (byte)0x5c,
                (byte)0x5c,                
                (byte)0x81, 
                (byte)0x81, 
                (byte)0x81,                 
                (byte)0x5c,                
                (byte)0x5c,
                (byte)0x5c,                
                (byte)0x27,
                (byte)0x27,                
                (byte)0x27
        	};
        insertByteArray(expected);
    }

    
    
    
    
    
    //==========================================================
    // Tests for string
    //==========================================================
    public void testBug8064_string01() throws Exception {
        String expected = "It's a boy.";
        insertString(expected);
    }
    
    public void testBug8064_string02() throws Exception {
        String expected = "It's a boy.\\n";
        insertString(expected);
    }

    public void testBug8064_string03() throws Exception {
        String expected = "It's a boy.\\\n";
        insertString(expected);
    }
    
    
    
    //===============================================================
    // Utility methods
    //===============================================================
    private void insertByteArray(byte[] expected) throws Exception {
        byte[] result = null;
        if ("sjis".equalsIgnoreCase(getMysqlVariable("character_set"))) {
            
            try {
                // Init table
                stmt.executeUpdate("DROP TABLE IF EXISTS blobTable");
                stmt.executeUpdate("CREATE TABLE blobTable (field1 BLOB)"); 

                // Insert
                pstmt = 
                    conn.prepareStatement("INSERT INTO blobTable(field1) VALUES(?)");
                pstmt.setBytes(1, expected);
                pstmt.executeUpdate();

                // Result
                this.rs = stmt.executeQuery("SELECT * FROM blobTable");
                if (rs.next()) {
                    result = rs.getBytes(1);
                }
                assertTrue(byteArrayEqual(expected, result));
            } finally {
                stmt.executeUpdate("DROP TABLE IF EXISTS blobTable");
            }
        } else {
            warn(
                    "WARN: Test not valid for servers not running SJIS encoding");
        }
    }

    private void insertString(String expected) throws Exception {
        String result = null;
        if ("sjis".equalsIgnoreCase(getMysqlVariable("character_set"))) {
            
            
            try {
                // Init table
                stmt.executeUpdate("DROP TABLE IF EXISTS stringTable");
                stmt.executeUpdate("CREATE TABLE stringTable (field1 varchar(255))"); 

                // Insert
                pstmt = 
                    conn.prepareStatement("INSERT INTO stringTable(field1) VALUES(?)");
                pstmt.setString(1, expected);
                pstmt.executeUpdate();

                // Result
                this.rs = stmt.executeQuery("SELECT * FROM stringTable");
                if (rs.next()) {
                    result = rs.getString(1);
                }
                assertEquals(expected, result);
            } finally {
                stmt.executeUpdate("DROP TABLE IF EXISTS stringTable");
            }
        } else {
            warn(
                    "WARN: Test not valid for servers not running SJIS encoding");
        }
    }

    
    
    
    private boolean byteArrayEqual(byte[] first, byte[] second) {
        boolean isEqual = true;
        if (first.length == second.length ){ 
            for (int i = 0; i < first.length; i++) {
                if (first[i] != second[i]) {
                    isEqual = false;
                    break;
                }
            }
        } else {
            isEqual = false;
        }
        
        if (!isEqual) {
            System.out.println("Expected : " + getByteArray(first));
            System.out.println("Result   : " + getByteArray(second));
        }

        return isEqual;
    }
    
    private static String getByteArray(byte[] ba) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < ba.length; i++) {
            buffer.append("0x" + Integer.toHexString(ba[i] & 255) + " ");
        }
        return buffer.toString();
    }
}
