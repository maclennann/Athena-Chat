import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.io.*;
import java.math.BigInteger;
import javax.crypto.Cipher;

public class RSADecrypt{

	public static void main(String[] args){
		byte[] messagebytes=null;
		try{
		messagebytes = getMessage("encrypted.msg");
		}catch(Exception e){
			System.out.println("Can't get message");
			System.exit(0);
		}
		doStuff(messagebytes);	
	}
	
	public static void doStuff(byte[] messageBytes){
		byte[] plaintext = rsaDecrypt(messageBytes);
		String plain = new String(plaintext);
		System.out.println(plain);
		try{
		saveToFile("plaintext.msg",plaintext);
		}catch(Exception e){
			System.out.println("Fail");
		}
	}
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
	static PrivateKey readKeyFromFile(String keyFileName) throws IOException {
		System.out.println("pokemans");
		ObjectInputStream oin = null;
		try{
		oin = new ObjectInputStream(new FileInputStream("private.key"));
		}catch(Exception e){e.printStackTrace();}
		System.out.println("Polywag");
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			System.out.println("Got MOD and EXP");
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact.generatePrivate(keySpec);
			System.out.println(privKey);
			return privKey;
		} catch (Exception e) {
			System.out.println("Trying is the first step towards failure.");
		} finally {
			System.out.println("Poly-whirl");
			oin.close();
		}System.out.println("null");return null;
	}
	
	public static byte[] rsaDecrypt(byte[] data) {
		try{
			PrivateKey privKey = readKeyFromFile("private.key");
		
		
		
			
			try{
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, privKey);
		
				try{
					byte[] cipherData = cipher.doFinal(data);
					return cipherData;
		
		
				}
				catch(Exception e){
					System.out.println("CIPHERING\n\n\n\n");
					e.printStackTrace();
				}
			}
			catch(Exception e){
				System.out.println("MAKINGCIPHEER\n\n\n");
				e.printStackTrace();
			}
		}catch(Exception e){System.out.println("Getting private key\n\n\n");e.printStackTrace();}
		return null;
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

