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

/**
 * 	We'll have to replicate what this does in Client
 * 
 * 	public static void doStuff(String message){
		byte[] messageBytes = message.getBytes();
		byte[] encrypted = rsaEncrypt(messageBytes);
		System.out.println(encrypted);
		try{
		saveToFile("encrypted.msg",encrypted);
		}catch(Exception e){
			System.out.println("Fail");
		}
	}
*
*	
	This one as well
	
	static byte[] getMessage(String messageFile) throws IOException {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream("encrypted.msg"));
		try {
			byte[] msg = (byte[]) oin.readObject();
			return msg;
		} catch (Exception e) {
			throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			oin.close();
		}
	}
 *
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
	// TODO There be a constructor that calls this method? Might make things more
	//      standardized and easier to follow.
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
			// TODO Should we do this automatically, or wait until it is called?
			//saveToFile("public.key", pub.getModulus(), pub.getPublicExponent());
			//saveToFile("private.key", priv.getModulus(), priv.getPrivateExponent());
			
		}catch(Exception e){
			System.out.println("An error has occured in 'generateRSAKeyPair'");
		}

	}

	
	
//-------------------------------
// The following two methods encrypt and decrypt data passed to them
// using public and private keys stored on the hard drive
	// Encrypts the given data with the public key in ./public.key
	// TODO Perhaps this should change back to taking in a filename?
	public static byte[] rsaEncrypt(byte[] data) {
		try{
			// Grab the key from this file 
			PublicKey pubKey = readPubKeyFromFile("/public.key");
			
			// Define the cipher style
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			
			// Encrypt the message
			byte[] cipherData = cipher.doFinal(data);
			return cipherData;
		}catch(Exception e){
			System.out.println("An error has occured in 'rsaEncrypt'");
		}return null;
	}
	//Generic RSA decryption of data using private key private.key
	public static byte[] rsaDecrypt(byte[] data) {
		try{
			// Grab the private key from the file
			PrivateKey privKey = readPrivKeyFromFile("private.key");	
			
			// Define the decryption type
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			
			// DECRYPT!
			byte[] plaintext = cipher.doFinal(data);
			return plaintext;
		}catch(Exception e){
			System.out.println("An error has occured in 'rsaDecrypt'");e.printStackTrace();
		}
		return null;
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
			System.out.println("An error has occured in 'rsaDecryptPublic'");
		}return null;
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
			System.out.println("An error has occured in 'rsaEncrypt'");
		}return null;
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
	static PublicKey readPubKeyFromFile(String keyFileName) throws IOException {
		//Define the name of the file
		//MAKE sure it's the same as the one we're looking for!
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(keyFileName));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			//Create new key for the key read from the file to be stored in
			//MAKE sure it's RSA!
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);
			return pubKey;
		} catch (Exception e) {
			throw new RuntimeException("An error has occured in 'readKeyFromFile'", e);
		} finally {
			//Close the file stream
			oin.close();
		}
	}
	//This method grabs the private key from the file
	//TODO Make this more secure! 3/31/2010
	static PrivateKey readPrivKeyFromFile(String keyFileName) throws IOException {
		//This is how we'll get the file
		ObjectInputStream oin = null;
		try{
			//Make sure this selects the correct file!
			oin = new ObjectInputStream(new FileInputStream("private.key"));
			//Grab the m and e values for the RSA key file process
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();

			//Run RSA Private Key input 
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact.generatePrivate(keySpec);
			//Return the private key
			return privKey;
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
