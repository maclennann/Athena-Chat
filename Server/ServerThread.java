import java.io.*;
import java.sql.*;
import java.io.*;
import java.net.*;

public class ServerThread extends Thread
{
	//Define the MySQL connection
	private static Connection con = null;
	
	// The Server that spawned us
	private static Server server;
	// The Socket connected to our client
	private static Socket socket;
	
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
			
			System.out.println(login(username, password));
			
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
			
			//Create the connection from 
			Connection con = server.dbConnect();
		}
	}
	
	//Deprecated
	public void emo () { 
		this.destroy();			
	}
	
	public static String login (String clientName, String clientPassword) { 
		try { 
			//Defining the Statement and ResultSet holders
			Statement stmt;
			ResultSet rs; 
		
			stmt = con.createStatement(); //
			rs = stmt.executeQuery("SELECT * from Users ORDER BY user_id"); //Here is where the query goes that we would like to run.
			System.out.println("\nResults are:"); //Don't need this, just for debug.
		
			//Here is where we get the results
			while(rs.next()) { 
				String hashedPassword = server.authentication.get(clientName).toString(); //Grabbing the HashedPassword from the Database
				
				//Here is where we find if the User's Inputed information is correct
				if (clientPassword.equals(hashedPassword)) { 
					//Run some command that let's user log in!
					String returnMessage = "You're logged in!!!!";
					return returnMessage;
				}else { 
					server.removeConnection(socket);
					return "Login Failed"; }
		}
		
		stmt.close();
		
		}catch ( SQLException e) { 
				e.printStackTrace ( );
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
