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
import javax.xml.transform.Result;
import javax.xml.transform.Source;
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
	String toUser;
	
	//TODO: GUI components and window for username/password
	
	//TODO: Make otherUsers array read from buddylist.xml
	String[] otherUsers = {"Norm", "Steve", "Greg"};
	
	// Components for the visual display of the chat windows
	private TextField tf = new TextField();
	private TextArea ta = new TextArea();
	private JComboBox userBox = new JComboBox(otherUsers);
	
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
		add( "South", userBox);
		
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
		try {
			//Get user to send message to from ComboBox
			toUser = userBox.getSelectedItem().toString();
			
			//Send recipient's name and message to server
			dout.writeUTF(toUser);
			dout.writeUTF(message);
			
			// Append own message to IM window
			ta.append(username + ": " + message + "\n");
			
			// Clear out text input field
			tf.setText( "" );
		} catch( IOException ie ) { System.out.println( ie ); }
	}
	
	//This method will be used to add to the buddy list
	private void buddyList(String usernameToAdd) throws Exception {

		//Using the DocumentBuilderFactory class, create a DocumentBuilder (docFactory)
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		//Use the DocumentBuilderFactory to instanciate a DocumentBuilder (docBuilder)
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		//use the DocumentBuilder object to create a Document (buddyListDoc)
		org.w3c.dom.Document buddyListDoc = docBuilder.newDocument();		
		
		//Create the Source input for the buddyListDoc
		Source source = new DOMSource(buddyListDoc);
		
		//Use the File class to create the new Buddy list file (buddylist.xml) - NOTE: File name will probably change
		//Perhaps it should be encrypted?
		//TODO: Encrypt buddylist.xml with user's public key?
		File buddyListfile = new File("./buddylist.xml");
		
		//Create StreamResult object for the buddyListFile
		Result result = new StreamResult(buddyListfile);
		
		//TODO: What if the root (and maybe some buddies) already exist.
		//		right now it completely overwrites.
		
		//Here is where we create the rootElement
		Element rootElement = buddyListDoc.createElement("buddylist");
	        buddyListDoc.appendChild(rootElement);
	        
		//Add an attribute to the buddylist node
	    Element buddyName = buddyListDoc.createElement("username");
	    buddyName.appendChild(buddyListDoc.createTextNode(usernameToAdd));
	    rootElement.appendChild(buddyName);
		
		//Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);
	}
		
	//When the client receives a message.
	public void recvMesg(DataInputStream din){
		try{
			// Who is the message from? 
			String fromUser = din.readUTF();
			// What is the message?
			String message = din.readUTF();
			
			// Print it to our text window
			ta.append( fromUser + ": " + message+"\n" );
		} catch( IOException ie ) { System.out.println( ie ); }
	}
	
	// Background thread runs this: show messages from other window
	public void run() {
		try {
			//User chooses the remote client they want to talk to
			//TODO: Do this in a less-stupid way. Buddylist with all active sockets
			//String toUser = JOptionPane.showInputDialog("Please input the user you want to talk to!");			
			//dout.writeUTF(toUser);
			
			//Add user to your buddy list?
			//TODO: Obviously, break this out into a method that can be called from a GUI action.
			String answer = JOptionPane.showInputDialog("Do you want to add a user to your buddy list?");
			if (answer.equals("yes")) {
				String usernameToAdd = JOptionPane.showInputDialog("Enter the username");
				buddyList(usernameToAdd);
			} else if (answer.equals("no")) { 
				JOptionPane.showMessageDialog(null, "Ok continuing..");
			}
			else {
				JOptionPane.showMessageDialog(null, "Wrong answer - try again.");
			}
			
			//Receive messages until something breaks or we disconnect
			while (true) {
				recvMesg(din);
			}

		} catch( IOException ie ) { System.out.println( ie ); } 
		catch (Exception e) { System.out.println(e); }
		
	}
}
