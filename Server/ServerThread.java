/* Athena/Aegis Encrypted Chat Platform
 * ServerThread.java: Controls the threads for each user connected to Aegis
 *
 * Copyright (C) 2010  OlympuSoft
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.BASE64Encoder;

/**
 * A ServerThread is created for each connected user, to route messages around
 * @author OlympuSoft
 */
public class ServerThread extends Thread {

	//Change to 1 or 2 for debug output
	private static int debug = 1;
	//Create the DataStreams on the current sockets
	private DataInputStream serverDin = null;
	private DataInputStream clientDin = null;
	private DataOutputStream serverDout = null;
	private DataOutputStream clientDout = null;
	// The Server that created this thread
	private static Server server;
	//Define Global Variable Username / Password
	private String username;
	private String password;
	//Our current sockets
	private Socket c2ssocket;
	private Socket c2csocket;
	//Message digest for the hashed password
	private MessageDigest hashedPassword;
	//Governs thread life. If connection is not alive, thread terminates
	private int isAlive = 1;

	/**
	 * Instantiate this thread for the current sockets
	 * @param server The server that spawned this thread
	 * @param c2ssocket The "server" thread
	 * @param c2csocket The "client" thread
	 */
	public ServerThread(Server server, Socket c2ssocket, Socket c2csocket) {

		// Remember which socket we are on
		this.server = server;
		this.c2ssocket = c2ssocket;
		this.c2csocket = c2csocket;

		//Start up the thread
		start();
	}

