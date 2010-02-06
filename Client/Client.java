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
import javax.swing.*;

//Making the XML buddy list file
import javax.xml.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import com.sun.xml.internal.txw2.Document;

public class Client extends Panel implements Runnable
{
	//Global username variable
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
			new Thread(this).start();

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
	
	//This method will be used to add to the buddy list
	private void buddyList() throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		org.w3c.dom.Document buddyListDoc = docBuilder.parse("./buddylist.xml");
		
		//Add an attribute to the buddylist node
		Node buddyListNode = buddyListDoc.createElement("username");
		buddyListNode.setTextContent("xanasasis13");
		
		//Write the XML to the file!
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		//Initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(buddyListDoc);
		transformer.transform(source, result);
		
		//Output to file
		String xmlString = result.getWriter().toString();
		System.out.println(xmlString);
		
	}


	// Background thread runs this: show messages from other window
	public void run() {
		try {
			//User chooses the remote client they want to talk to
			//TODO: Do this in a less-stupid way. Buddylist with all active sockets
			String toUser = JOptionPane.showInputDialog("Please input the user you want to talk to!");			
			dout.writeUTF(toUser);
			
			//Add user to your buddy list?
			String answer = JOptionPane.showInputDialog("Do you want to add a user to your buddy list?");
			if (answer.equals("yes")) {
				String usernameToAdd = JOptionPane.showInputDialog("Enter the username");
				buddyList(usernameToAdd);
			}
			
			
			
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
