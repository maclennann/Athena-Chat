import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;

public class RSAGenerate{
	public static void main (String[] args){
		doStuff();
	}
	public static void doStuff(){
		try{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();
		Key publicKey = kp.getPublic();
		Key privateKey = kp.getPrivate();

		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
		RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
		RSAPrivateKeySpec.class);
		saveToFile("public.key", pub.getModulus(), pub.getPublicExponent());
		saveToFile("private.key", priv.getModulus(), priv.getPrivateExponent());
		}catch(Exception e){
			System.out.println("FLAKJ");
		}
		
	}
	
	public static void saveToFile(String fileName,	BigInteger mod, BigInteger exp) throws IOException {
		ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		try {
			oout.writeObject(mod);
			oout.writeObject(exp);
		} catch (Exception e) {
			throw new IOException("Unexpected error", e);
		} finally {
			oout.close();
		}
	}
}
