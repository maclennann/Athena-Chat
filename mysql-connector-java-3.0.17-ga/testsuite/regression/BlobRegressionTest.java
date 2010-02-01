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

import testsuite.BaseTestCase;

import java.io.*;
import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 * Tests for blob-related regressions.
 *
 * @author Mark Matthews
 * @version $Id: BlobRegressionTest.java,v 1.1.4.8 2005/02/01 20:36:41 mmatthew Exp $
 */
public class BlobRegressionTest extends BaseTestCase {
    /**
     * Creates a new BlobRegressionTest.
     *
     * @param name name of the test to run
     */
    public BlobRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BlobRegressionTest.class);
    }

    /**
     *
     *
     * @throws Exception ...
     */
    public void testBug2670() throws Exception {
        try {
            byte[] blobData = new byte[32];

            for (int i = 0; i < blobData.length; i++) {
                blobData[i] = 1;
            }

            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2670");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug2670(blobField LONGBLOB)");

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "INSERT INTO testBug2670 (blobField) VALUES (?)");
            pStmt.setBytes(1, blobData);
            pStmt.executeUpdate();

            this.rs = this.stmt.executeQuery(
                    "SELECT blobField FROM testBug2670");
            this.rs.next();

            Blob blob = this.rs.getBlob(1);

            //
            // Test mid-point insertion
            //
            blob.setBytes(4, new byte[] { 2, 2, 2, 2 });

            byte[] newBlobData = blob.getBytes(1L, (int) blob.length());

            assertTrue("Blob changed length", blob.length() == blobData.length);

            assertTrue("New data inserted wrongly",
                ((newBlobData[3] == 2) && (newBlobData[4] == 2)
                && (newBlobData[5] == 2) && (newBlobData[6] == 2)));

            //
            // Test end-point insertion
            //
            blob.setBytes(32, new byte[] { 2, 2, 2, 2 });

            assertTrue("Blob length should be 3 larger",
                blob.length() == (blobData.length + 3));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateLongBlob");
        }
    }

    /**
     *
     *
     * @throws Exception ...
     */
    public void testUpdateLongBlobGT16M() throws Exception {
    	this.rs = this.stmt.executeQuery("SHOW VARIABLES LIKE 'max_allowed_packet'");
    	
    	this.rs.next();
    	
    	int maxAllowedPacket = this.rs.getInt(2);
    	
    	int blobSize = 18 * 1024 * 1024; // 18m blob
    	
    	int neededPacketSize = blobSize * 3;
    	
    	if (maxAllowedPacket > neededPacketSize) { // pad for space
    		try {
	            byte[] blobData = new byte[18 * 1024 * 1024]; // 18M blob
	
	            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateLongBlob");
	            this.stmt.executeUpdate(
	                "CREATE TABLE testUpdateLongBlob(blobField LONGBLOB)");
	            this.stmt.executeUpdate(
	                "INSERT INTO testUpdateLongBlob (blobField) VALUES (NULL)");
	
	            PreparedStatement pStmt = this.conn.prepareStatement(
	                    "UPDATE testUpdateLongBlob SET blobField=?");
	            pStmt.setBytes(1, blobData);
	            pStmt.executeUpdate();
	        } finally {
	            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateLongBlob");
	        }
    	} else {
    		warn("Not running test as max_allowed_packet not set to at least " + neededPacketSize + " bytes.");
    	}
    }
    
    /**
     * 
     * @throws Exception
     */
    public void testUpdatableBlobsWithCharsets() throws Exception {
    	byte[] smallBlob = new byte[32];
    	
    	for (byte i = 0; i < smallBlob.length; i++) {
    		smallBlob[i] = i;
    	}
    	
    	try {
    		this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdatableBlobsWithCharsets");
    		this.stmt.executeUpdate("CREATE TABLE testUpdatableBlobsWithCharsets(pk INT NOT NULL PRIMARY KEY, field1 BLOB)");
    		
    		PreparedStatement pStmt = this.conn.prepareStatement("INSERT INTO testUpdatableBlobsWithCharsets (pk, field1) VALUES (1, ?)");
    		pStmt.setBinaryStream(1, new ByteArrayInputStream(smallBlob), smallBlob.length);
    		pStmt.executeUpdate();
    		
    		Statement updStmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    		
    		this.rs = updStmt.executeQuery("SELECT pk, field1 FROM testUpdatableBlobsWithCharsets");
    		System.out.println(this.rs);
    		this.rs.next();
    		
    		for (byte i = 0; i < smallBlob.length; i++) {
        		smallBlob[i] = (byte)(i + 32);
        	}
    		
    		this.rs.updateBinaryStream(2, new ByteArrayInputStream(smallBlob), smallBlob.length);
    		this.rs.updateRow();
    		
    		ResultSet newRs = this.stmt.executeQuery("SELECT field1 FROM testUpdatableBlobsWithCharsets");
    		
    		newRs.next();
    		
    		byte[] updatedBlob = newRs.getBytes(1);
    		
    		for (byte i = 0; i < smallBlob.length; i++) {
        		byte origValue = smallBlob[i];
        		byte newValue = updatedBlob[i];
        		
        		assertTrue("Original byte at position " + i + ", " + origValue + " != new value, " + newValue, origValue == newValue);
        	}
    		
    	} finally {
    		this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdatableBlobsWithCharsets");
    	}
    }

    public void testBug5490() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug5490");
            this.stmt.executeUpdate("CREATE TABLE testBug5490"
                    + "(pk INT NOT NULL PRIMARY KEY, blobField BLOB)");
            String sql = "insert into testBug5490 values(?,?)";

            int blobFileSize = 871;
            File blobFile = newTempBinaryFile("Bug5490", blobFileSize);

            PreparedStatement pStmt = conn.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pStmt.setInt(1, 2);
            FileInputStream str = new FileInputStream(blobFile);
            pStmt.setBinaryStream(2, str, blobFileSize);
            pStmt.execute();
            str.close();
            pStmt.close();

            ResultSet newRs = this.stmt
                    .executeQuery("SELECT blobField FROM testBug5490");

            newRs.next();

            byte[] returned = newRs.getBytes(1);

            assertEquals(blobFileSize, returned.length);
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug5490");
        }
    }

    private File newTempBinaryFile(String name, long size) throws IOException {
        File tempFile = File.createTempFile(name, "tmp");
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        for (long i = 0; i < size; i++) {
            bos.write((byte) i);
        }
        bos.close();
        assertTrue(tempFile.exists());
        assertEquals(size, tempFile.length());
        return tempFile;
    }
}
