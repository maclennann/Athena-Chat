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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;
import java.lang.Thread;
import java.lang.Runnable;
//XML Imports
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
//import com.sun.xml.internal.txw2.Document;
import org.w3c.dom.*;


public class Client
{
	//Global username variable
	private static String username;
	private static String toUser;
	
	//Created GUI builder object
	public static ClientApplet clientResource;
	
	//TODO: Make otherUsers array read from buddylist.xml

	// The socket connecting us to the server
	public static Socket socket;
	
	// The datastreams we use to move data through the socket
	private static DataOutputStream dout;
	private static DataInputStream din;
	
	static Thread listeningProcedure;
	static int connected=0;	

	//Exit the program
	public static void exit(){
		System.exit(0);
	}

	//Called from the actionListener on the tf textfield
	//User wants to send a message
	public static void processMessage( String message ) {
		try {
			//Get user to send message to from ComboBox
			toUser = clientResource.userBox.getSelectedValue().toString();
			
			//Send recipient's name and message to server
			dout.writeUTF(toUser);
			dout.writeUTF(message);
			
			// Append own message to IM window
			clientResource.mainConsole.append(username + ": " + message + "\n");
			
			// Clear out text input field
			clientResource.tf.setText( "" );
		} catch( IOException ie ) { System.out.println( ie ); }
	}
	
	//This method will be used to add to the buddy list
	private static void buddyList(String usernameToAdd) throws Exception {

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
		//Right now it completely overwrites.
		
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
	public static void recvMesg(DataInputStream din){
		try{
			// Who is the message from? 
			String fromUser = din.readUTF();
			// What is the message?
			String message = din.readUTF();
			
			// Print it to our text window
			clientResource.mainConsole.append( fromUser + ": " + message+"\n" );
		}catch ( IOException ie ) {
			//If we can't use the inputStream, we probably aren't connected
			 connected=0; 
		}
	}

	// Method to connect the user
	public static void connect() { 
		//Try to connect with and authenticate to the socket
		try {
			//Connect to auth server at defined port over socket
			socket = new Socket( "192.168.1.4", 7777 );
			
			//Get the username and password for the user for authentication
			//This should be in it's own fancy window
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
			connected=1;
			//Thread created to listen for messages coming in from the server
			listeningProcedure = new Thread(
				new Runnable() {
					public void run() {
						while(true) {
							if(connected==1)Client.recvMesg(din);
		      				}
		  	}});	

			listeningProcedure.start();
		} catch( IOException ie ) { System.out.println( ie ); }
	}
	
	// Method to disconnect
	public static void disconnect() { 
		try{
			socket.close();
			dout.close();
			din.close();
			connected=0;
			listeningProcedure.interrupt();
		}catch(Exception e){}
	}

	// Background thread runs this: show messages from other window
	public static void main(String[] args) {
	
		clientResource = new ClientApplet();
	//	clientResource.setVisible(true);

//THIS SHOULD BE A METHOD		
				//try {
		/*	
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
		*/	
//THAT SHOULD BE A METHOD

			//Receive messages until something breaks or we disconnect
	//		while (true) {
	//			recvMesg(din);
	//		}
	//		}

	//	}// catch( IOException ie ) { System.out.println( ie ); } 
	//	catch (Exception e) { System.out.println(e); }
		
	}
}
