import java.awt.AWTException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.security.spec.RSAPublicKeySpec;
import java.math.BigInteger;
import sun.misc.BASE64Encoder;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class hax {
	//Display welcome message
	hax() { 
	}
	
	public static void main(String[] args) throws AWTException {
		
		//Send the welcome message
		System.out.println("Welcome to the swordfish stress testing program for Athena...");
		menu();
	}
	
	public static void menu(){
		clearScreen();
		Scanner in = new Scanner(System.in);
		//Display a menu
		//TODO Add more features!
		System.out.println("Choose one option.\n1. Attempt DoS\n2. Spam x number of anonymous connections\n3. Spam x number of authenticated connections.\n4. Exit");
		System.out.print("Choice: ");
		int answer = in.nextInt();
		
		switch(answer) { 
		case 1: try { //DoS the server
				Dos();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case 2: //Spam an anonymous connection
			try {
				SpamX();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case 3: //Spam an authenticated session
			try {
				clearScreen();
				System.out.println("Beginning authenticated session spam.");
				
				//Disregard previous CR
				in.nextLine();
				
				//Read in username and password to spam
				System.out.print("\nEnter Authentication Username: ");
				String username = in.nextLine();
				System.out.print("Enter Autentication Password: ");
				String password = in.nextLine();
				
				//Spam with provided credentials
				SpamX(username,password);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case 4: System.exit(0);
		break;
		}
	}

	private static void SpamX() throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		Scanner in = new Scanner(System.in);
		System.out.println("Enter how many times you would like to fake a connection with Aegis.");
		int x = in.nextInt();
		
		for(int y=0; y<=x; y++) { 
		Socket serverSocket = new Socket("10.1.10.49", 7777 );
		Socket clientSocket = new Socket("10.1.10.49", 7778 );
		clientSocket.close();
		serverSocket.close();
		}
		
		System.out.println("Anonymous sessions created.\nPress a key to continue...");
		in.nextLine();
		in.nextLine();
		clearScreen();
		menu();
	}
	
	private static void SpamX(String user, String pass) throws UnknownHostException, IOException {
		String hashedPassword="";
		//System.out.println("Username: "+user+"\nPassword: "+pass);
		try{
			hashedPassword = computeHash(pass);
		}catch (Exception e){ e.printStackTrace();}
		Socket clientSocket;
		Socket serverSocket;
		DataInputStream spamIn;
		DataOutputStream spamOut;
		String result;
		RSAPublicKeySpec serverPublic = RSACrypto.readPubKeyFromFile("Aegis.pub");
		int passed=0, fail=0;
		
		// TODO Auto-generated method stub
		Scanner in = new Scanner(System.in);
		System.out.print("Enter how many times you would like to authenticate with Aegis? ");
		int x = in.nextInt();
		System.out.println("\nBeginning authentications. Please wait...");
		for(int y=1; y<=x; y++) { 
			try{
				serverSocket = new Socket("10.1.10.49", 7777 );
				clientSocket = new Socket("10.1.10.49", 7778 );
								
				//Bind the datastreams to the socket in order to send/receive spam
				spamIn = new DataInputStream( serverSocket.getInputStream() );
				spamOut = new DataOutputStream( serverSocket.getOutputStream() );

				spamOut.writeUTF(encryptServerPublic(user,serverPublic)); //Sending Username
				spamOut.writeUTF(encryptServerPublic(hashedPassword,serverPublic)); //Sending Password
				result = decryptServerPublic(spamIn.readUTF(),serverPublic); //Read in the result
				//System.out.print("Attempt "+y+": ");
				System.out.println(result);
				if(result.equals("Success")) passed++;
				else fail++;
				
				//Close things
				spamOut.close();
				spamIn.close();
				serverSocket.close();
				clientSocket.close();
				
			} catch(Exception e){
				System.out.println("\nIt looks like you took down the server!");
			}
		}	
		System.out.println("\nAuthentications sent. Report follows:");
		System.out.println("Success: "+passed+"\nFailure: "+fail+"\nTotal: "+x);
		System.out.println("Drop rate: "+fail/x);
		System.out.println("\nTest complete. Press a key to go back to the menu.");
		in.nextLine();
		in.nextLine();
		clearScreen();
		menu();
	}

	private static void Dos() throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		InetAddress address = InetAddress.getByName("10.1.10.49");
		Scanner in = new Scanner(System.in);
		System.out.println("Will commence DoS in 5 seconds...");
		//Sleep for 5
		Thread.sleep(5000);
		for(int x=0;x<5000;x++) { 
			 address.isReachable(3000);
		}
		System.out.println("DoS complete.\nPress a key to continue...");
		in.nextLine();
		in.nextLine();
		clearScreen();
		menu();
	}
	
	
	
	
	
	
	
	//Utility methods (help us hack)
	public static void clearScreen(){
		for(int g=0;g<25;g++){
			System.out.println("");
		}
		System.out.println(" _      _");
		System.out.println("| |    | |");
		System.out.println("| |____| |EPHAESTUS");
		System.out.println("| ______ |");
		System.out.println("| |    | |ACKING");
		System.out.println("|_|    |_|");
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
	}
	
	public static String encryptServerPublic(String plaintext, RSAPublicKeySpec key) { 
		BigInteger cipherText = new BigInteger(RSACrypto.rsaEncryptPublic(plaintext,key.getModulus(),key.getPublicExponent()));
		return cipherText.toString();
	}

	public static String decryptServerPublic(String ciphertext, RSAPublicKeySpec key) { 
		//Turn the String into a BigInteger. Get the bytes of the BigInteger for a byte[]
		byte[] cipherBytes = (new BigInteger(ciphertext)).toByteArray();
		//Decrypt the byte[], returns a String
		return RSACrypto.rsaDecryptPublic(cipherBytes,key.getModulus(),key.getPublicExponent());
	}	
	
	public static String computeHash(String toHash) throws Exception { 
		MessageDigest md = null;
		try
		{
			md = MessageDigest.getInstance("SHA-1"); //step 2
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new Exception(e.getMessage());
		}
		try
		{
			md.update(toHash.getBytes("UTF-8")); //step 3
		}
		catch(UnsupportedEncodingException e)
		{
			throw new Exception(e.getMessage());
		}

		byte raw[] = md.digest(); //step 4
		String hash = (new BASE64Encoder()).encode(raw); //step 5
		return hash; //step 6
	}
}
	

