
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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
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

import sun.misc.BASE64Encoder;
import sun.security.util.BigInt;

public class ServerThread extends Thread
{
	//Change to 1 for debug output
	private static int debug = 1;

	//Create the DataInputStream on the current socket 
	public DataInputStream din = null;
	public DataOutputStream dout = null;

	// The Server that created this thread
	private static Server server;

	//Define Global Variable Username / Password
	private String username;
	private String password;

	//Our current socket
	public Socket socket;

	//Message digest for the hashed password
	MessageDigest hashedPassword;
	//Governs thread life. If connection is not alive, thread terminates
	private int isAlive=1;

	private RSAPrivateKeySpec serverPrivate;
	// Constructor. Instantiate this thread on the current socket
	public ServerThread( Server server, Socket socket ) {

		// Remember which socket we are on
		this.server = server;
		this.socket = socket;

		//Start up the thread
		start();
	}

	//This runs when the thread starts. It controls everything.
	public void run() {
		try {
			//Create a datainputstream on the current socket to accept data from the client
			din = new DataInputStream( socket.getInputStream() );

			//Getting the Username and Password over the stream for authentication
			String usernameCipher = din.readUTF();
			System.out.println("EncryptedUsername: " + usernameCipher);
			username = RSACrypto.rsaDecryptPrivate(new BigInteger(usernameCipher).toByteArray(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());

			System.out.println("USER TRYING TO LOG IN DECRYPTED:::: "+username);
			if(username.equals("Interupt")) { 

			} else { 
				String passwordCipher = din.readUTF(); 
				password = RSACrypto.rsaDecryptPrivate(new BigInteger(passwordCipher).toByteArray(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
				System.out.println("PASSWORD: " + password);

				//Debug statements
				if (debug==1)System.out.println("Username: " + username);
				if (debug==1)System.out.println("Password: " + password);

				//Authenticate the user.
				String loginOutcome = login(username, password);
				if (debug==1)System.out.println(loginOutcome);

				//Maps username to socket after user logs in
				server.mapUserSocket(username, socket);	
			}
			if(username.equals("Interupt")) {
				routeMessage(din);
				//server.removeConnection(socket);				
			} else { 
				//Route around messages coming in from the client while they are connected
				while (isAlive==1) {
					//Take in messages from this thread's client and route them to another client
					routeMessage(din);
				}
			}

		} catch ( EOFException ie ) {
		} catch ( IOException ie ) {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			//Socket is closed, remove it from the list
			server.removeConnection( socket, username );
		}
	}

	//Takes in a recipient and message from this thread's user
	//and routes the message to the recipient.
	public void routeMessage(DataInputStream din) throws NumberFormatException, InterruptedException{
		try {
			//Grab the server's private key - SHHH!!
			serverPrivate = RSACrypto.readPrivKeyFromFile("keys/Aegis.priv");
			//Read in the Encrypted toUser
			String toUserEncrypted=din.readUTF();
			//Read in the Encrypted message
			String messageEncrypted=din.readUTF(); 
			//Read in the Digital Signature
			//String digitalSignatureEncrypted = din.readUTF();
			System.out.println("Encrypted:" +  toUserEncrypted);
			//Decrypt the to user
			byte[] toUserBytes = (new BigInteger(toUserEncrypted)).toByteArray();
			String toUserDecrypted = RSACrypto.rsaDecryptPrivate(toUserBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());

			System.out.println("Decrypted:" +  toUserDecrypted);

			//Is the message an eventcode meant for the server?
			if (toUserDecrypted.equals("Aegis")) { 
				//if(debug==1)System.out.println("Server eventcode detected!");

				systemMessageListener(Integer.parseInt(RSACrypto.rsaDecryptPrivate(new BigInteger(messageEncrypted).toByteArray(), serverPrivate.getModulus(), serverPrivate.getPrivateExponent())));
				return;
			}
			if (toUserDecrypted.equals("Interupt")) {
				systemMessageListener(Integer.parseInt(RSACrypto.rsaDecryptPrivate(new BigInteger(messageEncrypted).toByteArray(), serverPrivate.getModulus(), serverPrivate.getPrivateExponent())));
				return;
			}	
			else { 
				if(debug==1)System.out.println("Routing normal message");
				sendMessage(toUserDecrypted, username, messageEncrypted);
			}

		} catch (IOException e) {isAlive=0;}
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
			System.out.println("Event code received. negotiateClientStatus() run.");
			negotiateClientStatus();
			//System.out.println("Event code received. negotiateClientStatus() run.");
			break;
		case 002: server.sendToAll("ServerLogOn", username);
		break;
		case 003: negotiateClientStatus("CheckUserStatus");
		break;
		case 004: publicKeyRequest();
		break;
		case 005: returnBuddyListHash();
		break;
		case 006: recieveBuddyListFromClient();
		break;
		case 007: sendPrivateKeyToClient();
		break;
		default: return;
		}
	}
	private void sendPrivateKeyToClient() throws IOException {
		RSAPrivateKeySpec privateKey = RSACrypto.readPrivKeyFromFile("keys/" + username + ".priv");
		//Send over ack message
		sendSystemMessage(username, "Incoming private key components");
		
		//Send over components
		String privateKeyMod = privateKey.getModulus().toString();
		String privateKeyExp = privateKey.getPrivateExponent().toString();
		//Send half a time plz!
		System.out.println(privateKeyMod.length());
		int chunks = (privateKeyMod.length()/245);
		//Send how many chunks will be coming
		dout.writeUTF(String.valueOf(chunks));
		/*
		 * Copied code
		 */
		StringReader r = new StringReader (privateKeyMod);
		String encoding = "UTF-8"; 
		ByteArrayOutputStream buffer = new ByteArrayOutputStream (245); // Twice as large as necessary 
		OutputStreamWriter w = new OutputStreamWriter  (buffer, encoding); 
		 
		char[] cbuf = new char[100]; 
		byte[] tempBuf; 
		int len; 
		while ((len = r.read(cbuf, 0, cbuf.length)) > 0) { 
		    w.write(cbuf, 0, len); 
		    w.flush(); 
		    if (buffer.size() >= 245) { 
		        tempBuf = buffer.toByteArray(); 		        
		        //Try to write one chunk!!
		        dout.writeUTF(encryptServerPrivate(tempBuf.toString()));
		        buffer.reset(); 
		        if (tempBuf.length > 245) { 
		            buffer.write(tempBuf, 245, tempBuf.length - 245); 
		        } 
		    } 
		}
		dout.writeUTF("end");
		/*
		 * End copied code
		 */
		/*dout.writeUTF(encryptServerPrivate(privateKey.getModulus().toString().substring(0, ((privateKeyMod.length()/4)-1))));
		dout.writeUTF(encryptServerPrivate(privateKey.getModulus().toString().substring((privateKeyMod.length()/4),(privateKeyMod.length()/2))));
		dout.writeUTF(encryptServerPrivate(privateKey.getModulus().toString().substring((privateKeyMod.length()/2)-((privateKeyMod.length()-1)-(privateKeyMod.length()/4)))));
		dout.writeUTF(encryptServerPrivate(privateKey.getModulus().toString().substring(((privateKeyMod.length()-1)-(privateKeyMod.length()/4)), ((privateKeyMod.length()-1)))));
		dout.writeUTF(encryptServerPrivate(privateKey.getPrivateExponent().toString()));*/		
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
		sendSystemMessage(username, "Access Granted. Send me the username.");
		System.out.println("Should be begin: " + decryptServerPrivate(din.readUTF()));
		buddyListLines = new String[(Integer.parseInt(decryptServerPrivate(din.readUTF())))];
		System.out.println("Buddylist lines: "  + buddyListLines.length);
		for(int y=0; y<buddyListLines.length;y++) { 
				buddyListLines[y] = decryptServerPrivate(din.readUTF());
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

			sendSystemMessage(username, "Access Granted. Send me the username.");
			//Listen for the username
			String findUserCipher = din.readUTF();
			System.out.println("FINDUSERCIPHER!@$!#@" +findUserCipher);
			byte[] findUserBytes = (new BigInteger(findUserCipher)).toByteArray();
			String findUserDecrypted = RSACrypto.rsaDecryptPrivate(findUserBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			//Print out the received username
			System.out.println("Username received: " + findUserDecrypted);
			//Check to see if the username is in the current Hashtable, return result
			if ((server.userToSocket.containsKey(findUserDecrypted))) { 
				sendSystemMessage(username,"1");
				System.out.println("(Online)\n");
			} else { sendSystemMessage(username,"0");
			System.out.println("(Offline)\n");
			} 
		} catch ( Exception e ) { 
			e.printStackTrace();
		} 
	}

	public void negotiateClientStatus(String checkUserFlag) {
		try {
			System.out.println(username);
			//Encrypt the String, turn it into a BigInteger
			BigInteger accessGrantedCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Access granted. Send me the username.",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
			//Acknowledge connection. Make sure we are doing the right thing
			sendMessage(username, "CheckUserStatus", accessGrantedCipher.toString());
			//Listen for the username
			String findUserCipher = din.readUTF();

			byte[] findUserBytes = (new BigInteger(findUserCipher)).toByteArray();
			String findUserDecrypted = RSACrypto.rsaDecryptPrivate(findUserBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			//Print out the received username
			System.out.println("Username received: " + findUserDecrypted);
			//Check to see if the username is in the current Hashtable, return result
			if ((server.userToSocket.containsKey(findUserDecrypted))) { 
				String message = "1";
				BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(message,serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
				sendMessage(username, "CheckUserStatusResult", messageCipher.toString());
				System.out.println("(Online)\n");
			} else {
				String message = "0";
				BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(message,serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
				sendMessage(username, "CheckUserStatusResult", messageCipher.toString());
				System.out.println("(Offline)\n");
			} 
		} catch ( Exception e ) { 
			e.printStackTrace();
		} 
	}
	
	public void returnBuddyListHash() { 
		//BigInteger accessGrantedCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Access granted. Send me the username.",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
		sendSystemMessage(username, "Access granted. Send me the username.");
		
		try {
			String buddyListToFind = din.readUTF();
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
				sendSystemMessage(username, hashOfBuddyList);
				sendSystemMessage(username, lastModDateOfBuddyList);
		} catch (Exception e) {
		}
	}
	//TODO Make this work better.
	public boolean createUsername() throws IOException { 
		try { 
			//Use dbConnect() to connect to the database
			Connection con = server.dbConnect();
			
			//Get the DataOutputStream 
			dout = new DataOutputStream(socket.getOutputStream());

			//Create a statement and resultset for the query
			Statement stmt;
			Statement insertSTMT;
			ResultSet rs = null; 
			
			//Read the new user's public key components
			//TODO in this test we use this key for decryption, but we need to generate server keys for this
			String publicModString = din.readUTF();
			String publicExpString = din.readUTF();
			//Read in the private key components
			String privateKeyModString = din.readUTF();
			String privateKeyExpString = din.readUTF();

			//Read all encrypted data in
			String firstNameCipher = din.readUTF();
			String lastNameCipher = din.readUTF();
			String emailAddressCipher = din.readUTF();
			String userNameCipher = din.readUTF();
			String passwordCipher = din.readUTF();

			//Turn the public key components into BigIntegers for use
			BigInteger publicMod = new BigInteger(publicModString);
			BigInteger publicExp = new BigInteger(publicExpString);
			
			//Turn the private key components into BigIntegers for use
			BigInteger privateMod = new BigInteger(privateKeyModString);
			BigInteger privateExp = new BigInteger(privateKeyExpString);


			//Turn encrypted data into BigIntegers, then byte[]s
			byte[] firstNameBytes = (new BigInteger(firstNameCipher)).toByteArray(); //does this work?
			byte[] lastNameBytes = (new BigInteger(lastNameCipher)).toByteArray();
			byte[] emailAddressBytes = (new BigInteger(emailAddressCipher)).toByteArray();
			byte[] userNameBytes = (new BigInteger(userNameCipher)).toByteArray();
			byte[] passwordBytes = (new BigInteger(passwordCipher)).toByteArray();
			//BigInteger firstNameNumber = new BigInteger(firstNameCipher);
			//byte[] firstNameBytes = firstNameNumber.toByteArray();

			//Finally, decrypt the ciphertext
			String firstName = RSACrypto.rsaDecryptPrivate(firstNameBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			String lastName = RSACrypto.rsaDecryptPrivate(lastNameBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			String emailAddress = RSACrypto.rsaDecryptPrivate(emailAddressBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			String newUser = RSACrypto.rsaDecryptPrivate(userNameBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			String newPassword = RSACrypto.rsaDecryptPrivate(passwordBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());
			
			//Write encrpyted private key to file		
			RSACrypto.saveToFile("keys/" + newUser + ".priv", privateMod, privateExp);

			System.out.println("New User Decrypted Information:");
			System.out.println("First Name: "+firstName);
			System.out.println("Last Name: "+lastName);
			System.out.println("Email Address: "+emailAddress);
			System.out.println("User Name: "+newUser);
			System.out.println("Password Hash: "+newPassword);

			stmt = con.createStatement();
			if(debug==1)System.out.println("Statement created\nCreating username: "+newUser+"\nPassword: "+ newPassword);

			//See if the username already exists.
			rs = stmt.executeQuery("SELECT * FROM Users WHERE username = '" + newUser+"'");
			if(debug==1)System.out.println("newUser: " + newUser);

			//Test to see if there are any results
			if (rs.next()) { 
				//Send status message that the username has already been taken.
				BigInteger failedRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Username has already been taken, please try again.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				dout.writeUTF(failedRegistrationResultBigInt.toString());
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
				String insertString = "insert into Users (FirstName, LastName, EmailAddress, username, password) values('" + firstName + "', '" + lastName + "', '" + emailAddress + "', '" + newUser + "', '" + newPassword + "')";
				insertSTMT = con.createStatement();
				insertSTMT.executeUpdate(insertString);
				

				//Inform of our success
				BigInteger successfulRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Username has been sucessfully created.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				dout.writeUTF(successfulRegistrationResultBigInt.toString());
				server.updateHashTable();
				
				//Close Connections
				stmt.close();
				insertSTMT.close();
				con.close();
				return true;
			}
		}catch (SQLException se) { 
			//Inform of our failure
			BigInteger exceptionRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Something went wrong, please inform the Athena Administrators.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			dout.writeUTF(exceptionRegistrationResultBigInt.toString());
			return false;
		}catch (IOException ie) { 
			//Inform of our failure
			BigInteger exceptionRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Something went wrong, please inform the Athena Administrators.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			dout.writeUTF(exceptionRegistrationResultBigInt.toString());
			return false;
		} catch (Exception e) {
			//Inform of our failure
			BigInteger exceptionRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Something went wrong, please inform the Athena Administrators.",server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			dout.writeUTF(exceptionRegistrationResultBigInt.toString());
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
		if ((server.userToSocket.containsKey(toUser))) { 
			if(debug==1)System.out.print("Found user.. Continuing...");
			foundSocket = (Socket) server.userToSocket.get(toUser);
			if(debug==1)System.out.print("Found Socket: " + foundSocket);
		} else { sendMessage(fromUser, "UnavailableUser", toUser); return; } 

		//Find the outputstream associated with toUser's socket
		//We send data through this outputstream to send the message
		//If we cannot find the outputstream, send back an error
		//This should not fail
		if (server.outputStreams.containsKey(foundSocket)) { 
			dout = (DataOutputStream) server.outputStreams.get(foundSocket);
		} else { sendMessage(fromUser, "MissingSocket", toUser); return; }

		//Send the message, and the user it is from
		try {
			//Encrypt the fromUser with the public key of userB (we want everything to be anonymous, so we can't encrypt the fromUser with the private key of the server, anyone can decrypt that)
			BigInteger fromUserCipherBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate(fromUser, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			dout.writeUTF(fromUserCipherBigInt.toString());
			dout.writeUTF(message);
		} catch( IOException ie ) { System.out.println( ie ); }
		System.out.println("message sent, i think");
	}

	//Send system Messages to selected user
	void sendSystemMessage(String toUser, String message) { 
		Socket foundSocket = null;

		//Debug statement: who is this going to?
		if(debug==1)System.out.println("Who is this message going to? " + toUser);

		//Look up the socket associated with the with whom we want to talk
		//We will use this to find which outputstream to send out
		//If we cannot find the user or socket, send back an error
		if ((server.userToSocket.containsKey(toUser))) { 
			if(debug==1)System.out.println("Found user.. Continuing...");
			foundSocket = (Socket) server.userToSocket.get(toUser);
			if(debug==1)System.out.println("Found Socket: " + foundSocket);
		} 

		//Find the outputstream associated with toUser's socket
		//We send data through this outputstream to send the message
		//If we cannot find the outputstream, send back an error
		//This should not fail
		if (server.outputStreams.containsKey(foundSocket)) { 
			dout = (DataOutputStream) server.outputStreams.get(foundSocket);
		} 

		//Send the message, and the user it is from
		try {
			System.out.println("TOUSER: " + toUser + "\nMESSAGE: " + message);
			BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(message,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			BigInteger toUserCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(toUser, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			//dout.writeUTF(toUserCipher.toString());
			dout.writeUTF(messageCipher.toString());
			System.out.println("Message sent:\n " + message);
		} catch( IOException ie ) { System.out.println( ie ); }
	}
	//This will authenticate the user, before they are allowed to send messages.	
	public String login(String clientName, String clientPassword) throws IOException { 
		dout = new DataOutputStream(socket.getOutputStream());
		String hashedPassword;
		try{
			//Get the password from the hashtable
			hashedPassword = server.authentication.get(clientName).toString();
		}catch(Exception e){
			//Login fail handler
			System.out.println("FUCKING FAILED");
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Failed", server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			dout.writeUTF(returnCipher.toString());
			server.removeConnection(socket, clientName);
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
			dout.writeUTF(returnCipher.toString());
			return returnCipher.toString();
		}else { 
			//Login fail handler
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Failed", server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
			dout.writeUTF(returnCipher.toString());
			server.removeConnection(socket, clientName);
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
	
	
	public void publicKeyRequest(){

		System.out.println(username);
		//Acknowledge connection. Make sure we are doing the right thing
		BigInteger accessGrantedCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Access granted. Send me the username to find the key for.",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
		sendMessage(username, "ReturnPublicKey", accessGrantedCipher.toString());
		try{
			//Listen for the username
			String findUser = din.readUTF();
			byte[] findUserBytes = (new BigInteger(findUser)).toByteArray();
			String findUserDecrypted = RSACrypto.rsaDecryptPrivate(findUserBytes,server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent());

			//Print out the received username
			System.out.println("Username received PUBLIC FUCKING KEY REQUEST: " + findUser);


			File newFile = new File("keys/" + findUserDecrypted + ".pub");
			if((newFile.exists())) {
				RSAPublicKeySpec keyToReturn = RSACrypto.readPubKeyFromFile("keys/"+findUserDecrypted+".pub");
				System.out.println("MOD: " + keyToReturn.getModulus().toString());
				System.out.println("EXP: " + keyToReturn.getPublicExponent().toString());

				//Check to see if the user has a key file on the server
				//BigInteger keyToReturnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(keyToReturn.getModulus().toString(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				sendMessage(username, "ReturnPublicKeyMod", keyToReturn.getModulus().toString());
				System.out.println("Modulus Returned\n");
				//BigInteger exponentToReturnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(keyToReturn.getPublicExponent().toString(),server.serverPriv.getModulus(),server.serverPriv.getPrivateExponent()));
				sendMessage(username, "ReturnPublicKeyExp", keyToReturn.getPublicExponent().toString());
				System.out.println("Exponent Returned\n");

			} else { 
				BigInteger keyNotFoundCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("-1",serverPrivate.getModulus(),serverPrivate.getPrivateExponent()));
				sendMessage(username, "ReturnPublicKeyMod",keyNotFoundCipher.toString() );
				System.out.println("User does not have a keyfile with us");
			} }catch(Exception e){e.printStackTrace();}
	}
}

