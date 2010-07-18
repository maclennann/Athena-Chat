/* Athena/Aegis Encrypted Chat Platform
 * Server.java: Accepts connections, governs user threads (ServerThread instances) and is the gateway to the DB
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

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

/**
 * The main server component. Accepts/manages connections and threads
 * @author OlympuSoft
 */
public class Server {

	//Change to 1 or 2 for debug output
	private int debug = 0;
	//This socket will accept new connection
	private ServerSocket c2ss;
	private ServerSocket c2css;
	private static File debugLog;
	private static BufferedWriter debugWriter;
	/**
	 * Holds the usernames and hashed passwords read in from the database.
	 */
	//Creates a SQL connection object. See dbConnect()
	private static Connection con = null;
	private static String dbUser = "";
	private static String dbPass = "";
	//Defines which port on which we listen for client
	private static int listenPort = 7777;
	private static Scanner in = new Scanner(System.in);
	/**
	 * A hashtable that maps users to their server socket
	 */
	public Hashtable<String, Socket> userToServerSocket = new Hashtable<String, Socket>();
	/**
	 * A hashtable that maps users to their client socket
	 */
	public Hashtable<String, Socket> userToClientSocket = new Hashtable<String, Socket>();
	/**
	 * Server's private RSA key
	 */
	public RSAPrivateKeySpec serverPriv;
	/**
	 * Server's public key
	 */
	public RSAPublicKeySpec serverPub;

	/**
	 * Starts the server listening
	 * @param port The port to listen on
	 * @throws IOException
	 */
	public Server(int port) throws IOException {
		listen(port);
	}

	/**
	 * Gets a connection to the database
	 * @return The connection to the database
	 */
	public static Connection dbConnect() {

		//Location of the database
		String url = "jdbc:mysql://localhost:3306/aegis";

		//Database username and password. shhhhh.
		String un = dbUser; //Database Username
		String pw = dbPass; //Database Password

		//Load the JDBC driver. Make sure the mysql jar is in your classpath!
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException ex) {
		}

		//Connect to the database using the driver
		try {
			con = DriverManager.getConnection(url, un, pw);

			//Return the established connection
			return con;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Adds a user and server socket to the hashtable for later reference
	 * @param username The username
	 * @param userSocket The socket to map the username to
	 */
	public void mapUserServerSocket(String username, Socket userSocket) {
		userToServerSocket.put(username, userSocket);
	}

	/**
	 * Maps a user and a client socket in the hashtable
	 * @param username The username
	 * @param userSocket The socket to map the username to
	 */
	public void mapUserClientSocket(String username, Socket userSocket) {
		userToClientSocket.put(username, userSocket);
	}

	/**
	 * The server listens for client connections, threads them, and loops. Forever.
	 * @param port Port to listen on
	 * @throws IOException
	 */
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	private void listen(int port) throws IOException {
		// Create the ServerSocket
		c2ss = new ServerSocket(port);
		c2css = new ServerSocket(7778);

		//Fetch public and private keys so the threads can deal with encryption
		serverPub = RSACrypto.readPubKeyFromFile("keys/Aegis.pub");
		serverPriv = RSACrypto.readPrivKeyFromFile("keys/Aegis.priv");

		// Tell the world we're ready to go
		System.out.println("*******************************************");
		System.out.println("**     Welcome to Athena Chat Server     **");
		System.out.println("**            Codename: Aegis            **");
		System.out.println("**                                       **");
		System.out.println("**     v1.0.1b                           **");
		System.out.println("**                                       **");
		System.out.println("**     Server accepting connections:     **");
		System.out.println("**     Port 7777   &&   7778             **");
		System.out.println("**                                       **");
		System.out.println("*******************************************");

		//Open the log file for writing
		openLog(new File("logs/Aegis.txt"));

		//Start listening for connections
		ListenerThread listener = new ListenerThread(c2ss,c2css,this);
		listener.start();

		//The Administration menu
		while(true){
			System.out.println("\n\nMenu:\nPlease Choose an Action -");
			System.out.println("\n1. Change Debug Level\n2. View Log\n3. View Users\n4. Exit\n");
			System.out.print("?> ");
			int answer=in.nextInt();
			if(answer==1){
				System.out.println("Choose new debug level (0,1,2):");
				System.out.print("?> ");
				debug = in.nextInt();
			}
			else if(answer == 2) {
				//Need a better way to do this
				Runtime.getRuntime().exec("tail ./logs/Aegis.txt");
			}
			else if(answer == 3) {
				//Prints out number of users connected, then usernames
				System.out.println(userToClientSocket.size() + " users connected:\n");
				Enumeration userEnumeration = userToClientSocket.keys();

				//Get the outputStream for each socket and send message
				for (Enumeration<?> e = userEnumeration; e.hasMoreElements();) {
					System.out.println((String)userEnumeration.nextElement());
				}
			}
			else if(answer == 4) {
				//Get a reason for the shutdown
				System.out.println("\nPlease provide a reason for this shutdown:");
				System.out.print("?> ");
				in.nextLine();
				String message = in.nextLine();
				//Inform users of the shutdown, and reason
				sendToAll("ServerShutDown",message,null);
				try{
					//Wait 30 seconds before shutdown
					Thread.sleep(30000);
				}catch(Exception e){
					System.out.println("Could not sleep. Shutting down immediately!");
				}
				System.out.println("Shutting down...");
				System.exit(0);
			}
		}
	}

	public static void openLog(File fileName) {
		try{
			debugLog = fileName;
			if (!(debugLog.exists())) {
				boolean success = new File("logs").mkdirs();
				if (success) {
					debugLog.createNewFile();
				} else {
					debugLog.createNewFile();
				}
			}

			debugWriter = new BufferedWriter(new FileWriter(debugLog));
		} catch(Exception e) {
			System.out.println("Unable to open log file");
		}
	}

	public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

	public static void writeLog(String debugText) {
		try{
			debugWriter.write(getDateTime() + ": "+debugText+"\r\n");
			debugWriter.flush();
		} catch(Exception e){
			//e.printStackTrace();
			System.out.println("Unable to write to log file. Make sure the file is open for writing.");
		}
	}

	public static void closeLog() throws IOException {
		debugWriter.close();
	}

	/**
	 * Sends a message to every connected user
	 * @param eventCode What we are talking to them about
	 * @param message The data
	 */
	synchronized void sendToAll(String eventCode, String message,String[] blockList) {
		System.gc();
		BigInteger eventCodeCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(eventCode, serverPriv.getModulus(), serverPriv.getPrivateExponent()));
		BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPrivate(message, serverPriv.getModulus(), serverPriv.getPrivateExponent()));
		Enumeration userEnumeration = userToClientSocket.elements();
		Enumeration usernameEnumeration = userToClientSocket.keys();

