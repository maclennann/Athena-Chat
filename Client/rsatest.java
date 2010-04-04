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

public class rsatest{
	public static void main(String[] args){
		String message;
		BigInteger pubMod;
		BigInteger pubExp;
		BigInteger privMod;
		BigInteger privExp;
		RSAPublicKeySpec pub;
		RSAPrivateKeySpec priv;
		
		message = args[0];
		RSACrypto.generateRSAKeyPair();
		pub = RSACrypto.getPublicKey();
		priv = RSACrypto.getPrivateKey();
		pubMod = pub.getModulus();
		pubExp = pub.getPublicExponent();
		privMod = priv.getModulus();
		privExp = priv.getPrivateExponent();
		byte[] cipherText = RSACrypto.rsaEncryptPrivate(message,privMod,privExp);
		String plainText = RSACrypto.rsaDecryptPublic(cipherText,pubMod,pubExp);
		System.out.println("Original Message: "+message+"\n\nCipher Text: "+cipherText.toString()+"\n\nDecrypted Plaintext: "+plainText);
	}
}