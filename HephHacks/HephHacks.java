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

public class HephHacks {
	//Display welcome message
	//hax() { 
	//}
	static String serverIP="";
	
	public static void main(String[] args) throws AWTException {
		Scanner in = new Scanner(System.in);
		//Send the welcome message
		//System.out.println("Welcome to the swordfish stress testing program for Athena...");
		clearScreen("Strike while the iron is hot!",14);
		System.out.print("Enter the IP of the Aegis server: ");
		serverIP = in.nextLine();
		menu();
	}
	
	public static void menu(){
		clearScreen("Strike while the iron is hot!",7);
		Scanner in = new Scanner(System.in);
		String clean="r";
		//Display a menu
		//TODO Add more features!
		System.out.println("Choose one option.\n1. Attempt DoS\n2. Spam anonymous connections\n3. Spam authenticated connections.\n4. Spam Server commands\n5. Spam user messages\n6. Exit");
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
				boolean again=true;
				clearScreen("Spam Anonymous Connection",14);
				//String clean = "r";
				in.nextLine();
				do{
					System.out.print("Clean disconnect (y/n): ");
					clean = in.nextLine();
					if(clean.equals("y")||clean.equals("n")) again=false;
					//ystem.out.println("CLEAN: "+clean);
				}while(again);
				SpamX(clean);
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
				clearScreen("Spam Authenticated Session",13);
				//System.out.println("Beginning authenticated session spam.");
				
				//Disregard previous CR
				in.nextLine();
				
				//Read in username and password to spam
				System.out.print("\nEnter Authentication Username: ");
				String username = in.nextLine();
				clearScreen("Spam Authenticated Session",14);
				System.out.print("Enter Authentication Password: ");
				String password = in.nextLine();
				clearScreen("Spam Authenticated Session",14);
				//String clean = "r";
				boolean again=true;
				do{
					System.out.print("Clean disconnect (y/n): ");
					clean = in.nextLine();
					if(clean.equals("y")||clean.equals("n")) again=false;
				}while(again);
				//Spam with provided credentials
				SpamX(username,password,clean);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case 4:
			try {
				clearScreen("Spam Server Commands",13);
				//System.out.println("Beginning authenticated session spam.");
				
				//Disregard previous CR
				in.nextLine();
				
				//Read in username and password to spam
				System.out.print("\nEnter Authentication Username: ");
				String username = in.nextLine();
				clearScreen("Spam Server Commands",14);
				System.out.print("Enter Authentication Password: ");
				String password = in.nextLine();
				clearScreen("Spam Server Commands",14);
				//Spam with provided credentials
				SpamMesg(username,password,false);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case 5:
			try {
				clearScreen("Spam User Messages",13);
				//System.out.println("Beginning authenticated session spam.");
				
				//Disregard previous CR
				in.nextLine();
				
				//Read in username and password to spam
				System.out.print("\nEnter Authentication Username: ");
				String username = in.nextLine();
				clearScreen("Spam User Messages",14);
				System.out.print("Enter Authentication Password: ");
				String password = in.nextLine();
				clearScreen("Spam User Messages",14);
				//Spam with provided credentials
				SpamMesg(username,password,true);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case 6: System.exit(0);
		break;
		}
	}

	private static void SpamX(String clean) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		clearScreen("Spam Anonymous Sessions",14);
		Scanner in = new Scanner(System.in);
		System.out.print("How many anonymous connections would you like to create?");
		int x = in.nextInt();
		
		for(int y=1; y<=x; y++) { 
			Socket serverSocket = new Socket(serverIP, 7777 );
			Socket clientSocket = new Socket(serverIP, 7778 );
			if(clean.equals("y")){
				clientSocket.close();
				serverSocket.close();
			}
		}
		clearScreen("Test Completed Successfully!",12);
		System.out.println(x+ " Anonymous sessions created successfully.\nPress a key to continue...");
		in.nextLine();
		in.nextLine();
		//clearScreen(false,0);
		menu();
	}
	