	/**
	 * Runs when the thread starts. Let's user log in, then routes messages
	 */
	@Override
	public void run() {
		try {
			//Create a datainputstream on the current socket to accept data from the client
			serverDin = new DataInputStream(c2ssocket.getInputStream());
			clientDin = new DataInputStream(c2csocket.getInputStream());

			//Getting the Username over the stream for authentication
			String usernameCipher = serverDin.readUTF();

			if (debug == 2) {
				System.out.println("Encrypted Username: " + usernameCipher);
			}

			//Decrypt the username
			username = RSACrypto.rsaDecryptPrivate(new BigInteger(usernameCipher).toByteArray(),
					server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent());

			if (debug >= 1) {
				System.out.println("Decrypted Username: " + username);
			}

			//Interupt means they want to create a new user
			if (username.equals("Interupt")) {
				//Do nothing
			} else {
				//Get the password hash
				password = decryptServerPrivate(serverDin.readUTF());

				//Debug statements
				if (debug >= 1) {
					System.out.println("Username: " + username);
					System.out.println("Password: " + password);
				}

				//Authenticate the user.
				String loginOutcome = login(username, password);
				if (debug >= 1) {
					System.out.println(loginOutcome);
				}

				//Maps username to socket after user logs in
				server.mapUserServerSocket(username, c2ssocket);
				server.mapUserClientSocket(username, c2csocket);
				System.gc();
			}
			if (username.equals("Interupt")) {
				routeMessage(serverDin, clientDin);
			} else {

				//Route around messages coming in from the client while they are connected
				while (isAlive == 1) {
					//Take in messages from this thread's client and route them to another client
					routeMessage(serverDin, clientDin);
					System.gc();
				}


			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//Socket is closed, remove it from the list
			try {

				if (debug >= 1) {
					System.out.println("REMOVING USERNAME: " + username);
				}

				if (username == null) {
					server.removeConnection(c2ssocket, c2csocket);
				} else {
					server.removeConnection(c2ssocket, c2csocket, username);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Routes messages for the thread's user
	 * @param serverDin The "server" inputstream
	 * @param clientDin The "client" inputstream
	 * @throws NumberFormatException Generally, difficulty encrypting or decrypting message headers
	 * @throws InterruptedException
	 */
	public void routeMessage(DataInputStream serverDin, DataInputStream clientDin) throws NumberFormatException, InterruptedException {
		try {
			//Read in the toUser
			String toUser = decryptServerPrivate(serverDin.readUTF());
			//Read in the From User
			String fromUser = decryptServerPrivate(serverDin.readUTF());
			//Read in the Encrypted message
			String messageEncrypted = serverDin.readUTF();

			if (debug >= 1) {
				System.out.println("Decrypted:" + toUser);
			}

			//Is the message an eventcode meant for the server?
			if (toUser.equals("Aegis")) {
				if (debug >= 1) {
					System.out.print("Server eventcode detected! ");
				}
				if (debug >= 1) {
					System.out.println(decryptServerPrivate(messageEncrypted));
				}
				try {
					systemMessageListener(Integer.parseInt(decryptServerPrivate(messageEncrypted)));
				} catch (NumberFormatException e) {
					System.out.println("Message is NOT an eventcode. Ignoring...");
				}
				return;
			}//Is the message someone trying to create an account?
			if (toUser.equals("Interupt")) {
				try {
					systemMessageListener(Integer.parseInt(decryptServerPrivate(messageEncrypted)));
				} catch (NumberFormatException e) {
					System.out.println("Message is NOT an eventcode. Continuing...");
				}
				return;
			} //Is this a normal message to another client
			else {
				if (debug >= 1) {
					System.out.println("Routing normal message to: " + toUser + "\nmessage from: " + fromUser);
				}
				if (debug == 2) {
					System.out.println("\nEncrypted message: " + messageEncrypted);
				}
				sendMessage(toUser, fromUser, messageEncrypted);

			}
			//Collect some garbage
			System.gc();

		} catch (IOException e) {
			//Something broke. Disconnect the user.
			if (debug == 2) {
				e.printStackTrace();
			}
			isAlive = 0;
		}
	}

	/**
	 * Handles all messages addressed to "Aegis"
	 * @param eventCode The eventcode requested by the client
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void systemMessageListener(int eventCode) throws InterruptedException, IOException {

		switch (eventCode) {
			case 000:
				if (debug == 1) {
					System.out.println("Event code received. createUsername() run.");
				}
				createUsername();
				break;
			case 001:
				if (debug == 1) {
					System.out.println("Event code received. negotiateClientStatus() run.");
				}
				negotiateClientStatus();
				break;
			case 002:
				if (debug == 1) {
					System.out.println("Event code received. senToAll() run.");
				}
				server.sendToAll("ServerLogOn", username);
				break;
			case 003:
				if (debug == 1) {
					System.out.println("Event code received. negotiateClientStatus(\"Checkuserstatus\") run.");
				}
				negotiateClientStatus("CheckUserStatus");
				break;
			case 004:
				if (debug == 1) {
					System.out.println("Event code received. publicKeyRequest() run.");
				}
				publicKeyRequest();
				break;
			case 005:
				if (debug == 1) {
					System.out.println("Event code received. returnBuddyListHash() run.");
				}
				returnBuddyListHash();
				break;
			case 006:
				if (debug == 1) {
					System.out.println("Event code received. receiveBuddyListfromClient() run.");
				}
				recieveBuddyListFromClient();
				break;
			case 007:
				if (debug == 1) {
					System.out.println("Event code received. sendPrivateKeyToClients() run.");
				}
				sendPrivateKeyToClient();
				break;
			case 8:
				if (debug == 1) {
					System.out.println("Event code received. sendBuddyListToClient() run.");
				}
				sendBuddyListToClient();
				break;
			case 9:
				if (debug == 1) {
					System.out.println("Event code received. receiveBugReport() run.");
				}
				receiveBugReport();
				break;
			case 10:
				if (debug == 1) {
					System.out.println("Event code received. receiveBugReport(flag) run.");
				}
				receiveBugReport(true);
				break;
			case 11:
				if (debug == 1) {
					System.out.println("Event code received. resetPassword() run.");
				}
				resetPassword();
				break;
			case 12:
				if (debug == 1) {
					System.out.println("Event code received. createChat() run.");
				}
				createChat();
				break;
			case 13:
				if (debug == 1) {
					System.out.println("Event code received. requestSocketInfo() run.");
				}
				//requestSocketInfo();
				break;
			case 14:
				if (debug == 1) {
					System.out.println("Event code received. joinChat() run.");
				}
				joinChat();
				break;
			case 15:
				if (debug == 1) {
					System.out.println("Event code received. leaveChat() run.");
				}
				leaveChat();
				break;
			case 16:
				if (debug == 1) {
					System.out.println("Event code received. chatInvite() run.");
				}
				chatInvite();
				break;
			case 17:
				if (debug == 1) {
					System.out.println("Event code received. chatTalk() run.");
				}
				chatTalk();
				break;
			case 18:
				if (debug == 1) {
					System.out.println("Event code received. userList() run.");
				}
				userList();
				break;
			case 19:
				if (debug >= 1) {
					System.out.println("Event code received. DPInvite() run.");
				}
				dPInvite();
				break;
			case 20:
				if (debug >= 1) {
					System.out.println("Event code received. dPResult() run.");
				}
				dPResult();
				break;
			default:
				return;
		}
	}

	private void dPResult() {
		try{
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());
			String usernameResult = decryptServerPrivate(serverDin.readUTF());
			String success = decryptServerPrivate(serverDin.readUTF());
			String inviteString = username + "," + success + "," + requestSocketInfo(username);

			sendMessage(usernameResult, "DPResult", encryptServerPrivate(inviteString));

		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	private void dPInvite() {
		try {
			if (debug == 1) {
				System.out.println("In dPInvite()");
			}
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());
			
			String invitingUser = decryptServerPrivate(serverDin.readUTF());
			String sessionKey = serverDin.readUTF();

			//For each user to invite, take their name, and take the session key encrypted with their public key
			if (debug == 1) {
				System.out.println("Inviting user: " + invitingUser);
			}
			sendMessage(invitingUser, "DPInvite", encryptServerPrivate(username));
			sendMessage(invitingUser, "DPSessionKey", sessionKey);
			if (debug >= 1) {
				System.out.println("Sent invitation");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends the client the userlist for the selected group chat
	 */
	private void userList() {
		try {
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());

			//Get the chatUID
			int chatNum = Integer.parseInt(decryptServerPrivate(serverDin.readUTF()));

			//Grab a DB connection
			Connection con = server.dbConnect();
			Statement stmt;
			ResultSet rs = null;

			//Get a list of existing chats
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT username FROM chat WHERE chatid = '" + chatNum + "';");

			if (debug >= 1) {
				System.out.println("Got list of users");
			}

			String message = "";

			//Send the message to the users in the chat
			while (rs.next()) {
				message += rs.getString(1) + ",";
			}

			//Close everything
			rs.close();
			stmt.close();
			con.close();

			//Return the userlist
			serverDout.writeUTF(encryptServerPrivate(message));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message from the client to the selected group chat
	 */
	private void chatTalk() {
		try {
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());

			//Take in the chatUID and the message
			int chatNum = Integer.parseInt(decryptServerPrivate(serverDin.readUTF()));
			String message = serverDin.readUTF();

			//We have the message. Now we have to find out who to send it to.
			if (debug == 1) {
				System.out.println("Sending received message to chat number " + chatNum);
			}

			//Grab a DB connection
			Connection con = server.dbConnect();
			Statement stmt;
			ResultSet rs = null;

			//Get a list of existing chats
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT username FROM chat WHERE chatid = '" + chatNum + "' AND username != '" + username + "';");
			System.out.println("Got list of users");

			//Send the message to the users in the chat
			while (rs.next()) {
				sendMessage(rs.getString(1), String.valueOf(chatNum), message);
			}

			//Close all DB connections
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a chat invitation to a specific chat to a list of selected users
	 */
	private void chatInvite() {
		try {
			if (debug == 1) {
				System.out.println("In chatInvite()");
			}
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());

			//Get the chatUID, chat name, and number of users to invite
			int chatNum = Integer.parseInt(decryptServerPrivate(serverDin.readUTF()));
			String chatName = decryptServerPrivate(serverDin.readUTF());
			String inviteString = chatName + "," + username + "," + chatNum;
			int inviteList = Integer.parseInt(decryptServerPrivate(serverDin.readUTF()));

			//Debug statements
			if (debug >= 1) {
				System.out.println("Received chat number: " + chatNum);
				System.out.println("Received chat name: " + chatName);
				System.out.println("Constructed inviteString: " + inviteString);
				System.out.println("Inviting " + inviteList + " people");
			}

			String sessionKey = "";
			String invitingUser = "";
			int x = 0;

			//For each user to invite, take their name, and take the session key encrypted with their public key
			for (x = 0; x < inviteList; x++) {
				invitingUser = decryptServerPrivate(serverDin.readUTF());
				sessionKey = serverDin.readUTF();
				if (debug == 1) {
					System.out.println("Inviting user: " + invitingUser);
				}
				sendMessage(invitingUser, "ChatInvite", encryptServerPrivate(inviteString));
				sendMessage(invitingUser, "SessionKey", sessionKey);
			}

			if (debug >= 1) {
				System.out.println("Invited " + (x + 1) + " people");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove the user from a specified chat, and delete the chat if it is empty
	 */
	private void leaveChat() {
		try {
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());

			//Get the chatUID
			int chatNum = Integer.parseInt(decryptServerPrivate(serverDin.readUTF()));

			if (debug >= 1) {
				System.out.println("User " + username + " is leaving chat number " + chatNum + "!!!!");
			}

			//Grab a connection to the database
			Connection con = server.dbConnect();
			Statement stmt;
			ResultSet rs = null;

			//Delete the user from the chat table, since he can't get messages for it anymore. Left for some reason
			stmt = con.createStatement();
			int deleted = stmt.executeUpdate("DELETE FROM chat WHERE username='" + username + "' AND chatid='" + chatNum + "';");
			if (deleted == 1 && debug >= 1) {
				System.out.println(username + " was successfully removed from the chat with UID " + chatNum);
			}

			//Get the users left in the chat
			rs = stmt.executeQuery("SELECT username FROM chat WHERE chatid = '" + chatNum + "';");

			//Delete the whole chat if it is empty
			if (rs.next()) {
				if (debug >= 1) {
					System.out.println("Still people in the chat. Not closing.");
				}
			} else {
				if (debug >= 1) {
					System.out.println("No one is left in chat " + chatNum + " closing chat");
				}

				stmt.executeUpdate("DELETE FROM allchats WHERE chatid='" + chatNum + "';");
			}

			//Close the connections
			rs.close();
			stmt.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Join the user to a specific chat if they accept the invitation
	 */
	private void joinChat() {
		try {
			//Get chat UID to join
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());
			int chatNum = Integer.parseInt(decryptServerPrivate(serverDin.readUTF()));

			if (debug >= 1) {
				System.out.println("Joining user " + username + " to chat number " + chatNum + "!!!!!");
			}

			//Grab a connection to the database
			Connection con = server.dbConnect();
			Statement stmt = con.createStatement();

			//Add user to chat in Database so they will get messages
			stmt.executeUpdate("INSERT into chat (username,chatid) values('" + username + "','" + chatNum + "')");
			if (debug >= 1) {
				System.out.println("Inserted the chatid into the allchat table.");
			}

			//Close the connections
			stmt.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pass off the socket information for a username. Useful for direct-protect and file transfer
	 */
	private String requestSocketInfo(String user) {
		try {
			String userForSocket = decryptServerPrivate(serverDin.readUTF());
			Socket foundSocket = (Socket) server.userToClientSocket.get(userForSocket);
			return foundSocket.getInetAddress().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create a chat. Generates a UID, adds it to the DB, and informs the creator
	 */
	private void createChat() {
		try {
			if (debug >= 1) {
				System.out.println("In the method.");
			}

			//Grab output stream for the user.
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());

			if (debug >= 1) {
				System.out.println("Created the output stream which we shouldn't have to do.");
			}

			//Grab a connection to the database
			Connection con = server.dbConnect();

			if (debug >= 1) {
				System.out.println("Connected to the database.");
			}

			//Get the chat name from the user
			String chatName = decryptServerPrivate(serverDin.readUTF());

			if (debug >= 1) {
				System.out.println("Took in the desired chat name.");
			}

			//Is the chatID a dupe?
			int dupe = 0;
			Statement stmt;
			ResultSet rs = null;
			int randInt = 0;

			//Generate a unique, random number for the chat
			do {
				//Generate a random number for the chat
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				byte seed[] = random.generateSeed(20);
				random.setSeed(seed);
				randInt = random.nextInt(9999);

				if (debug >= 1) {
					System.out.println("Generated random chat ID: " + randInt);
				}

				//Get a list of existing chats
				stmt = con.createStatement();
				rs = stmt.executeQuery("SELECT chatid FROM allchats");

				if (debug >= 1) {
					System.out.println("Got list of existing chats.");
				}

				//Read chatIDs into array
				while (rs.next()) {
					if (rs.getInt("chatid") == randInt) {
						dupe = 1;
					} else {
						dupe = 0;
					}
				}
			} while (dupe == 1);

			//Close the statement and result set
			rs.close();

			//The chatID is not a duplicate. We can create the chat and add it to the DB
			if (debug >= 1) {
				System.out.println("Generated number is not a duplicate.");
			}

			stmt.executeUpdate("INSERT into allchats (chatid,chatname) values('" + randInt + "','" + chatName.replace('\'', '\\') + "')");

			if (debug >= 1) {
				System.out.println("Inserted the chatid into the allchat table.");
			}

			//Now insert the username and the chat id into the chat table
			stmt.executeUpdate("INSERT into chat (username, chatid) values('" + username + "','" + randInt + "')");

			if (debug >= 1) {
				System.out.println("Inserted the username and chatid into the chat table.");
			}

			//Close the DB connections
			stmt.close();
			con.close();

			//Send back the chatUID
			serverDout.writeUTF(encryptServerPrivate(String.valueOf(randInt)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Allow the user to reset the password, provided they answer their secret question correctly
	 */
	private void resetPassword() {
		try {
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());

			//Use dbConnect() to connect to the database
			Connection con = server.dbConnect();

			//Take in the username to find the secret question and answer for
			String userToReset = decryptServerPrivate(serverDin.readUTF());

			String secretQuestion = null;
			String secretAnswer = null;

			//Create a statement and resultset for the query
			Statement stmt;
			Statement insertSTMT;
			ResultSet rs = null;

			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM Users WHERE username = '" + userToReset + "'");

			//Get their secret question and answer hash
			if (rs.next()) {
				secretQuestion = rs.getString("secretq");
				secretAnswer = rs.getString("secreta");
			} else {
				secretQuestion = "Invalid Username, try again.";
				return;
			}

			//Close the resultset
			rs.close();

			//Send the secret question to the client for answering
			serverDout.writeUTF(encryptServerPrivate(secretQuestion));
			if (debug >= 1) {
				System.out.println("SENT Question: " + secretQuestion);
			}

			//Read in the user's answer hash
			String secretAnswerHash = decryptServerPrivate(serverDin.readUTF());
			if (debug >= 1) {
				System.out.println("READ Answer: " + secretAnswerHash);
			}

			//Read in the user's desired passwordchange
			String newPassword = decryptServerPrivate(serverDin.readUTF());

			if (secretAnswerHash.equals(secretAnswer)) {
				System.out.println("HASHES MATCH");

				String insertString = "UPDATE Users SET password='" + newPassword + "' WHERE username = '" + userToReset + "'";
				insertSTMT = con.createStatement();
				insertSTMT.executeUpdate(insertString);

				if (debug >= 1) {
					System.out.println("PASSWORDCHANGED");
				}

				//Close Connections
				insertSTMT.close();

				//Success message
				serverDout.writeUTF(encryptServerPrivate("1"));
			} else {
				if (debug >= 1) {
					System.out.println("HASH MISMATCH. ABORT");
				}

				//Failure message
				serverDout.writeUTF(encryptServerPrivate("0"));
			}

			//Close Connections
			stmt.close();
			con.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receive an automated bug report from the user
	 */
	private void receiveBugReport() {
		if (debug == 1) {
			System.out.println("Receiving bug report stacktrace");
			System.out.println("Receiving bug report comments");
		}

		String trace = "";
		String comments = "";
		try {
			trace = decryptServerPrivate(serverDin.readUTF());
			comments = decryptServerPrivate(serverDin.readUTF());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (debug >= 1) {
			System.out.println("Bug report received from " + username + ": " + comments);
			System.out.println("Stacktrace follows:" + trace);
		}
	}

	/**
	 * Receive a manual bug report/feature request from the user
	 * @param flag Overload differentiation
	 */
	private void receiveBugReport(boolean flag) {
		if (debug == 1) {
			System.out.println("Receiving bug report/feature request");
		}

		String title = "";
		String recreate = "";
		String expected = "";
		String actual = "";
		try {
			title = decryptServerPrivate(serverDin.readUTF());
			recreate = decryptServerPrivate(serverDin.readUTF());
			expected = decryptServerPrivate(serverDin.readUTF());
			actual = decryptServerPrivate(serverDin.readUTF());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (debug >= 1) {
			System.out.println("BEGIN BUG REPORT");
			System.out.println("Bug report received from " + username + ": ");
			System.out.println("Brief Description: " + title);
			System.out.println("Steps to recreate: " + recreate);
			System.out.println("Expected Outcome: " + expected);
			System.out.println("Actual Outcome: " + actual);
			System.out.println("END OF REPORT");
		}
	}

	/**
	 * If the user needs a copy of their buddylist, send it to them
	 * @return success
	 * @throws IOException
	 */
	private boolean sendBuddyListToClient() throws IOException {

		String[] buddyListArray = returnBuddyListArray(true);
		if (debug >= 1) {
			System.out.println("Inside sendBuddyListToClient");
		}

		int numLines = buddyListArray.length;
		if (debug >= 1) {
			System.out.println("numLines: " + numLines);
		}

		//Send Athena the number of lines we're sending
		serverDout.writeUTF(encryptServerPrivate(String.valueOf(numLines)));

		//Send the lines of the file!
		for (int x = 0; x < buddyListArray.length; x++) {
			serverDout.writeUTF(encryptServerPrivate(buddyListArray[x]));
		}
		return true;

	}

	/**
	 * This method returns a nice string array full of the usernames (for now) that are in the buddylist file
	 * @param flag Overload differentiator
	 * @return String array of the buddylist pieces
	 * @throws IOException
	 */
	public String[] returnBuddyListArray(boolean flag) throws IOException {
		int count;
		int readChars;
		InputStream is;

		//Let's get the number of lines in the file
		File newFile = new File("buddylists/" + username + "/buddylist.csv");
		if (!(newFile.exists())) {
			boolean success = new File("buddylists/" + username).mkdirs();
			if (success) {
				newFile.createNewFile();
				is = new BufferedInputStream(new FileInputStream("buddylists/" + username + "/buddylist.csv"));
			} else {
				newFile.createNewFile();
			}
		}

		is = new BufferedInputStream(new FileInputStream("buddylists/" + username + "/buddylist.csv"));
		byte[] c = new byte[1024];
		count = 0;
		readChars = 0;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {
				if (c[i] == '\n') {
					++count;
				}
			}
		}

		//Make the string array the size of the number of lines in the file
		String[] usernames = new String[count];

		//If there are no lines in the file we know that the user has no buddies! :(
		if (count == 0) {
			return usernames;
		} else {
			File newFile2 = new File("buddylists/" + username + "/buddylist.csv");
			if (!(newFile2.exists())) {
				newFile2.createNewFile();
			}
			BufferedReader in = new BufferedReader(new FileReader("buddylists/" + username + "/buddylist.csv"));
			int x = 0;
			String raw;
			//Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
			while ((raw = in.readLine()) != null) {
				String foo[] = raw.split(",");
				usernames[x] = foo[0];
				x++;
			}
			return usernames;
		}
	}

	/**
	 * Sends the user a copy of his private key
	 * @throws IOException
	 */
	private void sendPrivateKeyToClient() throws IOException {
		RSAPrivateKeySpec privateKey = RSACrypto.readPrivKeyFromFile("keys/" + username + ".priv");
		//Send over ack message
		sendSystemMessage(username, "Incoming private key components");

		//Send over components
		String privateKeyMod = privateKey.getModulus().toString();
		if (debug == 2) {
			System.out.println("PRIVATE KEY MOD: " + privateKeyMod);
		}
		String privateKeyExp = privateKey.getPrivateExponent().toString();
		if (debug == 2) {
			System.out.println("PRIVATE KEY MOD: " + privateKeyExp);
		}
		//Send half a time plz!
		if (debug == 2) {
			System.out.println("Length of the private key: " + privateKeyMod.length());
		}
		//Send how many chunks will be coming		
		if (privateKeyMod.length() > 245) {
			double messageNumbers = (double) privateKeyMod.length() / 245;
			int messageNumbersInt = (int) Math.ceil(messageNumbers);
			serverDout.writeUTF(String.valueOf(messageNumbersInt));

			if (debug >= 1) {
				System.out.println("MessageLength: " + privateKeyMod.length() + "\nMessageLength/245: " + messageNumbers + "\nCeiling of that: " + messageNumbersInt);
			}
			//TODO THIS IS A MESSSSS
			String[] messageChunks = new String[(int) messageNumbersInt];
			for (int i = 0; i < messageChunks.length; i++) {
				int begin = i * 245;
				int end = begin + 245;
				if (end > privateKeyMod.length()) {
					end = privateKeyMod.length();
				}
				messageChunks[i] = privateKeyMod.substring(begin, end);
				serverDout.writeUTF(encryptServerPrivate(messageChunks[i]));
			}
		}

		if (privateKeyExp.length() > 245) {
			double messageNumbers = (double) privateKeyExp.length() / 245;
			int messageNumbersInt = (int) Math.ceil(messageNumbers);
			serverDout.writeUTF(String.valueOf(messageNumbersInt));

			if (debug >= 1) {
				System.out.println("MessageLength: " + privateKeyMod.length() + "\nMessageLength/245: " + messageNumbers + "\nCeiling of that: " + messageNumbersInt);
			}

			String[] messageChunks = new String[(int) messageNumbersInt];
			for (int i = 0; i < messageChunks.length; i++) {
				int begin = i * 245;
				int end = begin + 245;
				if (end > privateKeyExp.length()) {
					end = privateKeyExp.length();
				}
				messageChunks[i] = privateKeyExp.substring(begin, end);
				serverDout.writeUTF(encryptServerPrivate(messageChunks[i]));
			}
		}
	}

	/**
	 * Takes in user's buddylist and writes it to a file as backup
	 * @param buddyList
	 * @param buddyListName
	 */
	public void writeBuddyListToFile(String[] buddyList, String buddyListName) {
		BufferedWriter out;
		File newFile = new File("buddylists/" + buddyListName + "/buddylist.csv");
		try {
			if (!(newFile.exists())) {
				boolean success = new File("users/" + buddyListName).mkdirs();
				if (success) {
					newFile.createNewFile();
				} else {
					newFile.createNewFile();
				}
			} else {
				newFile.delete();
				newFile.createNewFile();
			}
			out = new BufferedWriter(new FileWriter("buddylists/" + buddyListName + "/buddylist.csv"));

			for (int i = 0; i < buddyList.length; i++) {
				out.write(buddyList[i] + "\n");
			}
			out.close();
		} catch (Exception e) {
			if (debug == 1) {
				System.out.println("ERROR WRITING BUDDYLIST");
			}
		}
	}

	/**
	 * Takes in buddylist from the user
	 * @throws IOException
	 */
	private void recieveBuddyListFromClient() throws IOException {
		String[] buddyListLines;

		System.out.println("Should be begin: " + decryptServerPrivate(serverDin.readUTF()));
		buddyListLines = new String[(Integer.parseInt(decryptServerPrivate(serverDin.readUTF())))];
		System.out.println("Buddylist lines: " + buddyListLines.length);
		for (int y = 0; y < buddyListLines.length; y++) {
			buddyListLines[y] = decryptServerPrivate(serverDin.readUTF());
			System.out.println("Encrypted buddylist lines " + buddyListLines[y]);
		}
		writeBuddyListToFile(buddyListLines, username);
		System.out.println("Successfully wrote buddy list to file");
	}

	/**
	 * Decrypt a message using the server's private key
	 * @param ciphertext The encrypted message
	 * @return The decrypted message
	 */
	public static String decryptServerPrivate(String ciphertext) {
		//Turn the String into a BigInteger. Get the bytes of the BigInteger for a byte[]
		byte[] cipherBytes = (new BigInteger(ciphertext)).toByteArray();
		//Decrypt the byte[], returns a String
		return RSACrypto.rsaDecryptPrivate(cipherBytes, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent());
	}

	/**
	 * Encrypt a message using the server's private key
	 * @param plaintext The plaintext message
	 * @return The encrypted message
	 */
	public String encryptServerPrivate(String plaintext) {
		//Encrypt the string and return it
		if (debug == 1) {
			System.out.println("Plaintext in encryptServerPrivate: " + plaintext);
		}
		BigInteger plaintextBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate(plaintext, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
		return plaintextBigInt.toString();
	}

	/**
	 * Figure out if a user is online or not
	 */
	public void negotiateClientStatus() {
		try {
			//Listen for the username
			String findUserCipher = serverDin.readUTF();
			if (debug >= 1) {
				System.out.println("FINDUSERCIPHER!@$!#@" + findUserCipher);
			}
			byte[] findUserBytes = (new BigInteger(findUserCipher)).toByteArray();
			String findUserDecrypted = RSACrypto.rsaDecryptPrivate(findUserBytes, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent());
			//Print out the received username
			if (debug >= 1) {
				System.out.println("Username received: " + findUserDecrypted);
			}
			//Check to see if the username is in the current Hashtable, return result
			if ((server.userToServerSocket.containsKey(findUserDecrypted))) {
				serverDout.writeUTF(encryptServerPrivate("1"));
				System.out.println("(Online)\n");
			} else {
				serverDout.writeUTF(encryptServerPrivate("0"));
				System.out.println("(Offline)\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check a user's online status
	 * @param checkUserFlag Overload differentiator
	 */
	public void negotiateClientStatus(String checkUserFlag) {
		try {
			if (debug >= 1) {
				System.out.println(username);
			}
			//Listen for the username
			String findUser = decryptServerPrivate(serverDin.readUTF());
			serverDout = new DataOutputStream(c2ssocket.getOutputStream());
			//Print out the received username
			if (debug >= 1) {
				System.out.println("Username received: " + findUser);
			}
			if (debug >= 1) {
				System.out.println("Socket serverDout: " + serverDout.toString());
			}
			//Check to see if the username is in the current Hashtable, return result
			if ((server.userToServerSocket.containsKey(findUser))) {

				serverDout.writeUTF(encryptServerPrivate("1"));
				if (debug >= 1) {
					System.out.println("(Online)\n");
				}
			} else {
				serverDout.writeUTF(encryptServerPrivate("0"));
				if (debug >= 1) {
					System.out.println("(Offline)\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check the hash of the backed-up buddylist for comparison
	 */
	public void returnBuddyListHash() {
		try {
			String buddyListToFind = serverDin.readUTF();
			byte[] buddyListBytes = (new BigInteger(buddyListToFind)).toByteArray();
			String buddyListDecrypted = RSACrypto.rsaDecryptPrivate(buddyListBytes, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent());

			//Grab the hash of the buddy list
			File buddylist = new File("buddylists/" + buddyListDecrypted + "/buddylist.csv");
			if (!(buddylist.exists())) {
				boolean success = new File("buddylists/" + buddyListDecrypted + "/").mkdirs();
				if (success) {
					buddylist.createNewFile();
				} else {
					buddylist.createNewFile();
				}
			}
			String path = "buddylists/".concat(buddyListDecrypted).concat("/buddylist.csv");

			if (debug >= 1) {
				System.out.println("PATH: " + path);
			}

			String hashOfBuddyList = FileHash.getMD5Checksum(path);
			String lastModDateOfBuddyList = String.valueOf(buddylist.lastModified());

			//Send information
			serverDout.writeUTF(encryptServerPrivate(hashOfBuddyList));
			serverDout.writeUTF(encryptServerPrivate(lastModDateOfBuddyList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new user
	 * @return success
	 * @throws IOException
	 */
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
			if (debug >= 1) {
				System.out.println("New User Decrypted Information:");
				System.out.println("First Name: " + firstName);
				System.out.println("Last Name: " + lastName);
				System.out.println("Email Address: " + emailAddress);
				System.out.println("User Name: " + newUser);
				System.out.println("Password Hash: " + newPassword);
				System.out.println("Secret Question: " + secretQuestion);
				System.out.println("Secret Answer Hash: " + secretAnswer);
			}

			stmt = con.createStatement();
			if (debug == 1) {
				System.out.println("Statement created\nCreating username: " + newUser + "\nPassword: " + newPassword);
			}

			//See if the username already exists.
			rs = stmt.executeQuery("SELECT * FROM Users WHERE username = '" + newUser + "'");
			if (debug == 1) {
				System.out.println("newUser: " + newUser);
			}

			//Test to see if there are any results
			if (rs.next()) {
				//Send status message that the username has already been taken.
				BigInteger failedRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Username has already been taken, please try again.",
						server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
				serverDout.writeUTF(failedRegistrationResultBigInt.toString());
				rs.close();
				return false;
			} else {
				//Store the new user's public key on to the filesystem
				File newFile = new File("keys/" + newUser + ".pub");
				if (!(newFile.exists())) {
					newFile.createNewFile();
				} else {
					return false;
				}
				RSACrypto.saveToFile("keys/" + newUser + ".pub", publicMod, publicExp);

				//Grab the users new password
				String insertString = "insert into Users (FirstName, LastName, EmailAddress, username, password, secretq, secreta) values('" + firstName + "', '"
						+ lastName + "', '" + emailAddress + "', '" + newUser + "', '" + newPassword + "', '" + secretQuestion.replace('\'', '\\') + "', '"
						+ secretAnswer + "')";
				insertSTMT = con.createStatement();
				insertSTMT.executeUpdate(insertString);


				//Inform of our success
				BigInteger successfulRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Account has been successfully created.",
						server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
				serverDout.writeUTF(successfulRegistrationResultBigInt.toString());

				//Close Connections
				stmt.close();
				insertSTMT.close();
				con.close();
				rs.close();
				return true;
			}

		} catch (Exception e) {
			//Inform of our failure
			BigInteger exceptionRegistrationResultBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate("Something went wrong, please inform the Athena Administrators.",
					server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(exceptionRegistrationResultBigInt.toString());
			return false;
		}
	}

	/**
	 * Sends a message from the user to another user
	 * @param toUser
	 * @param fromUser
	 * @param message
	 */
	void sendMessage(String toUser, String fromUser, String message) {
		Socket foundSocket = null;
		DataOutputStream clientDout = null;

		//Debug statement: who is this going to?
		if (debug == 1) {
			System.out.print(toUser);
		}

		//Look up the socket associated with the with whom we want to talk
		//We will use this to find which outputstream to send out
		//If we cannot find the user or socket, send back an error
		if ((server.userToClientSocket.containsKey(toUser))) {
			if (debug == 1) {
				System.out.print("Found user.. Continuing...");
			}
			foundSocket = (Socket) server.userToClientSocket.get(toUser);
			try {
				clientDout = new DataOutputStream(foundSocket.getOutputStream());
			} catch (IOException ex) {
				Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (debug == 1) {
				System.out.print("Found Socket: " + foundSocket);
			}
		} else {
			sendMessage(fromUser, "UnavailableUser", encryptServerPrivate(toUser));
			return;
		}

		//Send the message, and the user it is from
		try {
			//Encrypt the fromUser with the public key of userB (we want everything to be anonymous,
			//so we can't encrypt the fromUser with the private key of the server, anyone can decrypt that)
			//TODO WTF were we thinking here?
			//	   We are doing exactly what we said we shouldn't. FFS.
			BigInteger fromUserCipherBigInt = new BigInteger(RSACrypto.rsaEncryptPrivate(fromUser,
					server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			clientDout.writeUTF(fromUserCipherBigInt.toString());
			clientDout.writeUTF(message);
		} catch (IOException ie) {
			System.out.println(ie);
		}
		if (debug >= 1) {
			System.out.println("message sent, i think");
		}
	}

	/**
	 * Sends a system message to the selected user
	 * @param toUser
	 * @param message
	 */
	void sendSystemMessage(String toUser, String message) {
		Socket foundSocket = null;
		DataOutputStream serverDout = null;

		//Debug statement: who is this going to?
		if (debug == 1) {
			System.out.println("Who is this message going to? " + toUser);
		}

		//Look up the socket associated with the with whom we want to talk
		//We will use this to find which outputstream to send out
		//If we cannot find the user or socket, send back an error
		if ((server.userToServerSocket.containsKey(toUser))) {
			if (debug == 1) {
				System.out.println("Found user.. Continuing...");
			}
			foundSocket = (Socket) server.userToServerSocket.get(toUser);
			try {
				serverDout = new DataOutputStream(foundSocket.getOutputStream());
			} catch (IOException ex) {
				Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (debug == 1) {
				System.out.println("Found Socket: " + foundSocket);
			}
		}

		//Send the message, and the user it is from
		try {
			System.out.println("TOUSER: " + toUser + "\nMESSAGE: " + message);
			BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(message, server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(messageCipher.toString());
			if (debug >= 1) {
				System.out.println("Message sent:\n " + message);
			}
		} catch (IOException ie) {
			System.out.println(ie);
		}
	}

	/**
	 * Authenticates the user so they can being to send and receive messages
	 * @param clientName username
	 * @param clientPassword password hash
	 * @return success
	 * @throws IOException
	 */
	public String login(String clientName, String clientPassword) throws IOException {
		serverDout = new DataOutputStream(c2ssocket.getOutputStream());
		String localHashed = "";
		try {
			//Get the password from the database
			//Grab a DB connection
			Connection con = server.dbConnect();
			Statement stmt;
			ResultSet rs = null;

			//Get a list of existing chats
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT password FROM Users WHERE username = '" + clientName + "';");

			//Send the message to the users in the chat
			if (rs.next()) {
				localHashed = rs.getString(1);
			}

			//Close all DB connections
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			//Login fail handler
			if (debug >= 1) {
				System.out.println("User has failed to login");
			}
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Failed", server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(returnCipher.toString());
			server.removeConnection(c2ssocket, c2csocket, clientName);
			return returnCipher.toString();
		}

		//Debug messages.
		if (debug == 1) {
			System.out.println("User logging in...");
		}
		if (debug == 1) {
			System.out.println("Hashed Password:" + localHashed);
		}
		if (debug == 1) {
			System.out.println("Username :" + clientName);
		}

		//Verify the password hash provided from the user matches the one in the server's hashtable
		if (clientPassword.equals(localHashed)) {
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Success", server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(returnCipher.toString());
			return returnCipher.toString();
		} else {
			//Login fail handler
			BigInteger returnCipher = new BigInteger(RSACrypto.rsaEncryptPrivate("Failed", server.serverPriv.getModulus(), server.serverPriv.getPrivateExponent()));
			serverDout.writeUTF(returnCipher.toString());
			server.removeConnection(c2ssocket, c2csocket, clientName);
			return returnCipher.toString();
		}
	}

	/**
	 * Computer the SHA-1 hash of a string
	 * @param toHash The data to hash
	 * @return The hash
	 * @throws Exception
	 */
	public String computeHash(String toHash) throws Exception {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1"); //step 2
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e.getMessage());
		}
		try {
			md.update(toHash.getBytes("UTF-8")); //step 3
		} catch (UnsupportedEncodingException e) {
			throw new Exception(e.getMessage());
		}

		byte raw[] = md.digest(); //step 4
		String hash = (new BASE64Encoder()).encode(raw); //step 5
		return hash; //step 6
	}

	/**
	 * Sends the public key of a requested user to the user
	 * @throws IOException
	 */
	public void publicKeyRequest() throws IOException {

		if (debug >= 1) {
			System.out.println(username);
		}

		try {
			//Listen for the username
			String findUser = decryptServerPrivate(serverDin.readUTF());

			//Print out the received username
			if (debug >= 1) {
				System.out.println("Username received PUBLIC KEY REQUEST: " + findUser);
			}

			File newFile = new File("keys/" + findUser + ".pub");
			if ((newFile.exists())) {
				RSAPublicKeySpec keyToReturn = RSACrypto.readPubKeyFromFile("keys/" + findUser + ".pub");
				if (debug >= 1) {
					System.out.println("MOD: " + keyToReturn.getModulus().toString());
					System.out.println("EXP: " + keyToReturn.getPublicExponent().toString());
				}

				//Check to see if the user has a key file on the server
				serverDout.writeUTF(keyToReturn.getModulus().toString());
				serverDout.writeUTF(keyToReturn.getPublicExponent().toString());

				if (debug >= 1) {
					System.out.println("Modulus Returned\n");
					System.out.println("Exponent Returned\n");
				}

			} else {
				serverDout.writeUTF("-1");
				if (debug >= 1) {
					System.out.println("User does not have a keyfile with us");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
