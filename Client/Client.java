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
 * File: Client.java
 * 
 * This is where the detailed description of the file will go.
 *
 ****************************************************/

import java.applet.*;
import java.awt.Color;
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
import java.util.Enumeration;

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
//TODO: Linux doesn't seem to have this library
//What should we do with this then?
//import com.sun.xml.internal.txw2.Document;
import org.w3c.dom.*;


public class Client
{
	//Print debug messages?
	static int debug=0;

	//Global username variable
	private static String username="null";

	//Recipient for message
	private static String toUser;
	
	//Client's GUI
	public static ClientApplet clientResource;
	
	//TODO: Make otherUsers array read from buddylist.xml

	// The socket connecting us to the server
	public static Socket socket;
	
	// The datastreams we use to move data through the socket
	private static DataOutputStream dout;
	private static DataInputStream din;
	
	//Temporary object for the JPanel in a tab
	static MapTextArea print;

	//Thread that will be used to listen for incoming messages
	static Thread listeningProcedure;

	//If the client is connect to the server
	static int connected = 0;	

	//Exit the program
	public static void exit(){
		System.exit(0);
	}

	//Called from the actionListener on the tf textfield
	//User wants to send a message
	public static void processMessage( String message ) {	
		//Get user to send message to from active tab
		toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());
		
		//Get the JPanel in the active tab
		print = (MapTextArea)clientResource.tabPanels.get(toUser);
			
		//See if the user is logged in. If yes, send it. If no, error.
		if(username.equals("null")){
			print.writeToTextArea("Error: You are not connected!\n");
			print.moveToEnd();
			print.clearTextField();}
		else{
			//Print the message locally
			print.writeToTextArea(username+": ");
			print.writeToTextArea(message+"\n");

			//Send the message
			try{
				//Send recipient's name and message to server
				dout.writeUTF(toUser);
				dout.writeUTF(message);
				// Append own message to IM window
        			print.moveToEnd();
				// Clear out text input field
				print.clearTextField();
			} catch( IOException ie ) { 
				print.writeToTextArea("Error: You are not connected!\n");
				print.moveToEnd();
				print.clearTextField();
			}
		}
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
			
			//If the message is an unavailabe user response		
			if(fromUser.equals("UnavailableUser")){
				print = (MapTextArea)clientResource.tabPanels.get(message);
				print.writeToTextArea(fromUser+": ");
				print.writeToTextArea(message+"\n");
				return;
			}
			
			if(fromUser.equals("ServerLogOff")) { 
				clientResource.buddySignOff(message);
				return;
			}

			//If there isn't already a tab for the conversation, make one
			if(!clientResource.tabPanels.containsKey(fromUser)){
				clientResource.makeTab(fromUser);
			}

			//Write message to the correct tab
			print = (MapTextArea)clientResource.tabPanels.get(fromUser);
			print.setTextColor(Color.blue);
			print.writeToTextArea(fromUser+": ");
			print.setTextColor(Color.black);
			print.writeToTextArea(message+"\n");
			print.moveToEnd();
		}catch ( IOException ie ) {
			//If we can't use the inputStream, we probably aren't connected
			connected=0; 
		}
	}

	// Method to connect the user
	public static void connect() { 
		//Try to connect with and authenticate to the socket
		try {
			try{
			//Connect to auth server at defined port over socket
			socket = new Socket( "192.168.1.117", 7777 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}
			JPasswordField passwd = new JPasswordField();
			String password="";
			//Get the username and password for the user for authentication
			//TODO This should be in it's own fancy window
			username = JOptionPane.showInputDialog("Please enter your username");
			int action = JOptionPane.showConfirmDialog(null, passwd,"Enter Password",JOptionPane.OK_CANCEL_OPTION);  
			if(action < 0)JOptionPane.showMessageDialog(null,"Cancel, X or escape key selected");  
			else password = new String(passwd.getPassword());	
			
			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+socket );
			JOptionPane.showMessageDialog(null,"Connection Established!","Success!",JOptionPane.INFORMATION_MESSAGE);

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
						//While we are connected to the server, receive messages
						while(connected ==1) {
							Client.recvMesg(din);
		      				}
		  	}});	
			//Instanciate Buddy List
			instanciateBuddyList();
			
			//Start the thread
			listeningProcedure.start();
			

		} catch( IOException ie ) { System.out.println( ie ); }
	}
	
	// Disconnect from the server
	public static void disconnect() { 
		try{
			socket.close();
			dout.close();
			din.close();
			connected=0;
		}catch(Exception e){}
	}

		
	//TODO: Make buddylist actually work
	public static void addBuddy(){
		try {
			
			//Add user to your buddy list?
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
			

		} catch( IOException ie ) { System.out.println( ie ); } 
		catch (Exception e) { System.out.println(e); }
		
	}

	// Startup method to initiate the buddy list
	public static void instanciateBuddyList() { 
		//Check entire buddylist and fill hashtable with user online statuses
		for (int i=0; i < clientResource.otherUsers.length; i++) { 
			System.out.println("Current Buddy To Check: " + clientResource.otherUsers[i]);
			checkUserStatus(clientResource.otherUsers[i]);
		}
		//Counter
		int x=0;
		//Loop through the HashTable of available users and place them in the JList
		for (Enumeration e = clientResource.userStatus.keys(), f = clientResource.userStatus.elements(); x < clientResource.userStatus.size(); x++ ) {
				try { 
					String currentE = e.nextElement().toString();
					System.out.println("E: " + currentE);
				
					String currentF = f.nextElement().toString();
					System.out.println("F: " + currentF);
					if (currentF.equals("1")) { 
						System.out.println("Online user:" + currentE);
						clientResource.newBuddyListItems(currentE);
					}
				} catch (java.util.NoSuchElementException ie) { } 
		}
	}
	
	public static void checkUserStatus(String findUserName) {
		try { 
			//Initalize Result
			int result = -1;
			//Run the systemMessage Method to let Aegis know what we're about to do
			//First contact with Aegis!
			systemMessage("001");
			//Listen for the incoming Acknowledge message
			System.out.println("Message received from server: " + din.readUTF().toString());
			//Go ahead and send Aegis the user name we want to find 
			dout.writeUTF(findUserName);
			System.out.println("Username sent - now listening for result...");
			//Grab result
			result = Integer.parseInt(din.readUTF());
			//Print result 
			System.out.println("Result fo user " + findUserName + " is " + result + ".");
			//Call the mapUserStatus method in ClientApplet to fill the Hashtable of user's statuses
			clientResource.mapUserStatus(findUserName, result);
			} catch (java.io.IOException e) { 
		}	}	
	
	//Use this method if Contact with Aegis is needed
	public static void systemMessage( String message ) {	
		//Send the message
		try{
			//Send recipient's name and message to server
			dout.writeUTF("Aegis");
			dout.writeUTF(message);
		} catch( IOException ie ) {
		}
}
	// Create the GUI for the client.
	public static void main(String[] args) {
	
		clientResource = new ClientApplet();
	}
}