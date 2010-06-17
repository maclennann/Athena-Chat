/* Athena/Aegis Encrypted Chat Platform
 * FileHash.java: Used to create secure hashes of any data provided
 *
 * Copyright (C) 2010  OlympuSoft
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.io.*;
import java.security.MessageDigest;

public class FileHash {
	   public static byte[] createChecksum(String filename) throws Exception
	   {
		   InputStream fis =  new FileInputStream(filename);

		   byte[] buffer = new byte[1024];
		   MessageDigest complete = MessageDigest.getInstance("MD5");
		   int numRead;
		   
		   do {
			   numRead = fis.read(buffer);
			   if (numRead > 0) {
				   complete.update(buffer, 0, numRead);
			   }
		   } while (numRead != -1);
		   
		   fis.close();
		   return complete.digest();
	   }

	   // see this How-to for a faster way to convert 
	   // a byte array to a HEX string 
	   public static String getMD5Checksum(String filename) throws Exception {
	     byte[] b = createChecksum(filename);
	     String result = "";
	     for (int i=0; i < b.length; i++) {
	       result +=
	          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	      }
	     return result;
	   }
}


