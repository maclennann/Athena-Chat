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
 *  OlympuSoft Athena
 */

public class RSACrypto {

	/**
	 * @param args
	 */
	// The public and private keys that are used throughout the program
	// Generated in generateRSAKeyPair().
	static RSAPublicKeySpec pub;
	static RSAPrivateKeySpec priv;
	
	// Retrieve the publickeyspec
	public static RSAPublicKeySpec getPublicKey(){
		return pub;
	}
	
	// Retrieve the privatekeyspec
	public static RSAPrivateKeySpec getPrivateKey(){
		return priv;
	}
	
	// This method will generate the users RSA key files, one public and one private
	public static void generateRSAKeyPair() { 
		try{
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
			
			// Save the keys to their respective files.
			//saveToFile("public.key", pub.getModulus(), pub.getPublicExponent());
			//saveToFile("private.key", priv.getModulus(), priv.getPrivateExponent());
			
		}catch(Exception e){
			System.out.println("An error has occured in 'generateRSAKeyPair'");
		}

	}	
	
//----------------------------------
// The following two methods encrypt and decrypt messages by taking in the modulus and
// exponent of the public key passed to it. They construct the key on-the-fly perform
// the operation.
	//RSA encrypts plainText using a public key created from mod and exp
	public static byte[] rsaEncryptPublic(String plainText, BigInteger mod, BigInteger exp) {
		try{
			//Grab the key from this file 
			PublicKey pubKey = makePublicKey(mod,exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(plainText.getBytes());
			return cipherData;
		}
		catch(Exception e){
			System.out.println("An error has occured in 'rsaEncryptPublic'");
		}return null;
	}
	//RSA decrypts cipherText using a public key created from mod and exp
	public static String rsaDecryptPublic(byte[] cipherText, BigInteger mod, BigInteger exp) {
		try{
			//Grab the key from this file 
			PublicKey pubKey = makePublicKey(mod,exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, pubKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(cipherText);
			String plainText = new String(cipherData);
			return plainText;
		}
		catch(Exception e){
			e.printStackTrace();
			return "SYSTEM ERROR: There was an issue decrypting the message. Please check that you have the public keyfile for the user.";		
			//System.out.println("An error has occured in 'rsaDecryptPublic'");
		}//return null;
	}

	
	
//-----------------------------
// The following two methods encrypt and decrypt messages by taking in the modulus and
// exponent of the private key passed to it. They construct the key on-the-fly and perform
// the operation.	
	//RSA encrypts plainText using a private key created from mod and exp
	public static byte[] rsaEncryptPrivate(String plainText, BigInteger mod, BigInteger exp) {
		try{
			//Grab the key from this file 
			PrivateKey privKey = makePrivateKey(mod,exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, privKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(plainText.getBytes());
			return cipherData;
		}
		catch(Exception e){
			//return "SYSTEM ERROR: There was an issue encrypting the message. Please check your private key";
			System.out.println("An error has occured in 'rsaEncryptPrivate'");
		}return null;
	}
	//RSA decrypts cipherText using a private key create from mod and exp
	public static String rsaDecryptPrivate(byte[] cipherText, BigInteger mod, BigInteger exp) {
		try{
			//Grab the key from this file 
			PrivateKey privKey = makePrivateKey(mod,exp);
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(cipherText);
			String plainText = new String(cipherData);
			return plainText;
		}
		catch(Exception e){
			return "SYSTEM ERROR: There was an issue decrypting the message. Please check your private key";
			//System.out.println("An error has occured in 'rsaDecrypt Private'");
		}//return null;
	}
	
	
	
//----------------------------
// The following two methods are used to generate public and private keys from
// modulus and exponent parts passed to them.	
	//Creates a public key based on mod and exp
	public static PublicKey makePublicKey(BigInteger mod, BigInteger exp){
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod,exp);
		KeyFactory fact=null;
		PublicKey pubKey=null;
		try{
			fact = KeyFactory.getInstance("RSA");
		}catch(Exception e){System.out.println("KeyFactory issue in makePublicKey");}
		try{
			pubKey = fact.generatePublic(keySpec);
		}catch(Exception e){System.out.println("KeyFactory issue in makePublicKey");}
		return pubKey;
	}
	//Creates a private key based on mod and exp
	public static PrivateKey makePrivateKey(BigInteger mod, BigInteger exp){
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod,exp);
		KeyFactory fact=null;
		PrivateKey privKey=null;
		try{
			fact = KeyFactory.getInstance("RSA");
		}catch(Exception e){System.out.println("KeyFactory issue in makePublicKey");}
		try{
			privKey = fact.generatePrivate(keySpec);
		}catch(Exception e){System.out.println("KeyFactory issue in makePublicKey");}
		return privKey;
	}


	
//----------------------------------
// The following two methods read public and private keys from files of the hard disk.
	//This method returns the public key from the users pubkey file
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
	//This method grabs the private key from the file
	static RSAPrivateKeySpec readPrivKeyFromFile(String keyFileName, DESCrypto descrypto) throws IOException {
		//This is how we'll get the file
		ObjectInputStream oin = null;
		try{
			//Make sure this selects the correct file!
			oin = new ObjectInputStream(new FileInputStream(keyFileName));
			//Grab the m and e values for the RSA key file process
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			
			byte[] modBytes = new BigInteger(m.toString()).toByteArray();
			byte[] expBytes = new BigInteger(e.toString()).toByteArray();
			
			BigInteger modDecrypted = new BigInteger(descrypto.decryptData(modBytes));
			BigInteger expDecrypted = new BigInteger(descrypto.decryptData(expBytes));

			//Run RSA Private Key input 
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modDecrypted, expDecrypted);

			//Return the private key
			return keySpec;
		} catch (Exception e) {
			System.out.println("An error has occured in readPrivKeyFromFile");
		} finally {
			oin.close();
		}
		return null;
	}

	
	
	//Write a public or private key to a file on the hard disk.
	public static void saveToFile(String fileName,	BigInteger mod, BigInteger exp) throws IOException {
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
