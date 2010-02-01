// $Id$
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class ServerThread extends Thread
{
	//Define the MySQL connection
	private static Connection con = null;
	
	// The Server that spawned us
	private Server server;
	// The Socket connected to our client
	private Socket socket;
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
			
			login(username, password);
			
			while (true) {
				//... read the next message ...
				String message = din.readUTF();
				//... tell the world ...
				System.out.println( "Sending "+message );
				//... and have the server send it to all clients
				server.sendToAll( message );
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
	
	public static String login (String userName, String password) { 
		try { 
			//Debug
			System.out.println("1");
			
		//Here we will have to have some mysql code to verify with our Database that their username is correct.
		//JDBC URL for the database
		String url = "jdbc:mysql://external-db.s72292.gridserver.com/db72292_athenaauth";
		//Defining the Statement and ResultSet holders
		Statement stmt;
		ResultSet rs; 
		
		//Using the forName method to load the appropriate driver for JDBC
		Class.forName("com.mysql.jdbc.Driver");
		
		String un = "db72292_athena"; //Database Username
		String pw = "12345678"; //Database Password
		
		//Here is where the connection is made
		con = DriverManager.getConnection(url, un, pw);
		
		
		stmt = con.createStatement(); //
		rs = stmt.executeQuery("SELECT * from Users ORDER BY user_id"); //Here is where the query goes that we would like to run.
		System.out.println("\nResults are:"); //Don't need this, just for debug.
		
		//Here is where we get the results
		while(rs.next()) { 
			String username = rs.getString("username"); //Grab the field from the database and set it to the String 'username'
			String hashedPassword = rs.getString("password"); //Grab the field from the database and set it to the String 'password'
			
			//System.out.print(" key= " + username + someInputedUsername);
			//System.out.print(" str= " + hashedPassword + someInputedHashedPassword);
			//System.out.print("\n");
			
			//Here is where we find if the User's Inputed information is correct
			if ((userName.equals(username)) && (password.equals(hashedPassword))) { 
				//Run some command that let's user log in!
				String returnMessage = "You're logged in!!!!";
				return returnMessage;
				}
			//else { return "Failed"; }
			}
				stmt.close();		
		}catch ( SQLException e) { 
				e.printStackTrace ( );
		}
		catch ( ClassNotFoundException h) { 
				h.printStackTrace();
		}
		finally { 
			if( con != null) { 
				try { con.close( ); }
				catch( Exception e) { } 
			}
		}
		return "Failed";
	}
}
