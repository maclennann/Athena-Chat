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
 * This is where the detailed description of the file will go.
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
	private static Connection con = null;
	
	// The Server that spawned us
	private static Server server;
	// The Socket connected to our client
	public Socket socket;
	
	// Constructor.
	public ServerThread( Server server, Socket socket ) {
		// Save the parameters
		this.server = server;
		this.socket = socket;
		//Start up the thread
		start();
	}
	
	//This runs in a separate thread when start() is called in the
	//constructor.
	public void run() {
		try {
			//Create a DataInputStream for communication; the client
			//is using a DataOutputStream to write to us
			DataInputStream din = new DataInputStream( socket.getInputStream() );
			//Over and over, forever ...
			
			//Getting the Username and Password over the stream before the actual connection
			String username = din.readUTF(); // Get Username
			String password = din.readUTF(); // Get Password
			
			System.out.println("Username: " + username);
			System.out.println("Password: " + password);
			
			
			//Create the connection from 
			Connection con = server.dbConnect();
			System.out.print("Connection established..");
			
			//Login!
			System.out.println(login(username, password));
			
			//Maps username to socket after user logs in
			server.mapUserSocket(username, socket);	
			
			String toUser = din.readUTF();
			
			while (true) {
				//... read the next message ...
				String message = din.readUTF();
				//... tell the world ...
				System.out.println( "Sending "+message );
				//... and have the server send it to all clients
				sendMessage(toUser, username, message);
			}
			
		} catch( EOFException ie ) {
			//This doesn't need an error message
		} catch( IOException ie ) {
			//This does; tell the world!
			ie.printStackTrace();
		} finally {
			//The connection is closed for one reason or another,
			//so have the server dealing with it
			server.removeConnection( socket );
		}
	}
	
	//Deprecated
	public void emo () { 
		this.destroy();			
	}
	
	void sendMessage(String toUser, String fromUser, String message) {
		// We synchronize on this because another thread might be
		// calling removeConnection() and this would screw us up
		// as we tried to walk through the list

		// For each client ...
			Socket foundSocket = null;
			DataOutputStream dout = null;
			
			System.out.print(toUser);
			if ((server.userToSocket.containsKey(toUser))) { 
				System.out.print("Found user.. Continuing...");
				// ... get the output stream ...
				//System.out.print(foundSocket);
				foundSocket = (Socket) server.userToSocket.get(toUser);
				System.out.print("Found Socket: " + foundSocket);
			}
			
			if (server.outputStreams.containsKey(foundSocket)) { 
				// ... get the output stream ...
				//System.out.print(foundSocket);
				dout = (DataOutputStream) server.outputStreams.get(foundSocket);
			}
			
				// ... and send the message
				try {
					dout.writeUTF(fromUser);
					dout.writeUTF(message);
				} catch( IOException ie ) { System.out.println( ie ); }
	}

	
	public String login (String clientName, String clientPassword) { 
			System.out.print("We are in login.");

				System.out.print("HAIII");
				String hashedPassword = server.authentication.get(clientName).toString(); //Grabbing the HashedPassword from the Database
				//System.out.println(server.authentication.get(clientName)); //Grabbing the HashedPassword from the Database
				System.out.print("FHDHFSFHDSAAFS:" + hashedPassword);
				System.out.print("Name:" + clientName);
				
				//Here is where we find if the User's Inputed information is correct
				if (clientPassword.equals(hashedPassword)) { 
					//Run some command that let's user log in!
					String returnMessage = "You're logged in!!!!";
					return returnMessage;
				}else { 
					//Add Login Fail handler
					server.removeConnection(socket);
					return "Login Failed";  
					}	
		}				
}
