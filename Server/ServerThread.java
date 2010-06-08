
/****************************************************
 * Athena: Encrypted Messaging Application v.0.0.2
 * By: 	
 * 			Gregory LeBlanc
 * 			Norm Maclennan 
 * 			Stephen Failla
 * 
 * This program allows a user to send encrypted messages over a fully standardized messaging architecture. It uses RSA with (x) bit keys and SHA-256 to 
 * hash the keys on the server side. It also supports fully encrypted emails using a standardized email address. The user can also send "one-off" emails
 * using a randomly generated email address
 * 
 * File: ServerThread.java
 * 
 * Each connection from a client gets its own thread. User logs in, thread handles sending messages to other users.
 *
 * Sender's thread handles sending to recipient's socket.
 *
 * Thread's life is governed by an int isAlive. Set to 1 in the constructor, and set to 0 when user is likey disconnected.
 *
 ****************************************************/

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.SecureRandom;

import sun.misc.BASE64Encoder;
import sun.security.util.BigInt;

public class ServerThread extends Thread
{
	//Change to 1 for debug output
	private static int debug = 1;

	//Create the DataInputStream on the current socket 
	public DataInputStream serverDin = null;
	public DataInputStream clientDin = null;
	public DataOutputStream serverDout = null;
	public DataOutputStream clientDout = null;

	// The Server that created this thread
	private static Server server;

	//Define Global Variable Username / Password
	private static String username;
	private String realUsername="";
	private String password;

	//Our current socket
	public Socket c2ssocket;
	public Socket c2csocket;

	//Message digest for the hashed password
	MessageDigest hashedPassword;
	
	//Governs thread life. If connection is not alive, thread terminates
	private int isAlive=1;

	private RSAPrivateKeySpec serverPrivate;
	// Constructor. Instantiate this thread on the current socket
	public ServerThread( Server server, Socket c2ssocket, Socket c2csocket ) {

		// Remember which socket we are on
		this.server = server;
		this.c2ssocket = c2ssocket;
		this.c2csocket = c2csocket;

		//Start up the thread
		start();
	}

