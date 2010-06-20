/* Athena/Aegis Encrypted Chat Platform
 * RSACrypto.java: Provides access to RSA cryptography libraries
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

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;

/**
 * RSA Cryptography library methods
 * @author OlympuSoft
 */
public class RSACrypto {

	// The public and private keys that are used throughout the program
	private static RSAPublicKeySpec pub;
	private static RSAPrivateKeySpec priv;

	/**
	 * Retrieve the publickeyspec
	 * @return Public key spec
	 */
	public static RSAPublicKeySpec getPublicKey() {
		return pub;
	}

	/**
	 * Retrieve the privatekeyspec
	 * @return The private key spec
	 */
	public static RSAPrivateKeySpec getPrivateKey() {
		return priv;
	}

	/**
	 * This method will generate the users RSA key files, one public and one private
	 */
	public static void generateRSAKeyPair() {
		try {
			// Define type of encryption for which these keys are made
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

			// Define key size
			kpg.initialize(2048);

			// Generate the key pairs
			KeyPair kp = kpg.genKeyPair();
			Key publicKey = kp.getPublic();
			Key privateKey = kp.getPrivate();
			KeyFactory fact = KeyFactory.getInstance("RSA");

			// Define public key
			pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
			//Define private key
			priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

		} catch (Exception e) {
			System.out.println("An error has occured in 'generateRSAKeyPair'");
		}

	}

	/**
	 * Encrypt a message using a provided public key
	 * @param plainText Plaintext message
	 * @param mod Modulus of the public key
	 * @param exp Exponent of the public key
	 * @return Byte[] of the encrypted message
	 */
	public static byte[] rsaEncryptPublic(String plainText, BigInteger mod, BigInteger exp) {
		try {
			//Grab the key from this file
			PublicKey pubKey = makePublicKey(mod, exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(plainText.getBytes());
			return cipherData;
		} catch (Exception e) {
			System.out.println("An error has occured in 'rsaEncryptPublic'");
		}
		return null;
	}

	/**
	 * Decrypt a message using a provided public key
	 * @param cipherText The encrypted message byte[]
	 * @param mod Modulus of the public key
	 * @param exp Exponent of the public key
	 * @return The decrypted message as a string
	 */
	public static String rsaDecryptPublic(byte[] cipherText, BigInteger mod, BigInteger exp) {
		try {
			//Grab the key from this file
			PublicKey pubKey = makePublicKey(mod, exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, pubKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(cipherText);
			String plainText = new String(cipherData);
			return plainText;
		} catch (Exception e) {
			e.printStackTrace();
			return "SYSTEM ERROR: There was an issue decrypting the message. Please check that you have the public keyfile for the user.";
			//System.out.println("An error has occured in 'rsaDecryptPublic'");
		}//return null;
	}


	/**
	 * Encrypt a message using a provided private key
	 * @param plainText The plaintext message to be encrypted
	 * @param mod The modulus part of the private key
	 * @param exp The exponent part of the private key
	 * @return The encrypted message as a byte[]
	 */
	public static byte[] rsaEncryptPrivate(String plainText, BigInteger mod, BigInteger exp) {
		try {
			//Grab the key from this file
			PrivateKey privKey = makePrivateKey(mod, exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, privKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(plainText.getBytes());
			return cipherData;
		} catch (Exception e) {
			//return "SYSTEM ERROR: There was an issue encrypting the message. Please check your private key";
			System.out.println("An error has occured in 'rsaEncryptPrivate'");
		}
		return null;
	}

	/**
	 * Decrypt a message using a provided private key
	 * @param cipherText Byte[] of the encrypted message
	 * @param mod Modulus part of the private key
	 * @param exp Exponent part of the private key
	 * @return The decrypted message as a string
	 */
	public static String rsaDecryptPrivate(byte[] cipherText, BigInteger mod, BigInteger exp) {
		try {
			//Grab the key from this file
			PrivateKey privKey = makePrivateKey(mod, exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(cipherText);
			String plainText = new String(cipherData);
			return plainText;
		} catch (Exception e) {
			return "SYSTEM ERROR: There was an issue decrypting the message. Please check your private key";
		}
	}

	/**
	 * Puts a public key together using a modulus and exponent
	 * @param mod Modulus piece of the public key
	 * @param exp Exponent piece of the public key
	 * @return The public key constructed
	 */
	public static PublicKey makePublicKey(BigInteger mod, BigInteger exp) {
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
		KeyFactory fact = null;
		PublicKey pubKey = null;
		try {
			fact = KeyFactory.getInstance("RSA");
		} catch (Exception e) {
			System.out.println("KeyFactory issue in makePublicKey");
		}
		try {
			pubKey = fact.generatePublic(keySpec);
		} catch (Exception e) {
			System.out.println("KeyFactory issue in makePublicKey");
		}
		return pubKey;
	}

	/**
	 * Creates a private key using a modulus and exponent
	 * @param mod The modulus piece of the private key
	 * @param exp The exponent piece of the private key
	 * @return The private key constructed
	 */
	public static PrivateKey makePrivateKey(BigInteger mod, BigInteger exp) {
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod, exp);
		KeyFactory fact = null;
		PrivateKey privKey = null;
		try {
			fact = KeyFactory.getInstance("RSA");
		} catch (Exception e) {
			System.out.println("KeyFactory issue in makePublicKey");
		}
		try {
			privKey = fact.generatePrivate(keySpec);
		} catch (Exception e) {
			System.out.println("KeyFactory issue in makePublicKey");
		}
		return privKey;
	}

	/**
	 * Load a public key from a file
	 * @param The filename of the public key
	 * @return The public key read in from the file
	 * @throws IOException
	 */
	static RSAPublicKeySpec readPubKeyFromFile(String keyFileName) throws IOException {
		//Define the name of the file
		//MAKE sure it's the same as the one we're looking for!
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(keyFileName));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);

			return keySpec;
		} catch (Exception e) {
			throw new RuntimeException("An error has occured in 'readKeyFromFile'", e);
		} finally {
			//Close the file stream
			oin.close();
		}
	}
	
	/**
	 * Grab the private key from a file
	 * @param keyFileName The filename
	 * @return The private key
	 * @throws IOException
	 */
	static RSAPrivateKeySpec readPrivKeyFromFile(String keyFileName) throws IOException {
		//This is how we'll get the file
		ObjectInputStream oin = null;
		try {
			//Make sure this selects the correct file!
			oin = new ObjectInputStream(new FileInputStream(keyFileName));
			//Grab the m and e values for the RSA key file process
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();

			//Run RSA Private Key input 
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);

			//Return the private key
			return keySpec;
		} catch (Exception e) {
			System.out.println("An error has occured in readPrivKeyFromFile");
		} finally {
			oin.close();
		}
		return null;
	}

	/**
	 * Save a key to a file
	 * @param fileName File to write to
	 * @param mod Modulus
	 * @param exp Exponent
	 * @throws IOException
	 */
	public static void saveToFile(String fileName, BigInteger mod, BigInteger exp) throws IOException {
		//Define the new file
		ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		try {
			//Write the files
			oout.writeObject(mod);
			oout.writeObject(exp);
		} catch (Exception e) {
			throw new IOException("Unexpected error", e);
		} finally {
			oout.close();
		}
	}
}
