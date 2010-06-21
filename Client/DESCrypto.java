/* Athena/Aegis Encrypted Chat Platform
 * DESCrypto.java: Provides access to DES cryptography libraries
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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * DES cryptography library
 * @author OlympuSoft
 */
public class DESCrypto {
	// Hardcoded salt
	private byte[] salt = {
		(byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
		(byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
	};
	
	// Declare the key parts
	private PBEKeySpec pbeKeySpec;
	private PBEParameterSpec pbeParamSpec;
	private SecretKeyFactory keyFac;
	private SecretKey pbeKey;
	private Cipher pbeCipher;
	private Cipher pbeReverseCipher;

	// Iteration count
	private int count = 20;
	private char[] passphrase;
	//private byte[] realSalt;

	/**
	 * Create a DESCrypto object with the desired salt and passphrase
	 * @param phrase The passphrase for the cipher
	 * @param saltString The salt for the cipher
	 */
	public DESCrypto(String phrase, String saltString) {
		try {
			// Create PBE parameter set
			pbeParamSpec = new PBEParameterSpec(salt, count);
			pbeKeySpec = new PBEKeySpec(passphrase);

			// Initialize the key for encryption
			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			pbeKey = keyFac.generateSecret(pbeKeySpec);

			// Create PBE Cipher
			pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeReverseCipher = Cipher.getInstance("PBEWithMD5AndDES");

			// Initialize PBE Cipher with key and parameters
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
			pbeReverseCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		} catch (Exception e) {
			System.out.println("Unable to create DESCrypto object.");
		}
		//Set variables
		passphrase = phrase.toCharArray();
		//realSalt = encryptData(saltString);
	}

	/**
	 * Encrypts a message using passphrase and salt
	 * @param message The message to encrypt
	 * @return The ciphertext byte[]
	 */
	public final byte[] encryptData(String message) {
		try {
			// Encrypt the cleartext
			byte[] ciphertext = pbeCipher.doFinal(message.getBytes());

			return ciphertext;

		} catch (Exception e) {
			// Something went wrong. Return null.
			System.out.println("Error encryting data.");
			return null;
		}
	}

	/**
	 * Decrypt a message with a passphrase and salt
	 * @param ciphertext The encrypted bytes
	 * @return The plaintext messages
	 */
	public String decryptData(byte[] ciphertext) {
		try {
			//Decrypt the data
			byte[] plaintext = pbeReverseCipher.doFinal(ciphertext);

			//Return a string of the decrypted data
			return new String(plaintext);

		} catch (Exception e) {
			//Something went wrong. Return null.
			System.out.println("Error decryting data.");
			return null;
		}

	}
}
