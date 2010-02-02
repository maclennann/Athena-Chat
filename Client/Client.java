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
 * File: Client.java
 * 
 * This is where the detailed description of the file will go.
 *
 ****************************************************/
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;


public class Client extends Panel implements Runnable
{
	//Global username variabl
	String username;
	
	//TODO: GUI components and window for username/password
	
	// Components for the visual display of the chat windows
	private TextField tf = new TextField();
	private TextArea ta = new TextArea();
	
	
	// The socket connecting us to the server
	public Socket socket;
	
	// The datastreams we use to move data through the socket
	private DataOutputStream dout;
	private DataInputStream din;
	
	// Constructor
	public Client( String host, int port ) {
		
		//Simple window layout for IM session.
		//TODO: Better GUI.
		setLayout( new BorderLayout() );
		add( "North", tf );
		add( "Center", ta );
		
		//ActionListener on the 'tf' text field will send messages the user wants to send.
		tf.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				processMessage( e.getActionCommand() );
			}
		} );

		//Try to connect with and authenticate to the socket
		try {
			//Connect to auth server at defined port over socket
			socket = new Socket( host, port );
			
			//Get the username and password for the user for authentication
			username = JOptionPane.showInputDialog("Please enter your username");
			String password = JOptionPane.showInputDialog("Please enter your password");
						
			//Connection established debug code.
			System.out.println( "connected to "+socket );

			//Bind the datastreams to the socket in order to send/receive
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );
			
			//Send username and password over the socket for authentication
			dout.writeUTF(username); //Sending Username
			dout.writeUTF(password); //Sending Password
			
			// Start a background thread for receiving messages
			// See the 'run()' method
			new Thread( this ).start();

		} catch( IOException ie ) { System.out.println( ie ); }
	}

	//Called from the actionListener on the tf textfield
	//User wants to send a message
	private void processMessage( String message ) {
		//Try to send the message
		try {
			// Send it to the server
			dout.writeUTF( message );
			
			// Append own message to IM window
			ta.append(username + ": " + message + "\n");
			
			// Clear out text input field
			tf.setText( "" );
		//Catch the IOException
		} catch( IOException ie ) { System.out.println( ie ); }
	}

	// Background thread runs this: show messages from other window
	public void run() {
		try {
			//User chooses the remote client they want to talk to
			//TODO: Do this in a less-stupid way. Buddylist with all active sockets
			String toUser = JOptionPane.showInputDialog("Please input the user you want to talk to!");			
			dout.writeUTF(toUser);
			
			//While the thread is running and the socket is open
			while (true) {
				// Who is the message from? 
				String fromUser = din.readUTF();
				// What is the message?
				String message = din.readUTF();
				// Print it to our text window
				ta.append( fromUser + ": " + message+"\n" );
			}
		} catch( IOException ie ) { System.out.println( ie ); }
	}
}
