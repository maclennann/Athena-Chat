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
			
			
			//Create the connection from 
			Connection con = server.dbConnect();
			System.out.print("Connection established..");
			
			//Login!
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
		}
	}
	
	//Deprecated
	public void emo () { 
		this.destroy();			
	}
	
	public static String login (String clientName, String clientPassword) { 
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
