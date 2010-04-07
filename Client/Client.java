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

import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

import java.lang.Thread;
import java.lang.Runnable;
import java.util.Enumeration;
import java.math.BigInteger;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.applet.*;
import  sun.audio.*;    //import the sun.audio package
import  java.io.*;



public class Client
{
	//DESCrypto object for cryptography of local files
	static DESCrypto descrypto;
	


	//Print debug messages?
	public static int debug=1;

	//Global username variable
	private static String username="null";
	
	//Splash Screen
	static ClientSplash screen;

	//Recipient for message
	private static String toUser;

	//Client's GUI
	public static ClientApplet clientResource;
	public static ClientLogin loginGUI;

	//TODO: Make otherUsers array read from buddylist.xml
	//DONE 3/30/2010

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
	
	public static String userNameToCheck = null;
	
	//Aegis' public key
	static RSAPublicKeySpec serverPublic;
	
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

		//Set exists to 0, this means that the usernameToAdd is not already in the buddylist file
		int exists=0;
		BufferedWriter out;
		try {
			//Call the returnBuddyListArray method, this reads in the buddylist file and puts the user names into an array for us to use
			String[] usernames = returnBuddyListArray();

			//This for loop checks to see if the usernameToAdd is already in the buddylist file, if so, set exists to 1
			for(int y=0;y<usernames.length;y++) {
				if(usernames[y].equals(usernameToAdd)) {
					exists=1;
				}
			}

			//If the usernameToAdd IS NOT in the buddylist file, add it
			if(exists == 0) { 
				BigInteger encryptedUsername;
				//Append to the file the usernameToAdd
				File newFile = new File("users/" + username + "/buddylist.csv");
				if(!(newFile.exists())) { 
					boolean success = new File("users/" + username).mkdirs();
					if(success) { 
						newFile.createNewFile();
						out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));
					}
					else { 
						newFile.createNewFile();
					}
				}
				out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv", true));
				encryptedUsername = new BigInteger(descrypto.encryptData(usernameToAdd.concat(",")));
				out.write(encryptedUsername + "\n");
				out.close();	
			}
		}
		catch (IOException e) { } 
	} 

	static void writeBuddyListToFile(String[] buddyList){
		BigInteger encryptedUsername;
		BufferedWriter out;
		File newFile = new File("users/" + username + "/buddylist.csv");
		try{
			if(!(newFile.exists())) { 
				boolean success = new File("users/" + username).mkdirs();
				if(success) { 
					newFile.createNewFile();
				}
				else { 
					newFile.createNewFile();
				}
			}
			out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));

			for(int i = 0; i < buddyList.length;i++){
				encryptedUsername = new BigInteger(descrypto.encryptData(buddyList[i].concat(",")));
				out.write(encryptedUsername + "\n");
			}
			out.close();
		}catch(Exception e)
		{System.out.println("ERROR WRITING BUDDYLIST");
		}
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
				//Check to see if the user is in your buddy list, if not, don't care
				String[] usernames = returnBuddyListArray();
				for(int x=0;x<usernames.length;x++) {
					if(usernames[x].equals(message)) { 
						//We know that the buddy is in his/her buddy list! 
						clientResource.buddySignOff(message);
						//** add this into your application code as appropriate
						// Open an input stream  to the audio file.
						InputStream in = new FileInputStream("sounds/signOff.wav");
						// Create an AudioStream object from the input stream.
						AudioStream as = new AudioStream(in);         
						// Use the static class member "player" from class AudioPlayer to play
						// clip.
						AudioPlayer.player.start(as);   
					}
				}
         
				return;
			}
			if(fromUser.equals("CheckUserStatus"))
			{
				System.out.println(message);
				dout.writeUTF(userNameToCheck);
				return;
			}
			if(fromUser.equals("CheckUserStatusResult"))
			{
				int result = Integer.parseInt(message);
				clientResource.mapUserStatus(userNameToCheck, result);
				if (result == 1)
				{
					clientResource.newBuddyListItems(userNameToCheck);						
				}
				return;
			}

			//Create buddy list entry for user sign on
			if(fromUser.equals("ServerLogOn")) {
				if(!(message.equals(username))) 	{
					//Check to see if the user is in your buddylist, if not, don't care
					String[] usernames = returnBuddyListArray();
					for(int x=0;x<usernames.length;x++) {
						if(usernames[x].equals(message)) { 
							//We know that the buddy is in his/her buddy list! 
							clientResource.newBuddyListItems(message);
							//** add this into your application code as appropriate
							// Open an input stream  to the audio file.
							InputStream in = new FileInputStream("sounds/signOn.wav");
							// Create an AudioStream object from the input stream.
							AudioStream as = new AudioStream(in);         
							// Use the static class member "player" from class AudioPlayer to play
							// clip.
							AudioPlayer.player.start(as);   
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
	public static void connect(String user_name, String password) throws InterruptedException, AWTException { 
		//Try to connect with and authenticate to the socket
		username = user_name;
		try {
			try{
				//Connect to auth server at defined port over socket
				socket = new Socket( "71.234.132.9", 7777 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Create the DESCrypto object for buddylist and preferences cryptography
			String saltUser;
			if(username.length()>=8){
				saltUser = username.substring(0,8);
			}else saltUser = username;
			descrypto = new DESCrypto(password,saltUser);

			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+socket );


			//Bind the datastreams to the socket in order to send/receive
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );

			//Send username and password over the socket for authentication
			//FOR NOW MAKE A NEW STRING OUT OF THE CHAR[] BUT WE NEED TO HASH THIS!!!! 
			//String plainTextPassword = new String(password);
			System.out.println(password);
			dout.writeUTF(username); //Sending Username
			dout.writeUTF(password); //Sending Password
			String result = din.readUTF();
			System.out.println(result);
			if(result.equals("Failed")) { 
				ClientLoginFailed loginFailed = new ClientLoginFailed();
			}
			else { 
				connected=1;
				
				//Read in the server's public key for encryption of headers
				serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");
				
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
				instantiateBuddyList();	
				//Start the thread
				listeningProcedure.start();
			}
		} catch( IOException ie ) { System.out.println( ie ); }
	}

	// Method to connect the user
	public static void connect() { 

		//Try to connect with and authenticate to the socket
		try {
			try{
				//Connect to auth server at defined port over socket
				socket = new Socket( "71.234.132.9", 7777 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+socket );

			//Bind the datastreams to the socket in order to send/receive
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );
			
			//Read in server's public key for encryption of headers
			serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");

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


	// Startup method to initiate the buddy list
	//TODO Make sure the user's status gets changed when they sign on/off
	//DONE 3/30/2010
	public static void instantiateBuddyList() throws IOException { 	
		//Grab string array of the buddylist.csv file 
		String[] usernames = returnBuddyListArray();

		//Check entire buddylist and fill hashtable with user online statuses
		for (int i=0; i < usernames.length; i++) { 
			System.out.println("Current Buddy To Check: " + usernames[i]);
			checkUserStatus(usernames[i]);
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

				//If the user is online, add them to your buddylist
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
	/*
	 * @Overloaded
	 * This method is called when adding a user to ones buddy list, this immediately checks to see if the inputted user is online
	 */
	public static void instantiateBuddyList(String usernameToCheck) throws IOException {

			System.out.println("Current Buddy To Check: " + usernameToCheck);
			checkUserStatus(usernameToCheck, "PauseThread!");
	}

	//This method returns a nice string array full of the usernames (for now) that are in the buddylist file
	//TODO Make this return a multi-dementional array of all the fields in the CSV File
	public static String[] returnBuddyListArray() throws IOException {
		int count;
		int readChars;
		InputStream is;

		//Let's get the number of lines in the file
		File newFile = new File("users/" + username + "/buddylist.csv");
		if(!(newFile.exists())) { 
			boolean success = new File("users/" + username).mkdirs();
			if(success) { 
				newFile.createNewFile();
				is = new BufferedInputStream(new FileInputStream("./users/" + username + "/buddylist.csv"));
			}
			else { 
				newFile.createNewFile();
			}
		}

		is = new BufferedInputStream(new FileInputStream("./users/" + username + "/buddylist.csv"));
		byte[] c = new byte[1024];
		count = 0;
		readChars = 0;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {
				if (c[i] == '\n')
					++count;
			}
		} //End section

		//Make the string array the size of the number of lines in the file
		String[] usernames = new String[count];

		//If there are no lines in the file we know that the user has no buddies! :(
		if (count == 0) { 
			return usernames;
		}
		else { 
			File newFile2 = new File("users/" + username + "/buddylist.csv");
			if(!(newFile2.exists())) { 
				newFile2.createNewFile();
			}
			BufferedReader in = new BufferedReader(new FileReader("./users/" + username + "/buddylist.csv")); 
			int x=0;
			String raw, str;
			BigInteger strNum;

			//Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
			while ((raw = in.readLine()) != null) 
			{ 
				// Read in the BigInteger in String form. Turn it to a BigInteger
				// Turn the BigInteger to a byteArray, and decrypt it.
				strNum = new BigInteger(raw);
				str = descrypto.decryptData(strNum.toByteArray());

				String foo[] = str.split(","); 
				usernames[x] = foo[0];
				x++;
			}
			return usernames;
		}

	}

	public static void checkUserStatus(String findUserName) {
		try { 
			System.out.println("Checking availability for user: "+findUserName);
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
			System.out.println("HAIII");

		} catch (Exception e) { 
			System.out.println(e);
		}	
	}
	public static void checkUserStatus(String findUserName, String checkStatusFlag) {
		try {
			userNameToCheck = findUserName;
			//DataInputStream din = new DataInputStream(socket.getInputStream());
			System.out.println("Checking availability for user: "+findUserName);
			//Initialize Result
			int result = -1;
			//Run the systemMessage Method to let Aegis know what we're about to do
			//First contact with Aegis!
			systemMessage("003");
			
			System.out.println("HAIII");
			//listeningProcedure.start();

		} catch (Exception e) { 
			System.out.println(e);
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
	  private static void splashScreenDestruct() {
		    screen.setScreenVisible(false);
		  }

		  private static void splashScreenInit() {
		    ImageIcon myImage = new ImageIcon(("splash.jpg"));
		    screen = new ClientSplash(myImage);
		    screen.setLocationRelativeTo(null);
		    screen.setProgressMax(100);
		    screen.setScreenVisible(true);
		  }
	
	// Create the GUI for the client.
	public static void main(String[] args) throws AWTException {
		splashScreenInit();

		 for (int i = 0; i <= 100; i++)
		    {
		      for (long j=0; j<50000; ++j)
		      {
		        String takeMoreTime = " " + (j + i);
		      }
		      screen.setProgress("Loading:" + i, i);  // progress bar with a message
		    }   
			splashScreenDestruct();
		//clientResource = new ClientApplet();
		loginGUI = new ClientLogin();
		
	}
}
