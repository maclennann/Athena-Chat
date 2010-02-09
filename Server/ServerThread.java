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

import javax.swing.JOptionPane;

//TODO: Do we really need this? It does nothing ATM.
import com.sun.org.apache.xpath.internal.FoundIndex;

public class ServerThread extends Thread
{
	//Change to 1 for debug output
	private int debug = 0;
	
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
			username = din.readUTF(); // Get Username
			password = din.readUTF(); // Get Password
			
			//Debug statements
			if (debug==1)System.out.println("Username: " + username);
			if (debug==1)System.out.println("Password: " + password);
			
			//Authenticate the user.
			String loginOutcome = login(username, password);
			if (debug==1)System.out.println(loginOutcome);
			
			//Maps username to socket after user logs in
			server.mapUserSocket(username, socket);	
			
			//Route around messages coming in from the client while they are connected
			//TODO: Special message to end connection/destroy socket? Maybe?
			while (true) {
				//Take in messages from this thread's client and route them to another client
				routeMessage(din);
			}
			
		} catch ( EOFException ie ) {
		} catch ( IOException ie ) {
			ie.printStackTrace();
		}finally {
			//Socket is closed, remove it from the list
			server.removeConnection( socket, username );
		}
	}
	
	//Method that handles client to server messages
	public void sendToAegis(int eventCode) {
		
		switch(eventCode) { 
		case 000: createUsername();
		break;
		default: return;
		}
	}
	
	//Takes in a recipient and message from this thread's user
	//and routes the message to the recipient.
	//TODO: Can this be merged into sendMessage?
	public void routeMessage(DataInputStream din){
		try {
			String toUser=din.readUTF();
			String message=din.readUTF();
			if (toUser.equals("Aegis")) { 
				System.out.println("AEGIS WING IS THE BEST GAME EVER");
				sendToAegis(Integer.parseInt(message));
				return;
			}else { 
				System.out.println(":( norm is mean");
				sendMessage(toUser, username, message);
			}
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
	//TODO Make this work. Enable (Somehow) communication between the client and the server
	public boolean createUsername() { 
		try { 
			//Use dbConnect() to connect to the database
			Connection con = server.dbConnect();
			
			//Create a statement and resultset for the query
			Statement stmt;
			Statement insertSTMT;
			ResultSet rs; 
			
			//Here will be the wxWidget code for the new menu (assumingly)
			//But for now just some JOption
			din.readUTF();
			String newUser = din.readUTF();
			din.readUTF();
			String newPassword = din.readUTF();
			
			
			//Let's check to see if this username is already in the database			
			//Return true if the username is already registered
			stmt = con.createStatement();
			//Here is where the query goes that we would like to run.
			rs = stmt.executeQuery("SELECT * FROM Users WHERE username = " + newUser); 
		
			//Test to see if there are any results
			if (rs.next()) { 
				dout.writeUTF("Username has already been taken");
				return false;
			}
			else { 
				//Grab the users new password
				String insertString = "insert into Users values('" + newUser + "', '" + newPassword + "'";
				insertSTMT = con.createStatement();
				insertSTMT.executeUpdate(insertString);
				
				//Close Connections
				stmt.close();
				insertSTMT.close();
				con.close();
				
				dout.writeUTF("User created succesfully.");
				return true;
			}
		}catch (SQLException se) { 
			System.out.print(se.toString());
			return false;
		}catch (IOException ie) { 
			System.out.println(ie.toString());
			return false;
		}
	}

	
	//Sends message message from user fromUser (this thread/socket) to user toUser (another socket)
	//TODO: Separate findOuputSteam from this method?
	void sendMessage(String toUser, String fromUser, String message) {
		Socket foundSocket = null;
			
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
	
		String hashedPassword = server.authentication.get(clientName).toString(); //Grabbing the HashedPassword from the Database
		//System.out.println(server.authentication.get(clientName)); //Grabbing the HashedPassword from the Database

		//Debug messages.
		//TODO: Come up with better debug messages
		if (debug==1)System.out.println("User logging in...");
		if (debug==1)System.out.println("Hashed Password:" + hashedPassword);
		if (debug==1)System.out.println("Username :" + clientName);
				
		//Verify the password hash provided from the user matches the one in the server's hashtable
		if (clientPassword.equals(hashedPassword)) { 
			//Run some command that lets user log in!
			//TODO: We need to broadcast a message letting everyone know a user logged in?
			String returnMessage = "You're logged in!!!!";
			return returnMessage;
		}else { 
			//Login fail handler
			server.removeConnection(socket, clientName);
			return "Login Failed";  
		}	
	}				
}
