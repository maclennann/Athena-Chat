
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
import java.math.BigInteger;

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
                        System.out.println("AES KEY GENEERATED: "+asHex(raw));
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
		System.out.println("Secret Key: "+asHex(skeySpec.getEncoded()));
		
		String input = asHex(skeySpec.getEncoded());
		byte[] encoded = new BigInteger(input, 16).toByteArray();
		System.out.println("FIRST BYTE: "+encoded[0]);
		System.out.println("SECONDBYTE: "+encoded[1]);
		
		SecretKeySpec aesKey; 
		
		if(encoded[0] == 0){
			System.out.println("LEADING ZEREOS!!!!");
			
			byte[] encoded2= new byte[16];
			for(int x=0,y=-1; x<encoded.length;x++,y++) { 
				if(x>=1) encoded2[y]=encoded[x];
			}
			aesKey = new SecretKeySpec(encoded2, "AES");
		}
		else{
			aesKey = new SecretKeySpec(encoded, "AES");
		}
		
		System.out.println("Secret KEY: "+asHex(aesKey.getEncoded()));
		
		
		//Encrypt the message
		byte[] encrypted = encryptMessage(skeySpec, message);
		System.out.println("Encrypted string: " + asHex(encrypted));

		//Decrypt the message
		byte[] original = decryptMessage(aesKey, encrypted);
		String originalString = new String(original);
		System.out.println("Decrypted string: " +
		originalString + "\nDecrypted String in Hex: " + asHex(original));
	}
}
