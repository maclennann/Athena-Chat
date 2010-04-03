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
 * File: Server.java
 * 
 * This runs the code for the auth/messaging server. A client will connect to this server via ServerSocket ss. Each connection is handled by a thread
 * of ServerThread. Server handles the record-keeping and connection handling.
 * 
 * When the server starts up, we create a hash table of all of the usernames and passwords for all users, minimizing database transactions.
 *
 * As new users connect, their username is mapped to a socket, which is mapped to a datastream so we can communicate with the user.
 *
 ****************************************************/
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

//Do we need JOptionPane?
import javax.swing.JOptionPane;

public class Server
{
	//TODO Add debug mode
	//Change to 1 for debug output
	private int debug = 0;
	
	//This socket will accept new connection
	private ServerSocket ss;
	
	//This will be a table holding the usernames and hashed passwords pulled from the database
	//See: updateHashTable()
	public static Hashtable authentication = new Hashtable();
	
	//Creates a SQL connection object. See dbConnect()
	private static Connection con = null;
	
	//Defines which port on which we listen for client
	private static int listenPort = 7777;

	//Will control listener thread when we do that
	private int isListening = 1;
	
	//A hashtable that keeps track of the outputStreams linked to each socket
	public Hashtable outputStreams = new Hashtable();

	//A hashtable mapping each user to a socket
	//used to find which stream to use to send data to clients
	public Hashtable userToSocket = new Hashtable();
	
	//Constructor. Starts listening on the defined port.
	public Server( int port ) throws IOException {
		listen( port );
	}
	
	//Connect to the database. Returns the connection it established, or null
	public static Connection dbConnect () { 

		//Location of the database
		//TODO: DB Server on LAN with auth server (maybe same computer) only accessable from auth server
		//		of course, we need a real auth server first.
		//TODO: Don't store DB location, username, or password in the source. Break it out into a conf file.
		String url = "jdbc:mysql://external-db.s72292.gridserver.com/db72292_athenaauth";
	
		//Database username and password. shhhhh.
		String un = "db72292_athena"; //Database Username
		String pw = "xZN?uhwx"; //Database Password
		
		//Load the JDBC driver. Make sure the mysql jar is in your classpath!
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException ex){}
	
		//Connect to the database using the driver
		try{ 		
			con = DriverManager.getConnection(url, un, pw);

			//Return the established connection
			return con;

		}catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	//Writes all usernames and password hashes from the database into a hash table
	public static void updateHashTable () { 
		try { 
			//Use dbConnect() to connect to the database
			Connection getHashed = dbConnect();
		
			//Create a statement and resultset for the query
			Statement stmt;
			ResultSet rs; 
			
			//Return all usernames and hashes passwords
			stmt = getHashed.createStatement(); 
			rs = stmt.executeQuery("SELECT * from Users"); //Here is where the query goes that we would like to run.
		
			//Iterate through the results and write them to a hashtables
			while(rs.next()) { 
				String username = rs.getString("username");
				String hashedPassword = rs.getString("password");
			
				//Write username and hashed password to next element of hash table
				if (authentication.containsKey(username)) { 
					//DO nothing
				} else { 
					authentication.put(username, hashedPassword);
				}
			}
		
		}catch(SQLException ex) {
			ex.printStackTrace();
		}
		
	}

	//Adds a user (and the socket he is on) to the hashtable for later reference
	//Called after user authenticates
	public void mapUserSocket(String username, Socket userSocket) { 
		userToSocket.put(username, userSocket);
	}
	
	//Server listens for client connections, and passes them off to their own thread
	private void listen( int port ) throws IOException {
		// Create the ServerSocket
		ss = new ServerSocket( port );

		// Tell the world we're ready to go
		//System.out.println( "Listening on "+ss );
		System.out.println("*******************************************");
		System.out.println("**     Welcome to Athena Chat Server     **");
		System.out.println("**            Codename: Aegis            **");
		System.out.println("**                                       **");
		System.out.println("**     v0.0.2a                           **");
		System.out.println("**                                       **");
		System.out.println("**     Server accepting connections:     **");
		System.out.println("**     Port 7777                         **");
		System.out.println("**                                       **");
		System.out.println("*******************************************");

		//Accept client connections forever
		while (true) {
			//Accept a new connection on the serversocket
			//Create a socket for it
			Socket s = ss.accept();

			//Debug text announcing a new connection
			System.out.println( "Connection Established:\n "+s+"\n\n" );

			//DataOuputStream to send data from the client's socket
			DataOutputStream dout = new DataOutputStream( s.getOutputStream() );
			
			//Map the outputstream to the socket for later reference
			outputStreams.put( s, dout );
			
			//Handle the rest of the connection in the new thread
			new ServerThread( this, s );
		}
	}
	
	
	// Get an enumeration of all the OutputStreams.
	Enumeration getOutputStreams() {
		return outputStreams.elements();
	}
	
	// Send a message to all clients (utility routine)
	void sendToAll(String eventCode, String message ) {
		//make sure the outputStreams hashtable is up-to-date
		synchronized( outputStreams ) {
			
			//Get the outputStream for each socket and send message
			for (Enumeration e = getOutputStreams(); e.hasMoreElements(); ) {
				DataOutputStream dout = (DataOutputStream)e.nextElement();
				try{
					dout.writeUTF(eventCode);
					dout.writeUTF( message );
				} catch( IOException ie ) { System.out.println( ie ); }
			}
		}
	}
	
	//Just remove a socket (i.e. the user has failed to login)
	void removeConnection( Socket s) {
		// Synchronize so we don't mess up sendToAll() while it walks
		// down the list of all output streams.
		synchronized( outputStreams ) {
			// Debug text
			System.out.println( "Connection Terminated:\n"+s+"\n\n" );

			// Remove socket from our hashtable/list
			outputStreams.remove( s );

			// Make sure it's closed
			try {
				s.close();
			} catch( IOException ie ) {
				System.out.println( "Error closing "+s );
				ie.printStackTrace();
			}
		}
	}

	//Remove a socket/outputstream and user/socket relationship (i.e. user disconnects)
	void removeConnection( Socket s, String username ) {
		// Synchronize so we don't mess up sendToAll() while it walks
		// down the list of all output streams.
		synchronized( outputStreams ) {
			// Debug text
			System.out.println( "User Disconnected: "+username+"\n\n" );

			// Remove thread's entries from hashtables
			outputStreams.remove( s );
			userToSocket.remove(username);
			
			// Make sure the socket is closed
			try {
				s.close();
				
				//Sending User Log off message after we close the socket
				sendToAll("ServerLogOff",username);
			} catch( IOException ie ) {
				System.out.println( "Error closing "+s );
				ie.printStackTrace();
			}
		}
	}
	
	//Server program starts.
	 public static void main( String args[] ) throws Exception {
		/*Upon Starting Server
		*1. UpdateHashTable
		*2. Listen for connections 
		*3. The Universe collapses in on itself*/
		
		//Read all usernames and hashed passwords into hashtable from database
		updateHashTable();
		
		 // Create a Server object, which will automatically begin accepting connections.
		new Server( listenPort );

	}
}
