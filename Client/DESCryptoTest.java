public class DESCryptoTest{
	public static void main(String[] args){
		String message    = args[0];
		String passphrase = args[1];
		String salt       = args[2];
		
		DESCrypto desCrypto = new DESCrypto(passphrase,salt);
		
		System.out.println("Plain Text: "+message);
		System.out.println("--Encrypting--");
		
		byte[] ciphertext = desCrypto.encryptData(message);
		
		System.out.println("Encrypted Data: "+new String(ciphertext));
		System.out.println("--Decrypting--");
		
		String plaintext = desCrypto.decryptData(ciphertext);
		
		System.out.println("Decrypted Data: "+plaintext);
	}
}