		int send=1;
		//Get the outputStream for each socket and send message
		for (Enumeration<?> e = userEnumeration; e.hasMoreElements();) {
			Socket sendToAllSocket = (Socket) e.nextElement();
			String userToCheck = (String)usernameEnumeration.nextElement();
			writeLog("UserToCheck: "+userToCheck);
			if(blockList != null && blockList.length!=0){
				writeLog("Blocklist exists");
			for(int i=0;i<blockList.length;i++) {
				writeLog("Checking: "+blockList[i] + " against " + userToCheck);
				if(blockList[i].equals(userToCheck)) send=0;
			}}
			if(send==1){
			try {
				DataOutputStream dout = new DataOutputStream(sendToAllSocket.getOutputStream());
				dout.writeUTF(eventCodeCipher.toString());
				dout.writeUTF(messageCipher.toString());
			} catch (IOException ie) {
				System.out.println(ie);
			}}send=1;
		}
	}

	/**
	 * Remove a socket (user has failed to login)
	 * @param servsock The "server" socket to remove
	 * @param clientsock The "client" socket to remove
	 */
	void removeConnection(Socket servsock, Socket clientsock) {
		// Debug text
		writeLog("Connection Terminated:\n" + servsock + "\n\n");

		// Make sure it's closed
		try {
			servsock.close();
			clientsock.close();
		} catch (IOException ie) {
			writeLog("Error closing " + servsock);
			ie.printStackTrace();
		}
		System.gc();
	}

	/**
	 * Remove a socket/outputstream and user/socket relationship (i.e. user disconnects)
	 * @param servsock The "server" socket
	 * @param clientsock The "client" socket
	 * @param uname The username
	 */
	void removeConnection(Socket servsock, Socket clientsock, String uname) {
		// Debug text
		writeLog("User Disconnected: " + uname + "\n\n");

		// Remove thread's entries from hashtables
		userToServerSocket.remove(uname);
		userToClientSocket.remove(uname);

		// Make sure the socket is closed
		try {
			servsock.close();
			clientsock.close();

			//Sending User Log off message after we close the socket
			sendToAll("ServerLogOff", uname,null);
		} catch (IOException ie) {
			writeLog("Error closing " + servsock);
			ie.printStackTrace();
		}
		System.gc();
	}

	/**
	 * Read in the usernames from the DB and start listening
	 * @param args Database credentials
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		/*Upon Starting Server
		 *1. UpdateHashTable
		 *2. Listen for connections
		 *3. The Universe collapses in on itself*/
		dbUser = args[0];
		dbPass = args[1];

		// Create a Server object, which will automatically begin accepting connections.
		new Server(listenPort);

	}
}