	//This runs when the thread starts. It controls everything.
	public void run() {
		try {
		
			//Create a datainputstream on the current socket to accept data from the client
			serverDin = new DataInputStream( c2ssocket.getInputStream() );
			clientDin = new DataInputStream( c2csocket.getInputStream() );
			
			//Getting the Username over the stream for authentication
			String usernameCipher = serverDin.readUTF();
			
			if(debug==2)System.out.println("Encrypted Username: " + usernameCipher);
			
			//Decrypt the username
			username = RSACrypto.rsaDecryptPrivate(new BigInteger(usernameCipher).toByteArray(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			
			realUsername = username;
			if(debug>=1)System.out.println("Decrypted Username: "+username);
			System.out.println("\n\n\n\nFIRST REAL USERNAME::::: "+realUsername+"+\nUSERNAME:::::: "+username);
			//Interupt means they want to create a new user
			if(username.equals("Interupt")) { 
				//Do nothing
			} else { 
				//Receive their password hash from the stream
				String passwordCipher = serverDin.readUTF();
				
				//Decrypt the password hash
				password = RSACrypto.rsaDecryptPrivate(new BigInteger(passwordCipher).toByteArray(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());

				//Debug statements
				if (debug>=1)System.out.println("Password: " + password);

				//Authenticate the user.
				String loginOutcome = login(username, password);
				if (debug>=1)System.out.println(loginOutcome);
				System.out.println("\n\n\n\nSECOND REAL USERNAME (POSTLOGIN)::::: "+realUsername+"+\nUSERNAME:::::: "+username);
				//Maps username to socket after user logs in
				server.mapUserServerSocket(username, c2ssocket);	
				server.mapUserClientSocket(username, c2csocket);
				server.addServerOutputStream(c2ssocket,new DataOutputStream(c2ssocket.getOutputStream()));
				server.addClientOutputStream(c2csocket,new DataOutputStream(c2csocket.getOutputStream()));
			}
			if(username.equals("Interupt")) {
				routeMessage(serverDin,clientDin);
				//server.removeConnection(socket);				
			} else { 
			
				//Route around messages coming in from the client while they are connected
				while (isAlive==1) {
					//Take in messages from this thread's client and route them to another client
					routeMessage(serverDin,clientDin);
				}
				
				
			}
			
		} catch ( EOFException ie ) {
		} catch ( IOException ie ) {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			//Socket is closed, remove it from the list
			try { 
			System.out.println("REMOVING USERNAME: "+realUsername);

			if(realUsername==null) server.removeConnection(c2ssocket,c2csocket);

			else server.removeConnection( c2ssocket, c2csocket,realUsername );

			} catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	//Takes in a recipient and message from this thread's user
	//and routes the message to the recipient.
	public void routeMessage(DataInputStream serverDin, DataInputStream clientDin) throws NumberFormatException, InterruptedException{
		try {
			//Grab the server's private key - SHHH!!
			serverPrivate = RSACrypto.readPrivKeyFromFile("keys/Aegis.priv");
			//Read in the Encrypted toUser
			String toUserEncrypted=serverDin.readUTF();
			//Read in the From User
			String fromUserEncrypted=serverDin.readUTF();
			//Read in the Encrypted message
			String messageEncrypted=serverDin.readUTF(); 
			
			//Read in the Digital Signature
			//String digitalSignatureEncrypted = din.readUTF();
			
			
			if(debug==2)System.out.println("Encrypted:" +  toUserEncrypted);
			
			//Decrypt the to user
			byte[] toUserBytes = (new BigInteger(toUserEncrypted)).toByteArray();
			String toUserDecrypted = RSACrypto.rsaDecryptPrivate(toUserBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());

			//Decrypt the from user
			String fromUser = decryptServerPrivate(fromUserEncrypted);
			if(debug>=1)System.out.println("Decrypted:" +  toUserDecrypted);

			//Is the message an eventcode meant for the server?
			if (toUserDecrypted.equals("Aegis")) { 
				if(debug>=1)System.out.print("Server eventcode detected! ");
				if(debug>=1)System.out.println(decryptServerPrivate(messageEncrypted));
				try{
					int code = Integer.parseInt(decryptServerPrivate(messageEncrypted));
				
					systemMessageListener(code);
				} catch(NumberFormatException e){
					System.out.println("Message is NOT an eventcode. Ignoring...");
				}
				return;
			}//Is the message someone trying to create an account?
			if (toUserDecrypted.equals("Interupt")) {
					
				try{
					int code = Integer.parseInt(decryptServerPrivate(messageEncrypted));
				
					systemMessageListener(code);
				} catch(NumberFormatException e){
					System.out.println("Message is NOT an eventcode. Continuing...");
				}
				return;
			}//Is this a normal message to another client
			else { 
				if(debug>=1)System.out.println("Routing normal message to: " + toUserDecrypted + "\nmessage from: " + fromUser);
				if(debug==2)System.out.println("\nEncrypted message: "+messageEncrypted);
				sendMessage(toUserDecrypted, fromUser, messageEncrypted);
				
			}
			//Collect some garbage
			System.gc();

		} catch (IOException e) {
			//Something broke. Disconnect the user.
			if(debug==2)e.printStackTrace();
			isAlive=0;
		}
	}

	//Method that handles client to server messages
	public void systemMessageListener(int eventCode) throws InterruptedException, IOException {

		switch(eventCode) { 
		case 000: try {
			createUsername();
		} catch (IOException e) {
			e.printStackTrace();
		}
		break;
		case 001: 
			if(debug==1)System.out.println("Event code received. negotiateClientStatus() run.");
			negotiateClientStatus();
			//System.out.println("Event code received. negotiateClientStatus() run.");
			break;
		case 002: if(debug==1)System.out.println("Event code received. senToAll() run.");
		server.sendToAll("ServerLogOn", username);
		break;
		case 003: if(debug==1)System.out.println("Event code received. negotiateClientStatus(\"Checkuserstatus\") run.");
		negotiateClientStatus("CheckUserStatus");
		break;
		case 004: if(debug==1)System.out.println("Event code received. publicKeyRequest() run.");
		publicKeyRequest();
		break;
		case 005: if(debug==1)System.out.println("Event code received. returnBuddyListHash() run.");
		returnBuddyListHash();
		break;
		case 006: if(debug==1)System.out.println("Event code received. receiveBuddyListfromClient() run.");
		recieveBuddyListFromClient();
		break;
		case 007: if(debug==1)System.out.println("Event code received. sendPrivateKeyToClients() run.");
		sendPrivateKeyToClient();
		break;
		case 8: if(debug==1)System.out.println("Event code received. sendBuddyListToClient() run.");
		sendBuddyListToClient();
		break;
		case 9: if(debug==1)System.out.println("Event code received. receiveBugReport() run.");
		receiveBugReport();
		break;
		case 10: if(debug==1)System.out.println("Event code received. receiveBugReport(flag) run.");
		receiveBugReport(true);
		break;
		case 11: if(debug==1)System.out.println("Event code received. resetPassword() run.");
		resetPassword();
		break;
		case 12: if(debug==1)System.out.println("Event code received. createChat() run.");
		createChat();
		break;
		default: return;
		}
	}
	
	private void createChat(){
		try{
			//Grab output stream for the user.
			//TODO This probably isn't necessary
			System.out.println("In the method.");
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());
			System.out.println("Created the output stream which we shouldn't have to do.");
			//Grab a connection to the database
			Connection con = server.dbConnect();
			System.out.println("Connected to the database.");
			//Get the chat name from the user
			String chatName = decryptServerPrivate(serverDin.readUTF());
			
			System.out.println("Took in the desired chat name.");
			//Is the chatID a dupe?
			int dupe = 0;
			Statement stmt;
			ResultSet rs = null;
			int randInt=0;
			
			//Generate a unique, random number for the chat
			do{
				//Generate a random number for the chat
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); 
				byte seed[] = random.generateSeed(20);
				random.setSeed(seed); 
				randInt = random.nextInt(9999);
				System.out.println("Generated random chat ID: "+randInt);
				
				//Get a list of existing chats
				stmt = con.createStatement();
				rs = stmt.executeQuery("SELECT * FROM allchats");
				System.out.println("Got list of existing chats.");
				
				//Read chatIDs into array
				while(rs.next()){
					if (rs.getInt("chatid") == randInt){
						dupe = 1;
					}
					else dupe = 0;
				}
			} while(dupe == 1);
			
			//Close the statement and result set
			rs.close();
			
			//The chatID is not a duplicate. We can create the chat and add it to the DB
			System.out.println("Generated number is not");
			
			stmt.executeUpdate("INSERT into allchats (chatid) values('" + randInt +"')");
			System.out.println("Inserted into the database.");
			
			stmt.close();
			con.close();
			
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void resetPassword(){
		try{
			serverDout = new DataOutputStream(c2ssocket.getOutputStream()); 
			//Use dbConnect() to connect to the database
			Connection con = server.dbConnect();
			
			//Take in the username to find the secret question and answer for
			String userToReset = decryptServerPrivate(serverDin.readUTF());
			
			//TODO Pull the secret question and secret answer hash from the DB
			String secretQuestion = null;
			String secretAnswer = null;
			
			//Create a statement and resultset for the query
			Statement stmt;
			Statement insertSTMT;
			ResultSet rs = null; 
			
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM Users WHERE username = '" + userToReset + "'");
			
			if(rs.next()){
				secretQuestion = rs.getString("secretq");
				secretAnswer = rs.getString("secreta");
			}else{
				secretQuestion = "Invalid Username";
			}
			
			//Close the resultset
			rs.close();
			
			//Send the secret question to the client for answering
			serverDout.writeUTF(encryptServerPrivate(secretQuestion));
			System.out.println("SENT Question: "+secretQuestion);
			
			//Read in the user's answer hash
			String secretAnswerHash = decryptServerPrivate(serverDin.readUTF());
			System.out.println("READ Answer: "+secretAnswerHash);
			
			//Read in the user's desired passwordchange
			String newPassword = decryptServerPrivate(serverDin.readUTF());
			
			if(secretAnswerHash.equals(secretAnswer)){
				System.out.println("HASHES MATCH");
				//TODO insert newPassword into password field
				String insertString = "UPDATE Users SET password='" + newPassword + "' WHERE username = '" + userToReset + "'";
				insertSTMT = con.createStatement();
				insertSTMT.executeUpdate(insertString);
				
				if(server.authentication.containsKey(userToReset)){
					server.authentication.remove(userToReset);
					server.authentication.put(userToReset,newPassword);
				}
				else{
					server.authentication.put(userToReset,newPassword);
				}
				System.out.println("PASSWORDCHANGED");
				//Close Connections
				//stmt.close();
				insertSTMT.close();
				//con.close();
				
				//Success message
				serverDout.writeUTF(encryptServerPrivate("1"));
			}else{
				System.out.println("HASH MISMATCH. ABORT");
				//Failure message
				serverDout.writeUTF(encryptServerPrivate("0"));
			}
			
			//Close Connections
			stmt.close();
			//insertSTMT.close();
			con.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void receiveBugReport(){
		if(debug==1)System.out.println("Receiving bug report stacktrace");
		if(debug==1)System.out.println("Receiving bug report comments");
		String trace = "";
		String comments = "";
		try{
			trace = decryptServerPrivate(serverDin.readUTF());
			comments = decryptServerPrivate(serverDin.readUTF());
		}catch(Exception e){e.printStackTrace();}
		
		System.out.println("Bug report received from "+realUsername+": " + comments);
		System.out.println("Stacktrace follows:"+trace);
	}
	
		private void receiveBugReport(boolean flag){
		if(debug==1)System.out.println("Receiving bug report/feature request");

		String title = "";
		String recreate = "";
		String expected = "";
		String actual = "";
		try{
			title = decryptServerPrivate(serverDin.readUTF());
			recreate = decryptServerPrivate(serverDin.readUTF());
			expected = decryptServerPrivate(serverDin.readUTF());
			actual = decryptServerPrivate(serverDin.readUTF());
		}catch(Exception e){e.printStackTrace();}
		System.out.println("BEGIN BUG REPORT");
		System.out.println("Bug report received from "+realUsername+": ");
		System.out.println("Brief Description: "+title);
		System.out.println("Steps to recreate: "+recreate);
		System.out.println("Expected Outcome: "+expected);
		System.out.println("Actual Outcome: "+actual);
		System.out.println("END OF REPORT");
	}
	
	private boolean sendBuddyListToClient() throws IOException {
		// TODO Auto-generated method stub
		String[] buddyListArray = returnBuddyListArray(true);
		if(debug>=1)System.out.println("Inside sendBuddyListToClient");
		
		int numLines = buddyListArray.length;
		if(debug>=1)System.out.println("numLines: " + numLines);
		//Send Athena the begin message so it knows that this is the beginning of the file
		//serverDout.writeUTF(encryptServerPrivate("Begin"));
		//Send Athena the number of lines we're sending
		serverDout.writeUTF(encryptServerPrivate(new String(String.valueOf(numLines))));
		//Send the lines of the file!
		for(int x=0;x<buddyListArray.length;x++) { 
			serverDout.writeUTF(encryptServerPrivate(buddyListArray[x]));
		}
		return true;
		
	}
	//This method returns a nice string array full of the usernames (for now) that are in the buddylist file
	//TODO Make this return a multi-dementional array of all the fields in the CSV File
	public static String[] returnBuddyListArray(boolean flag) throws IOException {
		int count;
		int readChars;
		InputStream is;

		//Let's get the number of lines in the file
		File newFile = new File("buddylists/" + username + "/buddylist.csv");
		if(!(newFile.exists())) { 
			boolean success = new File("buddylists/" + username).mkdirs();
			if(success) { 
				newFile.createNewFile();
				is = new BufferedInputStream(new FileInputStream("buddylists/" + username + "/buddylist.csv"));
			}
			else { 
				newFile.createNewFile();
			}
		}

		is = new BufferedInputStream(new FileInputStream("buddylists/" + username + "/buddylist.csv"));
		byte[] c = new byte[1024];
		count = 0;
		readChars = 0;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {
				if (c[i] == '\n')
					++count;
			}
		} //End section

		//Make the string array the size of the number of lines in the file
		String[] usernames = new String[count];

		//If there are no lines in the file we know that the user has no buddies! :(
		if (count == 0) { 
			return usernames;
		}
		else { 
			File newFile2 = new File("buddylists/" + username + "/buddylist.csv");
			if(!(newFile2.exists())) { 
				newFile2.createNewFile();
			}
			BufferedReader in = new BufferedReader(new FileReader("buddylists/" + username + "/buddylist.csv")); 
			int x=0;
			String raw;
			//Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
			while ((raw = in.readLine()) != null) 
			{ 
				// Read in the BigInteger in String form. Turn it to a BigInteger
				// Turn the BigInteger to a byteArray, and decrypt it.
				//strNum = new BigInteger(raw);
				//str = descrypto.decryptData(strNum.toByteArray());

				String foo[] = raw.split(","); 
				usernames[x] = foo[0];
				x++;
			}
			return usernames;
		}
	}

	private void sendPrivateKeyToClient() throws IOException {
		RSAPrivateKeySpec privateKey = RSACrypto.readPrivKeyFromFile("keys/" + username + ".priv");
		//Send over ack message
		sendSystemMessage(username, "Incoming private key components");
		
		//Send over components
		String privateKeyMod = privateKey.getModulus().toString();
		if(debug==2)System.out.println("PRIVATE KEY MOD: " + privateKeyMod);
		String privateKeyExp = privateKey.getPrivateExponent().toString();
		if(debug==2)System.out.println("PRIVATE KEY MOD: " + privateKeyExp);
		//Send half a time plz!
		if(debug==2)System.out.println("Length of the private key: "+privateKeyMod.length());
		//Send how many chunks will be coming		
		if(privateKeyMod.length() > 245){
			double messageNumbers = (double)privateKeyMod.length()/245;
			int messageNumbersInt = (int)Math.ceil(messageNumbers);
			serverDout.writeUTF(String.valueOf(messageNumbersInt));
			
			if(debug>=1)System.out.println("MessageLength: "+privateKeyMod.length()+"\nMessageLength/245: "+messageNumbers+"\nCeiling of that: "+messageNumbersInt);
			
			String[] messageChunks = new String[(int)messageNumbersInt];
			for(int i=0;i<messageChunks.length;i++){
				int begin=i*245;
				int end = begin+245;
				if(end>privateKeyMod.length()){
					end = privateKeyMod.length();
				}
				messageChunks[i] = privateKeyMod.substring(begin,end);
				serverDout.writeUTF(encryptServerPrivate(messageChunks[i]));
			}
		}
		
		if(privateKeyExp.length() > 245){
			double messageNumbers = (double)privateKeyExp.length()/245;
			int messageNumbersInt = (int)Math.ceil(messageNumbers);
			serverDout.writeUTF(String.valueOf(messageNumbersInt));
			
			if(debug>=1)System.out.println("MessageLength: "+privateKeyMod.length()+"\nMessageLength/245: "+messageNumbers+"\nCeiling of that: "+messageNumbersInt);
			
			String[] messageChunks = new String[(int)messageNumbersInt];
			for(int i=0;i<messageChunks.length;i++){
				int begin=i*245;
				int end = begin+245;
				if(end>privateKeyExp.length()){
					end = privateKeyExp.length();
				}
				messageChunks[i] = privateKeyExp.substring(begin,end);
				serverDout.writeUTF(encryptServerPrivate(messageChunks[i]));
			}
		}
	}

	public void writeBuddyListToFile(String[] buddyList, String buddyListName){
		BufferedWriter out;
		File newFile = new File("buddylists/" + buddyListName + "/buddylist.csv");
		try{
			if(!(newFile.exists())) { 
				boolean success = new File("users/" + buddyListName).mkdirs();
				if(success) { 
					newFile.createNewFile();
				}
				else { 
					newFile.createNewFile();
				}
			}
			else { 
				newFile.delete();
				newFile.createNewFile();
			}
			out = new BufferedWriter(new FileWriter("buddylists/" + buddyListName + "/buddylist.csv"));

			for(int i = 0; i < buddyList.length;i++){
				out.write(buddyList[i] + "\n");
			}
			out.close();
		}catch(Exception e)
		{if(debug==1)System.out.println("ERROR WRITING BUDDYLIST");
		}
	}
	
	private void recieveBuddyListFromClient() throws IOException {
		//TODO make this dynamic
		String[] buddyListLines;
		// TODO Auto-generated method stub
		//sendSystemMessage(username, "Access Granted. Send me the username.");
		System.out.println("Should be begin: " + decryptServerPrivate(serverDin.readUTF()));
		buddyListLines = new String[(Integer.parseInt(decryptServerPrivate(serverDin.readUTF())))];
		System.out.println("Buddylist lines: "  + buddyListLines.length);
		for(int y=0; y<buddyListLines.length;y++) { 
				buddyListLines[y] = decryptServerPrivate(serverDin.readUTF());
				System.out.println("Encrypted buddylist lines " + buddyListLines[y]);
			}
		writeBuddyListToFile(buddyListLines, username);
		System.out.println("Successfully wrote buddy list to file");
	}
	
	//This method decrypts the ciphertext with the server's public key
	public static String decryptServerPrivate(String ciphertext) { 
		//Turn the String into a BigInteger. Get the bytes of the BigInteger for a byte[]
		byte[] cipherBytes = (new BigInteger(ciphertext)).toByteArray();
		//Decrypt the byte[], returns a String
		return RSACrypto.rsaDecryptPrivate(cipherBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
	}
	
	//This method decrypts the ciphertext with the server's public key
	public static String encryptServerPrivate(String plaintext) { 
		//Encrypt the string and return it
		if(debug==1) System.out.println("Plaintext in encryptServerPrivate: " + plaintext);
		BigInteger plaintextBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate(plaintext, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
		return plaintextBigInt.toString();
	}
	
	public void negotiateClientStatus() {
		try { 
			//Acknowledge connection. Make sure we are doing the right thing
			//Encrypt the String, turn it into a BigInteger
			//BigInteger accessGrantedCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Access granted. Send me the username.",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));

			//serverDout.write "Access Granted. Send me the username.");
			//Listen for the username
			String findUserCipher = serverDin.readUTF();
			System.out.println("FINDUSERCIPHER!@$!#@" +findUserCipher);
			byte[] findUserBytes = (new BigInteger(findUserCipher)).toByteArray();
			String findUserDecrypted = RSACrypto.rsaDecryptPrivate(findUserBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			//Print out the received username
			System.out.println("Username received: " + findUserDecrypted);
			//Check to see if the username is in the current Hashtable, return result
			if ((server.userToServerSocket.containsKey(findUserDecrypted))) { 
				serverDout.writeUTF(encryptServerPrivate("1"));
				System.out.println("(Online)\n");
			} else { serverDout.writeUTF(encryptServerPrivate("0"));
			System.out.println("(Offline)\n");
			} 
			
			/*//Maps username to socket after user logs in
			server.mapUserServerSocket(username, c2ssocket);	
			server.mapUserClientSocket(username, c2csocket);
			server.addServerOutputStream(c2ssocket,new DataOutputStream(c2ssocket.getOutputStream()));
			server.addClientOutputStream(c2csocket,new DataOutputStream(c2csocket.getOutputStream()));*/
		} catch ( Exception e ) { 
			e.printStackTrace();
		} 
	}

	public void negotiateClientStatus(String checkUserFlag) {
		try {
			System.out.println(username);
			//Encrypt the String, turn it into a BigInteger
			//BigInteger accessGrantedCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Access granted. Send me the username.",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
			//Acknowledge connection. Make sure we are doing the right thing
			//(username, "CheckUserStatus", accessGrantedCipher.toString());
			//Listen for the username
			String findUser = decryptServerPrivate(serverDin.readUTF());
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());
			//Print out the received username
			System.out.println("Username received: " + findUser);
			System.out.println("Socket serverDout: "+serverDout.toString());
			//Check to see if the username is in the current Hashtable, return result
			if ((server.userToServerSocket.containsKey(findUser))) { 
				
				serverDout.writeUTF(encryptServerPrivate("1"));
				System.out.println("(Online)\n");
			} else {				
				serverDout.writeUTF(encryptServerPrivate("0"));
				System.out.println("(Offline)\n");
			} 
		} catch ( Exception e ) { 
			e.printStackTrace();
		} 
	}
	
	public void returnBuddyListHash() { 
		//BigInteger accessGrantedCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Access granted. Send me the username.",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
		//"Access granted. Send me the username.");
		
		try {
			String buddyListToFind = serverDin.readUTF();
			byte[] buddyListBytes = (new BigInteger(buddyListToFind)).toByteArray();
			String buddyListDecrypted = RSACrypto.rsaDecryptPrivate(buddyListBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			
			//Grab the hash of the buddy list
				File buddylist = new File("buddylists/" + buddyListDecrypted + "/buddylist.csv");
				if(!(buddylist.exists())) { 
					boolean success = new File("buddylists/" + buddyListDecrypted + "/").mkdirs();
					if(success) { 
						buddylist.createNewFile();
					}
					else { 
					buddylist.createNewFile();
					}
				}
				String path = "buddylists/".concat(buddyListDecrypted).concat("/buddylist.csv");
				System.out.println("PATH: " + path);
				String hashOfBuddyList = FileHash.getMD5Checksum(path);
				String lastModDateOfBuddyList = String.valueOf(buddylist.lastModified());
				serverDout.writeUTF(encryptServerPrivate(hashOfBuddyList));
				serverDout.writeUTF(encryptServerPrivate(lastModDateOfBuddyList));
		} catch (Exception e) {
		}
	}
	//TODO Make this work better.
	public boolean createUsername() throws IOException { 
		try { 
			//Use dbConnect() to connect to the database
			Connection con = server.dbConnect();
			
			//Get the DataOutputStream 
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());

			//Create a statement and resultset for the query
			Statement stmt;
			Statement insertSTMT;
			ResultSet rs = null; 
			
			//Read the new user's public key components
			//TODO in this test we use this key for decryption, but we need to generate server keys for this
			String publicModString = serverDin.readUTF();
			String publicExpString = serverDin.readUTF();
			//Read in the private key components
			String privateKeyModString = serverDin.readUTF();
			String privateKeyExpString = serverDin.readUTF();

			//Read all encrypted data in
			String firstName = decryptServerPrivate(serverDin.readUTF());
			String lastName = decryptServerPrivate(serverDin.readUTF());
			String emailAddress = decryptServerPrivate(serverDin.readUTF());			
			String newUser = decryptServerPrivate(serverDin.readUTF());
			String newPassword = decryptServerPrivate(serverDin.readUTF());
			
			//TODO These have to go into the database for later recall
			String secretQuestion = decryptServerPrivate(serverDin.readUTF());
			String secretAnswer = decryptServerPrivate(serverDin.readUTF());

			//Turn the public key components into BigIntegers for use
			BigInteger publicMod = new BigInteger(publicModString);
			BigInteger publicExp = new BigInteger(publicExpString);
			
			//Turn the private key components into BigIntegers for use
			BigInteger privateMod = new BigInteger(privateKeyModString);
			BigInteger privateExp = new BigInteger(privateKeyExpString);


			//Write encrpyted private key to file		
			RSACrypto.saveToFile("keys/" + newUser + ".priv", privateMod, privateExp);
			if(debug>=1){
				System.out.println("New User Decrypted Information:");
				System.out.println("First Name: "+firstName);
				System.out.println("Last Name: "+lastName);
				System.out.println("Email Address: "+emailAddress);
				System.out.println("User Name: "+newUser);
				System.out.println("Password Hash: "+newPassword);
				System.out.println("Secret Question: "+secretQuestion);
				System.out.println("Secret Answer Hash: "+secretAnswer);
			}
			
			stmt = con.createStatement();
			if(debug==1)System.out.println("Statement created\nCreating username: "+newUser+"\nPassword: "+ newPassword);

			//See if the username already exists.
			rs = stmt.executeQuery("SELECT * FROM Users WHERE username = '" + newUser+"'");
			if(debug==1)System.out.println("newUser: " + newUser);

			//Test to see if there are any results
			if (rs.next()) { 
				//Send status message that the username has already been taken.
				BigInteger failedRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Username has already been taken, please try again.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				serverDout.writeUTF(failedRegistrationResultBigInt.toString());
				rs.close();
				return false;
			}
			else { 
				//Store the new user's public key on to the filesystem
				File newFile = new File("keys/" + newUser+ ".pub");
				if(!(newFile.exists())) { 
					newFile.createNewFile();
				}
				else { 
					return false;
				}
				RSACrypto.saveToFile("keys/"+newUser+".pub",publicMod,publicExp);

				//Grab the users new password
				String insertString = "insert into Users (FirstName, LastName, EmailAddress, username, password, secretq, secreta) values('" + firstName + "', '" + lastName + "', '" + emailAddress + "', '" + newUser + "', '" + newPassword + "', '" + secretQuestion + "', '" + secretAnswer +"')";
				insertSTMT = con.createStatement();
				insertSTMT.executeUpdate(insertString);
				

				//Inform of our success
				BigInteger successfulRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Account has been successfully created.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				serverDout.writeUTF(successfulRegistrationResultBigInt.toString());
				server.updateHashTable();
				
				//Close Connections
				stmt.close();
				insertSTMT.close();
				con.close();
				rs.close();
				return true;
			}
			
		}catch (SQLException se) { 
			//Inform of our failure
			BigInteger exceptionRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Something went wrong, please inform the Athena Administrators.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(exceptionRegistrationResultBigInt.toString());
			return false;
		}catch (IOException ie) { 
			//Inform of our failure
			BigInteger exceptionRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Something went wrong, please inform the Athena Administrators.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(exceptionRegistrationResultBigInt.toString());
			return false;
		} catch (Exception e) {
			//Inform of our failure
			BigInteger exceptionRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Something went wrong, please inform the Athena Administrators.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(exceptionRegistrationResultBigInt.toString());
			return false;
		}
	}


	//Sends message message from user fromUser (this thread/socket) to user toUser (another socket)
	//TODO: Separate findOuputSteam from this method?
	void sendMessage(String toUser, String fromUser, String message) {
		Socket foundSocket = null;

		//Debug statement: who is this going to?
		if(debug==1)System.out.print(toUser);

		//Look up the socket associated with the with whom we want to talk
		//We will use this to find which outputstream to send out
		//If we cannot find the user or socket, send back an error
		if ((server.userToClientSocket.containsKey(toUser))) { 
			if(debug==1)System.out.print("Found user.. Continuing...");
			foundSocket = (Socket) server.userToClientSocket.get(toUser);
			if(debug==1)System.out.print("Found Socket: " + foundSocket);
		} else { sendMessage(fromUser, "UnavailableUser", encryptServerPrivate(toUser)); return; } 

		//Find the outputstream associated with toUser's socket
		//We send data through this outputstream to send the message
		//If we cannot find the outputstream, send back an error
		//This should not fail
		if (server.clientOutputStreams.containsKey(foundSocket)) { 
			clientDout = (DataOutputStream) server.clientOutputStreams.get(foundSocket);
		} else { sendMessage(fromUser, "MissingSocket", encryptServerPrivate(toUser)); return; }

		//Send the message, and the user it is from
		try {
			//Encrypt the fromUser with the public key of userB (we want everything to be anonymous, so we can't encrypt the fromUser with the private key of the server, anyone can decrypt that)
			BigInteger fromUserCipherBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate(fromUser, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			clientDout.writeUTF(fromUserCipherBigInt.toString());
			clientDout.writeUTF(message);
		} catch( IOException ie ) { System.out.println( ie ); }
		if(debug>=1)System.out.println("message sent, i think");
	}

	//Send system Messages to selected user
	void sendSystemMessage(String toUser, String message) { 
		Socket foundSocket = null;

		//Debug statement: who is this going to?
		if(debug==1)System.out.println("Who is this message going to? " + toUser);

		//Look up the socket associated with the with whom we want to talk
		//We will use this to find which outputstream to send out
		//If we cannot find the user or socket, send back an error
		if ((server.userToServerSocket.containsKey(toUser))) { 
			if(debug==1)System.out.println("Found user.. Continuing...");
			foundSocket = (Socket) server.userToServerSocket.get(toUser);
			if(debug==1)System.out.println("Found Socket: " + foundSocket);
		} 

		//Find the outputstream associated with toUser's socket
		//We send data through this outputstream to send the message
		//If we cannot find the outputstream, send back an error
		//This should not fail
		if (server.serverOutputStreams.containsKey(foundSocket)) { 
			serverDout = (DataOutputStream) server.serverOutputStreams.get(foundSocket);
		} 

		//Send the message, and the user it is from
		try {
			System.out.println("TOUSER: " + toUser + "\nMESSAGE: " + message);
			BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(message,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			BigInteger toUserCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(toUser, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			//dout.writeUTF(toUserCipher.toString());
			serverDout.writeUTF(messageCipher.toString());
			if(debug>=1)System.out.println("Message sent:\n " + message);
		} catch( IOException ie ) { System.out.println( ie ); }
	}
	
	//This will authenticate the user, before they are allowed to send messages.	
	public String login(String clientName, String clientPassword) throws IOException { 
		serverDout = new DataOutputStream(c2ssocket.getOutputStream());
		String hashedPassword;
		try{
			//Get the password from the hashtable
			hashedPassword = server.authentication.get(clientName).toString();
		}catch(Exception e){
			//Login fail handler
			if(debug>=1)System.out.println("User has failed to login");
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Failed", server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(returnCipher.toString());
			server.removeConnection(c2ssocket,c2csocket, clientName);
			return returnCipher.toString();  
		}	

		//Debug messages.
		//TODO: Come up with better debug messages
		if (debug==1)System.out.println("User logging in...");
		if (debug==1)System.out.println("Hashed Password:" + hashedPassword);
		if (debug==1)System.out.println("Username :" + clientName);

		//Verify the password hash provided from the user matches the one in the server's hashtable
		if (clientPassword.equals(hashedPassword)) { 
			//Run some command that lets user log in!
			//TODO: We need to broadcast a message letting everyone know a user logged in?
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Success", server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(returnCipher.toString());
			return returnCipher.toString();
		}else { 
			//Login fail handler
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Failed", server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(returnCipher.toString());
			server.removeConnection(c2ssocket, c2csocket,clientName);
			return returnCipher.toString();  
		}	
	}
	//This will return the hashed input string
	public String computeHash(String toHash) throws Exception { 
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
	
	
	public void publicKeyRequest() throws IOException{

		System.out.println(username);
		//Acknowledge connection. Make sure we are doing the right thing
		BigInteger accessGrantedCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Access granted. Send me the username to find the key for.",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
		//sendSystemMessage(username, "Aegis");
		//serverDout.writeUTF(encryptServerPrivate("ReturnPublicKey"));
		
		//serverDout.writeUTF(accessGrantedCipher.toString());
		try{
			//Listen for the username
			String findUser = decryptServerPrivate(serverDin.readUTF());
			
			//Print out the received username
			if(debug>=1)System.out.println("Username received PUBLIC KEY REQUEST: " + findUser);


			File newFile = new File("keys/" + findUser + ".pub");
			if((newFile.exists())) {
				RSAPublicKeySpec keyToReturn = RSACrypto.readPubKeyFromFile("keys/"+findUser+".pub");
				System.out.println("MOD: " + keyToReturn.getModulus().toString());
				System.out.println("EXP: " + keyToReturn.getPublicExponent().toString());

				//Check to see if the user has a key file on the server
				//BigInteger keyToReturnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(keyToReturn.getModulus().toString(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				serverDout.writeUTF(keyToReturn.getModulus().toString());
				if(debug>=1)System.out.println("Modulus Returned\n");
				//BigInteger exponentToReturnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(keyToReturn.getPublicExponent().toString(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				serverDout.writeUTF(keyToReturn.getPublicExponent().toString());
				if(debug>=1)System.out.println("Exponent Returned\n");

			} else { 
				//BigInteger keyNotFoundCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("-1",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
				serverDout.writeUTF("-1" );
				if(debug>=1)System.out.println("User does not have a keyfile with us");
			} }catch(Exception e){e.printStackTrace();}
	}
}

