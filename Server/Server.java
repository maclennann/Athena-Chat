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
 * File: Server.java
 * 
 * This is where the detailed description of the file will go.
 * 
 *
 ****************************************************/
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class Server
{
	// The ServerSocket we'll use for accepting new connections
	private ServerSocket ss;
	
	//Create Public HashTable for the Username/Hashed Passwords
	public static Hashtable authentication = new Hashtable();
	
	//Define the MySQL connection
	private static Connection con = null;
	
	//Define Server Listening port
	private static int listenPort = 7777;
	
	// A mapping from sockets to DataOutputStreams. This will
	// help us avoid having to create a DataOutputStream each time
	// we want to write to a stream.
	public Hashtable outputStreams = new Hashtable();
	public Hashtable userToSocket = new Hashtable();
	
	// Constructor and while-accept loop all in one.
	public Server( int port ) throws IOException {
		// All we have to do is listen
		listen( port );
	}
	
	public static Connection dbConnect () { 
		//Here we will have to have some mysql code to verify with our Database that their username is correct.
		//JDBC URL for the database
		String url = "jdbc:mysql://external-db.s72292.gridserver.com/db72292_athenaauth";
	
		//Using the forName method to load the appropriate driver for JDBC
	
		String un = "db72292_athena"; //Database Username
		String pw = "12345678"; //Database Password
		
		//Try to set the Driver for the JDBC
		try{
			Class.forName("com.mysql.jdbc.Driver");
			}catch(ClassNotFoundException ex){}
	
		//Here is where the connection is made
		try{ 		
		con = DriverManager.getConnection(url, un, pw);
		return con;
		}catch (SQLException e) {
			e.printStackTrace();
			return null;
			}
	}
	
	//Function to update hash table of usernames/sockets
	private static void updateHashTable () { 
		
		try { 
		
		Connection getHashed = dbConnect();
		
		//Defining the Statement and ResultSet holders
		Statement stmt;
		ResultSet rs; 
	
		stmt = getHashed.createStatement(); //
		rs = stmt.executeQuery("SELECT * from Users"); //Here is where the query goes that we would like to run.
		
		//Here is where we get the results
		while(rs.next()) { 
			String username = rs.getString("username"); //Grab the field from the database and set it to the String 'username'
			String hashedPassword = rs.getString("password"); //Grab the field from the database and set it to the String 'password'
			
			authentication.put(username, hashedPassword);
	    }
		
	     Enumeration name  = authentication.elements();
	     for (Enumeration OMFG = name; OMFG.hasMoreElements();) { 
	    	 System.out.println(OMFG.nextElement());
		}

	}catch(SQLException ex) {
		ex.printStackTrace();
	}
		
}
	public void mapUserSocket(String username, Socket userSocket) { 
		userToSocket.put(username, userSocket);
	}
	
	private void listen( int port ) throws IOException {
		// Create the ServerSocket
		ss = new ServerSocket( port );
		// Tell the world we're ready to go
		System.out.println( "Listening on "+ss );
		
		// Keep accepting connections forever 
		// This is true, but we need to make it such that the connection gets sent to the correct client -> client 
		while (true) {
			// Grab the next incoming connection
			Socket s = ss.accept();
			// Tell the world we've got it
			System.out.println( "Connection from "+s );
			// Create a DataOutputStream for writing data to the
			// other side
			
			// How are we going to send the DataOutputStream to the correct client?
			DataOutputStream dout = new DataOutputStream( s.getOutputStream() );
			
			// Save this stream so we don't need to make it again (Maybe here we can have an extra field in outputStreams 
			//Where we can define the client
			outputStreams.put( s, dout );
			
			// Create a new thread for this connection, and then forget
			// about it
			new ServerThread( this, s );
		}
	}
	
	// Get an enumeration of all the OutputStreams, one for each client connected to us
	Enumeration getOutputStreams() {
		return outputStreams.elements();
	}
	
	// Send a message to all clients (utility routine)
	/*void sendToAll( String message ) {
		// We synchronize on this because another thread might be
		// calling removeConnection() and this would screw us up
		// as we tried to walk through the list
		synchronized( outputStreams ) {
			// For each client ...
			for (Enumeration e = getOutputStreams(); e.hasMoreElements(); ) {
				// ... get the output stream ...
				DataOutputStream dout = (DataOutputStream)e.nextElement();
				// ... and send the message
				try {
					dout.writeUTF( message );
				} catch( IOException ie ) { System.out.println( ie ); }
			}
		}
	}*/
	
	// Remove a socket, and it's corresponding output stream, from our
	// list. This is usually called by a connection thread that has
	// discovered that the connection to the client is dead.
	void removeConnection( Socket s ) {
		// Synchronize so we don't mess up sendToAll() while it walks
		// down the list of all output streams
		synchronized( outputStreams ) {
			// Tell the world
			System.out.println( "Removing connection to "+s );
			// Remove it from our hashtable/list
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
	
	
	// Main routine
	// Usage: java Server <port>
	 public static void main( String args[] ) throws Exception {
		 /*Upon Starting Server
		 *1. UpdateHashTable
		 *2. Listen for connections 
		 */
		
		updateHashTable();
		
		// Get the port # from the command line
		int port = listenPort;
		
		 // Create a Server object, which will automatically begin accepting connections.
		new Server( port );

	}
}
