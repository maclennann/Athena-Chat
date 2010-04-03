import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.io.*;
import java.math.BigInteger;

import javax.crypto.Cipher;

public class RSAEncrypt{
	public static void main(String[] args){
		String message = args[0];
		doStuff(message);
		
	}
	public static void doStuff(String message){
		byte[] messageBytes = message.getBytes();
		byte[] encrypted = rsaEncrypt(messageBytes);
		System.out.println(encrypted);
		try{
		saveToFile("encrypted.msg",encrypted);
		}catch(Exception e){
			System.out.println("Fail");
		}
	}
		
	static PublicKey readKeyFromFile(String keyFileName) throws IOException {
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream("public.key"));
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);
			return pubKey;
		} catch (Exception e) {
			throw new RuntimeException("Spurious serialisation error", e);
		} finally {
			oin.close();
		}
	}
	
	public static byte[] rsaEncrypt(byte[] data) {
		try{
		PublicKey pubKey = readKeyFromFile("/public.key");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherData = cipher.doFinal(data);
		return cipherData;
		}
		catch(Exception e){
			System.out.println("KILLYOURSELF");
		}return null;
	}
	
	
	public static void saveToFile(String fileName, byte[] encrypted) throws IOException {
		ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		try {
			oout.writeObject(encrypted);
		} catch (Exception e) {
			throw new IOException("Unexpected error", e);
		} finally {
			oout.close();
		}
	}
}

