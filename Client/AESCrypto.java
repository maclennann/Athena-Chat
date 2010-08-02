/* Athena/Aegis Encrypted Chat Platform
 * AESCrypto.java: Allows easy access to AES cryptography libraries
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
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Provides access to AES cryptography libraries
 * @author OlympuSoft
 */
public class AESCrypto {

	/**
	 * Converts a byte array into a string representing the bytes in hex
	 * @param buf Array of bytes to convert to hex string
	 * @return	Generated hex string
	 */
	public static String asHex(byte buf[]) {
		StringBuilder strbuf = new StringBuilder(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10) {
				strbuf.append("0");
			}

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}

	/**
	 * Generate a 128-bit AES key
	 * @return The SecretKeySpec for the key
	 */
	public static SecretKeySpec generateKey() {
		try {
			// Get the KeyGenerator
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128); // 192 and 256 bits are not available

			// Generate the secret key specs.
			SecretKey skey = kgen.generateKey();
			byte[] raw = skey.getEncoded();
			System.out.println("AES KEY GENEERATED: " + asHex(raw));
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			return skeySpec;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Encrypt a String with an AES key
	 * @param skeySpec The SecretKeySpec of the AES to use in encryption
	 * @param message The string to encrypt
	 * @return The encrypted message as a byte[]
	 */
	public static byte[] encryptMessage(SecretKeySpec skeySpec, String message) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return cipher.doFinal(message.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] encryptMessage(SecretKeySpec skeySpec, byte[] message) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return cipher.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Decrypt an encrypted message using AES
	 * @param skeySpec The AES key's SecretKeySpec
	 * @param message The encrypted message
	 * @return The decrypted message
	 */
	public static byte[] decryptMessage(SecretKeySpec skeySpec, byte[] message) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			return cipher.doFinal(message);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
