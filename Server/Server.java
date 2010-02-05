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

public class Server
{
	//This socket will accept new connection
	private ServerSocket ss;
	
	//This will be a table holding the usernames and hashed passwords pulled from the database
	//See: updateHashTable()
	public static Hashtable authentication = new Hashtable();
	
	//Creates a SQL connection object. See dbConnect()
	private static Connection con = null;
	
	//Defines which port on which we listen for client
	private static int listenPort = 7777;
	
	//A hashtable that keeps track of the outputStreams linked to each socket
	public Hashtable outputStreams = new Hashtable();

	//A hashtable mapping each user to a socket
	//used to find which stream to use to send data to clients
	public Hashtable userToSocket = new Hashtable();
	
	// Constructor. Starts listening on the defined port.
	public Server( int port ) throws IOException {
		listen( port );
	}
	
	//Connect to the database. Returns the connection it established, or null
	public static Connection dbConnect () { 

		//Location of the database
		String url = "jdbc:mysql://external-db.s72292.gridserver.com/db72292_athenaauth";
	
		//database username and password. shhhhh.
		String un = "db72292_athena"; //Database Username
		String pw = "xZN?uhwx"; //Database Password
		
		//Load the JDBC driver
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
	private static void updateHashTable () { 
		try { 
			//Use dbConnect() to connect to the database
			Connection getHashed = dbConnect();
		
			//Create a statement and resultset for the query
			Statement stmt;
			ResultSet rs; 
			
			//Return all usernames and hashes passwords
			stmt = getHashed.createStatement(); //
			rs = stmt.executeQuery("SELECT * from Users"); //Here is where the query goes that we would like to run.
		
			//Iterate through the results and write them to a hashtables
			while(rs.next()) { 
				String username = rs.getString("username");
				String hashedPassword = rs.getString("password");
			
				//Write username and hashed password to next element of hash table
				authentication.put(username, hashedPassword);
			}
		
			/*Debug code to print out all of the usernames in the hash table
			Enumeration name  = authentication.elements();
			for (Enumeration OMFG = name; OMFG.hasMoreElements();) { 
	    			 System.out.println(OMFG.nextElement());
			}*/

		}catch(SQLException ex) {
		ex.printStackTrace();
		}
		
	}

	//Adds a user (and the socket he is on) to the hashtable for later reference
	//Called after user authenticates
	public void mapUserSocket(String username, Socket userSocket) { 
		userToSocket.put(username, userSocket);
	}
	
	//Server listens for client connections, and passes them off to its own thread
	private void listen( int port ) throws IOException {
		// Create the ServerSocket
		ss = new ServerSocket( port );

		// Tell the world we're ready to go
		System.out.println( "Listening on "+ss );
		
		//Accept client connections forever
		while (true) {

			//Accept a new connection on the serversocket
			//Create a socket for it
			Socket s = ss.accept();

			//Debug text announcing a new connection
			System.out.println( "Connection from "+s );

			//DataOuputStream to send data from the client's socket
			DataOutputStream dout = new DataOutputStream( s.getOutputStream() );
			
			//Map the outputstream to the socket for later reference
			outputStreams.put( s, dout );
			
			//Handle the rest of the connection in the new thread
			new ServerThread( this, s );
		}
	}
	
	// Get an enumeration of all the OutputStreams.
	//TODO: This isn't really usefully without a sendToAll.
	//      sendToAll should be re-implemented for broadcast messages
	//	(for example, user sign-ons)
	Enumeration getOutputStreams() {
		return outputStreams.elements();
	}
	
	// Send a message to all clients (utility routine)
	//TODO: Reimplement to broadcast user logins.
	void sendToAll(String eventCode, String message ) {
		// We synchronize on this because another thread might be
		// calling removeConnection() and this would screw us up
		// as we tried to walk through the list
		synchronized( outputStreams ) {
			// For each client ...
			for (Enumeration e = getOutputStreams(); e.hasMoreElements(); ) {
				// ... get the output stream ...
				DataOutputStream dout = (DataOutputStream)e.nextElement();
				// ... and send the message
				try{
					dout.writeUTF(eventCode);
					dout.writeUTF( message );
				} catch( IOException ie ) { System.out.println( ie ); }
			}
		}
	}
	
	//Remove a socket once a client disconnects
	//TODO: Remove the entries from the hashtable so sending messages
	//	doesn't get confused
	void removeConnection( Socket s) {
		// Synchronize so we don't mess up sendToAll() while it walks
		// down the list of all output streams.
		synchronized( outputStreams ) {
			// Debug text
			System.out.println( "Removing connection to "+s );

			// Remove it from our hashtable/list
			// TODO: As above, remove from userToSocket, as well
			outputStreams.remove( s );
			// Make sure it's closed
			try {
				s.close();
				//Sending User Log off message after we close the socket
			} catch( IOException ie ) {
				System.out.println( "Error closing "+s );
				ie.printStackTrace();
			}
		}
	}

	//Overloaded with the username	
	void removeConnection( Socket s, String username ) {
		// Synchronize so we don't mess up sendToAll() while it walks
		// down the list of all output streams.
		synchronized( outputStreams ) {
			// Debug text
			System.out.println( "Removing connection to "+s );

			// Remove it from our hashtable/list
			// TODO: As above, remove from userToSocket, as well
			outputStreams.remove( s );
			userToSocket.remove(username);
			// Make sure it's closed
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
		*3. The Universe collapses in on itself
		*/
		
		//Read all usernames and hashed passwords into hashtable from database
		updateHashTable();
		
		// Get the port # from the command line
		// TODO: This doesn't actually do anything
		int port = listenPort;
		
		 // Create a Server object, which will automatically begin accepting connections.
		new Server( port );

	}
}
