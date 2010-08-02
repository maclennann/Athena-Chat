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
import sun.misc.BASE64Encoder;


public class KeyExportTest{
	public static void main (String[] args){
	try{
		
		RSAPrivateKeySpec rsakeyspec = RSACrypto.readPrivKeyFromFile("Aegis.priv");
		KeyFactory fact = KeyFactory.getInstance("RSA");
		BASE64Encoder myB64 = new BASE64Encoder();

		PrivateKey privKey = fact.generatePrivate(rsakeyspec);
		System.out.println(privKey.getFormat());
		String b64 = myB64.encode(privKey.getEncoded());
		//System.out.println(privKey.getEncoded().toString);
		//System.out.println(new String(privKey.getEncoded()));
		System.out.println("-----BEGIN PRIVATE KEY-----");
		System.out.println(b64);
		System.out.println("-----END PRIVATE KEY-----");
		}catch(Exception e){}
	}
}