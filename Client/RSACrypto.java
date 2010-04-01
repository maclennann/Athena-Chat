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

	//This method will generate the users RSA key files, one public and one private
	public static void generateRSAKeyPair() { 
		try{
			//Define type of encryption - of course we want RSA!
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			//Define key size
			kpg.initialize(2048); // Are you kidding, no one can hack a 2048bit encrypted firewall!
			//Generate the key pairs
			KeyPair kp = kpg.genKeyPair();
			Key publicKey = kp.getPublic();
			Key privateKey = kp.getPrivate();


			KeyFactory fact = KeyFactory.getInstance("RSA");
			//Define public key
			RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
					RSAPublicKeySpec.class);
			//Define private key
			RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
					RSAPrivateKeySpec.class);
			//Save the keys to their respective files
			saveToFile("public.key", pub.getModulus(), pub.getPublicExponent());
			saveToFile("private.key", priv.getModulus(), priv.getPrivateExponent());
		}catch(Exception e){
			System.out.println("An error has occured in 'generateRSAKeyPair'");
		}

	}

	//This method is where we encrypt the data! 
	//This is where the magic happens
	public static byte[] rsaEncrypt(byte[] data) {
		try{
			//Grab the key from this file 
			PublicKey pubKey = readPubKeyFromFile("/public.key");
			//Define the cipher style
			//RSA OF COURSE
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			//That's right, cipherData, you wish you knew what the message was.
			byte[] cipherData = cipher.doFinal(data);
			return cipherData;
		}
		catch(Exception e){
			System.out.println("An error has occured in 'rsaEncrypt'");
		}return null;
	}

	public static byte[] rsaDecrypt(byte[] data) {
		try{
			//Grab the private key from the file
			PrivateKey privKey = readPrivKeyFromFile("private.key");	
			//Define the decryption type
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			//DECRYPT!
			byte[] plaintext = cipher.doFinal(data);
			//Return the decrypted data
			return plaintext;
		}catch(Exception e){
			System.out.println("An error has occured in 'rsaDecrypt'");e.printStackTrace();
		}
		return null;
	}

	//This method returns the public key from the users pubkey file
	static PublicKey readPubKeyFromFile(String keyFileName) throws IOException {
		//Define the name of the file
		//MAKE sure it's the same as the one we're looking for!
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream("public.key"));
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

	//This method outputs data to a file
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
