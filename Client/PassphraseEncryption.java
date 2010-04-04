/**  PassphraseEncryption.java
 **  Author: Norm MacLennan/OlympuSoft
 **  Adapted from code found online at:
 **    http://java.sun.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html
 **
 **  A test of passphrase/password-based encryption
 **  and decryption using standard java libraries.
 **  
 **  To run this test, pass two arguments to the program
 **  The first argument should be the strng you want to 
 **  test with. The second argument should be your
 **  passphrase/password. The third argument is used to
 **  calculate the salt.
 **
 **  Sample program input:
 **    java PassphraseEncryption "Hello, World!" "Call me Ishmael." "7 chars"
 **
 **  Sample program output:
 **    Plain Text: Hello, World!
 **    --Encrypting--
 **    Cipher Text: Jç¦d;A49?-t£¦D˜6
 **    --Decrypting--
 **    Clear Text: Hello, World!
 **
 **  A short overview of what this program does:
 **    The program should get an array of 3 arguments when it is
 **    run. The message, the passphrase, and the salt. The program
 **    has a hard-coded salt. The salt that is passed from the command
 **    lane is passed through encryption with the provided passphrase
 **    and the hard-coded salt. The resulting char[] is used as the
 **    new salt. This doesn't work if the provided salt is more than
 **    seven characters long. The real message is then encrypted using
 **    the passphrase and the generated salt. The message is then retrieved
 **    using the same passphrase and salt.
 **
 **  This code can be adapted to allow Athena and Aegis to store
 **  confidential data in external files (preferences, private key,
 **  contact list, etc) in a secure manner through the use of
 **  symmetric encryption. The files can be encrypted with the 
 **  user's account password (or a hash thereof) and it can be used
 **  to decrypt their private key from their hard drive when they 
 **  log in.
 **/
 
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class PassphraseEncryption{
	public static void main(String[] args){
	    // Hardcoded salt
	    byte[] salt = {
		(byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
		(byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
	    };
		
		// Get the passphrase from the command line arguments
	    char[] passphrase = args[1].toCharArray();
		
		// Get the plaintext message from the command line arguments
	    String plaintext  = args[0]; 
		
		// Encrypt the passed salt with the passphrase and hard-coded salt
		byte[] realSalt = encryptData(args[2],passphrase,salt);
		
		// Encrypt the message using the passphrased and generated salt
		byte[] ciphertext = encryptData(plaintext,passphrase,realSalt);
		
		// Decrypt the ciphertext using the same passphrase and generated salt
		byte[] cleartext = decryptData(ciphertext,passphrase,realSalt);
		
		// Print the results
		System.out.println("Plain Text: "+plaintext);
		System.out.println("--Encrypting--");
		System.out.println("Cipher Text: "+new String(ciphertext));
		System.out.println("--Decrypting--");
		System.out.println("Clear Text: "+new String(cleartext));
	}
	
	// Encrypts message using passphrase and salt.
	public static byte[] encryptData(String message, char[] passphrase, byte[] salt){
		try{
			// Declare the key parts
			PBEKeySpec pbeKeySpec;
			PBEParameterSpec pbeParamSpec;
			SecretKeyFactory keyFac;
			
			// Iteration count
			int count = 20;

			// Create PBE parameter set
			pbeParamSpec = new PBEParameterSpec(salt, count);
			pbeKeySpec = new PBEKeySpec(passphrase);
			
			// Initialize the key for encryption
			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

			// Create PBE Cipher
			Cipher pbeCipher;
			pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
	
			// Initialize PBE Cipher with key and parameters
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
			
			// Turn the message string into bytes for encryption
			byte[] cleartext = message.getBytes();

			// Encrypt the cleartext
			byte[] ciphertext = pbeCipher.doFinal(cleartext);
					
			return ciphertext;
		}catch(Exception e){
		
			// Something went wrong. Return null.
			System.out.println("Error encryting data.");
			return null;
		}
		
	}
	
	public static byte[] decryptData(byte[] ciphertext, char[] passphrase, byte[] salt){
		try{
			// Declare the key parts
			PBEKeySpec pbeKeySpec;
			PBEParameterSpec pbeParamSpec;
			SecretKeyFactory keyFac;
			
			// Iteration count
			int count = 20;

			// Create PBE parameter set
			pbeParamSpec = new PBEParameterSpec(salt, count);
			pbeKeySpec = new PBEKeySpec(passphrase);
			
			// Initialize the key
			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

			// Create PBE Cipher
			Cipher pbeCipher;
			pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
	
			// Initialize PBE Cipher with key and parameters
			pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

			//Decrypt the ciphertext
			byte[] plaintext = pbeCipher.doFinal(ciphertext);
			
			return plaintext;
		}catch(Exception e){
			
			//Something went wrong. Return null.
			System.out.println("Error encryting data.");
			return null;
		}
		
	}
}