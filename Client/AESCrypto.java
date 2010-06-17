import javax.crypto.*;
import javax.crypto.spec.*;

/**
* This program generates a AES key, retrieves its raw bytes, and
* then reinstantiates a AES key from the key bytes.
* The reinstantiated key is used to initialize a AES cipher for
* encryption and decryption.
*/

public class AESCrypto {

	/**
	* Turns array of bytes into string
	*
	* @param buf	Array of bytes to convert to hex string
	* @return	Generated hex string
	*/
	public static String asHex (byte buf[]) {
		StringBuilder strbuf = new StringBuilder(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
			strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}
	
	public static SecretKeySpec generateKey(){
		try{
			// Get the KeyGenerator
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128); // 192 and 256 bits are not available

			// Generate the secret key specs.
			SecretKey skey = kgen.generateKey();
			byte[] raw = skey.getEncoded();

			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			return skeySpec;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] encryptMessage(SecretKeySpec skeySpec, String message){
		try{
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return cipher.doFinal(message.getBytes());
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] decryptMessage(SecretKeySpec skeySpec, byte[] message){
		try{
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			return cipher.doFinal(message);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		//Get the message
		String message=args[0];
		System.out.println("Original Message: "+message);
		System.out.println("Original Message in Hex: "+asHex(message.getBytes()));
		
		//Create the cipher
		SecretKeySpec skeySpec = generateKey();
		
		
		//Encrypt the message
		byte[] encrypted = encryptMessage(skeySpec, message);
		System.out.println("Encrypted string: " + asHex(encrypted));

		//Decrypt the message
		byte[] original = decryptMessage(skeySpec, encrypted);
		String originalString = new String(original);
		System.out.println("Decrypted string: " +
		originalString + "\nDecrypted String in Hex: " + asHex(original));
	}
}