	private static void SpamX(String user, String pass,String clean) throws UnknownHostException, IOException {
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
				serverSocket = new Socket(serverIP, 7777 );
				clientSocket = new Socket(serverIP, 7778 );
								
				//Bind the datastreams to the socket in order to send/receive spam
				spamIn = new DataInputStream( serverSocket.getInputStream() );
				spamOut = new DataOutputStream( serverSocket.getOutputStream() );

				spamOut.writeUTF(encryptServerPublic(user,serverPublic)); //Sending Username
				spamOut.writeUTF(encryptServerPublic(hashedPassword,serverPublic)); //Sending Password
				result = decryptServerPublic(spamIn.readUTF(),serverPublic); //Read in the result
				//System.out.print("Attempt "+y+": ");
				//System.out.println(result);
				if(result.equals("Success")) passed++;
				else fail++;
				
				//Close things
				if(clean.equals("y")){
					spamOut.close();
					spamIn.close();
					serverSocket.close();
					clientSocket.close();
				}
				
			} catch(Exception e){
				//System.out.println("\nIt looks like you took down the server!");
			}
		}
		clearScreen("Test Completed Successfully!",7);
		System.out.println("\nAuthentications sent. Report follows:");
		System.out.println("Success: "+passed+"\nFailure: "+fail+"\nTotal: "+x);
		if(x!=0) System.out.println("Drop rate: "+fail/x);
		else System.out.println("Drop rate: none");
		System.out.print("\nTest complete. Press a key to go back to the menu.");
		in.nextLine();
		in.nextLine();
		//clearScreen("Test Completed Successfully!",0);
		menu();
	}
	
	
	
	private static void SpamMesg(String user, String pass,boolean flag) throws UnknownHostException, IOException {
		String hashedPassword="";
		//System.out.println("Username: "+user+"\nPassword: "+pass);
		try{
			hashedPassword = computeHash(pass);
		}catch (Exception e){ e.printStackTrace();}
		Socket clientSocket;
		Socket serverSocket;
		DataInputStream spamIn;
		DataOutputStream spamOut;
		DataInputStream mesgSpamIn;
		String result;
		String userReply;
		RSAPublicKeySpec serverPublic = RSACrypto.readPubKeyFromFile("Aegis.pub");
		int passed=0, fail=0;
		
		// TODO Auto-generated method stub
		Scanner in = new Scanner(System.in);
		System.out.print("How many messages would you like to send? ");
		int x = in.nextInt();
		System.out.println("\nBeginning authentications. Please wait...");
		try{
			serverSocket = new Socket(serverIP, 7777 );
			clientSocket = new Socket(serverIP, 7778 );
								
			//Bind the datastreams to the socket in order to send/receive spam
			spamIn = new DataInputStream( serverSocket.getInputStream() );
			spamOut = new DataOutputStream( serverSocket.getOutputStream() );
			mesgSpamIn = new DataInputStream( clientSocket.getInputStream() );
			spamOut.writeUTF(encryptServerPublic(user,serverPublic)); //Sending Username
			spamOut.writeUTF(encryptServerPublic(hashedPassword,serverPublic)); //Sending Password
			result = decryptServerPublic(spamIn.readUTF(),serverPublic); //Read in the result
			
			if(result.equals("Success")){
				System.out.println("Connection established\nContinuing...");
				if(flag){
					userReply = user;
					for(int m=1;m<=x;m++){
						spamOut.writeUTF(encryptServerPublic(userReply,serverPublic));
						spamOut.writeUTF(encryptServerPublic(userReply,serverPublic));
						spamOut.writeUTF("here be dragons");
						mesgSpamIn.readUTF();
						//System.out.println("Read Response");
						if(mesgSpamIn.readUTF().equals("here be dragons")) passed++;
						else fail++;
					}
				}else{
					for(int m=1;m<=x;m++){
						spamOut.writeUTF(encryptServerPublic("Aegis",serverPublic));
						spamOut.writeUTF(encryptServerPublic(user,serverPublic));
						spamOut.writeUTF(encryptServerPublic("here be dragons",serverPublic));
						passed++;
					}
				}
			}
				
			//Close things
			spamOut.close();
			spamIn.close();
			serverSocket.close();
			clientSocket.close();
				
		} catch(Exception e){
			//System.out.println("\nIt looks like you took down the server!");
		}
		clearScreen("Test Completed Successfully!",7);
		System.out.println("\nMessages sent. Report follows:");
		System.out.println("Success: "+passed+"\nFailure: "+fail+"\nTotal: "+x);
		if(x!=0) System.out.println("Drop rate: "+fail/x);
		else System.out.println("Drop rate: none");
		System.out.print("\nTest complete. Press a key to go back to the menu.");
		in.nextLine();
		in.nextLine();
		//clearScreen("Test Completed Successfully!",0);
		menu();
	}
	
	
	
	

	private static void Dos() throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		InetAddress address = InetAddress.getByName(serverIP);
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
		clearScreen("Test Completed Successfully!",0);
		menu();
	}
	
	
	
	
	
	
	
	//Utility methods (help us hack)
	public static void clearScreen(String message, int lines){
		for(int g=0;g<25;g++){
			System.out.println("");
		}
		System.out.println(" _      _");
		System.out.println("| |    | |EPHAESTUS'");
		System.out.println("| |____| |");
		System.out.println("| ______ |ACKING");
		System.out.println("| |    | |");
		System.out.println("|_|    |_|ELPERS");
		System.out.println("   by OlympuSoft");
		System.out.println("\n\n"+message);
		for(int r=1;r<=lines;r++) System.out.print("\n");
		
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
	

