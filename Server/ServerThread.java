/****************************************************
 * Athena: Encrypted Messaging Application v.0.0.1
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
 * Sender's thread handles sending to recipient's socket.
 *
 ****************************************************/
import java.io.*;
import java.sql.*;
import java.util.Enumeration;
import java.io.*;
import java.net.*;

import com.sun.org.apache.xpath.internal.FoundIndex;

public class ServerThread extends Thread
{
	//Define the MySQL connection
	// private Connection con = null;
	
	// The Server that created this thread
	private static Server server;
	
	//Change to 1 for debug output
	private int debug = 0;

	//Define Global Variable Username
	String username;
	
	//Our current socket
	public Socket socket;
	
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
			DataInputStream din = new DataInputStream( socket.getInputStream() );
			
			//Getting the Username and Password over the stream for authentication
			username = din.readUTF(); // Get Username
			String password = din.readUTF(); // Get Password
			
			//Debug statements
			System.out.println("Username: " + username);
			System.out.println("Password: " + password);
			
			
			//Connect to the database 
			//TODO: We don't actually need to do this anymore.
			//	We have a hashtable
			// Connection con = server.dbConnect();
			// System.out.print("Connection established..");
			
			//Authenticate the user. Output outcome
			System.out.println(login(username, password));
			
			//Maps username to socket after user logs in
			server.mapUserSocket(username, socket);	
			
			//Route around messages coming in from the client while they are connected
			//TODO: Special message to end connection/destroy socket? Maybe?
			while (true) {
				//Take in messages from this thread's client and route them to another client
				routeMessage(din);
			}
			
		} catch( EOFException ie ) {}
		catch( IOException ie ) {ie.printStackTrace();} 
		finally {
			//Socket is closed, remove it from the list
			server.removeConnection( socket, username );
		}
	}
	
	//Takes in a recipient and message from this thread's user
	//and routes the message to the recipient.
	//TODO: Can this be merged into sendMessage?
	public void routeMessage(DataInputStream din){
		try {
			String toUser=din.readUTF();
			String message=din.readUTF();
			sendMessage(toUser, username, message);
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
	//Sends message message from user fromUser (this thread/socket) to user toUser (another socket)
	//TODO: Separate findOuputSteam from this method?
	void sendMessage(String toUser, String fromUser, String message) {
		Socket foundSocket = null;
		DataOutputStream dout = null;
			
		//Debug statement: who is this going to?
		System.out.print(toUser);

		//Look up the socket associated with the with whom we want to talk
		//We will use this to find which outputstream to send out
		//If we cannot find the user or socket, send back an error
		if ((server.userToSocket.containsKey(toUser))) { 
			System.out.print("Found user.. Continuing...");
			foundSocket = (Socket) server.userToSocket.get(toUser);
			System.out.print("Found Socket: " + foundSocket);
		} else { sendMessage(fromUser, "UnavailableUser", toUser); return; } 
			
		//Find the outputstream associated with toUser's socket
		//We send data through this outputstream to send the message
		//If we cannot find the outputstream, send back an error
		if (server.outputStreams.containsKey(foundSocket)) { 
			dout = (DataOutputStream) server.outputStreams.get(foundSocket);
		} else { sendMessage(fromUser, "MissingSocket", toUser); return; }
			
		//Send the message, and the user it is from
		try {
			dout.writeUTF(fromUser);
			dout.writeUTF(message);
		} catch( IOException ie ) { System.out.println( ie ); }
	}

	//This will authenticate the user, before they are allowed to send messages.	
	public String login (String clientName, String clientPassword) { 

		//Debug messages.
		//TODO: Come up with better debug messages
		System.out.print("We are in login.");

		String hashedPassword = server.authentication.get(clientName).toString(); //Grabbing the HashedPassword from the Database
		//System.out.println(server.authentication.get(clientName)); //Grabbing the HashedPassword from the Database

		//Debug messages.
		//TODO: Come up with better debug messages
		System.out.print("FHDHFSFHDSAAFS:" + hashedPassword);
		System.out.print("Name:" + clientName);
				
		//Verify the password hash provided from the user matches the one in the server's hashtable
		if (clientPassword.equals(hashedPassword)) { 
			//Run some command that lets user log in!
			//TODO: We need to broadcast a message letting everyone know a user logged in?
			String returnMessage = "You're logged in!!!!";
			return returnMessage;
		}else { 
			//Add Login Fail handler
			server.removeConnection(socket, clientName);
			return "Login Failed";  
		}	
	}				
}
