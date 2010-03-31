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

public class Client
{
	//Print debug messages?
	public static int debug=1;

	//Global username variable
	private static String username="null";

	//Recipient for message
	private static String toUser;

	//Client's GUI
	public static ClientApplet clientResource;
	public static ClientLogin loginGUI;

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
		if (debug == 1) System.out.println("USERNAME: " + username);
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
				print.writeToTextArea("Error: You are not connfected!\n");
				print.moveToEnd();
				print.clearTextField();
			}
		}
	}

	//This method will be used to add to the buddy list
	public static void buddyList(String usernameToAdd) throws Exception {		
		//Add the username to a new line in the file
		//Will take in more inputs as we add other functionality to Athena like Pubkey, group, etc
		int exists=0;
		try { 
			String[] usernames = returnBuddyListArray();

			for(int y=0;y<usernames.length;y++) {
				if(usernames[y].equals(usernameToAdd)) {
					exists=1;
				}
			}
			if(exists == 0) { //Then the username is not in the buddylist, let's add it!
				BufferedWriter out = new BufferedWriter(new FileWriter("buddylist.csv", true)); 
				out.write(usernameToAdd + "," + "\n");
				out.close();	
			}
		} catch (IOException e) { } 
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
			//Remove user from Buddylist

			if(fromUser.equals("ServerLogOff")) { 
				String[] usernames = returnBuddyListArray();
				for(int x=0;x<usernames.length;x++) {
					if(usernames[x].equals(message)) { 
						//We know that the buddy is in his/her buddy list! 
						clientResource.buddySignOff(message);
					}
				}
				return;
			}

			//Create buddy list entry for user sign on
			if(fromUser.equals("ServerLogOn")) {
				if(!(message.equals(username))) 	{
					String[] usernames = returnBuddyListArray();
					for(int x=0;x<usernames.length;x++) {
						if(usernames[x].equals(message)) { 
							//We know that the buddy is in his/her buddy list! 
							clientResource.newBuddyListItems(message);
						}
					}
					return;

				}
			}
			else { // Need this else in order to hide the system messages coming from Aegis

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
			}
		}catch ( IOException ie ) {
			//If we can't use the inputStream, we probably aren't connected
			connected=0; 
		}
	}

	// Method to connect the user
	public static void connect(String username, String password) { 
		//Try to connect with and authenticate to the socket
		try {
			try{
				//Connect to auth server at defined port over socket
				socket = new Socket( "aegis.athenachat.org", 7777 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+socket );
			JOptionPane.showMessageDialog(null,"Connection Established!","Success!",JOptionPane.INFORMATION_MESSAGE);

			//Bind the datastreams to the socket in order to send/receive
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );

			//Send username and password over the socket for authentication
			//FOR NOW MAKE A NEW STRING OUT OF THE CHAR[] BUT WE NEED TO HASH THIS!!!! 
			//String plainTextPassword = new String(password);
			System.out.println(password);
			dout.writeUTF(username); //Sending Username
			dout.writeUTF(password); //Sending Password
			connected=1;
			clientResource = new ClientApplet();
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

	// Method to connect the user
	public static void connect() { 
		//Try to connect with and authenticate to the socket
		try {
			try{
				//Connect to auth server at defined port over socket
				socket = new Socket( "aegis.athenachat.org", 7777 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+socket );
			JOptionPane.showMessageDialog(null,"Connection Established!","Success!",JOptionPane.INFORMATION_MESSAGE);

			//Bind the datastreams to the socket in order to send/receive
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );

		} catch( IOException ie ) { System.out.println( ie ); }
	}

	// Disconnect from the server
	public static void disconnect() { 
		try{
			socket.close();
			dout.close();
			din.close();
			connected=0;
			clientResource.setVisible(false);
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
	//TODO Make sure the user's status gets changed when they sign on/off
	public static void instanciateBuddyList() throws IOException { 		
		String[] usernames = returnBuddyListArray();

		//Check entire buddylist and fill hashtable with user online statuses
		for (int i=0; i < usernames.length; i++) { 
			System.out.println("Current Buddy To Check: " + usernames[i]);
			checkUserStatus(usernames[i]); 
			try {
				//buddyList(usernames[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Let's try to make the buddylist.xml file
		}
		//Counter
		int y=0;
		//Loop through the HashTable of available users and place them in the JList
		for (Enumeration e = clientResource.userStatus.keys(), f = clientResource.userStatus.elements(); y < clientResource.userStatus.size(); y++ ) {
			try { 
				String currentE = e.nextElement().toString();
				System.out.println("E: " + currentE);

				String currentF = f.nextElement().toString();
				System.out.println("F: " + currentF);

				if (currentF.equals("1")) { 
					System.out.println("Online user:" + currentE);
					clientResource.newBuddyListItems(currentE);						
				}
			} catch (java.util.NoSuchElementException ie) { } catch (Exception eix) {
				// TODO Auto-generated catch block
				eix.printStackTrace();
			} 
		}

		//Send Message to Aegis letting it know we're logged in
		systemMessage("002");
	}
	
	// Startup method to initiate the buddy list
	//TODO Make sure the user's status gets changed when they sign on/off
	public static void instanciateBuddyList(String username) throws IOException {
		String[] usernames = returnBuddyListArray();
		
		for(int x=0; x<usernames.length;x++) { 
			if(usernames[x].equals(username)) {
				//Name exists! Exit!!
				return;
			}
		}
		//Counter
		int y=0;
		//Loop through the HashTable of available users and place them in the JList
		for (Enumeration e = clientResource.userStatus.keys(), f = clientResource.userStatus.elements(); y < clientResource.userStatus.size(); y++ ) {
			try { 
				String currentE = e.nextElement().toString();
				System.out.println("E: " + currentE);

				String currentF = f.nextElement().toString();
				System.out.println("F: " + currentF);

				if (currentE.equals(username) && currentF.equals("1")) { 
					System.out.println("Online user:" + currentE);
					clientResource.newBuddyListItems(currentE);						
				}
				else {
					//offline user
				}
			} catch (java.util.NoSuchElementException ie) { } catch (Exception eix) {
				// TODO Auto-generated catch block
				eix.printStackTrace();
			} 
		}

		//Send Message to Aegis letting it know we're logged in
		systemMessage("002");
	}


	public static String[] returnBuddyListArray() throws IOException { 
		//Let's get the number of lines in the file
		InputStream is = new BufferedInputStream(new FileInputStream("buddylist.csv"));
		byte[] c = new byte[1024];
		int count = 0;
		int readChars = 0;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {
				if (c[i] == '\n')
					++count;
			}
		}
		System.out.println(count);
		String[] usernames = new String[count];

		if (count == 0) { 
			//We know that the user has no buddies!
			return usernames;
		}
		else { 

			BufferedReader in = new BufferedReader(new FileReader("buddylist.csv")); 
			int x=0;
			String str; 
			while ((str = in.readLine()) != null) 
			{ 
				String foo[] = str.split(","); 
				usernames[x] = foo[0];
				x++;
			}
			return usernames;
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
			System.out.println("Result for user " + findUserName + " is " + result + ".");
			//Call the mapUserStatus method in ClientApplet to fill the Hashtable of user's statuses
			clientResource.mapUserStatus(findUserName, result);

		} catch (java.io.IOException e) { 
		}	
	}	

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
	public static void setUsername(String usernameToSet) { 
		username = usernameToSet;
	}
	//This method returns a DOUT for other classes to use
	public static DataOutputStream returnDOUT() { 
		return dout;
	}
	// Create the GUI for the client.
	public static void main(String[] args) throws AWTException {

		//clientResource = new ClientApplet();
		loginGUI = new ClientLogin();
	}
}
